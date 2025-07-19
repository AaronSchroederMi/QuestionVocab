package org.Main;

import java.time.LocalDateTime;

public class Log {
    private final LocalDateTime time;
    private final boolean correctAnswer;

    Log(boolean correctAnswer) {
        this.correctAnswer = correctAnswer;
        this.time = LocalDateTime.now();
    }

    Log(LocalDateTime time, boolean correctAnswer) {
        this.time = time;
        this.correctAnswer = correctAnswer;
    }

    public LocalDateTime getTime() {
        return time;
    }
    public boolean isCorrectAnswer() {
        return correctAnswer;
    }
}
