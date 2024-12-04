package it.unibo.oop.reactivegui03;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Third experiment with reactive gui.
 */
@SuppressWarnings("PMD.AvoidPrintStackTrace")
public final class AnotherConcurrentGUI extends JFrame {

    private static final long serialVersionUID = 1L;
    private static final double WIDTH_PERC = 0.2;
    private static final double HEIGHT_PERC = 0.1;
    private static final long STOP_TIME = TimeUnit.SECONDS.toMillis(10);
    private final JLabel display = new JLabel();
    private final JButton up = new JButton("up");
    private final JButton down = new JButton("down");
    private final JButton stop = new JButton("stop");

    /**
     * Builds a new CGUI
     */
    public AnotherConcurrentGUI() {
        super();
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setSize((int) (screenSize.getWidth() * WIDTH_PERC), (int) (screenSize.getHeight() * HEIGHT_PERC));
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        final JPanel panel = new JPanel();
        panel.add(display);
        panel.add(up);
        panel.add(down);
        panel.add(stop);
        this.getContentPane().add(panel);
        this.setVisible(true);
        /*
         * Create the counter agent and start it.
         */
        final Agent agent = new Agent();
        new Thread(agent).start();
        /*
         * Register a listener that stops it
         */
        stop.addActionListener(e -> {
            agent.stopCounting();
            up.setEnabled(false);
            stop.setEnabled(false);
            down.setEnabled(false);
            });
        /*
         * Register a listener that up the incrementation
         */
        up.addActionListener(e -> agent.upCounting());
        /*
         * Register a listener that down the incrementation
         */
        down.addActionListener(e -> agent.downCounting());

    }

    /*
     * The counter agent is implemented as a nested class.
     */
    private class Agent implements Runnable {
        private volatile boolean stop;
        private int counter;
        private volatile boolean up = true;
        
        @Override
        public void run() {
            while (!this.stop) {
                try {
                    if (up) {
                        counter++;
                    } else {
                        counter--;
                    }
                    // The EDT doesn't access `counter` anymore, it doesn't need to be volatile 
                    final var nextText = Integer.toString(this.counter);
                    SwingUtilities.invokeAndWait(() -> AnotherConcurrentGUI.this.display.setText(nextText));
                    Thread.sleep(100);
                } catch (InvocationTargetException | InterruptedException ex) {
                    /*
                     * This is just a stack trace print, in a real program there
                     * should be some logging and decent error reporting
                     */
                    ex.printStackTrace();
                }
            }
        }
        
        /**
         * External command to stop counting.
         */
        public void stopCounting() {
            this.stop = true;
        }

        /**
         * External command to up counting
         */
        public void upCounting() {
            this.up = true;
        }

        /**
         * External command to down counting
         */
        public void downCounting() {
            this.up = false;
        }
    }
}
