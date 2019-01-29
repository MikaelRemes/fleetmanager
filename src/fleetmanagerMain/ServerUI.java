package fleetmanagerMain;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.sun.net.httpserver.HttpServer;

public class ServerUI extends JFrame implements ActionListener{
	
	private static final long serialVersionUID = 1L;
	private boolean serverStarted=false;
	private boolean serverStopped=false;
	
	JLabel serverStatusLabel;
	JButton serverStartStop;
	
	HttpServer server;
	
	/**
	 * constructor
	 * also creates the UI for user
	 */
	public ServerUI(HttpServer server) {
		super("Fleetmanager server GUI");								//	creates JFrame for UI
		this.server=server;
		
		
		
		//creates button for starting/stopping server
        JPanel panel = new JPanel(new GridLayout(2,1));					// adds panel for start/stop button and text label
        serverStartStop = new JButton("Start server");					// adds a button for starting and stopping server
        
        serverStartStop.setBackground(new Color(50, 200, 45));			//something to make UI look a little prettyer
        serverStartStop.setFocusPainted(false);
        serverStartStop.setFont(new Font("Tahoma", Font.BOLD, 12));
        panel.setBackground(Color.WHITE);
        
        panel.add(serverStartStop);										// adds the button to the panel
		add(panel, BorderLayout.NORTH);									// adds the panel to the frame top
        serverStartStop.addActionListener(this);						// adds actionlistener to button
        
        serverStatusLabel = new JLabel("Server is not running.");		// adds text label
        panel.add(serverStatusLabel);									// adds label to panel
        
        
        
        this.add(panel);												// adds panels to frame
        setSize(300, 200);												// size for JFrame
        this.setVisible(true);											// once JFrame complete, set it visible
		
		
	}
	
	
	/**
	 * handles what happens when button is pressed
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if(serverStarted==true) {												// if server is running
			server.stop(1);														// stop server
			serverStopped=true;
			this.setVisible(false);												// closes the UI
			this.dispose();
		}
		if(serverStarted==false) {												// if server is not running
			server.start();														// start server
			System.out.println("server and connection handler online \n");
			serverStarted=true;
			serverStartStop.setText("Shut down server");
			serverStatusLabel.setText("Sever is running.");
		}
		
	}
	
	public boolean getServerStopped() {
		return serverStopped;
	}

}
