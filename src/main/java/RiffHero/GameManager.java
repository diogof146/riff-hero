package RiffHero;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

public class GameManager {

  private Entity buttonA;
  private Entity buttonD;
  private Entity buttonJ;
  private Entity buttonK;
  private Entity buttonL;

  private ArrayList<Song> songs;

  private Song currentSong;
  private double elapsedTime = 0;
  private int noteIndex = 0;
  private static final double SPAWN_OFFSET = 2.0;
  private static final double HIT_THRESHOLD = 50.0;

  public GameManager() {
    songs = new ArrayList<>();
  }

  public void startGameplay(Song song) {
    this.currentSong = song;
    this.noteIndex = 0;
    this.elapsedTime = 0;
    createUI();
    FXGL.play(song.getAudioPath());
  }

  public void updateGameplay(double tpf) {
    if (currentSong == null)
      return;

    elapsedTime += tpf; // tpf = time per frame in seconds


    while (noteIndex < currentSong.getNotes().size()) {
      Note note = currentSong.getNotes().get(noteIndex);

      if (elapsedTime >= note.getTime() - SPAWN_OFFSET) {
        spawnNote(note);
        noteIndex++;
      } else {
        break;
      }
    }
  }

  private Entity getButtonForIndex(int ind) {
    switch (ind) {
      case 0 -> {
        return buttonA;
      }
      case 1 -> {
        return buttonD;
      }
      case 2 -> {
        return buttonJ;
      }
      case 3 -> {
        return buttonK;
      }
      case 4 -> {
        return buttonL;
      }
      default -> {
        return null;
      }
    }
  }

  private void spawnNote(Note note) {

    Entity targetButton = getButtonForIndex(note.getButton());

    // Create note entity at top of screen, aligned with button
    Entity noteEntity = FXGL.entityBuilder()
        .at(targetButton.getX(), 0)
        .view(new Circle(15, Color.YELLOW))
        .with("noteData", note)
        .with("targetY", targetButton.getY())
        .buildAndAttach();

    // Calculate fall speed so it reaches button at exact time
    double fallDistance = targetButton.getY();
    double fallTime = SPAWN_OFFSET;
    double fallSpeed = fallDistance / fallTime;

    // Make it fall
    FXGL.animationBuilder()
        .duration(javafx.util.Duration.seconds(SPAWN_OFFSET))
        .translate(noteEntity)
        .from(new Point2D(noteEntity.getX(), 0))
        .to(new Point2D(noteEntity.getX(), targetButton.getY()))
        .buildAndPlay();
  }

  /**
   * This method handles the inputs
   *
   */
  public void initInput() {
    FXGL.onKey(KeyCode.A, () -> {
      blink(buttonA);
      FXGL.inc("pixelsMoved", +5);
    });

    FXGL.onKey(KeyCode.D, () -> {
      blink(buttonD);
      FXGL.inc("pixelsMoved", +5);
    });

    FXGL.onKey(KeyCode.L, () -> {
      blink(buttonL);
      FXGL.inc("pixelsMoved", +5);
    });

    FXGL.onKey(KeyCode.J, () -> {
      blink(buttonJ);
      FXGL.inc("pixelsMoved", +5);
    });

    FXGL.onKey(KeyCode.K, () -> {
      blink(buttonK);
      FXGL.inc("pixelsMoved", +5);
    });

  }

  /**
   * This method sets the base effects of buttons
   *
   * @param entity
   */
  public void setButtonEffect(Entity entity) {
    Circle circle = (Circle) entity.getViewComponent().getChildren().get(0);
    Light.Distant light = new Light.Distant();
    light.setAzimuth(-135.0);
    Lighting lighting = new Lighting();
    lighting.setLight(light);
    lighting.setSurfaceScale(5.0);
    circle.setEffect(lighting);
  }

  /**
   * This method makes buttons blink when pressed
   *
   * @param entity
   */
  public void blink(Entity entity) {
    Circle circle = (Circle) entity.getViewComponent().getChildren().get(0);
    circle.setEffect(null);

    FXGL.getGameTimer().runOnceAfter(() -> {
      setButtonEffect(entity);
    }, javafx.util.Duration.seconds(0.1));

  }

