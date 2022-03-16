import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * This is ImageServer Class
 * It creates a server for peer to login
 * It also create a peer server for uploading image
 * @author Tai Zhe Hui
 */
public class ImageServer 
{
	private File file;
	private BufferedImage bi;
	private Image image;
	private String image_filename;
	private JFrame frame=new JFrame("Image Server");
	private JPanel panel=new JPanel();
	private JLabel label=new JLabel();
	private JButton button=new JButton("Load another image");
	private HashSet<Peer> Peer_list=new HashSet<Peer>();
	private ServerSocket serverSocket;
	private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
	private ImageIcon[][] image_icon_list=new ImageIcon[20][20];

	private int port=8000;
	private	static final int PORT =8000;
	private static final int MAX_PEER_RETURN=5;
	private static final int NO_IMAGE_BLOCK=400;
	private static final String IP_ADDRESS="127.0.0.1";
	
	public static void main(String[] args) throws Exception 
	{
		ImageServer imageServer = new ImageServer();
		imageServer.start();
	}
	
	private void start() throws Exception
	{
		chooseImage();
		displayImage();
		createServer();
		//createFirstPeer();
		activeCheck();
	}
	
	private void createServer() throws InterruptedException
	{
		Thread t=new Thread(new Server()); 
		t.start();
		
		Thread.sleep(500);
		
		Thread t2=new Thread(new FirstPeerServer());
		t2.start();
		
	}

	private class Server implements Runnable{
		public void run()
		{
			try 
			{
				System.out.println("SERVER PORT: " + PORT);
				serverSocket= new	ServerSocket(PORT);
		        while(true)
		        {
			        Socket clientSocket=serverSocket.accept();
			        createFirstPeer();
					Thread t=new Thread(new LoginHandler(clientSocket)); 
					t.start();
					
		        } 
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}	
		}       
	}

	private class LoginHandler implements Runnable
	{
		private Socket sock;
		private ObjectInputStream in;
	    private ObjectOutputStream out;
	    private Message exchangePort, request,respond;
	    private int portNumber;
		
		private LoginHandler(Socket clientSocket) throws IOException
		{
			this.sock=clientSocket;
			this.out=new ObjectOutputStream(sock.getOutputStream());
			this.in=new ObjectInputStream(sock.getInputStream());
			
		}
		public void run()
		{
			try 
			{
				exchangePort=(Message) in.readObject();
				
				if(exchangePort.getCommand().equals("EX_PORT"))
				{
					portNumber=exchangePort.getData_block_no()[0];
					Thread.sleep(1500);
					request=(Message)in.readObject();
					respond = new Message();
					
					if(request.getCommand().equals("LOGIN") && request.getMe()!=null && 
						request.getYou().equals("Teacher") && !request.getMe().equals("") )
					{		
						Peer Peer = new Peer();
						Peer.setStatus("free");
						Peer.setUsername(request.getMe());
						Peer.setlast_login_time(sdf.format( Calendar.getInstance().getTime()));
						Peer.setIP_address(IP_ADDRESS);
						Peer.setportNumber(portNumber);
						synchronized(Peer_list)
						{
							Peer_list.add(Peer);
						}
						
						respond.setMe("Teacher");
						respond.setYou(request.getMe());
						respond.setCommand("LOGIN_OK");
						respond.setData_image_name(file.getName());
						respond.setData_block_no(null);
						respond.setData_content(null);
						respond.setPeer_list(Peer_list);
						
						out.writeObject(respond);
					}	
					else
					{
						respond.setMe("Teacher");
						respond.setCommand("LOGIN_FAILED");
						out.writeObject(respond);
					}
				}
				
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}       
	}

