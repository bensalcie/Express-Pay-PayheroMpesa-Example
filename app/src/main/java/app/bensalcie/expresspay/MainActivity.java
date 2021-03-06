package app.bensalcie.expresspay;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

import app.bensalcie.expresspay.models.Transaction;
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
    public static MainActivity mpesalistener;
    private DarajaApiClient darajaApiClient;

    ProgressDialog mProgressDialog;
    private String userId = "CHURCHUSER001";
    private DatabaseReference databaseReference;
    Dialog d;
    private RecyclerView productsList;
    private ProgressBar progressBar;



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
        d = new Dialog(this);
        mProgressDialog = new ProgressDialog(MainActivity.this);
        mProgressDialog.setTitle("Processing");
        mProgressDialog.setMessage("Making payment...please wait");
        mpesalistener = this;
        databaseReference = FirebaseDatabase.getInstance().getReference().child("CHURCHUSERS");
        productsList=findViewById(R.id.paymentlist);
        progressBar = findViewById(R.id.progresspayments);
        LinearLayoutManager gridLayoutManager=new LinearLayoutManager(this);
        productsList.setLayoutManager(gridLayoutManager);


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
        d.setTitle("Make payment");
        d.setContentView(R.layout.payment_dialog);
        Window window = d.getWindow();
        window.setGravity(Gravity.CENTER);
        window.setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
        TextInputEditText etAmount = d.findViewById(R.id.etAmount);
        TextInputEditText etPhone = d.findViewById(R.id.etPhone);
        MaterialButton btnPay = d.findViewById(R.id.btnPay);

        btnPay.setOnClickListener(view1 -> {
            String amount = Objects.requireNonNull(etAmount.getText()).toString().trim();
            String phone = Objects.requireNonNull(etPhone.getText()).toString().trim();
            if (!TextUtils.isEmpty(amount) && !TextUtils.isEmpty(phone)) {
                processPayment(phone, amount);
            }
        });
        d.show();

    }

    private void processPayment(String phone, String amount) {

        mProgressDialog.show();
        String timestamp = Utils.INSTANCE.getTimestamp();
        STKPush stkPush = new STKPush("PAY TO CHURCH", amount, "174379", "https://bensalcie.payherokenya.com/expresspay/callback.php",
                Objects.requireNonNull(Utils.INSTANCE.sanitizePhoneNumber(phone)), "174379", Objects.requireNonNull(Utils.INSTANCE.getPassword("174379",
                "bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919", Objects.requireNonNull(timestamp)))
                , Objects.requireNonNull(Utils.INSTANCE.sanitizePhoneNumber(phone)), timestamp, "Trans. desc", Environment.TransactionType.CustomerPayBillOnline);
        darajaApiClient.setGetAccessToken(false);
        Objects.requireNonNull(darajaApiClient.mpesaService()).sendPush(stkPush).enqueue(new Callback<STKResponse>() {
            @Override
            public void onResponse(@NonNull Call<STKResponse> call, @NonNull Response<STKResponse> response) {
//                Toast.makeText(MainActivity.this, "Response: "+response.body(), Toast.LENGTH_LONG).show();
                Log.d("PAYMENTS", "onResponse: " + response.body());
                if (response.body() != null) {
                    STKResponse stkResponse = response.body();
                    databaseReference.child("PAYMENTS").child(stkResponse.getCheckoutRequestID()).setValue(userId).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()){

                                listenToPayments(stkResponse.getCheckoutRequestID());
                            }else {
                                mProgressDialog.dismiss();
                                d.dismiss();

                                Toast.makeText(MainActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
                }

            }

            @Override
            public void onFailure(@NonNull Call<STKResponse> call, @NonNull Throwable t) {
                t.printStackTrace();
                Log.d("PAYMENTS", "onResponse: " + t.toString());


            }
        });


    }

    private void listenToPayments(String checkoutRequestID) {
        databaseReference.child("PAYMENTS").child(userId).child(checkoutRequestID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String status = String.valueOf(snapshot.child("status").getValue());
                if (status.contains("COMPLETED")){
                    mProgressDialog.dismiss();
                    d.dismiss();
                    Toast.makeText(MainActivity.this, "Transaction completed successfully", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }



    @Override
    protected void onStart() {
        super.onStart();
        progressBar.setVisibility(View.VISIBLE);
        databaseReference.keepSynced(true);
        FirebaseRecyclerOptions<Transaction> options = new FirebaseRecyclerOptions.Builder<Transaction>()
                .setQuery(databaseReference.child("PAYMENTS").child(userId), Transaction.class)
                .build();

        final FirebaseRecyclerAdapter<Transaction, productsViewHolder> adapter = new FirebaseRecyclerAdapter<Transaction, productsViewHolder>(options) {
            @SuppressLint("NewApi")
            @Override
            protected void onBindViewHolder(@NonNull productsViewHolder holder, int position, @NonNull final Transaction model) {

                // milliseconds

//                // Creating date format
//                DateFormat simple = new SimpleDateFormat("dd MMM yyyy HH:mm ", Locale.US);
//
//                // Creating date from milliseconds
//                // using Date() constructor
//                Date result = new Date(model.getTransactiondate());

                // Formatting Date according to the
                // given format
                holder.tvRefcode.setText(model.getStatus());
                holder.tvReBody.setText(model.getConfirmation_code()+"\nKSh. "+model.getAmount() + " Paid to XYZ CHURCH \nFrom : "+model.getMsisdn()+"\n"+model.getTransactiondate());



            }

            @NonNull
            @Override
            public productsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.mpesa_statement_item, viewGroup, false);
                productsViewHolder viewHolder = new productsViewHolder(view);


                return viewHolder;
            }
        };

        productsList.setAdapter(adapter);
        adapter.startListening();
        productsList.smoothScrollToPosition(0);
        progressBar.setVisibility(View.GONE);


    }

    public static class productsViewHolder extends RecyclerView.ViewHolder {
        private MaterialTextView tvRefcode, tvReBody;


        public productsViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRefcode = itemView.findViewById(R.id.tvRefcode);
            tvReBody = itemView.findViewById(R.id.tvReBody);


        }
    }
}