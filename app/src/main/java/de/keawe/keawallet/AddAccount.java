package de.keawe.keawallet;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import java.io.IOException;

import de.keawe.keawallet.objects.CreditInstitute;

public class AddAccount extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_account);
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            CreditInstitute[] institutes = CreditInstitute.getList(getAssets());
        } catch (IOException e) {
            Toast.makeText(this, R.string.institutes_read_error, Toast.LENGTH_LONG).show();
        }
    }
}
