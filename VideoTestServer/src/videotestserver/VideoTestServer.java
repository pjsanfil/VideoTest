/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package videotestserver;

import org.opencv.core.Core;

/**
 *
 * @author pjsanfil
 */
public class VideoTestServer {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("usage: VideoTestServer <local port> <dest port>");
            return;
        }
        int localPort = Integer.parseInt(args[0]);
        int destPort = Integer.parseInt(args[1]);
        System.out.println("local port: " + localPort +" dest port: " + destPort);
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        //VideoServerUdpRunnable server = new VideoServerUdpRunnable(localPort, destPort);
        VideoServerSSLRunnable server = new VideoServerSSLRunnable(localPort, destPort);
        Thread serverThread = new Thread(server);
        serverThread.start();
        try {
            serverThread.join();
        } catch (InterruptedException e) {
            System.out.println("Terminating VideoServer due to interrupt: " + e);
        }
    }
    
}
