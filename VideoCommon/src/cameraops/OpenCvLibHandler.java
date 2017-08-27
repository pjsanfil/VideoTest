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

import java.io.File;
import org.opencv.core.Core;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Misc library handling capabilities for OpenCV.
 * 
 * without loading the actual OpenCV native library you get a wierd
 * Exception at runtime when your first OpenCV call occurs, it is a
 * java.lang.reflect.InvocationTargetException 
 */
public class OpenCvLibHandler {
    private static final AtomicBoolean libLoaded = new AtomicBoolean(false);
    /**
     * Loads the OpenCV shared object library. Will only load 1 instance of the
     * library regardless of how many times it is called.
     */
    public static void loadLib() {
        if (!libLoaded.getAndSet(true)) {
            // TODO fix this mess to find the library the correct way
            File selfInstalledLib = new File("/usr/local/share/OpenCV/java/libopencv_java330.so");
            //File selfInstalledLib = new File("/home/pjsanfil/Projects/opencv-3.3.0/build/lib/libopencv_java330.so");
            if (selfInstalledLib.exists()) {
                System.load(selfInstalledLib.getAbsolutePath());
            } else {
                //System.setProperty("java.library.path", System.getProperty("java.library.path") + ":/usr/local/share/OpenCV/java");
                //System.out.println("Java.library.path: " + System.getProperty("java.library.path"));
                //System.loadLibrary("libopencv_java330.so");
                // TODO bug where this doesn't seem to have the correct value
                // in it in OpenCV 3.3 on Linux
                System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            }
        }
    }
}
