package org.Main;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import javafx.animation.PauseTransition;
import javafx.application.Application;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.Pair;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
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
    private List<String> listToDisplay = new ArrayList<>();
    private LineChart<Number, Number> lineChart;
    private int upperQuarterQuestionSeed;
    private int ranImageIndex;


    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        root = new BorderPane();

        HBox navbar = createNavbar();
        root.setTop(navbar);

        showHome();

        Scene scene = new Scene(root, 700, 500);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/style.css")).toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Question -- Quiz");
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icon.png"))));
        stage.setMinWidth(600);
        stage.setMinHeight(500);
        stage.show();
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
    private XYChart.Series<Number, Number> generateData(String label) {
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName(label);

        // Flatten questions and logs
        List<Pair<Log, Question>> logsWithQuestions = questions.stream()
                .flatMap(Collection::stream)
                .flatMap(q -> q.getLogs().stream().map(log -> new Pair<>(log, q)))
                .sorted(Comparator.comparing(p -> p.getKey().getTimestamp()))
                .toList();

        if (logsWithQuestions.isEmpty()) return series;

        LocalDateTime minTime = logsWithQuestions.getFirst().getKey().getTimestamp();

        int totalQuestions = (int) questions.stream().mapToLong(List::size).sum();
        Set<Question> seen = new HashSet<>();

        int done = 0;
        int correctCount = 0;
        int wrongCount = 0;

        for (int i = 0; i < logsWithQuestions.size(); i++) {
            Log log = logsWithQuestions.get(i).getKey();
            Question question = logsWithQuestions.get(i).getValue();

            done++;
            if (log.isCorrect()) correctCount++;
            else wrongCount++;

            long x = ChronoUnit.SECONDS.between(minTime, log.getTimestamp());
            double y;

            switch (label) {
                case "Unasked Questions (%)" -> {
                    seen.add(question);
                    y = ((double)(totalQuestions - seen.size()) / totalQuestions) * 100;
                }
                case "Done Questions (%)" -> {
                    seen.add(question);
                    y = ((double) seen.size() / totalQuestions) * 100;
                }
                case "Confidence (%)" -> y = questions.stream()
                        .flatMap(List::stream)
                        .mapToDouble(Question::getConfidence)
                        .average()
                        .orElse(0) * 100.0;
                case "Wrong Answers (%)" -> y = (wrongCount * 100.0) / done;
                case "Right Answers (%)" -> y = (correctCount * 100.0) / done;
                default -> y = 0;
            }

            series.getData().add(new XYChart.Data<>(x, y));
        }

        return series;
    }
    private Gson getGsonDateTimeFormatter() {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>)
                        (json, unused1, unused2) -> LocalDateTime.parse(json.getAsString()))
                .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>)
                        (dateTime, unused1, unused2) -> new JsonPrimitive(dateTime.format(formatter)))
                .setPrettyPrinting()
                .create();
    }

    private void actionCheckAnswer(String answer) {
        if (questions.isEmpty()) return;

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
        pause.setOnFinished(unused1 -> {ranImageIndex = (int) (Math.random() * imageCount); showHome();});
        pause.play();

        writeToJsons(currentQuestion);
    }
    private void actionAddQuiz() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Quiz Files (JSON)");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));

        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(primaryStage);

        if (selectedFiles != null && !selectedFiles.isEmpty()) {
            Gson gson = getGsonDateTimeFormatter();
            Type questionListType = new TypeToken<ArrayList<Question>>() {}.getType();

            for (File selectedFile : selectedFiles) {
                Path pathOfSelectedFile = selectedFile.toPath();
                if (quizPaths.contains(pathOfSelectedFile)) continue;

                try (FileReader fileReader = new FileReader(selectedFile)) {
                    quizPaths.add(pathOfSelectedFile);
                    quizFiles.add(selectedFile);

                    List<Question> loadedQuestions = gson.fromJson(fileReader, questionListType);
                    questions.add((ArrayList<Question>) loadedQuestions);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    quizPaths.remove(pathOfSelectedFile);
                    quizFiles.remove(selectedFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            loadedFiles = "loaded Files: " + quizFiles + " (" + questions.stream().mapToLong(List::size).sum() + ")";
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
            MenuItem quizItem = createMenuItem(quizFile);
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
        GridPane answers = createButtonGrid();

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
        layout.setPadding(new Insets(10, 0, 0, 0));
        layout.setAlignment(Pos.CENTER);
        layout.setVisible(false);

        view.fitWidthProperty().bind(root.widthProperty());
        view.fitHeightProperty().bind(pane.heightProperty().subtract(answers.heightProperty().subtract(120)));
        PauseTransition pause = new PauseTransition(Duration.millis(100));
        pause.setOnFinished(unused1 -> layout.setVisible(true));
        pause.play();

        root.setCenter(layout);
    }
    private void showStats() {
        if (quizFiles.isEmpty()) {
            root.setCenter(new Label("No Quiz Files Found"));
            return;
        }

        GridPane graphSpace = new GridPane();
        graphSpace.setAlignment(Pos.CENTER);
        graphSpace.setHgap(10);

        VBox chartPanel = createChartPanel();
        VBox pieChart = createProgressPie();
        VBox listBox = createSelectionBox();

        graphSpace.add(listBox, 0, 0);
        graphSpace.add(pieChart, 1, 0);
        graphSpace.add(chartPanel, 0, 1, 2, 1);

        GridPane.setHgrow(listBox, Priority.ALWAYS);
        GridPane.setHgrow(pieChart, Priority.ALWAYS);
        GridPane.setHgrow(chartPanel, Priority.ALWAYS);

        root.setCenter(graphSpace);
    }
    private void showAbout() {
        VBox aboutContent = new VBox(15);
        aboutContent.setAlignment(Pos.CENTER);
        aboutContent.setPadding(new Insets(40));

        Label title = new Label("A Vocab Quiz Application");

        Label version = new Label("Version 1.0.0");

        Label author = new Label("Developed by Aaron Schröder");

        Label description = new Label(
                "This application is built with JavaFX. " +
                        "It aims to provide an easy way to load vocab-style learning material. " +
                        "The material is loaded from JSON files. (Example in Plan.md) " +
                        "Questions are presented in an order that adapts to the user's performance per question. " +
                        "Images can also be loaded to help maintain user engagement (not necessarily related to the questions)."
        );
        description.setWrapText(true);
        description.setMaxWidth(400);

        Label context = new Label(
                "This was built in roughly a week as a break from university work, which mostly involves Java AWT and Swing for GUIs. " +
                        "To be kind, those are quaint but outdated. " +
                        "The real goal here was to experiment with JavaFX, and for that purpose, even with an on-and-off approach, the app is doing just fine."
        );
        context.setWrapText(true);
        context.setMaxWidth(400);

        Label jsonStruktur = new Label();

        Hyperlink link = new Hyperlink("https://github.com/AaronSchroederMi/QuestionVocab");
        link.setOnAction(e -> {
            try {
                java.awt.Desktop.getDesktop().browse(new java.net.URI(link.getText()));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        aboutContent.getChildren().addAll(title, version, author, description, context, jsonStruktur, link);

        root.setCenter(aboutContent);
    }
    private void showSettings() {
        root.setCenter(new Label("Settings"));
    }

    private GridPane createButtonGrid() {
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
        btn1.setOnAction(unused1 -> actionCheckAnswer("A"));
        btn2.setOnAction(unused1 -> actionCheckAnswer("B"));
        btn3.setOnAction(unused1 -> actionCheckAnswer("C"));
        btn4.setOnAction(unused1 -> actionCheckAnswer("D"));
        //---Actions---

        if (!questions.isEmpty()) {
            List<Question> tmp = new ArrayList<>(questions.stream().flatMap(List::stream).toList());
            tmp.sort(Comparator.comparing(Question::getConfidence));
            upperQuarterQuestionSeed = (int) (Math.random() * (tmp.size() * 0.25));

            btn1.setText(tmp.get(upperQuarterQuestionSeed).getAnswers().get("A"));
            btn2.setText(tmp.get(upperQuarterQuestionSeed).getAnswers().get("B"));
            btn3.setText(tmp.get(upperQuarterQuestionSeed).getAnswers().get("C"));
            btn4.setText(tmp.get(upperQuarterQuestionSeed).getAnswers().get("D"));

            questionLabel.setText(tmp.get(upperQuarterQuestionSeed).getQuestion());
        }

        grid.getColumnConstraints().addAll(col1, col2);
        return grid;
    }
    private HBox createNavbar() {
        HBox navbar = new HBox();
        navbar.getStyleClass().add("createNavbar");

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

        menuList.setMinWidth(55);
        home.setMinWidth(65);
        stats.setMinWidth(55);
        settings.setMinWidth(75);
        about.setMinWidth(65);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        navButtons.addAll(List.of(home, stats, settings, about));
        navbar.getChildren().addAll(menuList, loadedInfo, spacer, home, stats, settings, about);

        //--- Actions ---
        addQuiz.setOnAction(unused1 -> actionAddQuiz());
        removeQuiz.setOnAction(unused1 -> actionRemoveQuiz());
        resetQuizStats.setOnAction(unused1 -> actionResetQuizStats());
        addImageDirectory.setOnAction(unused1 -> actionAddImageDirectory());
        removeImageDirectory.setOnAction(unused1 -> actionRemoveImageDirectory());

        home.setOnAction(unused1 -> showHome());
        stats.setOnAction(unused1 -> showStats());
        settings.setOnAction(unused1 -> showSettings());
        about.setOnAction(unused1 -> showAbout());
        //--- Action ---
        return navbar;
    }
    private MenuItem createMenuItem(File quizFile) {
        MenuItem quizItem = new MenuItem(quizFile.getName());
        quizItem.setOnAction(unused1 -> {
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
        return quizItem;
    }
    private VBox createProgressPie() {

        int doneQuestions = questions.stream()
                .flatMap(List::stream)
                .map(Question::getConfidence)
                .filter(e -> e > 0.8).toList().size();
        int questionCount = questions.stream().flatMap(List::stream).toList().size();

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                new PieChart.Data("Done", doneQuestions),
                new PieChart.Data("Todo", questionCount - doneQuestions)
        );
        PieChart chart = new PieChart(pieData);
        chart.setTitle("Done Questions");
        int tmp = 0;
        if (questionCount != 0)  tmp = (int) ((double) doneQuestions / questionCount * 100);
        Label label = new Label("Progress: " + tmp + " %");
        VBox layout = new VBox(chart, label);
        layout.setAlignment(Pos.CENTER);
        return layout;
    }
    private VBox createSelectionBox() {

        Label listLabel = new Label("Data Source");
        listLabel.setStyle("-fx-font-weight: bold; -fx-padding: 7;");

        ToggleGroup group = new ToggleGroup();

        ListView<String> listQuestions = new ListView<>();
        RadioButton option1 = new RadioButton("Loaded Files");
        option1.setStyle("-fx-padding: 4");
        option1.setOnAction(unused1 -> {
            listToDisplay = quizFiles.stream().map(File::getName).toList();
            ObservableList<String> items = FXCollections.observableArrayList(listToDisplay);
            listQuestions.setItems(items);
        });
        RadioButton option2 = new RadioButton("Loaded Questions");
        option2.setStyle("-fx-padding: 4");
        option2.setOnAction(unused1 -> {
            listToDisplay = questions.stream().flatMap(List::stream).map(Question::getQuestion).toList();
            ObservableList<String> items = FXCollections.observableArrayList(listToDisplay);
            listQuestions.setItems(items);
        });
        RadioButton option3 = new RadioButton("Loaded Images");
        option3.setStyle("-fx-padding: 4");
        option3.setOnAction(unused1 -> {
            listToDisplay = Arrays.stream(loadedImages).map(File::getName).toList();
            ObservableList<String> items = FXCollections.observableArrayList(listToDisplay);
            listQuestions.setItems(items);
        });

        option1.setToggleGroup(group);
        option2.setToggleGroup(group);
        option3.setToggleGroup(group);

        HBox selectionBox = new HBox(option1, option2, option3);
        selectionBox.setAlignment(Pos.CENTER);
        selectionBox.setStyle("-fx-padding: 8 10 8 10");

        //VBox listBox = new VBox(listLabel, selectionBox, listQuestions);
        VBox listBox = new VBox(listLabel, listQuestions, selectionBox);
        listBox.setAlignment(Pos.CENTER);
        return listBox;
    }
    private VBox createChartPanel() {
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Time");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Percentage");

        lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Quiz Statistics");

        CheckBox unaskedShare = new CheckBox("Unasked Questions Share");
        CheckBox doneQuestions = new CheckBox("Done Questions");
        CheckBox confidence = new CheckBox("Overall Confidence");
        CheckBox wrongPercentage = new CheckBox("Wrong %");
        CheckBox rightPercentage = new CheckBox("Right %");

        HBox toggles = new HBox(10, unaskedShare, doneQuestions, confidence, wrongPercentage, rightPercentage);
        toggles.setAlignment(Pos.CENTER);
        toggles.setPadding(new Insets(10));

        Button update = new Button("Update Chart");
        update.setOnAction(unused1 -> {
            lineChart.getData().clear();
            if (unaskedShare.isSelected()) lineChart.getData().add(generateData("Unasked Questions (%)"));
            if (doneQuestions.isSelected()) lineChart.getData().add(generateData("Done Questions (%)"));
            if (confidence.isSelected()) lineChart.getData().add(generateData("Confidence (%)"));
            if (wrongPercentage.isSelected()) lineChart.getData().add(generateData("Wrong Answers (%)"));
            if (rightPercentage.isSelected()) lineChart.getData().add(generateData("Right Answers (%)"));
        });

        VBox chartBox = new VBox(10, toggles, lineChart, update);
        chartBox.setPadding(new Insets(10));
        VBox.setVgrow(lineChart, Priority.ALWAYS);
        return chartBox;
    }

    public static void main(String[] args) {
        launch(args);
    }
}