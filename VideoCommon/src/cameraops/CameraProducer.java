
package cameraops;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;

/**
 * Thread which gets camera frames and sends them into the queue.
 * Is a ThreadFactory so it can make instances of itself with a useful thread
 * name.
 * @author pjsanfil
 */
public class CameraProducer implements Runnable, ThreadFactory {
    private static final int INTERVAL = 100;///you may use interval
    private final VideoCapture m_camera;
    private final BlockingQueue<Mat> m_frameQ;
    private final BlockingQueue<ConfigCommand> m_cmdQ;
    private static int m_threadCount = 0;
    

    public CameraProducer(BlockingQueue<Mat> frameQueue, BlockingQueue<ConfigCommand> cmdQueue) {
        m_camera = new VideoCapture(); // comes from org.opencv.highgui.VideoCapture;
        m_frameQ = frameQueue;
        m_cmdQ = cmdQueue;
        m_camera.open(0);
        setupCamera();
        setResolution(800, 600);
        //setResolution(Resolution.RES_1280x960);
        
        if (!m_camera.isOpened()) {
            System.out.println("ERROR Could not open camera");
            System.exit(0);
        }
    }
    /*
    public enum Resolution {RES_160x120, RES_640x480, RES_800x448, RES_752x416, RES_800x600, RES_960x544, RES_960x720, RES_1024x576, RES_1280x960;
        public String toString() {
            return this.name().substring(4);
        }
    }
    public static final double[][] resolutionVals = new double[][]{ {160,120},
                                                                     {640,480},
                                                                     {800,448},
                                                                     {752,416},
                                                                     {800,600},
                                                                     {960,544},
                                                                     {960,720},
                                                                     {1024,576},
                                                                    {1280,960}};
    
    public void setResolution(Resolution res) {
        setResolution(resolutionVals[res.ordinal()][0], resolutionVals[res.ordinal()][1]);
    }
    */

    
    @Override
    public void run() {
        boolean running = true;
        Mat frame = new Mat();
        int missedFrameCount = 0;
        ConfigCommand cmd;
        
        cmd = m_cmdQ.poll();
        if (cmd != null) {
            handleCommand(cmd);
        }
        
        while (!m_camera.read(frame) && running == true) {
            // the code in this while loop doesn't normally execute at all
            if (Thread.currentThread().isInterrupted()) {
                running = false;
                System.out.println("Thread " + Thread.currentThread().getName() + " Quitting, interrupted from polling");
                stopCapture();
            }
            missedFrameCount++;
            if (missedFrameCount > 5) {
                System.out.println("Missed frame count = " + missedFrameCount);
            }
        }
        missedFrameCount = 0;
        try {
            m_frameQ.put(frame);
        } catch (InterruptedException e) {
            System.out.println("Thread " + Thread.currentThread().getName() + " Quitting, interrupted from Queue put");
            stopCapture();
        }
    }
    
    @Override // ThreadFactory method
    public Thread newThread(Runnable r) {
        m_threadCount++;
        return new Thread(r, "Camera-Producer-" + m_threadCount);
    }
    
    private void handleCommand(ConfigCommand cmd) {
        if (cmd.getClass() == ResolutionCommand.class) {
            setResolution(((ResolutionCommand)cmd).getWidth(), ((ResolutionCommand)cmd).getHeight());
        }
    }
    
    private void setResolution(double width, double height) {
        System.out.println("Camera Resolution:  " + m_camera.get(Highgui.CV_CAP_PROP_FRAME_WIDTH) + " x " 
                + m_camera.get(Highgui.CV_CAP_PROP_FRAME_HEIGHT));
        // for some reason these 2 functions return false even when they work
        System.out.println("Setting to " + width + " x " + height);
        m_camera.set(Highgui.CV_CAP_PROP_FRAME_WIDTH, width);
        m_camera.set(Highgui.CV_CAP_PROP_FRAME_HEIGHT, height);
        double newwidth = m_camera.get(Highgui.CV_CAP_PROP_FRAME_WIDTH);
        double newheight = m_camera.get(Highgui.CV_CAP_PROP_FRAME_HEIGHT);
        System.out.println("Camera Resolution: " + newwidth + " x " + newheight);
    }
    
    private void stopCapture() {
        if (m_camera.isOpened()) {
            m_camera.release();
        }
    }
    
    private void setupCamera() {
        
    }
}
