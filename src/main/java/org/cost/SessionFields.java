package org.cost;

public enum SessionFields {
    GAME_NAME("game_name"),
    PLAYER_NAME("player_number");

    private final String text;

    private SessionFields(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
