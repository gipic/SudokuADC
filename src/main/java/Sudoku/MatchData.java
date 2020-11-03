package Sudoku;

import SudokuGenerator.Grid;


import java.io.Serializable;
import java.util.HashMap;


public class MatchData implements Serializable {
    private Grid startGrid;
    private Grid sharedGrid;
    private Grid solvedGrid;
    private HashMap<String,Player> players;

    public MatchData(Grid startGrid, Grid sharedGrid, Grid solvedGrid, HashMap<String,Player> players) {
        this.startGrid = startGrid;
        this.sharedGrid = sharedGrid;
        this.solvedGrid = solvedGrid;
        this.players = players;
    }

    public Grid getStartGrid() {
        return startGrid;
    }

    public Grid getSharedGrid() {
        return sharedGrid;
    }

    public Grid getSolvedGrid() {
        return solvedGrid;
    }

    public HashMap<String,Player> getPlayers() {
        return players;
    }

    public void addPlayer(String nickname, Player player) {
        players.put(nickname,player);
    }

    public void setSharedGrid(Grid sharedGrid) {
        this.sharedGrid = sharedGrid;
    }
}
