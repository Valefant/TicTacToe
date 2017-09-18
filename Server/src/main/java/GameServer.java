import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a game server for the TicTacToe game.
 */
class GameServer {

    /**
     * Logger which outputs all relevant information to the console.<br>
     * The output is also saved into a log file named {@code game-server.log}.
     */
    private static Logger LOGGER = LoggerFactory.getLogger(GameServer.class);

    /**
     * The port to listen to.
     */
    private final int port;

    /**
     * Holds references to the connected client sockets.
     */
    private final List<Socket> clientSockets = new ArrayList<>();

    /**
     * Holds references to the input stream of the client sockets.
     */
    private final List<BufferedReader> readers = new ArrayList<>();

    /**
     * Holds references to the output stream of the client sockets.
     */
    private final List<PrintWriter> writers = new ArrayList<>();

    /**
     * The toc protocol is used for setting and changing the active player of the session.<br>
     * It also defines the basic commands and field symbols.
     */
    private final TocProtocol tocProtocol = new TocProtocol();

    /**
     * Constructs a new {@link GameServer}.
     *
     * @param port The port to listen to
     */
    GameServer(final int port) {

        if (port < 1024) {

            throw new IllegalArgumentException("The given port number should not interfere with the well known ports!");
        }

        this.port = port;
    }

    /**
     * Runs the server.
     */
    void run() {

        try (final ServerSocket serverSocket = new ServerSocket(port)) {

            LOGGER.info("Starting server on port {}", port);
            accept(serverSocket);

            boolean restart = false;

            do {
                tocProtocol.reset();
                sendPlayerStartingOrder();

                while (true) {

                    announceActivePlayer();
                    notifyInactivePlayerAboutUpdate();

                    final String command = readers.get(tocProtocol.getActivePlayer() - 1).readLine();

                    if (command.equals(TocProtocol.QUIT)) {

                        restart = false;
                        break;
                    } else if (command.equals(TocProtocol.RESTART)) {

                        final String s = readers.get(tocProtocol.getInactivePlayer() - 1).readLine();

                        if (s.equals(TocProtocol.RESTART)) {
                            restart = true;
                            LOGGER.trace("Both players agreed to restart!");
                        }
                        break;
                    }

                    tocProtocol.nextActivePlayer();
                }
            } while (restart);

            for (final Socket clientSocket : clientSockets) {
                clientSocket.close();
            }
        } catch (final IOException e) {

            LOGGER.error("", e);
        }
    }

    /**
     * Accepts two clients sockets.
     *
     * @param serverSocket Used for obtaining the client sockets.
     *
     * @throws IOException
     */
    private void accept(final ServerSocket serverSocket) throws IOException {

        for (int i = 0; i < TocProtocol.MAX_PLAYERS; i++) {

            final Socket socket = serverSocket.accept();

            clientSockets.add(socket);
            readers.add(new BufferedReader(new InputStreamReader(socket.getInputStream())));
            writers.add(new PrintWriter(socket.getOutputStream(), true));

            LOGGER.info("Client connected {}", socket);
        }

        LOGGER.info("All clients connected. The game can be started!");
    }

    /**
     * Sends the starting order.
     */
    private void sendPlayerStartingOrder() {

        writers.get(0).println(1);
        writers.get(1).println(2);
    }

    /**
     * Announces the active player of each turn.
     */
    private void announceActivePlayer() {

        final int activePlayer = tocProtocol.getActivePlayer();

        writers.forEach(writer -> writer.println(activePlayer));

        LOGGER.trace("Player {} is active", activePlayer);
    }

    /**
     * The inactive player will be informed about the update of the game board.
     *
     * @throws IOException
     */
    private void notifyInactivePlayerAboutUpdate() throws IOException {

        final int activePlayer = tocProtocol.getActivePlayer();

        LOGGER.info("Waiting for player {} input", activePlayer);

        final String index = readers.get(activePlayer - 1).readLine();

        LOGGER.info("Received {} index from player {}", index, activePlayer);

        writers.get(tocProtocol.getInactivePlayer() - 1).println(index);
    }
}
