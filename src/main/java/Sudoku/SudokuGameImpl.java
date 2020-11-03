package Sudoku;

import java.net.InetAddress;
import java.util.*;

import SudokuGenerator.Generator;
import SudokuGenerator.Grid;
import SudokuGenerator.Solver;
import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDirect;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.rpc.ObjectDataReply;
import net.tomp2p.storage.Data;


public class SudokuGameImpl implements SudokuGame {

    final private Peer peer;
    final private PeerDHT dht;
    private String nickname = "";
    private int totalScore = -1;

    public SudokuGameImpl(int idPeer, String masterPeer, final MessageListener listener) throws Exception {
        peer= new PeerBuilder(Number160.createHash(idPeer)).ports(Utils.DEFAULT_MASTER_PORT + idPeer).start();
        dht = new PeerBuilderDHT(peer).start();

        FutureBootstrap fb = peer.bootstrap().inetAddress(InetAddress.getByName(masterPeer)).ports(Utils.DEFAULT_MASTER_PORT).start();
        fb.awaitUninterruptibly();
        if(fb.isSuccess()) {
            peer.discover().peerAddress(fb.bootstrapTo().iterator().next()).start().awaitUninterruptibly();
        }else {
            throw new Exception("Error in master peer bootstrap.");
        }

        peer.objectDataReply(new ObjectDataReply() {
            public Object reply(PeerAddress sender, Object request) throws Exception {
                return listener.parseMessage(request);
            }
        });
    }

    @Override
    public Grid generateNewSudoku(String gameName) {
        FutureGet futureGet = dht.get(Number160.createHash(gameName)).start();  // Controllo se gia esiste un game con lo stesso nome
        futureGet.awaitUninterruptibly();
        if (futureGet.isSuccess() && futureGet.isEmpty()) {
            Generator generator = new Generator();
            Grid sudokuGrid = generator.generate(Utils.NUMBER_EMPTY_CELLS);   // Genero la griglia con il numero di celle vuote specificato (default 4)

            Grid sodukuStartGrid = Grid.emptyGrid();                    // Copio la griglia iniziale perche sudokuGrid sara sovrascritta dalla soluzione
            for (int row = 0; row < sudokuGrid.getSize(); row++) {
                for (int column = 0; column < sudokuGrid.getSize(); column++) {
                    int cellValue = sudokuGrid.getCell(row,column).getValue();
                    sodukuStartGrid.getCell(row,column).setValue(cellValue);
                }
            }

            Solver solver = new Solver();
            try {
                solver.solve(sudokuGrid);       // Risolvo la griglia. Puo lanciare una eccezione.

                Grid sudokuSharedGrid = Grid.emptyGrid();
                for (int row = 0; row < sodukuStartGrid.getSize(); row++) {      // Faccio una ulteriore copia per inizializzare la griglia in comune
                    for (int column = 0; column < sodukuStartGrid.getSize(); column++) {
                        int cellValue = sodukuStartGrid.getCell(row,column).getValue();
                        sudokuSharedGrid.getCell(row,column).setValue(cellValue);
                    }
                }

                MatchData match = new MatchData(sodukuStartGrid,sudokuSharedGrid,sudokuGrid,new HashMap<String,Player>());
                dht.put(Number160.createHash(gameName)).data(new Data(match)).start().awaitUninterruptibly();   // Salvo i dati del match
                return sodukuStartGrid;
            } catch (Exception e) {
                System.err.println("Error: Grid not solvable. Retry");
            }
        }
        return null;
    }

