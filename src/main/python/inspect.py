import mido
import sys

midi_file = sys.argv[1]
mid = mido.MidiFile(midi_file)

print(f"Total tracks: {len(mid.tracks)}\n")

for i, track in enumerate(mid.tracks):
    note_count = sum(1 for msg in track if msg.type == "note_on" and msg.velocity > 0)
    print(f"Track {i}: '{track.name}'")
    print(f"  - {note_count} notes")
    print(f"  - {len(track)} total events")

    # Show note range
    notes = [msg.note for msg in track if msg.type == "note_on" and msg.velocity > 0]
    if notes:
        print(f"  - Note range: {min(notes)} to {max(notes)}")
    print()
