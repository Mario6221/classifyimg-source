package czyz.pl.app;

import com.google.common.primitives.Longs;
import main.java.org.imgscalr.Scalr;
import czyz.pl.model.ImageWrapper;
import czyz.pl.model.Loader;
import sample.Main;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
//TODO - this class should probably be runnalbe
public final class ImageGate {
    private static ImageGate instance;
    private List<ImageWrapper> images;

    private ImageGate(){images = Loader.getInstance().getAllFiles(); }

    public static void reinstanciate(){
        instance = null;
        System.gc();
        instance = new ImageGate();
    }

    public static ImageGate getInstance(){
        if (instance==null){
            instance = new ImageGate();
        }
        return instance;
    }

    private static float calculateAvr(BufferedImage img){
        int sum=0;
        int pixel;
        for(int i=0;i<img.getWidth();i++){
            for (int j=0;j<img.getHeight();j++){
                pixel = img.getRGB(i,j);
                sum+=(pixel) & 0xff;
            }
        }
        return sum/(img.getWidth()*img.getHeight());
    }

    //MIGHT BE USEFUL SOMETIME IN THE FUTURE
//    public boolean addToDatabase(String imagePath,String vtag){
//        ImageWrapper x = checkAgainstDatabase(new File(imagePath));
//        if (x!=null){
//            x.setVtag(vtag);
//            addToDatabase(x);
//            return true;
//        }
//        return false;
//    }

    public void addToDatabase(ImageWrapper image){
        Loader.getInstance().addToDatabase(image);
        images.add(image);
    }

    public ImageWrapper checkAgainstDatabase(File file){
        try {
            if (file.exists()){
                BufferedImage img = ImageIO.read(file);
                ImageWrapper newImage = new ImageWrapper(file.getAbsolutePath(),calculateHash(img));
                if (images == null) {
                    images = new ArrayList<>();}
                else
                    for (ImageWrapper image: images){
                        if (image.isMissingFile()){
                            continue;
                        }
                        if (compareHashValues(newImage.getHash(),image.getHash())){
                            if (identical(img,ImageIO.read(new File(image.getPath())),img.getHeight()*img.getWidth()/10))
                                return null;
                        }
                    }
                return newImage;
            }
            return null;
        } catch (IOException e) {
            System.out.println("Error occurred: "+e.getMessage());
            Main.getController().updateBar("FATAL ERROR");
            return null;
        }
    }

    //tolerance in number of pixels
    private boolean identical(BufferedImage img1, BufferedImage img2,int tolerance){
        if ((img1.getHeight()!=img2.getHeight())||img1.getWidth()!=img2.getWidth())
            return false;
        int tolCounter=0;
        for(int i=0;i<img1.getWidth();i++){
            for (int j=0;j<img1.getHeight();j++){
                if (img1.getRGB(i,j)!=img2.getRGB(i,j))
                    tolCounter++;
                if (tolCounter==tolerance)
                    return false;
            }
        }
        return true;
    }

    //equal to tolerance=0
//    private boolean identical(BufferedImage img1, BufferedImage img2){
//        if ((img1.getHeight()!=img2.getHeight())||img1.getWidth()!=img2.getWidth())
//            return false;
//        for(int i=0;i<img1.getWidth();i++){
//            for (int j=0;j<img1.getHeight();j++){
//                if (img1.getRGB(i,j)!=img2.getRGB(i,j))
//                    return false;
//            }
//        }
//        return true;
//    }

    //AHash
    private long calculateHash(BufferedImage img){
        img = Scalr.resize(img,Scalr.Method.SPEED,Scalr.Mode.FIT_EXACT,8,8,Scalr.OP_GRAYSCALE);
        float avg = calculateAvr(img);
        boolean[] result = new boolean[64];
        int z=0;
        for (int i=0;i<8;i++){
            for (int j=0;j<8;j++){
                if ((img.getRGB(i,j) & 0xff)>avg){
                    result[z]=true;
                }
                else result[z]=false;
                z++;
            }
        }
        return booleansToLong(result);
    }

    //returns true for nearly same hashes and false for different hashes
    private boolean compareHashValues(long hash1,long hash2){
        return cmp(BitSet.valueOf(Longs.toByteArray(hash1)), BitSet.valueOf(Longs.toByteArray(hash2)));
    }

    // calculate the difference vector
    private boolean cmp(BitSet h1, BitSet h2) {
        int diff =0;
        for (int i=0;i<h1.size();i++){
            if (h1.get(i)!=h2.get(i))
                diff++;
        }
        return diff <=5;
    }

    private long booleansToLong(boolean[] arr){
        long n = 0;
        for (boolean b : arr)
            n = (n << 1) | (b ? 1 : 0);
        return n;
    }



}
