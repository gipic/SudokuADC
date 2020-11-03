import Sudoku.MessageListenerImpl;
import Sudoku.SudokuGameImpl;
import SudokuGenerator.Grid;


import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;


public class TestSudokuGameImpl {

    static private SudokuGameImpl peer1;
    static private SudokuGameImpl peer2;
    static private SudokuGameImpl peer3;
    static private SudokuGameImpl peer4;

    @BeforeAll
    static void setUp() throws Exception {

        peer1 = new SudokuGameImpl(0, "127.0.0.1", new MessageListenerImpl(0));
        peer2 = new SudokuGameImpl(1, "127.0.0.1", new MessageListenerImpl(1));
        peer3 = new SudokuGameImpl(2, "127.0.0.1", new MessageListenerImpl(2));
        peer4 = new SudokuGameImpl(3, "127.0.0.1", new MessageListenerImpl(3));

        /*
        peer1 = new SudokuGameImpl(0, "127.0.0.1", null);
        peer2 = new SudokuGameImpl(1, "127.0.0.1", null);
        peer3 = new SudokuGameImpl(2, "127.0.0.1", null);
        peer4 = new SudokuGameImpl(3, "127.0.0.1", null);
         */

        assertNotEquals(null,peer1.generateNewSudoku("game"));
        assertEquals(true,peer1.join("game","player1"));
        peer1.setNickname("player1");
        assertEquals(true,peer2.join("game","player2"));
        peer2.setNickname("player2");
        assertEquals(true,peer3.join("game","player3"));
        peer3.setNickname("player3");
        assertEquals(true,peer4.join("game","player4"));
        peer4.setNickname("player4");
    }

    @AfterAll
    static void afterAll() {
        peer1.leaveNetwork();
        peer2.leaveNetwork();
        peer3.leaveNetwork();
        peer4.leaveNetwork();
    }

    @Test
    void testUserAlreadyExists() throws Exception {
        SudokuGameImpl newPeer = new SudokuGameImpl(4, "127.0.0.1", new MessageListenerImpl(4));
        assertEquals(false,newPeer.join("game","player2")); // Nickname gia utilizzato
    }

    @Test
    void testGameAlreadyExists() throws Exception {
        SudokuGameImpl newPeer = new SudokuGameImpl(5, "127.0.0.1", new MessageListenerImpl(5));
        assertEquals(null,newPeer.generateNewSudoku("game"));   // Game gia presente
    }

    @Test
    void testGameNotExists() throws Exception {
        SudokuGameImpl newPeer = new SudokuGameImpl(6, "127.0.0.1", new MessageListenerImpl(6));
        assertEquals(false,newPeer.join("anotherGame","newPlayer")); // Game non presente
    }

    // Cerco di riempire una cella gia riempita dall'inizio o da me (-1 pt)
    @Test
    void testCellAlreadyFilled() throws Exception {
        Grid sudokuGrid = peer1.getSudoku("game");
        assertNotEquals(null,sudokuGrid);

        int ris = 0;
        boolean exit = false;
        for (int row = 0; row < (sudokuGrid.getSize()) && !exit; row++) {
            for (int column = 0; column < (sudokuGrid.getSize()) && !exit; column++) {
                if (!sudokuGrid.getCell(row,column).isEmpty()) {
                    ris = peer1.placeNumber("game",row,column,1);
                    exit = true;
                }
            }
        }
        assertEquals(-1,ris);
    }

    /* Player1 inserisce numero corretto non preso da nessuno (1 pt)
    mentre player2 inserisce numero corretto ma preso gia da player1 (0 pt)
     */
    @Test
    void testNumberCorrect() throws Exception {
        Grid sudokuGridPlayer1 = peer1.getSudoku("game");
        assertNotEquals(null,sudokuGridPlayer1);

        /*
        System.out.println("Sudoku player1 prima della mossa");
        System.out.println(sudokuGridPlayer1);
         */

        int rowSolution = -1;
        int columnSolution = -1;
        int numberSolution = -1;

        int ris = 0;
        boolean exit = false;

        for (int row = 0; row < (sudokuGridPlayer1.getSize()) && !exit; row++) {
            for (int column = 0; column < (sudokuGridPlayer1.getSize()) && !exit; column++) {
                if (sudokuGridPlayer1.getCell(row,column).isEmpty()) {
                    for (int number = 1; (number <= 9) && !exit; number++) {
                        ris = peer1.placeNumber("game",row,column,number);
                        if (ris == 1) {
                            //System.out.println(row + " -- " + column + " -- " + number);
                            rowSolution = row;
                            columnSolution = column;
                            numberSolution = number;
                            exit = true;
                        }
                    }
                }
            }
        }
        assertEquals(1,ris);

        /*
        System.out.println("Sudoku player1 dopo la sua mossa");
        System.out.println(peer1.getSudoku("game"));
         */

        Grid sudokuGridPlayer2 = peer2.getSudoku("game");
        assertNotEquals(null,sudokuGridPlayer2);

        /*
        System.out.println("Sudoku player2 prima della mossa");
        System.out.println(sudokuGridPlayer2);

         */

        ris = peer2.placeNumber("game",rowSolution,columnSolution,numberSolution);
        assertEquals(0,ris);

        /*
        System.out.println("Sudoku player2 dopo la sua mossa");
        System.out.println(peer2.getSudoku("game"));
         */
    }


}
