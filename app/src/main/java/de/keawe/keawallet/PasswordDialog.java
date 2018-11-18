package de.keawe.keawallet;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import de.keawe.keawallet.objects.Globals;
import de.keawe.keawallet.objects.database.Settings;

import static de.keawe.keawallet.objects.Globals.byteArrayToHexString;
import static de.keawe.keawallet.objects.Globals.hash;

public class PasswordDialog extends AppCompatActivity {

    private byte[] storedKeyHash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.password_title);
        setContentView(R.layout.activity_password_dialog);


        final EditText input = (EditText) findViewById(R.id.password_input);
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

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

                byte[] encryptionKey = Globals.hash(pass);
                String encryptionKeyAsHex = Globals.byteArrayToHexString(encryptionKey);

                byte[] keyHash = Globals.hash(encryptionKey);
                String keyHashAsHex = Globals.byteArrayToHexString(keyHash);

                if (storedKeyHash == null){
                    Settings.set(Settings.PASSWORD_HASH,keyHashAsHex);
                    storedKeyHash = keyHash;
                }

                Log.d(Globals.ENCRYPTION,"Stored key hash is 0x"+Globals.byteArrayToHexString(storedKeyHash));

                if (byteArrayToHexString(storedKeyHash).equals(keyHashAsHex)) {
                    Globals.setEncryptionKey(encryptionKey);
                    PasswordDialog.this.finish();
                } else {
                    Toast.makeText(PasswordDialog.this,R.string.wrong_password,Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Globals.d("Resuming PasswordDialog");
        String keyHashAsHex = Settings.getString(Settings.PASSWORD_HASH);
        storedKeyHash = keyHashAsHex == null? null : Globals.hexStringToByteArray(keyHashAsHex);
        ((TextView) findViewById(R.id.password_explanation)).setText(storedKeyHash == null?R.string.password_explanation:R.string.enter_password);
        ((Button) findViewById(R.id.store_password_button)).setText(storedKeyHash == null?R.string.store_password:R.string.unlock_database);
    }
}
