package czyz.pl.app;

import czyz.pl.ui.mainApp.OptionSet;
import javafx.application.Platform;
import javafx.scene.control.RadioButton;
import sample.Main;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
//Done badly
//TODO - change to System property where possible, change to database where possible, otherwise ok.
public final class CachedProperties {
    private static boolean useProps = true;
    public static String CHOOSER_INITIAL_DIRECTORY = System.getProperty("user.home");

    private static final String SEPARATOR = "\n";
    private static final String FILE_NAME = "resources/compareImg.txt";
    //TODO - Change that \/
    private static final String OPTION_SETS_SEPARATOR = "!@#SAD#fasdASDNJFEWknrgkd;&%@#%123";
    private static final String OPTION_SETS_FILE_NAME ="resources/optionsets.txt";

    public static void setChooserInitialDirectory(String s){
        CHOOSER_INITIAL_DIRECTORY=s;
    }


    private static void setUseProps(boolean b) { useProps=b;}

    public static void init(){
            new File("resources").mkdir();
            try {
                List<String> lines = Files.readAllLines(FileSystems.getDefault().getPath(FILE_NAME));
                if (!lines.isEmpty()){
                    System.out.println("CACHE: Loading props file...");
                    if (lines.get(0).endsWith("TRUE")){
                        setChooserInitialDirectory(lines.get(1));
                    }
                    else setUseProps(false);
                }
            } catch (IOException e) {
                System.out.println("CACHE: File "+FILE_NAME+" nonexistent or corrupted. Loading defaults...");
            }
            finally {
                Runtime.getRuntime().addShutdownHook(new Thread(CachedProperties::saveCache));
            }
            try {
                List<String> lines = Files.readAllLines(FileSystems.getDefault().getPath(OPTION_SETS_FILE_NAME));
                List<OptionSet> results = new ArrayList<>();
                if (!lines.isEmpty()){
                    System.out.println("CACHE: Loading option sets...");
                    boolean isName = true;
                    OptionSet set = new OptionSet();
                    for (String line: lines){
                        if (line.isEmpty())
                            continue;
                        if (isName){
                            set.setName(line);
                            isName=false;
                            continue;
                        }
                        if (line.equals(OPTION_SETS_SEPARATOR)){
                            results.add(set);
                            isName=true;
                            set = new OptionSet();
                        }
                         else {
                            set.addButton(new RadioButton(line));
                        }
                    }
                    Platform.runLater(()->Main.getController().setOptionSetList(results));
                }
            } catch (IOException e) {
                System.out.println("CACHE: Nothing to load or file corrupted...");
            }
    }
    private static void saveCache(){
        if (useProps){
            List<String> lines = new ArrayList<>();
            lines.add("USE_PROPERTIES=TRUE"+SEPARATOR);
            lines.add(CHOOSER_INITIAL_DIRECTORY+SEPARATOR);
            try (PrintWriter writer = new PrintWriter(FILE_NAME,"UTF-8")){
                for (String lane: lines){
                    writer.print(lane);
                }
                System.out.println("CACHE: Save successful");
            } catch (Exception e) {
                System.out.println("CACHE: Error: "+e.getMessage());
            }
        }
        if (!Main.getController().getOptionSetList().isEmpty())
        try (PrintWriter writer = new PrintWriter(OPTION_SETS_FILE_NAME,"UTF-8")){
            Main.getController().getOptionSetList().forEach(l->writer.print(l.save()+"\n"+OPTION_SETS_SEPARATOR));
            System.out.println("CACHE: Options saved successfully");
        } catch (Exception e) {
            System.out.println("CACHE: Error: "+e.getMessage());
        }

    }

}
