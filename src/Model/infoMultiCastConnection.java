package Model;

import java.io.Serializable;
import java.net.MulticastSocket;

/**
 * Informazioni sulle connessioni multicast
 */
public class infoMultiCastConnection implements Serializable{
    private MulticastSocket multicastsocket;
    private int port;
    private String mAddress;

    public infoMultiCastConnection(MulticastSocket multicastsocket, int port, String mAddress) {
        super();
        this.multicastsocket = multicastsocket;
        this.port = port;
        this.mAddress = mAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getmAddress() {
        return mAddress;
    }

    public void setmAddress(String mAddress) {
        this.mAddress = mAddress;
    }

    public MulticastSocket getMulticastsocket() {
        return multicastsocket;
    }

    public void setMulticastsocket(MulticastSocket multicastsocket) {
        this.multicastsocket = multicastsocket;
    }
}
