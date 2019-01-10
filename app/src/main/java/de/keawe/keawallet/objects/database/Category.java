package de.keawe.keawallet.objects.database;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.HashMap;
import java.util.Vector;

import de.keawe.keawallet.R;
import de.keawe.keawallet.TransactionList;
import de.keawe.keawallet.objects.Globals;

public class Category {

    private static final String TABLE_NAME = "categories";
    private static final String KEY = "id";
    private static final String CATEGORY = "category";
    private static final String PARENT = "parent";
    public static final String TABLE_CREATION = "CREATE TABLE " + TABLE_NAME + " (" + KEY + " INTEGER PRIMARY KEY AUTOINCREMENT, " + PARENT+" LONG, "+CATEGORY + " VARCHAR(255))";
    public static final boolean HIDE_EMPTY = true;
    public static final boolean SHOW_EMPTY = false;
    private static HashMap<Long,Category> catList = new HashMap<>();
    private static LinearLayout.LayoutParams btnLayout = null; // will be created on first call of buttonLayout()
    private static LinearLayout.LayoutParams marginLayout = null; // will be created on first call of marginLayout()
    private final String definition;
    private long parent_id = 0;
    private long id = 0;
    private RelativeLayout layout = null;
    private Vector<Transaction> transactions = new Vector<>();
    private Vector<Transaction> expectedTransactions = new Vector<>();
    private int sum = 0;
    private int expectedSum = 0;

    public Category(String def, long parent_id) {
        definition = def;
        this.parent_id = parent_id;
    }

    public static Category load(long id) {
        Category cat = catList.get(id);
        if (cat != null) return cat;

        SQLiteDatabase db = Globals.readableDatabase();
        Cursor cursor=db.query(TABLE_NAME,null,KEY+" = ?",new String[]{""+id},null,null,null);
        if (cursor.moveToNext()){
            String definition = cursor.getString(cursor.getColumnIndex(CATEGORY));
            long parent = cursor.getLong(cursor.getColumnIndex(PARENT));
            cat = new Category(definition,parent);
            cat.id = id;
            catList.put(id,cat);
        }
        cursor.close();
        db.close();
        return cat;
    }

    public static Vector<Category> loadRoots() {
        return loadByParent(0);
    }

    public static Vector<Category> loadByParent(long parent){

        Vector<Category> cats = new Vector<>();
        SQLiteDatabase db = Globals.readableDatabase();
        String selection = PARENT+" = "+parent;
        Cursor cursor=db.query(TABLE_NAME,null,selection,null,null,null,CATEGORY+" ASC");
        while (cursor.moveToNext()){
            long id = cursor.getLong(cursor.getColumnIndex(KEY));
            Category cat = catList.get(id);
            if (cat == null){
                String definition = cursor.getString(cursor.getColumnIndex(CATEGORY));
                parent = cursor.getLong(cursor.getColumnIndex(PARENT));
                cat = new Category(definition,parent);
                cat.id = id;
                catList.put(id,cat);
            }
            cats.add(cat);
        }
        cursor.close();
        db.close();
        return cats;
    }

    @Override
    public String toString() {
        return "Category(id: "+id+", parent: "+parent_id+", def: "+definition+")";
    }

    public static String preset() {
        StringBuffer result = new StringBuffer("INSERT OR IGNORE INTO "+TABLE_NAME+"("+KEY+", "+CATEGORY+", "+PARENT+") VALUES ");

        int[][] values = {
                { 1,R.string.category_insurance, 0},
                { 2,R.string.category_life_insurance, 1},
                { 3,R.string.category_health_insurance, 1},
                { 4,R.string.category_mobility, 0},
                { 5,R.string.category_mobility_car,4},
                { 6,R.string.category_mobility_public,4},
                { 7,R.string.category_consumption,0},
                { 8,R.string.category_food,7},
                { 9,R.string.category_leisure,0},
                {10,R.string.category_income,0},
                {11,R.string.category_ventures,0},
                {12,R.string.category_fees,0},
                {13,R.string.category_accomodation,0},
                {14,R.string.category_cash,7},
                {15,R.string.category_sport,9},
                {16,R.string.category_bike,4},
                {17,R.string.category_investment_fund,11},
                {18,R.string.category_account_fee,12},
                {19,R.string.category_miscellaneous,0},
                {20,R.string.category_public_braodcasting,12},
                {21,R.string.category_rent,13},
                {22,R.string.category_electricity,13},
                {23,R.string.category_liability,1},
        };

        for (int[] entry:values) result.append("("+entry[0]+", '"+Globals.string(entry[1])+"', "+entry[2]+") ");

        return result.toString().replace(") (","), (");
    }

