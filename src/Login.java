import Model.InfoMultiCastConnection;

import java.io.Serializable;
import java.util.ArrayList;

public class Login<T> extends ToClient<T> implements Serializable {
    private ArrayList<InfoMultiCastConnection> multicast;

    public Login(String message , ArrayList<T> list, ArrayList<InfoMultiCastConnection> multicast) {
        super(message,list);
        this.multicast = multicast;
    }

    public ArrayList<InfoMultiCastConnection> getMulticast() {
        return multicast;
    }

    public void setMulticast(ArrayList<InfoMultiCastConnection> multicast) {
        this.multicast = multicast;
    }
}
