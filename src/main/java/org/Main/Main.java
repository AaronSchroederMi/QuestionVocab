package org.Main;

import javafx.application.Application;

import javafx.geometry.Pos;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Main extends Application {
    private BorderPane root;
    private List<Button> navButtons = new ArrayList<Button>();

    @Override
    public void start(Stage stage) throws Exception {
        root = new BorderPane();

        HBox navbar = navbar();
        root.setTop(navbar);

        showHome();

        Scene scene = new Scene(root, 400, 300);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Question -- Quiz");
        stage.setMinHeight(300);
        stage.setMinWidth(300);
        stage.show();
    }

    private HBox navbar() {
        HBox navbar = new HBox();
        navbar.getStyleClass().add("navbar");

        MenuItem addQuiz = new MenuItem("Add Quiz");
        MenuItem removeQuiz = new MenuItem("Remove Quiz");
        MenuItem resetQuizStats = new MenuItem("Reset Quiz Stats");
        MenuItem addImageDirectory = new MenuItem("Add Image Directory");
        MenuItem removeImageDirectory = new MenuItem("Remove Image Directory");
        MenuButton menuList = new MenuButton("File", null,
                addQuiz,
                removeQuiz,
                resetQuizStats,
                addImageDirectory,
                removeImageDirectory);

        Button home = new Button("Home");
        Button stats = new Button("Stats");
        Button settings = new Button("Settings");
        Button about = new Button("About");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        navButtons.addAll(List.of(home, stats, settings, about));
        navbar.getChildren().addAll(menuList, spacer, home, stats, settings, about);

        home.setOnAction(e -> showHome());
        stats.setOnAction(e -> showStats());
        settings.setOnAction(e -> showSettings());
        about.setOnAction(e -> showAbout());
        return navbar;
    }

    private void addQuiz() {

    }
    private void removeQuiz() {

    }
    private void resetQuizStats() {

    }
    private void addImageDirectory() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Directory");
        File selectedDirectory = directoryChooser.showDialog(primaryStage);

        if (selectedDirectory != null) {
            imageSource = Path.of(selectedDirectory.getAbsolutePath());
            imageDir = new File(imageSource.toString());
            loadedImages = imageDir.listFiles();
        }
        showHome();
    }
    private void removeImageDirectory() {
        imageSource = null;
        imageDir = null;
        loadedImages = new File[0];
        showHome();
    }
    private void showHome() {
        GridPane answers = ButtonGrid();
        ImageView view = new ImageView();

        int ran = (int) (Math.random() * loadedImages.length);
        if (loadedImages.length != 0) {
            Image image = new Image(loadedImages[ran].toURI().toString());
            view.setImage(image);
        }

        view.setPreserveRatio(true);
        view.setSmooth(true);

        StackPane pane = new StackPane(view);

        VBox layout = new VBox(pane, answers);
        VBox.setVgrow(pane, Priority.ALWAYS);

        view.fitWidthProperty().bind(root.widthProperty());
        view.fitHeightProperty().bind(pane.heightProperty().subtract(answers.heightProperty()));

        root.setCenter(layout);
    }
    private static GridPane ButtonGrid() {
        Button btn1 = new Button("A");
        btn1.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        btn1.getStyleClass().add("A");
        Button btn2 = new Button("B");
        btn2.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        btn2.getStyleClass().add("B");
        Button btn3 = new Button("C");
        btn3.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        btn3.getStyleClass().add("C");
        Button btn4 = new Button("D");
        btn4.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        btn4.getStyleClass().add("D");

        GridPane grid = new GridPane();
        grid.add(btn1, 0, 0);
        grid.add(btn2, 1, 0);
        grid.add(btn3, 0, 1);
        grid.add(btn4, 1, 1);
        grid.setAlignment(Pos.BOTTOM_CENTER);
        grid.setStyle("-fx-padding: 5;");

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(100);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(100);
        RowConstraints row1 = new RowConstraints();
        row1.setPercentHeight(30);
        RowConstraints row2 = new RowConstraints();
        row2.setPercentHeight(30);

        grid.getColumnConstraints().addAll(col1, col2);
        grid.getRowConstraints().addAll(row1, row2);
        return grid;
    }

    private void showAbout() {

    }

    private void showSettings() {

    }

    private void showStats() {

    }

    public static void main(String[] args) {
        launch(args);
    }
}