package com.suryaviyyapu.SecureBox;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.suryaviyyapu.SecureBox.Utils.AESUtils;
import com.himanshurawat.hasher.HashType;
import com.himanshurawat.hasher.Hasher;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Add extends AppCompatActivity implements View.OnClickListener {
    public static final String EXTRA_PROVIDER_NAME = "com.suryaviyyapu.SecureBox.EXTRA_PROVIDER_NAME";
    public static final String EXTRA_PROVIDER = "com.suryaviyyapu.SecureBox.EXTRA_PROVIDER";
    public static final String EXTRA_ENCRYPT = "com.suryaviyyapu.SecureBox.EXTRA_ENCRYPT";
    public static final String EXTRA_EMAIL = "com.suryaviyyapu.SecureBox.EXTRA_EMAIL";
    public static final String EXTRA_IV = "com.suryaviyyapu.SecureBox.EXTRA_IV";
    public static final String EXTRA_SALT = "com.suryaviyyapu.SecureBox.EXTRA_SALT";
    public static final String PASSWORD = "";
    final String PREFS_NAME = "appEssentials";
    String[] providersEmail = {
            "其他","Gmail","Github", "Spotify"
    };
    String[] providersSocial = {
             "其他","Facebook", "Instagram", "Twitter", "Medium"
    };
    MasterKey masterKey = null;
    String providerNameString, passwordFromCOPY;
    Button add_button;
    Spinner providerName;
    TextView prov_tv;
    String provider;
    SharedPreferences sharedPreferences = null;
    String PREF_KEY_SECURE_CORE_MODE = "SECURE_CORE";
    private EditText email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //ProgressBar progressBar = findViewById(R.id.progress_bar);
        providerName = findViewById(R.id.provider_name);
        email = findViewById(R.id.add_email);
        password = findViewById(R.id.add_password);
        CheckBox checkBox = findViewById(R.id.add_show_password);
        add_button = findViewById(R.id.add_record);
        prov_tv = findViewById(R.id.prov_tv);


        // Encrypted SharedPrefs
        try {
            //x.security
            masterKey = new MasterKey.Builder(getApplicationContext(), MasterKey.DEFAULT_MASTER_KEY_ALIAS)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            //init sharedPef
            sharedPreferences = EncryptedSharedPreferences.create(
                    getApplicationContext(),
                    PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }

        if (sharedPreferences.getBoolean(PREF_KEY_SECURE_CORE_MODE, false)) {

            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        }

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    password.setInputType(InputType.TYPE_NUMBER_VARIATION_PASSWORD);
                } else {
                    password.setInputType(129);
                }
            }
        });

        add_button.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        provider = getIntent().getStringExtra(EXTRA_PROVIDER);
        if (provider == null)
            provider = "mail";
        passwordFromCOPY = getIntent().getStringExtra(PASSWORD);
        assert provider != null;
        switch (provider) {
            case "social":
                email.setHint("帳號/email");
                ArrayAdapter arrayAdapterSocial = new ArrayAdapter(this, android.R.layout.simple_spinner_item, providersSocial);
                arrayAdapterSocial.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                //Setting the ArrayAdapter data on the Spinner
                providerName.setAdapter(arrayAdapterSocial);
                providerName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        providerNameString = parent.getItemAtPosition(position).toString();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
                break;
            case "wifi":
                prov_tv.setVisibility(View.GONE);
                providerName.setVisibility(View.GONE);
                email.setHint("SSID");
                break;
            default:
                email.setHint("Email");
                password.setText(passwordFromCOPY);
                ArrayAdapter arrayAdapterEmail = new ArrayAdapter(this, android.R.layout.simple_spinner_item, providersEmail);
                arrayAdapterEmail.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                //Setting the ArrayAdapter data on the Spinner
                providerName.setAdapter(arrayAdapterEmail);
                providerName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        providerNameString = parent.getItemAtPosition(position).toString();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
                break;
        }

    }

    private void save_data() {
        String text_email, text_password;
        text_email = email.getText().toString();
        text_password = password.getText().toString();
        String sha = sharedPreferences.getString("HASH", "0");
        String HASH = Hasher.Companion.hash(sha, HashType.MD5);

        if (provider.equals("mail")) {
            if (text_email.trim().isEmpty()) {
                email.setError("Required");
                email.requestFocus();
                return;
            }

        }
        if (text_password.trim().isEmpty()) {
            password.setError("Required");
            password.requestFocus();
            return;
        }
        Intent intent = new Intent();
        intent.putExtra(EXTRA_PROVIDER_NAME, providerNameString);
        // AES UTILS ENC and DEC
        try {
            String encEmail = AESUtils.encrypt(text_email, HASH);
            String encPass = AESUtils.encrypt(text_password, HASH);
            intent.putExtra(EXTRA_EMAIL, encEmail);
            intent.putExtra(EXTRA_ENCRYPT, encPass);
        } catch (Exception e) {
            e.printStackTrace();
        }
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.add_record) {
            save_data();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; goto parent activity.
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}