package de.keawe.keawallet;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import de.keawe.keawallet.objects.Globals;

public class TransactionList extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Globals.setAppcontext(getApplicationContext());
        setContentView(R.layout.activity_transaction_list);

        ImageButton button = (ImageButton)findViewById(R.id.addAccountButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoAddAccount();
            }
        });
    }

    private void gotoAddAccount() {
        Intent addAccount = new Intent(this,AddAccount.class);
        startActivity(addAccount);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater findMenuItems = getMenuInflater();
        findMenuItems.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.fetch_transactions) {
            Intent fetchTransactions = new Intent(this,PasswordDialog.class);
            startActivity(fetchTransactions);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
