import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Notify_Interface extends Remote {
    void notifyEvent(String nickName, String status) throws RemoteException;
}
