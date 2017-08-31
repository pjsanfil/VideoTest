# VideoTest
This is a webcam viewing program. It is written in Java using JavaFX and using 
the OpenCV library to get webcam images and do image processing on them.

# Dependancies
1. JRE 1.8 (Java SE 8 or higher)
2. OpenCV Java Library 3.3 (3.0 or higher)
   See [OpenCV 3.3](#opencv-3.3) for instructions.

# Building and Running
1. Download the source code.
2. In VideoTest/VideoCommon run: `ant jar`
3. In VideoTest/VideoTest run: `ant jar`
4. In VideoTest/VideoTest run: `java -jar VideoTest.jar`

# Bugs (running against OpenCV 2.4)
NOTE: Currently the code is written to run against OpenCV 3.0 or higher and will
not work with OpenCV 2.4 which is the default in most Linux Distros.

Camera Resolution: 800.0 x 600.0
select: Bad file descriptor
Capture stopped
Missed frame count = 6
VIDIOC_DQBUF: Bad file descriptor
select: Bad file descriptor
VIDIOC_DQBUF: Bad file descriptor
OpenCV Error: Assertion failed (scn == 3 || scn == 4) in cvtColor, file /build/opencv-SviWsf/opencv-2.4.9.1+dfsg/modules/imgproc/src/color.cpp, line 3737
Exception in thread "Camera-Consumer-1" CvException [org.opencv.core.CvException: cv::Exception: /build/opencv-SviWsf/opencv-2.4.9.1+dfsg/modules/imgproc/src/color.cpp:3737: error: (-215) scn == 3 || scn == 4 in function cvtColor
]
	at org.opencv.imgproc.Imgproc.cvtColor_1(Native Method)
	at org.opencv.imgproc.Imgproc.cvtColor(Imgproc.java:4598)
	at videotest.CameraConsumer.run(CameraConsumer.java:77)
	at java.lang.Thread.run(Thread.java:748)

# OpenCV 3.3
## Building OpenCV
Modification of procedures from [OpenCV Java Webpage](http://docs.opencv.org/2.4/doc/tutorials/introduction/desktop_java/java_dev_intro.html).
```bash
cd opencv
mkdir build
cd build
ccmake ..
```
Configure the settings shown in the [next section](#cmake-config-for-opencv-3.3).

```bash
make -j8
execstack -c lib/libopencv_java330.so
cp lib/libopencv_java330.so <VideoTest dir>/libs/
cd bin/opencv-330.jar <VideoTest dir>/libs/
```

## CMAKE Config for OpenCV 3.3
`BUILD_SHARED_LIBS=OFF`
`WITH_JPEG=OFF`

## Loading Library in Project
When the project is built, both opencv-330.jar and libopencv_java330.so must 
be available to load. The program loads the .so with System.loadLibrary("bla.so")
or System.load("/path/to/bla.so")
The build process does not move the .so file to the correct location and putting
the .so in the jar is only possible if you write some code to extract it before
loading it.

## Bugs (OpenCV 3.3)
### Extraneous bytes before marker repeat warning during execution
- [bug filed here](https://github.com/opencv/opencv/issues/9477)
- In OpenCV 3.3 reading from the camera results in continuous messages like these
being printed out:
Corrupt JPEG data: 2 extraneous bytes before marker 0xd3
Corrupt JPEG data: 1 extraneous bytes before marker 0xd3
Corrupt JPEG data: 1 extraneous bytes before marker 0xd5
Corrupt JPEG data: 2 extraneous bytes before marker 0xd7
Corrupt JPEG data: 1 extraneous bytes before marker 0xd7
Corrupt JPEG data: 1 extraneous bytes before marker 0xd5
Corrupt JPEG data: 1 extraneous bytes before marker 0xd5
Corrupt JPEG data: 2 extraneous bytes before marker 0xd1
Corrupt JPEG data: 2 extraneous bytes before marker 0xd2
Corrupt JPEG data: 1 extraneous bytes before marker 0xd5

--> Fixed by compiling OpenCv with `WITH_JPEG=OFF` setting.
--> Fixed by pull request [9479](https://github.com/opencv/opencv/pull/9479)

## Compile bug
Scanning dependencies of target opencv_dnn
In file included from /home/pjsanfil/Projects/opencv-3.3.0/modules/imgcodecs/src/grfmts.hpp:53:0,
                 from /home/pjsanfil/Projects/opencv-3.3.0/modules/imgcodecs/src/loadsave.cpp:47:
/home/pjsanfil/Projects/opencv-3.3.0/modules/imgcodecs/src/grfmt_exr.hpp:52:31: fatal error: ImfChromaticities.h: No such file or directory
compilation terminated.

### Stack guard warning during startup
OpenJDK 64-Bit Server VM warning: You have loaded library /home/pjsanfil/Projects/VideoTest/libs/libopencv_java330.so which might have disabled stack guard. The VM will try to fix the stack guard now.
It's highly recommended that you fix the library with 'execstack -c <libfile>', or link it with '-z noexecstack'.

--> Fixed by adding execstack build step to Building OpenCV instructions.

### Issues loading native library
Core.NATIVE_LIBRARY_NAME appears to resolve to "opencv_java330" when the name should be "libopencv_java330.so"

System.setProperty() to set the java.library.path to search for libopencv_java330.so does not seem to work.


# References for Development

http://docs.opencv.org/2.4/doc/tutorials/introduction/desktop_java/java_dev_intro.html

