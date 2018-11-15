package de.keawe.keawallet;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.IOException;

import de.keawe.keawallet.objects.CreditInstitute;

public class TransactionList extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_list);

        ImageButton button = (ImageButton)findViewById(R.id.addAccountButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoAddAccount(v);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            CreditInstitute[] institutes = CreditInstitute.getList(getAssets());
        } catch (IOException e) {
            Toast.makeText(this,R.string.institutes_read_error, Toast.LENGTH_LONG).show();
        }
    }

    private void gotoAddAccount(View v) {
        Intent addAccount = new Intent(this,AddAccount.class);
        startActivity(addAccount);
    }
}
