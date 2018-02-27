package de.jcm.home3d.task;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
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
				File f2=new File(Home3D.stlDir, file.getName());
				file.renameTo(f2);
				file=f2;
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
							
							file=new File(Home3D.stlDir, entry.getName());
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
						update();
						
						File output = new File(Home3D.gcodeDir, file.getName().replace(' ', '_')+".gcode.tmp");
						
						System.out.println(file.getAbsolutePath()+" -> "+output.getAbsolutePath());
						
						Process process=Runtime.getRuntime().exec("slic3r --load " + "slic3r.ini" + " -o " + output.getAbsolutePath() + " -- " + file.getAbsolutePath());
						InputStream in=process.getInputStream();
						
						int r;
						while((r=in.read())!=-1)
							System.out.write(r);
						
						int exit=process.waitFor();
						if(output.exists() && exit==0)
						{
							File gcode = new File(Home3D.gcodeDir, file.getName().replace(' ', '_')+".gcode");
							
							process=Runtime.getRuntime().exec("./strip_gcode -o "+gcode.getAbsolutePath()+" "+output.getAbsolutePath());
							exit=process.waitFor();
							if(gcode.exists() && exit==0)
							{
								output.delete();
								
								FileOutputStream fout=new FileOutputStream(gcode, true);
								fout.write("M140 S0\n".getBytes());
								fout.close();
								
								setStatus("Printing...");
								update();
								
								Runtime.getRuntime().exec("kill -10 " + Home3D.pid);
								
								ByteBuffer buffer=ByteBuffer.allocate(4096 + 1);
								buffer.put((byte) 'P');
								buffer.put(gcode.getAbsolutePath().getBytes());
								buffer.put((byte) 0);
								
								Home3D.fifo.write(buffer.array());
								
								sleep(1000);
								
								while(true)
								{
									String line=Home3D.fifo.readLine();
									if(line.matches("[0-9]* [0-9]*"))
									{
										int step=Integer.parseInt(line.split(" ")[0]);
										int max=Integer.parseInt(line.split(" ")[1]);
										
										if(step==max)
										{
											setCode(3);
											setStatus("Print task done!");
											update();
											break;
										}
										
										setStatus("Printing... "+step+"/"+max);
										//update();
									}
									else
									{
										setCode(4);
										setStatus("Error: "+line);
										update();
										break;
									}
								}
							}
							else
							{
								setCode(4);
								setStatus("strip_gcode returned exit code "+exit+"!");
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
				update();
				return null;
			}
		});
		Home3D.sendPacket(packet);
	}
	
}
