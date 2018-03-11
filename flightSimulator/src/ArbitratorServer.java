import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * Created by Παναγιώτης on 7/12/2017.
 */
public class ArbitratorServer extends Thread {

    private ServerSocket server = null;

    public void run() {
        int port = 49152;
        try {
            server = new ServerSocket(port);
            //System.out.println("Server started");
        } catch (IOException i) {
            System.out.println(i);
        }
        while (true) {
            // starts server and waits for a connection
            try {
                Socket socket = new Socket();
                try {
                    socket = server.accept(); // wait for a flight request
                } catch (SocketException i) { //here, arbitratorServer is interrupted because STOP was pressed
                    System.out.println("arbitratorServer exited successfully");
                    return;
                }
                // I had a request, also I opened a socket..
                Arbitrator myArbit = new Arbitrator(socket); // from now on, an arbitrator takes this socket and take care of what this flight should do
                myArbit.start();
            } catch (IOException i) {
                System.out.println("hey 3");
                System.out.println(i);
            }
        }
    }

    public ServerSocket getServerSocket() {
        return server;
    }
}
