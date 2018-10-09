package czyz.pl.ui.mainApp;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;

public class OptionSet {
    private ToggleGroup group;
    private ObservableList<RadioButton> children;
    private String name;

    public OptionSet(String name){
        group = new ToggleGroup();
        children = FXCollections.observableArrayList();
        this.name=name;
    }
    public OptionSet(){
        group = new ToggleGroup();
        children = FXCollections.observableArrayList();
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addButton(RadioButton r){
        children.add(r);
        r.setToggleGroup(group);
        r.setUserData(r.getText());
    }

    public void setAllButtons(RadioButton... r){
        group = new ToggleGroup();
        children = FXCollections.observableArrayList();
        for (RadioButton x: r){
            addButton(x);
        }
    }

    public void setAllButtons(ObservableList<RadioButton> r){
        group = new ToggleGroup();
        children = FXCollections.observableArrayList();
        for (RadioButton x: r){
            addButton(x);
        }
    }

    public String getName() {
        return name;
    }

    public ToggleGroup getGroup() {
        return group;
    }

    public ObservableList<RadioButton> getChildren() {
        return children;
    }

    public String save(){
        final String separator = "\n";
        StringBuilder result=new StringBuilder();
        result.append(separator).append(name);
        for (RadioButton r: children){
            result.append(separator).append(r.getText());
        }
        return result.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj==this) return true;
        if (obj==null) return false;
        if (obj instanceof OptionSet){
            OptionSet x = (OptionSet) obj;
            boolean flag;
            for (RadioButton r1 : children){
                flag = false;
                for (RadioButton r2: x.getChildren()){
                    if (r1.getText().equals(r2.getText())){
                        flag = true;
                        break;
                    }
                }
                if (!flag)
                    return false;
            }
            return true;
        }
        return false;
    }
}
