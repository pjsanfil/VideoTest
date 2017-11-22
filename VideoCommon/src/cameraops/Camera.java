/*
 * Copyright (C) 2017 pjsanfil
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cameraops;

import java.io.IOException;
import java.util.Arrays;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.VideoWriter;
import org.opencv.videoio.Videoio;

/**
 * Provides camera config settings for the webcam or other attached camera getting
 * a video stream.
 */
public final class Camera {
    private final VideoCapture m_camera;
    
    public Camera() {
        m_camera = new VideoCapture(); // comes from org.opencv.highgui.VideoCapture;
        m_camera.open(0);
        setResolution(800, 600);
    }
    
    public void open() throws IOException {
        m_camera.open(0);
        if (!m_camera.isOpened()) {
            throw new IOException ("Cannot open camera");
        }
    }
    
    public void close() {
        if (m_camera.isOpened()) {
            m_camera.release();
        }
    }
    
    public Mat getFrame() {
        Mat frame = new Mat();
        m_camera.read(frame);
        return frame;
    }
    
    public void setCodec(String codec) throws IllegalArgumentException {
        if (codec.length() != 4) {
            throw new IllegalArgumentException("codec parameter of improper length, must be 4 characters");
        }
        char [] co = codec.toCharArray();
        int fourcc = VideoWriter.fourcc(co[0], co[1], co[2], co[3]);
        System.out.println("Setting codec to: " + Arrays.toString(co)); // DEBUG
        m_camera.set(Videoio.CAP_PROP_FOURCC, fourcc);
        setResolution(800, 600); // DEBUG (maybe) needs to be done after setting codec
    }
    
    public void setResolution(double width, double height) {
        System.out.println("Camera Resolution:  " + m_camera.get(Videoio.CV_CAP_PROP_FRAME_WIDTH) + " x " 
                + m_camera.get(Videoio.CV_CAP_PROP_FRAME_HEIGHT));
        // for some reason these 2 functions return false even when they work
        System.out.println("Setting to " + width + " x " + height);
        m_camera.set(Videoio.CV_CAP_PROP_FRAME_WIDTH, width);
        m_camera.set(Videoio.CV_CAP_PROP_FRAME_HEIGHT, height);
        double newwidth = m_camera.get(Videoio.CV_CAP_PROP_FRAME_WIDTH);
        double newheight = m_camera.get(Videoio.CV_CAP_PROP_FRAME_HEIGHT);
        System.out.println("Camera Resolution: " + newwidth + " x " + newheight);
    }
    
    public String getCodec() {
        // returns a double but actually wants to be an int
        int codecD = (int)m_camera.get(Videoio.CAP_PROP_FOURCC);
        char [] codec = new char[4];
        codec[0] = (char)(codecD & 0xff);
        codec[1] = (char)((codecD >> 8) & 0xff);
        codec[2] = (char)((codecD >> 16) & 0xff);
        codec[3] = (char)((codecD >> 24) & 0xff);
        return String.valueOf(codec);
    }
    
    public int [] getFrameWidthHeight() {
        int [] wxh = new int[2];
        wxh[0] = (int)m_camera.get(Videoio.CAP_PROP_FRAME_WIDTH);
        wxh[1] = (int)m_camera.get(Videoio.CAP_PROP_FRAME_HEIGHT);
        return wxh;
    }
}
