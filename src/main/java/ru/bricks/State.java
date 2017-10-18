package main.java.ru.bricks;

/**
 * Created by BODY on 15.10.2017.
 */
public class State {
    private boolean start;
    private boolean finish;
    private boolean correct;

    public State() {
        this.start = false;
        this.finish = false;
        this.correct = true;
    }

    public boolean isStart() {
        return start;
    }

    public void setStart(boolean start) {
        this.start = start;
    }

    public boolean isFinish() {
        return finish;
    }

    public void setFinish(boolean finish) {
        this.finish = finish;
    }

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }
}
