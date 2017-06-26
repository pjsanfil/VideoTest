
package videotest;

import cameraops.ResolutionCommand;
import cameraops.ConfigCommand;
import cameraops.ColorCommand;
import cameraops.CameraProducer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.ComboBox;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.opencv.core.Core;
import org.opencv.core.Mat;

/**
 * Entry point for this JavaFx Application is here.
 * @author pjsanfil
 */
public class VideoTest extends Application {
    static final String PROGRAM_TITLE = "Video Test Computer Program";
    static final int START_HEIGHT = 600;
    static final int START_WIDTH = 800;
    // GUI elements
    ComboBox<ColorCommand.ColorWord> m_selectColorOption;
    ComboBox<ResolutionCommand.Resolution> m_selectResolution;
    
    // timer for aquiring video stream
    private ScheduledExecutorService m_producerExec;
    private ArrayBlockingQueue<Mat> m_frameQ;
    // this is how I get any config information to the other threads
    private ArrayBlockingQueue<ConfigCommand> m_consumerCmdQ;
    private ArrayBlockingQueue<ConfigCommand> m_producerCmdQ;
    private VideoUpdater m_vidUpdater;
    private Thread m_consumerThread;
    private CameraProducer m_producer;
    
    private int m_procCount = 0;
    private OptionChangeEvent m_optionChange;
    private ProducerOptionChangeEvent m_producerOptionChange;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    // this runs before start but runs on the Launcher thread not the JavaFX
    // Application Thread.
    @Override
    public void init() {
        // without loading the actual OpenCV native library you get a wierd
        // Exception at runtime when your first OpenCV call occurs, it is a
        // java.lang.reflect.InvocationTargetException 
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        m_procCount = Runtime.getRuntime().availableProcessors();
        System.out.println("Available processors: " + m_procCount);
    }
    
    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle(PROGRAM_TITLE);
        FlowPane rootNode = new FlowPane();
        //GridPane grid = new GridPane();
        //grid.setAlignment(Pos.CENTER);
        rootNode.setAlignment(Pos.CENTER);
        rootNode.setOrientation(Orientation.VERTICAL);
        Scene scene = new Scene(rootNode, START_WIDTH, START_HEIGHT);
        stage.setScene(scene);
        ImageView videoWindow = new ImageView();
        m_optionChange = new OptionChangeEvent();
        
        ObservableList<ColorCommand.ColorWord> colorOptions = FXCollections.observableArrayList(
                ColorCommand.ColorWord.COLOR, ColorCommand.ColorWord.GREYSCALE);
        m_selectColorOption = new ComboBox<>(colorOptions);
        m_selectColorOption.setValue(ColorCommand.ColorWord.COLOR);
        m_selectColorOption.setOnAction(m_optionChange);
        
        ObservableList<ResolutionCommand.Resolution> resolutionOptions = FXCollections.observableArrayList();
        for (ResolutionCommand.Resolution r : ResolutionCommand.Resolution.values()) {
            resolutionOptions.add(r);
        }
        m_selectResolution = new ComboBox<>(resolutionOptions);
        m_selectResolution.setValue(ResolutionCommand.Resolution.RES_800x600);
        m_producerOptionChange = new ProducerOptionChangeEvent();
        m_selectResolution.setOnAction(m_producerOptionChange);
        
        rootNode.getChildren().addAll(m_selectColorOption, m_selectResolution);
        rootNode.getChildren().add(videoWindow);
        
        m_frameQ = new ArrayBlockingQueue<>(5);
        m_consumerCmdQ = new ArrayBlockingQueue<>(10);
        m_producerCmdQ = new ArrayBlockingQueue<>(10);
        
        m_vidUpdater = new VideoUpdater(videoWindow);
        m_consumerThread = new Thread(new CameraConsumer(m_frameQ, m_consumerCmdQ, m_vidUpdater), "Camera-Consumer-1");
        m_consumerThread.start();
        
        m_producer = new CameraProducer(m_frameQ, m_producerCmdQ);
        
        m_producerExec = Executors.newSingleThreadScheduledExecutor(m_producer);
        m_producerExec.scheduleAtFixedRate(m_producer, 0, 33, TimeUnit.MILLISECONDS);
        
        System.out.println("The JavaFX GUI runs on thread: " + Thread.currentThread().getName());
        stage.show();
    }
    
    @Override
    public void stop() {
        System.out.println("Shutting down");
        m_producerExec.shutdownNow(); // sends interrupt to thread
        m_consumerThread.interrupt();
    }
    
    
    
    private class OptionChangeEvent implements EventHandler<ActionEvent> {

        @Override
        public void handle(ActionEvent evt) {
            ColorCommand cmd = null;
            if (m_selectColorOption == evt.getSource()) {
                System.out.println("Changing color setting to: " + m_selectColorOption.getValue());
                cmd = new ColorCommand();
                cmd.set(m_selectColorOption.getValue());
            }
            if (cmd != null) {
                try {
                    m_consumerCmdQ.put(cmd);
                } catch (InterruptedException e) {
                    System.out.println("Quitting from OptionChangeEvent");
                }
            }
        }
    }
    
    private class ProducerOptionChangeEvent implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent evt) {
            ResolutionCommand cmd = null;
            if (m_selectResolution == evt.getSource()) {
                System.out.println("Changing resolution to: " + m_selectResolution.getValue());
                cmd = new ResolutionCommand();
                cmd.set(m_selectResolution.getValue());
            }
            if (cmd != null) {
                try {
                    m_producerCmdQ.put(cmd);
                } catch (InterruptedException e) {
                    System.out.println("Quitting from OptionChangeEvent");
                }
            }
        }
    }
    
}
