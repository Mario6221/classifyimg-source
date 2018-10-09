package czyz.pl.ui.queue;

import czyz.pl.model.VFile;
import javafx.scene.Cursor;
import javafx.scene.control.ListCell;

//TODO - probably this can be done better
public class CellFactory extends ListCell<VFile> {
    @Override
    protected void updateItem(VFile item, boolean empty) {
        super.updateItem(item, empty);
        setCursor(Cursor.DEFAULT);
        if (item!=null) {
            String s = item.getFile().getName();
            try {
                setText(s.substring(0, s.lastIndexOf(".")));
            } catch (IndexOutOfBoundsException e) {
                setText(s);
            }
        }
        else {
            setText("");
        }
    }
}
