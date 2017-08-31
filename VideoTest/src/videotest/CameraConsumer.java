
package videotest;

import cameraops.ConfigCommand;
import cameraops.ColorCommand;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.concurrent.BlockingQueue;
import javafx.scene.image.Image;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import static org.opencv.imgproc.Imgproc.cvtColor;

/**
 * Consumer of Video Frames, performs any processing operations on them,
 * then sends them to the destination.
 */
public final class CameraConsumer implements Runnable {
    private static final File HC_FRONTAL = new File("/usr/local/share/OpenCV/haarcascades/haarcascade_frontalface_default.xml");
    private static final File LBP_FRONTAL = new File("/usr/local/share/OpenCV/lbpcascades/lbpcascade_frontalface.xml");
    
    private BlockingQueue<Mat> m_frameQ;
    private BlockingQueue<ConfigCommand> m_cmdQ;
    private VideoUpdater m_vidUpdate;
    private ColorCommand.ColorWord m_colorSetting;
    
    private CascadeClassifier m_cascade;
    
    public CameraConsumer(BlockingQueue<Mat> frameQueue, BlockingQueue<ConfigCommand> cmdQueue, VideoUpdater toUpdate) {
        if (frameQueue == null || cmdQueue == null || toUpdate == null) {
            throw new NullPointerException();
        }
        m_frameQ = frameQueue;
        m_cmdQ = cmdQueue;
        m_vidUpdate = toUpdate;
        m_colorSetting = ColorCommand.ColorWord.COLOR;
        if (!HC_FRONTAL.exists()) {
            System.out.println("Error: HAAR CascadeClassifier file " + HC_FRONTAL.getAbsolutePath() + " does not exist, exiting");
            System.exit(0);
        }
        if (!LBP_FRONTAL.exists()) {
            System.out.println("Error: LBP CascadeClassifier file " + LBP_FRONTAL.getAbsolutePath() + " does not exist, exiting");
            System.exit(0);
        }
        m_cascade = null; // = new CascadeClassifier(HC_FRONTAL.getAbsolutePath());
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
                assert(origFrame != null); // impossible for assert to fail because Q take was blocking call
                
                cvtColor(origFrame, grayFrame, Imgproc.COLOR_BGR2GRAY); // TODO add back in, but causes exception when camera shutdown is initiated
                
                
                if (m_colorSetting == ColorCommand.ColorWord.GREYSCALE) {
                    showFrame = grayFrame;
                    //cvtColor(frame, showFrame, Imgproc.COLOR_BGR2GRAY);
                } else {
                    showFrame = origFrame;
                }
                
                if (m_cascade != null) {
                    FaceRecognize(grayFrame, showFrame);
                }
                // TODO: state machine to detect movement and OCR text in the image
                // after it stays still for a second
                
                MatOfByte byteMat = new MatOfByte();
                // converts MAT format frame to bmp format for conversion to Image for GUI
                Imgcodecs.imencode(".bmp", showFrame, byteMat);
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
    
    /**
     * Uses whatever the active CascadeClassifier is to detect something in the
     * image.
     * @param grayFrame Input image to detect something in.
     * @param outp Image with a box drawn around the object to detect, if found.
     */
    private void FaceRecognize(Mat grayFrame, Mat outp) {
        MatOfRect faceDetections = new MatOfRect();
        m_cascade.detectMultiScale(grayFrame, faceDetections);

        // Draw a bounding box around each face.
        for (Rect rect : faceDetections.toArray()) {
            Imgproc.rectangle(outp, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0));
        }
    }
    
    private void handleCommand(ConfigCommand cmd) {
        System.out.println("Consumer got a command type: " + cmd.getClass());
        if (cmd.getClass() == ColorCommand.class) {
            m_colorSetting = (ColorCommand.ColorWord)cmd.get();
            System.out.println("Consumer Setting color to " + m_colorSetting);
        } else if (cmd.getClass() == ImageRecCommand.class) {
            changeImageRec((ImageRecCommand)cmd);
        }
    }
    
    private void changeImageRec(ImageRecCommand cmd) {
        if (null != cmd.get()) switch (cmd.get()) {
            case NONE:
                m_cascade = null;
                break;
            case HAAR_CASCADE:
                m_cascade = new CascadeClassifier(HC_FRONTAL.getAbsolutePath());
                break;
            case LBP_CASCADE:
                m_cascade = new CascadeClassifier(LBP_FRONTAL.getAbsolutePath());
                break;
            default:
                break;
        }
    }
}
