package czyz.pl.ui.fileTree;

import javafx.scene.Cursor;
import javafx.scene.control.TreeCell;
import javafx.scene.input.MouseButton;
import sample.Main;

import java.io.File;
//TODO - drop this in the item factory
public class FileCellFactory extends TreeCell<File>    {
    @Override
    protected void updateItem(File item, boolean empty) {
        super.updateItem(item, empty);
        if (!(item == null || empty)){
            setCursor(Cursor.HAND);
            setText(item.getName());
            setContextMenu(new PFTabContextMenu(item));
            if (!item.isDirectory()){
                setOnMouseClicked(e->{
                    if (e.getButton().equals(MouseButton.PRIMARY)){
                        if (e.getClickCount()==2){
                            Main.getController().addToQueue(item);
                        }
                    }
                });
            }
        }
        //setText(item == null ? "" : item.getName());
        else {
            setText("");
            setCursor(Cursor.DEFAULT);
            setOnMouseClicked(e->{});
        }
    }

}
