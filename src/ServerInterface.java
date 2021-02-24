import java.rmi.RemoteException;
import java.util.ArrayList;

public interface ServerInterface {

    /**
     *
     * @param nickName nome dell'utente che si vuole scollegare
     * @return Una stringa dell'avvenuta o meno operazione di logout
     */
    String logout(String nickName) throws RemoteException;

    /**
     * @return utilizzata dal client per visualizzare
     *          la lista degli utenti registrati
     *          con il loro relativo stato
     */
    ArrayList<UserAndStatus> listUsers();
}
