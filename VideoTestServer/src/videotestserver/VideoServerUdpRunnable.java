
package videotestserver;

import cameraops.ConfigCommand;
import cameraops.CameraProducerLocal;
import java.io.IOException;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

/**
 *
 * @author pjsanfil
 */
public class VideoServerUdpRunnable implements Runnable {
    private final int localPort;
    private final int destPort;
    
    public VideoServerUdpRunnable(int localPort, int destPort) {
        this.localPort = localPort;
        this.destPort = destPort;
    }
    
    @Override
    public void run() {
        System.out.println("Running Server Thread: " + Thread.currentThread().getName());
        try {
            runAction();
        } catch (IOException e) {
            System.out.println("Quitting video server, error: " + e.getMessage());
        }
    }
    
    
    private void runAction() throws UnknownHostException, SocketException, IOException {    
        // TODO TEST ONLY, SEND THESE VIA CONSTRUCTOR
        BlockingQueue<Mat>frameQ = new ArrayBlockingQueue<>(5);
        BlockingQueue<ConfigCommand> producerCmdQ = new ArrayBlockingQueue<>(5);
        CameraProducerLocal producer = new CameraProducerLocal(frameQ, producerCmdQ);
        Thread producerThread = new Thread(producer);
        Mat frame;
        producerThread.start();
        // END TEST CODE
        
        boolean running = true;
        // TODO replace IP Address with whatever is correct way to get this
        InetAddress destIPAddress = InetAddress.getByName("localhost");
        DatagramSocket socket = new DatagramSocket(localPort);

        while(running) {
            try {
                frame = frameQ.take();
                // TODO pass out socket
                MatOfByte byteMat = new MatOfByte();
                // converts MAT format frame to bmp format for conversion to Image for GUI
                Imgcodecs.imencode(".bmp", frame, byteMat);
                byte [] sendData = byteMat.toArray();
                DatagramPacket packet = new DatagramPacket(sendData, sendData.length, destIPAddress, destPort);
                System.out.println("sending packet of length " + sendData.length);
                socket.send(packet);
            } catch (InterruptedException e) {
                running = false;
                System.out.println("Thread " + Thread.currentThread().getName() + " Quitting, interrupted from Queue take");
            }
        }
        // teardown
        producerThread.interrupt(); 
    }
}
