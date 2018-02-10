package de.jcm.home3d;

import java.io.File;
import java.util.ArrayList;

import de.jcm.home3d.packet.out.PacketOutEncryption;
import de.jcm.home3d.packet.out.PacketOutRequestFile;
import de.jcm.home3d.packet.out.PacketOutStatusUpdate;
import de.jcm.home3d.task.Task;
import de.jcm.util.Callback;

public class MainThread extends Thread
{
	@Override
	public void run()
	{
		Home3D.sendPacket(new PacketOutEncryption(Home3D.myKeys.getPublicKey()));
		Home3D.encrypted = true;
		
		synchronized(Home3D.notifier)
		{
			try
			{
				Home3D.notifier.wait();
			}
			catch(InterruptedException e1)
			{
				e1.printStackTrace();
			}
		}
		System.out.println("Main loop started");
		
		
		if(!(new File("slic3r.ini")).exists())
		{
			PacketOutRequestFile packet = new PacketOutRequestFile("slic3r.ini", new Callback<Void, File>()
			{
				@Override
				public Void call(File argument)
				{
					return null;
				}
			});
			Home3D.sendPacket(packet);
		}
		
		while(true)
		{
			synchronized(Home3D.tasks)
			{
				ArrayList<Task> tasks2=new ArrayList<>();
				for(Task task : Home3D.tasks.values())
				{
					if(task.hasChanged())
					{
						System.out.println("Task \""+task.getName()+"\" with id "+task.getTaskId()+" changed!");
						tasks2.add(task);
						task.setChanged(false);
					}
				}
				
				Home3D.sendPacket(new PacketOutStatusUpdate(tasks2));
			}
			
			try
			{
				Thread.sleep(1000*10);
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}
}
