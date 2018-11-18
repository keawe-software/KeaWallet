package de.keawe.keawallet;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import org.kapott.hbci.GV_Result.GVRKUms;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import de.keawe.keawallet.objects.Globals;
import de.keawe.keawallet.objects.database.BankAccount;
import de.keawe.keawallet.objects.database.BankLogin;

public class FetchTransactions extends AppCompatActivity {

    private static final int IDLE = 0;
    private static final int FETCHING = 1;
    private static int state = IDLE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.fetch_title);
        setContentView(R.layout.activity_fetch_transactions);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Globals.d("Resuming FetchTransactions. Enryption key is "+(Globals.encryption_key==null?"not ":"")+"set");
        if (Globals.encryption_key == null){
            Intent passwordDialog = new Intent(this,PasswordDialog.class);
            startActivity(passwordDialog);
            return;
        }

        if (state == IDLE){
            state = FETCHING;
            fetchTransactions();
        }


    }

    public void fetchTransactions(){
        final Vector<BankLogin> logins = BankLogin.loadAll();
        if (logins.isEmpty()) Toast.makeText(this,R.string.no_accouts,Toast.LENGTH_LONG).show();

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
            for (BankLogin login : logins){
                Globals.d("Fetching transactions for: "+login);
                for (BankAccount account : login.accounts()){
                    Globals.d("Fetching transactions for: "+account);
                    try {
                        GVRKUms transaction = account.fetchNewTransactions();
                        System.out.println(transaction);
                    } catch (ParserConfigurationException e) {
                        e.printStackTrace();
                    } catch (TransformerException e) {
                        e.printStackTrace();
                    } catch (SAXException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            state = IDLE;
            return null;
            }
        };
        task.execute();


    }
}
