package de.jcm.home3d.packet.out;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

import de.jcm.home3d.Home3D;
import de.jcm.util.Callback;

public class PacketOutRequestFile extends PacketOut
{
	private String fileName;
	
	public PacketOutRequestFile(String fileName, Callback<Void, File> fileCallback)
	{
		this.fileName = fileName;
		Home3D.fileName = fileName;
		Home3D.fileCallback = fileCallback;
		
		System.out.println("Requesting file "+fileName+"...");
	}
	
	@Override
	public void write(DataOutputStream out)
	{
		try
		{
			out.writeInt(fileName.length());
			out.write(fileName.getBytes());
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public byte getPacketId()
	{
		return 6;
	}
	
}
