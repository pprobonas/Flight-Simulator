import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.ServerException;

/**
 * Created by Παναγιώτης on 6/12/2017.
 */
public class ArbitratorClient extends Thread  {
    public int[] coordinates = new int[2];
    private int threadId, planeHeight;
    public ArbitratorClient(int planeHeight, int[] coordinates, int threadId){
        this.planeHeight = planeHeight;
        this.coordinates[0] = coordinates[0];
        this.coordinates[1] = coordinates[1];
        this.threadId = threadId;
    }

    public void run() {
        // initialize socket and input output streams
        Socket socket            = null;
        DataInputStream  input   = null;
        DataOutputStream out     = null;
        int port = 49152;
        String address = "localhost";



        // establish a connection
        try
        {
            socket = new Socket(address, port);
            //System.out.println("Connected");



            // sends output to the socket
            out    = new DataOutputStream(socket.getOutputStream());
            if (out != null) {
                out.writeUTF(String.valueOf(coordinates[0]) + " " + String.valueOf(coordinates[1]) + " " + String.valueOf(threadId) + " " + String.valueOf(planeHeight));
            }
        }
        catch(UnknownHostException u)
        {
            System.out.println(u);
            System.out.println("hey10");

        }
        catch(IOException i)
        {
            System.out.println(i);
            System.out.println("hey11");

        }



        // close the connection
        try
        {
            ///input.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        }
        catch(IOException i)
        {
            System.out.println(i);
            System.out.println("hey8");
        }







    }
}
