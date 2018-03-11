/**
 * Created by Παναγιώτης on 22/11/2017.
 */

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;

public class Square extends JPanel implements MouseListener {

    final private int SQUARE_WIDTH = 16;
    final private int SQUARE_HEIGHT = 16;
    private int red;
    private int green;
    private int blue;
    private int height;
    private Airports connectedAirport;
    JLabel txtArea;

    public Square(int height, int red, int green, int blue, Airports connectedAirport) {
        super();
        this.setLayout(null);
        this.height = height;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.connectedAirport = connectedAirport;
        this.setPreferredSize(new Dimension(16, 16));
    }


    @Override
    public void paint(Graphics g) {
        super.paintComponent(g);
        g.setColor(new Color(this.red, this.green, this.blue));
        g.fillRect(0, 0, SQUARE_WIDTH, SQUARE_HEIGHT);
        //g.setColor(Color.black); // must be set for visible borders
        g.drawRect(0, 0, 16, 16);
        // if connected to an airport, draw the airport in the middle
        if (this.connectedAirport != null) {
            if (this.connectedAirport.getOpen()) {
                try {
                    g.drawImage(ImageIO.read(new File("airport.png")), 4, 4, null);
                } catch (IOException ex) {
                    System.out.println("cannot find airport.png");
                    System.exit(0);
                }
            }
        }
    }



    public int getHght() {
        return this.height;
    }


    public void setAirport(Airports connectedAirport) {

        this.connectedAirport = connectedAirport;
        this.addMouseListener(this);
    }

    @Override
    public void mouseEntered(MouseEvent event) {
        String myMesg = String.format("<html> <font color='white'> Airport with: <br/> id: %s <br/> name: %s <br/> orientation: %s <br/> type: %s <br/> height: %s <br/> </html>", connectedAirport.getId(), connectedAirport.getAirportName(), connectedAirport.getStringOrientation(), connectedAirport.getType(), height);
        txtArea = new JLabel(myMesg);
        txtArea.setFont(new Font("Courier New", Font.ITALIC + Font.BOLD, 13));
        txtArea.setBounds(10, 10, 600, 100);
        Simulator.frame.mapFrame.add(txtArea, 0);
        Simulator.frame.getContentPane().revalidate();
        Simulator.frame.getContentPane().repaint();

    }

    @Override
    public void mouseExited(MouseEvent e) {
        Simulator.frame.mapFrame.remove(txtArea);
        Simulator.frame.getContentPane().revalidate();
        Simulator.frame.getContentPane().repaint();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

}