	private class FirstPeerServer implements Runnable
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
			System.out.println("UPLOADER PORT: " + port);
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
				int[] block=request.getData_block_no();
				respond.setMe("Teacher");
				respond.setYou(request.getMe());
				for(Peer p:Peer_list)
				{
					if(p.getUsername().equals("Teacher"))
					{
						if(request.getCommand().equals("GET_IMG_BLOCK"))
						{
							
							respond.setCommand("SEND_IMG_BLOCK");
							respond.setData_image_name(image_filename);
							respond.setData_block_no(request.getData_block_no());
							ImageIcon ic =image_icon_list[block[0]][block[1]];
							respond.setData_content(ic);
							//synchronized(Peer_list)
							{
								respond.setPeer_list(Peer_list);
							}
							out.writeObject(respond);
							break;
						}
						/*else if (p.getimage_block_list(block[0],block[1])!="f")
						{
							respond.setCommand("BLOCK_NOT_AVAILABLE");
							break;
						}*/
						else if (!request.getCommand().equals("GET_IMG_BLOCK"))
						{
							respond.setCommand("NAME_MISMATCH");
							break;
						}
						out.writeObject(respond);
					}
					
				}
			}
			catch (ClassNotFoundException e) 
			{
				e.printStackTrace();
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

	private void createFirstPeer()
	{
		Peer serverPeer = new Peer();
		serverPeer.setStatus("free");
		serverPeer.setUsername("Teacher");
		serverPeer.setlast_login_time(sdf.format( Calendar.getInstance().getTime()));
		serverPeer.setIP_address(IP_ADDRESS);
		serverPeer.setportNumber(port);
		serverPeer.setimage_filename(image_filename);
		for(int i=0;i<20;++i)
		{
			for(int j=0;j<20;++j)
			{
				BufferedImage bi2=bi.getSubimage(i*30,j*30,30,30);
				ImageIcon ic=new ImageIcon(bi2);
				serverPeer.setimage_icon_list(ic, i, j);
				serverPeer.setimage_block_list("f", i, j);
			}
		}
		synchronized(Peer_list)
		{
			synchronized(Peer_list)
			{
				Peer_list.remove(serverPeer);
				Peer_list.add(serverPeer);
			}			
		}
	}
	
	private void chooseImage()
	{
		JFileChooser fc = new JFileChooser();
        int result = fc.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) 
        {
            file = fc.getSelectedFile();
            try 
            {
            	image=ImageIO.read(file).getScaledInstance(600,600,Image.SCALE_FAST);
                if(fc.getSelectedFile()==null)
                {
                	System.exit(0);
                }
                image_filename=file.getName();
                bi = createImage();
                for(int i=0;i<20;++i)
        		{
        			for(int j=0;j<20;++j)
        			{
        				BufferedImage bi2=bi.getSubimage(i*30,j*30,30,30);
        				ImageIcon ic=new ImageIcon(bi2);
        				image_icon_list[i][j]=ic;
        			}
        		}
                label.setIcon(new ImageIcon(image));
                label.repaint();
            } 
            catch (IOException e) 
            {
            		e.printStackTrace();
            }
            catch(NullPointerException e)
            {
            	if(image==null)
            	{
            		System.exit(0);            	
            	}
            	else
            	{
            		frame.setVisible(true);
            	}
            }
        }
        if(result == JFileChooser.CANCEL_OPTION)
        {
        	System.exit(0);
        }
	}
	
	private void displayImage()
	{
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		panel.add(label);
		frame.add(panel);
		button.addActionListener(new ButtonListener());
		frame.add(BorderLayout.SOUTH,button);
		frame.setSize(630, 680);
		frame.setVisible(true);
		frame.setLocationRelativeTo(null);
	}
	
	private class ButtonListener implements ActionListener
	{
		public void actionPerformed(ActionEvent e) 
		{
			frame.setVisible(false);
			chooseImage();
			updateImage();
			frame.setVisible(true);
		}
	}
	
	private BufferedImage createImage() 
    {
        BufferedImage bi = new BufferedImage(600, 600, BufferedImage.TYPE_INT_RGB);
        Graphics g =  bi.getGraphics();
        g.drawImage(image, 0, 0, null);
        return bi;
    }
	
	private void updateImage()
	{
		for(Peer p:Peer_list)
		{
			int port=p.getportNumber();
			try 
			{
				Socket socket=new Socket(IP_ADDRESS,port);
				ObjectOutputStream out= new ObjectOutputStream(socket.getOutputStream());
				Message update = new Message();
				update.setMe("Teacher");
				update.setYou(p.getUsername());
				update.setCommand("IMAGE_UPDATE");
				update.setData_image_name(image_filename);
				out.reset();
				out.writeObject(update);
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
	}
	
	private void activeCheck()
	{
		Thread t=new Thread(new activeCheckHandler());
		t.start();
	}
	
	private class activeCheckHandler implements Runnable 
	{
		public void run()
		{
			while(true)
			{	
				int teacher=0;
				for(Peer p:Peer_list)
				{
					if(p.getUsername().equals("Teacher"))
					{
						teacher++;
					}
					if(teacher<2)
					{
						//int port=p.getportNumber();
						System.out.print(p.getUsername()+" ");
						System.out.println(p.getlast_login_time());
						//Socket socket=new Socket(IP_ADDRESS,port);
						//ObjectOutputStream out= new ObjectOutputStream(socket.getOutputStream());
						//ObjectInputStream in= new ObjectInputStream(socket.getInputStream());
						//Thread t=new Thread(new activeCheckHandler2(socket,out,in,p));
						//t.start();
					}
				}
				try {
					Thread.sleep(60000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("------display status------");
			}
		}
	}
	
	private class activeCheckHandler2 implements Runnable	//create problem
	{
		private Socket sock;
		private ObjectInputStream in;
	    private ObjectOutputStream out;
	    private Peer p;
		
		private activeCheckHandler2(Socket clientSocket,ObjectOutputStream out,ObjectInputStream in,Peer p) throws IOException
		{
			this.sock=clientSocket;
			this.out=new ObjectOutputStream(sock.getOutputStream());
			this.in=new ObjectInputStream(sock.getInputStream());
			this.p=p;
			
		}
		public void run()
		{
			Message OK;
			try {
				Message activeCheck = new Message();
				activeCheck.setMe("Teacher");
				activeCheck.setYou(p.getUsername());
				activeCheck.setCommand("ACTIVE_CHECK");
				out.reset();
				out.writeObject(activeCheck);
				
				Thread.sleep(1000);
				
				OK = (Message)in.readObject();
				if(!OK.getCommand().equals("ACTIVE_CHECK_OK"))
				{
					synchronized(Peer_list)
					{
						Peer_list.remove(p);
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
