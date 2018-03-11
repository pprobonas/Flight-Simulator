import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Created by Παναγιώτης on 27/11/2017.
 */
public class Airplane extends JPanel implements MouseListener{
    String orientation; //w, e, n, s, ne, nw, se, sw; valid orientations
    public int xPos, yPos, height; //starting point of an airplane
    public byte typeOfPlane;
    public int myFlightId;
    public String myFlightName;
    private JLabel txtArea =new JLabel(  "" ) ;

    public Airplane(int x, int y, String orientation, int height){
        this.setBounds(x,y,50,50);
        this.height = height;
        xPos = x;
        yPos = y;
        this.orientation = orientation;
        this.setOpaque(false);
        this.addMouseListener(this);
    }



    public static BufferedImage rotate(BufferedImage image, double angle) {
        // when a plane heads at west-east etc.
        double sin = Math.abs(Math.sin(angle)), cos = Math.abs(Math.cos(angle));
        int w = image.getWidth(), h = image.getHeight();
        int neww = (int)Math.floor(w*cos+h*sin), newh = (int) Math.floor(h * cos + w * sin);
        GraphicsConfiguration gc = getDefaultConfiguration();
        BufferedImage result = gc.createCompatibleImage(neww, newh, Transparency.TRANSLUCENT);
        Graphics2D g = result.createGraphics();
        g.translate((neww - w) / 2, (newh - h) / 2);
        g.rotate(angle, w / 2, h / 2);
        g.drawRenderedImage(image, null);
        g.dispose();
        return result;
    }

    public static GraphicsConfiguration getDefaultConfiguration() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        return gd.getDefaultConfiguration();
    }



    @Override
    public void mouseEntered(MouseEvent event) {
        // add event listener for showing some attributes of a plane on mouseover
        String myMesg = String.format("<html> <font color='white'> Airplane in flight with: <br/> id: %s <br/> name: %s <br/> height: %s feets </html>",myFlightId,myFlightName, this.height);
        txtArea.setText(myMesg);
        txtArea.setFont(new Font("Courier New", Font.ITALIC + Font.BOLD, 13));
        txtArea.setBounds(10,10,600,100);
        Simulator.frame.mapFrame.add(txtArea,0);
        Simulator.frame.getContentPane().revalidate();
        Simulator.frame.getContentPane().repaint();
    }

    @Override
    public void mouseExited(MouseEvent e) {
        // on mouse exit we need to clean text area
        clearMyTxtArea();
    }

    public void clearMyTxtArea(){
        if (txtArea.getText()!="") {
            Simulator.frame.mapFrame.remove(txtArea);
            txtArea.setText("");
            Simulator.frame.getContentPane().revalidate();
            Simulator.frame.getContentPane().repaint();
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) { }

    @Override
    public void mousePressed(MouseEvent e) { }

    @Override
    public void mouseReleased(MouseEvent e) { }




}
