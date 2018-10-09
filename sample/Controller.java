package sample;

import czyz.pl.app.CachedProperties;
import czyz.pl.app.ImageGate;
import czyz.pl.model.ImageWrapper;
import czyz.pl.model.Loader;
import czyz.pl.model.VFile;
import czyz.pl.ui.dialogs.selection.set.SelectSetDialog;
import czyz.pl.ui.fileTree.DirectoryManager;
import czyz.pl.ui.mainApp.OptionSet;
import czyz.pl.ui.queue.CellFactory;
import czyz.pl.ui.vFileListView.ListGenerator;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

//TODO - SPC / PLUGINY


//TODO - 3. ZROB IKONY DO TABOW.(nie chce mi sie) 5.PLUGINY
//TODO - chache props - comboboxes cached ?
public class Controller {
    @FXML
    private BorderPane rootPane = new BorderPane();
    @FXML
    private Label statusbar = new Label();
    @FXML
    private TabPane phisFTabPane = new TabPane();
    @FXML
    private TabPane vFTP = new TabPane();
    @FXML
    private ListView<VFile> queueLV = new ListView<>();
    @FXML
    private Tab queueTab = new Tab();
    @FXML
    private ImageView imageBox = new ImageView();
    @FXML
    private CheckBox autoQueue = new CheckBox();
    @FXML
    private Button mainAppOkButton = new Button();
    @FXML
    private Button mainAppCancelButton = new Button();
    @FXML
    private Button mainAppSelectButton = new Button();
    @FXML
    private FlowPane optionsFP = new FlowPane();
    @FXML
    private ScrollPane imageSP = new ScrollPane();
    @FXML
    private StackPane imageHolder = new StackPane();
    @FXML
    private ToggleButton toggleZoom = new ToggleButton();
    @FXML
    private TextField changeNameTF = new TextField();

    private final Lock queueLock = new ReentrantLock();
    private VFile currentlyQueued;
    private OptionSet currentlyPicked = null;
    private List<OptionSet> setList = new ArrayList<>();
    private final DoubleProperty zoomProperty = new SimpleDoubleProperty(1);
    private final DoubleProperty imWidthProperty = new SimpleDoubleProperty(0);
    private final DoubleProperty imHeightProperty = new SimpleDoubleProperty(0);
    private ExecutorService pool = Executors.newFixedThreadPool(5);
    private final BooleanProperty isStarted = new SimpleBooleanProperty(false);

    public void initialize(){
        pool.execute(new DirectoryManager(new File(System.getProperty("user.dir"))));
        CachedProperties.init();
        initQueue();
        initQueueTab();
        initMainApp();
    }

    public boolean isStarted(){
        return isStarted.get();
    }
    private Lock getQueueLock(){
        return queueLock;
    }

    public void addToQueue(File f){
        try {
            pool.execute(()->{ImageWrapper item = ImageGate.getInstance().checkAgainstDatabase(f);
                if (item!=null)
                    Platform.runLater(()->{
                        getQueueLock().lock();
                        boolean correct = true;
                        for (VFile a: queueLV.getItems()){
                            if (a.getImage().equals(item)){
                                correct=false;
                                break;
                            }
                        }
                        if (!correct){
                            getQueueLock().unlock();
                            updateBar(f.getName()+" is already in database");
                        }
                        else {
                            if (currentlyQueued!=null){
                                correct=!currentlyQueued.getImage().equals(item);
                            }
                            if (correct){
                                queueLV.getItems().add(new VFile(item));
                                getQueueLock().unlock();
                                updateBar("Added "+f.getName()+" to queue");
                                if (!isStarted.get())
                                    start();
                            }
                            else {
                                getQueueLock().unlock();
                                updateBar(f.getName()+" is already in the queue");
                            }
                        }
                    });
            });
        }catch (Exception e){
            System.out.println("POOL: "+e.getMessage());
        }

    }
    public void addToQueue(List<File> files){
        for (File f: files){
            addToQueue(f);
        }
    }

    public List<OptionSet> getOptionSetList(){
        return setList;
    }

    public void setOptionSetList(List<OptionSet> optionSets){ setList = new ArrayList<>(optionSets);}
    private void changeOptionSet(OptionSet o){
        if (o!=null){
            mainAppSelectButton.setVisible(false);
            currentlyPicked=o;
            optionsFP.getChildren().setAll(o.getChildren());
        }else optionsFP.getChildren().setAll();
    }
    public void setImage(Image image){
        Window tmp = Main.getStage().getScene().getWindow();
        imageSP.setMaxSize(tmp.getWidth()-500,tmp.getHeight()-200);
        zoomProperty.set(1);
        imHeightProperty.set(imageSP.getHeight()-2);
        imWidthProperty.set(imageSP.getWidth()-2);
        imageBox.setImage(image);
        imageBox.setVisible(true);
    }

