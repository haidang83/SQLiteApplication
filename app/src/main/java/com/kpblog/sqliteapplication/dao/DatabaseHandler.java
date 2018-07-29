package com.kpblog.sqliteapplication.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.kpblog.sqliteapplication.model.Customer;
import com.kpblog.sqliteapplication.model.CustomerPurchase;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;



public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "kpblogs";
    private static final String TABLE_CUSTOMER = "customer";
    private static final String KEY_CUSTOMER_ID = "customerID";
    private static final String KEY_TOTALCREDIT = "totalCredit";
    private static final String KEY_LAST_VISIT_DATE = "lastVisitDate";
    private static final String KEY_OPT_IN_DATE = "optInDate";
    private static final String KEY_OPT_OUT_DATE = "optOutDate";
    private static final String KEY_IS_OPT_IN = "isOptIn";
    private static final String KEY_IS_TEST_USER = "isTestUser";

    private static final String TABLE_CUSTOMER_PURCHASE = "customerPurchase";
    private static final String KEY_QUANTITY = "quantity";
    private static final String KEY_RECEIPT_NUM = "receiptNum";
    private static final String KEY_PURCHASE_DATE = "purchaseDate";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        //using INTEGER for DATE columns (unix time, seconds since epoch)
        String CREATE_CUSTOMER_TABLE = "CREATE TABLE %s (%s TEXT PRIMARY KEY, %s INTEGER, %s INTEGER, %s INTEGER, %s INTEGER, %s INTEGER, %s INTEGER)";
        sqLiteDatabase.execSQL(String.format(CREATE_CUSTOMER_TABLE, TABLE_CUSTOMER, KEY_CUSTOMER_ID, KEY_TOTALCREDIT, KEY_LAST_VISIT_DATE, KEY_IS_OPT_IN, KEY_OPT_IN_DATE, KEY_OPT_OUT_DATE, KEY_IS_TEST_USER));

        String CREATE_CUSTOMER_PURCHASE_TABLE = "CREATE TABLE %s (%s TEXT, %s INTEGER, %s INTEGER, %s INTEGER)";
        sqLiteDatabase.execSQL(String.format(CREATE_CUSTOMER_PURCHASE_TABLE, TABLE_CUSTOMER_PURCHASE, KEY_CUSTOMER_ID, KEY_QUANTITY, KEY_RECEIPT_NUM, KEY_PURCHASE_DATE));
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_CUSTOMER);
        onCreate(sqLiteDatabase);
    }

   public void addNewCustomer(Customer customer) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_CUSTOMER_ID, customer.getCustomerId());
        values.put(KEY_TOTALCREDIT, customer.getTotalCredit());

        // Inserting new Row
        db.insert(TABLE_CUSTOMER, null, values);
        db.close(); // Closing database connection
    }


  public Customer getCustomerByPhone(String phone) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CUSTOMER, new String[] {KEY_CUSTOMER_ID, KEY_TOTALCREDIT, KEY_IS_OPT_IN, KEY_OPT_IN_DATE, KEY_LAST_VISIT_DATE, KEY_OPT_OUT_DATE, KEY_IS_TEST_USER}, KEY_CUSTOMER_ID + "=?", new String[] { phone },
                null, null, null, null);
        Customer customer = null;
        if (cursor != null && cursor.moveToFirst()){
            customer = new Customer();
            customer.setCustomerId(cursor.getString(0));
            customer.setTotalCredit(cursor.getInt(1));

            setCustomerIsOptInFromDB(cursor, customer);
            customer.setOptInDate(new Date(cursor.getLong(3)));

            customer.setLastVisitDate(new Date(cursor.getLong(4)));

            customer.setOptOutDate(new Date(cursor.getLong(5)));

            customer.setTestUser((cursor.getInt(6)) == 1);
        }

        return customer;
    }

    private void setCustomerIsOptInFromDB(Cursor cursor, Customer customer) {
        final int optInIndicator = cursor.getInt(2);
        if (optInIndicator == 1){
            customer.setOptIn(true);
        }
        else {
            customer.setOptIn(false);
        }
    }

    /*public Customer getAddressByName(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CUSTOMER, new String[] { KEY_ID, KEY_CUSTOMER_ID, KEY_TOTALCREDIT}, KEY_CUSTOMER_ID + "=?", new String[] { name }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        Customer customer = new Customer();
        customer.setTotalCredit(Integer.parseInt(cursor.getString(0)));
        customer.setCustomerId(cursor.getString(1));
        customer.setAddress(cursor.getString(2));
        return customer;
    }*/

    public List<Customer> getAllAddress() {
        List<Customer> customerList = new ArrayList<Customer>();
        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_CUSTOMER;
        String selectQueryFormat = "SELECT %s, %s, %s, %s, %s, %s, %s FROM %s";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(String.format(selectQueryFormat, KEY_CUSTOMER_ID, KEY_TOTALCREDIT, KEY_IS_OPT_IN, KEY_LAST_VISIT_DATE, KEY_OPT_IN_DATE, KEY_OPT_OUT_DATE, KEY_IS_TEST_USER, TABLE_CUSTOMER), null);

        // Getting the address list which we already into our database
        if (cursor.moveToFirst()) {
            do {
                Customer customer = new Customer();
                customer.setCustomerId(cursor.getString(0));
                customer.setTotalCredit(cursor.getInt(1));
                customer.setLastVisitDate(new Date(cursor.getLong(3)));

                setCustomerIsOptInFromDB(cursor, customer);
                customer.setOptInDate(new Date(cursor.getLong(4)));
                customer.setOptOutDate(new Date(cursor.getLong(5)));
                customer.setTestUser(cursor.getInt(6) == 1);

                // Adding contact to list
                customerList.add(customer);
                Log.d("Customer: ", customer.getTotalCredit()+" , "+ customer.getCustomerId());
            } while (cursor.moveToNext());
        }

        // return contact list
        return customerList;
    }

    // Updating the customer based on customer ID
    public int updateAddress(Customer customer) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_CUSTOMER_ID, customer.getCustomerId());
        values.put(KEY_TOTALCREDIT, customer.getTotalCredit());
        return db.update(TABLE_CUSTOMER, values, KEY_CUSTOMER_ID + " = ?", new String[] { customer.getCustomerId() });
    }

    // Deleting single customer based on ID
    public void deleteCustomer(Customer customer) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CUSTOMER, KEY_CUSTOMER_ID + " = ?", new String[] { customer.getCustomerId() });
        db.close();
    }

    public void registerOrUpdateCustomer(Customer c) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_CUSTOMER_ID, c.getCustomerId());
        values.put(KEY_TOTALCREDIT, c.getTotalCredit());

        values.put(KEY_LAST_VISIT_DATE, c.getLastVisitDate().getTime());
        values.put(KEY_IS_OPT_IN, c.isOptIn());

        //All dates are stored as long (secs since epoch)
        if (c.getOptInDate() != null){
            values.put(KEY_OPT_IN_DATE, c.getOptInDate().getTime());
        }

        if (c.getOptOutDate() != null){
            values.put(KEY_OPT_OUT_DATE, c.getOptOutDate().getTime());
        }

        int id = (int) db.insertWithOnConflict(TABLE_CUSTOMER, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        if (id == -1) {
            //row already exists by Primary key(Phone Num)
            db.update(TABLE_CUSTOMER, values, KEY_CUSTOMER_ID + "=?", new String[] {c.getCustomerId()});
        }

        db.close();
    }

    public void insertCustomerPurchase(CustomerPurchase cp) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_CUSTOMER_ID, cp.getCustomerId());
        values.put(KEY_QUANTITY, cp.getQuantity());
        values.put(KEY_RECEIPT_NUM, cp.getReceiptNum());
        values.put(KEY_PURCHASE_DATE, cp.getPurchaseDate().getTime());

        // Inserting new Row
        db.insert(TABLE_CUSTOMER_PURCHASE, null, values);
        db.close(); // Closing database connection
    }

    public List<CustomerPurchase> getAllCustomerPurchase() {
        List<CustomerPurchase> cpList = new ArrayList<CustomerPurchase>();
        // Select All Query
        String selectQueryFormat = "SELECT %s, %s, %s, %s FROM %s";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(String.format(selectQueryFormat, KEY_CUSTOMER_ID, KEY_QUANTITY, KEY_RECEIPT_NUM, KEY_PURCHASE_DATE, TABLE_CUSTOMER_PURCHASE), null);

        // Getting the address list which we already into our database
        if (cursor.moveToFirst()) {
            do {
                CustomerPurchase cp = new CustomerPurchase();
                cp.setCustomerId(cursor.getString(0));
                cp.setQuantity(cursor.getInt(1));
                cp.setReceiptNum(cursor.getInt(2));
                cp.setPurchaseDate(new Date(cursor.getLong(3)));

                // Adding contact to list
                cpList.add(cp);
            } while (cursor.moveToNext());
        }

        // return contact list
        return cpList;
    }
}
