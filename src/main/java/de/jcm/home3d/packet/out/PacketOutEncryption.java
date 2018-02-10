package de.jcm.home3d.packet.out;

import java.io.DataOutputStream;

import de.jcm.security.rsa.RSAPublicKey;

public class PacketOutEncryption extends PacketOut
{
	private RSAPublicKey key;
	
	public PacketOutEncryption(RSAPublicKey key)
	{
		this.key = key;
	}
	
	@Override
	public void write(DataOutputStream out)
	{
		try
		{
			byte[] exponent = key.getExponent().toByteArray();
			byte[] modulo = key.getModulo().toByteArray();
			
			out.writeInt(exponent.length);
			out.write(exponent);
			out.writeInt(modulo.length);
			out.write(modulo);
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
