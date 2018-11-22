package de.keawe.keawallet;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
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
        Object item = ((Spinner) findViewById(R.id.account_selector)).getSelectedItem();
        if (!(item instanceof BankAccount)) return;
        BankAccount account = (BankAccount) item;
        Vector<Transaction> transactions = account.transactions(month);
        String currency = null;
        Stack<Transaction> unassignedtransactions = new Stack<>();
        for (Transaction transaction:transactions){
            Category cat = transaction.category();
            if (cat == null) {
                unassignedtransactions.push(transaction);
                continue;
            }
            if (currency == null) currency=transaction.currency();
            cat.addTransaction(transaction);
        }

        LinearLayout display = (LinearLayout) findViewById(R.id.first_uncategorized_transaction);
        display.removeAllViews();

        View infoButton = findViewById(R.id.transaction_info);

        if (!unassignedtransactions.isEmpty() && categoryForFirstTransaction != null) {
            Transaction transaction = unassignedtransactions.pop();
            transaction.setCategory(categoryForFirstTransaction);
            categoryForFirstTransaction.addTransaction(transaction);
        }
        if (unassignedtransactions.isEmpty()) {
            infoButton.setVisibility(View.GONE);
        } else {
            RelativeLayout unassignedTransactionDisplay = unassignedtransactions.peek().getView(this);
            display.addView(unassignedTransactionDisplay);
            infoButton.setVisibility(View.VISIBLE);
        }

        loadCategoryList(currency, unassignedtransactions.isEmpty());
    }

    private void loadCategoryList(String currency, boolean show_empty) {
        Vector<Category> root_categories = Category.loadRoots();

        LinearLayout list = (LinearLayout) findViewById(R.id.category_list);
        list.removeAllViews();

        for (Category cat : root_categories) {
            RelativeLayout view = cat.getView(this,currency,show_empty);
            if (view != null) list.addView(view);
        }
    }
}
