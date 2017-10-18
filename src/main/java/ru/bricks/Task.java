package main.java.ru.bricks;

/**
 * Created by BODY on 15.10.2017.
 */
public class Task {
    private Procedure procedure;
    private State state;

    public Task(Procedure procedure, State state) {
        this.procedure = procedure;
        this.state = state;
    }

    public Procedure getProcedure() {
        return procedure;
    }

    public State getState() {
        return state;
    }
}
