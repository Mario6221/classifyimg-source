package czyz.pl.model;

import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;

public class VFile {
    private ImageWrapper image;
    private File imFile;

    public VFile(ImageWrapper i){
        image=i;
        imFile = new File(i.getPath());
    }

    public String getTagName(){
        return image.getVtag();
    }

    public ImageWrapper getImage() {
        return image;
    }

    public File getFile(){
        return imFile;
    }

    public VFile rename(String newName){
        String absPath = imFile.getAbsolutePath();
        String pathStart = absPath.substring(0,absPath.lastIndexOf("/")+1);
        String extension = absPath.substring(absPath.lastIndexOf("."));
        File x = new File(pathStart+newName+extension);
        if (imFile.renameTo(x)){
            Loader.getInstance().editEntry(image.getPath(),x.getAbsolutePath());
            imFile=x;
            getImage().setPath(imFile.getAbsolutePath());
            return this;
        }
        return this;
    }

    public boolean changePath(String newPath){
        File x = new File(newPath+imFile.getName());
        if (imFile.renameTo(x)){
            Loader.getInstance().editEntry(image.getPath(),x.getAbsolutePath());
            imFile=x;
            getImage().setPath(imFile.getAbsolutePath());
            return true;
        }
        return false;
    }

    public boolean copy(String newPath){
        File x = new File(newPath+imFile.getName());
        try {
            Files.copy(imFile,x);
            return true;
        } catch (IOException e) {
            System.out.println("Error: "+e.getMessage());
            return false;
        }
    }
}
