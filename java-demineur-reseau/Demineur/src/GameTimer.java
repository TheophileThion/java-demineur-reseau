import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Quentin Chevalier
 */

public class GameTimer {

    public float timerVal = 0f;
    Timer timer;
    private GUI gui;
    /**
     * constructor of the timer.
     * @param gui current gui
     */
    GameTimer(GUI gui) {
        this.gui = gui;
    }
    
    public void runTimer() {
        long period = 500;
        TimerTask repeatedTask = new TimerTask() {
            public void run() {
                timerVal += (float)period / 1000f;
                gui.gameTime.setText("Time : "+String.format("%.1f",timerVal)+"s");
            }
        };
        timer = new Timer("Timer");
        long delay = 0;
        timer.scheduleAtFixedRate(repeatedTask, delay, period);
    }

    public void stopTimer() {
        timer.cancel();
    }

    public void resetTimer() {
        timerVal = 0f;
        gui.gameTime.setText("Time : "+timerVal+"s");
    }

}
