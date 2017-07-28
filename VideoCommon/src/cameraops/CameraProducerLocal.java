
package cameraops;

import java.util.concurrent.BlockingQueue;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

/**
 * Thread which gets camera frames and sends them into the queue.
 */
public class CameraProducerLocal extends CameraProducer {
    private final VideoCapture m_camera;

    private static int m_threadCount = 0;
    

    public CameraProducerLocal(BlockingQueue<Mat> frameQueue, BlockingQueue<ConfigCommand> cmdQueue) {
        super(frameQueue, cmdQueue);
        m_camera = new VideoCapture(); // comes from org.opencv.highgui.VideoCapture;
        m_camera.open(0);
        setupCamera();
        setResolution(800, 600);
        
        if (!m_camera.isOpened()) {
            System.out.println("ERROR Could not open camera");
            System.exit(0);
        }
    }
    
    
    @Override  // TODO throw an exception or something when it fails to get a frame
    protected Mat getFrame() {
        int missedFrameCount = 0;
        Mat frame = new Mat();
        m_camera.read(frame);
        /*
        while (!m_camera.read(frame) && m_running.get()) {
            // the code in this while loop doesn't normally execute at all
            if (Thread.currentThread().isInterrupted()) {
                System.out.println("Thread " + Thread.currentThread().getName() + " Quitting, interrupted from polling");
                stopCapture();
            }
            missedFrameCount++;
            if (missedFrameCount > 5) {
                System.out.println("Missed frame count = " + missedFrameCount);
            }
        }
        */
        return frame;
    }
    
    
    
    @Override // ThreadFactory method
    public Thread newThread(Runnable r) {
        m_threadCount++;
        return new Thread(r, "Camera-Producer-Local-" + m_threadCount);
    }
    
    @Override
    protected void handleCommand(ConfigCommand cmd) {
        if (cmd.getClass() == ResolutionCommand.class) {
            setResolution(((ResolutionCommand)cmd).getWidth(), ((ResolutionCommand)cmd).getHeight());
        }
    }
    
    private void setResolution(double width, double height) {
        System.out.println("Camera Resolution:  " + m_camera.get(Videoio.CV_CAP_PROP_FRAME_WIDTH) + " x " 
                + m_camera.get(Videoio.CV_CAP_PROP_FRAME_HEIGHT));
        // for some reason these 2 functions return false even when they work
        System.out.println("Setting to " + width + " x " + height);
        m_camera.set(Videoio.CV_CAP_PROP_FRAME_WIDTH, width);
        m_camera.set(Videoio.CV_CAP_PROP_FRAME_HEIGHT, height);
        double newwidth = m_camera.get(Videoio.CV_CAP_PROP_FRAME_WIDTH);
        double newheight = m_camera.get(Videoio.CV_CAP_PROP_FRAME_HEIGHT);
        System.out.println("Camera Resolution: " + newwidth + " x " + newheight);
    }
    
    @Override
    public void stopCapture() {
        if (m_camera.isOpened()) {
            m_camera.release();
            System.out.println("Capture stopped");
        } else {
            System.out.println("stopCapture called when already stopped");
        }
        m_running.set(false);
    }
    
    private void setupCamera() {
        
    }
}
