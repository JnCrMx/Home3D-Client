package de.jcm.home3d.packet.in;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.jcm.home3d.Home3D;
import de.jcm.home3d.packet.out.PacketOut;
import de.jcm.home3d.task.ConfigTask;
import de.jcm.home3d.task.PrintTask;
import de.jcm.home3d.task.Task;

public class PacketInTaskUpdate extends PacketIn<PacketOut>
{
	private List<Task> tasks;
	
	@Override
	public void read(DataInputStream in)
	{
		try
		{
			int len = in.readInt();
			tasks=new ArrayList<>();
			
			for(int i=0;i<len;i++)
			{
				int id=in.readInt();
				String name=readString(in);
				int type=in.readInt();
				String argument=readString(in);
				int code=in.readInt();
				String status=readString(in);
				
				Task task=null;
				if(type==1)
					task=new ConfigTask(id, name, argument);
				else if(type==2)
					task=new PrintTask(id, name, argument);
				
				if(task!=null)
				{
					task.setCode(code);
					task.setStatus(status);
					task.setChanged(false);
					
					tasks.add(task);
				}
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private String readString(DataInputStream in) throws IOException
	{
		int len=in.readInt();
		byte[] bytes=new byte[len];
		in.read(bytes);
		return new String(bytes);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public PacketOut handle()
	{
		for(Task task : tasks)
		{
			int id=task.getTaskId();
			if(Home3D.tasks.containsKey(id))
			{
				if(!Home3D.tasks.get(id).isAlive())
				{
					Home3D.tasks.replace(id, task);
				}
				else if(task.getCode()==5)
				{
					Home3D.tasks.get(id).stop();
					Home3D.tasks.replace(id, task);
				}
			}
			else
			{
				Home3D.tasks.put(id, task);
			}
		}
		
		return null;
	}
	
	@Override
	public byte getPacketId()
	{
		return 5;
	}
	
}
