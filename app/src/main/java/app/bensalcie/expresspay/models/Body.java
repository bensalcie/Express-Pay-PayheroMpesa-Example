package app.bensalcie.expresspay.models;

public class Body {
    private StkCallback StkCallback;

    public app.bensalcie.expresspay.models.StkCallback getStkCallback() {
        return StkCallback;
    }

    public void setStkCallback(app.bensalcie.expresspay.models.StkCallback stkCallback) {
        StkCallback = stkCallback;
    }

    public Body(app.bensalcie.expresspay.models.StkCallback stkCallback) {
        StkCallback = stkCallback;
    }
}
