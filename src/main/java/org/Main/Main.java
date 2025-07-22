package org.Main;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class Main extends Application {
    private BorderPane root;
    private List<Button> navButtons = new ArrayList<Button>();

    @Override
    public void start(Stage stage) throws Exception {
        root = new BorderPane();

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

        root.setTop(navbar);

        showHome();

        Scene scene = new Scene(root, 400, 300);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Question -- Quiz");
        stage.show();
    }

    private void selectButton(Button button) {
        for (Button b: navButtons) {
            b.getStyleClass().remove("selected");
        }
        button.getStyleClass().add("selected");
    }

    private void showHome() {
        GridPane answers = ButtonGrid();
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Screenshot (1).png")));
        ImageView view = new ImageView(image);
        view.setPreserveRatio(true);
        view.setSmooth(true);

        StackPane pane = new StackPane(view);
        pane.setStyle("-fx-background-color: #9ED; -fx-border-color: red;");

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