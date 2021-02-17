import Remote.Exception.UserAlreadyExistsException;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RMI_login_Class extends UnicastRemoteObject implements RMI_login_Interface {
    User user;

    protected RMI_login_Class(User user) throws RemoteException {
        super();
        this.user = user;
    }

    @Override
    public synchronized String register(String nickUtente, String password) throws RemoteException, UserAlreadyExistsException {
        System.out.println("Richiesta di registrazione da parte di: " + nickUtente);

        if(nickUtente.isEmpty() || password.isEmpty()) {
            System.err.println("Il nome utente o la password non possono essere vuoti");
            throw new IllegalArgumentException();
        }

        User user = new User(nickUtente,password);

        if(userAlreadyExist(user)) {
            System.err.println("Utente gia registrato");
            throw new UserAlreadyExistsException(); //se l'utente esiste già
        }
        else{
            users.add(user);
            update(nickName,"offline");
            try{
                mapper.writeValue(usersDir,users);
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Totale utenti registrati: " + users.size());
            return "OK";
        }

        return null;
    }

}
