package de.metaphoriker.randomizer.ui.view.controller;

import com.google.inject.Inject;
import de.metaphoriker.model.action.Action;
import de.metaphoriker.model.action.sequence.ActionSequence;
import de.metaphoriker.model.config.keybind.KeyBindType;
import de.metaphoriker.model.persistence.JsonUtil;
import de.metaphoriker.randomizer.ui.view.View;
import de.metaphoriker.randomizer.ui.view.ViewProvider;
import de.metaphoriker.randomizer.ui.view.component.ActionSettingsController;
import de.metaphoriker.randomizer.ui.view.viewmodel.BuilderViewModel;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.List;

@View
public class BuilderViewController {

    private final Separator dropIndicator = new Separator();

    private final ViewProvider viewProvider;
    private final BuilderViewModel builderViewModel;
    private final JsonUtil jsonUtil;

    @FXML
    private VBox actionSequencesVBox;
    @FXML
    private VBox actionsVBox;
    @FXML
    private VBox builderVBox;
    @FXML
    private TextField searchField;
    @FXML
    private Label sequenceDescriptionLabel;
    @FXML
    private Label sequenceNameLabel;
    @FXML
    private Button randomizeButton;
    @FXML
    private Button actionsClearButton;
    @FXML
    private Button saveSequenceButton;
    @FXML
    private VBox settingsHolder;

    private ActionSettingsController actionSettingsController;

    @Inject
    public BuilderViewController(ViewProvider viewProvider, BuilderViewModel builderViewModel, JsonUtil jsonUtil) {
        this.viewProvider = viewProvider;
        this.builderViewModel = builderViewModel;
        this.jsonUtil = jsonUtil;
        Platform.runLater(this::initialize);
    }

    private void setupBindings() {
        builderViewModel
                .getCurrentActionSequenceProperty()
                .addListener((_, _, newSequence) -> fillBuilderWithActionsOfSequence(newSequence));

        builderViewModel
                .getCurrentActionsProperty()
                .addListener((ListChangeListener<Action>) _ -> updateBuilderVBox());

        builderVBox
                .disableProperty()
                .bind(builderViewModel.getCurrentActionSequenceProperty().isNull());

        randomizeButton
                .disableProperty()
                .bind(builderViewModel.getCurrentActionSequenceProperty().isNull());

        actionsClearButton
                .disableProperty()
                .bind(builderViewModel.getCurrentActionSequenceProperty().isNull());

        saveSequenceButton
                .disableProperty()
                .bind(builderViewModel.getCurrentActionSequenceProperty().isNull());

        builderViewModel
                .getSequenceNameProperty()
                .addListener((_, _, newValue) -> sequenceNameLabel.setText(newValue));

        builderViewModel
                .getSequenceDescriptionProperty()
                .addListener(
                        (_, _, newValue) -> {
                            String description = newValue;
                            if (description.isEmpty()
                                    && builderViewModel.getCurrentActionSequenceProperty().get() != null)
                                description = "No description provided";
                            sequenceDescriptionLabel.setText(description);
                        });

        setupSearchFieldListener();
    }

    @FXML
    void onRandomize(ActionEvent event) {
        builderViewModel.addRandomActions(10);
    }

    @FXML
    void onActionsClear(ActionEvent event) {
        builderViewModel.setActions(List.of());
    }

    @FXML
    void onSaveSequence(ActionEvent event) {
        builderViewModel.saveActionSequence();
        fillActionSequences();
    }

    @FXML
    void onAddSequence(ActionEvent event) {
        builderViewModel.createNewActionSequence();
        fillActionSequences();
    }

    @FXML
    void onOpenSequenceFolder(ActionEvent event) {
        try {
            builderViewModel.openSequenceFolder();
        } catch (IOException e) {
            throw new IllegalStateException("Konnte Sequencefolder nicht öffnen, Fehler:", e);
        }
    }

    private void initialize() {
        initActionSettings();
        setupBindings();
        fillActions();
        fillActionSequences();
        setupDrop(builderVBox);
    }

