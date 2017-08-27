
package videotest;

import cameraops.ConfigCommand;
import java.util.concurrent.ArrayBlockingQueue;
import javafx.scene.image.ImageView;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.opencv.core.Core;
import org.opencv.core.Mat;

/**
 *
 * @author pjsanfil
 */
public class CameraConsumerTest {
    
    public CameraConsumerTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of class CameraConsumer.
     */
    @Test
    public void testRun() {
        System.out.println("run");
        ArrayBlockingQueue<Mat> frameQ = new ArrayBlockingQueue<>(5);
        ArrayBlockingQueue<ConfigCommand> cmdQ = new ArrayBlockingQueue<>(5);
        ImageView imageView = new ImageView();
        VideoUpdater videoUpdater = new VideoUpdater(imageView);
        try {
            CameraConsumer instance = new CameraConsumer(null, cmdQ, videoUpdater);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // good to go
        }
        
        try {
            CameraConsumer instance = new CameraConsumer(frameQ, null, videoUpdater);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // good to go
        }
        
        try {
            CameraConsumer instance = new CameraConsumer(frameQ, cmdQ, null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // good to go
        }
        CameraConsumer instance = new CameraConsumer(frameQ, cmdQ, videoUpdater);
        Thread cons = new Thread(instance);
        cons.start();
        cmdQ.add(new cameraops.ColorCommand());
        // TODO pass frames
        // how to check if commands received
    }
    
}
