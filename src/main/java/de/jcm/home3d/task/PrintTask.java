package de.jcm.home3d.task;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import de.jcm.home3d.Home3D;
import de.jcm.home3d.packet.out.PacketOutRequestFile;
import de.jcm.home3d.packet.out.PacketOutStatusUpdate;
import de.jcm.util.Callback;

public class PrintTask extends Task
{
	
	public PrintTask(int id, String name, String argument)
	{
		super(id, name, argument);
	}
	
	@Override
	public void run()
	{
		setStatus("Downloading...");
		PacketOutRequestFile packet = new PacketOutRequestFile(getArgument(), new Callback<Void, File>()
		{
			@Override
			public Void call(File argument)
			{
				File file=argument;
				if(file.getName().endsWith(".zip"))
				{
					ZipFile zip;
					try
					{
						zip = new ZipFile(file);
						Enumeration<? extends ZipEntry> entries=zip.entries();
						if(entries.hasMoreElements())
						{
							ZipEntry entry=entries.nextElement();
							InputStream in=zip.getInputStream(entry);
							
							file=new File(entry.getName());
							FileOutputStream fout=new FileOutputStream(file);
							
							byte[] bytes=new byte[(int) entry.getSize()];
							in.read(bytes);
							fout.write(bytes);
							
							in.close();
							fout.close();
						}
						else
						{
							setCode(4);
							setStatus("Zip-File empty");
							
							ArrayList<Task> list=new ArrayList<>();
							list.add(PrintTask.this);
							Home3D.sendPacket(new PacketOutStatusUpdate(list));
							
							return null;
						}
					}
					catch(ZipException e)
					{
						e.printStackTrace();
					}
					catch(IOException e)
					{
						e.printStackTrace();
					}
				}
				if(Home3D.pid>0)
				{
					try
					{
						setStatus("Slicing...");
						
						ArrayList<Task> list=new ArrayList<>();
						list.add(PrintTask.this);
						Home3D.sendPacket(new PacketOutStatusUpdate(list));
						
						File output = new File(file.getName()+".gcode");
						
						System.out.println(file+" -> "+output);
						
						Process proc=Runtime.getRuntime().exec("slic3r --load " + "slic3r.ini" + " -o " + output.getAbsolutePath() + " -- " + file.getAbsolutePath());
						InputStream in=proc.getInputStream();
						
						int r;
						while((r=in.read())!=-1)
							System.out.write(r);
						
						int exit=proc.waitFor();
						if(output.exists() && exit==0)
						{
							setStatus("Printing...");
							
							list=new ArrayList<>();
							list.add(PrintTask.this);
							Home3D.sendPacket(new PacketOutStatusUpdate(list));
							
							Runtime.getRuntime().exec("kill -10 " + Home3D.pid);
							Home3D.fifo.write(("P" + output.getAbsolutePath() + '\0').getBytes());
							
							sleep(1000);
							
							while(true)
							{
								String line=Home3D.fifo.readLine();
								if(line.matches("[0-9]* [0-9]*"))
								{
									int step=Integer.parseInt(line.split(" ")[0]);
									int max=Integer.parseInt(line.split(" ")[1]);
									
									setStatus("Printing... "+step+"/"+max);
								}
								else
								{
									setCode(4);
									setStatus("Error: "+line);
									break;
								}
							}
						}
						else
						{
							setCode(4);
							setStatus("Slic3r returned exit code "+exit+"!");
						}
					}
					catch(IOException e)
					{
						e.printStackTrace();
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				}
				else
				{						
					setStatus("Cannot access print3d");
					setCode(4);
					
					ArrayList<Task> list=new ArrayList<>();
					list.add(PrintTask.this);
					Home3D.sendPacket(new PacketOutStatusUpdate(list));
						
				}
				return null;
			}
		});
		Home3D.sendPacket(packet);
	}
	
}
