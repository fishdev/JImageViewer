JImageViewer
============

JImageViewer is a simple Java application designed to easily view, manipulate, and organize images. It was created with performance and easy of use in mind, so important functionality is displayed front and center and the entire interface is carefully crafted to be extremely intuitive. JImageViewer utilizes native technologies for all of its core functionality, but uses specialized functions to efficiently perform repetitive image tasks.

Although JImageViewer is very straightforward, it includes several useful features that allow the user to continue with their routine image-related activities. These include dynamic album creation, multiple views, basic search, and a variety of quick editing tools. In addition, JImageViewer is optimized for the latest hardware and integrated deeply with the user's operating system. The application fully supports Retina/HiDPI screens, multitouch displays and trackpads, and uses the default desktop theme and widget style.

The project is still in active development, so new features are on the way and some aspects of JImageViewer are not complete. Feel free to [contribute](https://github.com/fishdev/JImageViewer/pulls) to JImageViewer right here on GitHub or try it out and submit new [issues](https://github.com/fishdev/JImageViewer/issues) for features you'd like to see in the application.

### Features

JImageViewer offers tons of exciting and practical features:
* Simple, easy to use interface
* Two distinct viewing modes
* Multithreaded image loading
* Multitouch and HiDPI/Retina optimization
* Support for multiple image directories
* Compatibility with native photo management technologies
* Operating system theme integration
* Dynamic photo album creation
* Information about images and albums
* Basic editing capabilities
* Customizable slideshow mode
* Wikipedia metadata integration

### Installation

The files provided here are the only the source files for JImageViewer. To run the application, you can compile them yourself or create an executable JAR package by following the steps below:
1. In a terminal, enter the directory of the JImageViewer source files and run `javac JImageViewer.java`. If you just want to run the application for testing purposes, use the command `java JImageViewer` and stop after that. Continue if you want to use JImageViewer as a full-fledged app on your system.
2. Run `jar cvfe JImageViewer.jar JImageViewer *.class`. The output that follows is normal.
3. Launch JImageViewer with `java -jar JImageViewer.jar` or mark the file as executable and double-click on it.
That's it! By default, JImageViewer reads your normal Pictures directory, but you can set a new location in the preferences dialog.

### More Information

To learn more about how to install, use, and contribute to JImageViewer, please see our [wiki](https://github.com/fishdev/JImageViewer/wiki).
