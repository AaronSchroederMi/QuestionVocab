package org.Main;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import javafx.application.Application;

import javafx.event.Event;
import javafx.geometry.Pos;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Main extends Application {
    private Stage primaryStage;
    private BorderPane root;
    private final Label loadedInfo = new Label();
    private final Label questionLabel = new Label();

    private final List<Button> navButtons = new ArrayList<>();

    private Path imageSource;
    private File imageDir;
    private File[] loadedImages = new File[0];
    private int imageCount = 0;

    private final Set<Path> quizPaths = new HashSet<>();
    private final Set<File> quizFiles = new HashSet<>();
    private final List<ArrayList<Question>> questions = new ArrayList<>();
    private int upperQuarterQuestionSeed;


    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        root = new BorderPane();

        HBox navbar = navbar();
        root.setTop(navbar);

        showHome();

        Scene scene = new Scene(root, 700, 500);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/style.css")).toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Question -- Quiz");
        stage.setMinWidth(600);
        stage.setMinHeight(500);
        stage.show();
    }

    private GridPane ButtonGrid() {
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

        //---Actions---
        btn1.setOnAction(this::actionCheckAnswer);
        btn2.setOnAction(this::actionCheckAnswer);
        btn3.setOnAction(this::actionCheckAnswer);
        btn4.setOnAction(this::actionCheckAnswer);
        //---Actions---

        if (!questions.isEmpty()) {
            List<Question> tmp = new ArrayList<>(questions.stream().flatMap(List::stream).toList());
            tmp.sort(Comparator.comparing(Question::getConfidence).reversed());
            upperQuarterQuestionSeed = (int) (Math.random() * (tmp.size() * 0.25));
            System.out.println(upperQuarterQuestionSeed);
            System.out.println(tmp.get(upperQuarterQuestionSeed).getConfidence());

            btn1.setText(tmp.get(upperQuarterQuestionSeed).getAnswers().get("A"));
            btn2.setText(tmp.get(upperQuarterQuestionSeed).getAnswers().get("B"));
            btn3.setText(tmp.get(upperQuarterQuestionSeed).getAnswers().get("C"));
            btn4.setText(tmp.get(upperQuarterQuestionSeed).getAnswers().get("D"));

            questionLabel.setText(tmp.get(upperQuarterQuestionSeed).getQuestion());
        }

        grid.getColumnConstraints().addAll(col1, col2);
        return grid;
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
        navbar.getChildren().addAll(menuList, loadedInfo, spacer, home, stats, settings, about);

        //--- Actions ---
        addQuiz.setOnAction(_ -> actionAddQuiz());
        removeQuiz.setOnAction(_ -> actionRemoveQuiz());
        resetQuizStats.setOnAction(_ -> actionResetQuizStats());
        addImageDirectory.setOnAction(_ -> actionAddImageDirectory());
        removeImageDirectory.setOnAction(_ -> actionRemoveImageDirectory());

        home.setOnAction(_ -> showHome());
        stats.setOnAction(_ -> showStats());
        settings.setOnAction(_ -> showSettings());
        about.setOnAction(_ -> showAbout());
        //--- Action ---
        return navbar;
    }

    private void actionCheckAnswer(Event source) {
        System.out.println(source);
    }
    private void actionAddQuiz() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open a Quiz (JSON)");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        File selectedFile = fileChooser.showOpenDialog(primaryStage);

        if (selectedFile != null) {
            quizPaths.add(Path.of(selectedFile.getAbsolutePath()));
            quizFiles.add(selectedFile);

            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>)
                            (json, _, context) -> LocalDateTime.parse(json.getAsString()))
                    .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>)
                            (dateTime, _, context) -> new JsonPrimitive(dateTime.format(formatter)))
                    .setPrettyPrinting()
                    .create();

            Type questionListType = new TypeToken<ArrayList<Question>>() {}.getType();
            for (Path path : quizPaths) {
                FileReader fileReader;
                try {
                    fileReader = new FileReader(String.valueOf(path));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    quizPaths.remove(path);
                    quizFiles.remove(path.toFile());
                    break;
                }
                questions.add(gson.fromJson(fileReader, questionListType));
            }
            showHome();
        }
    }
    private void actionRemoveQuiz() {

    }
    private void actionResetQuizStats() {

    }
    private void actionAddImageDirectory() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Directory");
        File selectedDirectory = directoryChooser.showDialog(primaryStage);

        if (selectedDirectory != null) {
            imageSource = Path.of(selectedDirectory.getAbsolutePath());
            imageDir = selectedDirectory;
            loadedImages = imageDir.listFiles();
            if (loadedImages != null) {
                loadedImages = Arrays.stream(loadedImages)
                        .filter(image -> image.getName().endsWith(".jpg")
                                || image.getName().endsWith(".png")
                                || image.getName().endsWith(".jpeg")
                        )
                        .toArray(File[]::new);
                imageCount = loadedImages.length;
                loadedInfo.setText("Loaded " + imageCount + " images, from: " + imageDir.getName());
            }
        }
        showHome();
    }
    private void actionRemoveImageDirectory() {
        imageSource = null;
        imageDir = null;
        loadedImages = new File[0];
        imageCount = 0;
        loadedInfo.setText("");
        showHome();
    }

    private void showHome() {
        GridPane answers = ButtonGrid();

        questionLabel.setAlignment(Pos.CENTER);

        ImageView view = new ImageView();

        int ran = (int) (Math.random() * imageCount);
        if (loadedImages.length != 0) {
            Image image = new Image(loadedImages[ran].toURI().toString());
            view.setImage(image);
        }

        view.setPreserveRatio(true);
        view.setSmooth(true);

        StackPane pane = new StackPane(view);

        VBox layout = new VBox(pane, questionLabel, answers);
        VBox.setVgrow(pane, Priority.ALWAYS);

        view.fitWidthProperty().bind(root.widthProperty());
        view.fitHeightProperty().bind(pane.heightProperty().subtract(answers.heightProperty()));

        root.setCenter(layout);
    }
    private void showStats() {
        root.setCenter(new TextField("Stats"));
    }
    private void showSettings() {
        root.setCenter(new TextField("Settings"));
    }
    private void showAbout() {
        root.setCenter(new TextField("ABOUT"));
    }

    public static void main(String[] args) {
        launch(args);
    }
}