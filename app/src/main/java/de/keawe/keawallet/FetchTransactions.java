package de.keawe.keawallet;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.kapott.hbci.GV_Result.GVRKUms;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Calendar;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import de.keawe.keawallet.objects.Globals;
import de.keawe.keawallet.objects.database.BankAccount;
import de.keawe.keawallet.objects.database.BankLogin;
import de.keawe.keawallet.objects.database.Category;
import de.keawe.keawallet.objects.database.Transaction;

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

    public static void recognizeTransaction(Transaction transaction, Vector<Transaction> categorizedTransactions){
        Transaction similarTransaction = transaction.findMostSimilarIn(categorizedTransactions);
        if (similarTransaction != null && transaction.compare(similarTransaction) > 0.001) { // transaction is sufficiently similar
            transaction.setMostSimilar(similarTransaction); // add a reference to the similar transaction
            Category category = similarTransaction.category(); // get the category from the similar transaction
            transaction.setCategory(category,true); // assign that category to the new transaction
            long dayDiff = (transaction.bdate() - similarTransaction.bdate()) / (1000 * 3600 * 24); // time difference between new transaction and similar (in days)
            Calendar expectedDate = Calendar.getInstance(); // prepare expectedDate
            expectedDate.setTimeInMillis(transaction.bdate());

            if (dayDiff>25 && dayDiff<37) {
                expectedDate.add(Calendar.MONTH,1); // similar transaction was about a month ago
                transaction.expectRepetition(Globals.yearDate(expectedDate)); // expect repetition in a month
            }
            if (dayDiff>55 && dayDiff<70) {
                expectedDate.add(Calendar.MONTH,2); // similar transaction was about two months ago
                transaction.expectRepetition(Globals.yearDate(expectedDate)); // expect repetition in two months
            }
            if (dayDiff>85 && dayDiff<100) {
                expectedDate.add(Calendar.MONTH,3); // similar transaction was about three months ago
                transaction.expectRepetition(Globals.yearDate(expectedDate)); // expect repetition in three months
            }
            if (dayDiff>175 && dayDiff<190) {
                expectedDate.add(Calendar.MONTH,6); // similar transaction was about six months ago
                transaction.expectRepetition(Globals.yearDate(expectedDate)); // expect repetition in six months
            }
        }
    }

    public void fetchTransactions(){
        final Vector<BankLogin> logins = BankLogin.loadAll();
        if (logins.isEmpty()) Toast.makeText(this,R.string.no_accouts,Toast.LENGTH_LONG).show();
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.fetch_transactions_spinner);
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                setProgressBarVisibility(View.VISIBLE);

                for (BankLogin login : logins){
                    TextView bankEntry = new TextView(FetchTransactions.this);
                    bankEntry.setText(getString(R.string.fetch_login_transactions).replace("#",login.getInstitute().name()));
                    addItemToList(bankEntry);
                    for (BankAccount account : login.accounts()){
                        Vector<Transaction> categorizedTransactions = Transaction.loadCategorized(account.id());

                        Vector<Transaction> lastTransactions = Transaction.getLastFor(account);
                        Long lastDate = lastTransactions.isEmpty() ? null : lastTransactions.lastElement().bdate();

                        TextView accountEntry = new TextView(FetchTransactions.this);
                        accountEntry.setText(getString(R.string.fetch_account_transactions).replace("#",account.number()));
                        accountEntry.setPadding(30,0,0,0);
                        addItemToList(accountEntry);
                        int count = 0;
                        try {
                            GVRKUms transactions = account.fetchNewTransactionsSince(lastDate);
                            if (!transactions.isOK()) continue;
                            for (GVRKUms.UmsLine hbciTransaction:transactions.getFlatData()){
                                Transaction transaction = new Transaction(hbciTransaction,account);
                                if (!transaction.in(lastTransactions)) {
                                    transaction.saveToDb();
                                    updateNumber(accountEntry,account.number(),++count);
                                    recognizeTransaction(transaction,categorizedTransactions);
                                }
                            }
                        } catch (ParserConfigurationException e) {
                            e.printStackTrace();
                        } catch (TransformerException e) {
                            e.printStackTrace();
                        } catch (SAXException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        updateNumber(accountEntry,account.number(),count);
                    }
                }

                setProgressBarVisibility(View.INVISIBLE);
                state = IDLE;
                return null;
            }
        };
        task.execute();
    }

    private void updateNumber(final TextView text, final String accountNumber, final int count) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                text.setText(getString(R.string.fetch_account_transactions).replace("#",accountNumber)+" "+getString(R.string.transaction_count).replace("#",""+count));
            }
        });
    }

    private void setProgressBarVisibility(final int visibility){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.fetch_transactions_spinner).setVisibility(visibility);
            }
        });
    }

    private void addItemToList(final View v){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LinearLayout lin = (LinearLayout) findViewById(R.id.fetch_transactions_list);
                lin.addView(v);
            }
        });
    }
}
