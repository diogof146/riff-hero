package RiffHero.client;

import java.util.Map;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;

/**
 * @author Diogo Ferreira
 *
 */
public class Main extends GameApplication {

  private static GameManager game = new GameManager();

  @Override
  protected void initSettings(GameSettings settings) {
    settings.setWidth(1000);
    settings.setHeight(566);
    settings.setTitle("Riff Hero");
  }

  @Override
  protected void initInput() {
    game.initInput();
  }

  @Override
  protected void initUI() {
    game.createMenu();
  }

  @Override
  protected void initGameVars(Map<String, Object> vars) {
    vars.put("score", 0);
  }

  @Override
  protected void onUpdate(double tpf) {
    game.updateGameplay();
  }

  public static void main(String[] args) {

    launch(args);

  }

}
