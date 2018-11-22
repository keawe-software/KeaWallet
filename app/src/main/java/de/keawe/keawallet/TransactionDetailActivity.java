package de.keawe.keawallet;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.Serializable;

import de.keawe.keawallet.objects.database.Transaction;

public class TransactionDetailActivity extends AppCompatActivity {

    public static final String TRANSACTION = "transaction";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_detail);
    }

    @Override
    protected void onResume() {
        super.onResume();
        long id = getIntent().getLongExtra(TRANSACTION, 0);
        if (id == 0) finish();
        final Transaction transaction = Transaction.load(id);
        ((TextView) findViewById(R.id.date_view)).setText(transaction.bdate("yyyy-MM-dd"));
        ((TextView) findViewById(R.id.value_view)).setText(transaction.value(transaction.currency()));
        ((TextView) findViewById(R.id.participant_view)).setText(transaction.participant().name());
        ((TextView) findViewById(R.id.usage_view)).setText(transaction.niceUsage());

        ImageButton btn = (ImageButton) findViewById(R.id.drop_category_button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                transaction.setCategory(null);
                finish();
            }
        });
    }
}
