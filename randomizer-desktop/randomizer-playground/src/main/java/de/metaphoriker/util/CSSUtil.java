package de.metaphoriker.util;

import de.metaphoriker.Main;
import java.net.URL;
import java.text.MessageFormat;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class CSSUtil {

    private static final String STYLING_PATH = "styling/";
    private static final String STYLING_THEMES_PATH = STYLING_PATH + "themes/";
    private static final String STYLING_EXTENSION = ".css";

    private CSSUtil() {}

    /**
     * Applies the CSS file of the given theme to the given node.
     *
     * @param node The node to apply the CSS to.
     */
    public static void applyTheme(Scene node, Theme theme) {

        URL resource = Main.class.getResource(getThemePath(theme));

        if (resource != null) node.getStylesheets().add(resource.toExternalForm());
        else
            throw new IllegalStateException(
                    MessageFormat.format("Theme file could not get loaded for theme: {0}", theme));
    }

    /**
     * Applies the CSS file with the same name as the class to the given node.
     *
     * @param node The node to apply the CSS to. The node must have a class with the same name as the
     *     CSS file. The CSS file must be located in the {@link #STYLING_PATH}.
     */
    public static void applyStyle(Parent node, Class<?> clazz) {

        URL resource = clazz.getResource(getStylePath(clazz));

        if (resource != null) node.getStylesheets().add(resource.toExternalForm());
        else
            throw new IllegalStateException(
                    MessageFormat.format("CSS file could not get loaded for class: {0}", clazz));
    }

    private static String getStylePath(Class<?> clazz) {
        return STYLING_PATH + clazz.getSimpleName() + STYLING_EXTENSION;
    }

    private static String getThemePath(Theme theme) {
        return STYLING_THEMES_PATH + theme.fileName + STYLING_EXTENSION;
    }

    public enum Theme {
        HIBERBEE("Hiberbee", "hiberbee"),
        WIN7_GLASS("Win 7 glass", "win7glass"),
        DRACULA("Dracula", "dracula"),
        MODENA_DARK("Modena", "modenadark");

        private final String name;
        private final String fileName;

        Theme(String name, String fileName) {
            this.name = name;
            this.fileName = fileName;
        }

        public String getName() {
            return name;
        }

        public String getFileName() {
            return fileName;
        }
    }
}

