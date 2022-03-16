import java.awt.GridLayout;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * This is ImagePeer class
 * It creates a peer server for uploading image
 * @author Tai Zhe Hui
 */
public class ImagePeer 
{
    private JFrame frame = new JFrame("ImagePeer");
    private JPanel panel=new JPanel();
    private ServerSocket ss=null;
	private Socket socket;
	private	 int port=8000;
	private	static final int PORT =8000;
	private JLabel[][] block = new JLabel[20][20];
	private HashSet<Peer> Peer_list=new HashSet<Peer>();
	private static final String IP_ADDRESS="localhost";
	private String username;
	private String image_filename;
	
	public static void main(String[] args) throws Exception 
	{        
		ImagePeer	peer=new	ImagePeer();	
    	peer.start();
	}
	
	private void start() throws Exception
	{
		startUploadServer(); //get own port
		Thread.sleep(500);
		connectToServer(); 
		sendLoginRequest(); //exchange port, get peer list
		startDownloader(); //get image
		makeFrame();
		
	}
	
	private void connectToServer() throws IOException
	{
		try
		{
			socket= new Socket(IP_ADDRESS, PORT);
		}
		catch (UnknownHostException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	private void sendLoginRequest() throws Exception
	{
		ObjectOutputStream out;
		ObjectInputStream in;

		try 
		{
			out=new ObjectOutputStream(socket.getOutputStream());
			in=new ObjectInputStream(socket.getInputStream());
			
			Message exchangePort =new Message();
			int[] portarr=new int[1];
			portarr[0]=port;
			exchangePort.setCommand("EX_PORT");
			exchangePort.setData_block_no(portarr);
			out.writeObject(exchangePort);
			
			Thread.sleep(1000);
			
			Message login=new Message();
			username=getName();
			login.setMe(username);
			login.setYou("Teacher");
			login.setCommand("LOGIN");
			login.setData_image_name("NULL");
			login.setData_block_no(null);
			login.setData_content(null/*getPassword()*/);
			String password=getPassword();
			if(password==null)
			{
				loginFail();
				socket.close();
				frame.setVisible(false);
				System.exit(0);
			}
			else
			{
				out.reset();
				out.writeObject(login);
				
				Message respond = (Message)in.readObject();
				if(respond.getCommand().equals("LOGIN_OK"))    //shld update to peer
				{
					image_filename=respond.getData_image_name();
					System.out.println("LOGIN OK");
					synchronized(Peer_list)
					{
						Peer_list=respond.getPeer_list();
					}
				}
				if(respond.getCommand().equals("LOGIN_FAILED"))
				{
					loginFail();
					socket.close();
					frame.setVisible(false);
					System.exit(0);
				}	
			}
		} 
		catch (UnknownHostException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	private void startUploadServer() throws Exception
	{
		Thread t=new Thread(new UploaderServer()); 
		t.start();
	}
	
	private void startDownloader() throws Exception
	{
		Thread t2=new Thread(new Downloader());
		t2.start();
	}

	private class Downloader implements Runnable
	{
		public void run()
		{
			for(Peer p:Peer_list) //initialize
			{
				if(p.getUsername().equals(username))
				{
					for(int i=0;i<20;++i)
					{
						for(int j=0;j<20;++j)
						{
							p.setimage_block_list("n", i, j);
						}
					}
				}
			}
			while(true)
			{
				int count=0;
				for(Peer p:Peer_list) //fully filled
				{
					if(p.getUsername().equals(username))
					{
						for(int i=0;i<20;++i)
						{
							for(int j=0;j<20;++j)
							{
								if(p.getimage_block_list(i, j).equals("f"))
								{
									count++;
								}
							}
						}
					}
				}
				if(count==401)
				{
					break;
				}
				for(Peer p:Peer_list)
				{

					if(p.getStatus().equals("free"))
					{
						p.setStatus("busy");
						//System.out.println(p.getUsername()+" "+p.getportNumber());/////////////////////////////////////////////
						try 
						{
							Socket socket=new Socket(IP_ADDRESS,p.getportNumber());
							ObjectOutputStream out=new ObjectOutputStream(socket.getOutputStream());
							ObjectInputStream in=new ObjectInputStream(socket.getInputStream());
							
							Thread receiveImage;
							receiveImage = new Thread(new DownloaderHandler(socket,out,in,"Teacher"));
							receiveImage.start();
						} 
						catch (Exception e) 
						{
							e.printStackTrace();
						}
						
					}
				}
			    try 
				{
					Thread.sleep(100);
					
				} 
				catch (InterruptedException e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//System.out.println("---------------");//////////////////////////////////////////k
				
			}
		}
	}

	private class DownloaderHandler implements Runnable
	{
		Socket sock;
		ObjectOutputStream out;
		ObjectInputStream in;
		String you;

		private DownloaderHandler(Socket sock,ObjectOutputStream out,ObjectInputStream in,String username) throws Exception
		{
			this.sock=sock;
			this.in=in;
			this.out=out;
			you=username;
		}
		public void run()
		{
			try
			{
				for(Peer p:Peer_list)
				{
					if(p.getUsername().equals(username))
					{
						int i,j;
						int[] rand=new int[2];
						while(true)
						{
							i=(new Random()).nextInt(20);
			        		j=(new Random()).nextInt(20);
			        		if(p.getimage_block_list(i, j).equals("n"))
			        		{
			        			p.setimage_block_list("w", i, j);
			        			rand[0]=i;
			        			rand[1]=j;
			        			break;
			        		}
						}
						Message getImageBlock = new Message();
			        	getImageBlock.setMe(username);
			        	getImageBlock.setYou(you);
			        	getImageBlock.setCommand("GET_IMG_BLOCK");
			        	getImageBlock.setData_image_name(image_filename);
			        	getImageBlock.setData_block_no(rand);
			        	getImageBlock.setData_content(null);
			        	out.writeObject(getImageBlock);
			        	
			        	Message respond= (Message)in.readObject();
			        	if(respond.getCommand().equals("SEND_IMG_BLOCK"))
			        	{
			        		 ImageIcon ic=respond.getData_content();
			        		 int x=respond.getData_block_no()[0];
			        		 int y=respond.getData_block_no()[1];
			        		 block[y][x].setIcon(ic);
			        		 block[y][x].revalidate();
			        		 
			        		 for(Peer p2:Peer_list)
			        		 {
			        			 if(p2.getUsername().equals(username))
			        			 {
			        				 p2.setimage_block_list("f", x, y);
			        				 p2.setimage_icon_list(ic, x, y);
			        			 }
			        		 }
			        	} 
			        	else
			        	{
			        		for(Peer p2:Peer_list)
			        		{
			        			 if(p2.getUsername().equals(username))
			        			 {
			        				 p2.setimage_block_list("n", i, j);
			        			 }
			        		}
			        	}
			        	for(Peer p3:Peer_list)
		        		{
		        			 if(p3.getUsername().equals(respond.getMe()))
		        			 {
		        				 p3.setStatus("free");
		        			 }
		        		}
			        	
					}
				}
			}
			catch(IOException e)
			{
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private class UploaderServer implements Runnable
	{
		public void run()
		{
			ServerSocket uss=null;
			while(uss==null && port<65536)
			{
				try
				{
					uss=new ServerSocket(port);
				}
				catch(IOException e)
				{
					port++;
				}
			}
			System.out.println("PEER UPLOADER PORT: " + port);
			while(true)
	        {
		        Socket clientSocket;
				try 
				{
					clientSocket = uss.accept();
					Thread t=new Thread(new UploaderServerHandler(clientSocket)); 
					t.start();
				} 
				catch (IOException e) 
				{
					e.printStackTrace();
				}
				

	        } 
		}       
	}
	
	private class UploaderServerHandler implements Runnable
	{
		private Socket sock;
		private ObjectInputStream in;
	    private ObjectOutputStream out;
	    private Message request,respond;
		
		private UploaderServerHandler(Socket clientSocket) throws IOException
		{
			this.sock=clientSocket;
			this.out=new ObjectOutputStream(sock.getOutputStream());
			this.in=new ObjectInputStream(sock.getInputStream());
			
		}
		public void run() 
		{
			try 
			{
				request=(Message)in.readObject();
				respond = new Message();
				int[] d=request.getData_block_no();
				respond.setMe(username);
				respond.setYou(request.getMe());
				for(Peer p:Peer_list)
				{
					if(p.getUsername().equals(username))
					{
						if(request.getCommand().equals("GET_IMG_BLOCK") &&
							p.getimage_block_list(d[0], d[1]).equals("f"))
						{
								respond.setCommand("SEND_IMG_BLOCK");
								respond.setData_image_name(image_filename);
								respond.setData_block_no(request.getData_block_no());
								ImageIcon ic =p.getimage_icon_list(d[0],d[1]);
								respond.setData_content(ic);
								respond.setPeer_list(Peer_list);
						}
						/*else if(request.getCommand().equals("ACTIVE_CHECK")) //////////////new add
						{
							respond.setCommand("ACTIVE_CHECK_OK");
						}*/
						else if(request.getCommand().equals("IMAGE_UPDATE"))
						{
							System.out.println("UPDATE IMAGE");
							respond.setCommand("IMAGE_UPDATE_OK");
							p.setimage_filename(respond.getData_image_name());
							tellUpdateImage();
							Thread update =new Thread (new updateImage());
							update.start();
						}
						else if (!p.getimage_block_list(d[0],d[1]).equals("f"))
						{
							respond.setCommand("BLOCK_NOT_AVAILABLE");
						}
						else if (!request.getCommand().equals("GET_IMG_BLOCK"))
						{
							respond.setCommand("NAME_MISMATCH");
						}
						out.writeObject(respond);
					}
				}
			}
			catch (Exception e) 
			{
				e.printStackTrace();
			}
		}
	}

	private void makeFrame()
	{
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		panel.setLayout(new GridLayout(20,20,0,0));
		frame.add(panel);
    	frame.setSize(630, 650);
    	frame.setVisible(true);
    	frame.setLocationRelativeTo(null);
    	for (int i=0;i<20;++i)
		{
			for(int j=0;j<20;++j)
			{
				block[i][j]=new JLabel();
				panel.add(block[i][j]);
			}
		}
	}
	private class updateImage implements Runnable
	{
		public void run() 
		{
			Socket socket;
			try {
				socket = new Socket(IP_ADDRESS,PORT);
				ObjectOutputStream out=new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream in=new ObjectInputStream(socket.getInputStream());
				for(Peer p:Peer_list)
				{
					if(p.getUsername().equals(username))
					{
						for(int i=0;i<20;++i)
						{
							for(int j=0;j<20;++j)
							{
								p.setimage_block_list("n", i, j);
							}
						}
					}
				}
						
				Thread receiveImage;
				receiveImage = new Thread(new DownloaderHandler(socket,out,in,"Teacher"));
				receiveImage.start();
			} 
			catch (IOException e)
			{
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
		
	}
	
	private String getName() {
    	return	JOptionPane.showInputDialog( // GUI for input username
				frame,	
				"Username:",	
				"Input",	
				JOptionPane.QUESTION_MESSAGE);	
        
    }
    private String getPassword() {
    	return	JOptionPane.showInputDialog( // GUI for input IP
				frame,	
				"Password:",	
				"Input",	
				JOptionPane.QUESTION_MESSAGE);	
        
    }
    private void loginFail() {
    			JOptionPane.showMessageDialog( // GUI for input IP
				frame,	
				"Login Fail:",	
				"Message",	
				JOptionPane.INFORMATION_MESSAGE);
    }
    private void tellUpdateImage(){
    	JOptionPane.showMessageDialog( // GUI for input IP
				frame,	
				"Update Image:",	
				"Message",	
				JOptionPane.INFORMATION_MESSAGE);
    }
}
	
