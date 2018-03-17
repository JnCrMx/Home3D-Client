package de.jcm.home3d.packet.in;

import java.io.DataInputStream;
import java.math.BigInteger;

import de.jcm.home3d.Home3D;
import de.jcm.home3d.packet.out.PacketOut;
import de.jcm.home3d.packet.out.PacketOutEncryption;
import de.jcm.security.rsa.RSAPublicKey;

public class PacketInEncryption extends PacketIn<PacketOut>
{
	private RSAPublicKey key;
	
	public PacketInEncryption()
	{
	}
	
	@Override
	public void read(DataInputStream in)
	{
		try
		{
			int exponentLength = in.readInt();
			byte[] exponentBytes = new byte[exponentLength];
			in.read(exponentBytes);
			BigInteger exponent = new BigInteger(exponentBytes);
			
			int moduloLength = in.readInt();
			byte[] moduloBytes = new byte[moduloLength];
			in.read(moduloBytes);
			BigInteger modulo = new BigInteger(moduloBytes);
			
			key = new RSAPublicKey(modulo, exponent);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public PacketOutEncryption handle()
	{
		Home3D.startEncryption(key);
		return new PacketOutEncryption(Home3D.myKeyBytes);
	}
	
	@Override
	public byte getPacketId()
	{
		return 1;
	}
	
}
