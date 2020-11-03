package Sudoku;

import java.io.IOException;

import SudokuGenerator.Generator;
import SudokuGenerator.Grid;
import SudokuGenerator.Solver;
import org.beryx.textio.TextIO;
import org.beryx.textio.TextIoFactory;
import org.beryx.textio.TextTerminal;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

public class SudokuMain {

    @Option(name="-m", aliases="--masterip", usage="the master peer ip address", required=true)
    private static String masterIP;     // Indirizzo IP del master peer

    @Option(name="-id", aliases="--identifierpeer", usage="the unique identifier for this peer", required=true)
    private static int peerID;      // Identificatore univoco per questo peer

    public static void main(String[] args) throws Exception {

        SudokuMain main = new SudokuMain();
        final CmdLineParser parser = new CmdLineParser(main);

        try {
            parser.parseArgument(args);
            TextIO textIO = TextIoFactory.getTextIO();
            TextTerminal terminal = textIO.getTextTerminal();
            SudokuGameImpl peer = new SudokuGameImpl(peerID, masterIP, new MessageListenerImpl(peerID));

            terminal.printf("Staring peer id: %d on master node: %s\n", peerID, masterIP);

            boolean gameStarted = false;
            String gameName = "";

            while(!gameStarted) {
                printBeforeGameMenu(terminal);

                int option = textIO.newIntInputReader()
                        .withMaxVal(3)
                        .withMinVal(1)
                        .read("Option");

                switch (option) {
                    case 1:
                        terminal.printf("Enter game name\n");
                        gameName = textIO.newStringInputReader()
                                .withDefaultValue("sudoku" + peerID)
                                .read("Name:");     // Game name di default per il peer k -> sudoku-k
                        Grid startSudoku = peer.generateNewSudoku(gameName);
                        if (startSudoku != null) {
                            terminal.printf("Game '%s' successfully created\n",gameName);
                            terminal.printf("\nEnter nickname\n");
                            String nickname = textIO.newStringInputReader()
                                    .withMinLength(1)
                                    .read("Name:");
                            if(peer.join(gameName,nickname)) {
                                terminal.printf("Successfully joined to '%s' as '%s'\n\n", gameName, nickname);
                                terminal.printf("Sudoku '%s' start grid:\n", gameName);
                                terminal.printf(startSudoku.toString() + "\n");
                                peer.setNickname(nickname);
                                gameStarted = true;     // Sono in gioco, mostro l'altro menu
                            } else {
                                terminal.printf("Error in join\n\n");
                            }

                        }
                        else {
                            terminal.printf("Error in game creation\n\n");
                        }
                        break;
                    case 2:
                        terminal.printf("Enter game name to join\n");
                        gameName = textIO.newStringInputReader()
                                .withMinLength(1)
                                .read("Name:");
                        terminal.printf("\nEnter nickname\n");
                        String nickname = textIO.newStringInputReader()
                                .withMinLength(1)
                                .read("Name:");
                        if(peer.join(gameName,nickname)) {
                            terminal.printf("Successfully joined to '%s' as %s\n\n", gameName, nickname);
                            peer.setNickname(nickname);
                            gameStarted = true;     // Sono in gioco, mostro l'altro menu
                        } else {
                            terminal.printf("Error in join\n\n");
                        }
                        break;
                    case 3:
                        terminal.printf("Are you sure to leave the network?\n");
                        boolean exit = textIO.newBooleanInputReader().withDefaultValue(false).read("exit?");
                        if(exit) {
                            peer.leaveNetwork();
                            System.exit(0);
                        }
                        break;
                    default:
                        break;
                }
            }

            while(true) {
                printInGameMenu(terminal);

                int option = textIO.newIntInputReader()
                        .withMaxVal(3)
                        .withMinVal(1)
                        .read("Option");

                switch (option) {
                    case 1:
                        Grid localGrid = peer.getSudoku(gameName);
                        if (localGrid != null) {
                            terminal.printf("\nActual grid:\n");
                            terminal.printf(localGrid.toString() + "\n");
                        } else {
                            terminal.printf("Error in finding the game\n\n");
                        }
                        break;
                    case 2:
                        terminal.printf("\nEnter row (0-8)\n");
                        int row = textIO.newIntInputReader()
                                .withMinVal(0)
                                .withMaxVal(8)
                                .read("Row:");
                        terminal.printf("\nEnter column (0-8)\n");
                        int col = textIO.newIntInputReader()
                                .withMinVal(0)
                                .withMaxVal(8)
                                .read("Column:");
                        terminal.printf("\nEnter number (1-9)\n");
                        int num = textIO.newIntInputReader()
                                .withMinVal(1)
                                .withMaxVal(9)
                                .read("Number:");
                        Integer point = peer.placeNumber(gameName,row,col,num);
                        if (point != null) {
                            terminal.printf("\nScore of this move: %d\n", point);
                            terminal.printf("Total score: %d\n\n", peer.getTotalScore());
                        } else {
                            terminal.printf("Error in placing the number\n\n");
                        }
                        break;
                    case 3:
                        terminal.printf("Are you sure to leave the network?\n");
                        boolean exit = textIO.newBooleanInputReader().withDefaultValue(false).read("exit?");
                        if(exit) {
                            peer.leaveNetwork();
                            System.exit(0);
                        }
                        break;
                    default:
                        break;
                }
            }

        } catch (CmdLineException clEx) {
            System.err.println("ERROR: Unable to parse command-line options: " + clEx);
        }

    }

    public static void printBeforeGameMenu(TextTerminal terminal) {
        terminal.printf("1 - CREATE GAME\n");
        terminal.printf("2 - JOIN GAME\n");
        terminal.printf("3 - EXIT\n");
    }

    public static void printInGameMenu(TextTerminal terminal) {
        terminal.printf("1 - VIEW SUDOKU\n");
        terminal.printf("2 - PLACE NUMBER\n");
        terminal.printf("3 - EXIT\n");
    }
}