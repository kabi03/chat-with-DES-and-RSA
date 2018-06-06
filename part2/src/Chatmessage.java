import java.io.Serializable;

public class Chatmessage implements Serializable {

    protected static final long serialVersionUID = 1112122200L;

    private String message;
    private int nonce;

    Chatmessage(String message, int nonce){
        this.message = message;
        this.nonce = nonce;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setNonce(int nonce) {
        this.nonce = nonce;
    }

    public int getNonce() {
        return nonce;
    }
}
