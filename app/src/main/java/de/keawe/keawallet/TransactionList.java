package de.keawe.keawallet;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import org.iban4j.Iban;

import java.sql.Struct;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");

        changeMonth(0);

        Vector<BankAccount> accounts = new Vector<>();
        Vector<BankLogin> bankLogins = BankLogin.loadAll();
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

    public void changeMonth(int d){
        month.add(Calendar.MONTH,d);
        Calendar now = Calendar.getInstance();
        now.add(Calendar.MONTH,-1);
        findViewById(R.id.right_button).setVisibility(month.after(now)?View.GONE:View.VISIBLE);

        int dummy = month.get(Calendar.MONTH)+1;
        setTitle(getString(R.string.app_name)+" - "+month.get(Calendar.YEAR)+"-"+(dummy<10?"0":"")+dummy);

        loadTransactioList(null);
    }

    public void loadTransactioList(Category categoryForFirstTransaction) {
        loadCategoryList();

        Object item = ((Spinner) findViewById(R.id.account_selector)).getSelectedItem();
        if (!(item instanceof BankAccount)) return;
        BankAccount account = (BankAccount) item;
        Vector<Transaction> transactions = account.transactions(month);

        TextView dateView = (TextView) findViewById(R.id.transaction_date_view);
        TextView usageView = (TextView) findViewById(R.id.transaction_usage_view);
        TextView valueView = (TextView) findViewById(R.id.transaction_value_view);
        TextView partView = (TextView) findViewById(R.id.transaction_participant_view);

        boolean uncategorizedAlreadyVisible = false;
        boolean noUncategorizedTransaction = true;
        for (Transaction t : transactions){
            if (t.category() == null){
                if (uncategorizedAlreadyVisible) continue;
                if (categoryForFirstTransaction != null) {
                    t.setCategory(categoryForFirstTransaction);
                    categoryForFirstTransaction = null;
                }
                if (t.category() == null) {
                    noUncategorizedTransaction = false;
                    dateView.setText(t.bdate("yyyy-MM-dd"));
                    usageView.setText(t.niceUsage());
                    valueView.setText(t.value(account.currency()));
                    partView.setText(t.participant()==null?"":t.participant().name());
                    uncategorizedAlreadyVisible = true;
                    continue;
                }
            }
            // at this point, only transactions assigned to a category should appear
            Category cat = t.category();
            cat.displayTransaction(this,t);
        }

        if (noUncategorizedTransaction){
            dateView.setText("");
            usageView.setText("");
            valueView.setText("");
            partView.setText(R.string.no_transaction_found);
        }

    }

    private void loadCategoryList() {
        Vector<Category> root_categories = Category.loadRoots();

        LinearLayout list = (LinearLayout) findViewById(R.id.category_list);
        list.removeAllViews();

        for (Category cat : root_categories) list.addView(cat.getView(this));
    }
}
