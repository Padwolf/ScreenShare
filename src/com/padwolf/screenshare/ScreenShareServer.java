package com.padwolf.screenshare;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.*;
import java.util.Arrays;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JWindow;

public class ScreenShareServer {
	
	private Client[] connectedClients, waitingClients;
	
	public ScreenShareServer(int port, int capacity) throws Exception{
		DatagramSocket serverSocket = new DatagramSocket(port);
		connectedClients = new Client[capacity];
		waitingClients = new Client[capacity];
		Client active = null, host = null;
		boolean collecting = false;
		byte[] receiveData = new byte[1024];
		byte[] sendData = new byte[1024];
		byte[] tempImg = new byte[0];
		
		ImageFrame frame = new ImageFrame();
		
//		while(getLastFilledIndex(connectedClients) == -1){
//			serverSocket.setSoTimeout(0);
//			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
//			serverSocket.receive(receivePacket);
//			System.out.println(new String(receivePacket.getData()));
//			if (new String(receivePacket.getData()).equals("con") || new String(receivePacket.getData()) == "hst"){
//				InetAddress lIp = receivePacket.getAddress();
//				sendData = "ack".getBytes();
//				DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
//						receivePacket.getAddress(), receivePacket.getPort());
//				serverSocket.send(sendPacket);
//				receivePacket = new DatagramPacket(receiveData, receiveData.length);
//				serverSocket.setSoTimeout(1000);
//				try {
//					serverSocket.receive(receivePacket);
//				} catch (SocketTimeoutException e) {
//					
//				}
//				if (receivePacket.getAddress() == lIp && new String(receivePacket.getData()) == "rdy"){
//				connectedClients[0] = new Client(receivePacket.getAddress(), receivePacket.getPort());
//				active = connectedClients[0];
//				}
//			}
//			
//		}
		
		//For the random chance of switching the screen and the random screen to choose
		Random rand = new Random();
		
		while (true){
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			serverSocket.receive(receivePacket);
			System.out.println(new String(receivePacket.getData()));
			if ( active != null && receivePacket.getAddress() == active.ip){
				//A signal from the screen currently being streamed.
				if (new String(receivePacket.getData()) == "srt"){
					//A signal to start collecting data as the image
					collecting = true;
					tempImg = new byte[0];
				} else if (new String(receivePacket.getData()) == "end"){
					//A signal saying the image is completely streamed. This stage constructs the image and sends it to the frame
					collecting = false;
					InputStream in = new ByteArrayInputStream(tempImg);
					BufferedImage nCapture = ImageIO.read(in);
					frame.updateImage(nCapture);
					if (rand.nextInt(100000000) == 16101){
						//changes the screen being recorded. Only happens at the end of a capture.
						DatagramPacket sendPacket = new DatagramPacket("dct".getBytes(), "dct".getBytes().length, active.ip, active.port);
						serverSocket.send(sendPacket);
						active = connectedClients[rand.nextInt(getLastFilledIndex(connectedClients)+1)];
						sendPacket = new DatagramPacket("act".getBytes(), "act".getBytes().length, active.ip, active.port);
						serverSocket.send(sendPacket);
					}
				} else if (new String(receivePacket.getData()) == "dcn"){
					collecting = false;
					connectedClients = removeClientByIp(connectedClients, receivePacket.getAddress());
				} else if (collecting){
					concat(tempImg, receivePacket.getData());
					DatagramPacket sendPacket = new DatagramPacket("rdy".getBytes(), "rdy".getBytes().length, active.ip, active.port);
				}
				
			} else if (new String(receivePacket.getData()) == "hst"){
				if (host != null)
					sendData = "den".getBytes();
				else {
					sendData = "ack".getBytes();
					host = new Client(receivePacket.getAddress(), receivePacket.getPort());
				}
				DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
						receivePacket.getAddress(), receivePacket.getPort());
				serverSocket.send(sendPacket);
			} else if (host != null && receivePacket.getAddress() == host.ip){
					if (new String(receivePacket.getData()).equals("sdn")){
						for (int i = 0; i < connectedClients.length; i++){
							serverSocket.send(new DatagramPacket("sdn".getBytes(), "sdn".getBytes().length, connectedClients[i].ip, connectedClients[i].port));
						}
						serverSocket.send(new DatagramPacket("sdn".getBytes(), "sdn".getBytes().length, host.ip, host.port));
						serverSocket.close();
						System.exit(0);
					} else if (new String(receivePacket.getData()).equals("nxt")){
						DatagramPacket sendPacket = new DatagramPacket("dct".getBytes(), "dct".getBytes().length, active.ip, active.port);
						serverSocket.send(sendPacket);
						active = connectedClients[rand.nextInt(getLastFilledIndex(connectedClients)+1)];
						sendPacket = new DatagramPacket("act".getBytes(), "act".getBytes().length, active.ip, active.port);
						serverSocket.send(sendPacket);
					}
			} else if(Arrays.asList(waitingClients).contains(receivePacket.getAddress())){
				if (new String(receivePacket.getData()) == "rdy"){
					connectedClients[getLastFilledIndex(connectedClients)+1].ip = receivePacket.getAddress();
					connectedClients[getLastFilledIndex(connectedClients)+1].port = receivePacket.getPort();
				}
			} else {
				if (new String(receivePacket.getData()).equals("con")){
					if (getLastFilledIndex(connectedClients) == connectedClients.length-1)
						sendData = "den".getBytes();
					else {
						sendData = "ack".getBytes();
						waitingClients[getLastFilledIndex(waitingClients)+1] = new Client(receivePacket.getAddress(), receivePacket.getPort());  
					}
					DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
							receivePacket.getAddress(), receivePacket.getPort());
					serverSocket.send(sendPacket);
				}
			}
			
		}
	}
	
	public int getLastFilledIndex(Client[] array){
		for (int i = 0; i < array.length; i++){
			if (array[i] == null){
				return i-1;
			}
		}
		return array.length-1;
	}
	
	public int getLastFilledIndex(int[] array){
		for (int i = 0; i < array.length; i++){
			if (array[i] == 0){
				return i-1;
			}
		}
		return array.length-1;
	}
	
	public byte[] concat(byte[] a, byte[] b) {
		   int aLen = a.length;
		   int bLen = b.length;
		   byte[] c= new byte[aLen+bLen];
		   System.arraycopy(a, 0, c, 0, aLen);
		   System.arraycopy(b, 0, c, aLen, bLen);
		   return c;
	}
	
	public Client[] removeClientByIp(Client[] array, InetAddress ip){
		int index = -1;
		for (int i = 0; i < array.length; i++){
			if (connectedClients[i].ip.equals(ip)){
				index = i;
			}
		}
		
		if (index == -1) return array;
		
		for (int i = index+1; i < array.length; i++){
			array[i-1] = array[i];
		}
		Client[] temp = new Client[array.length-1];
		for (int i = 0; i < temp.length; i++){
			temp[i] = array[i];
		}
		return temp;
	}
	
	private class Client{
		public InetAddress ip;
		public int port;
		public Client(InetAddress ipAddress, int port){
			ip = ipAddress;
			this.port = port;
		}
	}
	
	private class ImageFrame extends JWindow{
		
		ImageFrame(){
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			this.setSize(screenSize);
			this.setAlwaysOnTop(false);
			JLabel loading = new JLabel("Loading...");
			loading.setFont(loading.getFont().deriveFont(64.0f));
			add(loading);
			
			setVisible(true);
		}
		
		void updateImage(BufferedImage image){
			this.removeAll();
			double widthRatio = Toolkit.getDefaultToolkit().getScreenSize().width/image.getWidth();
			double heightRatio = Toolkit.getDefaultToolkit().getScreenSize().height/image.getHeight();
			add(new JLabel(new ImageIcon(image.getScaledInstance((int)(image.getWidth()*widthRatio), (int)(image.getHeight()*heightRatio), BufferedImage.SCALE_DEFAULT))));
		}
	}

}
