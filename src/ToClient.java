import java.io.Serializable;
import java.util.ArrayList;

public class ToClient<T> implements Serializable {
    private String message;
    private ArrayList<T> list;

    public ToClient(String message, ArrayList<T> list) {
        this.message = message;
        this.list = list;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ArrayList<T> getList() {
        return list;
    }

    public void setList(ArrayList<T> list) {
        this.list = list;
    }
}
