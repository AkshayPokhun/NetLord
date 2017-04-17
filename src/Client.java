import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Calendar;
import java.util.Scanner;
/**
 * Created by aksha on 16-Apr-17.
 */
public class Client implements Runnable{


    private int port, timeOut = 3000, packetLength, echoRequests;
    private byte[] sendData, receiveData;
    private InetAddress ipAddress;
    private DatagramPacket sendPacket, receivePacket;
    private DatagramSocket clientSocket;
    private boolean clientIsRunning = true;
    private String command;
    Scanner sn = new Scanner(System.in);

    public Client() {

        try {
            System.out.format("-------- Client Side --------%n%n");

            help();

            clientSocket = new DatagramSocket();
            if (command.contains("w")) {
//            timeOut = Integer.parseInt(this.command.get("w").getText()); // time out
            }
            clientSocket.setSoTimeout(timeOut);

            run();

        } catch (Exception e) {}


    }

    public void help(){

        try {
        System.out.format("You may enter any of the ping options below: %n%n");
        System.out.println("-t Ping the specified host until stopped.");
        System.out.println("-a Resolve addresses to hostnames");
        System.out.println("-n Number of echo requests to be sent.");
        System.out.println("-l Send buffer size.");
        System.out.println("-w Timeout in milliseconds to wait for each reply.");
        System.out.println("-4 Force using IPv4");
        System.out.format("%nWaiting for command: ");
        command = sn.nextLine();
        System.out.println("");

        System.out.println("Enter Port Number: ");
        port = sn.nextInt();


        } catch (Exception e) {}


    }

    public void run(){
        if(command.contains("t")){
            while(clientIsRunning){
                pingClient();
            }
        }
    }


    public void pingClient(){
        try{
            String address = command.substring(command.indexOf("1"), command.indexOf("-") -1);
            ipAddress = InetAddress.getByName(address);


            if(command.contains("n")){
                //Getting the echo request entered by the client
//                echoRequests = Integer.parseInt(data.get("n").getText());
            }else{
                //if not entered, then default is 4
                echoRequests = 4;
            }
            if(command.contains("l")){
//                packetLength  = Integer.parseInt(data.get("l").getText());
            }else{
                packetLength  = 32;
            }
            System.out.println(packetLength);
            sendData = new byte[packetLength];

            // allocate a data buffer
            for(int i =0; i < sendData.length; i++){
                // loop and fill in with data at each index position of the byte array of sendData
                sendData[i] = '0';
            }

            receiveData = new byte[packetLength];
            // receive the datagrams and specify the complete address of the server to ping

            sendPacket = new DatagramPacket(sendData, sendData.length, ipAddress, port);
            System.out.println("Pinging " + ipAddress.getHostName()
                                + " with " + echoRequests + " packets"
                                + " each of " + packetLength + " bytes ");

            int sent = 0;
            int received = 0;
            long maxTime = 0;
            long minTime = 0;
            long totalTime = 0;
            boolean firstSend = true;


            while (sent < echoRequests) {
                long start = Calendar.getInstance().getTimeInMillis(); // gets the current in ms
                try {
                    clientSocket.send(sendPacket);
                    sent++;
                } catch (SocketTimeoutException ex) {
                    System.out.println("- Unable to send packet: connection timed out...");
                    continue;
                }catch (IOException ex) { }

                receivePacket = new DatagramPacket(receiveData, receiveData.length); // contains client's data and the length

                try {
                    clientSocket.receive(receivePacket);
                } catch (SocketTimeoutException ex) {
                    System.out.println("- Unable to receive packet: connection timed out...");
                    continue;
                } catch (IOException ex) {}

                long end = Calendar.getInstance().getTimeInMillis();
                long duration = end - start; // gets the duration by subtracting the ending time from the starting time

                totalTime += duration;

                if (firstSend) {
                    minTime = maxTime = duration;
                    firstSend = false;
                } else {
                    if (minTime > duration) {
                        minTime = duration;
                    }
                    if (maxTime < duration) {
                        maxTime = duration;
                    }
                }
                if (receivePacket.getData().length == sendData.length) { // fully received packets
                    received++;
                    System.out.println(
                            "+ Reply from " + ipAddress.getHostAddress() // get the IP add
                                    + " bytes=" + packetLength + " time"
                                    + ((duration == 0) ? "<1ms" : "=" + (duration) + "ms")
                    );
                } else if (receivePacket.getData().length != 0) { // received packets but some lost
                    System.out.println(
                            "+ Reply from " + ipAddress.getHostAddress()
                                    + " bytes=" + receivePacket.getData().length + " time"
                                    + ((duration == 0) ? "<1ms" : "=" + (duration) + "ms")
                    );
                }
                int lost = sent - received;
                // checks if any packets lost and performs the & % lost
                float percentLoss = (sent != 0) ? ((float) lost / sent) * 100 : 0;
                float averageTime = (float) totalTime / sent;
                System.out.println(
                        "+ Ping statistics for " + ipAddress.getHostAddress()
                                + ":Packets: Sent =" + sent + ", Received = " + received
                                + ", Lost = " + lost + " (" + percentLoss + "% loss)"
                );
                System.out.println(
                        "+ Approximate round trip times in milli-seconds:"
                                + "Minimum = " + minTime + "ms, Maximum = " + maxTime
                                + "ms, Average = " + averageTime + "ms");

                String break_line = sn.next();
                if(break_line.equals("break"))
                    System.out.println(break_line);
                    clientSocket.close();
            }
        }catch (Exception e){}
    }

}
