package de.keawe.keawallet;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;

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

    private void gotoAddAccount(View v) {
        Intent addAccount = new Intent(this,AddAccount.class);
        startActivity(addAccount);
    }
}
