package czyz.pl.ui.dialogs.selection.set;

import czyz.pl.ui.mainApp.OptionSet;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;

import java.io.File;
import java.io.IOException;

public class SelectSetDialog extends Dialog<OptionSet> {

    public SelectSetDialog() {
        super();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("dialog.fxml"));
            Parent root = loader.load();
            DialogController controller = loader.getController();
            getDialogPane().setContent(root);
            ButtonType yes = new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE);
            ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            getDialogPane().getButtonTypes().addAll(yes,cancel);
            getDialogPane().getStylesheets().clear();
            getDialogPane().getStylesheets().add("file:///"+new File("resources/main.css").getAbsolutePath().replace("\\", "/"));
            setResultConverter(param -> {
                controller.save();
                if (param.getButtonData()== ButtonBar.ButtonData.OK_DONE){
                    return controller.getValue();
                }
                return null;
            });
            setTitle("Change option set");
            setResizable(false);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
