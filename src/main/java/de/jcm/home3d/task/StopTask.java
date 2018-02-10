package de.jcm.home3d.task;

import java.io.IOException;

import de.jcm.home3d.Home3D;

public class StopTask extends Task
{
	
	public StopTask(int id, String name, String argument)
	{
		super(id, name, argument);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void run()
	{
		for(Task task : Home3D.tasks.values())
		{
			if(task!=this)
			{
				task.stop();
			}
		}
		
		try
		{
			Runtime.getRuntime().exec("kill -10 " + Home3D.pid);
			Home3D.fifo.write(("S"+'\0').getBytes());
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
}
