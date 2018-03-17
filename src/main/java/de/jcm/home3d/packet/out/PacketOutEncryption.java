package de.jcm.home3d.packet.out;

import java.io.DataOutputStream;

public class PacketOutEncryption extends PacketOut
{
	byte[] key;
	
	public PacketOutEncryption(byte[] key)
	{
		this.key = key;
	}
	
	@Override
	public void write(DataOutputStream out)
	{
		try
		{
			out.writeInt(key.length);
			out.write(key);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public byte getPacketId()
	{
		return 1;
	}
	
}
