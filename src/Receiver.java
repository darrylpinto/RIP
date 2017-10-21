import java.io.IOException;
import java.net.*;
import java.util.LinkedList;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Darryl Pinto on 10/12/2017.
 * <p>
 * The class Receiver is the receiver thread of a Router r
 */
public class Receiver implements Runnable {

    private final Object startConnection;
    public Sender sender;
    Table routingTable;
    Router router;
    private int myReceiverPort;
    private String name;

    /**
     * Cionstructor of Receiver thread of r
     *
     * @param r    Router
     * @param send Sender thread of r
     */
    Receiver(Router r, Sender send) {
        this.name = r.name;
        this.myReceiverPort = r.receiverPort;
        this.startConnection = r.startConnection;
        this.routingTable = r.routingTable;
        this.router = r;
        this.sender = send;

    }

    /**
     * Method to receive the packets and update the routing table
     */
    private void receive() {
        System.out.println("RECEIVER:" + name);
        boolean connectionFlag = true;

        LinkedList<String> messageList = new LinkedList<>();
        DatagramSocket receiver = null;

        try {
            receiver = new DatagramSocket(myReceiverPort);

        } catch (SocketException e) {
            e.printStackTrace();
        }
        int itCount = 0;
        System.out.println("Initially:");
        this.routingTable.printTable();

        do {
            try {

                byte[] receiveData = new byte[2048];

                DatagramPacket receivePacket = new DatagramPacket(receiveData,
                        receiveData.length);

                receiver.receive(receivePacket);
                this.router.lastMessageSentTime = System.currentTimeMillis();

                if (connectionFlag) {
                    synchronized (this.startConnection) {
                        this.router.startMessageReceived = true;
                        this.startConnection.notifyAll();
                    }
                }
                connectionFlag = false;

                String message = new String(receivePacket.getData());
                //synchronized (this.routingTable) {

                messageList.add(message);
                LinkedList<String> ll = new LinkedList<>();
                ll.add(messageList.removeFirst());
                updateRoutingTable(ll);
                System.out.printf("Iteration %d:\n", ++itCount);
                this.routingTable.printTable();

                //  this.routingTable.notify();
                // }

            } catch (IOException e) {
                e.printStackTrace();
            }
        } while (true);

//        receiver.close();

    }

    /**
     * method to check for Timeout
     *
     * @param ip IP address of a neighboring router
     */
    private void checkForTimeOut(InetAddress ip) {


        for (Router r : this.router.getNeighbors().keySet()) {

            if (r.IP.toString().equals(ip.toString())) {
                r.lastMessageSentTime = System.currentTimeMillis();
            }

            if (!r.failedRouter && r.startMessageReceived
                    && System.currentTimeMillis()
                    - r.lastMessageSentTime > 4000) {

                System.out.println("Timeout occurred from " + r.name);

                this.router.neighbors.put(r, Double.POSITIVE_INFINITY);

                Vector routingVector =
                        this.routingTable.currentEntries.get(r.IP);

                routingVector.set(1, Double.POSITIVE_INFINITY);
                this.routingTable.currentEntries.put(r.IP, routingVector);

                for (InetAddress destIP : this.routingTable) {

                    Vector vTemp = this.routingTable.currentEntries.get(destIP);

                    if (vTemp.get(0).toString().equals(r.IP.toString())) {
                        vTemp.set(1, Double.POSITIVE_INFINITY);
                    }
                }

                this.sender.sendTriggerUpdate();
                System.out.println("Trigger Updates sent");
                r.failedRouter = true;

            }

            if (!r.startMessageReceived) {
                r.startMessageReceived = true;
            }

        }

    }

