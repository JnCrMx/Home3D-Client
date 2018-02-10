package de.jcm.home3d.packet.out;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import de.jcm.home3d.task.Task;

public class PacketOutStatusUpdate extends PacketOut
{
	private List<Task> tasks;
	
	public PacketOutStatusUpdate(List<Task> tasks)
	{
		this.tasks = tasks;
	}
	
	@Override
	public void write(DataOutputStream out)
	{
		try
		{
			out.writeInt(tasks.size());
			for(int i = 0; i < tasks.size(); i++)
			{
				out.writeInt(tasks.get(i).getTaskId());
				
				out.writeInt(tasks.get(i).getCode());
				
				String status = tasks.get(i).getStatus();
				out.writeInt(status.length());
				out.write(status.getBytes());
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public byte getPacketId()
	{
		return 4;
	}
	
}
