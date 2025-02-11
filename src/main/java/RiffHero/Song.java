package RiffHero;

import java.util.ArrayList;

class Song {

  private String name;
  private String jsonPath;
  private String audioPath;
  private double duration;
  private double tempo;
  private ArrayList<Note> notes;

  public Song(String name, String jsonPath, double duration, double tempo) {
    this.name = name;
    this.jsonPath = jsonPath;
    this.audioPath = "byob.mp3";
    this.duration = duration;
    this.tempo = tempo;
    notes = new ArrayList<>();
  }

  public void addNote(Note note) {
    notes.add(note);
  }

  public ArrayList<Note> getNotes() {
      return notes;
  }

  public String getAudioPath() {
      return audioPath;
  }
}
