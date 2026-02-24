package RiffHero.client.components;

import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;

/**
 * Menu Button used throughout the platform
 *
 */
public class RHMenuBtn extends Button {

  private static final Font QUICKSAND_BOLD;

  static {
    QUICKSAND_BOLD = Font.loadFont(RHMenuBtn.class.getResourceAsStream("/fonts/Quicksand-Bold.ttf"), 20);
  }

  public RHMenuBtn(String text) {
    setText(text);
    setFont(QUICKSAND_BOLD);
    setStyle(
        "-fx-background-color: transparent; -fx-text-fill: #00CFD2;");
    setCursor(Cursor.HAND);
    setHoverStyle();
  }

  private void setHoverStyle() {
    DropShadow glow = new DropShadow();
    glow.setColor(Color.web("#00707E"));
    glow.setRadius(10);
    glow.setSpread(0.5);

    setOnMouseEntered(e -> {
      setEffect(glow);
      setStyle(
          "-fx-background-color: transparent; -fx-text-fill: #00CFD2; -fx-underline: true");
      setTextFill(Color.web("#00CFD2"));
    });

    setOnMouseExited(e -> {
      setEffect(null);
      setStyle(
          "-fx-background-color: transparent; -fx-text-fill: #00CFD2;");
      setTextFill(Paint.valueOf("#00CFD2"));
    });
  }
}
