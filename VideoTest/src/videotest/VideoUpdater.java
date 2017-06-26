/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package videotest;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Object to update the video in the GUI.
 * @author pjsanfil
 */
public class VideoUpdater implements Runnable {
    private Image m_frame;
    private ImageView m_imgView;
    
    public VideoUpdater(ImageView toUpdate) {
        m_imgView = toUpdate;
    }
    
    public void setFrame(Image vidframe) {
        m_frame = vidframe;
        // causes this Runnable to be executed on the JavaFX Application Thread
        Platform.runLater(this);
    }
    
    @Override
    public void run() {
        m_imgView.setImage(m_frame);
        //System.out.println("Updating image on thread " + Thread.currentThread().getName());
    }
}