    private void initActionSettings() {
        actionSettingsController = viewProvider.requestView(ActionSettingsController.class).controller();
        actionSettingsController.bindOnVisibleProperty(visible -> {
            if (visible)
                settingsHolder.getChildren().setAll(viewProvider.requestView(ActionSettingsController.class).parent());
            else
                settingsHolder.getChildren().clear();
        });
        builderViewModel.getCurrentActionSequenceProperty().addListener((_, _, _) -> actionSettingsController.setAction(null));
    }

    private void updateBuilderVBox() {
        builderVBox.getChildren().clear();
    builderViewModel
        .getCurrentActionsProperty()
        .forEach(
            action -> {
              Label actionLabel = new Label(action.getName());
              actionLabel.getStyleClass().add("selected-actions-label");
                ImageView imageView = new ImageView(new Image("de/metaphoriker/randomizer/images/heart.png"));
                imageView.setFitWidth(16);
                imageView.setFitHeight(16);
                actionLabel.setGraphic(imageView);
              actionLabel.setOnMouseClicked(_ -> actionSettingsController.setAction(action));
              setupDragAlreadyDropped(actionLabel, action); // setup special drag within listview
              builderVBox.getChildren().add(actionLabel);
            });
    }

    private void setupDrag(Label label, Action action) {
        label.setCursor(Cursor.HAND);
        label.setOnDragDetected(
                dragEvent -> {
                    Dragboard dragboard = label.startDragAndDrop(TransferMode.MOVE);
                    dragboard.setDragView(label.snapshot(null, null), dragEvent.getX(), dragEvent.getY());

                    ClipboardContent content = new ClipboardContent();
                    String serializedAction = jsonUtil.serialize(action);
                    content.putString(serializedAction);

                    dragboard.setContent(content);
                    dragEvent.consume();
                });
    }

    private void setupDragAlreadyDropped(Label label, Action action) {
        label.setCursor(Cursor.HAND);
        label.setOnDragDetected(
                dragEvent -> {
                    Dragboard dragboard = label.startDragAndDrop(TransferMode.MOVE);
                    dragboard.setDragView(label.snapshot(null, null), dragEvent.getX(), dragEvent.getY());

                    ClipboardContent content = new ClipboardContent();
                    String serializedAction = jsonUtil.serialize(action);
                    builderViewModel.removeAction(action);
                    content.putString(serializedAction);

                    dragboard.setContent(content);
                    dragEvent.consume();
                });

        label.setOnDragDropped(
                dragEvent -> {
                    Dragboard dragboard = dragEvent.getDragboard();
                    boolean success = false;

                    if (dragboard.hasString()) {
                        String serializedAction = dragboard.getString();
                        Action droppedAction = jsonUtil.deserializeAction(serializedAction);
                        int index = builderVBox.getChildren().indexOf(label);
                        builderViewModel.addActionAt(droppedAction, Math.max(0, index - 1));
                        success = true;
                    }

                    dragEvent.setDropCompleted(success);
                    dragEvent.consume();
                });

        label.setOnDragEntered(
                _ ->
                        builderVBox.getChildren().add(builderVBox.getChildren().indexOf(label), dropIndicator));

        label.setOnDragExited(_ -> builderVBox.getChildren().remove(dropIndicator));
    }

    private void setupDrop(VBox target) {
        target.setOnDragOver(
                dragEvent -> {
                    if (dragEvent.getGestureSource() != target && dragEvent.getDragboard().hasString()) {
                        dragEvent.acceptTransferModes(TransferMode.MOVE);
                    }
                    dragEvent.consume();
                });

        target.setOnDragDropped(
                dragEvent -> {
                    Dragboard dragboard = dragEvent.getDragboard();
                    boolean success = false;

                    if (dragboard.hasString()) {
                        String serializedAction = dragboard.getString();
                        Action droppedAction = jsonUtil.deserializeAction(serializedAction);

                        builderViewModel.addAction(droppedAction);
                        success = true;
                    }

                    dragEvent.setDropCompleted(success);
                    dragEvent.consume();
                });
    }

