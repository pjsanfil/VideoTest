
package videotestserver;

import cameraops.CameraProducerLocal;
import cameraops.ConfigCommand;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import org.opencv.core.Mat;

public class VideoServerSSLRunnable implements Runnable {
    private final int localPort, destPort;
    
    VideoServerSSLRunnable(int localPort, int destPort) {
        this.localPort = localPort;
        this.destPort = destPort;
    }
    
    @Override
    public void run() {
        System.out.println("Running Server Thread: " + Thread.currentThread().getName());
        try {
            runAction();
        } catch (IOException e) {
            System.out.println("Quitting SSL video server, error: " + e.getMessage());
        }
    }
    private void runAction() throws IOException {
        BlockingQueue<Mat>frameQ = new ArrayBlockingQueue<>(5);
        BlockingQueue<ConfigCommand> producerCmdQ = new ArrayBlockingQueue<>(5);
        CameraProducerLocal producer = new CameraProducerLocal(frameQ, producerCmdQ);
        Thread producerThread = new Thread(producer);
        Mat frame;
        boolean running = true;
        
        producerThread.start();
        
        // TODO can I do this without the cast?
        SSLServerSocketFactory sslfac = (SSLServerSocketFactory)SSLServerSocketFactory.getDefault();
        SSLServerSocket serversock;
        SSLSocket sock;
        serversock = (SSLServerSocket)sslfac.createServerSocket(localPort);
            //sock = (SSLSocket)serversock.accept(); // BLOCKS

        
        System.out.println("Server address: " + serversock.getInetAddress());
        System.out.println("Port: " + serversock.getLocalPort());
        String[] protocols = serversock.getEnabledProtocols();
        System.out.println("SSL Enabled Protocols:");
        for (String proto : protocols) {
            System.out.println(proto);
        }
        String[] suites = serversock.getSupportedCipherSuites();
        
        /*
        System.out.println("Supported Cipher Suites:");
        for (String suite : suites) {
            System.out.println(suite);
        }
        */
        
        while(running) {
            try {
                frame = frameQ.take();
                // TODO pass out socket
                
            } catch (InterruptedException e) {
                running = false;
                System.out.println("Thread " + Thread.currentThread().getName() + " Quitting, interrupted from Queue take");
            }
        }
        // teardown
        producerThread.interrupt();
    }
}
