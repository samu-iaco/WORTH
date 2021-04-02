import java.io.Serializable;

public class ToClientChat implements Serializable {
    private String message;
    private String multicastChat;

    public ToClientChat(String message, String multicastChat) {
        this.message = message;
        this.multicastChat = multicastChat;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMulticastChat() {
        return multicastChat;
    }

    public void setMulticastChat(String multicastChat) {
        this.multicastChat = multicastChat;
    }
}
