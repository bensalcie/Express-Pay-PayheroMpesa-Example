package app.bensalcie.expresspay;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Objects;

import app.bensalcie.expresspay.interfaces.MpesaListener;
import bensalcie.payhero.mpesa.mpesa.model.AccessToken;
import bensalcie.payhero.mpesa.mpesa.model.STKPush;
import bensalcie.payhero.mpesa.mpesa.model.STKResponse;
import bensalcie.payhero.mpesa.mpesa.services.DarajaApiClient;
import bensalcie.payhero.mpesa.mpesa.services.Environment;
import bensalcie.payhero.mpesa.mpesa.services.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements MpesaListener {
    private DarajaApiClient darajaApiClient;

    ProgressDialog mProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        darajaApiClient = new DarajaApiClient(
                "tENEMaFsLNKzAMRycuyPmd02ABsCv8IU",
                "hPHKZVS5VJCapkQf",
                Environment.SANDBOX
        );
        darajaApiClient.setIsDebug(true);
        getAccessToken();//make request availabe and ready for processing.
        mProgressDialog = new ProgressDialog(MainActivity.this);
        mProgressDialog.setTitle("Processing");
        mProgressDialog.setMessage("Making payment...please wait");

    }





    private void getAccessToken() {
        darajaApiClient.setGetAccessToken(true);
        Objects.requireNonNull(darajaApiClient.mpesaService()).getAccessToken().enqueue(new Callback<AccessToken>() {
            @Override
            public void onResponse(@NonNull Call<AccessToken> call, @NonNull Response<AccessToken> response) {
                if (response.isSuccessful()) {
                    darajaApiClient.setAuthToken(Objects.requireNonNull(response.body()).getAccessToken());
                }
            }

            @Override
            public void onFailure(@NonNull Call<AccessToken> call, @NonNull Throwable t) {

            }
        });
    }

    public void showPaymentDialog(View view) {
        Dialog d = new Dialog(this);
        d.setTitle("Make payment");
        d.setContentView(R.layout.payment_dialog);
        Window window =d.getWindow();
        window.setGravity(Gravity.CENTER);
        window.setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
        TextInputEditText etAmount = d.findViewById(R.id.etAmount);
        TextInputEditText etPhone = d.findViewById(R.id.etPhone);
        MaterialButton btnPay = d.findViewById(R.id.btnPay);

        btnPay.setOnClickListener(view1 -> {
            String amount = Objects.requireNonNull(etAmount.getText()).toString().trim();
            String phone = Objects.requireNonNull(etPhone.getText()).toString().trim();
            if (!TextUtils.isEmpty(amount) &&!TextUtils.isEmpty(phone) ) {
                processPayment(phone,amount);
            }
        });
        d.show();

    }

    private void processPayment(String phone, String amount) {



        mProgressDialog.show();


        String timestamp = Utils.INSTANCE.getTimestamp();
        STKPush stkPush = new STKPush("PAY TO CHURCH",amount,"174379","https://us-central1-dev-apps-6f6e8.cloudfunctions.net/api/javacallback",
                Objects.requireNonNull(Utils.INSTANCE.sanitizePhoneNumber(phone)), "174379", Objects.requireNonNull(Utils.INSTANCE.getPassword("174379",
                "bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919", Objects.requireNonNull(timestamp)))
                , Objects.requireNonNull(Utils.INSTANCE.sanitizePhoneNumber(phone)), timestamp,"Trans. desc",Environment.TransactionType.CustomerPayBillOnline);
        darajaApiClient.setGetAccessToken(false);
        Objects.requireNonNull(darajaApiClient.mpesaService()).sendPush(stkPush).enqueue(new Callback<STKResponse>() {
            @Override
            public void onResponse(@NonNull Call<STKResponse> call, @NonNull Response<STKResponse> response) {
                Toast.makeText(MainActivity.this, "Response: "+response.body(), Toast.LENGTH_LONG).show();
                Log.d("PAYMENTS", "onResponse: "+response.body());
                if (response.body() != null) {

                    STKResponse stkResponse = response.body();
                    FirebaseMessaging.getInstance().subscribeToTopic(stkResponse.getCheckoutRequestID()).addOnCompleteListener(task -> {
                        if (task.isSuccessful()){
                            Log.d("PAYMENTS",
                                    "onResponse: Subscribed successfully  : "
                            );
                        }else{
                            Log.d("PAYMENTS",
                                    "onResponse: Subscribed unsuccessfull  : ${it.exception?.message}"
                            );
                        }
                    });


                }

            }

            @Override
            public void onFailure(@NonNull Call<STKResponse> call, @NonNull Throwable t) {
                t.printStackTrace();
                Log.d("PAYMENTS", "onResponse: "+t.toString());


            }
        });



    }

    @Override
    public void sendSuccesfull(String amount, String phone, String date, String receipt) {
        mProgressDialog.dismiss();
            Toast.makeText(
                    this, "Payment Succesfull\n" +
                            "Receipt: $receipt\n" +
                            "Date: $date\n" +
                            "Phone: $phone\n" +
                            "Amount: $amount", Toast.LENGTH_LONG
            ).show();


    }

    @Override
    public void sendFailed(String reason) {
        mProgressDialog.dismiss();

        Toast.makeText(
                    this, "Payment Failed\n" +
                            "Reason: $reason"
                    , Toast.LENGTH_LONG
            ).show();
        }


}