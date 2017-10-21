import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Created by Darryl Pinto on 10/12/2017.
 * The class Sender is the sender thread of a Router r
 */
public class Sender implements Runnable {

    private final Router router;
    private final Object startConnection;
    Table routingTable;
    private InetAddress IP;
    private int mySenderPort;
    private String name;


    /**
     * constructor for Sender class
     * @param r Router
     */
    Sender(Router r) {

        this.router = r;
        this.mySenderPort = r.senderPort;
        this.name = r.name;
        this.startConnection = r.startConnection;
        this.routingTable = r.routingTable;
        this.IP = r.IP;

    }

    /**
     * Method to send the Trigger Updates
     */
    public void sendTriggerUpdate() {
        try {

            synchronized (this.routingTable) {
                DatagramSocket socket = new DatagramSocket(mySenderPort + 1);
                String message = IP.toString() + "\n" + this.routingTable.sendTableValues();

                byte[] sendData = message.getBytes();
                for (Router r : this.router.getNeighbors().keySet()) {
                    int theirReceiverPort = r.receiverPort;
                    String ipString = r.getIPString();
                    ipString = ipString.substring(1, ipString.length());

                    DatagramPacket packet = new DatagramPacket(sendData,
                            sendData.length,
                            InetAddress.getByName(ipString),
                            theirReceiverPort);

                    socket.send(packet);

                }
                socket.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to Send the Routing Table information to the neighbors
     */
    public void send() {
        synchronized (this.startConnection) {

            try {
                this.startConnection.wait(12000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(mySenderPort);
        } catch (SocketException e) {
            e.printStackTrace();
        }


//        socket.close();
        while (true) {
            try {

               // synchronized (this.routingTable) {

                String message = IP.toString() + "\n" + this.routingTable.sendTableValues();

                byte[] sendData = message.getBytes();
                for (Router r : this.router.getNeighbors().keySet()) {
                    int theirReceiverPort = r.receiverPort;
                    String ipString = r.getIPString();
                    ipString = ipString.substring(1, ipString.length());

                    DatagramPacket packet = new DatagramPacket(sendData,
                            sendData.length,
                            InetAddress.getByName(ipString),
                            theirReceiverPort);

                    socket.send(packet);

                }

                //     System.out.println(name +" is waiting");
                //     this.routingTable.wait();
                Thread.sleep(1000);
              //      }

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * The run method of Receiver class
     */
    @Override
    public void run() {


        this.send();

    }
}
