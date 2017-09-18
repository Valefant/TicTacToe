public class App {

    public static void main(String[] args) {

        if (args.length < 1) {

            System.err.printf("Usage: program_name port\n");
            return;
        }

        final int port = Integer.parseInt(args[0]);
        final GameServer server = new GameServer(port);

        server.run();
    }
}
