import javax.swing.*;
import java.awt.*;

/**
 * Created by Παναγιώτης on 26/11/2017.
 */
// The right panel that handles messages
// Call addNewMessage with proper color to show a message
// Call clearMessagePanel to remove all messages

public class MessagePanel extends JPanel{
    static final Color WARNING = new Color(182, 21, 14);
    static final Color SUCCESS = new Color(42, 158, 0);
    static final Color NEUTRAL = new Color(37, 37, 37);

    public MessagePanel(){
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setBackground(new Color(199, 229, 190));
    }


    public void addNewMessage(String newMessage, Color messageColor){
        newMessage = "\u21E8 " + newMessage + "   "; //u00BB
        JLabel myMsg = new JLabel(newMessage);
        myMsg.setForeground(messageColor);
        myMsg.setFont(new Font("Serif",Font.ITALIC, 16));
        this.add(myMsg);
    }

    public void clearMessagePanel(){
        this.removeAll();
    }



}
