import java.io.IOException;

import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Application extends javafx.application.Application {

	
	
	public static void main(String[] args) {
		launch();
	}
	
	
	@Override
	public void start(Stage mainStage) throws InterruptedException {
		Rectangle2D screenDimensions = Screen.getPrimary().getVisualBounds();
	    /* FXML part */
	    Stage stage = new Stage();
        FXMLLoader loader = FXLoader.getLoader("UiMain.fxml");
        BorderPane pane = null;
        try {
            pane = (BorderPane) loader.load();
        } catch (IOException e1) {
            e1.printStackTrace();
            System.exit(-1);
        }
        MainController uiMain = ((MainController)(loader.getController()));
        
        //stage.setFullScreen(true);
        stage.setX(screenDimensions.getMinX());
        stage.setY(screenDimensions.getMinY());
        stage.setWidth(screenDimensions.getWidth() / 1.5);
        stage.setHeight(screenDimensions.getHeight() / 1.5);
        stage.centerOnScreen();
        stage.setMaximized(true);
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent arg0) {
				System.exit(0);
			}
        	
        });
//        stage.setMinHeight(800);
//        stage.setMinWidth(960);
        Scene scene = new Scene(pane, stage.getWidth(), stage.getHeight());
        
        
        stage.setScene(scene);
        stage.setTitle("Dominators MapEditor");
        //stage.setResizable(false);
        //stage.centerOnScreen();
        
        //uiMain.init(stage, database);
        stage.show();
        /* End of FXML part */
	}
}
