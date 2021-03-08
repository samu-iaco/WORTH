package Model;

import java.io.Serializable;

public class infoMultiCastConnection implements Serializable {
    private int port;
    private String mAddress;

    public infoMultiCastConnection(int port, String mAddress) {
        super();
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
}
