package Srv;
public interface AuthService {

    void start();
    void stop();

    String getNickByLoginPass(String login, String password);

    default int insertUser(String login, String password, String nickname) {
        System.out.println("Incorrect class");
        return 0;
    }

    default int updateUser(String login, String password, String nickname) {
        System.out.println("Incorrect class");
        return 0;
    }


}