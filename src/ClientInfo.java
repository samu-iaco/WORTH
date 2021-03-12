import java.io.DataOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientInfo {
    private boolean isWaiting = false;
    private boolean isBusy = false;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    private Integer UDPPortAnswers;
    private Socket socket;

    public boolean isWaiting() {
        return isWaiting;
    }

    public void setWaiting(boolean waiting) {
        isWaiting = waiting;
    }

    public boolean isBusy() {
        return isBusy;
    }

    public void setBusy(boolean busy) {
        isBusy = busy;
    }

    public ObjectInputStream getObjectInputStream() {
        return objectInputStream;
    }

    public void setObjectInputStream(ObjectInputStream ois) {
        this.objectInputStream = ois;
    }

    public ObjectOutputStream getObjectOutputStream() {
        return objectOutputStream;
    }

    public void setObjectOutputStream(ObjectOutputStream oos) {
        this.objectOutputStream = oos;
    }

    public Integer getUDPPortAnswers() {
        return UDPPortAnswers;
    }

    public void setUDPPortAnswers(Integer UDPPortAnswers) {
        this.UDPPortAnswers = UDPPortAnswers;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }
}