    public RelativeLayout getView(final TransactionList transactionList, String currency, boolean hideEmpty) {
        final Vector<View> childViews = new Vector<>();
        sum = 0;
        expectedSum = 0;
        for (Category child : children()){
            RelativeLayout childView = child.getView(transactionList,currency,hideEmpty);
            if (childView != null) {
                childViews.add(childView);
                sum += child.sum;
                expectedSum += child.expectedSum;
            }
        }
        for (Transaction transaction : transactions){
            childViews.add(transaction.getView(transactionList));
            sum += transaction.value();
        }

        for (Transaction expectedTransaction : expectedTransactions){
            RelativeLayout v = expectedTransaction.getExpectationView(transactionList);
            childViews.add(v);
            expectedSum += expectedTransaction.value();
        }


        if (childViews.isEmpty() && hideEmpty == HIDE_EMPTY) return null;

        layout = (RelativeLayout) transactionList.getLayoutInflater().inflate(R.layout.category_list_entry,null);

        final LinearLayout childList = (LinearLayout) layout.findViewById(R.id.category_child_list);
        for (View childView : childViews) childList.addView(childView);

        final ImageButton collapseButton = (ImageButton) layout.findViewById(R.id.toggle_category_button);
        collapseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (childList.getVisibility() == View.VISIBLE){
                    collapseButton.setImageResource(R.drawable.expand);
                    childList.setVisibility(View.GONE);
                } else {
                    collapseButton.setImageResource(R.drawable.collapse);
                    childList.setVisibility(View.VISIBLE);
                }
            }
        });

        Button assignButton = (Button) layout.findViewById(R.id.assign_category_button);
        String text = definition+" ("+String.format("%.2f",sum/100.0);
        if (expectedSum!=0) text+=" / "+String.format("%.2f",expectedSum/100.0);
        assignButton.setText(text+" "+currency+")");
        assignButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (childList.getVisibility() == View.VISIBLE || childViews.isEmpty()) {
                    transactionList.loadTransactioList(Category.this);
                } else {
                    collapseButton.setImageResource(R.drawable.collapse);
                    childList.setVisibility(View.VISIBLE);
                }
            }
        });

        ImageButton addCatButton = (ImageButton) layout.findViewById(R.id.add_sub_category_button);
        addCatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addCategoryDialog(transactionList);
            }
        });
        transactions.clear();
        expectedTransactions.clear();
        return layout;
    }

    private void addCategoryDialog(final TransactionList transactionList) {
        AlertDialog.Builder builder = new AlertDialog.Builder(transactionList);
        builder.setTitle(String.format(Globals.string(R.string.add_new_category), Category.this.definition));


        final EditText input = new EditText(transactionList);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Category newCat = new Category(input.getText().toString().trim(),Category.this.id);
                newCat.saveToDb();
                dialog.dismiss();
                transactionList.loadTransactioList(null);

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void saveToDb() {
        ContentValues values = new ContentValues();
        values.put(CATEGORY,definition);
        values.put(PARENT,parent_id);
        SQLiteDatabase db = Globals.writableDatabase();
        id = db.insert(TABLE_NAME,null,values);
        db.close();
        catList.put(id,this);
    }


    private Vector<Category> children() {
        return Category.loadByParent(id);
    }

    public long getId() {
        return id;
    }

    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
    }

    public void addExpectedTransaction(Transaction transaction) {
        expectedTransactions.add(transaction);
    }


    public String name() {
        return definition;
    }

    public String full() {
        return (parent_id!=0?Category.load(parent_id).full()+"/":"")+name();
    }

    public int getExpectedSum() {
        return expectedSum;
    }
}
