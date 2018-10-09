package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.nio.charset.Charset;

public class Main extends Application {

    static Controller c;
    static Stage s;

    @Override
    public void start(Stage primaryStage) throws Exception{

        URL location = getClass().getResource("sample.fxml");
        FXMLLoader loader = createFXMLLoader(location);
        Parent root = loader.load(location.openStream());
        c = (Controller)loader.getController();
        s=primaryStage;
        primaryStage.setTitle("ClassifyImg");
        primaryStage.setScene(new Scene(root, 500, 300));
        primaryStage.setMaximized(true);
        primaryStage.setOnCloseRequest(e->System.exit(0));
        primaryStage.getScene().getStylesheets().clear();
        primaryStage.getScene().getStylesheets().add("file:///"+new File("resources/main.css").getAbsolutePath().replace("\\", "/"));

        primaryStage.show();
    }

    private FXMLLoader createFXMLLoader(URL location) {
        return new FXMLLoader(location, null, new JavaFXBuilderFactory(), null, Charset.forName(FXMLLoader.DEFAULT_CHARSET_NAME));
    }


    public static Controller getController(){
        return c;
    }
    //TODO - this probably is useless, idk
    static Stage getStage(){return s;}

    public static void main(String[] args) {
        launch(args);
    }
}
