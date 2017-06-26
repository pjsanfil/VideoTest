/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package videotestserver;

import cameraops.ConfigCommand;
import cameraops.CameraProducer;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import javax.net.ssl.*;
import java.io.IOException;

import org.opencv.core.Mat;

/**
 *
 * @author pjsanfil
 */
public class VideoServerUdpRunnable implements Runnable {
    private final int port;
    public VideoServerUdpRunnable(int port) {
        this.port = port;
    }
    
    @Override
    public void run() {
        System.out.println("Running Server Thread: " + Thread.currentThread().getName());
        BlockingQueue<Mat>frameQ = new ArrayBlockingQueue<>(5);
        BlockingQueue<ConfigCommand> producerCmdQ = new ArrayBlockingQueue<>(5);
        CameraProducer producer = new CameraProducer(frameQ, producerCmdQ);
        Thread producerThread = new Thread(producer);
        Mat frame;
        boolean running = true;
        
        producerThread.start();
        
        // TODO can I do this without the cast?
        SSLServerSocketFactory sslfac = (SSLServerSocketFactory)SSLServerSocketFactory.getDefault();
        SSLServerSocket serversock;
        SSLSocket sock;
        try {
            serversock = (SSLServerSocket)sslfac.createServerSocket(port);
            //sock = (SSLSocket)serversock.accept(); // BLOCKS
        } catch (IOException e) {
            System.out.println("SSL Server socket creation failed: " + e);
            return;
        }
        
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
