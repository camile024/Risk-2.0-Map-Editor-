

import javafx.fxml.FXMLLoader;

public abstract class FXLoader {

    public static FXMLLoader getLoader(String path) {
        return new FXMLLoader(FXLoader.class.getClassLoader().getResource(path));
    }
}
