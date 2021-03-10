import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Notify_Interface extends Remote {
    void notifyEvent(String userName, String status) throws RemoteException;

    void notifyEventChat(String mAddress, int port) throws RemoteException;
}
