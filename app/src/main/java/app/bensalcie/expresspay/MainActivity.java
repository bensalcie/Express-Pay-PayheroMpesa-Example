package app.bensalcie.expresspay;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

import bensalcie.payhero.mpesa.mpesa.model.AccessToken;
import bensalcie.payhero.mpesa.mpesa.model.STKPush;
import bensalcie.payhero.mpesa.mpesa.model.STKResponse;
import bensalcie.payhero.mpesa.mpesa.services.DarajaApiClient;
import bensalcie.payhero.mpesa.mpesa.services.Environment;
import bensalcie.payhero.mpesa.mpesa.services.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private DarajaApiClient darajaApiClient;

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
        ProgressDialog d = new ProgressDialog(this);
        d.setTitle("Processing");
        d.setMessage("Making payment...please wait");
        d.show();

        String timestamp = Utils.INSTANCE.getTimestamp();
        STKPush stkPush = new STKPush("PAY TO CHURCH",amount,"174379","https://mydomain.com/path",
                Objects.requireNonNull(Utils.INSTANCE.sanitizePhoneNumber(phone)), "174379", Objects.requireNonNull(Utils.INSTANCE.getPassword("174379",
                "bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919", Objects.requireNonNull(timestamp)))
                , Objects.requireNonNull(Utils.INSTANCE.sanitizePhoneNumber(phone)), timestamp,"Trans. desc",Environment.TransactionType.CustomerPayBillOnline);
        darajaApiClient.setGetAccessToken(false);
        Objects.requireNonNull(darajaApiClient.mpesaService()).sendPush(stkPush).enqueue(new Callback<STKResponse>() {
            @Override
            public void onResponse(@NonNull Call<STKResponse> call, @NonNull Response<STKResponse> response) {
                //process response here.
                //You get things like:
                //handle response here
                //response contains CheckoutRequestID,CustomerMessage,MerchantRequestID,ResponseCode,ResponseDescription


                d.dismiss();
                Toast.makeText(MainActivity.this, "Response: "+response.body(), Toast.LENGTH_LONG).show();
                Log.d("PAYMENTS", "onResponse: "+response.body());
                if (response.body() != null) {
                    Log.d("PAYMENTS", "onResponse: Time to process, confirm etc : "+response.body().component1());

                }

            }

            @Override
            public void onFailure(@NonNull Call<STKResponse> call, @NonNull Throwable t) {
                t.printStackTrace();
                Log.d("PAYMENTS", "onResponse: "+t.toString());


            }
        });



    }
}