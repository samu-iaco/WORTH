import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMI_register_Interface extends Remote {

    /**
     *
     * @param nickUtente nome utente
     * @param password password dell'utente
     * @throws RemoteException se ci sono problemi con l'RMI
     * @return "ok" se l'operazione ha aggiunto l'utente
     */
    String register(String nickUtente, String password) throws RemoteException;

    void registerForCallback (Notify_Interface ClientInterface, String nickUtente) throws RemoteException;

    void unregisterForCallback (Notify_Interface ClientInterface) throws RemoteException;
}