    @Override
    public boolean join(String gameName, String user) {
        try {
            FutureGet futureGetNickname = dht.get(Number160.createHash(user)).start();  // Controllo se esiste un player con lo stesso nome
            futureGetNickname.awaitUninterruptibly();
            if (futureGetNickname.isSuccess() && futureGetNickname.isEmpty()) {
                FutureGet futureGetGame = dht.get(Number160.createHash(gameName)).start();  // Controllo se esiste un game per entrare
                futureGetGame.awaitUninterruptibly();
                if (futureGetGame.isSuccess() && !futureGetGame.isEmpty()) {
                    Player me = new Player(dht.peer().peerAddress(),0);
                    MatchData match;
                    match = (MatchData) futureGetGame.dataMap().values().iterator().next().object();
                    match.addPlayer(user,me);   // Mi "registro" al match per i messaggi della partita. Map nickname -> Player (peer,punti)
                    Grid startGrid = match.getStartGrid();
                    dht.put(Number160.createHash(user)).data(new Data(startGrid)).start().awaitUninterruptibly();  // Salvo i dati del player (griglia iniziale)
                    dht.put(Number160.createHash(gameName)).data(new Data(match)).start().awaitUninterruptibly();   // Salvo i dati del match (player aggiornati)
                    return true;
                }
            }
        } catch (Exception e) {
            System.err.println("Error in join");
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Grid getSudoku(String gameName) {
        try {
            FutureGet futureGetGame = dht.get(Number160.createHash(gameName)).start();  // Controllo se esiste il game
            futureGetGame.awaitUninterruptibly();
            if (futureGetGame.isSuccess() && !futureGetGame.isEmpty()) {
                FutureGet futureGetData = dht.get(Number160.createHash(nickname)).start();  // Ottengo i miei dati
                futureGetData.awaitUninterruptibly();
                Grid localGrid;
                localGrid = (Grid) futureGetData.dataMap().values().iterator().next().object();
                return localGrid;
            }
        } catch (Exception e) {
            System.err.println("Error in finding the game");
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Integer placeNumber(String gameName, int i, int j, int number) {
        try {
            FutureGet futureGetGame = dht.get(Number160.createHash(gameName)).start();  // Controllo se esiste il game
            futureGetGame.awaitUninterruptibly();
            if (futureGetGame.isSuccess() && !futureGetGame.isEmpty()) {
                MatchData match;
                match = (MatchData) futureGetGame.dataMap().values().iterator().next().object();    // Ottengo i dati del match
                Grid solutionGrid = match.getSolvedGrid();  // Griglia risolta per vedere se il numero corrisponde alla soluzione
                Grid sharedGrid = match.getSharedGrid();    // Griglia in comune per vedere se il numero e stato gia inserito da altri

                /*
                System.out.println("-- Griglia risolta:");
                System.out.println(solutionGrid);

                System.out.println("-- Griglia in comune:");
                System.out.println(sharedGrid);
                 */

                HashMap<String,Player> players;
                players = match.getPlayers();    // Player del match per notificarli della mia mossa

                totalScore = players.get(nickname).getTotalScore();     // Ottengo il mio punteggio

                FutureGet futureGetData = dht.get(Number160.createHash(nickname)).start();
                futureGetData.awaitUninterruptibly();
                Grid localGrid;
                localGrid = (Grid) futureGetData.dataMap().values().iterator().next().object();     // Ottengo la mia griglia

                int point = 0;
                boolean matchIsFinished = false;

                if (!localGrid.getCell(i,j).isEmpty()) {    // Cella locale non vuota
                    point = -1;     // Cella locale non vuota. Sto cercando di modificare un numero gia presente, quindi -1
                } else {
                    if (number == solutionGrid.getCell(i,j).getValue()) {    // Numero giusto ma devo vedere se e gia stato preso
                        if (sharedGrid.getCell(i,j).isEmpty()) {
                            point = 1;  // Cella shared vuota, numero corrisponde alla soluzione e non e gia stato preso
                            sharedGrid.getCell(i,j).setValue(number);   // Devo aggiornare la griglia in comune
                        } else {
                            point = 0;  // Cella shared non vuota, numero corrisponde alla soluzione ma e gia stato preso
                        }
                        localGrid.getCell(i,j).setValue(number);    // Se il numero fa parte della soluzione, in ogni caso aggiorno la mia griglia locale
                        if (!localGrid.getFirstEmptyCell().isPresent()) {   // Controllo se la partita e terminata
                            matchIsFinished = true;
                        }
                    } else {
                        point = -1;     // Il numero non corrisponde alla soluzione
                    }
                }

                if (totalScore == 0 && point == -1) {
                    totalScore = 0;     // Non permetto il punteggio negativo
                } else {
                    totalScore = totalScore + point;
                }

                sendMatchMessages(players, point);  // Mando l'aggiornamento della mia mossa agli altri della partita

                match.setSharedGrid(sharedGrid);
                players.get(nickname).updateTotalScore(totalScore);

                dht.put(Number160.createHash(nickname)).data(new Data(localGrid)).start().awaitUninterruptibly();  // Aggiorno griglia locale
                dht.put(Number160.createHash(gameName)).data(new Data(match)).start().awaitUninterruptibly();  // Aggiorno griglia in comune e punteggio

                if (matchIsFinished) {
                    sendEndGame(players);   // Manda la fine della partita agli altri
                }

                return point;
            }
        } catch (Exception e) {
            System.err.println("Error in placing the number");
            e.printStackTrace();
        }
        return null;
    }

    public boolean sendMatchMessages(HashMap<String,Player> players, int result) {
        String message = nickname + " scores " + result + " pt. Total score: " + totalScore + " pt.";
        try {
            for (Map.Entry<String,Player> playerEntry : players.entrySet()) {            // Mando ai player registrati al match (tranne me) le info sul punteggio
                Player player = playerEntry.getValue();
                if (!player.getPeer().equals(peer.peerAddress())) {
                    FutureDirect futureDirect = dht.peer().sendDirect(player.getPeer()).object(message).start();
                    futureDirect.awaitUninterruptibly();
                }
            }
            return true;
        } catch (Exception e) {
            System.err.println("Error");
        }
        return false;
    }

    public void sendEndGame(HashMap<String,Player> players) {
        LinkedHashMap<String,Player> playersSorted = Utils.sortPlayers(players);    // Ordino i player per punteggio decrescente
        Map.Entry<String,Player> playerWinnerEntry = playersSorted.entrySet().iterator().next(); // Ottengo il primo elemento della LinkedHashMap
        String nicknameWinner = playerWinnerEntry.getKey();
        int scoreWinner = playerWinnerEntry.getValue().getTotalScore();

        String message = "Game finished. Winner: " + nicknameWinner + " -- Score: " + scoreWinner;
        try {
            for (Map.Entry<String,Player> playerEntry : players.entrySet()) {            // Mando ai player registrati al match (tranne me) la fine della partita
                Player player = playerEntry.getValue();
                if (!player.getPeer().equals(peer.peerAddress())) {
                    FutureDirect futureDirect1 = dht.peer().sendDirect(player.getPeer()).object(message).start();
                    futureDirect1.awaitUninterruptibly();
                    FutureDirect futureDirect2 = dht.peer().sendDirect(player.getPeer()).object(Utils.GAME_FINISHED).start();
                    futureDirect2.awaitUninterruptibly();
                }
            }
            System.out.println(message);
            System.exit(0);     // Termino anche io. Termino a parte altrimenti potrei terminare prima di mandare il messaggio ad altri
        } catch (Exception e) {
            System.err.println("Error");
        }
    }

    public boolean leaveNetwork() {
        dht.peer().announceShutdown().start().awaitUninterruptibly();
        return true;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public int getTotalScore() {
        return totalScore;
    }
}
