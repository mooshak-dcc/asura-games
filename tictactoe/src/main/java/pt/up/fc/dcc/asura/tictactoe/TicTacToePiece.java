package pt.up.fc.dcc.asura.tictactoe;

import pt.up.fc.dcc.asura.builder.base.movie.models.MooshakClassification;

import java.io.Serializable;

public class TicTacToePiece implements Serializable {
    private static final long serialVersionUID = 1L;

    char piece;

    int points;
    MooshakClassification classification;
    String observations = "";

    public TicTacToePiece(char piece) {
        this.piece = piece;
    }

    public int getPoints() {
        return piece;
    }

    public MooshakClassification getClassification() {
        return classification;
    }

    public String getObservations() {
        return observations;
    }
}
