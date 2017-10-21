import java.net.InetAddress;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Darryl Pinto on 10/10/2017.
 *
 * Router Class
 */
public class Router {


    InetAddress IP;
    int senderPort;    int receiverPort;
    String name;
    ConcurrentHashMap<Router, Double> neighbors;
    boolean failedRouter;
    Object startConnection;
    Table routingTable = null;
    long lastMessageSentTime;
    boolean startMessageReceived;

    /**
     * Constructor for Router class
     * @param name Router name
     * @param IP Ip address of Router
     * @param senderPort Sending port of the Router
     * @param receiverPort Receiving port of the Router
     */
    Router(String name, InetAddress IP, int senderPort, int receiverPort) {
        this.name = name;
        this.IP = IP;
        this.neighbors = new ConcurrentHashMap<>();
        this.failedRouter = false;
        this.senderPort = senderPort;
        this.receiverPort = receiverPort;
        this.startConnection = new Object();
        this.lastMessageSentTime = 0;
        this.startMessageReceived = false;

    }

    /**
     * method to get IP string of the router
     * @return IP string of the router
     */
    public  String getIPString(){
        return this.IP.toString();

    }

    /**
     * String Representation of the Router
     * @return String Representation of the Router
     */
    public String toString() {
        return String.format("%s(%s)Sending:%d Receiving:%d",
                this.IP.toString(), this.name,
                this.senderPort, this.receiverPort);

    }

    /**
     * Method to check if  a Router is the neighbor
     * @param IP IP address of the querying router
     * @return Boolean indicating neighbor or not
     */
    public boolean isNeighbor(InetAddress IP) {

        for (Router r: neighbors.keySet()){
            if(r.getIPString().equals(IP.toString()))
                return true;

        }
        return false;
    }

    /**
     * Method to add a router as the neighbor
     * @param r Router to be added
     * @param cost Link Cost
     */
    public  void addNeighbor(Router r, double cost){
        this.neighbors.put(r, cost);
    }

    /**
     * Method to get neighbors
     * @return Hashmap with neighbors and their corresponding cost
     */
    public ConcurrentHashMap<Router, Double> getNeighbors(){
        return this.neighbors;
    }

    /**
     * Method to get Map of IP address and Routers of the neighbors
     * @return Map of IP address and Routers of the neighbors
     */
    public ConcurrentHashMap<InetAddress, Router> getIPRouterMap(){

        ConcurrentHashMap<InetAddress, Router> IPRouterMap =
                new ConcurrentHashMap<>();

        for(Router r: this.getNeighbors().keySet()){
            IPRouterMap.put(r.IP, r);
        }

        return IPRouterMap;
    }

    /**
     * Method to initialize the routing table
     */
    public void initializeRoutingTable(){
        if(this.routingTable == null){
            this.routingTable = new Table(this);

        }

    }


}
