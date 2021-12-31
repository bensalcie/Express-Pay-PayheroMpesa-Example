package app.bensalcie.expresspay.interfaces;

public interface MpesaListener {
      default void sendSuccesfull(String amount, String phone, String date, String receipt){

    }
     default void  sendFailed(String reason){

    }
}
