import Remote.Exception.UserAlreadyExistsException;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMI_login_Interface extends Remote {

    /**
     *
     * @param nickUtente nome utente
     * @param password password dell'utente
     * @throws RemoteException se ci sono problemi con l'RMI
     * @return "ok" se l'operazione ha aggiunto l'utente
     */
    String register(String nickUtente, String password) throws RemoteException, UserAlreadyExistsException;
}
