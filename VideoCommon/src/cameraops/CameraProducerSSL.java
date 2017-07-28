
package cameraops;

import java.util.concurrent.BlockingQueue;
import org.opencv.core.Mat;

/**
 * Thread which gets camera frames from an SSL socket and sends them into the queue.
 */
public class CameraProducerSSL extends CameraProducer {
    private static int m_threadCount = 0;
    
    public CameraProducerSSL(BlockingQueue<Mat> frameQueue, BlockingQueue<ConfigCommand> cmdQueue) {
        super(frameQueue, cmdQueue);
    }

    @Override
    public void stopCapture() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected Mat getFrame() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void handleCommand(ConfigCommand cmd) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Thread newThread(Runnable r) {
        m_threadCount++;
        return new Thread(r, "Camera-Producer-SSL-" + m_threadCount);
    }
    
}
