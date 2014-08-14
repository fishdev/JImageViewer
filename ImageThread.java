import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.util.Iterator;
import java.util.concurrent.Callable;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.ImageIcon;

public class ImageThread implements Callable<ImageIcon> {
  private final String imagePath;
  private final double constant;
  
  public ImageThread(String p, double c) {
    imagePath = p;
    constant = c;
  }
  
  public ImageIcon call() throws Exception {
    FileInputStream fileInput = new FileInputStream(imagePath);
    ImageInputStream imageInput = ImageIO.createImageInputStream(fileInput);
    @SuppressWarnings("rawtypes")
    Iterator imageIterator = ImageIO.getImageReaders(imageInput);
    if(!imageIterator.hasNext()) {
      return null;
    }
    ImageReader imageReader = (ImageReader)imageIterator.next();
    ImageReadParam imageParameters = imageReader.getDefaultReadParam();
    imageReader.setInput(imageInput, true, true);
    
    double m = 0;
    if(imageReader.getWidth(0)>imageReader.getHeight(0)) {
      m = (double)imageReader.getWidth(0) / constant;
    } else if(imageReader.getWidth(0)<=imageReader.getHeight(0)) {
      m = (double)imageReader.getHeight(0) / constant;
    }
    m = m + 0.5;
    if(m<1.0) {
      m = 1.0;
    }
    
    imageParameters.setSourceSubsampling((int)m, (int)m, 0, 0);
    BufferedImage image = imageReader.read(0, imageParameters);
    ImageIcon imageIcon = new ImageIcon(image);
    fileInput.close();
    imageInput.close();
    return imageIcon;
  }
}
