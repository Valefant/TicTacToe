/**
 * The toc protocol is used for setting and changing the active player of the session.<br>
 * It also defines the basic commands and field symbols.
 */
class TocProtocol {

    static final int MAX_PLAYERS = 2;

    // field symbols
    static final char P1 = 'X';
    static final char P2 = '0';
    static final char EMPTY = ' ';

    // commands
    static final String CONTINUE = "c";
    static final String RESTART = "r";
    static final String QUIT = "q";

    // the active player of a session
    private int activePlayer = 1;

    /**
     * Constructs a new {@link TocProtocol}.
     */
    TocProtocol() {
    }

    /**
     * Sets the next active player.
     */
    void nextActivePlayer() {

        activePlayer = activePlayer % MAX_PLAYERS + 1;
    }

    /**
     * Gets the active player.
     *
     * @return The active player
     */
    int getActivePlayer() {

        return activePlayer;
    }

    /**
     * Resets the protocol by setting the active player to one again
     */
    void reset() {

        activePlayer = 1;
    }

    /**
     * Retrieves the inactive player of the current session.
     *
     * @return The inactive player
     */
    int getInactivePlayer() {

        return activePlayer % MAX_PLAYERS + 1;
    }

    /**
     * Gets the related symbol for the {@code player}.
     *
     * @param player The player to get the symbol from
     *
     * @return The matching symbol for the {@code player}
     */
    static char getSymbol(final int player) {

        return player == 1 ? P1 : P2;
    }
}
