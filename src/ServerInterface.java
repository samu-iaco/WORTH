import Model.Project;

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

    /**
     * @return restituisce la lista degli utenti registrati
     *         ed online nel momento in cui il metodo viene chiamato
     */
    ArrayList<UserAndStatus> listOnlineusers();

    /**
     * @param username nome utente di cui si vuole la lista dei progetti
     * @return la lista dei progetti di cui il client è membro
     */
    ArrayList<Project> listProjects(String username);

    /**
     * @param projectName nome del progetto
     * @return Crea un nuovo progetto di nome projectname
     *         dove ci sarà come primo membro l'utente
     *         che ha richiesto la creazione
     */
    String createProject(String projectName, String username);

    /**
     *
     * @param projectName nome del progetto
     * @param username nome dell'utente
     * @return aggiunge al progetto projectName l'utente username
     */
    String addMember(String projectName, String username);

}
