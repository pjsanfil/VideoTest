
package videotest;

import cameraops.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.*;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.opencv.core.Mat;

/**
 * Entry point for this JavaFx Application is here.
 */
public class VideoTest extends Application {
    static final String PROGRAM_TITLE = "Video Test Computer Program";
    static final int START_HEIGHT = 800;
    static final int START_WIDTH = 1000;
    
    static final int EXTRA_HEIGHT = 200;
    static final int EXTRA_WIDTH  = 80;
    
    // GUI elements
    Stage m_stage;
    Button m_connectToButton;
    ComboBox<ColorCommand.ColorWord> m_selectColorOption;
    ComboBox<ResolutionCommand.Resolution> m_selectResolution;
    
    // GUI event handlers
    private ConnectToEvent m_connectToEvent;
    private OptionChangeEvent m_optionChangeEvent;
    private ProducerOptionChangeEvent m_producerOptionChangeEvent;
    
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
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * this runs before start but runs on the Launcher thread not the JavaFX
     * Application Thread.
     */ 
    @Override
    public void init() {
        OpenCvLibHandler.loadLib();
        m_procCount = Runtime.getRuntime().availableProcessors();
        System.out.println("Available processors: " + m_procCount);
    }
    
    @Override
    public void start(Stage stage) throws Exception {
        m_stage = stage;
        stage.setTitle(PROGRAM_TITLE);
        BorderPane rootNode = new BorderPane();
        
        Scene scene = new Scene(rootNode, START_WIDTH, START_HEIGHT);
        stage.setScene(scene);
        ImageView videoWindow = new ImageView();
        
        rootNode.setCenter(videoWindow);
        rootNode.setBottom(setupButtonBar());
        
        m_frameQ = new ArrayBlockingQueue<>(5);
        m_consumerCmdQ = new ArrayBlockingQueue<>(10);
        m_producerCmdQ = new ArrayBlockingQueue<>(10);
        
        m_vidUpdater = new VideoUpdater(videoWindow);
        m_consumerThread = new Thread(new CameraConsumer(m_frameQ, m_consumerCmdQ, m_vidUpdater), "Camera-Consumer-1");
        m_consumerThread.start();
        System.out.println("The JavaFX GUI runs on thread: " + Thread.currentThread().getName());
        stage.show();
    }
    
    @Override
    public void stop() {
        System.out.println("Shutting down");
        if (m_producerExec != null) {
            m_producerExec.shutdownNow(); // sends interrupt to thread
        }
        if (m_consumerThread != null) {
            m_consumerThread.interrupt();
        }
    }
    
    /**
     * Creates an HBox full of controls for the program.
     * @return The HBox containing the controls for the program.
     */
    private HBox setupButtonBar() {
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(15, 12, 15, 12));
        hbox.setSpacing(10);
        hbox.setStyle("-fx-background-color: #336699;");
        
        m_connectToEvent = new ConnectToEvent();
        m_connectToButton = new Button("Connect to...");
        m_connectToButton.setOnAction(m_connectToEvent);
        // added to scene below
        
        m_optionChangeEvent = new OptionChangeEvent();
        ObservableList<ColorCommand.ColorWord> colorOptions = FXCollections.observableArrayList(
                ColorCommand.ColorWord.COLOR, ColorCommand.ColorWord.GREYSCALE);
        m_selectColorOption = new ComboBox<>(colorOptions);
        m_selectColorOption.setValue(ColorCommand.ColorWord.COLOR);
        m_selectColorOption.setOnAction(m_optionChangeEvent);
        // added to scene below
        
        ObservableList<ResolutionCommand.Resolution> resolutionOptions = FXCollections.observableArrayList();
        for (ResolutionCommand.Resolution r : ResolutionCommand.Resolution.values()) {
            resolutionOptions.add(r);
        }
        m_selectResolution = new ComboBox<>(resolutionOptions);
        m_selectResolution.setValue(ResolutionCommand.Resolution.RES_800x600);
        m_producerOptionChangeEvent = new ProducerOptionChangeEvent();
        m_selectResolution.setOnAction(m_producerOptionChangeEvent);
        
        hbox.getChildren().addAll(m_connectToButton, m_selectColorOption, m_selectResolution);
        return hbox;
    }
    
    
    
    
    private class ConnectToEvent implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent evt) {
            startLocalProducer();
            // TODO will add a dialog box to allow configuring a remote connection
        }
        
        private void startLocalProducer() {
            if (m_producer != null) {
                m_producer.stopCapture();
                m_producerExec.shutdownNow();
            }
            m_producer = new CameraProducerLocal(m_frameQ, m_producerCmdQ);
            m_producerExec = Executors.newSingleThreadScheduledExecutor(m_producer);
            m_producerExec.scheduleAtFixedRate(m_producer, 0, 33, TimeUnit.MILLISECONDS);
        }

        private void startRemoteProducer() {
            if (m_producer != null) {
                m_producer.stopCapture();
                m_producerExec.shutdownNow();
            }
        }
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
                // resize window if it's too small
                if (m_stage.getWidth() < cmd.getWidth() + EXTRA_WIDTH) {
                    m_stage.setWidth(cmd.getWidth() + EXTRA_WIDTH);
                }
                if (m_stage.getHeight() < cmd.getHeight() + EXTRA_HEIGHT) {
                    m_stage.setHeight(cmd.getHeight() + EXTRA_HEIGHT);
                }
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
