package javaProgramming;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class Server extends JFrame {
	private JTextField enterField; // text field to enter messages
	private JTextArea display; // text area to display messages
	private JScrollPane scroller; // scrollpane to scroll text area
	private List<ClientRunnable> clients; // list of clients
	private ExecutorService clientExecutor; // executor service for clients
	private ServerSocket server; // server socket to accept connections

	// set up GUI
	public Server() {
		super("Server");

		enterField = new JTextField(); // create enterField
		enterField.setEnabled(false); // disable editability
		enterField.addActionListener(new ActionListener() {
			// send message to each client
            @Override
            public void actionPerformed(ActionEvent event) {
            	for (ClientRunnable eachClient : clients) {
            		eachClient.sendData(event.getActionCommand());
            		
            	enterField.setText(""); // clear enterField
            	} // end actionPerformed
            } // end anonymous inner class
		}); //end call to addActionListener

		display = new JTextArea(); // create JTextArea
		display.setEditable(false); // disable editing
		scroller = new JScrollPane(display); // add display to scrollpane

		add(enterField, BorderLayout.NORTH); // add enterField
		add(scroller, BorderLayout.CENTER); // add scrollpane

		setSize(300, 150); // set window size
		setVisible(true); // show window

		clientExecutor = Executors.newCachedThreadPool(); // for clients
		clients = new CopyOnWriteArrayList<>(); // initialize clients
	} // end constructor Server

	// set up and run server
	public void runServer() {
		try { // set up server and process connections
			server = new ServerSocket(5558, 100); // create server socket
			ClientRunnable newClient; // to hold new clients
			
			// accept connections and add clients to ArrayList
			while (true) {
				displayMessage("Waiting for connection\n");
				newClient = new ClientRunnable(server.accept(),
						display, clients.size()); //create a new client
				clients.add(newClient); // add client to ArrayList
				clientExecutor.execute(newClient); // run client
				enterField.setEnabled(true); // enable enterField
			} // end while
		} // end try
		catch (IOException ioException) {
			ioException.printStackTrace();
		} // end catch
	} // end method runServer

	public void displayMessage(final String message) {
		SwingUtilities.invokeLater(
				new Runnable() {
					public void run() {
						display.append(message);
					} // end method run
				} // end anonymous inner class
			); // end call to SwingUtilities.invokeLater
		} // end method displayMessage

    // private inner class ClientRunnable manages each client
	private class ClientRunnable implements Runnable {
		private int clientNumber; // number of client in server
		private Socket connection; // connection to client
		private ObjectOutputStream output; // output stream to client
		private ObjectInputStream input; // input stream from client
		private JTextArea display; // displays messages

		// set up a client runnable
		public ClientRunnable(Socket socket, JTextArea displayArea, int number) {
			display = displayArea; // store the display
			clientNumber = number; // store this client's number
			connection = socket; // store connection to client

			try { //obtain streams from Socket
				output = new ObjectOutputStream(connection.getOutputStream());
				sendData("Connection successful"); // notify of connection
				displayMessage("\nConnection " + clientNumber + 
						" received from: " + connection.getInetAddress().getHostName() + "\n");	
				output.flush();
				input = new ObjectInputStream(connection.getInputStream());
			} // end try
			catch (IOException ioException) {
				ioException.printStackTrace();
			} // end catch
		} // end ClientRunnable constructor

		// send message to client
		public void sendData(String message) {
			try { // send object to client
				output.writeObject("SERVER>>> " + message); // write output
				output.flush(); // flush data to client
				displayMessage("\nSERVER>>>" + message); // output message
			} // end try
			catch (IOException ioException) {
				displayMessage("\nError writing object");
			} // end catch
		} // end method sendData

		// control thread's execution
		@Override
		public void run() {
			String message = null; // initialize message string
			try { // process connection
				do { // read message from client
					try {
						message = (String) input.readObject();
						displayMessage("\n" + message);
					} // end try
					catch (ClassNotFoundException classNotFoundException) {
						displayMessage("\nUnknown object type received");
					} // end catch
				} 
				while (!message.equals("CLIENT>>> TERMINATE"));
				displayMessage("\nClient terminated connection");
				display = null;
			} // end try
			catch (IOException ioException) {
				System.out.println("\nClient terminated connection");
			} // end catch
			finally {
				try {
					if(output != null)output.close(); // close output stream
					if(input != null)input.close(); // close input stream
					if(connection != null && !connection.isClosed())connection.close(); // close socket
				} // end try
				catch (IOException ioException) {
					ioException.printStackTrace();
				} // end catch
				clients.remove(this); // remove this client from list
			} // end finally
		} // end method run
	} // end class ClientRunnable
} // end class Server 
