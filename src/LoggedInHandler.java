import Model.Card;
import Model.Project;
import Model.User;
import java.io.IOException;
import java.net.SocketException;
import java.rmi.RemoteException;
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

    public LoggedInHandler(ClientInfo info, TCPServer server, SignedUpUsers userList)
    {
        this.SignedUpUsers = userList;
        this.info = info;
        this.server = server;
    }

    public void run()
    {
        try {
            while (!stop) {
                start();
            }
        }catch (Exception e){
            stop = true;
            System.out.println("Chiusura inaspettata");
        }
    }

    public void start() throws RemoteException {
        String[] command;
        try{
            command = (String[]) info.getObjectInputStream().readObject();
            System.out.println("command: " + command[0]);
            switch (command[0]){
                case "login":
                    clientUser = (User) info.getObjectInputStream().readObject();
                    if(SignedUpUsers.isValid(clientUser)){
                        System.out.println("connessione accettata da: " + clientUser.getName());
                        setClientUser(clientUser);
                        setSignedUpUsers(SignedUpUsers);
                    } else {
                        System.err.println("Login non accettata da: " + clientUser.getName());
                    }
                    Login<UserAndStatus> resultLogin = server.login(clientUser, clientUser.getName());
                    info.getObjectOutputStream().writeObject(resultLogin);
                    break;
                case "logout":
                    String resultLogout = server.logout(command[1], clientUser.getName());
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
                    ToClientProject resultCreateProject = new ToClientProject("" , null);
                    if(command.length > 2){
                        resultCreateProject.setMessage("Hai inserito troppi argomenti per questo comando");
                        info.getObjectOutputStream().writeObject(resultCreateProject);
                    }else if(command.length < 2){
                        resultCreateProject.setMessage("Hai inserito pochi argomenti per questo comando");
                        info.getObjectOutputStream().writeObject(resultCreateProject);
                    }else{
                        resultCreateProject = server.createProject(command[1],clientUser.getName());
                        info.getObjectOutputStream().writeObject(resultCreateProject);
                    }
                    break;
                case "addmember":
                    String resultCreateMember;
                    if(command.length > 3){
                        resultCreateMember = "Hai inserito troppi argomenti per questo comando";
                        info.getObjectOutputStream().writeObject(resultCreateMember);
                    }else if(command.length < 3){
                        resultCreateMember = "Hai inserito pochi argomenti per questo comando";
                        info.getObjectOutputStream().writeObject(resultCreateMember);
                    }else{
                        resultCreateMember = server.addMember(command[1],command[2], clientUser.getName());
                        info.getObjectOutputStream().writeObject(resultCreateMember);
                    }
                    break;
                case "showmembers":
                    ToClient<String> resultShowMembers = new ToClient<>("",null);
                    if(command.length > 2){
                        resultShowMembers.setMessage("Hai inserito troppi argomenti per questo messaggio");
                        info.getObjectOutputStream().writeObject(resultShowMembers);
                    }else if(command.length < 2){
                        resultShowMembers.setMessage("Hai inserito pochi argomenti per questo messaggio");
                        info.getObjectOutputStream().writeObject(resultShowMembers);
                    }else{
                        resultShowMembers = server.showMembers(command[1], clientUser.getName());
                        info.getObjectOutputStream().writeObject(resultShowMembers);
                    }
                    break;
                case "showcards":
                    ToClient<Card> resultShowCards = new ToClient<>("",null);
                    if(command.length>2){
                        resultShowCards.setMessage("Hai inserito troppi argomenti per questo comando");
                        info.getObjectOutputStream().writeObject(resultShowCards);
                    }
                    else if(command.length<2){
                        resultShowCards.setMessage("Hai inserito pochi argomenti per questo comando");
                        info.getObjectOutputStream().writeObject(resultShowCards);
                    }
                    else{
                        resultShowCards = server.showCards(command[1], clientUser.getName());

                    }
                    info.getObjectOutputStream().writeObject(resultShowCards);

                    break;
                case "showcard":
                    ToClient<Card> resultShowCard = new ToClient<>("",null);
                    if(command.length>3){
                        resultShowCard.setMessage("hai inserito troppi argomenti per questo comando");
                        info.getObjectOutputStream().writeObject(resultShowCard);
                    }
                    else if(command.length<3) {
                        resultShowCard.setMessage("hai inserito pochi argomenti per questo comando");
                        info.getObjectOutputStream().writeObject(resultShowCard);
                    }
                    else{
                        resultShowCard = server.showCard(command[1],command[2], clientUser.getName());

                    }
                    info.getObjectOutputStream().writeObject(resultShowCard);
                    break;
                case "addcard":
                    String resultAddCard;
                    if(command.length>4){
                        resultAddCard = "hai inserito troppi argomenti per questo comando";
                        info.getObjectOutputStream().writeObject(resultAddCard);
                    }
                    else if(command.length<4) {
                        resultAddCard = "hai inserito pochi argomenti per questo comando";
                        info.getObjectOutputStream().writeObject(resultAddCard);
                    }
                    else{
                        resultAddCard = server.addCard(command[1],command[2],command[3], clientUser.getName());

                    }
                    info.getObjectOutputStream().writeObject(resultAddCard);
                    break;
                case "movecard":
                    String resultMoveCard;
                    if(command.length>5) {
                        resultMoveCard = "hai inserito troppi argomenti per questo comando";
                        info.getObjectOutputStream().writeObject(resultMoveCard);
                    }
                    else if(command.length<5) {
                        resultMoveCard = "hai inserito pochi argomenti per questo comando";
                        info.getObjectOutputStream().writeObject(resultMoveCard);
                    }
                    else {
                        resultMoveCard = server.moveCard(command[1], command[2], command[3], command[4], clientUser.getName());
                        info.getObjectOutputStream().writeObject(resultMoveCard);
                    }
                    break;
                case "getcardhistory":
                    ToClient<String> resultCardHistory = new ToClient<>("",null);
                    if(command.length > 3){
                        resultCardHistory.setMessage("Hai inserito troppi argomenti per questo comando");
                        info.getObjectOutputStream().writeObject(resultCardHistory);
                    }
                    else if(command.length < 3){
                        resultCardHistory.setMessage("Hai inserito pochi argomenti per questo comando");
                        info.getObjectOutputStream().writeObject(resultCardHistory);
                    }
                    else{
                        resultCardHistory = server.getCardHistory(command[1],command[2], clientUser.getName());
                        info.getObjectOutputStream().writeObject(resultCardHistory);
                    }

                    break;
                case "readchat":
                    ToClientChat resultReadChat = new ToClientChat("",null);
                    if(command.length > 2){
                        resultReadChat.setMessage("Hai inserito troppi argomenti per questo comando");
                        info.getObjectOutputStream().writeObject(resultReadChat);
                    }
                    else if(command.length < 2){
                        resultReadChat.setMessage("Hai inserito pochi argomenti per questo comando");
                        info.getObjectOutputStream().writeObject(resultReadChat);
                    }
                    else{
                        resultReadChat = server.readChat(command[1],clientUser.getName());
                        info.getObjectOutputStream().writeObject(resultReadChat);
                    }
                    break;
                case "sendchatmsg":
                    ToClientChat resultSendMsg = new ToClientChat("",null);
                    if(command.length > 2){
                        resultSendMsg.setMessage("Hai inserito troppi argomenti per questo comando");
                        info.getObjectOutputStream().writeObject(resultSendMsg);
                    }
                    else if(command.length < 2){
                        resultSendMsg.setMessage("Hai inserito pochi argomenti per questo comando");
                        info.getObjectOutputStream().writeObject(resultSendMsg);
                    }
                    else{
                        resultSendMsg = server.sendChatMsg(command[1], clientUser.getName());
                        info.getObjectOutputStream().writeObject(resultSendMsg);
                    }
                    break;
                case "cancelproject":
                    String resultCancelProject;
                    if(command.length > 2){
                        resultCancelProject = "Hai inserito troppi argomenti per questo comando";
                        info.getObjectOutputStream().writeObject(resultCancelProject);
                    }
                    else if(command.length < 2){
                        resultCancelProject = "Hai inserito pochi argomenti per questo comando";
                        info.getObjectOutputStream().writeObject(resultCancelProject);
                    }else{
                        resultCancelProject = server.cancelProject(command[1], clientUser.getName());
                        info.getObjectOutputStream().writeObject(resultCancelProject);
                    }
                    break;

                default:
                    String otherCommands = "Comando non valido";
                    info.getObjectOutputStream().writeObject(otherCommands);
                    System.out.println(otherCommands);
                    break;
            }
        } catch (Exception e){
            stop = true;
            server.logout(clientUser.getName(),clientUser.getName());
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

