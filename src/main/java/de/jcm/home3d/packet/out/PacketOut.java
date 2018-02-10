package de.jcm.home3d.packet.out;

import java.io.DataOutputStream;

public abstract class PacketOut
{
	/**
	 * Do NOT write packet id to out
	 * 
	 * @param out
	 */
	public abstract void write(DataOutputStream out);
	
	public abstract byte getPacketId();
}
