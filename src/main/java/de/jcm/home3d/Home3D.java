package de.jcm.home3d;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Scanner;

import de.jcm.home3d.packet.in.PacketIn;
import de.jcm.home3d.packet.in.PacketInDisconnected;
import de.jcm.home3d.packet.in.PacketInEncryption;
import de.jcm.home3d.packet.in.PacketInFile;
import de.jcm.home3d.packet.in.PacketInLoginSuccess;
import de.jcm.home3d.packet.in.PacketInTaskUpdate;
import de.jcm.home3d.packet.out.PacketOut;
import de.jcm.home3d.task.Task;
import de.jcm.security.rsa.RSAKeyPair;
import de.jcm.security.rsa.RSAPublicKey;
import de.jcm.util.Callback;

public class Home3D
{
	public static RSAKeyPair myKeys;
	public static RSAPublicKey serverKey;
	static boolean encrypted;
	
	private static DataOutputStream out;
	private static DataInputStream in;
	
	public static int BIT_LEGTH = 1024;
	
	public static int userId;
	
	public static Callback<Void, File> fileCallback;
	public static String fileName="";
	
	public static int pid;
	public static RandomAccessFile fifo;
	
	public static String username;
	public static String password;
	public static int printerId = 1;
	
	public static String printerModel;
	public static String printerPort;
	
	public static String hostname;
	public static int port;
	
	public static HashMap<Integer, Task> tasks=new HashMap<>();
	public static File config;
	private static MainThread main;
	
	public static Object notifier;
	public static File stlDir;
	public static File gcodeDir;
	
	@SuppressWarnings("deprecation")
	public static void main(String[] args) throws Exception
	{
		if(args.length>0)
		{
			BIT_LEGTH=Integer.parseInt(args[0]);
		}
		
		notifier=new Object();
		
		//Read config
		config=new File("config.txt");
		readConfig();
		
		try
		{
			fifo = new RandomAccessFile("/var/print3d.pipe", "rw");
			Scanner fin = new Scanner(new FileInputStream("/var/print3d.pid"));
			pid = Integer.parseInt(fin.nextLine());
			fin.close();
		}
		catch(Exception e)
		{
			System.out.println("Stating print3d daemon...");
			
			try
			{
				Runtime.getRuntime().exec("./print3d --start-daemon -m " + printerModel + " -p " + printerPort);
				
				Thread.sleep(1000);
				
				fifo = new RandomAccessFile("/var/print3d.pipe", "rw");
				Scanner fin = new Scanner(new FileInputStream("/var/print3d.pid"));
				pid = Integer.parseInt(fin.nextLine());
				fin.close();
			}
			catch(Exception e2)
			{
				System.out.println("Cannot access print3d daemon");
				System.out.println("Dry run...");
			}
		}
		
		stlDir=new File("downloads/stl");
		if(!stlDir.exists())
		{
			stlDir.mkdirs();
		}
		
		gcodeDir=new File("downloads/gcode");
		if(!gcodeDir.exists())
		{
			gcodeDir.mkdirs();
		}
		
		PacketIn.register(PacketInEncryption.class); // 1
		PacketIn.register(PacketInLoginSuccess.class); // 2
		PacketIn.register(PacketInDisconnected.class); // 3
														// 4
		PacketIn.register(PacketInTaskUpdate.class); // 5
														// 6
		PacketIn.register(PacketInFile.class); // 7
		
		System.out.println("Generating keys (length=" + BIT_LEGTH + ")...");
		for(int i = 0; i < 100; i++)
		{
			System.out.println("Try " + (i + 1) + "/" + 100);
			System.out.flush();
			myKeys = RSAKeyPair.generate(new SecureRandom(), BIT_LEGTH);
			
			BigInteger a1 = new BigInteger(myKeys.getPublicKey().getModulo().bitLength() - 1, new SecureRandom());
			BigInteger b1 = myKeys.getPublicKey().encrypt(a1);
			BigInteger c1 = myKeys.getPrivateKey().decrypt(b1);

			byte[] a2 = new BigInteger(myKeys.getPublicKey().getModulo().bitLength() * 2, new SecureRandom()).toByteArray();
			byte[] b2 = myKeys.getPublicKey().encrypt(a2);
			byte[] c2 = myKeys.getPrivateKey().decrypt(b2);
			
			boolean array = true;
			for(int j = 0; j < a2.length; j++)
			{
				if(a2[j] != c2[j])
				{
					System.out.println("a2["+j+"] != c2["+j+"]");
					System.out.println(a2[j]+" != "+c2[j]);
					
					array = false;
					break;
				}
			}
			
			if(a1.equals(c1) && (!a1.equals(b1)) && array)
			{
				System.out.println("Key generation succeeded");
				System.err.println("Public key: " + myKeys.getPublicKey().getExponent());
				System.err.println("Private key: " + myKeys.getPrivateKey().getExponent());
				System.err.println("Modulo: " + myKeys.getPublicKey().getModulo());
				// System.err.println("Results:");
				// System.err.println("a="+a1);
				// System.err.println("b="+b1);
				// System.err.println("c="+c1);
				// System.err.println("Details:");
				// System.err.println("Bit
				// length="+myKeys.getPublicKey().getModulo().bitLength());
				break;
			}
			myKeys = null;
		}
		if(myKeys == null)
		{
			System.out.println("Key generation failed");
			return;
		}
		
		while(true)
		{
			Socket sck = new Socket(hostname, port);
			in = new DataInputStream(sck.getInputStream());
			out = new DataOutputStream(sck.getOutputStream());
			
			if(main!=null)
				main.stop();
			
			main=new MainThread();
			main.start();
			
			while(!sck.isClosed())
			{
				for(Task task : tasks.values())
				{
					if(task.getCode()==1)
					{
						task.start();
					}
				}
				receive();
			}
			
			System.out.println("Lost connection!");
			System.out.println("Restarting...");
			
			sck.close();
		}
	}
	
