package czyz.pl.model;

import com.google.common.primitives.Longs;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
//TODO - create an Interface for the model
public final class Loader {

    private static Loader instance = new Loader();

    private static final String DATABASE_NAME="resources/imgDatabase.db";

    private static final String DATABASE_PATH = "jdbc:sqlite:"+DATABASE_NAME;

    //IMAGE FILES TABLE
    private static final String FILES_TABLE_NAME = "images";
    private static final String FILES_COLUMN_ID = "id";
    //PATH is absolute
    private static final String FILES_COLUMN_PATH = "path";
    //HASH is stored in base64
    private static final String FILES_COLUMN_HASH = "hash";
    private static final int FILES_HASH_LENGTH = 12;
    private static final String FILES_COLUMN_VTAG_ID = "vtag_id";
    //CREATE TABLE IF NOT EXISTS images (id INTEGER PRIMARY KEY,path TEXT,hash TEXT(12),vtag_id INTEGER);
    private static final String CREATE_FILES_STATEMENT = "CREATE TABLE IF NOT EXISTS "+FILES_TABLE_NAME+
                                                        "("+FILES_COLUMN_ID+" INTEGER PRIMARY KEY,"+ FILES_COLUMN_PATH+" TEXT,"+FILES_COLUMN_HASH+" TEXT("+ FILES_HASH_LENGTH +"),"
                                                        +FILES_COLUMN_VTAG_ID+" INTEGER);";

    //VTAG TABLE
    private static final String VTAGS_TABLE_NAME = "vtags";
    private static final String VTAGS_COLUMN_ID = "_id";
    private static final String VTAGS_COLUMN_VTAG = "vtag";
    //CREATE TABLE IF NOT EXISTS vtags(_id INTEGER PRIMARY KEY,vtag TEXT);
    private static final String CREATE_VTAG_STATEMENT = "CREATE TABLE IF NOT EXISTS "+VTAGS_TABLE_NAME+"("+VTAGS_COLUMN_ID+" INTEGER PRIMARY KEY,"+VTAGS_COLUMN_VTAG+" TEXT);";

    //FULL TABLE VIEW
    private static final String FULL_TABLE_NAME = "fullTable";
    //CREATE VIEW fullTable AS SELECT images.path,images.hash,vtags.vtag FROM images INNER JOIN vtags ON images.vtag_id=vtags._id;
    private static final String CREATE_FULL_TABLE_STATEMENT = "CREATE VIEW IF NOT EXISTS "+FULL_TABLE_NAME+" AS SELECT "+FILES_TABLE_NAME+"."+FILES_COLUMN_PATH+
                                                            ","+FILES_TABLE_NAME+"."+FILES_COLUMN_HASH+","+VTAGS_TABLE_NAME+"."+VTAGS_COLUMN_VTAG+
                                                            " FROM "+FILES_TABLE_NAME+" INNER JOIN "+VTAGS_TABLE_NAME+" ON "+
                                                            FILES_TABLE_NAME+"."+FILES_COLUMN_VTAG_ID+"="+VTAGS_TABLE_NAME +"."+VTAGS_COLUMN_ID+";";

    //INSERTS
    private static final String ADD_TO_FILES_STATEMENT = "INSERT INTO "+FILES_TABLE_NAME+"("+FILES_COLUMN_PATH+","+FILES_COLUMN_HASH+","+FILES_COLUMN_VTAG_ID+") VALUES (?,?,?);";
    private static final String ADD_TO_VTAGS_STATEMENT = "INSERT INTO "+VTAGS_TABLE_NAME+"("+VTAGS_COLUMN_VTAG+") VALUES (";

