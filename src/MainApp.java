/**
 * Created by aksha on 16-Apr-17.
 */

import java.util.Scanner;

public class MainApp{

    static String input;
    static Scanner sn = new Scanner(System.in);
    public static void main(String[] args) {

        try {

            System.out.println("Initializing App....");

            input = takeInput();

            if( input.equals("client") ) {
                Client client = new Client();
            } else if ( input.equals("server") ) {
                Server server = new Server();
                Thread thread = new Thread(server);
                thread.start();
            } else {
                System.out.println("Please chose either client or server ");
                input = takeInput();
            }

        } catch (Exception e) { System.out.println(e.toString()); }


    }

    public static String takeInput(){

        System.out.print("Run app as server or client? : ");

        input = sn.next();

        return input;
    }


}
