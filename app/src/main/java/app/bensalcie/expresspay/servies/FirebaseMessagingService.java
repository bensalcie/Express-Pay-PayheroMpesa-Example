package app.bensalcie.expresspay.servies;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;


import java.util.List;

import app.bensalcie.expresspay.MainActivity;
import app.bensalcie.expresspay.models.Item;
import app.bensalcie.expresspay.models.MpesaResponse;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d("MessagingService", remoteMessage.getData().toString());

        String payload = remoteMessage.getData().get("payload");

        Gson gson = new Gson();

        MpesaResponse mpesaResponse = gson.fromJson(payload, MpesaResponse.class);

        Log.d("MessagingServiceSecond", mpesaResponse.toString());
            String id = mpesaResponse.getBody().getStkCallback().getCheckoutRequestID();
                if (mpesaResponse.getBody().getStkCallback().getResultCode() != 0) {
                    String reason = mpesaResponse.getBody().getStkCallback().getResultDesc();
                    MainActivity.mpesalistener.sendFailed(reason);
                    Log.d("MessagingServiceThird", "Operation Failed");
                }else{
                    Log.d("MessagingServiceThird", "Operation Success");

                    List<Item> list = mpesaResponse.getBody().getStkCallback().getCallbackMetadata().getItem();

                    String  receipt = "";
                    String date = "";
                    String phone = "";
                    String amount = "";

                    for (int i = 0; i<list.size();i++) {
                        Item item = list.get(i);
                        if (item.getName().equals("MpesaReceiptNumber")) {
                            receipt = item.getValue();
                        }
                        if (item.getName().equals("TransactionDate")) {
                            date = item.getValue();
                        }
                        if (item.getName().equals("PhoneNumber")) {
                            phone = item.getValue();

                        }
                        if (item.getName().equals("Amount")) {
                            amount = item.getValue();
                        }
                    }
                    MainActivity.mpesalistener.sendSuccesfull(amount, phone, date, receipt);
                    Log.d("MetaData", "\nReceipt: $receipt\nDate: $date\nPhone: $phone\nAmount: $amount");
                    //Log.d("NewDate", getDate(date.toLong()))
                }

                FirebaseMessaging.getInstance()
                        .unsubscribeFromTopic(id);







    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
    }
}