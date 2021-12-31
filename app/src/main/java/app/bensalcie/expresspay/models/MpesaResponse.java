package app.bensalcie.expresspay.models;

public class MpesaResponse {
    private Body Body;

    public app.bensalcie.expresspay.models.Body getBody() {
        return Body;
    }

    public void setBody(app.bensalcie.expresspay.models.Body body) {
        Body = body;
    }

    public MpesaResponse(app.bensalcie.expresspay.models.Body body) {
        Body = body;
    }
}
