package org.Main;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import javafx.animation.PauseTransition;
import javafx.application.Application;

import javafx.geometry.Bounds;
import javafx.geometry.Pos;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Main extends Application {
    private Stage primaryStage;
    private BorderPane root;
    private String loadedImage = "";
    private String loadedFiles = "";
    private final Label loadedInfo = new Label(loadedImage + loadedFiles);
    private final Label questionLabel = new Label();

    private final List<Button> navButtons = new ArrayList<>();
    private final List<Button> answerButtons = new ArrayList<>();

    private Path imageSource;
    private File imageDir;
    private File[] loadedImages = new File[0];
    private int imageCount = 0;

    private final List<Path> quizPaths = new ArrayList<>();
    private final List<File> quizFiles = new ArrayList<>();
    private final List<ArrayList<Question>> questions = new ArrayList<>();
    private int upperQuarterQuestionSeed;
    private int ranImageIndex;


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
        btn1.setStyle("-fx-padding: 20 0 20 0");
        Button btn2 = new Button("B");
        btn2.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        btn2.setStyle("-fx-padding: 20 0 20 0");
        Button btn3 = new Button("C");
        btn3.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        btn3.setStyle("-fx-padding: 20 0 20 0");
        Button btn4 = new Button("D");
        btn4.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        btn4.setStyle("-fx-padding: 20 0 20 0");

        answerButtons.clear();
        answerButtons.add(btn1);
        answerButtons.add(btn2);
        answerButtons.add(btn3);
        answerButtons.add(btn4);

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
        btn1.setOnAction(_ -> actionCheckAnswer("A"));
        btn2.setOnAction(_ -> actionCheckAnswer("B"));
        btn3.setOnAction(_ -> actionCheckAnswer("C"));
        btn4.setOnAction(_ -> actionCheckAnswer("D"));
        //---Actions---

        if (!questions.isEmpty()) {
            List<Question> tmp = new ArrayList<>(questions.stream().flatMap(List::stream).toList());
            tmp.sort(Comparator.comparing(Question::getConfidence));
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
                new SeparatorMenuItem(),
                addImageDirectory,
                removeImageDirectory
        );

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
    private void writeToJsons(Question currentQuestion) {
        try {
            int relevantIndex = questions.size();
            for (int i = 0; i < questions.size(); i++) {
                for (Question question : questions.get(i)) {
                    if (question == currentQuestion) {
                        relevantIndex = i;
                        break;
                    }
                }
            }
            if (relevantIndex == questions.size()) throw new IOException();

            String fileToUpdate = quizPaths.get(relevantIndex).toString();
            Gson gson = getGsonDateTimeFormatter();
            FileWriter fw = new FileWriter(fileToUpdate);
            gson.toJson(questions.get(relevantIndex), fw);
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private Gson getGsonDateTimeFormatter() {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>)
                        (json, _, _) -> LocalDateTime.parse(json.getAsString()))
                .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>)
                        (dateTime, _, _) -> new JsonPrimitive(dateTime.format(formatter)))
                .setPrettyPrinting()
                .create();
    }

    private void actionCheckAnswer(String answer) {
        if (questions.isEmpty()) return;

        System.out.println(answerButtons);
        answerButtons.forEach(button -> button.setDisable(true));
        List<Question> tmp = new ArrayList<>(questions.stream().flatMap(List::stream).toList());
        tmp.sort(Comparator.comparing(Question::getConfidence));
        Question currentQuestion = tmp.get(upperQuarterQuestionSeed);
        currentQuestion.addLog(new Log(currentQuestion.isCorrect(answer)));

        if (currentQuestion.isCorrect("A")) answerButtons.get(0).setStyle("-fx-background-color: lightGreen; -fx-border-color: green;");
        if (currentQuestion.isCorrect("B")) answerButtons.get(1).setStyle("-fx-background-color: lightGreen; -fx-border-color: green;");
        if (currentQuestion.isCorrect("C")) answerButtons.get(2).setStyle("-fx-background-color: lightGreen; -fx-border-color: green;");
        if (currentQuestion.isCorrect("D")) answerButtons.get(3).setStyle("-fx-background-color: lightGreen; -fx-border-color: green;");

        PauseTransition pause = new PauseTransition(Duration.seconds(5));
        pause.setOnFinished(_ -> {ranImageIndex = (int) (Math.random() * imageCount); showHome();});
        pause.play();

        writeToJsons(currentQuestion);

        System.out.println(currentQuestion.getLogs());
    }
    private void actionAddQuiz() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open a Quiz (JSON)");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        File selectedFile = fileChooser.showOpenDialog(primaryStage);

        if (selectedFile != null) {
            Path pathOfSelectedFile = Path.of(selectedFile.getAbsolutePath());
            if (quizPaths.contains(pathOfSelectedFile)) return;
            quizPaths.add(pathOfSelectedFile);
            quizFiles.add(selectedFile);

            Gson gson = getGsonDateTimeFormatter();

            Type questionListType = new TypeToken<ArrayList<Question>>() {}.getType();
            questions.clear();
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
            loadedFiles = "loaded Files: " + quizFiles + " (" + questions.stream().flatMap(List::stream).toList().size() + ")";
            loadedInfo.setText(loadedImage + "; " + loadedFiles);
            showHome();
        }
    }
    private void actionRemoveQuiz() {
        List<File> currentQuizFiles = new ArrayList<>(quizFiles);
        if (currentQuizFiles.isEmpty()) return;
        ContextMenu contextMenu = new ContextMenu();
        contextMenu.setStyle("-fx-padding: 8 12 8 12");

        for (File quizFile : currentQuizFiles) {
            MenuItem quizItem = new MenuItem(quizFile.getName());
            quizItem.setOnAction(_ -> {
                int tmp = quizPaths.indexOf(quizFile.toPath());
                quizPaths.remove(tmp);
                questions.remove(tmp);
                quizFiles.remove(quizFile);
                questionLabel.setText("");

                loadedFiles = "loaded Quiz: " + quizFiles;
                if (quizFiles.isEmpty()) loadedFiles = "";
                loadedInfo.setText(loadedImage + loadedFiles);
                showHome();
            });
            contextMenu.getItems().add(quizItem);
        }

        Bounds bounds = root.localToScreen(root.getBoundsInLocal());
        double centerX = bounds.getMinX() + bounds.getWidth() / 2;
        double centerY = bounds.getMinY() + bounds.getHeight() / 2;

        contextMenu.show(root, centerX, centerY);
    }
    private void actionResetQuizStats() {
        for (ArrayList<Question> tmp : questions) {
            for (Question question : tmp) {
                question.clearLogs();
            }
        }
        showHome();
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
                loadedImage = "Loaded " + imageCount + " images, from: " + imageDir.getName();
                loadedInfo.setText(loadedImage + "; " + loadedFiles);
            }
        }
        showHome();
    }
    private void actionRemoveImageDirectory() {
        imageSource = null;
        imageDir = null;
        loadedImages = new File[0];
        imageCount = 0;
        loadedImage = "";
        loadedInfo.setText(loadedImage + loadedFiles);
        showHome();
    }

    private void showHome() {
        GridPane answers = ButtonGrid();

        questionLabel.setStyle("-fx-alignment: center; -fx-font-size: 16px; -fx-font-weight: bold;");

        ImageView view = new ImageView();

        if (loadedImages.length != 0) {
            Image image = new Image(loadedImages[ranImageIndex].toURI().toString());
            view.setImage(image);
        }

        view.setPreserveRatio(true);
        view.setSmooth(true);

        StackPane pane = new StackPane(view);

        VBox layout = new VBox(questionLabel, pane, answers);
        VBox.setVgrow(pane, Priority.ALWAYS);
        layout.setAlignment(Pos.CENTER);

        view.fitWidthProperty().bind(root.widthProperty());
        view.fitHeightProperty().bind(pane.heightProperty().subtract(answers.heightProperty().subtract(120)));

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