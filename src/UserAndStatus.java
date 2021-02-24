import java.io.Serializable;

/**
 * Classe usata per restituire al client la coppia username-stato
 */
public class UserAndStatus implements Serializable {
    private String userName;
    private String status;

    public UserAndStatus(String userName, String status) {
        this.userName = userName;
        this.status = status;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Utente{ " +
                "userName: " + userName + '\'' +
                ", status: " + status + '\'' +
                '}';
    }
}
