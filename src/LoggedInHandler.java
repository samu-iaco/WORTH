import Model.Card;
import Model.Project;
import Model.User;
import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Classe che gestisce il thread del client nel server
 */
public class LoggedInHandler implements Runnable {

    private ClientInfo info;
    /**
     * Le informazioni dell'utente che fa riferimento a questo Runnable
     */
    private User clientUser;
    private SignedUpUsers SignedUpUsers;
    private boolean stop = false;
    private TCPServer server;
    // Constructor
    public LoggedInHandler(ClientInfo info, TCPServer server)
    {
        this.info = info;
        this.server = server;
    }

    public void run()
    {
        try {
            while (!stop) {
                //Aspetto che mi arrivi un nuovo Object dal client
                start();
            }
        }catch (Exception e){
            stop = true;
            System.out.println("Chiusura inaspettata");
        }
    }

    public synchronized void start() {
        String[] splittedCommand;
        try{
            splittedCommand = (String[]) info.getObjectInputStream().readObject();
            System.out.println("command: " + splittedCommand[0]);
            switch (splittedCommand[0]){
                case "logout":
                    String resultLogout = server.logout(splittedCommand[1]);
                    info.getObjectOutputStream().writeObject(resultLogout); //invio verso il client
                    break;
                case "listusers":
                    ArrayList<UserAndStatus> listToClient;
                    listToClient = server.listUsers();
                    info.getObjectOutputStream().writeObject(listToClient); //invio al client
                    break;
                case "listonlineusers":
                    ArrayList<UserAndStatus> listOnlineUsers;
                    listOnlineUsers = server.listOnlineusers();
                    info.getObjectOutputStream().writeObject(listOnlineUsers);
                    break;
                case "listprojects":
                    ArrayList<Project> userProjects;
                    userProjects = server.listProjects(clientUser.getName());
                    info.getObjectOutputStream().writeObject(userProjects);
                    break;
                case "createproject":
                    String resultCreateProject = server.createProject(splittedCommand[1],clientUser.getName());
                    info.getObjectOutputStream().writeObject(resultCreateProject);
                    break;
                case "addmember":
                    String resultCreateMember = server.addMember(splittedCommand[1],splittedCommand[2]);
                    info.getObjectOutputStream().writeObject(resultCreateMember);
                    break;
                case "showmembers":
                    ToClient<String> resultShowMembers = server.showMembers(splittedCommand[1]);
                    info.getObjectOutputStream().writeObject(resultShowMembers);
                    break;
                case "showcards":
                    ToClient<Card> resultShowCards = server.showCards(splittedCommand[1]);
                    info.getObjectOutputStream().writeObject(resultShowCards);
                    break;
                case "addcard":
                    String resultAddCard = server.addCard(splittedCommand[1],splittedCommand[2],splittedCommand[3]);
                    info.getObjectOutputStream().writeObject(resultAddCard);
                    break;
                case "movecard":
                    String resultMoveCard = server.moveCard(splittedCommand[1],splittedCommand[2],splittedCommand[3],splittedCommand[4]);
                    info.getObjectOutputStream().writeObject(resultMoveCard);
                    break;
                case "getcardhistory":
                    ToClient<String> resultCardHistory = server.getCardHistory(splittedCommand[1],splittedCommand[2]);
                    info.getObjectOutputStream().writeObject(resultCardHistory);
                    break;
                case "readchat":
                    String resultReadChat = server.readChat(splittedCommand[1]);
                    info.getObjectOutputStream().writeObject(resultReadChat);
                    break;
                case "sendchatmsg":
                    String resultSendMsg = server.sendChatMsg(splittedCommand[1]);
                    info.getObjectOutputStream().writeObject(resultSendMsg);
                    break;
                case "cancelproject":
                    String resultCancelProject = server.cancelProject(splittedCommand[1]);
                    info.getObjectOutputStream().writeObject(resultCancelProject);
                    break;
            }
        } catch (Exception e){
            stop = true;
            System.out.println("Disconnessione improvvisa");
        }

    }

    public ClientInfo getInfo() {
        return info;
    }

    public void setInfo(ClientInfo info) {
        this.info = info;
    }

    public User getClientUser() {
        return clientUser;
    }

    public void setClientUser(User clientUser) {
        this.clientUser = clientUser;
    }

    public SignedUpUsers getSignedUpUsers() {
        return SignedUpUsers;
    }

    public void setSignedUpUsers(SignedUpUsers SignedUpUsers) {
        this.SignedUpUsers = SignedUpUsers;
    }

}
