package czyz.pl.model;

import java.io.File;
//TODO - connect this class to image checking mechanism in ImageGate
public class ImageWrapper {
    private String path;
    private long hash;
    private String vtag;
    private boolean missingFile;

    public ImageWrapper(String path,long hash, String vtag){
        this.path=path;
        this.hash=hash;
        this.vtag=vtag;
        this.missingFile=false;
    }

    public ImageWrapper(String path, long hash){
        this.path=path;
        this.hash=hash;
        this.missingFile=false;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getHash() {
        return hash;
    }

    public void setHash(long hash) {
        this.hash = hash;
    }

    public String getVtag() {
        return vtag;
    }

    public void setVtag(String vtag) {
        this.vtag = vtag;
    }

    public boolean isMissingFile() {
        return missingFile;
    }

    public void setMissingFile(boolean missingFile) {
        this.missingFile = missingFile;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj==null)
            return false;
        if (obj==this)
            return true;
        else if (obj instanceof ImageWrapper){
            ImageWrapper o = (ImageWrapper) obj;
            return o.getPath().equals(path);
        }
        return false;
    }

    @Override
    public String toString() {
        return "Image "+(new File(path).getName())+": "+path+" "+vtag+" "+Long.toUnsignedString(hash);
    }
}
