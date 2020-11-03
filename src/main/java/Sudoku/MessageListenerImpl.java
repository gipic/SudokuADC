package Sudoku;

import org.beryx.textio.TextIO;
import org.beryx.textio.TextIoFactory;
import org.beryx.textio.TextTerminal;

public class MessageListenerImpl implements MessageListener {
    int peerid;

    public MessageListenerImpl(int peerid) {
        this.peerid = peerid;

    }

    public Object parseMessage(Object obj) {
        TextIO textIO = TextIoFactory.getTextIO();
        TextTerminal terminal = textIO.getTextTerminal();
        String message = (String) obj;
        if (message.equals(Utils.GAME_FINISHED)) {
            System.exit(0);     // Il programma termina
        } else {
            terminal.printf("\n" + peerid + "] (Direct Message Received) " + message + "\n");   // Semplice messaggio
        }
        return "success";
    }
}