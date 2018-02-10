package de.jcm.home3d.packet.in;

import java.io.DataInputStream;
import java.io.IOException;

import de.jcm.home3d.Home3D;
import de.jcm.home3d.packet.out.PacketOut;

public class PacketInLoginSuccess extends PacketIn<PacketOut>
{
	
	private int userId;
	
	@Override
	public void read(DataInputStream in)
	{
		try
		{
			this.userId = in.readInt();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public PacketOut handle()
	{
		Home3D.userId = this.userId;
		
		System.out.println("Login successfully! User id is " + userId);
		
		return null;
	}
	
	@Override
	public byte getPacketId()
	{
		return 2;
	}
	
}
