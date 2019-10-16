package shulamit.hila.letsmeet.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import shulamit.hila.letsmeet.R;

/**
 * the login screen display once
 */
public class LoginActivity extends AppCompatActivity {
    private static final String USERS =  "users";
    private static final String HISTORY_DB = "history_DB";
    private static final String SAVES_DB = "saves_DB";
    public static final String SHARED_PREFS = "user_details";
    public static String USER_TOKEN= "user_token";
    public static String USER_NAME = "user_name";
    public static String NUMBER = "number";

    private EditText txvNumber;
    private Button btnSendCode;
    private Button btnSendAgain;
    private EditText txvCode;
    private EditText txvName;
    private Button btnOk;
    private String phoneVerificationId;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks verificationCallbacks;
    private PhoneAuthProvider.ForceResendingToken resendingToken;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private ProgressDialog progressDialog;
    private String token;
    private SQLiteDatabase historyDB = null;
    private SQLiteDatabase savesDB = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        progressDialog = new ProgressDialog(this);
        setContentView(R.layout.activity_login);
        db = FirebaseFirestore.getInstance();
        txvNumber = findViewById(R.id.txv_number);
        txvName = findViewById(R.id.txv_user_name);
        btnSendCode = findViewById(R.id.btn_send_code);
        btnSendAgain = findViewById(R.id.btn_send_again);
        txvCode = findViewById(R.id.txv_code);
        btnOk = findViewById(R.id.btn_ok);
        btnSendAgain.setEnabled(false);
        btnOk.setEnabled(false);
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            return;
                        }
                        // Get new Instance ID token
                        token = task.getResult().getToken();
                    }
                });
        //set listeners
        btnSendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnSendCode.setEnabled(false);
                sendCode(v);//send the code to verify user
            }
        });

        btnSendAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resendCode(v);
            }
        });
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyCode(v);
            }
        });
        createSavesDB();//create Saves and History local databases
        createHistoryDB();
    }
    public void sendCode(View view) {

        String phoneNumber = txvNumber.getText().toString();

        setUpVerificationCallbacks();//set the listeners
        //send the code
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                verificationCallbacks);
    }

    private void setUpVerificationCallbacks() {

        verificationCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential credential) {
                        //verification success ->sign in
                        btnOk.setEnabled(false);
                        txvCode.setText("");
                        signInWithPhoneAuthCredential(credential);
                    }

                    @Override
                    public void onVerificationFailed(FirebaseException e) {
                        //if the verification failed tel the user now
                        if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            // Invalid request
                            Log.d("ver", "Invalid credential: "
                                    + e.getLocalizedMessage());
                        } else if (e instanceof FirebaseTooManyRequestsException) {
                            // SMS quota exceeded
                            Log.d("ver", "SMS Quota exceeded.");
                        }
                    }

                    @Override
                    public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                        //when the code sent in the database enable buttons
                        phoneVerificationId = verificationId;
                        resendingToken = token;
                        btnOk.setEnabled(true);
                        btnSendCode.setEnabled(false);
                        btnSendAgain.setEnabled(true);
                    }

                };
    }

    /**
     * send the code to verification
     * @param view
     */
    public void verifyCode(View view) {

        String code = txvCode.getText().toString();
        PhoneAuthCredential credential =
                PhoneAuthProvider.getCredential(phoneVerificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        progressDialog.setMessage("registering please wait...");
        progressDialog.show();
        firebaseAuth.signInWithCredential(credential)//sign in with phone number
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            txvCode.setText("");
                            btnSendAgain.setEnabled(false);
                            btnOk.setEnabled(false);
                            addUser();

                        } else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                Toast.makeText(LoginActivity.this,"this user allredy exist in the system", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
    }

    /*** send the code again
     * @param view
     */
    public void resendCode(View view) {

        String phoneNumber = txvNumber.getText().toString();

        setUpVerificationCallbacks();

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                60,
                TimeUnit.SECONDS,
                this,
                verificationCallbacks,
                resendingToken);
    }


    /***
     * add user to firebase
     */
    private void addUser(){
    Map<String, Object> number = new HashMap<>();
    number.put("number", txvNumber.getText().toString());
    number.put("token",token);

    db.collection(USERS).document(FirebaseAuth.getInstance().getCurrentUser().getUid())
            .set(number)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(USER_NAME, txvName.getText().toString());
                    editor.putString(NUMBER, txvNumber.getText().toString());
                    editor.putString(USER_TOKEN, token);
                    editor.commit();
                    Intent intent = new Intent(LoginActivity.this,MainActivity.class);// start the main activity
                    startActivity(intent);
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                }
            });

}
    public void createHistoryDB()
    {
        try
        {
            // Opens a current database or creates it
            // Pass the database name, designate that only this app can use it
            // and a DatabaseErrorHandler in the case of database corruption
            historyDB = openOrCreateDatabase(HISTORY_DB, MODE_PRIVATE, null);

            // build an SQL statement to create 'contacts' table (if not exists)
            String sql = "CREATE TABLE IF NOT EXISTS historyPoints (name VARCHAR, lat VARCHAR, lng VARCHAR);";
            historyDB.execSQL(sql);
        }

        catch(Exception e){
            Log.d("debug", "Error Creating Database");
        }

        // Make buttons clickable since the database was created
    }
    public void createSavesDB()
    {
        try
        {
            // Opens a current database or creates it
            // Pass the database name, designate that only this app can use it
            // and a DatabaseErrorHandler in the case of database corruption
            savesDB = openOrCreateDatabase(SAVES_DB, MODE_PRIVATE, null);

            // build an SQL statement to create 'contacts' table (if not exists)
            String sql = "CREATE TABLE IF NOT EXISTS savesPoints (name VARCHAR, lat VARCHAR, lng VARCHAR);";
            savesDB.execSQL(sql);

        }

        catch(Exception e){
            Log.d("debug", "Error Creating Database");
        }

        // Make buttons clickable since the database was created
    }

}
