/**
 * Created by Παναγιώτης on 22/11/2017.
 */

import javax.swing.*;
import java.awt.*;

import static javax.swing.BorderFactory.createEmptyBorder;

public class SimulationFrame extends JFrame {

    private final int FRAME_WIDTH = 1260;
    private final int FRAME_HEIGHT = 480;
    public InfoBar myInfoBar;
    public Map myMap;
    public JPanel mapFrame;
    public MessagePanel myMessagePanel;
    private ΜyJMenuBar menuBar;


    public SimulationFrame() {
        super("MediaLab Flight Simulation");
        this.setResizable(false);
        this.setSize(FRAME_WIDTH, FRAME_HEIGHT); // fixed pixels
        this.setLocationRelativeTo(null); // to center
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new BorderLayout(0, 0));
        this.reInit(); // place menuBar + infoBar
        createMapAndMessages(""); // just make Dimensions look right
        this.setVisible(true);
    }

    public void reInit() {  // Initializes info bar and menu (top elements)
        this.getContentPane().removeAll();
        menuBar = new ΜyJMenuBar(); // also listens to load button to create a map
        this.setJMenuBar(menuBar);
        myInfoBar = new InfoBar();
        this.getContentPane().add(myInfoBar, BorderLayout.PAGE_START);
        this.pack();
    }

    public void createMapAndMessages(String MAPID) {
        // Wait for menu bar to call you when someone tries to load a world
        myMap = new Map(MAPID); // a map object that consists of squares and aiports
        mapFrame = new JPanel(null);
        myMap.setBounds(0, 0, 960, 480);
        mapFrame.add(myMap, -1);
        this.getContentPane().add(mapFrame, BorderLayout.CENTER); // add a map in my frame at center
        myMessagePanel = new MessagePanel();
        JScrollPane scrollPane = new JScrollPane(myMessagePanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setPreferredSize(new Dimension(300, 480));
        this.getContentPane().add(scrollPane, BorderLayout.EAST); // ad message panel at right of my frame
        this.pack();
    }


}
