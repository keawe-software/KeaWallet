package de.keawe.keawallet;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Vector;

import javax.crypto.spec.SecretKeySpec;

import de.keawe.keawallet.objects.AccountSetupListener;
import de.keawe.keawallet.objects.CreditInstitute;
import de.keawe.keawallet.objects.Globals;
import de.keawe.keawallet.objects.database.BankAccount;
import de.keawe.keawallet.objects.database.BankLogin;

public class AddAccount extends AppCompatActivity implements AccountSetupListener {

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */

    private CreditInstitute selectedInstitute =null;
    private boolean checkRunning = false;
    private Vector<BankAccount> accounts;
    BankLogin bankLogin = null;
    private final static int REQUESTING_PASSWORD = 1;
    private final static int IDLE = 0;
    private static int state = IDLE;

    public void accountButtonClicked(){
        String login = ((TextView) findViewById(R.id.institute_login)).getText().toString();
        String secret = ((TextView) findViewById(R.id.institute_password)).getText().toString();

        bankLogin = new BankLogin(selectedInstitute, login, secret);
        RelativeLayout checkView = (RelativeLayout) findViewById(R.id.institute_credentials_checks);
        checkView.setVisibility(View.VISIBLE);

        if (!checkRunning) {
            final AccountSetupListener listener = this;
            checkRunning = true;
            AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... params) {
                    accounts = bankLogin.findAccounts(listener);// this method performs the actual hbci task
                    checkRunning = false;
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    storeLoginAndAccounts();
                }
            };
            task.execute();
        }
    }

    public void addInstituteList(){
        try {
            Vector<CreditInstitute> institutes = CreditInstitute.getList(CreditInstitute.HBCI_ONLY);
            institutes.insertElementAt(new CreditInstitute(null,getString(R.string.institute_dropdown_initial),null,null,null,null),0);
            CreditInstitute[] inst_arr = institutes.toArray(new CreditInstitute[institutes.size()]);
            ArrayAdapter<CreditInstitute> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, inst_arr);
            AutoCompleteTextView instituteSelector = (AutoCompleteTextView) findViewById(R.id.institute_selector);
            instituteSelector.setAdapter(adapter);
        } catch (IOException e) {
            Toast.makeText(this, R.string.institutes_read_error, Toast.LENGTH_LONG).show();
        }
    }

    private void disableButtonOnEmptyField() {
        String login = ((EditText) findViewById(R.id.institute_login)).getText().toString();
        String password = ((EditText) findViewById(R.id.institute_password)).getText().toString();
        Button addAccountBtn = (Button) findViewById(R.id.add_account_button);
        addAccountBtn.setEnabled(!login.isEmpty() && !password.isEmpty());
    }

    private SecretKeySpec getOrCreateEncryptionKey() {
        if (Globals.encryption_key == null){
            Intent passwordDialog = new Intent(this,PasswordDialog.class);
            startActivity(passwordDialog);
        }
        return Globals.encryption_key;
    }

    @Override
    public void notifyHandlerCreated(boolean success) {
        TextView tv = (TextView) findViewById(R.id.check_data_state);
        tv.setText(getText(R.string.check_data_info)+""+getText(success?R.string.success:R.string.failed));
    }

    @Override
    public void notifyLoggedIn(boolean success) {
        TextView tv = (TextView) findViewById(R.id.server_connect_state);
        tv.setText(getText(R.string.connect_info)+""+getText(success?R.string.success:R.string.failed));
    }

    @Override
    public void notifyJobDone(boolean success) {
        if (success) {
        } else {
            findViewById(R.id.add_account_button).setEnabled(true);
        }
    }

    @Override
    public void notifyAccount(String accountNumber, String saldoString, String currency) {
        TextView tv = (TextView) findViewById(R.id.account_state);
        tv.setText(getText(R.string.account_info)+" "+accountNumber+" ("+saldoString+" "+currency+")");
    }

    @Override
    public void notifyFoundAccounts(Vector<BankAccount> accounts) {
        TextView tv = (TextView) findViewById(R.id.account_state);
        tv.setText(String.format(getString(R.string.account_summary),accounts.size()));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_account);
        addInstituteList();
        setListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Globals.d("Resuming AddAccount. State = "+(state==REQUESTING_PASSWORD?"REQUESTING PASSWORD":(state==IDLE?"IDLE":"UNKNOWN")));

        RelativeLayout credentialsForm = (RelativeLayout) findViewById(R.id.institute_credentials_form);
        credentialsForm.setVisibility(View.INVISIBLE);

        RelativeLayout checkView = (RelativeLayout) findViewById(R.id.institute_credentials_checks);
        checkView.setVisibility(View.INVISIBLE);

        if (state == REQUESTING_PASSWORD) storeLoginAndAccounts();
    }

    public void setListeners(){
        AutoCompleteTextView instituteSelector = (AutoCompleteTextView) findViewById(R.id.institute_selector);
        instituteSelector.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object item = parent.getItemAtPosition(position);
                if (item instanceof CreditInstitute){
                    selectedInstitute = (CreditInstitute) item;
                    findViewById(R.id.institute_credentials_form).setVisibility(selectedInstitute.hasUrl() ? View.VISIBLE : View.INVISIBLE);
                }
            }
        });

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                disableButtonOnEmptyField();
            }
        };

        EditText loginField = (EditText) findViewById(R.id.institute_login);
        loginField.addTextChangedListener(textWatcher);

        EditText loginPassword = (EditText) findViewById(R.id.institute_password);
        loginPassword.addTextChangedListener(textWatcher);

        final Button addAccountBtn = (Button) findViewById(R.id.add_account_button);
        addAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addAccountBtn.setEnabled(false);
                accountButtonClicked();
            }
        });
    }

    private void storeLoginAndAccounts() {
        SecretKeySpec key = getOrCreateEncryptionKey();
        if (key == null) {
            state=REQUESTING_PASSWORD;
            return; // password dialog will be started from getOrCreateEncryptionKey
        }
        try {
            bankLogin.saveToDb();
            for (BankAccount account : accounts) {
                account.saveToDb();
            }
            Intent fetchTransactions = new Intent(this,FetchTransactions.class);
            startActivity(fetchTransactions);
        } catch (Exception e){
            e.printStackTrace();
        }
        state=IDLE;
    }
}