    @FXML
    public void optionOpenVFiles(){
            Dialog<String> dialog = createDialog(new FlowPane());
            if (dialog!=null){
            Optional<String> e = dialog.showAndWait();
            if (e.isPresent()){
                String result = e.get();
                List<ImageWrapper> images = Loader.getInstance().getAllFiles(result);
                new ListGenerator().getView(images);
            }
            else updateBar("Canceled");
        }
        else updateBar("Database error / no tags returned");
    }
    @FXML
    public void optionOpenPhisicalDirectory(){
            DirectoryChooser dirChooser = new DirectoryChooser();
            dirChooser.setTitle("Choose Directory");
            dirChooser.setInitialDirectory(new File(CachedProperties.CHOOSER_INITIAL_DIRECTORY));
            File dir = dirChooser.showDialog(null);
            if (dir!=null){
                CachedProperties.setChooserInitialDirectory(dir.getAbsolutePath());
                new Thread(new DirectoryManager(dir)).start();
            }
            else updateBar("Cancelled");
    }
    @FXML
    public void optionOpenPhisicalFiles(){
        final FileChooser fileChooser = new FileChooser();
        final FileChooser.ExtensionFilter images = new FileChooser.ExtensionFilter("Images","*.jpg","*.bmp","*.gif","*.png");
        fileChooser.setInitialDirectory(new File(CachedProperties.CHOOSER_INITIAL_DIRECTORY));
        fileChooser.getExtensionFilters().addAll(images,
                new FileChooser.ExtensionFilter("All Files","*.*"));
        fileChooser.setSelectedExtensionFilter(images);
        List<File> files = fileChooser.showOpenMultipleDialog(null);
        if (!files.isEmpty()){
            CachedProperties.setChooserInitialDirectory(files.get(0).getParentFile().getAbsolutePath());
            addToQueue(files);
        }
    }
    @FXML
    public void optionChangeSelectionSet(){
        SelectSetDialog dialog = new SelectSetDialog();
        Optional<OptionSet> result = dialog.showAndWait();
        if (result.isPresent()){
            OptionSet o = result.get();
            changeOptionSet(o);
            updateBar("Picked set: "+o.getName());
        }
        else updateBar("Cancelled");
    }
    @FXML
    public void optionDumpDatabase(){
        FlowPane flowPane = new FlowPane();
        Dialog<String> dialog = createDialog(flowPane);
        if (dialog!=null){
            CheckBox isCopied = new CheckBox();
            isCopied.setSelected(true);
            isCopied.setText("Leave images in database?(copy)");
            flowPane.getChildren().add(isCopied);
            Optional<String> e = dialog.showAndWait();
            if(e.isPresent()){
                String result = e.get();
                List<ImageWrapper> images = Loader.getInstance().getAllFiles(result);
                DirectoryChooser directoryChooser = new DirectoryChooser();
                directoryChooser.setTitle("Save to");
                File dir = directoryChooser.showDialog(null);
                if (dir!=null){
                    String dirPath = dir.getAbsolutePath();
                    if (images != null) {
                        boolean selected = isCopied.isSelected();
                        System.out.println(selected);
                        for (ImageWrapper image: images){
                            if (!image.isMissingFile()){
                                VFile v = new VFile(image);
                                if(selected){
                                    v.copy(dirPath);
                                }
                                else v.changePath(dirPath);
                            }
                        }
                    } else updateBar("No tags returned");
                }else updateBar("Cancelled");
            }
            else updateBar("Cancelled");
        }
        else updateBar("Database error / no tags returned");
    }

    private Dialog<String> createDialog(FlowPane flowPane){
        Dialog<String> dialog = new Dialog<>();
        ButtonType okButton = new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(okButton);
        ComboBox<String> comboBox = new ComboBox<>();
        dialog.setResultConverter(param -> comboBox.getValue());
        dialog.getDialogPane().getStylesheets().clear();
        dialog.getDialogPane().getStylesheets().add("file:///"+new File("resources/main.css").getAbsolutePath().replace("\\", "/"));
        dialog.getDialogPane().setContent(flowPane);
        dialog.setResizable(true);
        ObservableList<String> itemList = FXCollections.observableArrayList();
        List<String> l = Loader.getInstance().getAllNotEmptyTags();
        if (l!=null) {
            itemList.addAll(l);
            comboBox.setItems(itemList);
            flowPane.getChildren().add(comboBox);
            return dialog;
        }
        return null;
    }

