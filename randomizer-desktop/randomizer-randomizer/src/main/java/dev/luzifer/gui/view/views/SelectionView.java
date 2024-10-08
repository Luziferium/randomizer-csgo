package dev.luzifer.gui.view.views;

import dev.luzifer.gui.util.CSSUtil;
import dev.luzifer.gui.util.ImageUtil;
import dev.luzifer.gui.view.View;
import dev.luzifer.gui.view.models.SelectionViewModel;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;

public class SelectionView extends View<SelectionViewModel> {

  @FXML private Circle logoShape;

  @FXML private Label randomizerLabel;

  @FXML private Label builderLabel;

  @FXML private ImageView invasionGameBanner;

  @FXML private ComboBox<CSSUtil.Theme> themeComboBox;

  public SelectionView(SelectionViewModel selectionViewModel) {
    super(selectionViewModel);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {

    setResizable(false);

    setupGraphics();
    setupMouseEvents();
    setupThemeComboBox();
  }

  @Override
  public void onClose() {
    System.exit(0);
  }

  private void setupGraphics() {

    getIcons().add(ImageUtil.getImage("images/csgoremote_icon.png"));

    logoShape.setFill(
        ImageUtil.getImagePattern(
            "images/csgoremote_icon.png", ImageUtil.ImageResolution.ORIGINAL));

    randomizerLabel.setGraphic(
        ImageUtil.getImageView("images/shuffle_icon.png", ImageUtil.ImageResolution.SMALL));
    builderLabel.setGraphic(
        ImageUtil.getImageView("images/build_icon.png", ImageUtil.ImageResolution.SMALL));
    invasionGameBanner.setImage(ImageUtil.getRawImage("images/game/invasion_game_banner.png"));
  }

  private void setupMouseEvents() {

    randomizerLabel.setOnMouseClicked(event -> getViewModel().openRandomizer());
    builderLabel.setOnMouseClicked(event -> getViewModel().openBuilder());
    invasionGameBanner.setOnMouseClicked(event -> getViewModel().openGame());
  }

  private void setupThemeComboBox() {

    getViewModel().getThemeProperty().bindBidirectional(themeComboBox.valueProperty());

    themeComboBox
        .getSelectionModel()
        .selectedItemProperty()
        .addListener((observableValue, oldTheme, newTheme) -> getViewModel().switchTheme());

    themeComboBox.getItems().addAll(CSSUtil.Theme.values());
    themeComboBox.getSelectionModel().select(CSSUtil.Theme.RANDOMIZER);
  }
}
