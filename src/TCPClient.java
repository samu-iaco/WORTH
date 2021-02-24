import Model.User;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.channels.SocketChannel;

public class TCPClient {
    private static final int PORT_TCP = 9999;
    private User user;
    private SocketChannel client;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private Login<UserAndStatus> resultLogin;

    public TCPClient(User user) throws IOException, ClassNotFoundException {
        this.user = user;
        InetSocketAddress hA = new InetSocketAddress("localhost", PORT_TCP);
        client = SocketChannel.open(hA);
        System.out.println("invio della richiesta al server...");
        oos = new ObjectOutputStream(client.socket().getOutputStream());
        // Invio le credenziali al server
        oos.writeObject(user);
        ois = new ObjectInputStream(client.socket().getInputStream());
        this.resultLogin = (Login) ois.readObject();
    }

    /**
     *
     * @return l'utente connesso
     */
    public User getUser(){
        return this.user;
    }

    /**
     * @return risultato dell'operazione di login
     */
    public synchronized Boolean getResultLogin(){
        if(resultLogin.getMessage().equals("OK")){
            return true;
        }

        else return false;
    }

    public synchronized Login<UserAndStatus> getLogin(){
        return resultLogin;
    }

    public boolean logout(String userName){
        System.out.println("il bro: " + userName + " vuole fare il logout");
        return false;
    }

}
