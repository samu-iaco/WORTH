import java.io.Serializable;
import java.util.ArrayList;

public class Login<T> extends ToClient<T> implements Serializable {

    public Login(String message , ArrayList<T> list) {
        super(message,list);
    }

}
