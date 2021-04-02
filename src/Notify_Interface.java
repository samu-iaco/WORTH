import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Notify_Interface extends Remote {
    void notifyEvent(String userName, String status) throws RemoteException;

    /**
     * @param projectMulticast indirizzo multicast del progetto
     * @param projectPort porta del progetto
     * notifica l'aggiornamento delle informazioni di multicast del progetto
     */
    void notifyMulticastEvent(String projectMulticast, int projectPort) throws RemoteException;

    /**
     * @param projectMulticast indirizzo multicast del progetto
     * @param projectPort porta del progetto
     * notifica gli utenti della cancellazione del progetto
     */
    void notifyCancelProject(String projectMulticast, int projectPort) throws RemoteException;
}
