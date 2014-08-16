import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.awt.image.ImageObserver;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

class WindowPanel extends JPanel {
  private static final long serialVersionUID = 1L;
  
  String toDraw = null;
  String configPath = ".jimageviewer/config.xml";
  ArrayList<String> options = null;
  ArrayList<File> fileIndex = new ArrayList<File>();
  ArrayList<File> fileIndexBak = new ArrayList<File>();
  String currentImageDirectory;
  int pageStatus = 0;
  int pageStatusBak = 0;
  boolean pageSearch = false;
  boolean toolbarBackDrawn = false;
  
  JScrollPane imageScroller = null;
  JLabel currentDirectory;
  boolean viewGridBool = true;
  private static final ImageObserver ImageObserver = null;
  private int imageX = 0;
  private int imageY = 0;
  
  public ArrayList<String> parseXML(String path) {
    ArrayList<String> options = new ArrayList<String>();
    
    try {
      DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
      Document doc = docBuilder.parse(new File(path));
      doc.getDocumentElement().normalize();
      NodeList optionList = doc.getElementsByTagName("option");

      for(int s=0; s<optionList.getLength() ; s++){
        Node optionNode = optionList.item(s);
        if(optionNode.getNodeType() == Node.ELEMENT_NODE){
            Element optionElement = (Element)optionNode;

            NodeList optionValueList = optionElement.getElementsByTagName("value");
            Element optionValueElement = (Element)optionValueList.item(0);
            NodeList optionValueNodeList = optionValueElement.getChildNodes();
            options.add(((Node)optionValueNodeList.item(0)).getNodeValue().trim());
        }
      }
    } catch(Throwable e) {
      JOptionPane.showMessageDialog(this, "Error retrieving application data.", "Fatal Error", JOptionPane.ERROR_MESSAGE);
      System.exit(1);
    }
    
    return options;
  }
  
  public WindowPanel(String draw) throws ParserConfigurationException, SAXException, IOException {
    toDraw = draw;
    try {
      FileInputStream fileTest = new FileInputStream(configPath);
      fileTest.close();
      options = parseXML(configPath);
      
      if(options.get(0).equals("true")) {
        toDraw = "welcome";
      } else {
        toDraw = "main";
      }
      
    } catch(IOException e0) {
      File directory = new File(".jimageviewer");
      try {
        directory.mkdir();
        
        FileOutputStream fileOut;
        try {
          fileOut = new FileOutputStream(configPath);
          JImageViewer.populateOptions(fileOut);
          fileOut.close();
          options = parseXML(configPath);
          toDraw = "welcome";
        } catch(IOException e1) {
          JOptionPane.showMessageDialog(this, "Error creating application data.", "Fatal Error", JOptionPane.ERROR_MESSAGE);
          System.exit(1);
        }
      } catch(SecurityException e1) {
        JOptionPane.showMessageDialog(this, "Error creating application data.", "Fatal Error", JOptionPane.ERROR_MESSAGE);
        System.exit(1);
      }
    }
    
    switch(toDraw) {
    case "welcome":
      WindowPanel panelWelcome = new WindowPanel("welcome", options);
      this.remove(this);
      this.add(panelWelcome);
      break;
    case "main":
      WindowPanel panelMain = new WindowPanel("main", options);
      this.remove(this);
      this.add(panelMain);
      break;
    }
  }
  
  public WindowPanel(String draw, ArrayList<String> configs) throws IOException {
    super(new GridBagLayout());
    options = configs;
    
    toDraw = draw;
    if(toDraw.equals("welcome")) {
      panelWelcome();
    }
    if(toDraw.equals("main")) {
      panelMain();
    }
  }
  
