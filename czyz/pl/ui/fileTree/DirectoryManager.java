package czyz.pl.ui.fileTree;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import sample.Main;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
//TODO - OPTIMIZE THIS! Seriously, it's terrible
public class DirectoryManager implements Runnable {
    File dir;
    public DirectoryManager(File f){
        dir=f;
    }

    @Override
    public void run() {
        try{
            TreeItem<File> r = new FileTreeItem().buildFileSystemBrowser(dir.getAbsolutePath());
            long fileNum = getFilesNumber(r);
            if (fileNum != 0){
                Platform.runLater(()->{
                    Dialog<Boolean> addToQueue = new Dialog<>();
                    addToQueue.setContentText("Detected "+fileNum+" images. Add to queue?");
                    addToQueue.setTitle("Images detected");
                    addToQueue.getDialogPane().getButtonTypes().addAll(new ButtonType("Yes", ButtonBar.ButtonData.YES),new ButtonType("No", ButtonBar.ButtonData.NO));
                    addToQueue.setResultConverter(param -> param.getButtonData() == ButtonBar.ButtonData.YES);
                    Optional<Boolean> res = addToQueue.showAndWait();
                    if (res.isPresent()){
                        boolean x = res.get();
                        if (x){
                            Main.getController().addToQueue(getFiles(r));
                        }
                    }
                });
            }
            TreeView<File> tv = new TreeView<>(r);
            tv.setCellFactory(prop->new FileCellFactory());
            tv.setShowRoot(false);
            Platform.runLater(()->{
                Main.getController().addToPTabPane(tv);
                Main.getController().updateBar("Added tab "+tv.getRoot().getValue().getName() +" in Phisical Files tab");
            });
        }catch (Exception e){
            System.out.println("Error: "+e.getMessage());
            Platform.runLater(()-> {
                Main.getController().updateBar("Error in adding new tab");
            });
        }

    }

    private long getFilesNumber(TreeItem<File> r){
        long result=0;
        ObservableList<TreeItem<File>> children = r.getChildren();
        for (int i=0;i<children.size();i++){
            if (children.get(i).isLeaf())
                result++;
            else result+=getFilesNumber(children.get(i));
        }
        return result;
    }

    private List<File> getFiles(TreeItem<File> r){
        List<File> result = new ArrayList<>();
        ObservableList<TreeItem<File>> children = r.getChildren();
        for (int i=0;i<children.size();i++){
            if (children.get(i).isLeaf())
                result.add(children.get(i).getValue());
            else result.addAll(getFiles(children.get(i)));
        }
        return result;
    }
}
