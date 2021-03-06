package Model;

import java.io.Serializable;

/**
 * classe che crea la struttura dati dell'utente
 */
public class User implements Serializable {
    private String name;
    private String password;
    private String status;

    public User(String name, String password)  {
        super();
        this.name = name;
        this.password = password;
        this.status = "offline";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
       this.status = status;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
