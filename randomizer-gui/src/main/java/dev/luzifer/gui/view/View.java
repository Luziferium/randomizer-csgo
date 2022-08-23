package dev.luzifer.gui.view;

import javafx.fxml.Initializable;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public abstract class View<T extends ViewModel> extends Stage implements Initializable {

    private final T viewModel;
    
    protected View(T viewModel) {
        this.viewModel = viewModel;
    }
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }
    
    public T getViewModel() {
        return viewModel;
    }
}
