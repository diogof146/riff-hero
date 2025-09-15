package RiffHero.client;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.input.UserAction;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import RiffHero.client.components.RHMenuBtn;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

public class GameManager {

  private Entity buttonA;
  private Entity buttonD;
  private Entity buttonJ;
  private Entity buttonK;
  private Entity buttonL;

  private ArrayList<Song> songs;
  private ArrayDeque<Entity> activeNotes = new ArrayDeque<>();

  private Song currentSong;
  private MediaPlayer mediaPlayer;
  private static final double SPAWN_OFFSET = 1;
  private static final double HIT_THRESHOLD = 50.0;

  private ArrayDeque<Note> pendingNotes = new ArrayDeque<>();

  public GameManager() {
    songs = new ArrayList<>();
  }

  public void startGameplay(Song song) {
    if (song == null)
      return;
    this.currentSong = song;
    pendingNotes.clear();
    activeNotes.clear();
    pendingNotes.addAll(song.getNotes());
    createUI();

    try {
      String audioResource = getClass().getResource("/assets/music/" + song.getAudioPath()).toExternalForm();
      Media media = new Media(audioResource);
      mediaPlayer = new MediaPlayer(media);

      mediaPlayer.setOnEndOfMedia(() -> {
        activeNotes.forEach(Entity::removeFromWorld);
        activeNotes.clear();
        endScreen(currentSong);
      });

      mediaPlayer.play();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void updateGameplay() {

    if (currentSong == null)
      return;

    double currentTime = mediaPlayer.getCurrentTime().toSeconds();

    while (!pendingNotes.isEmpty()) {
      Note note = pendingNotes.peek();
      if (currentTime >= note.getTime() + currentSong.getOffSet() - SPAWN_OFFSET) {
        spawnNote(pendingNotes.poll());
      } else {
        break;
      }
    }

    activeNotes.removeIf(e -> {
      if (e.getY() > e.getDouble("targetY") + HIT_THRESHOLD) {
        e.removeFromWorld();
        FXGL.inc("score", -50);
        return true;
      }
      return false;
    });
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

  private void endScreen(Song currentSong) {
    RHMenuBtn playAgainButton = new RHMenuBtn("Play Again");
    playAgainButton.setAlignment(Pos.CENTER);
    RHMenuBtn mainMenu = new RHMenuBtn("Main Menu");
    mainMenu.setAlignment(Pos.CENTER);
    RHMenuBtn exitButton = new RHMenuBtn("Exit");
    exitButton.setAlignment(Pos.CENTER);

    playAgainButton.setOnAction(e -> {
      createUI();
      startGameplay(currentSong);
    });

    mainMenu.setOnAction(e -> {
      createMenu();
    });

    exitButton.setOnAction(e -> {
      System.exit(0);
    });
    VBox menuBox = new VBox(10);
    menuBox.setAlignment(Pos.CENTER);
    menuBox.getChildren().addAll(playAgainButton, mainMenu, exitButton);
    menuBox.setMaxWidth(200);
    menuBox.setMinWidth(200);
    menuBox.setMinHeight(200);
    menuBox.setMaxHeight(200);

    menuBox.setTranslateX(FXGL.getSettings().getWidth() / 2 - 100);
    menuBox.setTranslateY(FXGL.getSettings().getHeight() / 2 - 100);

    Image img = new Image(getClass().getResourceAsStream("/menubg.jpg"));
    FXGL.getGameScene().setBackgroundRepeat(img);
    FXGL.getGameScene().addUINode(menuBox);

  }

  private void spawnNote(Note note) {
    Entity targetButton = getButtonForIndex(note.getButton());

    Entity noteEntity = FXGL.entityBuilder()
        .at(targetButton.getX(), 0)
        .view(new Circle(15, targetButton.getObject("color")))
        .with("noteData", note)
        .with("targetY", targetButton.getY())
        .buildAndAttach();

    FXGL.animationBuilder()
        .duration(javafx.util.Duration.seconds(SPAWN_OFFSET))
        .translate(noteEntity)
        .from(new Point2D(noteEntity.getX(), 0))
        .to(new Point2D(noteEntity.getX(), targetButton.getY()))
        .buildAndPlay();

    FXGL.runOnce(() -> {
      if (activeNotes.remove(noteEntity)) {
        noteEntity.removeFromWorld();
        FXGL.inc("score", -15);
      }
    }, javafx.util.Duration.seconds(SPAWN_OFFSET));

    activeNotes.add(noteEntity);
  }

  /**
   * This method handles the inputs
   *
   */
  public void initInput() {
    FXGL.getInput().addAction(new UserAction("A") {
      protected void onActionBegin() {
        blinkOn(buttonA);
        checkHit(0);
      }

      protected void onActionEnd() {
        blinkOff(buttonA);
      }
    }, KeyCode.A);

    FXGL.getInput().addAction(new UserAction("D") {
      protected void onActionBegin() {
        blinkOn(buttonD);
        checkHit(1);
      }

      protected void onActionEnd() {
        blinkOff(buttonD);
      }
    }, KeyCode.D);

    FXGL.getInput().addAction(new UserAction("J") {
      protected void onActionBegin() {
        blinkOn(buttonJ);
        checkHit(2);
      }

      protected void onActionEnd() {
        blinkOff(buttonJ);
      }
    }, KeyCode.J);

    FXGL.getInput().addAction(new UserAction("K") {
      protected void onActionBegin() {
        blinkOn(buttonK);
        checkHit(3);
      }

      protected void onActionEnd() {
        blinkOff(buttonK);
      }
    }, KeyCode.K);

    FXGL.getInput().addAction(new UserAction("L") {
      protected void onActionBegin() {
        blinkOn(buttonL);
        checkHit(4);
      }

      protected void onActionEnd() {
        blinkOff(buttonL);
      }
    }, KeyCode.L);
  }

  private void checkHit(int buttonIndex) {
    Entity targetButton = getButtonForIndex(buttonIndex);

    Iterator<Entity> it = activeNotes.iterator();
    while (it.hasNext()) {
      Entity e = it.next();
      Note note = e.getObject("noteData");
      if (note.getButton() == buttonIndex
          && Math.abs(e.getY() - targetButton.getY()) <= HIT_THRESHOLD) {
        e.removeFromWorld();
        it.remove();
        FXGL.inc("score", +100);
        return;
      }
    }
    FXGL.inc("score", -25);
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

  public void blinkOn(Entity entity) {
    Circle circle = (Circle) entity.getViewComponent().getChildren().get(0);
    circle.setEffect(null);
  }

  public void blinkOff(Entity entity) {
    setButtonEffect(entity);
  }

  /**
   * This method creates the UI of the game (after selecting song)
   *
   */
  public void createUI() {
    Text score = new Text();
    score.setFill(Color.web("#00CFD2"));
    score.setFont(javafx.scene.text.Font.font(18));
    score.setTranslateX(50);
    score.setTranslateY(100);
    score.textProperty().bind(FXGL.getWorldProperties().intProperty("score").asString());

    buttonA = FXGL.entityBuilder().at(FXGL.getSettings().getWidth() * 1 / 6, FXGL.getSettings().getHeight() - 100)
        .view(new Circle(25, Color.CYAN)).with("color", Color.CYAN)
        .buildAndAttach();
    setButtonEffect(buttonA);
    buttonD = FXGL.entityBuilder().at(FXGL.getSettings().getWidth() * 2 / 6, FXGL.getSettings().getHeight() - 100)
        .view(new Circle(25, Color.AZURE)).with("color", Color.AZURE)
        .buildAndAttach();
    setButtonEffect(buttonD);
    buttonJ = FXGL.entityBuilder().at(FXGL.getSettings().getWidth() * 3 / 6, FXGL.getSettings().getHeight() - 100)
        .view(new Circle(25, Color.CORAL)).with("color", Color.CORAL)
        .buildAndAttach();
    setButtonEffect(buttonJ);
    buttonK = FXGL.entityBuilder().at(FXGL.getSettings().getWidth() * 4 / 6, FXGL.getSettings().getHeight() - 100)
        .view(new Circle(25, Color.BISQUE)).with("color", Color.BISQUE)
        .buildAndAttach();
    setButtonEffect(buttonK);
    buttonL = FXGL.entityBuilder().at(FXGL.getSettings().getWidth() * 5 / 6, FXGL.getSettings().getHeight() - 100)
        .view(new Circle(25, Color.FUCHSIA)).with("color", Color.FUCHSIA)
        .buildAndAttach();
    setButtonEffect(buttonL);

    Image img = new Image(getClass().getResourceAsStream("/bg.jpg"));
    FXGL.getGameScene().setBackgroundRepeat(img);
    FXGL.getGameScene().clearUINodes();
    FXGL.getGameScene().addUINodes(score);
  }

  /**
   * This method creates the main menu
   *
   */
  public void createMenu() {
    RHMenuBtn playButton = new RHMenuBtn("Play");
    playButton.setAlignment(Pos.CENTER);
    RHMenuBtn loadSong = new RHMenuBtn("Load Song");
    loadSong.setAlignment(Pos.CENTER);
    RHMenuBtn exitButton = new RHMenuBtn("Exit");
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
    menuBox.setMaxWidth(200);
    menuBox.setMinWidth(200);
    menuBox.setMinHeight(200);
    menuBox.setMaxHeight(200);

    menuBox.setTranslateX(FXGL.getSettings().getWidth() / 2 - 100);
    menuBox.setTranslateY(FXGL.getSettings().getHeight() / 2 - 100);

    Image img = new Image(getClass().getResourceAsStream("/menubg.jpg"));
    FXGL.getGameScene().setBackgroundRepeat(img);
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

        songs.add(new Song(songName, jsonFile.getPath(), duration, tempo, 100));

      } catch (IOException e) {
        e.printStackTrace();

      }
    }
  }

  public Song loadSong() {
    String songName = "scarlet";
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
      Song song = new Song(songName, "songs/" + songName + ".json", duration, tempo, 0.5);
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