    /**
     * Method to update the routing table
     *
     * @param l Linked list having incoming information
     */
    private void updateRoutingTable(LinkedList<String> l) {

        ConcurrentHashMap<InetAddress, Vector> incomingTable =
                new ConcurrentHashMap<>();

        for (String str : l) {

            String[] paragraph = str.trim().split("\n");
            InetAddress sentFrom = null;

            try {
                sentFrom = InetAddress.getByName(paragraph[0].
                        substring(1, paragraph[0].length()));

                for (Router r : this.router.getNeighbors().keySet()) {

                    if (!r.failedRouter && r.IP.toString()
                            .equals(sentFrom.toString())) {

                        checkForTimeOut(sentFrom);
                    }

                }

            } catch (UnknownHostException e) {
                e.printStackTrace();
            }

            for (int i = 1; i < paragraph.length; i++) {

                try {

                    String[] word = paragraph[i].trim().split(" ");

                    InetAddress destIP = InetAddress.getByName(word[0].
                            substring(1, word[0].length()));

                    // Subnet is at word[1]

                    InetAddress nextHop = InetAddress.getByName(word[2].
                            substring(1, word[2].length()));

                    double distance = Double.parseDouble(word[3]);

                    if (nextHop.toString().equals(this.router.getIPString())) {
                        distance = Double.POSITIVE_INFINITY;
                    }

                    Vector v = new Vector();
                    v.add(sentFrom);  // sentFrom becomes nextHop
                    v.add(distance);  // incoming cost
                    if (!incomingTable.containsKey(destIP)) {
                        incomingTable.put(destIP, v);
                    } else {
                        Vector vNew = incomingTable.get(destIP);
                        vNew.addAll(v);
                        incomingTable.put(destIP, vNew);

                    }

                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            }

        }
        for (InetAddress ip : incomingTable.keySet()) {

//            Got SELF information, Not adding
            if (!ip.toString().equals(this.router.getIPString())) {

                Vector incomingVector = incomingTable.get(ip);

                for (int i = 0; i < incomingVector.size(); i += 2) {

                    InetAddress sentFrom = (InetAddress) incomingVector.get(i);

                    ConcurrentHashMap<InetAddress, Router> IPRouterMap =
                            this.router.getIPRouterMap();

                    Router neighbor = IPRouterMap.get(incomingVector.get(i));

                    double neighborCost =
                            this.router.getNeighbors().get(neighbor);

                    double incomingDistance =
                            (Double) incomingVector.get(i + 1);

                    double newCost = incomingDistance + neighborCost;
                    Vector vNew = new Vector();

                    if (!this.routingTable.containsIP(ip)) {

                        vNew.add(sentFrom);
                        vNew.add(newCost);
                        this.routingTable.addEntryToTable(ip, vNew);

                        System.out.println(name + " has a new entry:" + ip +
                                " vector added: " + vNew);

                    } else {

                        Vector routingVector =
                                this.routingTable.currentEntries.get(ip);

                        double oldCost = (Double) routingVector.get(1);
                        // RT has 2 entries

                        if (newCost < oldCost) {
                            vNew.add(neighbor.IP);
                            vNew.add(newCost);
                            this.routingTable.currentEntries.put(ip, vNew);
                        }

                        if (routingVector.get(0).toString()
                                .equals(sentFrom.toString())) {

                            if (oldCost != newCost) {
                                vNew.add(neighbor.IP);
                                vNew.add(newCost);

                                this.routingTable.currentEntries
                                        .put(ip, vNew);
                            }

                        }

                        if (this.router.isNeighbor(ip)) {

                            Router miniNeighbor = IPRouterMap.get(ip);

                            double originalCost = this.
                                    router.getNeighbors().get(miniNeighbor);

                            if (originalCost < newCost) {
                                vNew.add(ip);
                                vNew.add(originalCost);

                                this.routingTable.currentEntries.put(ip, vNew);

                            }
                            else{
  //                              if (newCost < oldCost) {
                                    vNew.add(ip);
                                    vNew.add(newCost);
                                    this.routingTable.currentEntries.put(ip, vNew);
//                                }

                            }

                        }

                    }

                }

            }
        }
    }

    /**
     * The run method of Receiver class
     */
    @Override
    public void run() {
        this.receive();
    }


}

