import java.io.IOException;

public class App {

    public static void main(String[] args) throws IOException {

        if (args.length < 2) {

            System.err.printf("Usage: program_name hostname port\n");
            return;
        }

        final String hostname = args[0];
        final int port = Integer.parseInt(args[1]);

        final GameClient client = new GameClient(hostname, port);
        client.run();
    }
}