package com.padwolf.screenshare;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

import javax.swing.JButton;
import javax.swing.JFrame;

public class ScreenShareClient {
	
	InetAddress IPAddress;
	int port;
	boolean host;
	
	public ScreenShareClient(InetAddress ip, int port, boolean host) throws Exception{
		IPAddress = ip;
		this.port = port;
		this.host = host;
		DatagramSocket clientSocket = new DatagramSocket();
		DatagramPacket send = new DatagramPacket("hst".getBytes(), "hst".getBytes().length, IPAddress, port);
		clientSocket.send(send);
		DatagramPacket receive = new DatagramPacket(new byte[1024], 1024);
		String rm;
		do{
			clientSocket.receive(receive);
			rm = new String(receive.getData());
			System.out.println(rm);
		} while (rm != "ack" && rm != "den");
		
		if (rm == "den") System.exit(0);
		
		new exitWindow();
//		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
//		DatagramSocket clientSocket = new DatagramSocket();
//		byte[] sendData = new byte[1024];
//		byte[] receiveData = new byte[1024];
//		String received = inFromUser.readLine();
//		sendData = received.getBytes();
//		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
//		clientSocket.send(sendPacket);
//		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
//		clientSocket.receive(receivePacket);
//		System.out.println("FROM SERVER: " + new String(receivePacket.getData()));
//		clientSocket.close();
	}
	
	public class exitWindow extends JFrame{
		private static final long serialVersionUID = -2306992044326369908L;

		public exitWindow() throws Exception{
			System.out.println(IPAddress + ": " + port + " | host: " + host);
			if (!host){
				this.setSize(350, 200);
				this.setUndecorated(true);
				setTitle("Screen Share Client");
				setLocation(0, 0);
				JButton disconnect = new JButton("Diconnect");
				disconnect.setSize(100, 75);
				disconnect.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							DatagramSocket clientSocket = new DatagramSocket();
							clientSocket.send(new DatagramPacket("nxt".getBytes(), "nxt".getBytes().length, IPAddress, port));
							clientSocket.close();
						} catch (IOException e1){ e1.printStackTrace();}
					}
				});
				
				setLayout(new GridLayout(2, 1));
				add(disconnect);
				setVisible(true);
			} else {
				this.setSize(350, 200);
				this.setUndecorated(true);
				setTitle("Screen Share Host Client");
				setLocation(0, 0);
				JButton newScreen = new JButton("New Screen");
				newScreen.setSize(100, 75);
				newScreen.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							DatagramSocket clientSocket = new DatagramSocket();
							clientSocket.send(new DatagramPacket("nxt".getBytes(), "nxt".getBytes().length, IPAddress, port));
							clientSocket.close();
						} catch (IOException e1){ e1.printStackTrace();}
					}
				});
				
				JButton shutdown = new JButton("Shutdown");
				shutdown.setSize(100, 75);
				shutdown.addActionListener(new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							DatagramSocket clientSocket = new DatagramSocket();
							DatagramPacket send = new DatagramPacket("sdn".getBytes(), "sdn".getBytes().length, IPAddress, port);
							clientSocket.send(send);
							DatagramPacket receive;
							clientSocket.setSoTimeout(1000);
							for (int i = 0; i < 5; i++){ 
								receive = new DatagramPacket(new byte[1024], 1024);
								try {
									clientSocket.receive(receive);
								} catch (SocketTimeoutException e1) {
									e1.printStackTrace();
								}
								if (receive.getAddress() == IPAddress && new String(receive.getData()) == "sdn") break;
								clientSocket.send(send);
							}
							clientSocket.close();
							System.exit(0);
						} catch (IOException e1) {e1.printStackTrace();}
					}
				});
				setLayout(new GridLayout(2, 1));
				add(newScreen);
				add(shutdown);
				setVisible(true);
			}
		}
	}
	
	public void waitForSignal() throws Exception{
		while (true){
			byte[] receiveData = new byte[1024];
			DatagramSocket clientSocket = new DatagramSocket();
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			clientSocket.receive(receivePacket);
			
			clientSocket.close();
		}
	}
	
	public void sendData(byte[] data) throws Exception{
		//if (data.length != 1024) throw new IllegalArgumentException("Please use a byte array with a size of 1024");
		DatagramSocket clientSocket = new DatagramSocket();
		DatagramPacket sendPacket = new DatagramPacket(data, data.length, IPAddress, port);
		clientSocket.send(sendPacket);
		
		clientSocket.close();
	}
}
