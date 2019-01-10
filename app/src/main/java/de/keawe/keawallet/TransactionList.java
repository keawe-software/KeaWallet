package de.keawe.keawallet;

import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Stack;
import java.util.Vector;

import de.keawe.keawallet.objects.Globals;
import de.keawe.keawallet.objects.database.BankAccount;
import de.keawe.keawallet.objects.database.BankLogin;
import de.keawe.keawallet.objects.database.Category;
import de.keawe.keawallet.objects.database.Transaction;

public class TransactionList extends AppCompatActivity {

    private Calendar month;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Globals.setAppcontext(getApplicationContext());
        setContentView(R.layout.activity_transaction_list);

        ImageButton addAccountButton = (ImageButton)findViewById(R.id.addAccountButton);
        addAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoAddAccount();
            }
        });

        ImageButton fetchTransactionButton = (ImageButton) findViewById(R.id.updateButton);
        fetchTransactionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoFetchTransactions();
            }
        });
        
        ImageButton rightButton = (ImageButton) findViewById(R.id.right_button);
        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeMonth(+1);
            }
        });


        ImageButton leftButton = (ImageButton) findViewById(R.id.left_button);
        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeMonth(-1);
            }
        });

        ImageButton helpBtn = (ImageButton)findViewById(R.id.transaction_info);
        helpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View overlay = findViewById(R.id.help_overlay);
                overlay.setVisibility((overlay.getVisibility()==View.VISIBLE)?View.GONE:View.VISIBLE);
            }
        });

     /*   ImageButton analyzeBtn = (ImageButton)findViewById(R.id.analyzeButton);
        analyzeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                analyze();
            }
        });*/

        final Spinner accountDropdown = (Spinner) findViewById(R.id.account_selector);
        accountDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                loadTransactioList(null);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        month = Calendar.getInstance();
    }

    private void analyze() {
        Toast.makeText(this,"Analyze has been disabled for security reasons.",Toast.LENGTH_LONG).show();
        if (2>1) return; //*/
        Object item = ((Spinner) findViewById(R.id.account_selector)).getSelectedItem();
        if (!(item instanceof BankAccount)) return;
        BankAccount account = (BankAccount) item;
        Vector<Transaction> transactionsOfVirtualAccount = Transaction.getLastFor(0);
        if (transactionsOfVirtualAccount.isEmpty()) {
            Toast.makeText(this,"Set bank account null for all transactions of "+account,Toast.LENGTH_LONG).show();
            Transaction.reassign(account.id(),0);
            changeMonth(0);
        } else {
            Transaction transaction = Transaction.first(0);
            if (transaction != null) {
                transaction.setCategory(null, false);
                transaction.setMostSimilar(null);
                transaction.setAccount(account);
                Long transactionTimeStamp = transaction.bdate();
                long day = 24*3600*1000;

                Vector<Transaction> categorizedTransactions = Transaction.loadCategorized(account.id());
                long dateLimit35 = transactionTimeStamp - 35 * day; // timestamp dating back 35 days before transaction
                long dateLimit65 = transactionTimeStamp - 65 * day; // timestamp dating back 65 days before transaction

                Vector<Transaction>transactions35 = new Vector<>();
                Vector<Transaction>transactions65 = new Vector<>();
                Vector<Transaction>olderTransactions = new Vector<>();

                for (Transaction t:categorizedTransactions){
                    if (t.bdate() > dateLimit35){
                        transactions35.add(t);
                    } else if (t.bdate() > dateLimit65){
                        transactions65.add(t);
                    } else olderTransactions.add(t);
                }

                FetchTransactions.recognizeTransaction(transaction, transactions35); // compare with transactions of the last 35 days
                if (transaction.category()==null) FetchTransactions.recognizeTransaction(transaction, transactions65); // compare with transactions of the last 65 days
                if (transaction.category()==null) FetchTransactions.recognizeTransaction(transaction, olderTransactions); // compare with remaining transactions

                if (transaction.category()!= null) { // we found a similar transaction and assigned a category.
                    Toast.makeText(this, "Category „" + transaction.category().name() + "“ assigned to " + transaction.participant()+"/"+transaction.firstLine(), Toast.LENGTH_LONG).show();
                }

                month.setTimeInMillis(transactionTimeStamp);
                changeMonth(0);
                if (Transaction.first(0) == null) System.out.println("#### THIS WAS THE LAST TRANSACTION FROM THE NULL-ACCOUNT! ###");
            }
        }
    }

    private void gotoAddAccount() {
        Intent addAccount = new Intent(this,AddAccount.class);
        startActivity(addAccount);
    }

    private void gotoFetchTransactions() {
        Intent fetchTransactions = new Intent(TransactionList.this,FetchTransactions.class);
        startActivity(fetchTransactions);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Globals.d("Resuming TransactionList");

        changeMonth(0);

        Vector<BankAccount> accounts = new Vector<>();
        Vector<BankLogin> bankLogins = BankLogin.loadAll();
        findViewById(R.id.welcome_overlay).setVisibility(bankLogins.isEmpty()?View.VISIBLE:View.GONE);
        for (BankLogin login:bankLogins) accounts.addAll(login.accounts());
        if (accounts.isEmpty()) {
            findViewById(R.id.transaction_and_category_list).setVisibility(View.INVISIBLE);
            findViewById(R.id.left_button).setVisibility(View.INVISIBLE);
        } else {
            findViewById(R.id.transaction_and_category_list).setVisibility(View.VISIBLE);
            findViewById(R.id.left_button).setVisibility(View.VISIBLE);
            BankAccount[] accountArray = accounts.toArray(new BankAccount[accounts.size()]);
            Spinner accountSelector = (Spinner) findViewById(R.id.account_selector);
            accountSelector.setAdapter(new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, accountArray));
        }
    }

    public void changeMonth(int months){
        month.add(Calendar.MONTH,months); // add or subtract the number of months
        Calendar now = Calendar.getInstance();
        //now.add(Calendar.MONTH,-1);
        findViewById(R.id.right_button).setVisibility(month.after(now)?View.GONE:View.VISIBLE);

        setTitle(getString(R.string.app_name)+" - "+Globals.yearDate(month));

        loadTransactioList(null);
    }

    public void loadTransactioList(Category categoryForFirstTransaction) {
        Object item = ((Spinner) findViewById(R.id.account_selector)).getSelectedItem();
        if (!(item instanceof BankAccount)) return;
        BankAccount account = (BankAccount) item;
        String currency = null;
        Stack<Transaction> unassignedtransactions = new Stack<>();

        { // add assigned transactions to categories
            Vector<Transaction> transactions = account.transactions(month);
            for (Transaction transaction : transactions) {
                Category cat = transaction.category();
                if (currency == null) currency = transaction.currency();
                if (cat == null) {
                    unassignedtransactions.push(transaction);
                    continue;
                }
                cat.addTransaction(transaction);
            }

            if (!transactions.isEmpty()){ // display current account balance
                TextView balance = (TextView) findViewById(R.id.current_balance);
                String saldo = transactions.lastElement().getSaldo();
                if (saldo == null) saldo = Globals.string(R.string.unknown_saldo);
                balance.setText(getString(R.string.current_balance).replace("?", saldo));
            }
        }

        { // add expected transactions to categories
            Calendar firstOfMonth = Globals.firstOf(month);
            Vector<Transaction> expectedTransactions = account.expectedTransactions(month);
            for (Transaction transaction : expectedTransactions){
                Calendar expectedDate = Calendar.getInstance();
                long now = expectedDate.getTimeInMillis();
                expectedDate.setTimeInMillis(transaction.bdate());
                while (expectedDate.before(firstOfMonth)) expectedDate.add(Calendar.MONTH,1);
                transaction.setBDate(expectedDate.getTimeInMillis() - now);

                Category cat = transaction.category();
                if (currency == null) currency = transaction.currency();
                if (cat == null) continue;
                cat.addExpectedTransaction(transaction);
            }
        }


        if (!unassignedtransactions.isEmpty() && categoryForFirstTransaction != null) { // unassigned transactions present and category provided:
            Transaction transaction = unassignedtransactions.pop(); // get first unassigned transaction and assign to category
            transaction.setCategory(categoryForFirstTransaction);
            categoryForFirstTransaction.addTransaction(transaction);
        }

        LinearLayout display = (LinearLayout) findViewById(R.id.first_uncategorized_transaction);
        display.removeAllViews();

        View infoButton = findViewById(R.id.transaction_info);

        if (unassignedtransactions.isEmpty()) {
            infoButton.setVisibility(View.GONE);
        } else {
            Transaction unassignedtransaction = unassignedtransactions.peek();
            RelativeLayout unassignedTransactionDisplay = unassignedtransaction.getView(this);
            unassignedTransactionDisplay.setBackgroundColor(Globals.color(R.color.yellow));
            display.addView(unassignedTransactionDisplay);
            infoButton.setVisibility(View.VISIBLE);
        }

        loadCategoryList(currency, unassignedtransactions.isEmpty());
    }

    private void loadCategoryList(String currency, boolean hideEmpty) {
        Vector<Category> root_categories = Category.loadRoots();

        LinearLayout list = (LinearLayout) findViewById(R.id.category_list);
        list.removeAllViews();

        boolean noContent = true;
        int expectedSum = 0;
        for (Category cat : root_categories) {
            RelativeLayout view = cat.getView(this,currency,hideEmpty);
            if (view != null) {
                list.addView(view);
                noContent = false;
                expectedSum += cat.getExpectedSum();
            }
        }
        TextView tv = (TextView) findViewById(R.id.expected_sum);
        if (noContent) {
            TextView text = new TextView(this);
            text.setText(R.string.no_transaction_found);
            list.addView(text);
            tv.setText("");
        } else {
            String text = (expectedSum == 0)?"":Globals.string(R.string.expected_sum).replace("?",String.format("%.2f",expectedSum/100.0)+" "+currency);
            tv.setText(text);
        }
    }
}
