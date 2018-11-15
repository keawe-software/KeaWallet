package de.keawe.keawallet;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.IOException;
import java.util.Vector;

import de.keawe.keawallet.objects.CreditInstitute;

public class AddAccount extends AppCompatActivity {

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    Vector<CreditInstitute> institutes = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_account);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        addInstituteList();
        setListeners();

    }

    public void accountButtonClicked(){

    }

    public void addInstituteList(){
        if (institutes == null) try {
            institutes = CreditInstitute.getList(getAssets(), CreditInstitute.HBCI_ONLY);
            institutes.insertElementAt(new CreditInstitute(null,getString(R.string.institute_dropdown_initial),null,null,null),0);
            CreditInstitute[] inst_arr = institutes.toArray(new CreditInstitute[institutes.size()]);
            ArrayAdapter<CreditInstitute> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, inst_arr);
            Spinner instituteSelector = (Spinner) findViewById(R.id.instituteSelector);
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

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "AddAccount Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://de.keawe.keawallet/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "AddAccount Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://de.keawe.keawallet/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    public void setListeners(){
        Spinner instituteSelector = (Spinner) findViewById(R.id.instituteSelector);
        instituteSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                CreditInstitute institute = institutes.get(position);
                findViewById(R.id.institute_credentials_form).setVisibility(institute.hasUrl() ? View.VISIBLE : View.INVISIBLE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO: implement onNothingSelected

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

        Button addAccountBtn = (Button) findViewById(R.id.add_account_button);
        addAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                accountButtonClicked();
            }
        });

    }


}
