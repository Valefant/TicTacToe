import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Represents a game client for the TicTacToe game.
 */
class GameClient {

    /**
     * Logger for this class used only for debugging and tracing purposes.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(GameClient.class);

    /**
     * This 2D array represents the winning indices for the game board.
     */
    private static final int[][] winIndicesEntries = new int[][]{
            // horizontal
            {0, 1, 2},
            {3, 4, 5},
            {6, 7, 8},

            // vertical
            {0, 3, 6},
            {1, 4, 7},
            {2, 5, 8},

            // diagonal
            {0, 4, 8},
            {2, 4, 6},
    };

    /**
     * A 1D representation of the board which is initially filled with {@link TocProtocol#EMPTY} fields.<br>
     * It will be filled with the player inputs 'X' and 'O' over time.
     */
    private final char[] board = new char[9];

    /**
     * The hostname of the computer running the server
     */
    private final String hostname;

    /**
     * The listening port of the server to connect to
     */
    private final int port;

    /**
     * Used for reading the server responses.
     */
    private BufferedReader in;

    /**
     * Used for sending a response to the server.
     */
    private PrintWriter out;

    /**
     * Used for getting the player input.
     */
    private final Scanner scanner = new Scanner(System.in);

    /**
     * Constructs a new {@link GameClient}.
     *
     * @param hostname The hostname of the computer running the server
     * @param port     The listening port of the server to connect to
     */
    GameClient(final String hostname, final int port) {

        this.hostname = hostname;
        this.port = port;
    }

    /**
     * After this method is invoked a connection the server is established.<br>
     * <br>
     * This method composites the rendering of the board, the input handling and the exchange of data with the server.<br>
     * <br>
     * After the game is finished, the game can be restarted if both player agree.
     */
    void run() {

        try (final Socket socket = new Socket(hostname, port)) {

            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            LOGGER.debug("Connected to server");

            boolean restart;

            do {
                // initialize board with empty fields
                Arrays.fill(board, TocProtocol.EMPTY);

                // sets the player for the active game session
                final int player = Util.convertToDigit(in.readLine());

                LOGGER.debug("Assigned player: {}", player);

                int winner = 0;

                while (true) {

                    renderBoard();

                    // get the active player
                    final int activePlayer = Util.convertToDigit(in.readLine());

                    // each turn the active player gets to set a symbol the board
                    // this has to be communicated with the game server
                    processTurn(player, activePlayer);

                    // a winner or a full board are conditions for exiting the loop
                    if ((winner = calculateWinner(activePlayer)) != 0 || boardIsFull()) {

                        break;
                    }

                    // the active player sends a continue command to the server
                    // thus the server knows to continue with the game
                    if (player == activePlayer) {

                        out.println(TocProtocol.CONTINUE);
                    }
                }

                renderBoard();

                final String winText = winner == 0 ? "Draw" : "Player " + winner + " won the game";
                System.out.println(winText);

                restart = handlePossibleRestart();
            } while (restart);
        } catch (IOException e) {

            LOGGER.error("", e);
        }
    }

    /**
     * Responsible for rendering the board.
     */
    private void renderBoard() throws IOException {

        System.out.printf("\n");

        for (int i = 0; i < board.length; i++) {

            if (i != 0 && i % 3 == 0) {

                System.out.printf("|\n");
                System.out.printf("-------\n");
            }

            System.out.printf("|%c", board[i]);
        }

        System.out.printf("|\n");
    }

    /**
     * Determines a winner of the current game session, if there is one.
     *
     * @param activePlayer The current active player
     *
     * @return {@code 0} if no winner is found; {@code 1} if player one won; {@code 2} if player two won
     */
    private int calculateWinner(final int activePlayer) {

        final char symbol = TocProtocol.getSymbol(activePlayer);

        for (final int[] entry : winIndicesEntries) {

            if (board[entry[0]] == symbol && board[entry[1]] == symbol && board[entry[2]] == symbol) {

                return activePlayer;
            }
        }

        return 0;
    }

    /**
     * If no winner can be determined then the conclusion is that the board is full.
     *
     * @return {@code true} if the board is full; {@code false} otherwise.
     */
    private boolean boardIsFull() {

        return new String(board).chars().allMatch(field -> field != ((int) TocProtocol.EMPTY));
    }

    /**
     * Handles input from the player and exchanges data with the server.
     *
     * @param player       The player of the current running session
     * @param activePlayer If the {@code player} equals the active player then he obtains the power to make an input :D
     *
     * @throws IOException
     */
    private void processTurn(final int player, final int activePlayer) throws IOException {

        final int index;
        final char symbol = TocProtocol.getSymbol(activePlayer);

        if (player == activePlayer) {

            index = input(activePlayer, symbol);
            out.println(index);
        } else {

            index = Character.digit(in.readLine().charAt(0), 10);
        }

        board[index] = symbol;
    }

    /**
     * Processes the input of the current active player.<br>
     * <br>
     * If it does not match the given constraints like the input is out of range<br>
     * or an field is already taken, then the active player should try again.
     *
     * @param activePlayer The current active player
     * @param symbol       The symbol of the active player
     *
     * @return The index to set a symbol for the {@link #board}
     */
    private int input(final int activePlayer, final char symbol) {

        int inputIndex;

        while (true) {

            System.out.printf("P%d(%c): ", activePlayer, symbol);

            inputIndex = Util.convertToDigit(scanner.next());

            if (inputIndex <= 0) {

                System.out.printf("Wrong input!\n");
                continue;
            }

            // adjust input to match the board indices
            inputIndex = inputIndex - 1;

            if (board[inputIndex] == TocProtocol.EMPTY) {

                break;
            }

            System.out.printf("Field is already taken!\n");
        }

        return inputIndex;
    }

    /**
     * Handles a possible restart of the game.
     *
     * @return {@code true} if the game should be restarted; {@code false} otherwise
     */
    private boolean handlePossibleRestart() {

        System.out.println("Restart [y/n]? ");

        final String input = scanner.next();

        if (input.equals("y")) {
            out.println(TocProtocol.RESTART);
            return true;
        } else {
            out.println(TocProtocol.QUIT);
            return false;
        }
    }
}
