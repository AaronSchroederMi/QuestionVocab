package org.Main;

import java.time.LocalDateTime;

public class Log {
    private LocalDateTime timestamp;
    private boolean correct;

    Log(LocalDateTime time, boolean correctAnswer) {
        this.timestamp = time;
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
