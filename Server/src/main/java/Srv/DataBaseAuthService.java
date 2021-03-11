package Srv;



import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class DataBaseAuthService implements AuthService {
    private static final Logger logger = Logger.getLogger(MyServer.class.getName());
    private static Connection connection;
    private static Statement stmt;
    private static PreparedStatement psSelect;
    private static PreparedStatement psInsert;
    private static PreparedStatement psUpdate;
    private static PreparedStatement psSelectAllNick;
    private static PreparedStatement psSelectNicByLoginPass;

    static {
        LogManager manager = LogManager.getLogManager();
        try {
            manager.readConfiguration(new FileInputStream("logging.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void prepareAllStatements() throws SQLException {
        psSelect = connection.prepareStatement("SELECT nickname FROM users WHERE login = ? AND password = ?;");
        psUpdate = connection.prepareStatement("UPDATE users SET nickname = ? WHERE login = ? AND password = ?");
        psInsert = connection.prepareStatement("INSERT INTO users (login,password,nickname) VALUES(?,?,?)");
        psSelectAllNick = connection.prepareStatement("SELECT nickname, login FROM users");
    }

    private static void connect() throws Exception {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:main.db");
        stmt = connection.createStatement();
    }

    private static void disconnect() {
        try {
            stmt.close();
            logger.log(Level.SEVERE,"Data base has been closed");

        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void start() {
        logger.log(Level.SEVERE,"Auth service is running");
        //System.out.println("Auth service is running");

        try {
            connect();
            prepareAllStatements();
            logger.log(Level.SEVERE,"Connect to bd main is successful");
            //System.out.println("Connect to bd main is successful");
        } catch (Exception e) {
            logger.log(Level.WARNING,"Auth service err",e);
        } finally {
            //disconnect();
        }




    }

    @Override
    public void stop() {
        logger.log(Level.SEVERE,"Auth service has been stopped");
        //System.out.println("Auth service has been stopped");
        disconnect();
    }

    @Override
    public String getNickByLoginPass(String login, String password) {//Изменил на метод идентификации из БД.

        try {//Блок провеки через подготовленный запрос
            psSelect.setString(1,login);
            psSelect.setString(2,password);
            ResultSet rs = psSelect.executeQuery();
            while (rs.next()){
                //System.out.println(rs.getString("nickname"));
                return rs.getString("nickname");
            }

        } catch (SQLException e) {
            logger.log(Level.WARNING,"DB Error",e);
        }
        return null;
    }

    @Override
    public int insertUser(String login, String password, String nickname) {
        try {//Блок провеки через подготовленный запрос
            psInsert.setString(1,nickname);
            psInsert.setString(2,login);
            psInsert.setString(3,password);
            //System.out.printf("Login: %s\npassword: %s\nNickname: %s\n",login,password,nickname);

            ResultSet rs = psSelectAllNick.executeQuery();
            String dataBaseNick;
            String dataBaseLogin;

            while (rs.next()){

                dataBaseNick = rs.getString("nickname");
                dataBaseLogin = rs.getString("login");
                System.out.println("nick: " + dataBaseNick);
                System.out.println("login: " + dataBaseLogin);
                if (dataBaseNick.equals(nickname)||dataBaseLogin.equals(login)) {
                    logger.log(Level.SEVERE,"Error, Nick or Login exists");
                    //System.out.printf("Error, Nick or Login exists");
                   return 0;
                }
            }

            if(psInsert.executeUpdate()==1) {
                logger.log(Level.SEVERE,"Insert is OK");
                System.out.println("Insert is OK");
                return 1;
            }

                //ResultSet rs = psSelect.executeQuery();

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "DB Error",e);


        }

        return 0;
    }

    @Override
    public int updateUser(String login, String password, String nickname) {
        try {//Блок провеки через подготовленный запрос
            psUpdate.setString(1, nickname);
            psUpdate.setString(2, login);
            psUpdate.setString(3, password);
            //System.out.printf("Login: %s\npassword: %s\nNickname: %s\n",login,password,nickname);
            String oldNicname = getNickByLoginPass(login, password);

            File file = null;
            if (oldNicname != null)

            //System.out.println("oldNick = " + oldNicname);

            if (psUpdate.executeUpdate() == 1) {
                logger.log(Level.SEVERE,"Update is OK");
                //System.out.println("Update is OK");

                file.delete();
                return 1;
            }

            //ResultSet rs = psSelect.executeQuery();

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "DB Error",e);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error",e);
        }
            return 0;



    }
}

