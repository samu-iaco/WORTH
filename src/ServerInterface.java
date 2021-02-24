import java.rmi.RemoteException;

public interface ServerInterface {

    /**
     *
     * @param nickName nome dell'utente che si vuole scollegare
     * @return Una stringa dell'avvenuta o meno operazione di logout
     */
    String logout(String nickName) throws RemoteException;
}
