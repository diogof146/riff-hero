package RiffHero.client;

import java.util.ArrayList;

class Song {

  private String name;
  private String jsonPath;
  private String audioPath;
  private double offSet;
  private double duration;
  private double tempo;
  private ArrayList<Note> notes;

  public Song(String name, String jsonPath, double duration, double tempo, double offSet) {
    this.name = name;
    this.jsonPath = jsonPath;
    this.audioPath = "scarlet.wav";
    this.duration = duration;
    this.tempo = tempo;
    this.offSet = offSet;
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

  public void setOffSet(double offSet) {
    this.offSet = offSet;
  }

  public double getOffSet() {
    return offSet;
  }
}
