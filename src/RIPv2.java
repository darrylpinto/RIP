import java.io.IOException;
import java.net.InetAddress;
import java.util.Scanner;


/**
 * Created by Darryl Pinto on 10/10/2017.
 *
 * The main class for RIP protocol which uses distance-vector routing
 */
public class RIPv2 {


    public static void main(String[] args) throws IOException,
            InterruptedException {

//        String filepath = "src\\_r4.txt";
//        File f = new File(filepath);
//        BufferedReader bf = new BufferedReader(new FileReader(f));
//        String read;
//        int lineCount = 0;
//        while ((read = bf.readLine()) != null) {
//            String[] line = read.trim().split(" ");
//            if(lineCount == 1){
//                // While converting to System input
//            }
//            else if (lineCount == 0) {
//                r1 = new Router(line[0], InetAddress.getByName(line[1]),
//                        Integer.parseInt(line[2]), Integer.parseInt(line[3]));
//            } else {
//                Router r2 = new Router(line[0], InetAddress.getByName(line[1]),
//                        -1, Integer.parseInt(line[2]));
//
//                int cost = Integer.parseInt(line[3]);
//                r1.addNeighbor(r2, cost);
//
//            }
//            lineCount++;
//        }

        // For example queeg 127.0.0.1 21000 11000
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter name of the router:");
        String name = sc.next();
        System.out.println("Enter IP of the router:");
        String ipString = sc.next();
        System.out.println("Enter sending Port of the router:");
        int sendingPort = sc.nextInt();
        System.out.println("Enter receiver Port of the router:");
        int receivingPort = sc.nextInt();

        Router r1 = new Router(name, InetAddress.getByName(ipString),
                sendingPort, receivingPort);

        System.out.println("Enter number of neighbors");
        int neighbors = sc.nextInt();


        for (int i = 0; i < neighbors; i++) {

            // For example comet 127.0.0.2 12000 10
            System.out.println("Enter name of the Neighbor:");
            String nameN = sc.next();
            System.out.println("Enter IP of the Neighbor:");
            String ipStringN = sc.next();
            System.out.println("Enter receiver Port of the Neighbor:");
            int receivingPortN = sc.nextInt();
            System.out.println("Enter cost:");
            int cost = sc.nextInt();
            Router r2 = new Router(nameN,InetAddress.getByName(ipStringN),
                    -1, receivingPortN);
            r1.addNeighbor(r2, cost);
        }

        communicate(r1);

    }

    /**
     * Method to initialize the routing table of r
     * and to start the receiver and sender threads.
     * @param r Router
     */
    private static void communicate(Router r) {

        r.initializeRoutingTable();
        Sender sen = new Sender(r);

        Receiver rec = new Receiver(r, sen);

        new Thread(rec).start();
        new Thread(sen).start();


    }


}

