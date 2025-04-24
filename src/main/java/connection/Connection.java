package connection;
import java.io.*;
import java.net.*;

public class Connection {
    private BufferedReader in;
    private BufferedWriter out;
    private Socket socket;

    public Connection(String ipAddress, int port) {
        try {
            socket = new Socket(ipAddress, port);
            in     = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out    = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            System.out.println("Successfully inited connection to remote!");
        }
        catch (UnknownHostException e) {
            System.out.println("Host with ip:" + ipAddress + " and port: " + port + " is unknown!");
            e.printStackTrace();
        }
        catch (IOException e) {
            System.out.println("Unable to init stable connection!");
            e.printStackTrace();
        }
    }

    public Connection(Socket socket) {
        if (socket.isClosed() || !socket.isConnected()) {
            System.out.println("Attempt to init connection via invlid socket!");
            throw new ExceptionInInitializerError(new String("Bad socket on connection init!"));
        }
        try {
            this.socket = socket;
            in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            System.out.println("Successfully inited connection to remote!");
        }
        catch (IOException e) {
            System.out.println("Unable to init stable connection!");
            e.printStackTrace();
        }
    }

    public void sendString(String string) throws IOException {
        out.write(string + "\n");
        out.flush();
    }

    public String recieveString() {
        String incomming = new String();
        try {
            incomming = in.readLine();
            if (incomming == null) {
                System.err.println("End of file! Closing connection...");
                tearConnection();
                return null;
            }
        }
        catch (SocketException e) {
            System.out.println("Client fails with socket exception!");
            e.printStackTrace();
            return null;
        }
        catch (IOException e) {
            System.out.println("Unable to recieve string from remote!");
            e.printStackTrace();
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
