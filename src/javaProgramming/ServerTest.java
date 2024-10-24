package javaProgramming;

//Tests the server class.
import javax.swing.JFrame;

public class ServerTest
{
public static void main(String[] args)
{
   Server application = new Server(); // create the server
   application.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
   application.runServer(); // run the server
} // end main
} // end class ServerTest
