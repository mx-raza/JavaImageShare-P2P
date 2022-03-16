
import java.io.Serializable;
import java.util.HashSet;
import javax.swing.ImageIcon;

/**
 * This is Peer class
 * It implements Serializable class
 * @author Tai Zhe Hui
 */
public class Peer implements Serializable
{
	private String status;
	private String username;
	private String last_login_time;
	private HashSet<Peer> Peer_list=new HashSet<Peer>();
	private String IP_address;
	private int portNumber=8000;
	private String image_filename;
	private String [][] image_block_list=new String[20][20];
	private ImageIcon[][] image_icon_list=new ImageIcon[20][20];
	
	/**
	 * set peer as free or buzy
	 * @param status free or buzy
	 */
	public void setStatus(String status)
	{
		 this.status=status;
	}
	/**
	 * set user's name
	 * @param username user's name
	 */
	public void setUsername(String username)
	{
		 this.username=username;
	}
	/**
	 * set last login time
	 * @param last_login_time last login time
	 */
	public void setlast_login_time(String last_login_time)
	{
		 this.last_login_time=last_login_time;
	}
	/**
	 * set peer list
	 * @param Peer_list peer list
	 */
	public void setPeer_list(HashSet<Peer> Peer_list)
	{
		 this.Peer_list=Peer_list;
	}
	/**
	 * set peer's IP address 
	 * @param IP_address peer's IP address 
	 */
	public void setIP_address(String IP_address)
	{
		 this.IP_address=IP_address;
	}
	/**
	 * set port number
	 * @param portNumber port number
	 */
	public void setportNumber(int portNumber)
	{
		 this.portNumber=portNumber;
	}
	/** set image_filename
	 * @param image_filename image_filename
	 */
	public void setimage_filename(String image_filename)
	{
		 this.image_filename=image_filename;
	}
	/**
	 * set image as filled, waiting or not-filled
	 * @param s filled, waiting or not-filled
	 * @param i x coordinate
	 * @param j y coordinate
	 */
	public void setimage_block_list(String s,int i,int j)
	{
		synchronized(image_block_list)
		{
			image_block_list[i][j]=s;
		}
	}
	/**
	 * set image icon
	 * @param ic image icon
	 * @param i x coordinate
	 * @param j y coordinate
	 */
	public void setimage_icon_list(ImageIcon ic,int i,int j)
	{
		synchronized(image_icon_list)
		{
			image_icon_list[i][j]=ic;
		}
	}
	
	/**
	 * get free or buzy
	 * @return free or buzy
	 */
	public String getStatus()
	{
		 return status;
	}
	/**
	 * get user's name
	 * @return user's name
	 */
	public String getUsername()
	{
		return username;
	}
	/**
	 * get last login time
	 * @return last login time
	 */
	public String getlast_login_time()
	{
		return last_login_time;
	}
	/**
	 * get peer list
	 * @return peer list
	 */
	public HashSet<Peer> getPeer_list()
	{
		return Peer_list;
	}
	/**
	 * get peer's IP address
	 * @return peer's IP address
	 */
	public String getIP_address()
	{
		 return IP_address;
	}
	/**
	 * get port number
	 * @return port number
	 */
	public int getportNumber()
	{
		 return portNumber;
	}
	/**
	 * get image's name
	 * @return image's name
	 */
	public String getimage_filename()
	{
		 return image_filename;
	}
	/**
	 * get filled, waiting or not-filled
	 * @param i x coordinate
	 * @param j y coordinate
	 * @return filled, waiting or not-filled
	 */
	public String getimage_block_list(int i,int j)
	{
		synchronized(image_block_list)
		{
			return image_block_list[i][j];
		}
	}
	/**
	 * get image icon
	 * @param i x coordinate
	 * @param j y coordinate
	 * @return image icon
	 */
	public ImageIcon getimage_icon_list(int i,int j)
	{
		synchronized(image_icon_list)
		{
			return image_icon_list[i][j];
		}
	}
	
}
	


