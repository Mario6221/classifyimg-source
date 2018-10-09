package czyz.pl.ui.dialogs.selection.set;

import czyz.pl.model.Loader;
import czyz.pl.ui.mainApp.OptionSet;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import sample.Main;

import java.io.File;
import java.util.List;
import java.util.Optional;

public class DialogController {
    @FXML
    private ComboBox<String> tagBox = new ComboBox<>();
    @FXML
    private ListView<RadioButton> radioButtons = new ListView<>();
    @FXML
    private Button newSet = new Button();
    @FXML
    private Label chosenSetLabel = new Label();
    @FXML
    private ListView<OptionSet> optionsLV = new ListView<>();
    @FXML
    private Button addTagButton = new Button();
    @FXML
    private Button newTagButton = new Button();

    private static final String LABEL_START = "Active set: ";

    private ObservableList<OptionSet> items = FXCollections.observableArrayList();
    private ObservableList<String> tags = FXCollections.observableArrayList();
    private OptionSet currentlySelected = null;

    public void initialize(){
        initLeft();
        initRight();
    }


    private void initLeft(){
        newSet.setOnAction(e->{
            Dialog<OptionSet> dialog = new Dialog<>();
            ButtonType ok = new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(ok,new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE));
            TextField field = new TextField();
            field.setPrefColumnCount(1);
            field.setPromptText("Start typing...");
            dialog.getDialogPane().setContent(field);
            dialog.setTitle("New set");
            dialog.setResultConverter(param -> {
                try {
                    if (param.getButtonData()== ButtonBar.ButtonData.OK_DONE)
                        if (!field.getText().trim().isEmpty())
                        return new OptionSet(field.getText().trim());
                }catch (Exception ex) {
                    return null;
                }
                return null;
            });
            dialog.getDialogPane().getStylesheets().clear();
            dialog.getDialogPane().getStylesheets().add("file:///"+new File("resources/main.css").getAbsolutePath().replace("\\", "/"));
            Optional<OptionSet> res = dialog.showAndWait();
            if(res.isPresent()){
                optionsLV.getItems().add(res.get());
                changeSet(res.get());
            }
        });
        chosenSetLabel.setText(LABEL_START);
        initLeftList();
    }
    private void initLeftList(){
        items.addAll(Main.getController().getOptionSetList());
        optionsLV.setCellFactory(param -> new SetFactory());
        optionsLV.setItems(items);
        optionsLV.setOnKeyPressed(e->{
            if (e.getCode().equals(KeyCode.DELETE)){
                optionsLV.getItems().remove(optionsLV.getFocusModel().getFocusedIndex());
            }
            else if (e.getCode().equals(KeyCode.ENTER))
                changeSet(optionsLV.getFocusModel().getFocusedItem());
        });
        optionsLV.setVisible(true);
        if (!optionsLV.getItems().isEmpty()){
            optionsLV.requestFocus();
            changeSet(optionsLV.getItems().get(0));
        }
    }

    private void initRight(){
        initTagBox();
        initRadioList();
    }
    private void initTagBox(){
        tagBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue!=null){
                if (!newValue.equals("")){
                    RadioButton temp = new RadioButton(newValue);
                    boolean flag = true;
                    for (RadioButton item: radioButtons.getItems()){
                        if (item.getText().equals(newValue)){
                            flag=false;
                            break;
                        }
                    }
                    if (flag)
                        radioButtons.getItems().add(temp);
                }
            }
        });
        List<String> t = Loader.getInstance().getAllTags();
        if (t!=null){
            tags.addAll(t);
        }
        newTagButton.setOnAction(e->{
            Dialog<String> dialog = new Dialog<>();
            dialog.getDialogPane().getButtonTypes().addAll(new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE),new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE));
            TextField field = new TextField();
            field.setPrefColumnCount(1);
            field.setPromptText("Start typing...");
            dialog.getDialogPane().setContent(field);
            dialog.setTitle("New tag");
            dialog.setResultConverter(param -> {
                try {
                    if (param.getButtonData()== ButtonBar.ButtonData.OK_DONE)
                        if (!field.getText().trim().isEmpty())
                            return field.getText().trim();
                }catch (Exception ex) {
                    return null;
                }
                return null;
            });
            dialog.getDialogPane().getStylesheets().clear();
            dialog.getDialogPane().getStylesheets().add("file:///"+new File("resources/main.css").getAbsolutePath().replace("\\", "/"));
            Optional<String> x = dialog.showAndWait();
            if (x.isPresent()){
                String result = x.get();
                if (!tags.contains(result))
                    tags.add(result);
            }
        });
        addTagButton.setOnAction(e->{
            String value = tagBox.getValue();
            if (value!=null){
                RadioButton temp = new RadioButton(value);
                boolean flag = true;
                for (RadioButton item: radioButtons.getItems()){
                    if (item.getText().equals(value)){
                        flag=false;
                        break;
                    }
                }
                if (flag)
                radioButtons.getItems().add(temp);
            }
        });
        radioButtons.setOnKeyPressed(e->{
            if (e.getCode().equals(KeyCode.DELETE))
                radioButtons.getItems().remove(radioButtons.getFocusModel().getFocusedIndex());
        });
        tagBox.setVisibleRowCount(6);
        tagBox.setItems(tags);
    }
    private void initRadioList(){
        radioButtons.setCellFactory(param -> new RadioFactory());
        radioButtons.setVisible(true);
    }


    private void changeSet(OptionSet set){
        if (set!=null){
            if (!set.getName().isEmpty() && set.getName()!=null){
                if (currentlySelected!=null){
                    currentlySelected.setAllButtons(radioButtons.getItems());
                    currentlySelected = set;
                    radioButtons.setItems(currentlySelected.getChildren());
                    chosenSetLabel.setText(LABEL_START+set.getName());
                }
                else {
                    radioButtons.setItems(set.getChildren());
                    currentlySelected = set;
                    chosenSetLabel.setText(LABEL_START+set.getName());
                }
            }
        }
    }


    void save(){
        saveRadios();
        Main.getController().setOptionSetList(items);
    }
    private void saveRadios(){
        if (currentlySelected!=null){
            currentlySelected.setAllButtons(radioButtons.getItems());
        }
    }
    OptionSet getValue(){
        return currentlySelected;
    }

    private class RadioFactory extends ListCell<RadioButton>{
        @Override
        protected void updateItem(RadioButton item, boolean empty) {
            super.updateItem(item, empty);
            if (item!=null && !empty){
                setText(item.getText());
                setOnMouseClicked(e->{
                    if (e.getButton().equals(MouseButton.PRIMARY))
                        if (e.getClickCount()==2){
                            radioButtons.getItems().remove(item);
                        }
                });
                setContextMenu(new RadioCM());
            }
            else {
                setText("");
                setOnMouseClicked(Event::consume);
                setContextMenu(null);
            }
        }
        private class RadioCM extends ContextMenu{
            RadioCM(){
                super();
                MenuItem delete = new MenuItem();
                delete.setText("Delete this item");
                delete.setOnAction(e->radioButtons.getItems().remove(getItem()));
                this.getItems().add(delete);
            }
        }
    }
    private class SetFactory extends ListCell<OptionSet>{
        @Override
        protected void updateItem(OptionSet item, boolean empty) {
            super.updateItem(item, empty);
            if (item!=null){
                if (item.getName()!=null){
                    setText(item.getName());
                    setOnMouseClicked(e->{
                        if (e.getButton().equals(MouseButton.PRIMARY)){
                                changeSet(optionsLV.getFocusModel().getFocusedItem());
                    }
                    setContextMenu(new SetCM());
                });
            } else setDefault();

            }
            else setDefault();
        }

        private void setDefault(){
            setText("");
            setOnMouseClicked(Event::consume);
            setContextMenu(null);
            setOnKeyPressed(Event::consume);
        }
        private class SetCM extends ContextMenu{
            SetCM(){
                super();
                MenuItem delete = new MenuItem();
                delete.setText("Delete this set");
                delete.setOnAction(e-> optionsLV.getItems().remove(optionsLV.getFocusModel().getFocusedIndex()));
                this.getItems().add(delete);
            }
        }
        }
    }
