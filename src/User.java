public class User {
    String name;
    String password;
    String status;

    public User(String name, String password) {
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

    public void changeStatus(String newStatus){
        if(newStatus.equals("online") || newStatus.equals("Online")) this.status = "online";
        else if(newStatus.equals("offline") || newStatus.equals("Offline")) this.status = "offline";
    }
}
