package de.jcm.home3d.task;

import java.io.IOException;
import java.nio.ByteBuffer;

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
		super.run();
		
		for(Task task : Home3D.tasks.values())
		{
			if(task!=this && task.getCode()==2)
			{
				task.stop();		//stop thread
				task.setCode(5);	//set status code to stopped
			}
		}
		
		try
		{
			Home3D.fifo.setLength(0);	//clear fifo
			
			Runtime.getRuntime().exec("kill -10 " + Home3D.pid);	//interrupt daemon
			
			ByteBuffer buffer=ByteBuffer.allocate(4096 + 1);	// PATH_MAX + ACTION_MAX
			buffer.put((byte) 'S');				//action
			buffer.put((byte) 0);				//null terminated string (arg)
			
			Home3D.fifo.write(buffer.array());	//write action buffer
			
			setCode(3);
			setStatus("stop done");
		}
		catch(IOException e)
		{
			e.printStackTrace();
			setCode(4);
			setStatus(e.getMessage());
		}
	}
}
