package de.keawe.keawallet;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import de.keawe.keawallet.objects.Globals;
import de.keawe.keawallet.objects.database.Settings;

import static de.keawe.keawallet.objects.Globals.hash;

public class PasswordDialog extends AppCompatActivity {

    private byte[] stored_key_hash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_dialog);

        String pw_hash_string = Settings.getString(Settings.PASSWORD_HASH);
        stored_key_hash = pw_hash_string == null? null : pw_hash_string.getBytes();
        Log.d(Globals.ENCRYPTION,"Stored key hash is 0x"+Globals.byteArrayToHexString(stored_key_hash));

        final EditText input = (EditText) findViewById(R.id.password_input);
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                Button pw_btn = (Button) findViewById(R.id.store_password_button);
                pw_btn.setEnabled(!editable.toString().isEmpty());
            }

        });

        Button pw_btn = (Button) findViewById(R.id.store_password_button);
        pw_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String pass=input.getText().toString();
                Log.d(Globals.ENCRYPTION,"Password read: "+pass);

                byte[] enryptionKey = Globals.hash(pass);
                String encryptionKeyAsHex = Globals.byteArrayToHexString(enryptionKey);
                Log.d(Globals.ENCRYPTION,"Key generated: 0x"+encryptionKeyAsHex);

                byte[] keyHash = Globals.hash(enryptionKey);
                String keyHashAsHex = Globals.byteArrayToHexString(keyHash);
                Log.d(Globals.ENCRYPTION,"Key hash: 0x"+keyHashAsHex);

                if (stored_key_hash == null){
                    Settings.set(Settings.PASSWORD_HASH,keyHash);
                    Log.d(Globals.ENCRYPTION,"Saved key hash 0x"+keyHashAsHex+" to settings.");
                    stored_key_hash = keyHash;
                }
                if (stored_key_hash.equals(keyHash)) {
                    Log.d(Globals.ENCRYPTION,"Set global key to 0x"+encryptionKeyAsHex);
                    Globals.setEncryptionKey(enryptionKey);
                    PasswordDialog.this.finish();
                } else {
                    Toast.makeText(PasswordDialog.this,R.string.wrong_password,Toast.LENGTH_LONG).show();
                }
            }
        });
    }


}
