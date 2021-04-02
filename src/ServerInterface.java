import Model.Card;
import Model.Project;
import Model.User;

import java.io.IOException;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface ServerInterface {


    /**
     * @param u utente da loggare
     * @param currUsername utente corrente
     * @return logga o meno l'utente se le credenziali sono corrette
     */
    Login<UserAndStatus> login(User u, String currUsername) throws RemoteException;

    /**
     *
     * @param nickName nome dell'utente che si vuole scollegare
     * @return Una stringa dell'avvenuta o meno operazione di logout
     */
    String logout(String nickName, String currUsername) throws RemoteException;

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
    ToClientProject createProject(String projectName, String username);

    /**
     *
     * @param projectName nome del progetto
     * @param username nome dell'utente
     * @param currUsername nome utente corrente
     * @return aggiunge al progetto projectName l'utente username
     */
    String addMember(String projectName, String username, String currUsername);

    /**
     * @param projectName nome del progetto di cui si vuole conoscere i membri
     * @param currUsername nome utente corrente
     * @return la lista dei membri del progetto
     */
    ToClient<String> showMembers(String projectName, String currUsername);

    /**
     * @param projectName nome del progetto
     * @param currUsername utente corrente
     * @return mostra le cards relative al progetto
     */
    ToClient<Card> showCards(String projectName, String currUsername);

    /**
     * @param projectName nome del progetto
     * @param cardName nome della card
     * @param description breve descrizione
     * @param currUsername utente corrente
     * @return aggiunge al progetto una nuova card con una breve descrizione
     */
    String addCard(String projectName, String cardName, String description,String currUsername);

    /**
     * @param projectName nome del progetto
     * @param cardName Nome della card
     * @param partenza lista dove si trova la card
     * @param arrivo lista dove la card deve andare
     * @param currUsername utente corrente
     * @return sposta la card dalla lista di partenza a quella di arrivo
     */
    String moveCard(String projectName, String cardName, String partenza, String arrivo,String currUsername);

    /**
     * @param projectName nome del progetto
     * @param cardName nome della card
     * @param currUsername utente corrente
     * @return gli spostamenti della card all'interno del progeto
     */
    ToClient<String> getCardHistory(String projectName, String cardName,String currUsername);


    /**
     * @param projectName nome del progetto su cui si vuole inviare il messaggio
     * @param currUsername utente corrente
     * @return l'utente invia il messaggio sulla chat del progetto projectName
     */
    ToClientChat sendChatMsg(String projectName, String currUsername);

    /**
     * @param projectName nome del progetto
     * @param currUsername utente corrente
     * @return i messaggi della chat del progetto projectName
     */
    ToClientChat readChat(String projectName,String currUsername);

    /**
     * @param projectName nome del progetto
     * @param currUsername utente corrente
     * @return cancella il progetto se tutte le card sono nella lista DONE
     */
    String cancelProject(String projectName,String currUsername) throws IOException;
}