    private void setupSearchFieldListener() {
        searchField
                .textProperty()
                .addListener(
                        (_, _, newValue) -> {
                            if (newValue == null || newValue.isEmpty()) {
                                actionsVBox.getChildren().clear();
                                fillActions();
                                return;
                            }

                            String filter = newValue.toLowerCase();
                            actionsVBox.getChildren().clear();
                            builderViewModel
                                    .getActionToTypeMap()
                                    .forEach(
                                            (_, actionList) -> {
                                                List<Action> filteredActions =
                                                        actionList.stream()
                                                                .filter(action -> action.getName().toLowerCase().contains(filter))
                                                                .toList();

                                                filteredActions.forEach(
                                                        action -> {
                                                            Label actionLabel = new Label(action.getName());
                                                            setupDrag(actionLabel, action);
                                                            actionsVBox.getChildren().add(actionLabel);
                                                        });
                                            });
                        });
    }

    private TitledPane createTitledPane(KeyBindType type, List<Action> actions) {
        TitledPane titledPane = new TitledPane();
        titledPane.setCollapsible(true);
        titledPane.setAnimated(true);
        titledPane.setExpanded(false);
        titledPane.getStyleClass().add("builder-actions-category");
        titledPane.setText(type.name());

        VBox vBox = new VBox();
        actions.forEach(
                action -> {
                    Label actionLabel = new Label(action.getName());
                    actionLabel.getStyleClass().add("builder-actions-title");
                    setupDrag(actionLabel, action);
                    vBox.getStyleClass().add("builder-actions-title-vbox");
                    vBox.getChildren().add(actionLabel);
                });
        applyExpandListener(titledPane);
        titledPane.setContent(vBox);

        return titledPane;
    }

    private void fillActions() {
        builderViewModel
                .getActionToTypeMap()
                .forEach(
                        (type, actionList) ->
                                actionsVBox.getChildren().add(createTitledPane(type, actionList)));
    }

    private void applyExpandListener(TitledPane titledPane) {
        titledPane
                .expandedProperty()
                .addListener(
                        (_, _, newValue) -> {
                            if (newValue) {
                                actionsVBox.getChildren().stream()
                                        .filter(node -> node instanceof TitledPane && node != titledPane)
                                        .forEach(node -> ((TitledPane) node).setExpanded(false));
                            }
                        });
    }

    private void fillActionSequences() {
        actionSequencesVBox.getChildren().clear();
        builderViewModel
                .getActionSequences()
                .forEach(
                        actionSequence -> {
                            HBox hBox = new HBox();
                            hBox.setCursor(Cursor.HAND);
                            hBox.getStyleClass().add("builder-sequences-hbox");
                            Label label = new Label(actionSequence.getName());
                            label.getStyleClass().add("builder-sequences-title");
                            hBox.setOnMouseClicked(
                                    _ -> builderViewModel.getCurrentActionSequenceProperty().set(actionSequence));

                            hBox.getChildren().add(label);
                            Button deleteSequenceButton = createDeleteButton(actionSequence);

                            HBox buttonHBox = new HBox();
                            buttonHBox.setAlignment(Pos.CENTER_RIGHT);
                            HBox.setHgrow(buttonHBox, Priority.ALWAYS);
                            buttonHBox.getChildren().add(deleteSequenceButton);
                            hBox.getChildren().add(buttonHBox);

                            actionSequencesVBox.getChildren().add(hBox);
                        });
    }

    private Button createDeleteButton(ActionSequence actionSequence) {
        Button deleteSequenceButton = new Button("");
        deleteSequenceButton.getStyleClass().add("builder-sequences-delete-button");
        deleteSequenceButton.setOnAction(
                event -> {
                    builderViewModel.deleteActionSequence(actionSequence);
                    if (builderViewModel.getCurrentActionSequenceProperty().get() != null
                            && builderViewModel
                            .getCurrentActionSequenceProperty()
                            .get()
                            .getName()
                            .equalsIgnoreCase(actionSequence.getName()))
                        builderViewModel.getCurrentActionSequenceProperty().set(null);
                    fillActionSequences();
                    event.consume();
                });
        return deleteSequenceButton;
    }

    private void fillBuilderWithActionsOfSequence(ActionSequence sequence) {
        if (sequence == null) return;
        List<Action> actions = builderViewModel.getActionsOfSequence(sequence);
        builderViewModel.setActions(actions);
    }
}
