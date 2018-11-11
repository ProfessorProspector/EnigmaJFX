package cuchaz.enigma;

import cuchaz.enigma.lib.Constants;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class Main extends Application {

	public static Stage stage;
	public static List<String> args;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {
		args = getParameters().getRaw();
		Main.stage = stage;
		FXMLLoader loader = new FXMLLoader(Main.class.getResource("/assets/main.fxml"));
		Parent root = loader.load();
		Scene scene = new Scene(root, 1050, 700);
		scene.getStylesheets().add("/assets/theme/dark.css");
		stage.setScene(scene);
		stage.getIcons().add(new Image("/assets/icon.png"));
		stage.setTitle(Constants.NAME);
		stage.show();
	}
}
