package RiffHero.client;

public class Note {

  private double time;
  private int button;
  private double duration;

  public Note(double time, int button) {
    this.time = time;
    this.button = button;
    duration = 0;
  }

  public double getTime() {
    return time;
  }

  public int getButton() {
    return button;
  }

  public void setDuration(double duration) {
    this.duration = duration;
  }

  public double getDuration() {
    return duration;
  }

}
