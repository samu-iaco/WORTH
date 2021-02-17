import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ServerMain {
    private static final int PORT_RMI = 5000;


    public static void main(String[] args) {
        try{
            //avvio del server RMI
            RMI_login_Class server = new RMI_login_Class(); //devo creare il file con la lista degli utenti
            //Interfaccia remota
            Registry registry = LocateRegistry.createRegistry(PORT_RMI);
            registry.rebind("SignUp" , server);
            //parte TCP

        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }
}
