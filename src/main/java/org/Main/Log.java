package org.Main;

import java.time.LocalDateTime;

public class Log {
    private LocalDateTime timestamp;
    private boolean correct;

    Log(boolean correctAnswer) {
        this.timestamp = LocalDateTime.now();
        this.correct = correctAnswer;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    public boolean isCorrect() {
        return correct;
    }
    public String toString() {
        return "Time: " + timestamp + ", Correct: " + correct;
    }
}
