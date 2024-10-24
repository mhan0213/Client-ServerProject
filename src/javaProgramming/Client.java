package javaProgramming;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class Client extends JFrame {
    private JTextField enterField; // text field to enter messages
    private JTextArea displayArea; // text area to display messages
    private JScrollPane scroller; // scrollpane for text area
    private Socket client; // client socket
    private ObjectOutputStream output; // output stream
    private ObjectInputStream input; // input stream
    private String message = ""; // message to send to server

    // set up GUI
	public Client() {
		super("Client");

		// WindowListener
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (client != null && !client.isClosed()) {
					sendData("TERMINATE"); // send termination message
					System.exit(0); // exit application
                } //end method windowClosing
            } //end anonymous inner class
        }); //end call to addWindowListener

		enterField = new JTextField(); // create enterField
		enterField.setEnabled(false); // disable editability
		enterField.addActionListener(new ActionListener() {
			// send message to server
			@Override
			public void actionPerformed(ActionEvent event) {
				sendData(event.getActionCommand());
				enterField.setText(""); // clear text field
				} // end method actionPerformed
			} // end anonymous inner class
		); // end call to addActionListener

		displayArea = new JTextArea(); // create text area
		displayArea.setEditable(false); // disable editing
		scroller = new JScrollPane(displayArea); // create scrollpane
		add(enterField, BorderLayout.NORTH); // add enterField
		add(scroller, BorderLayout.CENTER); // add scrollpane
		setSize(300, 150); //set window size
		this.setVisible(true); //show window
		} // end constructor Client

	// connect to server, get streams, process connection
	public void runClient() {
		//connect to server, get streams, process connection
		try {
			displayMessage("Attempting connection\n");

			// create socket to make connection to server
			client = new Socket(
					InetAddress.getLocalHost(), 5558);

			// display connection information
			displayMessage("Connected to: " + 
					client.getInetAddress().getHostName());

			// set up output stream for objects
			output = new ObjectOutputStream(client.getOutputStream());
			output.flush(); // flush output to send header information

			// set up input stream for objects
			input = new ObjectInputStream(client.getInputStream());

			displayMessage("\nGot I/O streams\n");
			enterField.setEnabled(true); // allow client to send messages

			do { // process messages sent from server
				try { //read message and display it
                    message = (String) input.readObject(); // read message
                    displayMessage("\n" + message); // append message
				} // end try
				catch (ClassNotFoundException classNotFoundException) {
                    displayMessage("\nUnknown object type received");
                } //end catch
            } 
			while (!message.equals("SERVER>>> TERMINATE"));
			
			displayMessage("\nClosing connection.\n");
		} // end try
		catch (EOFException eofException) {
			System.err.println("Server terminated connection");
        } // end catch
		catch (IOException ioException) {
			ioException.printStackTrace();
		} // end catch
		finally {
			try {
				if(output != null)output.close(); // close output stream
				if(input != null)input.close(); // close input stream
				if(client != null && !client.isClosed())client.close(); // close socket
				displayMessage("\nConnection closed.");
			} // end try
			catch (IOException ioException) {
				ioException.printStackTrace();
				System.exit(0);
			} // end catch
		} // end finally
	} // end method runClient

	public void displayMessage(final String message) {
		SwingUtilities.invokeLater(
			new Runnable() {
				@Override
				public void run() {
					displayArea.append(message);
				} // end method run
			} // end anonymous inner class
		); // end call to SwingUtilities.invokeLater
	} // end method displayMessage

	// send message to server
	private void sendData(String string) {
		try { //send object to client
			message = string; // set message
			output.writeObject("CLIENT>>> " + string); // write to server
			output.flush(); // flush output to server
			displayMessage("\nCLIENT>>> " + string);
		} // end try
		catch (IOException ioException) {
			displayMessage("\nError writing object");
			ioException.printStackTrace();
		} // end catch
	} //end method sendData
} // end class Client