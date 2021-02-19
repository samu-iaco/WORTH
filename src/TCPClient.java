import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.channels.SocketChannel;

public class TCPClient {
    private static final int PORT_TCP = 9999;
    private User user;
    private SocketChannel client;
    private ObjectOutputStream oos;

    public TCPClient(User user) throws IOException {
        this.user = user;
        InetSocketAddress hA = new InetSocketAddress("localhost", PORT_TCP);
        client = SocketChannel.open(hA);
        System.out.println("invio della richiesta al server...");
        oos = new ObjectOutputStream(client.socket().getOutputStream());
        // Invio le credenziali al server
        oos.writeObject(user);
    }

    /**
     *
     * @return l'utente connesso
     */
    public User getUser(){
        return this.user;
    }
}
