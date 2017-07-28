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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.opencv.core.Mat;

public class CameraProducerLocalTest {
    
    public CameraProducerLocalTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        OpenCvLibHandler.loadLib();
    }
    
    @After
    public void tearDown() {
    }
    
    @Test
    public void testRun() {
        System.out.println("run");
        ArrayBlockingQueue<Mat> frameQ = new ArrayBlockingQueue<>(1);
        ArrayBlockingQueue<ConfigCommand> cmdQ = new ArrayBlockingQueue<>(1);
        CameraProducerLocal instance = new CameraProducerLocal(frameQ, cmdQ);
        instance.run();
        instance.stopCapture();
    }

    /**
     * Test of getFrame method, of class CameraProducerLocal.
     */
    @Test
    public void testGetFrame() {
        System.out.println("getFrame");
        ArrayBlockingQueue<Mat> frameQ = new ArrayBlockingQueue<>(1);
        ArrayBlockingQueue<ConfigCommand> cmdQ = new ArrayBlockingQueue<>(1);
        CameraProducerLocal instance = new CameraProducerLocal(frameQ, cmdQ);
        for (int i = 0; i < 90; i++) {
            Mat result = instance.getFrame();
            assertTrue(result != null);
        }
        instance.stopCapture();
    }

    /**
     * Test of newThread method, of class CameraProducerLocal.
     */
    @Test
    public void testNewThread() {
        System.out.println("newThread");
        Runnable r = null;
        CameraProducerLocal instance = null;
        Thread expResult = null;
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of handleCommand method, of class CameraProducerLocal.
     */
    @Test
    public void testHandleCommand() {
        System.out.println("handleCommand");
        ConfigCommand cmd = null;
        CameraProducerLocal instance = null;
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
