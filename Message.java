import java.io.Serializable;
import java.util.HashSet;

import javax.swing.ImageIcon;

/**
 * This is Message class
 * It implements Serializable class
 * The message object is used for communication between server and client
 * @author Tai Zhe Hui
 */
public class Message implements Serializable
{
	 private String Me;
	 private String You;
	 private String Command;
	 private String Data_image_name;
	 private int[] Data_block_no;
	 private ImageIcon Data_content;
	 private HashSet<Peer> Peer_list; 
	 
	 /**
	  * set sender's name
	 * @param Me sender's name
	 */
	 public void setMe(String Me)
	 {
		 this.Me=Me;
	 }
	 /**
	  * set receiver's name
	 * @param You receiver's name
	 */
	 public void setYou(String You)
	 {
		 this.You=You;
	 }
	 /**
	  * set command
	 * @param Command command
	 */
	 public void setCommand(String Command)
	 {
		 this.Command=Command;
	 }
	 /**
	  * set image's name
	 * @param Data_image_name image's name
	 */
	 public void setData_image_name(String Data_image_name)
	 {
		 this.Data_image_name=Data_image_name;
	 }
	 /**
	  * set number of block
	 * @param Data_block_no number of block
	 */
	 public void setData_block_no(int[] Data_block_no)
	 {
		 this.Data_block_no=Data_block_no;
	 }
	 /**
	  * set the content of data
	 * @param Data_content content of data
	 */
	 public void setData_content(ImageIcon Data_content)
	 {
		 this.Data_content=Data_content;
	 }
	 /**
	  * set the peer list
	 * @param Peer_list peer list
	 */
	 public void setPeer_list(HashSet<Peer> Peer_list)
	 {
		 this.Peer_list=Peer_list;
	 }
	 /**
	  * get sender's name
	 * @return sender's name
	 */
	 public String getMe()
	 {
		 return Me;
	 }
	 /**
	  * get receiver's name
	 * @return receiver's name
	 */
	 public String getYou()
	 {
		 return You;
	 }
	 /**
	  * get command
	 * @return command
	 */
	 public String getCommand()
	 {
		 return Command;
	 }
	 /**
	  * get image's filename
	 * @return image's filename
	 */
	 public String getData_image_name()
	 {
		 return Data_image_name;
	 }
	 /**
	  * get number of block
	 * @return number of block
	 */
	 public int[] getData_block_no()
	 {
		 return Data_block_no;
	 }
	 /**
	  * get data content
	 * @return data content
	 */
	 public ImageIcon getData_content()
	 {
		 return Data_content;
	 }
	 /**
	  * get peer list
	 * @return peer list
	 */
	 public HashSet<Peer> getPeer_list()
	 {
		 return Peer_list;
	 }
}
