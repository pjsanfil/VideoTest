
package cameraops;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import org.opencv.core.Mat;

/**
 * Thread which gets camera frames and sends them into the queue.
 */
public class CameraProducerLocal extends CameraProducer {
    private final Camera m_camera;

    private static int m_threadCount = 0;
    

    public CameraProducerLocal(BlockingQueue<Mat> frameQueue, BlockingQueue<ConfigCommand> cmdQueue) {
        super(frameQueue, cmdQueue);
        m_camera = new Camera();
        
        try {
            m_camera.open();
        } catch (IOException e) {
            System.err.println("Error, quitting: " + e.getMessage());
            System.exit(0);
        }
        System.out.println("camera initial state: " + this);
    }
    
    
    @Override  // TODO throw an exception or something when it fails to get a frame
    protected Mat getFrame() {
        int missedFrameCount = 0;
        Mat frame = m_camera.getFrame();
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
    public String toString() {
        StringBuilder ans = new StringBuilder();
        int [] wxh = m_camera.getFrameWidthHeight();
        ans.append(wxh[0]).append("x").append(wxh[1]);
        ans.append(" codec: ").append(m_camera.getCodec());
        
        return ans.toString();
    }
    
    @Override
    public void stopCapture() {
        m_camera.close();
        m_running.set(false);
    }
    
    @Override
    protected void handleCommand(ConfigCommand cmd) {
        if (cmd.getClass() == ResolutionCommand.class) {
            m_camera.setResolution(((ResolutionCommand)cmd).getWidth(), ((ResolutionCommand)cmd).getHeight());
        } else if (cmd.getClass() == CodecCommand.class) {
            m_camera.setCodec(((CodecCommand)cmd).get().name());
        }
        System.out.println("camera state: " + this);
    }
}
