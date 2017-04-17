/**
 * Created by aksha on 16-Apr-17.
 */

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.Scanner;

public class Server implements Runnable{

    Scanner sn = new Scanner(System.in);
    private int port, packetSize;
    private String ipAddress;
    private DatagramSocket serverSocket;
    private DatagramPacket receivePacket;
    private byte[] receiveData;

    public Server() {

        setServerDetails();
        getIpAddress();
//        waitForInput();


    }

    private void waitForInput() {

        System.out.println("Next command: ");
        String command = sn.next();

        if(command.equals("stop"))
            serverSocket.close();
        else
            setServerDetails();
            getIpAddress();
    }


    private void setServerDetails(){

        try  {

            System.out.print("Enter port number: ");
            port = sn.nextInt();
            System.out.print("Enter packet size: ");
            packetSize = sn.nextInt();

        } catch (Exception e) { System.out.println(e.toString()); setServerDetails();}

    }

    private String getIpAddress() {

        try {
            String address = InetAddress.getLocalHost().toString().substring(InetAddress.getLocalHost().toString().indexOf("/") + 1);
            if (address.equals("127.0.0.1")) {
                System.out.println("Your computer is not connected to a network");
                System.exit(1);
            }
            System.out.format("Server successfully started.... %nIPv4 Address: " + address + " port: " + port + "%n");
            return address;

        } catch (Exception e) {
            System.out.println(e.toString());
            return "Unknown";

        }
    }

    public void run(){

        try{
            //Create a server socket
            serverSocket = new DatagramSocket(port);
            System.out.println("");
            System.out.println("Server successfully started.");

        } catch (Exception e) {
            System.out.println("Failed: UNABLE TO START SERVER. Try a different port number.");
        }


        receiveData = new byte[packetSize];

        while (true){

            receivePacket = new DatagramPacket(receiveData, receiveData.length);
            int count = 0;
            try {
                serverSocket.receive(receivePacket);
                System.out.println("Received a ping from " + receivePacket.getAddress().getHostName());
            } catch (Exception e) {}

            if (receivePacket.getPort() == -1) {
                receivePacket.setPort(port);
            }

            DatagramPacket sendPacket = new DatagramPacket(
                    receivePacket.getData(),
                    count,
                    receivePacket.getAddress(),
                    receivePacket.getPort());
            try {
                serverSocket.send(sendPacket);
            } catch (Exception ex) {
                System.out.println(ex.toString());
            }
        }

    }

}
