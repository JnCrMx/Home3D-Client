package de.jcm.home3d.packet.in;

import java.io.DataInputStream;
import java.io.IOException;

import de.jcm.home3d.packet.out.PacketOut;

public class PacketInDisconnected extends PacketIn<PacketOut>
{
	
	private String reason;
	
	@Override
	public void read(DataInputStream in)
	{
		try
		{
			int len = in.readInt();
			byte[] bytes = new byte[len];
			in.read(bytes);
			reason = new String(bytes);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public PacketOut handle()
	{
		System.out.println("Disconnected: " + reason);
		
		System.exit(0);
		
		return null;
	}
	
	@Override
	public byte getPacketId()
	{
		return 3;
	}
	
}
