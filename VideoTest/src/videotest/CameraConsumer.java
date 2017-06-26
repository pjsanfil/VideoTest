
package videotest;

import cameraops.ConfigCommand;
import cameraops.ColorCommand;
import java.io.ByteArrayInputStream;
import java.util.concurrent.BlockingQueue;
import javafx.scene.image.Image;
import org.opencv.contrib.FaceRecognizer;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import static org.opencv.imgproc.Imgproc.cvtColor;
import org.opencv.objdetect.CascadeClassifier;

/**
 * Consumer of Video Frames, performs any signal processing operations on them,
 * then sends them to the destination.
 * @author pjsanfil
 */
public class CameraConsumer implements Runnable {
    private static final String HC_FRONTAL = "/usr/share/opencv/haarcascades/haarcascade_frontalface_default.xml";
    
    private BlockingQueue<Mat> m_frameQ;
    private BlockingQueue<ConfigCommand> m_cmdQ;
    private VideoUpdater m_vidUpdate;
    private ColorCommand.ColorWord m_colorSetting;
    
    private CascadeClassifier m_faceRec;
    
    public CameraConsumer(BlockingQueue<Mat> frameQueue, BlockingQueue<ConfigCommand> cmdQueue, VideoUpdater toUpdate) {
        m_frameQ = frameQueue;
        m_cmdQ = cmdQueue;
        m_vidUpdate = toUpdate;
        m_colorSetting = ColorCommand.ColorWord.COLOR;
        m_faceRec = new CascadeClassifier(HC_FRONTAL);
    }
    /**
     * OpenCV Packages.
     * libopencv-dev has opencv.pc but that package doesn't seem to
     * be available or no information is available about it.
     * 
     * opencv-data brings in /usr/share/opencv  which has haarcascades in it.
     */
    /**
     * Notes about facial recognition.
     * Haar like features classifier or Local Binary Patterns (LBP) in order to
     * encode the contrasts highlighted by the human face or whatever we are
     * looking for.  Features are extracted using a Cascade classifier which
     * has to be trained in order to recognize different objects.
     */
    /**
     * TODO OCR text in the image
     * 1. detect a book or paper in the frame, see OpenCV feature matching:
     * http://docs.opencv.org/3.0-beta/doc/tutorials/features2d/feature_flann_matcher/feature_flann_matcher.html
     * 2. OCR, use Tesseract-OCR library, C++ with Java bindings.
     */
    
    @Override
    public void run() {
        boolean running = true;
        Mat origFrame;
        Mat grayFrame = new Mat();
        Mat showFrame;
        ConfigCommand cmd;
        while (running) {
            try {
                cmd = m_cmdQ.poll();
                if (cmd != null) {
                    handleCommand(cmd);
                }
                origFrame = m_frameQ.take(); // blocking call
                cvtColor(origFrame, grayFrame, Imgproc.COLOR_BGR2GRAY);
                
                
                if (m_colorSetting == ColorCommand.ColorWord.GREYSCALE) {
                    showFrame = grayFrame;
                    //cvtColor(frame, showFrame, Imgproc.COLOR_BGR2GRAY);
                } else {
                    showFrame = origFrame;
                }
                // TODO: state machine to detect movement and OCR text in the image
                // after it stays still for a second
                
                MatOfByte byteMat = new MatOfByte();
                // converts MAT format frame to bmp format for conversion to Image for GUI
                Highgui.imencode(".bmp", showFrame, byteMat);
                m_vidUpdate.setFrame(new Image(new ByteArrayInputStream(byteMat.toArray())));
            } catch (InterruptedException e) {
                running = false;
                System.out.println("Thread " + Thread.currentThread().getName() + " Quitting, interrupted from Queue take");
            }
            if (Thread.currentThread().isInterrupted()) {
                running = false;
                System.out.println("Thread " + Thread.currentThread().getName() + " Quitting, interrupted");
            }
        }
    }
    
    private void handleCommand(ConfigCommand cmd) {
        System.out.println("Consumer got a command type: " + cmd.getClass());
        if (cmd.getClass() == ColorCommand.class) {
            m_colorSetting = (ColorCommand.ColorWord)cmd.get();
            System.out.println("Consumer Setting color to " + m_colorSetting);
        }
    }
}
