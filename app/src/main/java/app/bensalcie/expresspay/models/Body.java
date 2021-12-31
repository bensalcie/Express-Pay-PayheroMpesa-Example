package app.bensalcie.expresspay.models;

public class Body {
    private StkCallback stkCallback;

    public StkCallback getStkCallback() {
        return stkCallback;
    }

    public void setStkCallback(StkCallback stkCallback) {
        this.stkCallback = stkCallback;
    }

    public Body(StkCallback stkCallback) {
        this.stkCallback = stkCallback;
    }
}
