module de.metaphoriker.randomizer.playground {
  requires javafx.controls;
  requires javafx.fxml;
  requires javafx.graphics;
  requires javafx.base;

  opens de.metaphoriker to
      javafx.fxml;

  exports de.metaphoriker;
}