  /**
   * This method creates the UI of the game (after selecting song)
   *
   */
  public void createUI() {
    Text textPixels = new Text();
    textPixels.setTranslateX(50);
    textPixels.setTranslateY(100);
    textPixels.textProperty().bind(FXGL.getWorldProperties().intProperty("pixelsMoved").asString());

    buttonA = FXGL.entityBuilder().at(FXGL.getSettings().getWidth() * 1 / 6, FXGL.getSettings().getHeight() - 100)
        .view(new Circle(25, Color.CYAN))
        .buildAndAttach();
    setButtonEffect(buttonA);
    buttonD = FXGL.entityBuilder().at(FXGL.getSettings().getWidth() * 2 / 6, 500).view(new Circle(25, Color.AZURE))
        .buildAndAttach();
    setButtonEffect(buttonD);
    buttonJ = FXGL.entityBuilder().at(FXGL.getSettings().getWidth() * 3 / 6, 500).view(new Circle(25, Color.CORAL))
        .buildAndAttach();
    setButtonEffect(buttonJ);
    buttonK = FXGL.entityBuilder().at(FXGL.getSettings().getWidth() * 4 / 6, 500).view(new Circle(25, Color.BISQUE))
        .buildAndAttach();
    setButtonEffect(buttonK);
    buttonL = FXGL.entityBuilder().at(FXGL.getSettings().getWidth() * 5 / 6, 500).view(new Circle(25, Color.FUCHSIA))
        .buildAndAttach();
    setButtonEffect(buttonL);

    Label aText = new Label("A");
    aText.setLayoutX(FXGL.getSettings().getWidth() * 1 / 6 - 5);
    aText.setLayoutY(FXGL.getSettings().getHeight() - 100 - 9);
    aText.setFont(new Font(18));

    Label dText = new Label("D");
    dText.setLayoutX(FXGL.getSettings().getWidth() * 2 / 6 - 5);
    dText.setLayoutY(500 - 9);
    dText.setFont(new Font(18));

    Label jText = new Label("J");
    jText.setLayoutX(FXGL.getSettings().getWidth() * 3 / 6 - 5);
    jText.setLayoutY(500 - 9);
    jText.setFont(new Font(18));

    Label kText = new Label("K");
    kText.setLayoutX(FXGL.getSettings().getWidth() * 4 / 6 - 5);
    kText.setLayoutY(500 - 9);
    kText.setFont(new Font(18));

    Label lText = new Label("L");
    lText.setLayoutX(FXGL.getSettings().getWidth() * 5 / 6 - 5);
    lText.setLayoutY(500 - 9);
    lText.setFont(new Font(18));

    FXGL.getGameScene().clearUINodes();
    FXGL.getGameScene().addUINodes(textPixels, aText, dText, jText, kText, lText);
  }

  /**
   * This method creates the main menu
   *
   */
  public void createMenu() {
    Button playButton = new Button("Play");
    playButton.setAlignment(Pos.CENTER);
    Button loadSong = new Button("Load Song");
    loadSong.setAlignment(Pos.CENTER);
    Button exitButton = new Button("Exit");
    exitButton.setAlignment(Pos.CENTER);

    playButton.setOnAction(e -> {
      createUI();
      startGameplay(loadSong());
    });

    loadSong.setOnAction(e -> {
      readSong();
    });

    exitButton.setOnAction(e -> {
      System.exit(0);
    });
    VBox menuBox = new VBox(10); // 10px spacing
    menuBox.setAlignment(Pos.CENTER);
    menuBox.getChildren().addAll(playButton, loadSong, exitButton);
    menuBox.setMaxWidth(100);
    menuBox.setMinWidth(100);
    menuBox.setMinHeight(100);
    menuBox.setMaxHeight(100);

    menuBox.setTranslateX(FXGL.getSettings().getWidth() / 2 - 50);
    menuBox.setTranslateY(FXGL.getSettings().getHeight() / 2 - 50);

    FXGL.getGameScene().addUINode(menuBox);

  }

  public void loadSongs() {
    File songsDir = new File("resources/songs/");

    for (File jsonFile : songsDir.listFiles((dir, name) -> name.endsWith(".json"))) {

      try (FileReader reader = new FileReader(jsonFile)) {
        JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

        String songName = jsonFile.getName().replace(".json", "");
        double duration = json.get("duration").getAsDouble();
        double tempo = json.get("tempo").getAsDouble();

        songs.add(new Song(songName, jsonFile.getPath(), duration, tempo));

      } catch (IOException e) {
        e.printStackTrace();

      }
    }
  }

  public Song loadSong() {
    String songName = "byob";

    try {
      InputStream is = getClass().getClassLoader().getResourceAsStream("songs/" + songName + ".json");

      if (is == null) {
        System.out.println("Song file not found in classpath: songs/" + songName + ".json");
        return null;
      }

      InputStreamReader reader = new InputStreamReader(is);
      JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

      double duration = json.get("duration").getAsDouble();
      double tempo = json.get("tempo").getAsDouble();
      Song song = new Song(songName, "songs/" + songName + ".json", duration, tempo);

      JsonArray notesArray = json.getAsJsonArray("notes");
      for (int i = 0; i < notesArray.size(); i++) {
        JsonObject noteObj = notesArray.get(i).getAsJsonObject();
        double time = noteObj.get("time").getAsDouble();
        int button = noteObj.get("button").getAsInt();
        song.addNote(new Note(time, button));
      }

      reader.close();
      return song;

    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  public void readSong() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Select song to load");

    File selectedFile = fileChooser.showOpenDialog(FXGL.getPrimaryStage());

  }
}
