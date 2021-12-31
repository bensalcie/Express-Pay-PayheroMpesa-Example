package app.bensalcie.expresspay.models;

public class StkCallback {
    private CallbackMetadata CallbackMetadata;
    private String CheckoutRequestID,MerchantRequestID,ResultDesc;
    private int ResultCode;

    public StkCallback(app.bensalcie.expresspay.models.CallbackMetadata callbackMetadata, String checkoutRequestID, String merchantRequestID, String resultDesc, int resultCode) {
        CallbackMetadata = callbackMetadata;
        CheckoutRequestID = checkoutRequestID;
        MerchantRequestID = merchantRequestID;
        ResultDesc = resultDesc;
        ResultCode = resultCode;
    }

    public app.bensalcie.expresspay.models.CallbackMetadata getCallbackMetadata() {
        return CallbackMetadata;
    }

    public void setCallbackMetadata(app.bensalcie.expresspay.models.CallbackMetadata callbackMetadata) {
        CallbackMetadata = callbackMetadata;
    }

    public String getCheckoutRequestID() {
        return CheckoutRequestID;
    }

    public void setCheckoutRequestID(String checkoutRequestID) {
        CheckoutRequestID = checkoutRequestID;
    }

    public String getMerchantRequestID() {
        return MerchantRequestID;
    }

    public void setMerchantRequestID(String merchantRequestID) {
        MerchantRequestID = merchantRequestID;
    }

    public String getResultDesc() {
        return ResultDesc;
    }

    public void setResultDesc(String resultDesc) {
        ResultDesc = resultDesc;
    }

    public int getResultCode() {
        return ResultCode;
    }

    public void setResultCode(int resultCode) {
        ResultCode = resultCode;
    }
}
