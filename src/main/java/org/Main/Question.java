package org.Main;

import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDate;
import java.util.List;
import java.util.Map;

public class Question {
    private int id;
    private String question;
    private Map<String, String> answers;
    private String correctAnswer;
    private List<Log> logs;



    public List<Log> getLogOfToday() {
        return logs.stream()
                .filter(localTimeDate -> localTimeDate.getTimestamp().toLocalDate().isEqual(ChronoLocalDate.from(LocalDateTime.now())))
                .toList();
    }
    public int getAskedCount() {
        return logs.size();
    }
    public int getCorrectAnswerCount() {
        return (int) logs.stream()
                .filter(Log::isCorrect)
                .count();
    }
    public double getConfidence() {
        double correctAnswerCount = getCorrectAnswerCount();
        double askedCount = getAskedCount();
        if (askedCount < 5 || correctAnswerCount == 0) {
            return 0;
        }
        return correctAnswerCount / askedCount;
    }

    public void addLog(Log log) {
        logs.add(log);
    }
    public void clearLogs() {
        logs.clear();
    }

    public int getId() {
        return id;
    }
    public String getQuestion() {
        return question;
    }
    public String getCorrectAnswer() {
        return correctAnswer;
    }
    public List<Log> getLogs() {
        return logs;
    }
    public Map<String, String> getAnswers() {
        return answers;
    }
}
