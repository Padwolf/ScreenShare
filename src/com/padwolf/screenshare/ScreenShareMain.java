package com.padwolf.screenshare;

import java.net.InetAddress;

public class ScreenShareMain {

	public static void main(String[] args) throws Exception{
		boolean server = false;
		boolean host = false;
		int port = 16101;
		int cap = 7;
		InetAddress IPAddress = null;
		if (args.length > 0){
			for (int i = 0; i < args.length; i++){
				switch (args[i]){
				case "-s":
					server = true;
					break;
				case "--server":
					server = true;
					break;
				case "-p":
					i++;
					port = Integer.parseInt(args[i]);
					break;
				case "-c":
					i++;
					cap = Integer.parseInt(args[i]);
					break;
				case "-ip":
					i++;
//					String[] sAddress = args[i].split(".");
//					byte[] address = new byte[sAddress.length];
//					for (String part : sAddress){
//						address[i] = Byte.parseByte(part);
//					}
					IPAddress = InetAddress.getByName(args[i]);
					break;
				case "-h":
					IPAddress = InetAddress.getByName("localhost");
					host = true;
				default:
					break;
				}
				if (server) new ScreenShareServer(port, cap);
				else new ScreenShareClient(IPAddress, port, host);
			}
		}
	}

}
