package de.jcm.home3d.packet.out;

import java.io.DataOutputStream;

public class PacketOutLogin extends PacketOut
{
	private String username;
	private String password;
	private int printerId;
	
	public PacketOutLogin(String username, String password, int printerId)
	{
		this.username = username;
		this.password = password;
		this.printerId = printerId;
	}
	
	@Override
	public void write(DataOutputStream out)
	{
		try
		{
			out.writeInt(username.length());
			out.write(username.getBytes());
			
			out.writeInt(password.length());
			out.write(password.getBytes());
			
			out.writeInt(printerId);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public byte getPacketId()
	{
		return 2;
	}
	
}
