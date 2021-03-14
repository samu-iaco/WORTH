import Model.infoMultiCastConnection;

import java.io.Serializable;
import java.util.ArrayList;

public class Login<T> extends ToClient<T> implements Serializable {
    private ArrayList<infoMultiCastConnection> multicast;

    public Login(String message , ArrayList<T> list, ArrayList<infoMultiCastConnection> multicast) {
        super(message,list);
        this.multicast = multicast;
    }

    public ArrayList<infoMultiCastConnection> getMulticast() {
        return multicast;
    }

    public void setMulticast(ArrayList<infoMultiCastConnection> multicast) {
        this.multicast = multicast;
    }
}
