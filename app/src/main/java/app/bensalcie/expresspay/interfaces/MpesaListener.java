package app.bensalcie.expresspay.interfaces;

public interface MpesaListener {
       void sendSuccesfull(String amount, String phone, String date, String receipt);
      void  sendFailed(String reason);
}
