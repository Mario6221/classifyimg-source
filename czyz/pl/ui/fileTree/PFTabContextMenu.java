package czyz.pl.ui.fileTree;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import sample.Main;

import java.io.File;
//TODO - again, get rid of this
public class PFTabContextMenu extends ContextMenu {
    public PFTabContextMenu(File f){
        if (!f.isDirectory()){
            MenuItem addToQueue = new MenuItem("Add to Queue...");
            addToQueue.setOnAction(e-> Main.getController().addToQueue(f));
            this.getItems().add(addToQueue);
        }
    }
}
