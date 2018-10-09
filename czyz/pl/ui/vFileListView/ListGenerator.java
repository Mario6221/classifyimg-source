package czyz.pl.ui.vFileListView;

import czyz.pl.model.ImageWrapper;
import czyz.pl.model.Loader;
import czyz.pl.model.VFile;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import sample.Main;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
//TODO - generate all of this on STATIC mode
public class ListGenerator {
    private ListView<VFile> result;
    public void getView(List<ImageWrapper> list){
        ObservableList<VFile> oList = FXCollections.observableArrayList();
        for (ImageWrapper i: list){
            if (!i.isMissingFile()){
                oList.add(new VFile(i));
            }
        }
        result = new ListView<>(oList);
        result.setEditable(true);
        result.setCellFactory(param -> new VFileFactory());
        Platform.runLater(()->{
            Tab t = new Tab();
            t.setText(result.getItems().get(0).getTagName());
            t.setContent(result);
            Main.getController().addToVTabPane(t);
        });
    }


    private class VFileFactory extends ListCell<VFile>{


        private TextField textField;
        private VFile oldvalue;
        @Override
        protected void updateItem(VFile item, boolean empty) {
            super.updateItem(item, empty);
            if (!(item==null)){
                setEditable(true);
                String fileName = item.getFile().getName();
                setText(fileName.substring(0,fileName.lastIndexOf(".")));
                setCursor(Cursor.HAND);
                setOnMouseClicked(e->{
                    if (e.getButton()== MouseButton.PRIMARY)
                            try {
                                if (!Main.getController().isStarted()){
                                    Main.getController().setImage(new Image(new FileInputStream(item.getFile())));
                                }
                                else Platform.runLater(()->Main.getController().updateBar("Item in queue is already displayed. Clear that item and try again"));
                            } catch (FileNotFoundException e1) {
                                System.out.println("Error :"+e1.getMessage());
                            }
                });
                setContextMenu(new ContextMenux());
            }
            else{
                setOnMouseClicked(Event::consume);
                setCursor(Cursor.DEFAULT);
                setText("");
                setEditable(false);
                setContextMenu(null);
            }
        }

        @Override
        public void startEdit() {
            super.startEdit();
            if (textField == null){
                createTextField();
            }
            setGraphic(textField);
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            textField.selectAll();
            textField.requestFocus();
        }

        @Override
        public void commitEdit(VFile newValue) {
            super.commitEdit(newValue);
            getListView().getItems().remove(oldvalue);
            getListView().getItems().add(newValue);
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();

            String fileName = getItem().getFile().getName();
            setText(fileName.substring(0,fileName.lastIndexOf(".")));
            setGraphic(null);
            setContentDisplay(ContentDisplay.TEXT_ONLY);
        }


        private void createTextField() {
            String old = getText();
            textField = new TextField(old);
            textField.setOnKeyPressed(t -> {
                if (t.getCode() == KeyCode.ENTER) {
                    if (!textField.getText().trim().isEmpty()){
                        oldvalue = getItem();
                        commitEdit(getItem().rename(textField.getText().trim()));
                    }
                } else if (t.getCode() == KeyCode.ESCAPE) {
                    cancelEdit();
                }
            });
            textField.focusedProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue){
                    cancelEdit();
                }
            });
        }

        private class ContextMenux extends ContextMenu {
            ContextMenux() {
                MenuItem edit = new MenuItem();
                edit.setText("Edit");
                edit.setOnAction(e->startEdit());
                MenuItem remove = new MenuItem();
                remove.setText("Remove image from database");
                remove.setOnAction(e-> {
                    Loader.getInstance().delete(getItem().getImage());
                    getListView().getItems().remove(getItem());
                });
                MenuItem fRemove = new MenuItem();
                fRemove.setText("Delete image from database and disc");
                fRemove.setOnAction(e->{
                    Loader.getInstance().delete(getItem().getImage());
                    try {
                        //noinspection ResultOfMethodCallIgnored
                        getItem().getFile().delete();
                    }catch (Exception e1){
                        System.out.println(e1.getMessage());
                    }
                    getListView().getItems().remove(getItem());
                });
                MenuItem display = new MenuItem();
                display.setText("Display image");
                display.setOnAction(e->{
                    try {
                        if (!Main.getController().isStarted()){
                            Main.getController().setImage(new Image(new FileInputStream(getItem().getImage().getPath())));
                        }
                        else Platform.runLater(()->Main.getController().updateBar("Item in queue is already displayed. Clear that item and try again"));
                    } catch (FileNotFoundException e1) {
                        System.out.println(e1.getMessage());
                    }
                });
                this.getItems().addAll(edit,remove,fRemove,display);
            }
        }
    }

}


