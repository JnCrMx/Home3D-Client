package de.jcm.home3d.packet.in;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import de.jcm.home3d.Home3D;
import de.jcm.home3d.packet.out.PacketOutRequestFile;

public class PacketInFile extends PacketIn<PacketOutRequestFile>
{
	private File file;
	private String checksumServer;
	private String checksumClient;
	
	@Override
	public void read(DataInputStream in)
	{
		if(Home3D.fileName == null)
			Home3D.fileName = System.currentTimeMillis() + ".bin";
		try
		{
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			
			file = new File(Home3D.fileName);
			file.delete();
			FileOutputStream out = new FileOutputStream(file);
			
			int len = in.readInt();
			
			byte[] bytes=new byte[len];
			in.read(bytes);
			out.write(bytes);
			md.update(bytes);
			
			out.close();
			
			checksumClient="";
			byte[] mdbytes=md.digest();
			for(byte b : mdbytes)
			{
				String s = Integer.toHexString(Byte.toUnsignedInt(b));
				if(s.length() < 2)
					s = "0" + s;
				checksumClient = checksumClient + s;
			}
			
			int mdLen=in.readInt();
			byte[] mdBytes=new byte[mdLen];
			in.read(mdBytes);
			checksumServer=new String(mdBytes);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		catch(NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public PacketOutRequestFile handle()
	{
		System.gc();
		
		System.out.println("File "+Home3D.fileName+" received!");
		if(Home3D.fileCallback != null && !Home3D.fileName.isEmpty())
		{
			if(checksumClient.equals(checksumServer))
			{
				Home3D.fileCallback.call(file);
			}
			else
			{
				System.out.println("Wrong checksum for "+Home3D.fileName+": "+checksumServer+" != "+checksumClient);
				return new PacketOutRequestFile(Home3D.fileName, Home3D.fileCallback);
			}
		}
		
		Home3D.fileName="";
		
		return null;
	}
	
	@Override
	public byte getPacketId()
	{
		return 7;
	}
	
}