	public static void receive()
	{
		try
		{
			int packetLength = in.readInt();
			
			byte[] packetData = new byte[packetLength];
			for(int i=0;i<packetLength;i++)
				packetData[i]=in.readByte();
			
			if(encrypted)
				packetData = myKeys.getPrivateKey().decrypt(packetData);
			
			PacketOut response = PacketIn.handle(packetData);
			if(response != null)
				sendPacket(response);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void sendPacket(PacketOut packet)
	{
		try
		{
			byte id = packet.getPacketId();
			ByteArrayOutputStream array = new ByteArrayOutputStream();
			DataOutputStream tout = new DataOutputStream(array);
			tout.writeByte(id);
			packet.write(tout);
			
			byte[] bytes = array.toByteArray();
			
			if(encrypted)
				bytes = serverKey.encrypt(bytes);
			
			out.writeInt(bytes.length);
			out.write(bytes);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void startEncryption(RSAPublicKey key)
	{
		serverKey = key;
		encrypted = true;
		
		System.out.println("Encryption started");
		
		synchronized(notifier)
		{
			notifier.notifyAll();
		}
	}
	
	public static void readConfig()
	{
		try
		{
			Scanner scan=new Scanner(config);
			while(scan.hasNextLine())
			{
				String line=scan.nextLine();
				String key=line.split("=")[0];
				String value=line.split("=")[1];
				
				switch(key)
				{
					case "username":
						username=value;
						break;
					case "password":
						password=value;
						break;
					case "printerId":
						printerId=Integer.parseInt(value);
						break;
					case "printerModel":
						printerModel=value;
						break;
					case "printerPort":
						printerPort=value;
						break;
					case "hostname":
						hostname=value;
						break;
					case "port":
						port=Integer.parseInt(value);
						break;
					
					default:
						break;
				}
			}
			scan.close();
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	public static void writeConfig()
	{
		try
		{
			PrintStream print=new PrintStream(config);

			print.println("username="+username);
			print.println("password="+password);
			
			print.println("printerId="+printerId);
			print.println("printerModel="+printerModel);
			print.println("printerPort="+printerPort);
			
			print.close();
		}
		catch(FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}
}
