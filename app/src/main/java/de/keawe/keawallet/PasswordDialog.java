package de.keawe.keawallet;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import de.keawe.keawallet.objects.Globals;
import de.keawe.keawallet.objects.database.Settings;

public class PasswordDialog extends AppCompatActivity {

    private String stored_key_hash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_dialog);

        stored_key_hash = (new Settings(PasswordDialog.this)).getString(Settings.PASSWORD_HASH);

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

                String enryptionKey = hash(input.getText().toString());
                System.out.println("encryption key is "+enryptionKey);
                String keyHash = hash(enryptionKey);
                System.out.println("key hash is "+keyHash   );
                if (stored_key_hash == null){
                    (new Settings(PasswordDialog.this)).set(Settings.PASSWORD_HASH,keyHash);
                    stored_key_hash = keyHash;
                }
                if (stored_key_hash.equals(keyHash)) {
                    Globals.encryption_key = enryptionKey;
                } else {
                    Toast.makeText(PasswordDialog.this,R.string.wrong_password,Toast.LENGTH_LONG).show();
                }

                PasswordDialog.this.finish();
            }
        });
    }

    private String hash(String s) {
        try {
            System.out.println("Hashing "+s);
            MessageDigest digest = MessageDigest.getInstance("MD5");
            System.out.println("Using MD5 sum as hash algorithm is discouraged!");
            digest.update(s.getBytes());
            return new String(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
}
