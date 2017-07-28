
package cameraops;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import org.opencv.core.Mat;

/**
 * Provides a thread which produces video frames and shoves them into a queue.
 */
public abstract class CameraProducer implements Runnable, ThreadFactory {
    private final BlockingQueue<Mat> m_frameQ;
    private final BlockingQueue<ConfigCommand> m_cmdQ;
    protected AtomicBoolean m_running;
    
    public CameraProducer(BlockingQueue<Mat> frameQueue, BlockingQueue<ConfigCommand> cmdQueue) {
        m_frameQ = frameQueue;
        m_cmdQ = cmdQueue;
        m_running = new AtomicBoolean(true);
    }
    
    public abstract void stopCapture();
    
    @Override
    public void run() {
        boolean running = true;
        Mat frame;
        int missedFrameCount = 0;
        ConfigCommand cmd;
        
        if (m_running.get()) {
            cmd = m_cmdQ.poll();
            if (cmd != null) {
                handleCommand(cmd);
            }
            frame = getFrame();
            assert(frame != null); // probably using blocking Q calls so can't return null
            Mat fc = new Mat();
            frame.copyTo(fc);

            try {
                m_frameQ.put(fc);
            } catch (InterruptedException e) {
                System.out.println("Thread " + Thread.currentThread().getName() + " Quitting, interrupted from Queue put");
            }
        }
    }
    
    /**
     * Gets a single video frame and returns it.
     * @return A single frame from the video source.
     */
    protected abstract Mat getFrame();
    
    protected abstract void handleCommand(ConfigCommand cmd);
}
