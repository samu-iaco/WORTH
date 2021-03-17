import Model.User;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * classe che implementa i metodi RMI
 */
public class RMI_register_Class extends UnicastRemoteObject implements RMI_register_Interface {
    SignedUpUsers userList;
    private List<InfoCallback> clients;
    private ArrayList<User> dataUsers;


    public RMI_register_Class(SignedUpUsers userList) throws RemoteException {
        super();
        this.userList = userList;
        clients = new ArrayList<>();
        this.dataUsers = new ArrayList<>();
        userList.getList().forEach((s, user) -> {
            synchronized (user){
                dataUsers.add(user);
            }
        });
    }

    @Override
    public synchronized String register(String nickUtente, String password) throws RemoteException {


        if(nickUtente.isEmpty() || password.isEmpty()) {
            System.err.println("Il nome utente o la password non possono essere vuoti");
            throw new IllegalArgumentException("Nome utente o password vuoti");
        }

        User user = new User(nickUtente,password);

        if(userList.addUser(user)) {
            userList.store();
            return "OK"; //OK se l'utente viene registrato
        }

        return null;    //null se l'utente non viene registrato
    }


    @Override
    public synchronized void registerForCallback (Notify_Interface ClientInterface, String nickUtente) throws RemoteException {
        boolean contains = clients.stream()
                .anyMatch(client -> ClientInterface.equals(client.getClient()));
        if (!contains){
            clients.add(new InfoCallback(ClientInterface,nickUtente));
            System.out.println("Aggiunto un nuovo utente alla callback");
        }
    }

    public void update(String nickName, String status) throws RemoteException {
        doCallbacks(nickName,status);
    }

    private synchronized void doCallbacks(String nickName, String status) throws RemoteException {
        LinkedList<Notify_Interface> errors = new LinkedList<>();
        System.out.println("callback iniziate.");
        for (InfoCallback callbackinfoUser : clients) {
            Notify_Interface client = callbackinfoUser.getClient();
            try {
                client.notifyEvent(nickName, status);
            } catch (RemoteException e) {
                errors.add(client);
            }
        }
        if(!errors.isEmpty()) { //se c'è un errore
            System.out.println("errore nella registrazione di un client alla callback");
            for(Notify_Interface Ne : errors) unregisterForCallback(Ne);
        }
        System.out.println("callbacks completate.");
    }

    public synchronized void unregisterForCallback(Notify_Interface Client) throws RemoteException {
        InfoCallback user = clients.stream()
                .filter(client -> Client.equals(client.getClient()))
                .findAny()
                .orElse(null);
        if (user!=null) {
            clients.remove(user);
            System.out.println("client rimosso dalla callback");
        }
        else System.out.println("errore durante la rimozione del client dalla callback");
    }

}
