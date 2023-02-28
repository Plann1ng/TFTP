package assignment3;

import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class TFTPServer 
{
	public static final int TFTPPORT = 4970;
	public static final int BUFSIZE = 516;
	public static final String READDIR = "/home/username/read/"; //custom address at your PC
	public static final String WRITEDIR = "/home/username/write/"; //custom address at your PC
	// OP codes
	//RPQ stands for Regular Path Query. It is a type of query used in graph databases to retrieve information about paths between nodes in a graph.
	//In a graph database, nodes represent entities and edges represent the relationships between these entities. 
	//Regular Path Queries allow you to specify a pattern of edges that must be traversed to reach a target node from a starting node.
	public static final int OP_RRQ = 1;

	public static final int OP_WRQ = 2;
	public static final int OP_DAT = 3;
	public static final int OP_ACK = 4;
	public static final int OP_ERR = 5;

	public static void main(String[] args) {
		if (args.length > 0) 
		{
			System.err.printf("usage: java %s\n", TFTPServer.class.getCanonicalName());
			System.exit(1);
		}
		//Starting the server
		try 
		{
			TFTPServer server= new TFTPServer();
			server.start();
		}
		catch (SocketException e) 
			{e.printStackTrace();}
	}
	
	private void start() throws SocketException 
	{
		byte[] buf= new byte[BUFSIZE];
		
		// Create socket
		// UDP creating since it is not bound to any port.
		DatagramSocket socket= new DatagramSocket(null);
		
		// Create local bind point 
		SocketAddress localBindPoint= new InetSocketAddress(TFTPPORT);
		socket.bind(localBindPoint);

		System.out.printf("Listening at port %d for new requests\n", TFTPPORT);

		// Loop to handle client requests 
		while (true) 
		{        
			
			final InetSocketAddress clientAddress = receiveFrom(socket, buf);
			
			// If clientAddress is null, an error occurred in receiveFrom()
			if (clientAddress == null) 
				continue;

			final StringBuffer requestedFile= new StringBuffer();
			final int reqtype = ParseRQ(buf, requestedFile);

			new Thread() 
			{
				public void run() 
				{
					try 
					{
						DatagramSocket sendSocket= new DatagramSocket(0);

						// Connect to client
						sendSocket.connect(clientAddress);
						//TODO: define them on top
						CLIENT_TID = sendSocket.getPort();
						SERVER_TID = sendSocket.getLocalPort();						
						
						System.out.printf("%s request for %s from %s using port %d\n",
								(reqtype == OP_RRQ)?"Read":"Write",
								clientAddress.getHostName(), clientAddress.getPort());  
								
						// Read request
						if (reqtype == OP_RRQ) 
						{      
							requestedFile.insert(0, READDIR);
							HandleRQ(sendSocket, requestedFile.toString(), OP_RRQ);
						}
						// Write request
						else 
						{                       
							requestedFile.insert(0, WRITEDIR);
							HandleRQ(sendSocket,requestedFile.toString(),OP_WRQ);  
						}
						sendSocket.close();
					} 
					catch (SocketException e) 
						{e.printStackTrace();}
				}
			}.start();
		}
	}
	
	/**
	 * Reads the first block of data, i.e., the request for an action (read or write).
	 * @param socket (socket to read from)
	 * @param buf (where to store the read data)
	 * @return socketAddress (the socket address of the client)
	 */
	//Listen on the predefined port by implementing a receiveFrom() method.
	private InetSocketAddress receiveFrom(DatagramSocket socket, byte[] buf) 
	{
		// Create datagram packet
		DatagramPacket pckt = new DatagramPacket(buf, buf.length);
		
		
		
		// Receive packet
		socket.receive(pckt);
		// Get client address and port from the packet
		return new InetSocketAddress(packet.getAddress(), packet.getPort());
	}

	/**
	 * Parses the request in buf to retrieve the type of request and requestedFile
	 * 
	 * @param buf (received request)
	 * @param requestedFile (name of file to read/write)
	 * @return opcode (request type: RRQ or WRQ)
	 */
	// Parse a read request by implementing a ParseRQ() method. The first 2
	//bytes of the message contains the opcode indicating the type of request.

	private int ParseRQ(byte[] buf, StringBuffer requestedFile) 
	{
		// See "TFTP Formats" in TFTP specification for the RRQ/WRQ request contents
		ByteBuffer wrap = new ByteBuffer(buf);
		requestedFile.append(new String(buf, 2, buf.lenght, buf.lenght-2)); //TODO: To be checked here
		if (!requestedFile.toString().split("\0")[1].equals("octet")) {
			System.out.println("Invalid mode");
		}


		
		return wrap.getShort();
	}

	/**
	 * Handles RRQ and WRQ requests 
	 * 
	 * @param sendSocket (socket used to send/receive packets)
	 * @param requestedFile (name of file to read/write)
	 * @param opcode (RRQ or WRQ)
	 */
	private void HandleRQ(DatagramSocket sendSocket, String requestedFile, int opcode) 
	{		
		if(opcode == OP_RRQ)
		{
			int block = 0;
			//The block is initialized to zero because it is the initial block number for data transmission in TFTP. 
			//When a data packet is sent, it contains a block number that starts from 1, and the receiver expects 
			//the next block number to be the previous block number plus one. Therefore, the first data packet sent 
			//should have a block number of 1, and the initial value of the block variable should be 0 so that it can 
			//be incremented to 1 for the first data packet transmission.
			// Send data for this current request and request the next acknowledegment.
			boolean result = send_DATA_receive_ACK(sendSocket, requestedFile, ++block);
		}
		else if (opcode == OP_WRQ) 
		{
			int block = 0;
			// Receive the current data and send the current acknowledgement.
			boolean result = receive_DATA_send_ACK(sendSocket, requestedFile, block);
		}
		else 
		{
			System.err.println("Invalid request. Sending an error packet.");
			// See "TFTP Formats" in TFTP specification for the ERROR packet contents
			send_ERR(sendSocket, 4, "Invalid request");
			return;
		}		
	}
	
	/**
	To be implemented
	*/
	private boolean send_DATA_receive_ACK(params)
	{return true;}
	
	private boolean receive_DATA_send_ACK(params)
	{return true;}
	
	private void send_ERR(params)
	{}
	
}



