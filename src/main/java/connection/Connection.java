package connection;
import java.io.*;
import java.net.*;

public class Connection {
    private BufferedReader in;
    private BufferedWriter out;
    private Socket socket;

    public Connection(String ipAddress, int port) throws IOException {
        socket = new Socket(ipAddress, port);
        in     = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out    = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        System.out.println("Successfully inited connection to remote!");
    }

    public Connection(Socket socket) throws IOException {
        if (socket.isClosed() || !socket.isConnected()) {
            System.out.println("Attempt to init connection via invlid socket!");
            throw new ExceptionInInitializerError(new String("Bad socket on connection init!"));
        }

        this.socket = socket;
        in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        System.out.println("Successfully inited connection to remote!");
    }

    public void sendString(String string) throws IOException {
        out.write(string + "\n");
        out.flush();
    }

    public String recieveString() throws IOException {
        String incomming = new String();
        incomming = in.readLine();
        if (incomming == null) {
            System.err.println("End of file! Closing connection...");
            tearConnection();
            return null;
        }
        return incomming;
    }

    public void tearConnection() {
        try {
            if (socket.isConnected() && !socket.isClosed()) {
                socket.close();
                in.close();
                out.close();
            }
            System.out.println("Connection closed!");
        }
        catch (IOException e) {
            System.out.println("Unable to close connection!");
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        return !socket.isClosed() && socket.isConnected();
    }
}
