import Remote.Exception.UserAlreadyExistsException;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RMI_register_Class extends UnicastRemoteObject implements RMI_register_Interface {
    SignedUpUsers users;

    protected RMI_register_Class(SignedUpUsers users) throws RemoteException {
        super();
        this.users = users;
    }

    @Override
    public synchronized String register(String nickUtente, String password) throws RemoteException, UserAlreadyExistsException {
        System.out.println("Richiesta di registrazione da parte di: " + nickUtente);
        String result;
        if(nickUtente.isEmpty() || password.isEmpty()) {
            System.err.println("Il nome utente o la password non possono essere vuoti");
            throw new IllegalArgumentException("Nome utente o password vuoti");
        }

        User user = new User(nickUtente,password);

        if(users.addUser(user)) return "OK"; //OK se l'utente viene registrato

        return null;    //null se l'utente non viene registrato
    }

}
