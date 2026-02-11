import json
import os
import subprocess
import sys
import tempfile

import librosa
import numpy as np


def analyze_audio_for_guitar_hero(audio_path, n_buttons=5):
    audio_path = os.path.abspath(audio_path)

    # If it's an MP3, convert to WAV using ffmpeg first
    if audio_path.lower().endswith(".mp3"):
        temp_wav = tempfile.NamedTemporaryFile(suffix=".wav", delete=False)
        temp_wav_path = temp_wav.name
        temp_wav.close()

        try:
            subprocess.run(
                [
                    "ffmpeg",
                    "-i",
                    audio_path,
                    "-acodec",
                    "pcm_s16le",
                    "-ar",
                    "44100",
                    "-y",
                    temp_wav_path,
                ],
                check=True,
                capture_output=True,
            )
            load_path = temp_wav_path
            cleanup_temp = True
        except subprocess.CalledProcessError as e:
            print(f"FFmpeg error: {e.stderr.decode()}")
            raise
    else:
        load_path = audio_path
        cleanup_temp = False

    try:
        y, sr = librosa.load(load_path)

        # Separate harmonic (melody) from percussive (drums)
        y_harmonic, y_percussive = librosa.effects.hpss(y, margin=3.0)

        # Get RMS energy to detect loud sections (like yelling)
        rms = librosa.feature.rms(y=y_harmonic)[0]
        rms_threshold = np.percentile(rms, 60)

        # Get predominant pitch using pyin
        f0, voiced_flag, voiced_probs = librosa.pyin(
            y_harmonic,
            fmin=float(librosa.note_to_hz("C2")),
            fmax=float(librosa.note_to_hz("C7")),
            frame_length=2048,
        )

        # Filter settings
        MELODY_MIN = 20
        MELODY_MAX = 1800
        MIN_CONFIDENCE = 0.40
        MIN_NOTE_DURATION = 0.05
        SUSTAINED_NOTE_INTERVAL = 0.25

        hop_length = 512
        frame_times = librosa.frames_to_time(
            np.arange(len(f0)), sr=sr, hop_length=hop_length
        )

        # Track current note
        current_note_start = None
        current_freq = None
        last_note_time = None
        notes = []
        valid_freqs = []

        for i, (freq, time) in enumerate(zip(f0, frame_times)):
            is_loud = rms[i] > rms_threshold
            confidence_threshold = (
                MIN_CONFIDENCE if not is_loud else MIN_CONFIDENCE * 0.85
            )

            is_valid = (
                voiced_flag[i]
                and not np.isnan(freq)
                and voiced_probs[i] > confidence_threshold
                and MELODY_MIN <= freq <= MELODY_MAX
            )

            if is_valid:
                # Check if this is a new note (significant pitch change or first note)
                if current_freq is None or abs(freq - current_freq) > 20:
                    # Save previous note if it was long enough
                    if current_note_start is not None:
                        duration = time - current_note_start
                        if duration >= MIN_NOTE_DURATION:
                            notes.append((current_note_start, current_freq))
                            valid_freqs.append(current_freq)
                            last_note_time = current_note_start

                    # Start new note
                    current_note_start = time
                    current_freq = freq

                # For sustained notes: add periodic notes if it's been a while
                elif (
                    last_note_time is not None
                    and (time - last_note_time) >= SUSTAINED_NOTE_INTERVAL
                ):
                    notes.append((time, current_freq))
                    valid_freqs.append(current_freq)
                    last_note_time = time

            else:
                # No valid pitch - end current note if exists
                if current_note_start is not None:
                    duration = time - current_note_start
                    if duration >= MIN_NOTE_DURATION:
                        notes.append((current_note_start, current_freq))
                        valid_freqs.append(current_freq)
                        last_note_time = current_note_start
                    current_note_start = None
                    current_freq = None

        # Don't forget the last note
        if current_note_start is not None:
            notes.append((current_note_start, current_freq))
            valid_freqs.append(current_freq)

        # Map frequencies to buttons
        if len(valid_freqs) > 0:
            sorted_freqs = np.sort(valid_freqs)
            min_freq = sorted_freqs[int(len(sorted_freqs) * 0.05)]
            max_freq = sorted_freqs[int(len(sorted_freqs) * 0.95)]

            result_notes = []
            for note_time, freq in notes:
                if freq <= min_freq:
                    button = 0
                elif freq >= max_freq:
                    button = n_buttons - 1
                else:
                    normalized = (freq - min_freq) / (max_freq - min_freq)
                    button = int(normalized * n_buttons)
                    button = min(button, n_buttons - 1)

                result_notes.append(
                    {
                        "time": float(note_time),
                        "button": int(button),
                        "frequency": float(freq),
                    }
                )
        else:
            result_notes = []
            min_freq = 0
            max_freq = 0

        tempo, _ = librosa.beat.beat_track(y=y, sr=sr)

        result = {
            "tempo": float(tempo) if np.isscalar(tempo) else float(tempo[0]),
            "duration": float(librosa.get_duration(y=y, sr=sr)),
            "notes": result_notes,
            "freq_range": {
                "min": float(min_freq) if len(valid_freqs) > 0 else 0,
                "max": float(max_freq) if len(valid_freqs) > 0 else 0,
            },
        }

        return result

    finally:
        if cleanup_temp and os.path.exists(load_path):
            os.remove(load_path)


if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python loadsong.py <audio_file>")
        sys.exit(1)

    audio_file = sys.argv[1]
    base_name = os.path.splitext(os.path.basename(audio_file))[0]

    # Get the script's directory and navigate to project root
    script_dir = os.path.dirname(os.path.abspath(__file__))
    project_root = os.path.abspath(os.path.join(script_dir, "..", "..", ".."))
    songs_dir = os.path.join(project_root, "src", "main", "resources", "songs")

    # Make sure directory exists
    os.makedirs(songs_dir, exist_ok=True)

    output_file = os.path.join(songs_dir, f"{base_name}.json")

    print(f"Analyzing {audio_file}...")
    print(f"Output will be saved to: {output_file}")

    data = analyze_audio_for_guitar_hero(audio_file)

    print(f"Analysis complete! Found {len(data['notes'])} notes")

    try:
        with open(output_file, "w") as f:
            json.dump(data, f, indent=2)

        # Verify it exists
        if os.path.exists(output_file):
            print(f"Successfully saved to: {output_file}")
        else:
            print(f"ERROR: File was written but doesn't exist!")

    except Exception as e:
        print(f"ERROR writing file: {e}")
