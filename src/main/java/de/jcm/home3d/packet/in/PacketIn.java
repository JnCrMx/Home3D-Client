package de.jcm.home3d.packet.in;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;

import de.jcm.home3d.packet.out.PacketOut;

public abstract class PacketIn<R extends PacketOut>
{
	/**
	 * Use this constructor before PacketIn.read();
	 */
	public PacketIn()
	{
	}
	
	private static HashMap<Byte, Class<? extends PacketIn<?>>> packets = new HashMap<>();
	
	/**
	 * Do NOT read packet id from in
	 * 
	 * @param in
	 */
	public abstract void read(DataInputStream in);
	
	public abstract R handle();
	
	public abstract byte getPacketId();
	
	public static void register(Class<? extends PacketIn<?>> clazz)
	{
		try
		{
			PacketIn<?> instance = clazz.newInstance();
			
			packets.put(instance.getPacketId(), clazz);
		}
		catch(InstantiationException e)
		{
			e.printStackTrace();
		}
		catch(IllegalAccessException e)
		{
			e.printStackTrace();
		}
	}
	
	public static Class<? extends PacketIn<?>> findPacket(byte id)
	{
		return packets.get(id);
	}
	
	public static PacketOut handle(byte[] packetData)
	{
		ByteArrayInputStream array = new ByteArrayInputStream(packetData);
		DataInputStream in = new DataInputStream(array);
		try
		{
			byte id = in.readByte();
			Class<? extends PacketIn<?>> clazz = findPacket(id);
			
			if(clazz != null)
			{								
				PacketIn<?> instance = clazz.newInstance();
				instance.read(in);
				return instance.handle();
			}
			else
			{
				System.out.println("Unknown packet: " + id);
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		catch(InstantiationException e)
		{
			e.printStackTrace();
		}
		catch(IllegalAccessException e)
		{
			e.printStackTrace();
		}
		return null;
	}
}
