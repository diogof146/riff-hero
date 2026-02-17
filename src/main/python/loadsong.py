import json
import os
import sys
import mido


def find_best_melodic_track(mid):
    """
    Find the track that's most likely to be the melody/lead instrument.
    Looks for tracks with moderate note count and wide pitch range.
    """
    track_scores = []

    for idx, track in enumerate(mid.tracks):
        notes = [
            msg.note for msg in track if msg.type == "note_on" and msg.velocity > 0
        ]

        if len(notes) == 0:
            continue

        note_count = len(notes)
        note_range = max(notes) - min(notes)
        avg_note = sum(notes) / len(notes)

        # Score based on:
        # - Not too many notes (avoid drums)
        # - Wide pitch range (avoid single-note instruments)
        # - Higher average pitch (avoid bass)

        if note_count > 3000:  # Too many = probably drums
            density_score = 0
        elif note_count < 50:  # Too few = not useful
            density_score = 0
        else:
            density_score = min(note_count / 1000, 1.0)  # Favor 500-1000 notes

        range_score = min(note_range / 30, 1.0)  # Favor wide range
        pitch_score = min((avg_note - 40) / 30, 1.0)  # Favor higher pitched

        total_score = density_score * 0.4 + range_score * 0.3 + pitch_score * 0.3

        track_scores.append((idx, total_score, note_count, note_range, avg_note))
        print(
            f"Track {idx}: score={total_score:.2f}, notes={note_count}, range={note_range}, avg_note={avg_note:.1f}"
        )

    if not track_scores:
        return None

    # Return track with highest score
    best_track = max(track_scores, key=lambda x: x[1])
    print(f"\n✓ Selected Track {best_track[0]} (score: {best_track[1]:.2f})")
    return best_track[0]


def midi_to_json(midi_file_path, track_filter=None):
    """
    Converts a MIDI file to JSON format matching the Guitar Hero game format.
    If track_filter is None, auto-selects best melodic track.
    """

    mid = mido.MidiFile(midi_file_path)

    # Auto-select best track if not specified
    if track_filter is None:
        track_filter = find_best_melodic_track(mid)
        if track_filter is None:
            raise Exception("No suitable melodic track found")

    # Get tempo
    tempo = 500000  # Default: 120 BPM
    ticks_per_beat = mid.ticks_per_beat

    for track in mid.tracks:
        for msg in track:
            if msg.type == "set_tempo":
                tempo = msg.tempo
                break

    bpm = 60000000 / tempo
    total_time = mid.length

    # Collect all notes from selected track
    all_notes = []
    track = mid.tracks[track_filter]
    current_time = 0

    for msg in track:
        current_time += msg.time

        if msg.type == "note_on" and msg.velocity > 0:
            time_seconds = mido.tick2second(current_time, ticks_per_beat, tempo)
            all_notes.append(
                {
                    "time": float(time_seconds),
                    "note": msg.note,
                    "velocity": msg.velocity,
                }
            )

    # Calculate note range for better button mapping
    if len(all_notes) == 0:
        raise Exception(f"Track {track_filter} has no notes!")

    note_values = [n["note"] for n in all_notes]
    min_note = min(note_values)
    max_note = max(note_values)

    print(f"Note range: {min_note} to {max_note}")

    # Simple equal-range button mapping
    n_buttons = 5
    step = (max_note - min_note) / n_buttons
    boundaries = [min_note + i * step for i in range(n_buttons + 1)]

    print(f"Button boundaries: {boundaries}")

    notes = []

    for note_data in all_notes:
        # Map to button using boundaries
        button = n_buttons - 1  # default to highest
        for i in range(n_buttons):
            if note_data["note"] < boundaries[i + 1]:
                button = i
                break

        frequency = midi_note_to_freq(note_data["note"])

        notes.append(
            {
                "time": note_data["time"],
                "button": int(button),
                "frequency": float(frequency),
            }
        )

    # Calculate frequency range
    freqs = [n["frequency"] for n in notes]
    min_freq = min(freqs) if freqs else 0
    max_freq = max(freqs) if freqs else 0

    result = {
        "tempo": float(bpm),
        "duration": float(total_time),
        "notes": notes,
        "freq_range": {"min": float(min_freq), "max": float(max_freq)},
    }

    return result


def midi_note_to_freq(midi_note):
    """Convert MIDI note number to frequency in Hz"""
    return 440.0 * (2.0 ** ((midi_note - 69) / 12.0))


if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python loadsong.py <midi_file> [track_number]")
        sys.exit(1)

    midi_file = sys.argv[1]

    # Optional: manually specify track
    track_filter = None
    if len(sys.argv) >= 3:
        track_filter = int(sys.argv[2])

    base_name = os.path.splitext(os.path.basename(midi_file))[0]

    script_dir = os.path.dirname(os.path.abspath(__file__))
    project_root = os.path.abspath(os.path.join(script_dir, "..", "..", ".."))
    songs_dir = os.path.join(project_root, "src", "main", "resources", "songs")

    os.makedirs(songs_dir, exist_ok=True)
    output_file = os.path.join(songs_dir, f"{base_name}.json")

    print(f"Analyzing {midi_file}...")
    print(f"Output will be saved to: {output_file}\n")

    try:
        data = midi_to_json(midi_file, track_filter)

        print(f"\n✓ Analysis complete! Found {len(data['notes'])} notes")
        print(f"✓ Tempo: {data['tempo']:.2f} BPM")
        print(f"✓ Duration: {data['duration']:.2f} seconds")

        with open(output_file, "w") as f:
            json.dump(data, f, indent=2)

        if os.path.exists(output_file):
            print(f"✓ Successfully saved to: {output_file}")
        else:
            print(f"✗ ERROR: File was written but doesn't exist!")

    except Exception as e:
        print(f"✗ ERROR: {e}")
        import traceback

        traceback.print_exc()
        sys.exit(1)
