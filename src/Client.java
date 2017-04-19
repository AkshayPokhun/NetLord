import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
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
    private String command, address, ipHeader;
    Scanner sn = new Scanner(System.in);
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

    public Client() {

        try {
            System.out.format("-------- Client Side --------%n%n");

            help();

            clientSocket = new DatagramSocket();
            clientSocket.setSoTimeout(timeOut);
            getInputDetails();


        } catch (Exception e) {}


    }

    public void help(){

        System.out.format("You may enter any of the ping options below: %n%n");
        System.out.println("-t Ping the specified host until stopped.");
        System.out.println("-a Resolve addresses to hostnames");
        System.out.println("-n Number of echo requests to be sent.");
        System.out.println("-l Send buffer size.");
        System.out.println("-w Timeout in milliseconds to wait for each reply.");
        System.out.println("-4 Force using IPv4");

    }

    private void getInputDetails(){

        try {
            command = null;
            System.out.format("%nWaiting for command: ");
            command = br.readLine();
            System.out.println("");

            if(command.equals("help")) {
                help();
                getInputDetails();
            }
            else {
                System.out.print("Enter Port Number: ");
                port = sn.nextInt();
                System.out.println("");

                if (command.contains("n")) {
                    System.out.print("Number of echo requests: ");
                    echoRequests = sn.nextInt();
                    System.out.println("");
                } else
                    echoRequests = 4;

                if (command.contains("l")) {
                    System.out.print("Buffer size: ");
                    packetLength = sn.nextInt();
                    System.out.println("");
                } else {
                    packetLength = 32;
                }

                if (command.contains("w")) {
                    System.out.print("Time to wait for reply ");
                    timeOut = sn.nextInt(); // time out
                    System.out.println("");
                }

                run();
            }


        } catch (Exception e) {
            System.out.println("Please check your input");
            help();
            getInputDetails();
        }

    }

    public void run(){
        if(command.contains("t")){
            while(clientIsRunning) {
                pingClient();
            }
        }
        else{
            pingClient();
            getInputDetails();

        }
    }


    public void pingClient(){
        try{

            address = command;
            if (command.contains("-"))
                address = command.substring(command.indexOf("1"), command.indexOf("-") -1);


            ipAddress = InetAddress.getByName(address);

            ipHeader = ipAddress.getHostAddress();

            if (command.contains("a"))
                ipHeader = ipAddress.getHostName();



            sendData = new byte[packetLength];

            // allocate a data buffer
            for(int i =0; i < sendData.length; i++){
                // loop and fill in with data at each index position of the byte array of sendData
                sendData[i] = '0';
            }

            receiveData = new byte[packetLength];
            // receive the datagrams and specify the complete address of the server to ping

            sendPacket = new DatagramPacket(sendData, sendData.length, ipAddress, port);
            System.out.format("Pinging " + ipHeader
                                + " with " + echoRequests + " packets"
                                + " each of " + packetLength + " bytes %n");


            int sent = 0;
            int received = 0;
            long maxTime = 0;
            long minTime = 0;
            long totalTime = 0;
            boolean firstSend = true;


            while (sent < echoRequests) {
                long start = Calendar.getInstance().getTimeInMillis(); // gets the current in ms
                int lost = sent - received;
                // checks if any packets lost and performs the & % lost
                float percentLoss = (sent != 0) ? ((float) lost / sent) * 100 : 0;
                float averageTime = (float) totalTime / sent;


                try {
                    clientSocket.send(sendPacket);
                    sent++;
                } catch (SocketTimeoutException ex) {
                    System.out.println("- Unable to send packet: connection timed out...");
                    continue;
                }catch (IOException ex) {
                    System.out.println(ex.toString());
                }

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

                System.out.println(
                        "+ Ping statistics for " + ipAddress.getHostAddress()
                                + ":Packets: Sent =" + sent + ", Received = " + received
                                + ", Lost = " + lost + " (" + percentLoss + "% loss)"
                );

                System.out.println(
                        "+ Approximate round trip times in milli-seconds:"
                                + "Minimum = " + minTime + "ms, Maximum = " + maxTime
                                + "ms, Average = " + averageTime + "ms");
                System.out.println("");


            }
        }catch (Exception e){
            System.out.println(e.toString());
        }
    }

}