    private void start(){
        if (!queueLV.getItems().isEmpty()){
            try {
                getQueueLock().lock();
                currentlyQueued = queueLV.getItems().get(0);
                queueLV.getItems().remove(0);
            }catch (Exception e){
                System.out.println("Start error: "+e.getMessage());
            }finally {
                getQueueLock().unlock();
            }

            try {
                Image image = new Image(new FileInputStream(currentlyQueued.getFile().getAbsolutePath()));
                setImage(image);
                if (currentlyQueued!=null){
                    changeNameTF.setText(currentlyQueued.getFile().getName().substring(0,currentlyQueued.getFile().getName().lastIndexOf(".")));
                    changeNameTF.selectAll();
                    changeNameTF.requestFocus();
                }
                isStarted.setValue(true);
                System.gc();
            } catch (FileNotFoundException e) {
                System.out.println(e.getMessage());
                updateBar("File "+currentlyQueued.getFile().getName()+" not found");
                currentlyQueued=null;
                if (autoQueue.isSelected())
                    start();
            }
        }
    }

    private void initQueueTab(){
        Button b = new Button("Start");
        b.setFont(new Font(10));
        b.setOnAction(e->start());
        queueTab.setGraphic(b);
        b.disableProperty().bind(Bindings.isEmpty(queueLV.getItems()).or(isStarted));
    }

    private void initQueue(){
        queueLV.setCellFactory(prop-> new CellFactory());
        queueLV.setItems(FXCollections.observableArrayList());
        queueLV.setVisible(true);
    }

    private void initMainApp(){
        imageBox.managedProperty().bind(imageBox.visibleProperty());
        imageBox.setVisible(false);


        changeNameTF.managedProperty().bind(changeNameTF.visibleProperty());
        changeNameTF.setVisible(true);


        autoQueue.setSelected(true);


        mainAppSelectButton.setDefaultButton(true);
        mainAppSelectButton.managedProperty().bind(mainAppSelectButton.visibleProperty());
        mainAppSelectButton.setOnAction(e->{
            mainAppSelectButton.setVisible(false);
            optionChangeSelectionSet();
        });


        mainAppOkButton.disableProperty().bind(Bindings.not(imageBox.visibleProperty()));
        mainAppOkButton.setOnAction(e->{
            if (currentlyPicked!=null){
                if (currentlyPicked.getGroup().getSelectedToggle() != null){
                    String tag = (String) currentlyPicked.getGroup().getSelectedToggle().getUserData();
                    currentlyQueued.getImage().setVtag(tag);
                    if (!changeNameTF.getText().trim().equals("")){
                        currentlyQueued.rename(changeNameTF.getText().trim());
                    }
                    final ImageWrapper finalizedImage = currentlyQueued.getImage();
                    pool.execute(()->ImageGate.getInstance().addToDatabase(finalizedImage));
                    if (autoQueue.isSelected() && !queueLV.getItems().isEmpty())
                        start();
                    else {
                        imageBox.setVisible(false);
                        isStarted.setValue(false);
                    }
                }
            }
        });
        mainAppCancelButton.setOnAction(e->{if (autoQueue.isSelected() && !queueLV.getItems().isEmpty()) start();else {imageBox.setVisible(false);isStarted.setValue(false);}});


        imageSP.addEventFilter(ScrollEvent.ANY, event -> {
            if (toggleZoom.isSelected())
                if (event.getDeltaY()>0)
                    zoomProperty.set(zoomProperty.get()*1.1);
                else if (event.getDeltaY()<0)
                    zoomProperty.set(zoomProperty.get()/1.1);
        });
        imageBox.setFitHeight(imHeightProperty.get());
        imageBox.setFitWidth(imWidthProperty.get());
        imageHolder.minWidthProperty().bind(Bindings.createDoubleBinding(() ->
                imageSP.getViewportBounds().getWidth(), imageSP.viewportBoundsProperty()));
        imageHolder.minHeightProperty().bind(Bindings.createDoubleBinding(() ->
                imageSP.getViewportBounds().getHeight(), imageSP.viewportBoundsProperty()));


        final InvalidationListener onZoom = (arg1)->{
            if (imageBox.isVisible()){
                imageBox.setFitWidth(imWidthProperty.get()*zoomProperty.get());
                imageBox.setFitHeight(imHeightProperty.get()*zoomProperty.get());
            }
        };
        rootPane.setOnKeyPressed(e->{if (e.getCode().equals(KeyCode.Z))toggleZoom.setSelected(!toggleZoom.isSelected());});
        toggleZoom.selectedProperty().addListener((arg0)->{
            if (toggleZoom.isSelected()){
                zoomProperty.addListener(onZoom);
            }
            else zoomProperty.removeListener(onZoom);
        });
        toggleZoom.setSelected(true);


        mainAppSelectButton.requestFocus();
    }






    //sets the treeview in tabview
    public void addToPTabPane(TreeView<File> tv){
        phisFTabPane.getTabs().add(createTreeTab(tv,tv.getRoot().getValue().getName()));
    }
    private Tab createTreeTab(TreeView tree,String label){
        Tab t = new Tab();
        t.setText(label);
        t.setContent(tree);
        return t;
    }
    public void addToVTabPane(Tab t){
        vFTP.getTabs().add(t);
    }
    public void updateBar(String s){
        statusbar.setText(s);
    }
}
