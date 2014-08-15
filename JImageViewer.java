import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;


public class JImageViewer {
  public static String toDraw;
  static JFrame window = null;
  static JMenuBar menubar = new JMenuBar();
  
  public static FileOutputStream populateOptions(FileOutputStream fileOut) {
    PrintStream fileStream = new PrintStream(fileOut);
    fileStream.println("<config>");
    fileStream.println("  <option>");
    fileStream.println("    <name>showwelcome</name>");
    fileStream.println("    <value>true</value>");
    fileStream.println("  </option>");
    fileStream.println("  <option>");
    fileStream.println("    <name>defaultview</name>");
    fileStream.println("    <value>grid</value>");
    fileStream.println("  </option>");
    fileStream.println("  <option>");
    fileStream.println("    <name>showtoolbar</name>");
    fileStream.println("    <value>true</value>");
    fileStream.println("  </option>");
    fileStream.println("  <option>");
    fileStream.println("    <name>sourcedirectory</name>");
    fileStream.println("    <value>" + System.getProperty("user.home") + "/Pictures</value>");
    fileStream.println("  </option>");
    fileStream.println("  <option>");
    fileStream.println("    <name>showinfo</name>");
    fileStream.println("    <value>true</value>");
    fileStream.println("  </option>");
    fileStream.println("  <option>");
    fileStream.println("    <name>recentlyused</name>");
    fileStream.println("    <value>" + System.getProperty("user.home") + "/Pictures" + "</value>");
    fileStream.println("  </option>");
    fileStream.println("  <option>");
    fileStream.println("    <name>slideshowdelay</name>");
    fileStream.println("    <value>5</value>");
    fileStream.println("  </option>");
    fileStream.println("</config>");

    return fileOut;
  }
  
  public static void main(String args[]) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        window = new JFrame();
        window.setTitle("JImageViewer");
        window.setSize(1440, 900);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        WindowPanel panel = null;
        
        try {
          UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
          window.setIconImage(ImageIO.read(new File("resources/icon.png")));
          panel = new WindowPanel("start");
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException | ParserConfigurationException | SAXException | IOException e1) {
          JOptionPane.showMessageDialog(null, "Error launching JImageViewer", "Fatal Error", JOptionPane.ERROR_MESSAGE);
          e1.printStackTrace();
        }
        
        window.add(panel);
        window.setVisible(true);
      }
    });
  }
}
