import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ServerMain {
    private static final int PORT_RMI = 5000;

    /**
     *  Questa classe astrae il file json contenente gli utenti registrati
     */
    private static SignedUpUsers usersList = new SignedUpUsers();

    public static void main(String[] args) {
        try{
            //avvio del server RMI
            RMI_register_Class server = new RMI_register_Class(usersList); //devo creare il file con la lista degli utenti
            //Interfaccia remota
            System.out.println("ciao");
            Registry registry = LocateRegistry.createRegistry(PORT_RMI);
            registry.rebind("SignUp" , server);
            //parte TCP
            new TCPServer(usersList);
            System.out.println("Server ready...");

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