  public void panelWelcome() throws IOException {
    GridBagConstraints layoutConstraints = new GridBagConstraints();
    
    JLabel icon = new JLabel(getImage("resources/icon.png", 200));
    JLabel title = new JLabel("Welcome to JImageViewer!");
    title.setFont(new Font("Droid Sans", Font.BOLD, 54));
    title.setHorizontalAlignment(SwingConstants.CENTER);
    JLabel intro = new JLabel("<html><br>You are using JImageViewer, the Java Image Viewer. Please select a folder for your images:<br><br></html>");
    intro.setHorizontalAlignment(SwingConstants.CENTER);
    JButton dir = new JButton("Choose another directory");
    final JFileChooser newdir = new JFileChooser();
    newdir.setCurrentDirectory(new File(options.get(3)));
    newdir.setDialogTitle("Open Image Folder");
    newdir.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    
    layoutConstraints.fill = GridBagConstraints.HORIZONTAL;
    layoutConstraints.gridx = 0;
    layoutConstraints.gridy = 0;
    this.add(icon, layoutConstraints);
    layoutConstraints.fill = GridBagConstraints.HORIZONTAL;
    layoutConstraints.gridx = 0;
    layoutConstraints.gridy = 1;
    this.add(title, layoutConstraints);
    layoutConstraints.fill = GridBagConstraints.HORIZONTAL;
    layoutConstraints.gridx = 0;
    layoutConstraints.gridy = 2;
    this.add(intro, layoutConstraints);
    
    String rawRecent = options.get(5);
    final ArrayList<String> recent = new ArrayList<String>();
    int start = 0;
    for(int i=0; i<rawRecent.length(); i++) {
      if(rawRecent.charAt(i) == ',') {
        recent.add(rawRecent.substring(start, i));
        if(i<rawRecent.length()) {
          start = i + 1;
        }
      }
    }
    recent.add(rawRecent.substring(start, rawRecent.length()));
    
    int recentY = 3;
    for(final String currentRecent : recent) {
      final JLabel recentItemIcon = new JLabel(getImage("resources/folder.png", 50));
      recentItemIcon.setHorizontalAlignment(SwingConstants.LEFT);
      JButton recentItemTitle = new JButton(currentRecent);
      recentItemTitle.setFont(new Font("Droid Sans", Font.BOLD, 22));
      recentItemTitle.setHorizontalAlignment(SwingConstants.LEFT);
      recentItemTitle.setBorderPainted(false);
      recentItemTitle.setOpaque(false);
      recentItemTitle.setContentAreaFilled(false);
      recentItemTitle.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent arg0) {
          try {
            writePreference(configPath, 16, "    <value>" + currentRecent + "</value>");
            options = parseXML(configPath);
            panelMain();
          } catch (IOException e) {
            JOptionPane.showMessageDialog(WindowPanel.this, "Error loading folder", "Load Images", JOptionPane.ERROR_MESSAGE);
          }
        }
      });
      
      layoutConstraints.fill = GridBagConstraints.HORIZONTAL;
      layoutConstraints.gridx = 0;
      layoutConstraints.gridy = recentY;
      layoutConstraints.insets = new Insets(0, 300, 0, 0);
      this.add(recentItemIcon, layoutConstraints);
      layoutConstraints.insets = new Insets(0, 375, 0, 0);
      this.add(recentItemTitle, layoutConstraints);
      
      recentY = recentY + 1;
      if(recentY == 7) {
        break;
      }
    }
    
    layoutConstraints.fill = GridBagConstraints.HORIZONTAL;
    layoutConstraints.gridx = 0;
    layoutConstraints.gridy = recentY;
    this.add(new JLabel("<html> <br></html>"), layoutConstraints);
    
    dir.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        int returnVal = newdir.showOpenDialog(WindowPanel.this);
        
        if(returnVal==JFileChooser.APPROVE_OPTION) {
          try {
            writePreference(configPath, 16, "    <value>" + newdir.getSelectedFile().getAbsolutePath() + "</value>");
            if(!recent.contains(newdir.getSelectedFile().getAbsolutePath())) {
              writePreference(configPath, 24, "    <value>" + newdir.getSelectedFile().getAbsolutePath() + "," + options.get(5) + "</value>");
            }
            options = parseXML(configPath);
            panelMain();
          } catch (IOException e) {
            JOptionPane.showMessageDialog(WindowPanel.this, "Error loading folder", "Load Images", JOptionPane.ERROR_MESSAGE);
          }
        }
      }
    });
    layoutConstraints.fill = GridBagConstraints.HORIZONTAL;
    layoutConstraints.gridx = 0;
    layoutConstraints.gridy = recentY + 1;
    layoutConstraints.insets = new Insets(0, 350, 0, 350);
    this.add(dir, layoutConstraints);
    
    JButton preferences = new JButton("Preferences");
    preferences.addActionListener(new ActionListener() { 
      public void actionPerformed(ActionEvent e) {
        popupPreferences();
        options = parseXML(configPath);
        fileIndex = indexFiles(options.get(3));
      }
    });
    layoutConstraints.fill = GridBagConstraints.HORIZONTAL;
    layoutConstraints.gridx = 0;
    layoutConstraints.gridy = recentY + 2;
    layoutConstraints.insets = new Insets(0, 350, 0, 350);
    this.add(preferences, layoutConstraints);
    
    JButton quit = new JButton("Quit");
    quit.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        System.exit(0);
      }
    });
    layoutConstraints.fill = GridBagConstraints.HORIZONTAL;
    layoutConstraints.gridx = 0;
    layoutConstraints.gridy = recentY + 3;
    layoutConstraints.insets = new Insets(0, 350, 0, 350);
    this.add(quit, layoutConstraints);
  }
  
  public void panelMain() throws IOException {
    final JFrame window = JImageViewer.window;
    this.removeAll();
    this.revalidate();
    this.repaint();
    final GridBagConstraints layoutConstraints = new GridBagConstraints();
    
    final JPanel imageContainer = new JPanel();
    final GridBagConstraints imageContainerLayoutConstraints = new GridBagConstraints();
    fileIndex = indexFiles(options.get(3));
    for(File file : fileIndex) {
      fileIndexBak.add(file);
    }
    if(options.get(1).equals("grid")) {
      viewGridBool = true;
    } else {
      viewGridBool = false;
    }
    
    JMenuBar menubar = JImageViewer.menubar;
    JMenu menuFile = new JMenu("File");
    JMenuItem menuFileAlbum = new JMenuItem("New Album");
    JMenuItem menuFileImage = new JMenuItem("New Image");
    JMenuItem menuFileSearch = new JMenuItem("Search");
    JMenuItem menuFileReload = new JMenuItem("Reload");
    JMenuItem menuFileQuit = new JMenuItem("Quit");
    JMenu menuEdit = new JMenu("Edit");
    JMenuItem menuEditPreferences = new JMenuItem("Preferences");
    JMenu menuView = new JMenu("View");
    final JRadioButtonMenuItem menuViewGrid = new JRadioButtonMenuItem("Grid");
    final JRadioButtonMenuItem menuViewList = new JRadioButtonMenuItem("List");
    JMenuItem menuViewSlideshow = new JMenuItem("Slideshow");
    ButtonGroup menuViewGroup = new ButtonGroup();
    final JCheckBoxMenuItem menuViewToolbar = new JCheckBoxMenuItem("Show Toolbar");
    
    final JToolBar toolbar = new JToolBar("ImageViewer Tools");
    JButton toolAlbum = new JButton(getImage("resources/new-album.png", 25));
    JButton toolImage = new JButton(getImage("resources/new-image.png", 25));
    JButton toolSlideshow = new JButton(getImage("resources/slideshow.png", 25));
    final JToggleButton toolViewGrid = new JToggleButton(getImage("resources/grid.png", 25));
    final JToggleButton toolViewList = new JToggleButton(getImage("resources/list.png", 25));
    ButtonGroup toolViewGroup = new ButtonGroup();
    JButton toolReload = new JButton(getImage("resources/reload.png", 25));
    JButton toolSearch = new JButton(getImage("resources/search.png", 25));
    
    ActionListener albumAction = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if(createAlbum()) {
          try {
            fileIndex = indexFiles(currentImageDirectory);
            imageContainer.removeAll();
            drawPage(viewGridBool, !viewGridBool, layoutConstraints, toolbar, imageContainer, imageContainerLayoutConstraints);
          } catch(IOException e1) {
            JOptionPane.showMessageDialog(WindowPanel.this, "Error creating album.", "Create Album", JOptionPane.ERROR_MESSAGE);
          }
        }
      }
    };
    ActionListener imageAction = new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        try {
          if(addImageTo(currentImageDirectory)) {
            fileIndex = indexFiles(currentImageDirectory);
            imageScroller.setViewportView(imageContainer);
            imageContainer.removeAll();
            drawPage(viewGridBool, !viewGridBool, layoutConstraints, toolbar, imageContainer, imageContainerLayoutConstraints);
          }
        } catch(IOException e) {
          JOptionPane.showMessageDialog(WindowPanel.this, "Error adding images.", "Add Images", JOptionPane.ERROR_MESSAGE);
        }
      }
    };
    ActionListener slideshowAction = new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        @SuppressWarnings("unused")
        Slideshow slideFrame = new Slideshow(fileIndex, options.get(6));
      }
    };
    ActionListener gridAction = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        menuViewGrid.setSelected(true);
        menuViewList.setSelected(false);
        toolViewGrid.setSelected(true);
        toolViewList.setSelected(false);
        try {
          drawPage(!viewGridBool, viewGridBool, layoutConstraints, toolbar, imageContainer, imageContainerLayoutConstraints);
          viewGridBool = !viewGridBool;
        } catch (IOException e1) {
          JOptionPane.showMessageDialog(WindowPanel.this, "Error switching to grid view.", "Change View", JOptionPane.ERROR_MESSAGE);
        }
      }
    };
    ActionListener listAction = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        menuViewGrid.setSelected(false);
        menuViewList.setSelected(true);
        toolViewGrid.setSelected(false);
        toolViewList.setSelected(true);
        try {
          drawPage(!viewGridBool, viewGridBool, layoutConstraints, toolbar, imageContainer, imageContainerLayoutConstraints);
          viewGridBool = !viewGridBool;
        } catch (IOException e1) {
          JOptionPane.showMessageDialog(WindowPanel.this, "Error switching to list view.", "Change View", JOptionPane.ERROR_MESSAGE);
        }
      }
    };
    ActionListener reloadAction = new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        try {
          fileIndex = indexFiles(currentImageDirectory);
          drawPage(viewGridBool, !viewGridBool, layoutConstraints, toolbar, imageContainer, imageContainerLayoutConstraints);
        } catch(IOException e) {
          JOptionPane.showMessageDialog(WindowPanel.this, "Error reloading images.", "Reload Images", JOptionPane.ERROR_MESSAGE);
        }
      }
    };
    ActionListener searchAction = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        try {
          fileIndex = doSearch();
          if(fileIndex != null) {
            pageSearch = true;
            imageScroller.setViewportView(imageContainer);
            imageContainer.removeAll();
            drawPage(viewGridBool, !viewGridBool, layoutConstraints, toolbar, imageContainer, imageContainerLayoutConstraints);
          }
        } catch(IOException e1) {
          JOptionPane.showMessageDialog(WindowPanel.this, "Error searching images.", "Search", JOptionPane.ERROR_MESSAGE);
        }
      }
    };
    ActionListener preferencesAction = new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        popupPreferences();
        ArrayList<String> optionsOld = options;
        options = parseXML(configPath);
        
        if(!options.get(1).equals(optionsOld.get(1))) {          
          if(options.get(1).equals("grid")) {
            viewGridBool = true;
            menuViewGrid.setSelected(true);
            menuViewList.setSelected(false);
            toolViewGrid.setSelected(true);
            toolViewList.setSelected(false);
          } else {
            viewGridBool = false;
            menuViewGrid.setSelected(false);
            menuViewList.setSelected(true);
            toolViewGrid.setSelected(false);
            toolViewList.setSelected(true);
          }
          
          imageContainer.removeAll();
          try {
            drawPage(viewGridBool, !viewGridBool, layoutConstraints, toolbar, imageContainer, imageContainerLayoutConstraints);
          } catch (IOException e) {
            JOptionPane.showMessageDialog(WindowPanel.this, "Error saving preferences.", "Preferences", JOptionPane.ERROR_MESSAGE);
          }
        } else if(!options.get(3).equals(optionsOld.get(3))) {
          fileIndex = indexFiles(options.get(3));
          imageContainer.removeAll();
          
          try {
            drawPage(viewGridBool, !viewGridBool, layoutConstraints, toolbar, imageContainer, imageContainerLayoutConstraints);
          } catch (IOException e) {
            JOptionPane.showMessageDialog(WindowPanel.this, "Error saving preferences.", "Preferences", JOptionPane.ERROR_MESSAGE);
          }
        }
      }
    };
    ActionListener toolbarAction = new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        if(menuViewToolbar.isSelected()) {
          add(toolbar, BorderLayout.PAGE_START);
        } else {
          remove(toolbar);
        }
      }
    };
    ActionListener quitAction = new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        System.exit(0);
      }
    };
    
    toolAlbum.setToolTipText("Create New Album");
    toolAlbum.addActionListener(albumAction);
    toolImage.setToolTipText("Add Image");
    toolImage.addActionListener(imageAction);
    toolSlideshow.setToolTipText("Slideshow");
    toolSlideshow.addActionListener(slideshowAction);
    toolViewGrid.setToolTipText("Grid View");
    toolViewGrid.addActionListener(gridAction);
    toolViewList.setToolTipText("List View");
    toolViewList.addActionListener(listAction);
    if(viewGridBool) {
      toolViewGrid.setSelected(true);
    } else {
      toolViewList.setSelected(true);
    }
    toolViewGroup.add(toolViewGrid);
    toolViewGroup.add(toolViewList);
    toolReload.setToolTipText("Reload");
    toolReload.addActionListener(reloadAction);
    toolSearch.setToolTipText("Search Images");
    toolSearch.addActionListener(searchAction);
    
    menuFile.setMnemonic(KeyEvent.VK_F);
    menuFileAlbum.setMnemonic(KeyEvent.VK_A);
    menuFileAlbum.addActionListener(albumAction);
    menuFileImage.setMnemonic(KeyEvent.VK_I);
    menuFileImage.addActionListener(imageAction);
    menuFileSearch.setMnemonic(KeyEvent.VK_S);
    menuFileSearch.addActionListener(searchAction);
    menuFileReload.setMnemonic(KeyEvent.VK_R);
    menuFileReload.addActionListener(reloadAction);
    menuFileQuit.setMnemonic(KeyEvent.VK_Q);
    menuFileQuit.addActionListener(quitAction);
    menuFile.add(menuFileAlbum);
    menuFile.add(menuFileImage);
    menuFile.addSeparator();
    menuFile.add(menuFileSearch);
    menuFile.add(menuFileReload);
    menuFile.addSeparator();
    menuFile.add(menuFileQuit);
    
    menuEdit.setMnemonic(KeyEvent.VK_E);
    menuEditPreferences.setMnemonic(KeyEvent.VK_P);
    menuEditPreferences.addActionListener(preferencesAction);
    menuEdit.add(menuEditPreferences);
    
    menuView.setMnemonic(KeyEvent.VK_V);
    menuViewGrid.setMnemonic(KeyEvent.VK_G);
    menuViewGrid.addActionListener(gridAction);
    menuViewList.setMnemonic(KeyEvent.VK_L);
    menuViewList.addActionListener(listAction);
    if(viewGridBool) {
      menuViewGrid.setSelected(true);
    } else {
      menuViewList.setSelected(true);
    }
    menuViewGroup.add(menuViewGrid);
    menuViewGroup.add(menuViewList);
    menuViewSlideshow.setMnemonic(KeyEvent.VK_S);
    menuViewSlideshow.addActionListener(slideshowAction);
    menuViewToolbar.setMnemonic(KeyEvent.VK_T);
    menuViewToolbar.setSelected(true);
    menuViewToolbar.addActionListener(toolbarAction);
    menuView.add(menuViewGrid);
    menuView.add(menuViewList);
    menuView.addSeparator();
    menuView.add(menuViewSlideshow);
    menuView.add(menuViewToolbar);
    
    menubar.add(menuFile);
    menubar.add(menuEdit);
    menubar.add(menuView);
    window.setJMenuBar(menubar);
    
    toolbar.add(toolAlbum);
    toolbar.addSeparator();
    toolbar.add(toolImage);
    toolbar.addSeparator();
    toolbar.add(toolSlideshow);
    toolbar.add(Box.createHorizontalGlue());
    toolbar.add(toolViewGrid);
    toolbar.add(toolViewList);
    toolbar.addSeparator();
    toolbar.add(toolReload);
    toolbar.addSeparator();
    toolbar.add(toolSearch);
    toolbar.addSeparator();
    toolbar.setPreferredSize(new Dimension(1440, 40));
    layoutConstraints.fill = GridBagConstraints.HORIZONTAL;
    layoutConstraints.gridx = 0;
    layoutConstraints.gridy = 0;
    this.add(toolbar, layoutConstraints);
    
    imageContainer.setLayout(new GridBagLayout());
    currentImageDirectory = options.get(3);
    if(viewGridBool) {
      drawPage(viewGridBool, viewGridBool, layoutConstraints, toolbar, imageContainer, imageContainerLayoutConstraints);
    } else {
      drawPage(!viewGridBool, !viewGridBool, layoutConstraints, toolbar, imageContainer, imageContainerLayoutConstraints);
      drawPage(viewGridBool, !viewGridBool, layoutConstraints, toolbar, imageContainer, imageContainerLayoutConstraints);
    }
    
    imageScroller = new JScrollPane(imageContainer);
    imageScroller.setBorder(null);
    imageScroller.setPreferredSize(new Dimension(1440, 750));
    imageScroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    imageScroller.getHorizontalScrollBar().setUnitIncrement(10);
    imageScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    imageScroller.getVerticalScrollBar().setUnitIncrement(10);
    imageScroller.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
      public void adjustmentValueChanged(AdjustmentEvent e) {
        int scrollValue = imageScroller.getVerticalScrollBar().getValue() + imageScroller.getVerticalScrollBar().getModel().getExtent();
        int scrollMax = imageScroller.getVerticalScrollBar().getMaximum();
        if(scrollValue==scrollMax) {
          if(pageStatus!=fileIndex.size()) {
            try {
              drawPage(viewGridBool, viewGridBool, layoutConstraints, toolbar, imageContainer, imageContainerLayoutConstraints);
              Thread.sleep(200);
            } catch (IOException | InterruptedException e1) {
              
            }
          }
        }
      }
    });
    
    layoutConstraints.fill = GridBagConstraints.HORIZONTAL;
    layoutConstraints.gridx = 0;
    layoutConstraints.gridy = 1;
    this.add(imageScroller, layoutConstraints);
  }
  
  public void drawPage(final boolean grid, boolean prev, final GridBagConstraints layoutConstraints, final JToolBar toolbar, final JPanel imageContainer, final GridBagConstraints imageContainerLayoutConstraints) throws IOException {
    int pageLength = getPageLength(fileIndex.size(), pageStatus);
    if(pageLength==0 && grid==prev) {
      
    } else {
      int pageMax = pageStatus + pageLength;
      int xMax = 0;
      double imageConstant = 0;
      double folderConstant = 0;
      if(!currentImageDirectory.equals(options.get(3)) ||  pageSearch) {
        if(!toolbarBackDrawn) {
          final ArrayList<Component> toolbarBak = new ArrayList<Component>();
          for(int i=0; i<toolbar.getComponentCount(); i++) {
            toolbarBak.add(toolbar.getComponent(i));
          }
          toolbar.removeAll();
          final JMenuBar menubar = JImageViewer.window.getJMenuBar();
          final JMenu menuAlbum = new JMenu("Album");
          if(!pageSearch) {
            menubar.add(menuAlbum);
          }
          JMenuItem menuAlbumInfo = new JMenuItem("Info");
          JMenuItem menuAlbumBack = new JMenuItem("Back");
          JButton toolBack = new JButton(getImage("resources/back.png", 25));
          JButton toolInfo = new JButton(getImage("resources/info.png", 25));
          
          ActionListener infoAction = new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
              final File album = new File(currentImageDirectory);
              final File albumParent = album.getParentFile();
              final JButton albumName = new JButton(album.getName());
              JButton albumDelete = new JButton("Delete Album");
              JLabel albumInfo = new JLabel();
              
              JComponent[] infoObjects = new JComponent[] {
                  new JLabel("Album name:"),
                  albumName,
                  albumDelete,
                  new JSeparator(SwingConstants.HORIZONTAL),
                  albumInfo
              };
              
              ActionListener nameAction = new ActionListener() {
                public void actionPerformed(ActionEvent arg0) {
                  currentImageDirectory = albumParent.getAbsolutePath();
                  String albumNameNew = renameFile(album, "album");
                  if(albumNameNew != null) {
                    albumName.setText(albumNameNew);
                    currentImageDirectory = new File(albumParent.getAbsolutePath() + "/" + albumName.getText()).getAbsolutePath();
                  }
                  
                }
              };
              ActionListener deleteAction = new ActionListener() {
                public void actionPerformed(ActionEvent arg0) {
                  int response = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this album and all of its images?", "Delete Album", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                  if(response == JOptionPane.YES_OPTION) {
                    deleteFile(new File(albumParent.getAbsolutePath() + "/" + albumName.getText()));
                    fileIndex = indexFiles(albumParent.getAbsolutePath());
                  }
                }
              };
              
              albumName.setHorizontalAlignment(SwingConstants.LEFT);
              albumName.setBorderPainted(false);
              albumName.setOpaque(false);
              albumName.setContentAreaFilled(false);
              albumName.addActionListener(nameAction);
              
              albumDelete.addActionListener(deleteAction);
              
              DecimalFormat decimal = new DecimalFormat("#.00");
              albumInfo.setText("<html>Album size: " + decimal.format(getAlbumSize(new File(albumParent.getAbsolutePath() + "/" + albumName.getText())) / 1000000.0) + " MB<br>Contains: " + new File(albumParent.getAbsolutePath() + "/" + albumName.getText()).list().length + " images</html>");
              
              JOptionPane.showMessageDialog(WindowPanel.this, infoObjects, "Album Info", JOptionPane.PLAIN_MESSAGE);
            }
          };
          ActionListener backAction = new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
              try {
                menubar.remove(menuAlbum);
                menubar.revalidate();
                menubar.repaint();
                toolbar.removeAll();
                for(int i=0; i<toolbarBak.size(); i++) {
                  toolbar.add(toolbarBak.get(i));
                }
                pageStatus = (int)pageStatusBak;
                if(pageSearch) {
                  fileIndex = indexFiles(currentImageDirectory);
                  pageSearch = false;
                } else {
                  currentImageDirectory = new File(currentImageDirectory).getParentFile().getAbsolutePath();
                  fileIndex = indexFiles(currentImageDirectory);
                }
                toolbarBackDrawn = false;
                imageScroller.setViewportView(imageContainer);
                imageContainer.removeAll();
                drawPage(viewGridBool, !viewGridBool, layoutConstraints, toolbar, imageContainer, imageContainerLayoutConstraints);
              } catch (IOException e) {
                JOptionPane.showMessageDialog(WindowPanel.this, "Error going back.", "Back", JOptionPane.ERROR_MESSAGE);
              }
            }
          };
          
          menuAlbumInfo.setMnemonic(KeyEvent.VK_I);
          menuAlbumInfo.addActionListener(infoAction);
          menuAlbum.add(menuAlbumInfo);
          
          menuAlbumBack.addActionListener(backAction);
          menuAlbum.add(menuAlbumBack);
          
          toolBack.setToolTipText("Back");
          toolBack.addActionListener(backAction);
          toolbar.add(toolBack);
          toolbar.addSeparator();
          for(int i=0; i<toolbarBak.size(); i++) {
            toolbar.add(toolbarBak.get(i));
          }
          
          toolInfo.setToolTipText("Info");
          toolInfo.addActionListener(infoAction);
          if(!pageSearch) {
            toolbar.add(toolInfo);
            toolbar.addSeparator();
          }
          
          imageContainer.removeAll();
          toolbarBackDrawn = true;
        }
      } else {
        pageStatusBak = (int)pageStatus;
      }
      
      if(grid) {
        xMax = 3;
        imageConstant = 300.0;
        folderConstant = 200.0;
      } else if(!grid) {
        xMax = 4;
        imageConstant = 50.0;
        folderConstant = 40.0;
      }
      if(grid!=prev) {
        pageStatus = 0;
        imageX = 0;
        imageY = 0;
        imageContainer.removeAll();
      }
      
      for(int i=pageStatus; i<pageMax; i++) {
        final int j = i;
        JButton labelImage = null;
        Image srcImage = null;
        if(fileIndex.get(j).isDirectory()) {
          labelImage = new JButton(getImage("resources/folder.png", folderConstant));
          labelImage.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
              try {
                currentImageDirectory = fileIndex.get(j).getAbsolutePath();
                fileIndex = indexFiles(currentImageDirectory);
                pageStatus = 0;
                imageScroller.setViewportView(imageContainer);
                imageContainer.removeAll();
                drawPage(viewGridBool, !viewGridBool, layoutConstraints, toolbar, imageContainer, imageContainerLayoutConstraints);
              } catch (IOException e) {
                
              }
            }
          });
        } else {
          srcImage = ImageIO.read(fileIndex.get(j));
          ImageIcon scaledImage = getImage(fileIndex.get(j).getAbsolutePath(), imageConstant);
          labelImage = new JButton(scaledImage);
          labelImage.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
              try {
                showImage(layoutConstraints, toolbar, imageScroller, imageContainer, imageContainerLayoutConstraints, fileIndex.get(j), j);
              } catch (IOException e1) {
                
              }
            }
          });
        }
        
        labelImage.setToolTipText(fileIndex.get(j).getName());
        labelImage.setPreferredSize(new Dimension((int)imageConstant, (int)imageConstant));
        labelImage.setBorderPainted(false);
        labelImage.setOpaque(false);
        labelImage.setContentAreaFilled(false);
        labelImage.setSize(new Dimension((int)imageConstant, (int)imageConstant));
        
        if(grid) {
          layoutConstraints.gridx = imageX;
          layoutConstraints.gridy = imageY;
          layoutConstraints.insets = new Insets(10, 10, 10, 10);
          imageContainer.add(labelImage, layoutConstraints);
        } else if(!grid) {
          layoutConstraints.gridx = imageX;
          imageX++;
          layoutConstraints.gridy = imageY;
          imageContainer.add(labelImage, layoutConstraints);
          
          JLabel nameImage = new JLabel(removeExtension(fileIndex.get(i).getName()));
          layoutConstraints.gridx = imageX;
          imageX++;
          layoutConstraints.gridy = imageY;
          imageContainer.add(nameImage, layoutConstraints);
          
          layoutConstraints.gridx = imageX;
          imageX++;
          layoutConstraints.gridy = imageY;
          imageContainer.add(new JLabel(" "), layoutConstraints);
          
          layoutConstraints.gridx = imageX;
          imageX++;
          layoutConstraints.gridy = imageY;
          layoutConstraints.gridwidth = 1;
          imageContainer.add(new JLabel(" "), layoutConstraints);
          
          layoutConstraints.gridx = imageX;
          layoutConstraints.gridy = imageY;
          imageContainer.add(new JLabel(" "), layoutConstraints);
          
          if(!fileIndex.get(i).isDirectory()) {
            JLabel resolutionImage = new JLabel(srcImage.getWidth(ImageObserver) + "x" + srcImage.getHeight(ImageObserver) + " pixels");
            layoutConstraints.gridx = imageX;
            imageX++;
            layoutConstraints.gridy = imageY;
            imageContainer.add(resolutionImage, layoutConstraints);
            
            JLabel typeImage = new JLabel(getExtension(fileIndex.get(i).getName()).toUpperCase());
            layoutConstraints.gridx = imageX;
            imageX++;
            layoutConstraints.gridy = imageY;
            layoutConstraints.gridwidth = 1;
            imageContainer.add(typeImage, layoutConstraints);
            
            DecimalFormat decimal = new DecimalFormat("#.00");
            JLabel sizeImage = new JLabel(decimal.format(fileIndex.get(i).length() / 1000000.0) + " MB");
            layoutConstraints.gridx = imageX;
            layoutConstraints.gridy = imageY;
            imageContainer.add(sizeImage, layoutConstraints);
          }
        }
        
        if(imageX>=xMax) {
          imageY++;
              imageX = 0;
            } else {
              imageX++;
            }
      }
      pageStatus = pageMax;
    }
  }
  
  public void showImage(final GridBagConstraints layoutConstraints, final JToolBar toolbar, final JScrollPane imageScroller, final JPanel imageContainer, final GridBagConstraints imageContainerLayoutConstraints,  final File srcFile, final int fileNum) throws IOException {
    final ArrayList<Component> toolbarBak = new ArrayList<Component>();
    for(int i=0; i<toolbar.getComponentCount(); i++) {
      toolbarBak.add(toolbar.getComponent(i));
    }
    toolbar.removeAll();
    
    final JMenuBar menubar = JImageViewer.window.getJMenuBar();
    final JMenu menuImage = new JMenu("Image");
    menubar.add(menuImage);
    JMenuItem menuImageFlip = new JMenuItem("Flip");
    JMenuItem menuImageRotate = new JMenuItem("Rotate");
    JMenuItem menuImageNegative = new JMenuItem("Negative");
    JMenuItem menuImageFullscreen = new JMenuItem("Fullscreen");
    final JCheckBoxMenuItem menuImageInfo = new JCheckBoxMenuItem("Info");
    JMenuItem menuImagePrevious = new JMenuItem("Previous");
    JMenuItem menuImageNext = new JMenuItem("Next");
    JMenuItem menuImageBack = new JMenuItem("Back to Gallery");
    JButton toolBack = new JButton(getImage("resources/home.png", 25));
    JButton toolFlip = new JButton(getImage("resources/flip.png", 25));
    JButton toolRotate = new JButton(getImage("resources/rotate.png", 25));
    JButton toolNegative = new JButton(getImage("resources/negative.png", 25));
    JButton toolFullscreen = new JButton(getImage("resources/fullscreen.png", 25));
    final JToggleButton toolInfo = new JToggleButton(getImage("resources/info.png", 25));
    final JPanel imageViewer = new JPanel(new GridBagLayout());
    final GridBagConstraints imageViewerLayoutConstraints = new GridBagConstraints();
    final ImageIcon scaledImage = getImage(srcFile.getAbsolutePath(), 550.0);
    JLabel labelImage = new JLabel(scaledImage);
    final JPanel imageInfo = showImageInfo(srcFile, scaledImage);
    JButton imagePrev = new JButton(getImage("resources/previous.png", 25));
    JButton imageNext = new JButton(getImage("resources/next.png", 25));
    
    ActionListener backAction = new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        try {
          menubar.remove(menuImage);
          menubar.revalidate();
          JImageViewer.window.setJMenuBar(menubar);
          toolbar.removeAll();
          for(int i=0; i<toolbarBak.size(); i++) {
            toolbar.add(toolbarBak.get(i));
          }
          toolbar.setName("ImageViewer Tools");
          toolbar.revalidate();
          toolbar.repaint();
          imageScroller.setViewportView(imageContainer);
          imageContainer.removeAll();
          drawPage(viewGridBool, !viewGridBool, layoutConstraints, toolbar, imageContainer, imageContainerLayoutConstraints);
        } catch (IOException e) {
          
        }
      }
    };
    ActionListener clickAction = new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        @SuppressWarnings("unused")
        Fullscreen fullscreenImage = new Fullscreen(srcFile);
      }
    };
    ActionListener infoAction = new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        String text = null;
        if(menuImageInfo.isSelected() || toolInfo.isSelected()) {
          menuImageInfo.setSelected(true);
          toolInfo.setSelected(true);
          text = "    <value>true</value>";
          imageViewerLayoutConstraints.fill = GridBagConstraints.BOTH;
          imageViewerLayoutConstraints.anchor = GridBagConstraints.NORTHEAST;
          imageViewerLayoutConstraints.gridx = 3;
          imageViewerLayoutConstraints.gridwidth = 1;
          imageViewerLayoutConstraints.gridy = 0;
          imageViewerLayoutConstraints.gridheight = 3;
          imageViewerLayoutConstraints.insets = new Insets(0, 250, 0, 0);
          imageViewer.add(imageInfo, imageViewerLayoutConstraints);
        } else {
          menuImageInfo.setSelected(false);
          toolInfo.setSelected(false);
          text = "    <value>false</value>";
          imageViewer.remove(imageInfo);
        }
        imageViewer.revalidate();
        imageViewer.repaint();
        try {
          writePreference(configPath, 20, text);
          options = parseXML(configPath);
        } catch (IOException e) {
          JOptionPane.showMessageDialog(WindowPanel.this, "Error changing info panel option.", "Change Option", JOptionPane.ERROR_MESSAGE);
        }
      }
    };
    ActionListener flipAction = new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        try {
          fileIndex = indexFiles(currentImageDirectory);
          BufferedImage image = flipImage((BufferedImage)scaledImage.getImage());
          ImageIO.write(image, getExtension(srcFile.getName()).toLowerCase(), srcFile);
          menubar.remove(menuImage);
          menubar.revalidate();
          JImageViewer.window.setJMenuBar(menubar);
          toolbar.removeAll();
          for(int i=0; i<toolbarBak.size(); i++) {
            toolbar.add(toolbarBak.get(i));
          }
          toolbar.setName("ImageViewer Tools");
          toolbar.revalidate();
          toolbar.repaint();
          showImage(layoutConstraints, toolbar, imageScroller, imageContainer, imageContainerLayoutConstraints, srcFile, fileNum);
        } catch(IOException e) {
          JOptionPane.showMessageDialog(WindowPanel.this, "Error flipping image.", "Flip Image", JOptionPane.ERROR_MESSAGE);
        }
      }
    };
    ActionListener rotateAction = new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        try {
          fileIndex = indexFiles(currentImageDirectory);
          BufferedImage image = rotateImage((BufferedImage)scaledImage.getImage());
          ImageIO.write(image, getExtension(srcFile.getName()).toLowerCase(), srcFile);
          menubar.remove(menuImage);
          menubar.revalidate();
          JImageViewer.window.setJMenuBar(menubar);
          toolbar.removeAll();
          for(int i=0; i<toolbarBak.size(); i++) {
            toolbar.add(toolbarBak.get(i));
          }
          toolbar.setName("ImageViewer Tools");
          toolbar.revalidate();
          toolbar.repaint();
          showImage(layoutConstraints, toolbar, imageScroller, imageContainer, imageContainerLayoutConstraints, srcFile, fileNum);
        } catch(IOException e) {
          JOptionPane.showMessageDialog(WindowPanel.this, "Error rotating image.", "Rotate Image", JOptionPane.ERROR_MESSAGE);
        }
      }
    };
    ActionListener negativeAction = new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        try {
          fileIndex = indexFiles(currentImageDirectory);
          BufferedImage image = negativeImage((BufferedImage)scaledImage.getImage());
          ImageIO.write(image, getExtension(srcFile.getName()).toLowerCase(), srcFile);
          menubar.remove(menuImage);
          menubar.revalidate();
          JImageViewer.window.setJMenuBar(menubar);
          toolbar.removeAll();
          for(int i=0; i<toolbarBak.size(); i++) {
            toolbar.add(toolbarBak.get(i));
          }
          toolbar.setName("ImageViewer Tools");
          toolbar.revalidate();
          toolbar.repaint();
          showImage(layoutConstraints, toolbar, imageScroller, imageContainer, imageContainerLayoutConstraints, srcFile, fileNum);
        } catch(IOException e) {
          JOptionPane.showMessageDialog(WindowPanel.this, "Error inverting the colors in the image.", "Negative of Image", JOptionPane.ERROR_MESSAGE);
        }
      }
    };
    
    menuImage.setMnemonic(KeyEvent.VK_I);
    menuImageFlip.setMnemonic(KeyEvent.VK_F);
    menuImageFlip.addActionListener(flipAction);
    menuImage.add(menuImageFlip);
    menuImageRotate.setMnemonic(KeyEvent.VK_R);
    menuImageRotate.addActionListener(rotateAction);
    menuImage.add(menuImageRotate);
    menuImageNegative.setMnemonic(KeyEvent.VK_N);
    menuImageNegative.addActionListener(negativeAction);
    menuImage.add(menuImageNegative);
    menuImageFullscreen.setMnemonic(KeyEvent.VK_F);
    menuImageFullscreen.addActionListener(clickAction);
    menuImage.add(menuImageFullscreen);
    menuImageInfo.setMnemonic(KeyEvent.VK_I);
    menuImageInfo.addActionListener(infoAction);
    menuImage.add(menuImageInfo);
    menuImage.addSeparator();
    
    toolBack.setToolTipText("Back to Gallery");
    toolBack.addActionListener(backAction);
    toolbar.add(toolBack);
    toolbar.add(Box.createHorizontalGlue());
    
    if((fileNum - 1)>=0) {
      ActionListener imagePrevAction = new ActionListener() {
        public void actionPerformed(ActionEvent arg0) {
          menubar.remove(menuImage);
          menubar.revalidate();
          JToolBar toolbarMain = new JToolBar();
          for(int i=0; i<toolbarBak.size(); i++) {
            toolbarMain.add(toolbarBak.get(i));
          }
          try {
            int minus = 1;
            if(fileIndex.get(fileNum - 1).isDirectory()) {
              minus++;
            }
            showImage(layoutConstraints, toolbarMain, imageScroller, imageContainer, imageContainerLayoutConstraints, fileIndex.get(fileNum - minus), fileNum - minus);
          } catch (IOException e) {
            
          }
        }
      };
      
      imagePrev.setMnemonic(KeyEvent.VK_LEFT);
      imagePrev.addActionListener(imagePrevAction);
      imagePrev.setHorizontalAlignment(SwingConstants.LEFT);
      imageViewerLayoutConstraints.fill = GridBagConstraints.WEST;
      imageViewerLayoutConstraints.gridx = 0;
      imageViewerLayoutConstraints.gridwidth = 1;
      imageViewerLayoutConstraints.gridy = 0;
      imageViewerLayoutConstraints.insets = new Insets(0, 15, 0, 0);
      imageViewer.add(imagePrev, imageViewerLayoutConstraints);
      
      menuImagePrevious.setMnemonic(KeyEvent.VK_LEFT);
      menuImagePrevious.addActionListener(imagePrevAction);
      menuImage.add(menuImagePrevious);
    }
    
    labelImage.setName("Image");
    labelImage.setPreferredSize(getImageSize((double)scaledImage.getIconWidth(), (double)scaledImage.getIconHeight(), 550.0));
    labelImage.setHorizontalAlignment(SwingConstants.CENTER);
    imageViewerLayoutConstraints.fill = GridBagConstraints.CENTER;
    imageViewerLayoutConstraints.gridx = 1;
    imageViewerLayoutConstraints.gridwidth = 1;
    imageViewerLayoutConstraints.gridy = 0;
    imageViewer.add(labelImage, imageViewerLayoutConstraints);
    
    if((fileNum + 1)<fileIndex.size()) {
      ActionListener imageNextAction = new ActionListener() {
        public void actionPerformed(ActionEvent arg0) {
          menubar.remove(menuImage);
          menubar.revalidate();
          JToolBar toolbarMain = new JToolBar();
          for(int i=0; i<toolbarBak.size(); i++) {
            toolbarMain.add(toolbarBak.get(i));
          }
          try {
            int plus = 1;
            if(fileIndex.get(fileNum + 1).isDirectory()) {
              plus++;
            }
            showImage(layoutConstraints, toolbarMain, imageScroller, imageContainer, imageContainerLayoutConstraints, fileIndex.get(fileNum + plus), fileNum + plus);
          } catch (IOException e) {
            
          }
        }
      };
      
      imageNext.setMnemonic(KeyEvent.VK_RIGHT);
      imageNext.addActionListener(imageNextAction);
      imageNext.setHorizontalAlignment(SwingConstants.RIGHT);
      imageViewerLayoutConstraints.fill = GridBagConstraints.EAST;
      imageViewerLayoutConstraints.gridx = 2;
      imageViewerLayoutConstraints.gridwidth = 1;
      imageViewerLayoutConstraints.gridy = 0;
      imageViewerLayoutConstraints.insets = new Insets(0, 15, 0, 0);
      imageViewer.add(imageNext, imageViewerLayoutConstraints);
      
      menuImageNext.setMnemonic(KeyEvent.VK_RIGHT);
      menuImageNext.addActionListener(imageNextAction);
      menuImage.add(menuImageNext);
    }

    menuImageBack.addActionListener(backAction);
    menuImage.add(menuImageBack);
    
    toolFlip.setToolTipText("Flip");
    toolFlip.addActionListener(flipAction);
    toolbar.add(toolFlip);
    toolbar.addSeparator();
    
    toolRotate.setToolTipText("Rotate");
    toolRotate.addActionListener(rotateAction);
    toolbar.add(toolRotate);
    toolbar.addSeparator();
    
    toolNegative.setToolTipText("Negative");
    toolNegative.addActionListener(negativeAction);
    toolbar.add(toolNegative);
    toolbar.addSeparator();
    
    toolFullscreen.setToolTipText("Fullscreen");
    toolFullscreen.addActionListener(clickAction);
    toolbar.add(toolFullscreen);
    toolbar.addSeparator();
    
    toolInfo.setToolTipText("Image Info");
    if(options.get(4).equals("true")) {
      menuImageInfo.setSelected(true);
      toolInfo.setSelected(true);
      imageViewerLayoutConstraints.fill = GridBagConstraints.BOTH;
      imageViewerLayoutConstraints.anchor = GridBagConstraints.NORTHEAST;
      imageViewerLayoutConstraints.gridx = 3;
      imageViewerLayoutConstraints.gridwidth = 1;
      imageViewerLayoutConstraints.gridy = 0;
      imageViewerLayoutConstraints.gridheight = 3;
      imageViewerLayoutConstraints.insets = new Insets(0, 150, 0, 0);
      imageViewer.add(imageInfo, imageViewerLayoutConstraints);
    }
    toolInfo.addActionListener(infoAction);
    toolbar.add(toolInfo);
    toolbar.addSeparator();
    
    imageScroller.setViewportView(imageViewer);
    imageScroller.revalidate();
    imageScroller.repaint();
    
    this.updateUI();
    this.revalidate();
    this.repaint();
  }
  
  public JPanel showImageInfo(final File srcFile, ImageIcon srcImage) throws IOException {
    final JPanel imageInfo = new JPanel(new GridBagLayout());
    GridBagConstraints imageInfoLayoutConstraints = new GridBagConstraints();
    
    ArrayList<String> imageMetadata = getImageMetadata(srcFile.getAbsolutePath());
    JLabel imageInfoTitle = new JLabel("Image Properties\n\n");
    Font font = imageInfoTitle.getFont();
    Font boldFont = new Font(font.getFontName(), Font.BOLD, font.getSize());
    imageInfoTitle.setFont(boldFont);
    imageInfoLayoutConstraints.fill = GridBagConstraints.HORIZONTAL;
    imageInfoLayoutConstraints.gridx = 0;
    imageInfoLayoutConstraints.gridwidth = 2;
    imageInfoLayoutConstraints.gridy = 0;
    imageInfo.add(imageInfoTitle, imageInfoLayoutConstraints);
    
    JLabel imageInfoName = new JLabel("Name:");
    imageInfoLayoutConstraints.fill = GridBagConstraints.HORIZONTAL;
    imageInfoLayoutConstraints.gridx = 0;
    imageInfoLayoutConstraints.gridwidth = 1;
    imageInfoLayoutConstraints.gridy = 1;
    imageInfo.add(imageInfoName, imageInfoLayoutConstraints);
    final JButton imageName = new JButton(removeExtension(srcFile.getName()));
    imageName.setHorizontalAlignment(SwingConstants.LEFT);
    imageName.setMnemonic(KeyEvent.VK_N);
    imageName.setBorderPainted(false);
    imageName.setOpaque(false);
    imageName.setContentAreaFilled(false);
    imageName.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        String srcFileNewPath = renameFile(srcFile, "image");
        if(srcFileNewPath != null) {
          imageName.setText(srcFileNewPath);
        }
      }
    });
    imageInfoLayoutConstraints.fill = GridBagConstraints.HORIZONTAL;
    imageInfoLayoutConstraints.gridx = 1;
    imageInfoLayoutConstraints.gridwidth = 1;
    imageInfoLayoutConstraints.gridy = 1;
    imageInfo.add(imageName, imageInfoLayoutConstraints);
    
    JLabel imageInfoResolution = new JLabel("Resolution:");
    imageInfoLayoutConstraints.fill = GridBagConstraints.HORIZONTAL;
    imageInfoLayoutConstraints.gridx = 0;
    imageInfoLayoutConstraints.gridwidth = 1;
    imageInfoLayoutConstraints.gridy = 2;
    imageInfo.add(imageInfoResolution, imageInfoLayoutConstraints);
    JLabel imageResolution = new JLabel(srcImage.getIconWidth() + "x" + srcImage.getIconHeight() + " pixels");
    imageInfoLayoutConstraints.fill = GridBagConstraints.HORIZONTAL;
    imageInfoLayoutConstraints.gridx = 1;
    imageInfoLayoutConstraints.gridwidth = 1;
    imageInfoLayoutConstraints.gridy = 2;
    imageInfo.add(imageResolution, imageInfoLayoutConstraints);
    
    JLabel imageInfoSize = new JLabel("File Size:");
    imageInfoLayoutConstraints.fill = GridBagConstraints.HORIZONTAL;
    imageInfoLayoutConstraints.gridx = 0;
    imageInfoLayoutConstraints.gridwidth = 1;
    imageInfoLayoutConstraints.gridy = 3;
    imageInfo.add(imageInfoSize, imageInfoLayoutConstraints);
    DecimalFormat decimal = new DecimalFormat("#.00");
    JLabel imageSize = new JLabel(decimal.format(srcFile.length() / 1000000.0) + " MB");
    imageInfoLayoutConstraints.fill = GridBagConstraints.HORIZONTAL;
    imageInfoLayoutConstraints.gridx = 1;
    imageInfoLayoutConstraints.gridwidth = 1;
    imageInfoLayoutConstraints.gridy = 3;
    imageInfo.add(imageSize, imageInfoLayoutConstraints);
    
    JLabel imageInfoType = new JLabel("Type:");
    imageInfoLayoutConstraints.fill = GridBagConstraints.HORIZONTAL;
    imageInfoLayoutConstraints.gridx = 0;
    imageInfoLayoutConstraints.gridwidth = 1;
    imageInfoLayoutConstraints.gridy = 4;
    imageInfo.add(imageInfoType, imageInfoLayoutConstraints);
    JLabel imageType = new JLabel(getExtension(srcFile.getName()).toUpperCase());
    imageInfoLayoutConstraints.fill = GridBagConstraints.HORIZONTAL;
    imageInfoLayoutConstraints.gridx = 1;
    imageInfoLayoutConstraints.gridwidth = 1;
    imageInfoLayoutConstraints.gridy = 4;
    imageInfo.add(imageType, imageInfoLayoutConstraints);
    
    JLabel imageInfoColorspace = new JLabel("Colorspace:");
    imageInfoLayoutConstraints.fill = GridBagConstraints.HORIZONTAL;
    imageInfoLayoutConstraints.gridx = 0;
    imageInfoLayoutConstraints.gridwidth = 1;
    imageInfoLayoutConstraints.gridy = 5;
    imageInfo.add(imageInfoColorspace, imageInfoLayoutConstraints);
    final JButton imageColorspace = new JButton(imageMetadata.get(0));
    imageColorspace.setHorizontalAlignment(SwingConstants.LEFT);
    imageColorspace.setBorderPainted(false);
    imageColorspace.setOpaque(false);
    imageColorspace.setContentAreaFilled(false);
    imageColorspace.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        try {
          Desktop.getDesktop().browse(new URI("http://en.wikipedia.org/wiki/" + imageColorspace.getText()));
        } catch (IOException | URISyntaxException e) {
          JOptionPane.showMessageDialog(WindowPanel.this, "Error opening information about colorspace.", "More Info", JOptionPane.ERROR_MESSAGE);
        }
      }
    });
    imageInfoLayoutConstraints.fill = GridBagConstraints.HORIZONTAL;
    imageInfoLayoutConstraints.gridx = 1;
    imageInfoLayoutConstraints.gridwidth = 1;
    imageInfoLayoutConstraints.gridy = 5;
    imageInfo.add(imageColorspace, imageInfoLayoutConstraints);
    
    JLabel imageInfoCompression = new JLabel("Compression:  ");
    imageInfoLayoutConstraints.fill = GridBagConstraints.HORIZONTAL;
    imageInfoLayoutConstraints.gridx = 0;
    imageInfoLayoutConstraints.gridwidth = 1;
    imageInfoLayoutConstraints.gridy = 6;
    imageInfo.add(imageInfoCompression, imageInfoLayoutConstraints);
    JLabel imageCompression = new JLabel(imageMetadata.get(1));
    if(imageMetadata.get(2).equals("TRUE")) {
      imageCompression.setText("Lossless " + imageCompression.getText());
    } else if(imageMetadata.equals("FALSE")) {
      imageCompression.setText("Lossy " + imageCompression.getText());
    }
    imageInfoLayoutConstraints.fill = GridBagConstraints.HORIZONTAL;
    imageInfoLayoutConstraints.gridx = 1;
    imageInfoLayoutConstraints.gridwidth = 1;
    imageInfoLayoutConstraints.gridy = 6;
    imageInfo.add(imageCompression, imageInfoLayoutConstraints);
    
    JLabel imageInfoOrientation = new JLabel("Orientation:");
    imageInfoLayoutConstraints.fill = GridBagConstraints.HORIZONTAL;
    imageInfoLayoutConstraints.gridx = 0;
    imageInfoLayoutConstraints.gridwidth = 1;
    imageInfoLayoutConstraints.gridy = 7;
    imageInfo.add(imageInfoOrientation, imageInfoLayoutConstraints);
    String orientation = imageMetadata.get(3).substring(0, 1).toUpperCase() + imageMetadata.get(3).substring(1, imageMetadata.get(3).length());
    if(orientation.equals("1.0")) {
      orientation = "Rotated";
    }
    JLabel imageOrientation = new JLabel(orientation);
    imageInfoLayoutConstraints.fill = GridBagConstraints.HORIZONTAL;
    imageInfoLayoutConstraints.gridx = 1;
    imageInfoLayoutConstraints.gridwidth = 1;
    imageInfoLayoutConstraints.gridy = 7;
    imageInfo.add(imageOrientation, imageInfoLayoutConstraints);
    
    JButton imageDelete = new JButton("Delete Image");
    imageDelete.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        int response = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this image?", "Delete Image", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if(response == JOptionPane.YES_OPTION) {
          deleteFile(srcFile);
          fileIndex = indexFiles(currentImageDirectory);
        }
      }
    });
    imageInfoLayoutConstraints.fill = GridBagConstraints.HORIZONTAL;
    imageInfoLayoutConstraints.gridx = 0;
    imageInfoLayoutConstraints.gridwidth = 1;
    imageInfoLayoutConstraints.gridy = 8;
    imageInfo.add(imageDelete, imageInfoLayoutConstraints);
   
    imageInfo.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, Color.GRAY));
    Border border = imageInfo.getBorder();
    Border margin = new EmptyBorder(5, 10, 5, 5);
    imageInfo.setBorder(new CompoundBorder(border, margin));
    
    return imageInfo;
  }
  
  public boolean createAlbum() {
    String albumName = JOptionPane.showInputDialog("Name of album: ");
    if(albumName != null) {
      String locationFile = currentImageDirectory + "/";
      File newAlbum = new File(locationFile + albumName);
      if(newAlbum.exists()) {
        JOptionPane.showMessageDialog(WindowPanel.this, "That album already exists. Use a different name", "Create Album", JOptionPane.ERROR_MESSAGE);
        createAlbum();
      }
      newAlbum.mkdir();
      
      return true;
    } else {
      return false;
    }
  }
  
  public long getAlbumSize(File dir) {
    long length = 0;
    for(File file : dir.listFiles()) {
      if(file.isFile()) {
        length += file.length();
      } else {
        length += getAlbumSize(file);
      }
    }
    return length;
  }
  
  public boolean addImageTo(final String location) {
    final JFileChooser imagesChooser = new JFileChooser();
    JButton imagesChooserButton = new JButton("Select Images");
    final JLabel selectedImages = new JLabel("No images selected.");
    
    JComponent[] addImageObjects = new JComponent[] {
        new JLabel("Select image files to add:"),
        imagesChooserButton,
        selectedImages
    };
    
    imagesChooser.setCurrentDirectory(new File(location));
    imagesChooser.setDialogTitle("Choose Images to Add");
    imagesChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    imagesChooser.setMultiSelectionEnabled(true);
    imagesChooserButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        int returnVal = imagesChooser.showOpenDialog(WindowPanel.this);
        
        if(returnVal==JFileChooser.APPROVE_OPTION) {
          File[] selectedFiles = imagesChooser.getSelectedFiles();
          selectedImages.setText(selectedFiles.length + " images selected.");
          
          for(File currentImage : selectedFiles) {
            try {
              if(!currentImage.getAbsolutePath().equals(location + currentImage.getName())) {
                Files.move(currentImage.toPath(), new File(location + "/" + currentImage.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
              }
            } catch (IOException e) {
              JOptionPane.showMessageDialog(WindowPanel.this, "Error adding images.", "Add Images", JOptionPane.ERROR_MESSAGE);
            }
          }
        }
      }
    });
    
    int n = JOptionPane.showConfirmDialog(WindowPanel.this, addImageObjects, "Add Images", JOptionPane.OK_CANCEL_OPTION);
    if(n == JOptionPane.YES_OPTION) {
      return true;
    } else {
      return false;
    }
  }
  
  public String renameFile(File srcFile, String type) {
    String newPath = JOptionPane.showInputDialog("Rename " + type + ": ");
    if(newPath != null) {
      String locationFile = currentImageDirectory + "/";
      File newFile = null;
      if(type.equals("image")) {
        newFile = new File(locationFile + newPath + "." + getExtension(srcFile.getName()).toLowerCase());
      } else if(type.equals("album")) {
        newFile = new File(locationFile + newPath);
      }
      
      if(newFile.exists()) {
        JOptionPane.showMessageDialog(WindowPanel.this, "That file already exists. Use a different name.", "Rename File", JOptionPane.ERROR_MESSAGE);
        return renameFile(srcFile, type);
      } else {
        srcFile.renameTo(newFile);
        fileIndex = indexFiles(options.get(3));
        return removeExtension(newFile.getName());
      }
    } else {
      return null;
    }
  }
  
  public static boolean deleteFile(File file) {
    if(file.exists()) {
      File[] files = file.listFiles();
      if(files != null) {
        for(File currentFile : files) {
          if(currentFile.isDirectory()) {
            deleteFile(currentFile);
          } else {
            currentFile.delete();
          }
        }
      }
    }
    
    return(file.delete());
  }
  
  public BufferedImage rotateImage(BufferedImage image) {
    AffineTransform transform = new AffineTransform();
    transform.translate(image.getHeight() / 2, image.getWidth() / 2);
    transform.rotate(Math.PI / 2);
    transform.translate(-image.getWidth() / 2, -image.getHeight() / 2);
    
    AffineTransformOp transformExtension = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
    BufferedImage newimage = new BufferedImage(image.getHeight(), image.getWidth(), image.getType());
    image = transformExtension.filter(image, newimage);
    
    return newimage;
  }
  
  public BufferedImage flipImage(BufferedImage image) {
    AffineTransform transform = AffineTransform.getScaleInstance(-1, 1);
    transform.translate(-image.getWidth(null), 0);
    
    AffineTransformOp transformExtension = new AffineTransformOp(transform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
    BufferedImage newimage = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
    image = transformExtension.filter(image, newimage);
    
    return newimage;
  }
  
  public BufferedImage negativeImage(BufferedImage image) {
    int w = image.getWidth();
    int h = image.getHeight();

    BufferedImage newimage = new BufferedImage(w, h, 1);
    int value, alpha, r, g, b;
    for (int i = 0; i < w; i++) {
      for (int j = 0; j < h; j++) {
        value = image.getRGB(i, j);
        alpha = getAlpha(value);
        r = 255 - getRed(value);
        g = 255 - getGreen(value);
        b = 255 - getBlue(value);

        value = createRGB(alpha, r, g, b);
        newimage.setRGB(i, j, value);
      }
    }
    
    return newimage;
  }
  
  public int createRGB(int alpha, int r, int g, int b) {
    int rgb = (alpha << 24) + (r << 16) + (g << 8) + b;
    return rgb;
  }
  
  public int getAlpha(int rgb) {
    return (rgb >> 24) & 0xFF;
  }
  
  public int getRed(int rgb) {
    return (rgb >> 16) & 0xFF;
  }
  
  public int getGreen(int rgb) {
    return (rgb >> 8) & 0xFF;
  }
  
  public int getBlue(int rgb) {
    return rgb & 0xFF;
  }
  
  public ImageIcon getImage(String imagePath, double constant) throws IOException {
    ImageIcon image = null;
    try {
      ExecutorService executor = Executors.newSingleThreadExecutor();
      Future<ImageIcon> future = executor.submit(new ImageThread(imagePath, constant));
      image = future.get();
      executor.shutdown();
    } catch(ExecutionException | InterruptedException e) {
      JOptionPane.showMessageDialog(WindowPanel.this, "Error retrieving image.", "Retrieve Image", JOptionPane.ERROR_MESSAGE);
    }
    return image;
  }
  
  public ArrayList<String> getImageMetadata(String imagePath) throws IOException {
    File image = new File(imagePath);
    ImageInputStream imageInput = ImageIO.createImageInputStream(image);
    Iterator<ImageReader> imageIterator = ImageIO.getImageReaders(imageInput);
    
    if(imageIterator.hasNext()) {
      ImageReader imageReader = imageIterator.next();
      imageReader.setInput(imageInput, true);
      
      IIOMetadata imageMetadata = imageReader.getImageMetadata(0);
      String[] imageData = imageMetadata.getMetadataFormatNames();
      ArrayList<String> imageMetadataFormatted = new ArrayList<String>();
      
      for (int i=0; i<imageData.length; i++) {
        ArrayList<String> nodeData = getImageMetadataNode(imageMetadata.getAsTree(imageData[i]));
        for(String nodeDataString : nodeData) {
          imageMetadataFormatted.add(nodeDataString);
        }
      }
      
      return imageMetadataFormatted;
    } else {
      return null;
    }
  }
  
  public ArrayList<String> getImageMetadataNode(Node node) {
    NamedNodeMap nodeMap = node.getAttributes();
    ArrayList<String> metadataValues = new ArrayList<String>();
    
    if(node.getNodeName().equals("javax_imageio_1.0") || node.getNodeName().equals("Chroma") || node.getNodeName().equals("ColorSpaceType") || node.getNodeName().equals("Compression") || node.getNodeName().equals("CompressionTypeName") || node.getNodeName().equals("Lossless") || node.getNodeName().equals("Dimension") || node.getNodeName().equals("PixelAspectRatio") || node.getNodeName().equals("ImageOrientation") || node.getNodeName().equals("Transparency") || node.getNodeName().equals("Alpha")) {
      if(nodeMap != null) {
        for(int i = 0; i<nodeMap.getLength(); i++) {
          if(node.getNodeName().equals("ColorSpaceType")) {
            metadataValues.add(node.getAttributes().item(0).getNodeValue());
          } else {
            metadataValues.add(nodeMap.item(i).getNodeValue());
          }
        }
      }
      
      NodeList nodeChildren = node.getChildNodes();
      for(int i=0; i<nodeChildren.getLength(); i++) {
        ArrayList<String> nodeChildValues = getImageMetadataNode(nodeChildren.item(i));
        for(String nodeChildValue : nodeChildValues) {
          metadataValues.add(nodeChildValue);
        }
      }
    }
    
    return metadataValues;
  }
  
  public static Dimension getImageSize(double w, double h, double constant) {
    double m = 0;
    if(w>=h) {
      m = constant / w;
    } else if(w<h) {
      m = constant / h;
    }
    w = w * m;
    h = h * m;
    
    Dimension size = new Dimension((int)w, (int)h);
    
    return size;
  }
  
  public int getPageLength(int indexLength, int pageStatus) {
    int pageLength = 0;
    
    if((indexLength-pageStatus)>=4) {
      pageLength = 4;
    } else {
      pageLength = indexLength - pageStatus;
    }
    
    return pageLength;
  }
  
  public ArrayList<File> doSearch() {
    String searchValue = JOptionPane.showInputDialog("Search for: ");
    if(searchValue != null) {
      ArrayList<File> searchIndex = indexSearch(currentImageDirectory, searchValue);
      return searchIndex;
    } else {
      return null;
    }
  }
  
  public ArrayList<File> indexSearch(String searchPath, String searchKey) {
    ArrayList<File> matches = new ArrayList<File>();
    
    for(File searchSubject : indexFiles(searchPath)) {
      if(searchSubject.isDirectory()) {
        for(File searchSubjectSub : indexSearch(searchSubject.getAbsolutePath(), searchKey)) { 
          matches.add(searchSubjectSub);
        }
      } else {
        if(searchSubject.getAbsolutePath().contains(searchKey)) {
          if(searchSubject.getName().charAt(0) != '.') {
            if(searchSubject.isDirectory()) {
              matches.add(searchSubject);
            } else if(URLConnection.guessContentTypeFromName(searchSubject.getName()) != null) {
              if(URLConnection.guessContentTypeFromName(searchSubject.getName()).substring(0, 5).equalsIgnoreCase("image")) {
                matches.add(searchSubject);
              }
            }
          }
        }
      }
    }
    
    return matches;
  }
  
  public ArrayList<File> indexFiles(String sourcedirectory) {
    File srcdir = new File(sourcedirectory);
    File[] files = srcdir.listFiles();
    ArrayList<File> index = new ArrayList<File>();
    
    for(File file : files) {
      if(file.getName().charAt(0) != '.') {
        if(file.isDirectory()) {
          index.add(file);
        } else if(URLConnection.guessContentTypeFromName(file.getName()) != null) {
          if(URLConnection.guessContentTypeFromName(file.getName()).substring(0, 5).equalsIgnoreCase("image")) {
            index.add(file);
          }
        }
      }
    }
    
    return index;
  }
  
  public String getExtension(String filePath) {
    String fileExtension = null;
    
    int i = filePath.lastIndexOf('.');
    if (i >= 0) {
        fileExtension = filePath.substring(i+1);
    }
    
    return fileExtension;
  }
  
  public String removeExtension(String filePath) {
    try {
      return filePath.substring(0, filePath.lastIndexOf('.'));
    } catch(StringIndexOutOfBoundsException e) {
      return filePath;
    }
  }
  
  public void popupPreferences() {
    final JCheckBox showWelcome = new JCheckBox("Show welcome screen on startup");
    ButtonGroup viewGroup = new ButtonGroup();
    final JRadioButton viewGrid = new JRadioButton("Grid");
    final JRadioButton viewList = new JRadioButton("List");
    final JCheckBox showToolbar = new JCheckBox("Show toolbar");
    final JFileChooser dirChooser = new JFileChooser();
    final JButton srcDirectory = new JButton("Change Image Folder");
    currentDirectory = new JLabel("Current folder: " + options.get(3));
    JButton clearRecent = new JButton("Clear Recent Folders");
    JSlider slideshowDelay = new JSlider(JSlider.HORIZONTAL, 1, 20, Integer.parseInt(options.get(6)));
    
    final JComponent[] preferenceObjects = new JComponent[] {
        showWelcome,
        new JSeparator(SwingConstants.HORIZONTAL),
        new JLabel("Default view:"),
        viewGrid,
        viewList,
        new JSeparator(SwingConstants.HORIZONTAL),
        showToolbar,
        new JSeparator(SwingConstants.HORIZONTAL),
        currentDirectory,
        srcDirectory,
        clearRecent,
        new JSeparator(SwingConstants.HORIZONTAL),
        new JLabel("Slideshow delay:"),
        slideshowDelay
    };
    
    showWelcome.setMnemonic(KeyEvent.VK_W);
    showWelcome.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        String text = null;
        if(showWelcome.isSelected()) {
          text = "    <value>true</value>";
        } else {
          text = "    <value>false</value>";
        }
        try {
          writePreference(configPath, 4, text);
        } catch (IOException e) {
          JOptionPane.showMessageDialog(WindowPanel.this, "Error saving option", "Preferences", JOptionPane.ERROR_MESSAGE);
        }
      }
    });
    if(options.get(0).equals("true")) {
      showWelcome.setSelected(true);
    }
    
    viewGrid.setMnemonic(KeyEvent.VK_G);
    viewGrid.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        if(viewGrid.isSelected()) {
          try {
            writePreference(configPath, 8, "    <value>grid</value>");
          } catch (IOException e) {
            JOptionPane.showMessageDialog(WindowPanel.this, "Error saving option", "Preferences", JOptionPane.ERROR_MESSAGE);
          }
        }
      }
    });
    viewList.setMnemonic(KeyEvent.VK_L);
    viewList.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        if(viewList.isSelected()) {
          try {
            writePreference(configPath, 8, "    <value>list</value>");
          } catch (IOException e) {
            JOptionPane.showMessageDialog(WindowPanel.this, "Error saving option", "Preferences", JOptionPane.ERROR_MESSAGE);
          }
        }
      }
    });
    if(viewGridBool) {
      viewGrid.setSelected(true);
    } else {
      viewList.setSelected(true);
    }
    viewGroup.add(viewGrid);
    viewGroup.add(viewList);
    
    showToolbar.setMnemonic(KeyEvent.VK_T);
    showToolbar.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        String text = null;
        if(showToolbar.isSelected()) {
          text = "    <value>true</value>";
        } else {
          text = "    <value>false</value>";
        }
        try {
          writePreference(configPath, 12, text);
        } catch (IOException e) {
          JOptionPane.showMessageDialog(WindowPanel.this, "Error saving option", "Preferences", JOptionPane.ERROR_MESSAGE);
        }
      }
    });
    if(options.get(2).equals("true")) {
      showToolbar.setSelected(true);
    }
    
    dirChooser.setCurrentDirectory(new File(options.get(3)));
    dirChooser.setDialogTitle("Choose an Image Folder");
    dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    srcDirectory.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        int returnVal = dirChooser.showOpenDialog(WindowPanel.this);
        
        if(returnVal==JFileChooser.APPROVE_OPTION) {
          try {
            writePreference(configPath, 16, "    <value>" + dirChooser.getSelectedFile().getAbsolutePath() + "</value>");
            String rawRecent = options.get(5);
            final ArrayList<String> recent = new ArrayList<String>();
            int start = 0;
            for(int i=0; i<rawRecent.length(); i++) {
              if(rawRecent.charAt(i) == ',') {
                recent.add(rawRecent.substring(start, i));
                if(i<rawRecent.length()) {
                  start = i + 1;
                }
              }
            }
            if(!recent.contains(dirChooser.getSelectedFile().getAbsolutePath())) {
              writePreference(configPath, 24, "    <value>" + dirChooser.getSelectedFile().getAbsolutePath() + "," + options.get(5) + "</value>");
            }
            currentImageDirectory = dirChooser.getSelectedFile().getAbsolutePath();
          } catch (IOException e) {
            JOptionPane.showMessageDialog(WindowPanel.this, "Error saving option", "Preferences", JOptionPane.ERROR_MESSAGE);
          }
          currentDirectory.setText("Current folder: " + dirChooser.getSelectedFile().getAbsolutePath());
        }
      }
    });
    
    clearRecent.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        try {
          writePreference(configPath, 24, "    <value>" + currentImageDirectory + "</value>");
          options = parseXML(configPath);
        } catch (IOException e) {
          JOptionPane.showMessageDialog(WindowPanel.this, "Error saving option", "Preferences", JOptionPane.ERROR_MESSAGE);
        }
      }
    });
    
    slideshowDelay.setMajorTickSpacing(5);
    slideshowDelay.setMinorTickSpacing(1);
    slideshowDelay.setPaintTicks(false);
    slideshowDelay.setPaintLabels(false);
    slideshowDelay.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        JSlider source = (JSlider)e.getSource();
        if(!source.getValueIsAdjusting()) {          
          try {
            int value = (int)source.getValue();
            writePreference(configPath, 28, "    <value>" + new String(value + "") + "</value>");
            options = parseXML(configPath);
          } catch (IOException e1) {
            JOptionPane.showMessageDialog(WindowPanel.this, "Error saving option", "Preferences", JOptionPane.ERROR_MESSAGE);
          }
        }
      }
    });
    
    JOptionPane.showMessageDialog(WindowPanel.this, preferenceObjects, "ImageViewer Preferences", JOptionPane.PLAIN_MESSAGE);
  }
  
  public void writePreference(String filePath, int lineNum, String text) throws IOException {
    BufferedReader fileIn = new BufferedReader(new InputStreamReader(new FileInputStream(".jimageviewer/config.xml")));
    File file = new File(".jimageviewer/temp.xml");
    FileOutputStream fileOut = new FileOutputStream(file);
    PrintStream fileStream = new PrintStream(fileOut);
    for(int i=0; i<30; i++) {
      if(i==lineNum-1) {
        fileStream.println(text);
        fileIn.readLine();
      } else {
        fileStream.println(fileIn.readLine());
      }
    }
    fileOut.close();
    fileIn.close();
    
    Files.move(file.toPath(), new File(filePath).toPath(), StandardCopyOption.REPLACE_EXISTING);
  }
  
  public void paintComponent(Graphics g) {
    // Painting for panelStart
    // if(toDraw.equals(...) {
    
    // Painting for panelWelcome
    // if(toDraw.equals(...) {
    
    // Painting for panelMain 
    // if(toDraw.equals(...) {
  }
}
