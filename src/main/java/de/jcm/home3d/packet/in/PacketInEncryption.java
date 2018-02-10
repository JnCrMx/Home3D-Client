package de.jcm.home3d.packet.in;

import java.io.DataInputStream;
import java.math.BigInteger;

import de.jcm.home3d.Home3D;
import de.jcm.home3d.packet.out.PacketOutLogin;
import de.jcm.security.rsa.RSAPublicKey;

public class PacketInEncryption extends PacketIn<PacketOutLogin>
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
	public PacketOutLogin handle()
	{
		Home3D.startEncryption(key);
		return new PacketOutLogin(Home3D.username, Home3D.password, Home3D.printerId);
	}
	
	@Override
	public byte getPacketId()
	{
		return 1;
	}
	
}