    //QUERIES
    //PREPARED - well not rly
    //TODO - make this prepared thing a thing cause this is quite stupid
    private static final String QUERY_FIND_ID_BY_TAG = "SELECT "+VTAGS_COLUMN_ID + " FROM "+VTAGS_TABLE_NAME+" WHERE "+VTAGS_COLUMN_VTAG+"=";
    private static final String QUERY_GET_ALL_FILES_WITH_TAG = "SELECT * FROM "+FULL_TABLE_NAME+" WHERE "+VTAGS_COLUMN_VTAG+"=";
    //UNPREPARED
    private static final String QUERY_GET_ALL_TAGS = "SELECT "+VTAGS_COLUMN_VTAG+" FROM "+VTAGS_TABLE_NAME+";";
    private static final String QUERY_GET_ALL_FILES = "SELECT * FROM "+FULL_TABLE_NAME+";";
    private static final String QUERY_COUNT_TAG_USE = "SELECT COUNT(*) FROM "+FULL_TABLE_NAME+" WHERE "+VTAGS_COLUMN_VTAG+"=";
    //UPDATE RECORD
    private static final String UPDATE_IMAGE_PATH = "UPDATE "+FILES_TABLE_NAME+" SET "+FILES_COLUMN_PATH+"=? WHERE "+FILES_COLUMN_PATH+"=?;";

    //DELETE RECORD
    private static final String DELETE_FROM_FILES = "DELETE FROM "+FILES_TABLE_NAME+" WHERE "+FILES_COLUMN_PATH+"=?;";



            public int delete(ImageWrapper... images){
        int res = 0;
                try (Connection con = DriverManager.getConnection(DATABASE_PATH)) {
                    con.setAutoCommit(false);
                    try (PreparedStatement st = con.prepareStatement(DELETE_FROM_FILES)) {
                        for (ImageWrapper image: images){
                            st.setString(1,image.getPath());
                            st.addBatch();
                        }
                        st.executeBatch();
                        con.commit();
                    } catch (SQLException e){
                        con.rollback();
                        System.out.println(e.getMessage());
                    }
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                }
                return res;
            }

