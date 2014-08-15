import java.awt.Color;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

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
import javax.swing.Timer;

public class Slideshow extends JFrame {
  private static final long serialVersionUID = 1L;
  Timer timer;
  int i = 0;
  boolean paused = false;

  public Slideshow(final ArrayList<File> fileIndex, final String pause) {
    super("JImageViewer Slideshow");
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
        
        JLabel loading = loading = new JLabel("Loading...");
        slidePanel.add(loading);
        
        final ArrayList<JLabel> slides = new ArrayList<JLabel>();
        new Thread(new Runnable() {
          public void run() {
            for(final File file : fileIndex) {
              if(!file.isDirectory()) {
                try {
                  BufferedImage srcImage = ImageIO.read(file);
                  double width = srcImage.getWidth();
                  double height = srcImage.getHeight();
                  double m = 0;
                  m = Toolkit.getDefaultToolkit().getScreenSize().height / height;
                  width = width * m;
                  height = Toolkit.getDefaultToolkit().getScreenSize().height;
                  
                  Image image = Toolkit.getDefaultToolkit().getImage(file.getAbsolutePath());
                  Image scaledImage = image.getScaledInstance((int)width, (int)height, Image.SCALE_SMOOTH);
                  ImageIcon imageIcon = new ImageIcon(scaledImage);
                  final JLabel labelImage = new JLabel(imageIcon);
                  slides.add(labelImage);
                } catch(IOException e) {
                  JOptionPane.showMessageDialog(null, "Error displaying image", "Slideshow", JOptionPane.ERROR_MESSAGE);
                }
              }
            }
          }
        }).start();
        
        ActionListener displayslide = new ActionListener() {
          public void actionPerformed(ActionEvent arg0) {
            slide(slidePanel, slides);
          }
        };
        Action previousslide = new AbstractAction() {
          private static final long serialVersionUID = 1L;

          public void actionPerformed(ActionEvent e) {
            i = i - 2;
            if(i>0) {
              slide(slidePanel, slides);
            }
          }
        };
        Action nextslide = new AbstractAction() {
          private static final long serialVersionUID = 1L;
          
          public void actionPerformed(ActionEvent e) {
            i++;
            if(i<slides.size()) {
              slide(slidePanel, slides);
            }
          }
        };
        Action pauseslide = new AbstractAction() {
          private static final long serialVersionUID = 1L;
          
          public void actionPerformed(ActionEvent e) {
            if(paused) {
              paused = false;
              timer.start();
            } else {
              paused = true;
              timer.stop();
            }
          }
        };
        timer = new Timer(Integer.parseInt(pause)*1000, displayslide);
        timer.setRepeats(true);
        
        slidePanel.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "down");
        slidePanel.getActionMap().put("down", previousslide);
        slidePanel.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "up");
        slidePanel.getActionMap().put("up", nextslide);
        slidePanel.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "left");
        slidePanel.getActionMap().put("left", previousslide);
        slidePanel.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "right");
        slidePanel.getActionMap().put("right", nextslide);
        slidePanel.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "space");
        slidePanel.getActionMap().put("space", pauseslide);
        
        timer.start();
      }
    });
  }
  
  public void slide(JPanel slidePanel, ArrayList<JLabel> slides) {
    if(i==slides.size()) {
      timer.stop();
      dispose();
    } else {
      slidePanel.removeAll();
      slidePanel.add(slides.get(i));
      slidePanel.revalidate();
      slidePanel.repaint();
      i++;
    }
  }
}
