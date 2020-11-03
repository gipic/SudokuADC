package Sudoku;

import net.tomp2p.peers.PeerAddress;

import java.io.Serializable;

public class Player implements Serializable, Comparable<Player>{
    PeerAddress peer;
    int totalScore;

    public Player(PeerAddress peer, int totalScore) {
        this.peer = peer;
        this.totalScore = totalScore;
    }

    public PeerAddress getPeer() {
        return peer;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void updateTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    @Override
    public int compareTo(Player player) {
        int result;
        result = player.getTotalScore() - totalScore;   // Per l'ordinamento decrescente del punteggio quando termina il match
        return result;
    }

}


