import java.awt.Color;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

public class Fullscreen extends JFrame {
  private static final long serialVersionUID = 1L;
  JLabel fullImage = new JLabel();

  public Fullscreen(final File file) {
    super("JImageViewer: " + file.getName());
    getContentPane().setPreferredSize(Toolkit.getDefaultToolkit().getScreenSize());
    pack();
    setResizable(false);
    setVisible(true);

    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        Point p = new Point(0, 0);
        SwingUtilities.convertPointToScreen(p, getContentPane());
        Point l = getLocation();
        l.x -= p.x;
        l.y -= p.y;
        setLocation(l);
        
        final JPanel slidePanel = new JPanel();
        add(slidePanel);
        Action escape = new AbstractAction() {
          private static final long serialVersionUID = 1L;

          public void actionPerformed(ActionEvent e) {
            setVisible(false);
            dispose();
          }
        };
        slidePanel.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "escape");
        slidePanel.getActionMap().put("escape", escape);
        slidePanel.setBackground(Color.BLACK);
        
        new Thread(new Runnable() {
          public void run() {
            try {
              BufferedImage srcImage = ImageIO.read(file);
              double width = srcImage.getWidth();
              double height = srcImage.getHeight();
              double m = 0;
              if(width>height) {
                m = Toolkit.getDefaultToolkit().getScreenSize().height / height;
                width = width * m;
                height = Toolkit.getDefaultToolkit().getScreenSize().height;
              } else if(width<=height) {
                m = Toolkit.getDefaultToolkit().getScreenSize().width / width;
                width = Toolkit.getDefaultToolkit().getScreenSize().width;
                height = height * m;
              }
              
              Image image = Toolkit.getDefaultToolkit().getImage(file.getAbsolutePath());
              Image scaledImage = image.getScaledInstance((int)width, (int)height, Image.SCALE_SMOOTH);
              ImageIcon imageIcon = new ImageIcon(scaledImage);
              fullImage.setIcon(imageIcon);
            } catch(IOException e) {
              JOptionPane.showMessageDialog(null, "Error displaying image", "Slideshow", JOptionPane.ERROR_MESSAGE);
            }
          }
        }).start();
        
        slidePanel.add(fullImage);
      }
    });
  }
}
