package RiffHero;

public class Note {

  private double time;
  private int button;

  public Note(double time, int button) {
    this.time = time;
    this.button = button;
  }

  public double getTime() {
    return time;
  }

  public int getButton() {
    return button;
  }

}
