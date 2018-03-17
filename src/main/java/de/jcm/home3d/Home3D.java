package de.jcm.home3d;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Scanner;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import de.jcm.home3d.packet.in.PacketIn;
import de.jcm.home3d.packet.in.PacketInDisconnected;
import de.jcm.home3d.packet.in.PacketInEncryption;
import de.jcm.home3d.packet.in.PacketInFile;
import de.jcm.home3d.packet.in.PacketInLoginSuccess;
import de.jcm.home3d.packet.in.PacketInTaskUpdate;
import de.jcm.home3d.packet.out.PacketOut;
import de.jcm.home3d.packet.out.PacketOutEncryption;
import de.jcm.home3d.task.Task;
import de.jcm.security.rsa.RSAPublicKey;
import de.jcm.util.Callback;

public class Home3D
{
	public static int KEY_BYTES = 16;
	
	public static SecretKeySpec myKey;
	public static byte[] myKeyBytes;
	
	public static RSAPublicKey serverKey;
	static int encrypted;
	
	private static DataOutputStream out;
	private static DataInputStream in;
	
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
		
		System.out.println("Generating keys (length=" + KEY_BYTES + ")...");

		myKeyBytes=new byte[KEY_BYTES];
		SecureRandom random=new SecureRandom();
		random.nextBytes(myKeyBytes);
			
		myKey = new SecretKeySpec(myKeyBytes, "AES");
		
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
//				Set<Thread> set = Thread.getAllStackTraces().keySet();
//				set.forEach(thread -> System.out.println(thread));
				
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
			
			if(encrypted==2)
			{
				Cipher c = Cipher.getInstance("AES/CBC/PKCS5PADDING");
				c.init(Cipher.DECRYPT_MODE, myKey, new IvParameterSpec(myKey.getEncoded()));
				
				packetData = c.doFinal(packetData);
			}
			
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
			
			if(encrypted==1)
			{
				bytes = serverKey.encrypt(bytes);
			}
			else if(encrypted==2)
			{
				Cipher c = Cipher.getInstance("AES/CBC/PKCS5PADDING");
				c.init(Cipher.ENCRYPT_MODE, myKey, new IvParameterSpec(myKey.getEncoded()));
				
				bytes = c.doFinal(bytes);
			}

			System.out.println("-> " + packet + "@" + bytes.length);
			
			out.writeInt(bytes.length);
			out.write(bytes);
			
			if(packet instanceof PacketOutEncryption)
			{
				startEncryption();
				synchronized(notifier)
				{
					notifier.notifyAll();
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void startEncryption(RSAPublicKey key)
	{
		serverKey = key;
		encrypted = 1;
		
		System.out.println("RSA Encryption started");
	}
	
	public static void startEncryption()
	{
		encrypted = 2;
		
		System.out.println("AES Encryption started");
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
