import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {

    public static void main(String[] args) {
        int port = 3000;
        Gson gson = new Gson();

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Tetris Server is listening on port " + port);

            while (true) {
                try (Socket socket = serverSocket.accept()) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

                    // Read the JSON message from the client
                    String jsonMessage = in.readLine();

                    // Convert JSON to a Java object
                    PureGame pg = gson.fromJson(jsonMessage, PureGame.class);
                    if(pg !=null) {
                        Simulation sim = new Simulation(pg.getCells(), pg.getCurrentShape(),pg.getNextShape());
                        int[] opMoves = sim.getOptimizedMove();
                        OpMove opMove = new OpMove(opMoves[0],opMoves[1]);

                        String jsonResponse = gson.toJson(opMove);

                        // Send the JSON response back to the client
                        out.println(jsonResponse);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error in server. "+e.getMessage());
        }
    }

    // Define a simple class to represent the JSON message
    static class Message {
        private final String message;
        private final String sender;

        public Message(String message, String sender) {
            this.message = message;
            this.sender = sender;
        }

        public String getMessage() {
            return message;
        }

        public String getSender() {
            return sender;
        }
    }
}
