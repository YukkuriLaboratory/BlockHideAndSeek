package com.github.yukulab.blockhideandseekmod.game;

public class GameState {

    private static Phase currentState = Phase.IDLE;

    public static void setCurrentState(Phase currentState) {
        GameState.currentState = currentState;
    }

    public static Phase getCurrentState() {
        return currentState;
    }

    public enum Phase {
        IDLE, SELECT_TEAM, PREPARE, RUNNING,
    }
}
