package de.jcm.home3d.task;

import java.io.File;

import de.jcm.home3d.Home3D;
import de.jcm.home3d.packet.out.PacketOutRequestFile;
import de.jcm.util.Callback;

public class ConfigTask extends Task
{
	
	public ConfigTask(int id, String name, String argument)
	{
		super(id, name, argument);
	}
	
	@Override
	public void run()
	{
		if(getArgument().equals("updateSlic3r"))
		{
			setStatus("updating slic3r");
			PacketOutRequestFile packet = new PacketOutRequestFile("slic3r.ini", new Callback<Void, File>()
			{
				
				@Override
				public Void call(File argument)
				{
					setCode(3);
					setStatus("updating slic3r done");
					return null;
				}
			});
			Home3D.sendPacket(packet);
		}
		else
		{
			setStatus("updating config");
			String[] options=getArgument().split(";");
			for(String string : options)
			{
				String key=string.split("=")[0];
				String value=string.split("=")[1];
				
				switch(key)
				{
					case "username":
						Home3D.username=value;
						break;
					case "password":
						Home3D.password=value;
						break;
					case "printerId":
						Home3D.printerId=Integer.parseInt(value);
						break;
					case "printerModel":
						Home3D.printerModel=value;
						break;
					case "printerPort":
						Home3D.printerPort=value;
						break;
					
					default:
						break;
				}
				Home3D.writeConfig();
			}
			
			setCode(3);
			setStatus("updating config done");
		}
	}
	
}
