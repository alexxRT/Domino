package client;

public class ClientApp {
    public static void main(String[] args) {
        try {
            String[] posArgs = {"ClientApp", "ipAddr", "port"};
            if (args.length != posArgs.length) {
                System.out.println("Start application with positional arguments! Usage example:");
                System.out.println(posArgs[0] + " -i <" + posArgs[1] + "> <" + posArgs[2] + ">");
                return;
            }

            String ipAddr = args[1];
            int port = Integer.parseInt(args[2]);

            Client client = new Client();
            client.runNetworking(ipAddr, port);

            // simulating work of other modules, GUI for instance
            while (true) {}

            //client.stopNetworking();
        } catch(Exception e) {
            System.out.println("Exception occurred when app running!");
            e.printStackTrace();
        }
    }
}