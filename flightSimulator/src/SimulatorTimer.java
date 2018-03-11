import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Παναγιώτης on 26/1/2018.
 */
// Global timer
public class SimulatorTimer extends Thread{
    Object alarm = new Object();
    Timer innerTimer = new Timer();
    TimerTask task = new TimerTask() {
        public void run() {
            Simulator.simulatedTime += 0.01d;
            Simulator.frame.myInfoBar.myRefresh();
        }
    };


    public void run() {
        // every 0.01 sec, show the increase
        innerTimer.scheduleAtFixedRate(task, 10, 10);
        synchronized (alarm) {
            try {
                alarm.wait();
            }
            catch (InterruptedException ex){
                System.out.println("simulatorTimer has been interrupted");
            }
        }
        //here it should reset the time to 0.00
        innerTimer.cancel();
        innerTimer.purge();
        System.out.println("SimulatorTimer has been stopped succesfully");
        Simulator.simulatedTime = 0.00d;
        Simulator.frame.myInfoBar.myRefresh();
        return;
    }
}




