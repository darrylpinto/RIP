import java.net.InetAddress;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Darryl Pinto on 10/13/2017.
 *
 * Table class is used to represent the routing table of Router r
 */
public class Table implements Iterable<InetAddress> {

    public ConcurrentHashMap<InetAddress, Vector> currentEntries;

    /**
     * Constructor of Table class for Router
     * @param router Router
     */
    Table(Router router) {

        ConcurrentHashMap<Router, Double> costToNeighbors =
                router.getNeighbors();

        this.currentEntries = new ConcurrentHashMap<>();

        for (Router r : router.getNeighbors().keySet()) {
            Vector v = new Vector();

            v.add(r.IP);  //NextHop
            v.add(costToNeighbors.get(r));  //Cost

            this.currentEntries.put(r.IP, v);
        }
    }

    /**
     * String representation of Table class
     * @return string
     */
    public String toString() {
        String str = "";
        for (InetAddress ip : this) {
            str += "" + ip + "-->" + this.currentEntries.get(ip) + "\n";
        }
        return str;
    }

    /**
     * Prints the contents of Table class
     */
    public void printTable() {

        String str = "Dest IP addr\t\t\tSubnet Mask\t\t\t" +
                "Next Hop\t\tDistance\n";

        str += "------------------------------------------" +
                "----------------------------------\n";

        for (InetAddress ip : this) {

            String destIP = ip.toString();
            String subnet = "255.255.255.0";
            InetAddress nextHop =
                    (InetAddress) this.currentEntries.get(ip).elementAt(0);
            double distance =
                    (Double) this.currentEntries.get(ip).elementAt(1);

            str += String.format("%s\t\t\t%s\t\t\t%s\t\t%.0f\n",
                    destIP, subnet, nextHop, distance);

        }
        System.out.println(str);

    }

    /**
     * Sends the routing table information to the neighbors
     *
     * @return routing table information string
     */
    public String sendTableValues() {
        String str = "";
        for (InetAddress ip : this) {

            String destIP = ip.toString();
            String subnet = "255.255.255.0";
            InetAddress nextHop =
                    (InetAddress) this.currentEntries.get(ip).elementAt(0);
            double distance =
                    (Double) this.currentEntries.get(ip).elementAt(1);

            str += String.format("%s %s %s %.0f\n",
                    destIP, subnet, nextHop, distance);

        }
        return str;

    }

    /**
     * Method to check if Routing table contains the IP address
     * @param ip IP Address
     * @return boolean indicating if Routing table contains the IP address
     */
    public boolean containsIP(InetAddress ip) {

        for (InetAddress ip1 : this) {

            if (ip1.toString().equals(ip.toString()))
                return true;

        }
        return false;
    }

    /**
     * Iterator for Table class
     * @return Iterator for Table class
     */
    @Override
    public Iterator<InetAddress> iterator() {
        return this.currentEntries.keySet().iterator();
    }

    /**
     * Method to enter a new IP to the Routing table
     * @param ip IP address
     * @param v Routing Table information vector
     */
    public void addEntryToTable(InetAddress ip, Vector v) {
        if (!this.currentEntries.containsKey(ip))
            this.currentEntries.put(ip, v);
    }
}