            public void editEntry(String oldPath, String newPath){
                try (Connection con = DriverManager.getConnection(DATABASE_PATH)){
                    try (PreparedStatement st = con.prepareStatement(UPDATE_IMAGE_PATH)){
                        con.setAutoCommit(false);
                        st.setString(1,newPath);
                        st.setString(2,oldPath);
                        st.addBatch();
                        st.executeBatch();
                        con.commit();
                    }catch (SQLException e){
                        con.rollback();
                        System.out.println(e.getMessage());
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

    private Loader(){
        try (Connection con = DriverManager.getConnection(DATABASE_PATH);
             Statement create = con.createStatement()){
            create.execute(CREATE_FILES_STATEMENT);
            create.execute(CREATE_VTAG_STATEMENT);
            create.execute(CREATE_FULL_TABLE_STATEMENT);
            //DEV ONLY
//            addToDatabase(new ImageWrapper("img.jpg",1234,"'papaj'"));
        } catch (SQLException e) {
            System.out.println("SQL Exception: "+e.getMessage());
        }
    }

    public static Loader getInstance(){
        return instance;
    }
//Base64.getEncoder().encodeToString(Longs.toByteArray(hash))
    public boolean addToDatabase(ImageWrapper... images){
        try (Connection con = DriverManager.getConnection(DATABASE_PATH)){
            con.setAutoCommit(false);
            try (PreparedStatement addToFiles = con.prepareStatement(ADD_TO_FILES_STATEMENT)){

                for (ImageWrapper image: images){
                    image.setVtag("'"+image.getVtag()+"'");
                    addToFiles.setString(1,image.getPath());
                    addToFiles.setString(2,Base64.getEncoder().encodeToString(Longs.toByteArray(image.getHash())));
                    long temp = findTagIndex(con,image.getVtag());
                    if (temp == -1){
                        throw new SQLException("Failed to find/create VTag");
                    }
                    addToFiles.setLong(3,temp);
                    addToFiles.addBatch();
                }
                try {
                    addToFiles.executeBatch();
                }catch (Exception e){
                    System.out.println("SQL addition error: "+e.getMessage());
                    con.rollback();
                    return false;
                }
                con.commit();
                return true;
            }
            catch (SQLException e) {
                System.out.println("SQL Exception: "+e.getMessage());
                con.rollback();
                return false;
            }
            finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.out.println("SQL Exception: "+e.getMessage());
            return false;
        }
    }

    private long findTagIndex(Connection con,String tag){
        try (Statement findTag = con.createStatement();
             Statement addTag = con.createStatement()){
            try (ResultSet result = findTag.executeQuery(QUERY_FIND_ID_BY_TAG+tag+";")){
                if (result!=null){
                    result.next();
                    long res = result.getLong(1);
                    return res;
                }
            }
            catch (SQLException e){ }//ignored. The tag probably does not exist.
            findTag.clearBatch();
            addTag.execute(ADD_TO_VTAGS_STATEMENT+tag+");");
            ResultSet r1 = findTag.executeQuery(QUERY_FIND_ID_BY_TAG+tag+";");
            r1.next();
            long id = r1.getLong(1);
            r1.close();
            return id;

        } catch (SQLException e) {
            System.out.println("SQL Exception: "+e.getMessage());
            return -1;
        }
    }

    public List<ImageWrapper> getAllFiles(){
        try (Connection con = DriverManager.getConnection(DATABASE_PATH);
             Statement st = con.createStatement()){

            return queryResults(st.executeQuery(QUERY_GET_ALL_FILES));
        } catch (SQLException e) {
            System.out.println("SQL Exception: "+e.getMessage());
            return null;
        }
    }

    public List<ImageWrapper> getAllFiles(String vTag){
                vTag = "'"+vTag+"'";
        try (Connection con = DriverManager.getConnection(DATABASE_PATH);
             Statement st = con.createStatement()){
            return queryResults(st.executeQuery(QUERY_GET_ALL_FILES_WITH_TAG+vTag+";"));
        } catch (SQLException e) {
            System.out.println("SQL Exception: "+e.getMessage());
            return null;
        }
    }

    public List<String> getAllTags(){
        try (Connection con = DriverManager.getConnection(DATABASE_PATH);
             Statement st = con.createStatement()){
            ResultSet resultSet = st.executeQuery(QUERY_GET_ALL_TAGS);
            List<String> tags = new ArrayList<>();
            while (resultSet.next()){
                tags.add(resultSet.getString(1));
            }
            resultSet.close();
            return tags;
        } catch (SQLException e) {
            System.out.println("SQL Exception: "+e.getMessage());
            return null;
        }
    }

    public List<String> getAllNotEmptyTags(){
        try (Connection con = DriverManager.getConnection(DATABASE_PATH);
             Statement st = con.createStatement()) {
            ResultSet resultSet = st.executeQuery(QUERY_GET_ALL_TAGS);
            List<String> tags = new ArrayList<>();
            while (resultSet.next()) {
                tags.add(resultSet.getString(1));
            }
            resultSet.close();
            for (int i = 0; i < tags.size(); i++) {
                ResultSet r1;
                r1 = st.executeQuery(QUERY_COUNT_TAG_USE + "'" + tags.get(i) + "';");
                r1.next();
                int a = r1.getInt(1);
                r1.close();
                if (a <= 0) tags.remove(tags.get(i));
            }
                return tags;
            } catch(SQLException e){
                System.out.println("SQL Exception: " + e.getMessage());
                return null;
            }
    }


    private List<ImageWrapper> queryResults(ResultSet res) throws SQLException {
        List<ImageWrapper> result = new ArrayList<>();
        while (res.next()){
            ImageWrapper wrapper = new ImageWrapper(res.getString(1),Longs.fromByteArray(Base64.getDecoder().decode(res.getString(2))),res.getString(3));
            result.add(wrapper);
        }
        if(!result.isEmpty())
        for (ImageWrapper i: result){
            i.setMissingFile(!new File(i.getPath()).getAbsoluteFile().exists());
        }
        res.close();
        return result;
    }


}
