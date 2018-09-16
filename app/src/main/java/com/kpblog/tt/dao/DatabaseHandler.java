package com.kpblog.tt.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.util.Log;

import com.kpblog.tt.model.Customer;
import com.kpblog.tt.model.CustomerBroadcast;
import com.kpblog.tt.model.CustomerClaimCode;
import com.kpblog.tt.model.CustomerPurchase;
import com.kpblog.tt.util.Constants;
import com.kpblog.tt.util.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.Date;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;



public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "traTemptation";
    private static final String TABLE_CUSTOMER = "customer";
    private static final String KEY_CUSTOMER_ID = "customerID";

    //this is a computed field, only used to sum & sort purchaseCredit + referralCredit
    public static final String KEY_TOTAL_CREDIT = "totalCredit";

    private static final String KEY_PURCHASE_CREDIT = "purchaseCredit";
    private static final String KEY_REFERRAL_CREDIT = "referralCredit";
    public static final String KEY_LAST_VISIT_DATE = "lastVisitDate";
    private static final String KEY_OPT_IN_DATE = "optInDate";
    private static final String KEY_OPT_OUT_DATE = "optOutDate";
    private static final String KEY_IS_OPT_IN = "isOptIn";
    private static final String KEY_IS_TEST_USER = "isTestUser";
    public static final String KEY_LAST_CONTACTED_DATE = "lastContactedDate";
    private static final String KEY_REFERRER_ID = "referrerId";

    private static final String TABLE_CUSTOMER_PURCHASE = "customerPurchase";
    public static final String KEY_QUANTITY = "quantity";
    private static final String KEY_RECEIPT_NUM = "receiptNum";
    public static final String KEY_PURCHASE_DATE = "purchaseDate";
    private static final String KEY_NOTES = "notes";

    private static final String TABLE_CUSTOMER_CLAIM_CODE = "customerClaimCode";
    private static final String KEY_CLAIM_CODE = "claimCode";
    private static final String KEY_PROMO_NAME = "promoName";
    private static final String KEY_DATE_ISSUED = "dateIssued";

    private static final String TABLE_CUSTOMER_BROADCAST = "customerBroadcast";
    private static final String KEY_RECIPIENT_LIST_ID = "recipientListId";
    private static final String KEY_BROADCAST_TIME = "broadcastTime";
    private static final String KEY_BROADCAST_MESSAGE = "message";
    private static final String KEY_BROADCAST_TYPE = "broadcastType";
    private static final String KEY_CLAIM_CODE_TYPE = "claimCodeType";
    private static final String KEY_STATUS = "status";

    private static final String TABLE_RECIPIENT_LIST_CUSTOMER = "recipientListCustomer";


    private static final String TABLE_ADMIN = "admin";
    public static final String NOT_NULL_AND_NOT_EMPTY_PATTERN = "({0} is NOT NULL and {0} != '''') AND ";
    public static final String NULL_OR_EMPTY_PATTERN = "({0} is NULL OR {0} = '''') AND ";


    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This method only runs when there's no database
     * @param sqLiteDatabase
     */
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        //using INTEGER for DATE columns (unix time, seconds since epoch)
        String CREATE_CUSTOMER_TABLE = "CREATE TABLE %s (%s TEXT PRIMARY KEY, %s REAL, %s REAL, %s INTEGER, %s INTEGER, %s INTEGER, %s INTEGER, %s INTEGER, %s INTEGER, %s TEXT)";
        sqLiteDatabase.execSQL(String.format(CREATE_CUSTOMER_TABLE, TABLE_CUSTOMER, KEY_CUSTOMER_ID, KEY_PURCHASE_CREDIT, KEY_REFERRAL_CREDIT, KEY_LAST_VISIT_DATE, KEY_IS_OPT_IN, KEY_OPT_IN_DATE, KEY_OPT_OUT_DATE, KEY_IS_TEST_USER, KEY_LAST_CONTACTED_DATE, KEY_REFERRER_ID));

        //this table keeps track of all the customer purchases, each row represents a purchase, so there can be multiple rows per each customer
        String CREATE_CUSTOMER_PURCHASE_TABLE = "CREATE TABLE {0} ({1} TEXT, {2} REAL, {3} INTEGER, {4} INTEGER, {5} TEXT, FOREIGN KEY({1}) REFERENCES {6}({1}) ON DELETE CASCADE ON UPDATE CASCADE)";
        sqLiteDatabase.execSQL(MessageFormat.format(CREATE_CUSTOMER_PURCHASE_TABLE, TABLE_CUSTOMER_PURCHASE, KEY_CUSTOMER_ID, KEY_QUANTITY, KEY_RECEIPT_NUM, KEY_PURCHASE_DATE, KEY_NOTES, TABLE_CUSTOMER));

        //this table has the outstanding claim code for the customer, only at most 1 outstanding code per customer
        String CREATE_CUSTOMER_CLAIM_CODE_TABLE = "CREATE TABLE {0} ({1} TEXT, {2} TEXT, {3} INTEGER, {4} INTEGER, {5} TEXT, PRIMARY KEY({1}, {4}), FOREIGN KEY({1}) REFERENCES {6}({1}) ON DELETE CASCADE ON UPDATE CASCADE)";
        sqLiteDatabase.execSQL(MessageFormat.format(CREATE_CUSTOMER_CLAIM_CODE_TABLE, TABLE_CUSTOMER_CLAIM_CODE, KEY_CUSTOMER_ID, KEY_CLAIM_CODE, KEY_DATE_ISSUED, KEY_CLAIM_CODE_TYPE, KEY_PROMO_NAME, TABLE_CUSTOMER));

        //this table has all the broadcast schedule/sent to customers
        String CREATE_CUSTOMER_BROADCAST_TABLE = "CREATE TABLE %s (%s INTEGER PRIMARY KEY, %s INTEGER, %s TEXT, %s TEXT, %s TEXT, %s TEXT)";
        sqLiteDatabase.execSQL(String.format(CREATE_CUSTOMER_BROADCAST_TABLE, TABLE_CUSTOMER_BROADCAST, KEY_RECIPIENT_LIST_ID, KEY_BROADCAST_TIME, KEY_BROADCAST_MESSAGE, KEY_BROADCAST_TYPE, KEY_STATUS, KEY_PROMO_NAME));

        //this table has all the customers in a recipient list
        String CREATE_RECIPIENT_LIST_CUSTOMER_TABLE = "CREATE TABLE {0} ({1} INTEGER, {2} TEXT,  PRIMARY KEY({1}, {2}), FOREIGN KEY({1}) REFERENCES {3}({1}) ON DELETE CASCADE ON UPDATE CASCADE, FOREIGN KEY({2}) REFERENCES {4}({2}) ON DELETE CASCADE ON UPDATE CASCADE)";
        sqLiteDatabase.execSQL(MessageFormat.format(CREATE_RECIPIENT_LIST_CUSTOMER_TABLE, TABLE_RECIPIENT_LIST_CUSTOMER, KEY_RECIPIENT_LIST_ID, KEY_CUSTOMER_ID, TABLE_CUSTOMER_BROADCAST, TABLE_CUSTOMER));

        String CREATE_ADMIN_TABLE = "CREATE TABLE %s (%s TEXT PRIMARY KEY)";
        sqLiteDatabase.execSQL(String.format(CREATE_ADMIN_TABLE, TABLE_ADMIN, KEY_CUSTOMER_ID));
        ContentValues values = new ContentValues();
        values.put(KEY_CUSTOMER_ID, "4084257660");
        sqLiteDatabase.insertWithOnConflict(TABLE_ADMIN, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    /**
     * This method runs when there's an existing database, but the DATABASE_VERSION specified above is
     * later than the internal db version
     * https://thebhwgroup.com/blog/how-android-sqlite-onupgrade
     * @param sqLiteDatabase
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        //sample test implementation only, we already downgrade the db and add this code in onCreate()
        /*if (newVersion == 2){
            //version 2 is when we add the admin table
            String CREATE_ADMIN_TABLE = "CREATE TABLE %s (%s TEXT PRIMARY KEY)";
            sqLiteDatabase.execSQL(String.format(CREATE_ADMIN_TABLE, TABLE_ADMIN, KEY_CUSTOMER_ID));

            ContentValues values = new ContentValues();
            values.put(KEY_CUSTOMER_ID, "4084257660");
            sqLiteDatabase.insertWithOnConflict(TABLE_ADMIN, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        }*/
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //do nothing
        Log.d("DatabaseHander", String.format("oldversion=%d, newVersion=%d", oldVersion, newVersion));
    }

    @Override
    public void onConfigure(SQLiteDatabase db){
        db.setForeignKeyConstraintsEnabled(true);
    }

    /**
     * Copies the database file at the specified location over the current
     * internal application database.
     * */
    public boolean importDatabase(String sourceDbPath, String targetDbPath){
        boolean isSuccess = false;
        try {

            // Close the SQLiteOpenHelper so it will commit the created empty
            // database to internal storage.
            close();
            File newDb = new File(sourceDbPath);

            File oldDb = new File(targetDbPath);
            if (newDb.exists()) {
                Util.copyFile(new FileInputStream(newDb), new FileOutputStream(oldDb));
                // Access the copied database so SQLiteHelper will cache it and mark
                // it as created.
                getWritableDatabase().close();
                isSuccess = true;
            }
        } catch (Exception e){
            Log.e("DatabaseHandler", e.getMessage());
        }
        return isSuccess;
    }

   public void addNewCustomer(Customer customer) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_CUSTOMER_ID, customer.getCustomerId());
        values.put(KEY_PURCHASE_CREDIT, customer.getTotalCredit());

        // Inserting new Row
        db.insert(TABLE_CUSTOMER, null, values);
        db.close(); // Closing database connection
    }


  public Customer getCustomerById(String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CUSTOMER, new String[] {KEY_CUSTOMER_ID, KEY_PURCHASE_CREDIT, KEY_IS_OPT_IN, KEY_OPT_IN_DATE, KEY_LAST_VISIT_DATE, KEY_OPT_OUT_DATE, KEY_IS_TEST_USER, KEY_LAST_CONTACTED_DATE, KEY_REFERRER_ID, KEY_REFERRAL_CREDIT}, KEY_CUSTOMER_ID + "=?", new String[] { id },
                null, null, null, null);
        Customer customer = null;
        if (cursor != null && cursor.moveToFirst()){
            customer = new Customer();
            customer.setCustomerId(cursor.getString(0));
            customer.setPurchaseCredit(cursor.getDouble(1));

            setCustomerIsOptInFromDB(cursor, customer);
            customer.setOptInDate(new Date(cursor.getLong(3)));

            customer.setLastVisitDate(new Date(cursor.getLong(4)));

            customer.setOptOutDate(new Date(cursor.getLong(5)));

            customer.setTestUser((cursor.getInt(6)) == 1);

            customer.setLastContactedDate(new Date(cursor.getLong(7)));

            customer.setReferrerId(cursor.getString(8));
            customer.setReferralCredit(cursor.getDouble(9));
        }

        db.close();

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

    public List<Customer> getAllAddress() {
        List<Customer> customerList = new ArrayList<Customer>();
        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_CUSTOMER;
        String selectQueryFormat = "SELECT %s, %s, %s, %s, %s, %s, %s FROM %s";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(String.format(selectQueryFormat, KEY_CUSTOMER_ID, KEY_PURCHASE_CREDIT, KEY_IS_OPT_IN, KEY_LAST_VISIT_DATE, KEY_OPT_IN_DATE, KEY_OPT_OUT_DATE, KEY_IS_TEST_USER, KEY_REFERRAL_CREDIT, TABLE_CUSTOMER), null);

        // Getting the address list which we already into our database
        if (cursor.moveToFirst()) {
            do {
                Customer customer = new Customer();
                customer.setCustomerId(cursor.getString(0));
                customer.setPurchaseCredit(cursor.getDouble(1));
                customer.setLastVisitDate(new Date(cursor.getLong(3)));

                setCustomerIsOptInFromDB(cursor, customer);
                customer.setOptInDate(new Date(cursor.getLong(4)));
                customer.setOptOutDate(new Date(cursor.getLong(5)));
                customer.setTestUser(cursor.getInt(6) == 1);
                customer.setReferralCredit(cursor.getDouble(7));

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
        values.put(KEY_PURCHASE_CREDIT, customer.getPurchaseCredit());
        values.put(KEY_REFERRAL_CREDIT, customer.getReferralCredit());
        return db.update(TABLE_CUSTOMER, values, KEY_CUSTOMER_ID + " = ?", new String[] { customer.getCustomerId() });
    }

    // Deleting single customer based on ID
    public void deleteCustomer(Customer customer) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CUSTOMER, KEY_CUSTOMER_ID + " = ?", new String[] { customer.getCustomerId() });
        db.close();
    }

    public boolean registerOrUpdateCustomer(Customer c, boolean isNewCustomer) {
        SQLiteDatabase db = null;
        boolean isSuccess = false;
        try {

            db = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(KEY_CUSTOMER_ID, c.getCustomerId());
            values.put(KEY_PURCHASE_CREDIT, c.getPurchaseCredit());
            values.put(KEY_REFERRAL_CREDIT, c.getReferralCredit());

            values.put(KEY_LAST_VISIT_DATE, c.getLastVisitDate().getTime());
            if (c.getLastContactedDate() != null){
                values.put(KEY_LAST_CONTACTED_DATE, c.getLastContactedDate().getTime());
            }
            values.put(KEY_IS_OPT_IN, c.isOptIn()? 1 : 0);
            values.put(KEY_IS_TEST_USER, c.isTestUser()? 1 : 0);
            values.put(KEY_REFERRER_ID, c.getReferrerId());

            //All dates are stored as long (secs since epoch)
            if (c.getOptInDate() != null){
                values.put(KEY_OPT_IN_DATE, c.getOptInDate().getTime());
            }
            else {
                values.put(KEY_OPT_IN_DATE, 0);
            }

            if (c.getOptOutDate() != null){
                values.put(KEY_OPT_OUT_DATE, c.getOptOutDate().getTime());
            }
            else {
                values.put(KEY_OPT_OUT_DATE, 0);
            }

            if (isNewCustomer){
                db.insert(TABLE_CUSTOMER, null, values);
            }
            else {
                //row already exists by Primary key(Phone Num)
                db.update(TABLE_CUSTOMER, values, KEY_CUSTOMER_ID + "=?", new String[] {c.getCustomerId()});
            }

            isSuccess = true;
        } finally {
            if (db != null){
                db.close();
            }
        }

        return isSuccess;
    }

    public void insertCustomerPurchase(CustomerPurchase cp) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_CUSTOMER_ID, cp.getCustomerId());
        values.put(KEY_QUANTITY, cp.getQuantity());
        values.put(KEY_RECEIPT_NUM, cp.getReceiptNum());
        values.put(KEY_PURCHASE_DATE, cp.getPurchaseDate().getTime());
        values.put(KEY_NOTES, cp.getNotes());

        // Inserting new Row
        db.insert(TABLE_CUSTOMER_PURCHASE, null, values);
        db.close(); // Closing database connection
    }


    public CustomerPurchase[] getAllCustomerPurchaseById(String customerId) {
        List<CustomerPurchase> cpList = new ArrayList<CustomerPurchase>();
        // Select All Query
        String selectQueryFormat = "SELECT %s, %s, %s, %s, %s FROM %s WHERE %s=%s ORDER BY %s DESC";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(String.format(selectQueryFormat, KEY_CUSTOMER_ID, KEY_QUANTITY, KEY_RECEIPT_NUM, KEY_PURCHASE_DATE, KEY_NOTES, TABLE_CUSTOMER_PURCHASE, KEY_CUSTOMER_ID, customerId, KEY_PURCHASE_DATE), null);
        processCustomerPurchaseCursor(cpList, cursor);

        db.close();
        // return contact list
        return cpList.toArray(new CustomerPurchase[0]);
    }

    public CustomerPurchase[] getAllCustomerPurchaseByTypeAndTime(String note, int daysAgo, boolean allTime,
                                                                  String orderByCol, String sortAscDesc, boolean drinkClaim) {
        List<CustomerPurchase> cpList = new ArrayList<CustomerPurchase>();

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        long todayInMillis = today.getTimeInMillis();
        long transactionStart = todayInMillis - (daysAgo * Constants.DAYS_TO_MILLIS);

        String selectCondition = String.format("(%s >= %d) AND ", KEY_PURCHASE_DATE, transactionStart);
        if (allTime){
            //disregard the time restriction
            selectCondition = "";
        }

        if (!drinkClaim){
            //purchase
            String purchaseCondition = String.format("(%s is NULL OR %s='' OR %s NOT like '%s')", KEY_NOTES, KEY_NOTES, KEY_NOTES, "%drink%");
            selectCondition = selectCondition + purchaseCondition;
        }
        else {
            //claimed free drink
            String drinkClaimCondition = String.format("(%s like '%s')", KEY_NOTES, "%drink%");
            selectCondition = selectCondition + drinkClaimCondition;
        }

        // Select All Query
        String selectQueryFormat = "SELECT %s, %s, %s, %s, %s FROM %s WHERE %s ORDER BY %s %s";
        SQLiteDatabase db = this.getReadableDatabase();
        final String queryStr = String.format(selectQueryFormat, KEY_CUSTOMER_ID, KEY_QUANTITY, KEY_RECEIPT_NUM, KEY_PURCHASE_DATE, KEY_NOTES, TABLE_CUSTOMER_PURCHASE, selectCondition, orderByCol, sortAscDesc);
        Cursor cursor = db.rawQuery(queryStr, null);
        processCustomerPurchaseCursor(cpList, cursor);

        db.close();
        // return contact list
        return cpList.toArray(new CustomerPurchase[0]);
    }

    private void processCustomerPurchaseCursor(List<CustomerPurchase> cpList, Cursor cursor) {
        // Getting the address list which we already into our database
        if (cursor.moveToFirst()) {
            do {
                CustomerPurchase cp = new CustomerPurchase();
                cp.setCustomerId(cursor.getString(0));
                cp.setQuantity(cursor.getDouble(1));
                cp.setReceiptNum(cursor.getInt(2));
                cp.setPurchaseDate(new Date(cursor.getLong(3)));
                cp.setNotes(cursor.getString(4));

                // Adding contact to list
                cpList.add(cp);
            } while (cursor.moveToNext());
        }
    }

    public List<CustomerPurchase> getAllCustomerPurchase() {
        List<CustomerPurchase> cpList = new ArrayList<CustomerPurchase>();
        // Select All Query
        String selectQueryFormat = "SELECT %s, %s, %s, %s, %s FROM %s";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(String.format(selectQueryFormat, KEY_CUSTOMER_ID, KEY_QUANTITY, KEY_RECEIPT_NUM, KEY_PURCHASE_DATE, KEY_NOTES, TABLE_CUSTOMER_PURCHASE), null);

        // Getting the address list which we already into our database
        processCustomerPurchaseCursor(cpList, cursor);

        db.close();
        // return contact list
        return cpList;
    }

    public void insertOrUpdateCustomerClaimCode(CustomerClaimCode cc) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_CUSTOMER_ID, cc.getCustomerId());
        values.put(KEY_CLAIM_CODE, cc.getClaimCode());
        values.put(KEY_PROMO_NAME, cc.getPromoName());
        values.put(KEY_CLAIM_CODE_TYPE, cc.getClaimCodeType());

        values.put(KEY_DATE_ISSUED, cc.getIssuedDate().getTime());

        //since we're updating all columns of this row, it's ok to use conflict_replace
        int id = (int) db.insertWithOnConflict(TABLE_CUSTOMER_CLAIM_CODE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        /*if (id == -1) {
            //row already exists by Primary key(Phone Num)
            db.update(TABLE_CUSTOMER_CLAIM_CODE, values, KEY_CUSTOMER_ID + "=?", new String[] {cc.getCustomerId()});
        }*/

        db.close();
    }

    //There might be 2 claim codes, 1 for free drink(which the promoName is empty), and 1 for promo in which the promo is populated
    public CustomerClaimCode getClaimCodeByCustomerId(String customerId, boolean getPromo) {
        CustomerClaimCode cc = null;
        SQLiteDatabase db = this.getReadableDatabase();

        String selection = KEY_CUSTOMER_ID + "=?";
        final String claimCodeTypeQuery = getClaimCodeTypeQuery(getPromo);
        selection = claimCodeTypeQuery + selection;

        //get the latest one
        selection += MessageFormat.format(" ORDER BY {0} DESC", KEY_DATE_ISSUED);

        // Select All Query
        Cursor cursor = db.query(TABLE_CUSTOMER_CLAIM_CODE, new String[] {KEY_CLAIM_CODE, KEY_PROMO_NAME, KEY_DATE_ISSUED}, selection, new String[] { customerId },
                null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String code = cursor.getString(0);
            String promoName = cursor.getString(1);
            java.util.Date dateIssued = new java.util.Date(cursor.getLong(2));

            cc = new CustomerClaimCode(customerId, code, dateIssued, promoName);
        }

        db.close();
        return cc;
    }

    @NonNull
    private String getClaimCodeTypeQuery(boolean getPromo) {
        int claimCodeType = Constants.CLAIM_CODE_TYPE_FREE_DRINK;
        if (getPromo){
            claimCodeType = Constants.CLAIM_CODE_TYPE_PROMOTION;
        }

        return MessageFormat.format("{0} = {1} AND ", KEY_CLAIM_CODE_TYPE, claimCodeType);
    }


    /**
     * after successful claim, puts all remaining credit into purchase, zero out referral
     * @param customerId
     * @param totalCredit
     */
    public void updateTotalCreditForCustomerId(String customerId, double totalCredit) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_PURCHASE_CREDIT, totalCredit);
        values.put(KEY_REFERRAL_CREDIT, 0);
        db.update(TABLE_CUSTOMER, values, KEY_CUSTOMER_ID + " = ?", new String[] { customerId });
        db.close();
    }

    public void deleteClaimCodeForCustomerId(String customerId, boolean isPromo) {
        SQLiteDatabase db = this.getWritableDatabase();

        String whereClause = getClaimCodeTypeQuery(isPromo) + KEY_CUSTOMER_ID + "=?";

        db.delete(TABLE_CUSTOMER_CLAIM_CODE, whereClause, new String []{customerId});
        db.close();
    }

    public void unsubscribe(String customerId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_IS_OPT_IN, 0);
        values.put(KEY_OPT_IN_DATE, 0);
        values.put(KEY_OPT_OUT_DATE, new java.util.Date().getTime());

        db.update(TABLE_CUSTOMER, values, KEY_CUSTOMER_ID + "=?", new String[]{customerId});
        db.close();
    }

    public boolean addAdmin(String customerId) {
        boolean isSuccess = false;
        SQLiteDatabase db = null;
        try {

            db = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(KEY_CUSTOMER_ID, customerId);

            // Inserting new Row or overwrite existing one (this is ok since this table only has 1 column)
            db.insertWithOnConflict(TABLE_ADMIN, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            isSuccess = true;
        } finally{
            if (db != null)
                db.close(); // Closing database connection
        }

        return isSuccess;
    }

    public List<String> getAllAdmins(){
        List<String> admins = new ArrayList<String>();
        SQLiteDatabase db = null;
        try {
            String selectQueryFormat = "SELECT * FROM %s";
            db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery(String.format(selectQueryFormat, TABLE_ADMIN), null);
            if (cursor != null && cursor.moveToFirst()){

                do {
                    admins.add(cursor.getString(0));

                } while (cursor.moveToNext());
            }
        } finally {
            if (db != null){
                db.close();;
            }
        }

        return admins;
    }

    public boolean remove(String customerId) {
        boolean isSuccess = false;

        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();
            db.delete(TABLE_ADMIN, KEY_CUSTOMER_ID + " = ?", new String[] { customerId });
            isSuccess = true;
        } finally {
            if (db != null){
                db.close();
            }
        }

        return isSuccess;
    }

    public List<Customer> searchCustomerByLastVisitAndText(int lastVisitDayMin,
                                                           int lastVisitDayMax,
                                                           int lastTextMinDayInt,
                                                           int lastTextMaxDayInt,
                                                           double drinkCreditMinDouble, double drinkCreditMaxDouble,
                                                           String sortByDbColumn,
                                                           String sortOrder,
                                                           Constants.EXISTING_PROMO_REQUIREMENT existing_promo_requirement) {

        List<Customer> customerList = new ArrayList<Customer>();
        SQLiteDatabase db = null;
        try {

            String selectQuery = getSelectQuery(lastVisitDayMin, lastVisitDayMax,
                                    lastTextMinDayInt, lastTextMaxDayInt, drinkCreditMinDouble, drinkCreditMaxDouble, sortByDbColumn, sortOrder, existing_promo_requirement);

            db = getReadableDatabase();

            Cursor cursor = db.rawQuery(selectQuery, null);
            if (cursor != null && cursor.moveToFirst()){
                do {
                    Customer c = new Customer();
                    c.setCustomerId(cursor.getString(0));
                    c.setLastVisitDate(new Date(cursor.getLong(1)));
                    c.setPurchaseCredit(cursor.getDouble(2));
                    c.setLastContactedDate(new Date(cursor.getLong(3)));
                    c.setReferralCredit(cursor.getDouble(4));
                    c.setOptIn(cursor.getInt(6) == 1 ? true : false);

                    customerList.add(c);
                } while (cursor.moveToNext());
            }
        } finally {
            if (db != null){
                db.close();
            }
        }

        return customerList;
    }

    @NonNull
    private String getSelectQuery(int lastVisitMin,
                                  int lastVisitMax, int lastTextMinDayInt, int lastTextMaxDayInt,
                                  double drinkCreditMinDouble, double drinkCreditMaxDouble,
                                  String sortByDbColumn, String sortOrder, Constants.EXISTING_PROMO_REQUIREMENT existing_promo_requirement) {


        /**
         * SELECT customerId, broadcastTimestamp, totalCredit, lastContactDate
         FROM table_name
         WHERE (lastContactDate IS NULL OR (lastContactDate >= lastTextStartDate AND lastContactDate <= lastTextEndDate))
         AND (broadcastTimestamp >= lastVisitStartDate and broadcastTimestamp =< lastVisitEndDate)
         */
        String selectClauseFormat = String.format("SELECT %s, %s, %s, %s, %s, (%s + %s) as %s, %s FROM %s ", KEY_CUSTOMER_ID, KEY_LAST_VISIT_DATE, KEY_PURCHASE_CREDIT, KEY_LAST_CONTACTED_DATE,
                                KEY_REFERRAL_CREDIT, KEY_PURCHASE_CREDIT, KEY_REFERRAL_CREDIT, KEY_TOTAL_CREDIT, KEY_IS_OPT_IN, TABLE_CUSTOMER);

        String selectCondition = getSelectCondition(lastVisitMin, lastVisitMax, lastTextMinDayInt, lastTextMaxDayInt, drinkCreditMinDouble, drinkCreditMaxDouble);

        if (existing_promo_requirement == Constants.EXISTING_PROMO_REQUIREMENT.NEITHER_EXISTING_PROMO_NOR_FREE_DRINK){
            String customerIdsWithPromoOrFreeDrink = getSelectQueryForCustomerIdHavingPromoRequirement(Constants.EXISTING_PROMO_REQUIREMENT.HAS_PROMO_OR_FREE_DRINK);
            String customerIdsWithoutPromoCondition = MessageFormat.format(" AND ({0} NOT IN ({1}))", KEY_CUSTOMER_ID, customerIdsWithPromoOrFreeDrink);
            selectCondition += customerIdsWithoutPromoCondition;
        }
        else if (existing_promo_requirement == Constants.EXISTING_PROMO_REQUIREMENT.HAS_EXISTING_PROMO_ONLY){
            String customerIdsWithPromoOnly = getSelectQueryForCustomerIdHavingPromoRequirement(Constants.EXISTING_PROMO_REQUIREMENT.HAS_EXISTING_PROMO_ONLY);
            String customerIdsWithPromoCondition = MessageFormat.format(" AND ({0} IN ({1}))", KEY_CUSTOMER_ID, customerIdsWithPromoOnly);
            selectCondition += customerIdsWithPromoCondition;
        }

        if (selectCondition.isEmpty()){
            //no condition, retrieve all users
            return selectClauseFormat + " ORDER BY " + sortByDbColumn + " " + sortOrder;
        }
        else {
            //if any condition is specified, user meeting those criteria AND is opted in
            selectCondition +=  String.format(" AND %s=%d", KEY_IS_OPT_IN, 1);
            return selectClauseFormat + " WHERE " + selectCondition + " ORDER BY " + sortByDbColumn + " " + sortOrder;
        }
    }

    /**
     * the part between WHERE and ORDER BY
     * @param lastVisitMin
     * @param lastVisitMax
     * @param lastTextMinDayInt
     * @param lastTextMaxDayInt
     * @param drinkCreditMinDouble
     * @param drinkCreditMaxDouble
     * @return
     */
    private String getSelectCondition(int lastVisitMin, int lastVisitMax,
                                      int lastTextMinDayInt, int lastTextMaxDayInt,
                                      double drinkCreditMinDouble, double drinkCreditMaxDouble) {
        String selectCondition = "";
        Calendar today = new GregorianCalendar();
        long todayInMillis = today.getTimeInMillis();

        if (lastTextMinDayInt != 0 || lastTextMaxDayInt != 0){
            //if either min or max text day is specified, then set up the condition and query for opt-in customer. Otherwise leave it empty

            long lastTextStartDate = 0;
            long lastTextEndDate = todayInMillis - (lastTextMinDayInt * Constants.DAYS_TO_MILLIS);
            if (lastTextMaxDayInt > 0){
                //lastTextMaxDay is specified, calculate it from today. Otherwise just use 0 for epoch start
                lastTextStartDate = todayInMillis - (lastTextMaxDayInt * Constants.DAYS_TO_MILLIS);
            }

            String lastContactDateCondition = String.format("(%s >= %d AND %s <= %d)", KEY_LAST_CONTACTED_DATE, lastTextStartDate, KEY_LAST_CONTACTED_DATE, lastTextEndDate);
            selectCondition = lastContactDateCondition;
        }

        if (lastVisitMin != 0 || lastVisitMax != 0){
            //if last visit min or max is specified, build the condition, otherwise leave empty

            long lastVisitEndDate = todayInMillis - (lastVisitMin * Constants.DAYS_TO_MILLIS);
            long lastVisitStartDate = 0;
            if (lastVisitMax > 0) {
                //if lastVisitMaxDay is specified, then calculate it from today. Otherwise just use 0 for epoch start
                lastVisitStartDate = todayInMillis - (lastVisitMax * Constants.DAYS_TO_MILLIS);
            }

            String lastVisitDateCondition = String.format("(%s >= %d AND %s <= %d)", KEY_LAST_VISIT_DATE, lastVisitStartDate, KEY_LAST_VISIT_DATE, lastVisitEndDate);

            if (selectCondition.isEmpty()){
                selectCondition = lastVisitDateCondition;
            }
            else {
                selectCondition += " AND " + lastVisitDateCondition;
            }
        }

        if (drinkCreditMinDouble != 0 || drinkCreditMaxDouble != 0){
            String totalDrinkCreditCondition = String.format("(%s >= %f AND %s <= %f)", KEY_TOTAL_CREDIT, drinkCreditMinDouble, KEY_TOTAL_CREDIT, drinkCreditMaxDouble);
            if (drinkCreditMaxDouble == 0){
                //query for drink credit, but max credit not specified, then just skip the max condition
                totalDrinkCreditCondition = String.format("(%s >= %f)", KEY_TOTAL_CREDIT, drinkCreditMinDouble);
            }

            if (selectCondition.isEmpty()){
                selectCondition = totalDrinkCreditCondition;
            }
            else {
                selectCondition += " AND " + totalDrinkCreditCondition;
            }
        }
        return selectCondition;
    }

    public void addReferralCreditForCustomerId(String referrerId, double additionalReferralCredit) {
        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();
            String update = String.format("UPDATE %s SET %s = %s + %f WHERE %s = %s ", TABLE_CUSTOMER, KEY_REFERRAL_CREDIT, KEY_REFERRAL_CREDIT, additionalReferralCredit, KEY_CUSTOMER_ID, referrerId);
            db.execSQL(update);
        } finally {
            if (db != null){
                db.close();;
            }
        }
    }

    public void updateLastTexted(String customerId, long time) {
        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();
            String update = String.format("UPDATE %s SET %s = %d WHERE %s = %s", TABLE_CUSTOMER, KEY_LAST_CONTACTED_DATE, time, KEY_CUSTOMER_ID, customerId);
            db.execSQL(update);
        } finally {
            if (db != null){
                db.close();
            }
        }
    }

    public List<String> getTestUsers() {

        List<String> testUsers = new ArrayList<String>();
        SQLiteDatabase db = null;
        try {
            testUsers = getAllAdmins();
            db = getReadableDatabase();

            String query = String.format("SELECT %s FROM %s WHERE %s=%d", KEY_CUSTOMER_ID, TABLE_CUSTOMER, KEY_IS_TEST_USER, 1);

            Cursor cursor = db.rawQuery(query, null);
            if (cursor != null && cursor.moveToFirst()){
                do {
                    final String userId = cursor.getString(0);
                    if (!testUsers.contains(userId)){
                        testUsers.add(userId);
                    }

                } while (cursor.moveToNext());
            }
        } finally {
            if (db != null){
                db.close();
            }
        }


        return testUsers;
    }

    public int insertIntoCustomerBroadcastTable(long timeInMillis, String msg, String type,
                                                String promoName, Customer[] customers) {

        SQLiteDatabase db = null;
        int broadcastId = 0;
        try {
            db = this.getWritableDatabase();
            db.beginTransaction();

            ContentValues values = new ContentValues();
            values.put(KEY_BROADCAST_TIME, timeInMillis);
            values.put(KEY_BROADCAST_MESSAGE, msg);
            values.put(KEY_BROADCAST_TYPE, type);
            if (Constants.BROADCAST_TYPE_SCHEDULED_NEW_PROMO.equals(type)){
                //if sending new promo, save the promo name
                //(for old promo, ignore the promoName passed in, we'll load for each customer upon texting)
                values.put(KEY_PROMO_NAME, promoName);
            }

            values.put(KEY_STATUS, Constants.STATUS_READY);

            broadcastId = (int) db.insert(TABLE_CUSTOMER_BROADCAST, null, values);
            if (Constants.BROADCAST_TYPE_SCHEDULED_FREE_FORM.equals(type)){
                //only for on-demand type: insert the customer for this recipient list
                //for the other types, the customer list will be queried right before broadcast
                insertCustomerForRecipientList(broadcastId, Arrays.asList(customers), db);
            }

            db.setTransactionSuccessful();
        } finally {
            if (db != null){
                db.endTransaction();
                db.close();
            }
        }

        return broadcastId;
    }

    public void updateCustomerBroadcastById(int broadcastId, long timeInMillis, String msg, String promoName) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_BROADCAST_TIME, timeInMillis);
        values.put(KEY_BROADCAST_MESSAGE, msg);
        values.put(KEY_PROMO_NAME, promoName);

        db.update(TABLE_CUSTOMER_BROADCAST, values, KEY_RECIPIENT_LIST_ID + " = " + broadcastId, null);
        db.close();
    }

    private void insertCustomerForRecipientList(int id, List<Customer> customers, SQLiteDatabase db) {
        for (Customer c : customers){

            insertPhoneNumberIntoRecipientListId(id, db, c.getCustomerId());
        }
    }

    private void insertPhoneNumberIntoRecipientListId(int id, SQLiteDatabase db, String phoneNum) {
        ContentValues values = new ContentValues();
        values.put(KEY_RECIPIENT_LIST_ID, id);
        values.put(KEY_CUSTOMER_ID, phoneNum);

        db.insertWithOnConflict(TABLE_RECIPIENT_LIST_CUSTOMER, null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public void massInsertPhoneNumbersIntoRecipientListId(int id, List<String> phoneList){
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            db.beginTransaction();

            for (String num : phoneList){
                insertPhoneNumberIntoRecipientListId(id, db, num);
            }

            db.setTransactionSuccessful();
        } finally {
            if (db != null){
                db.endTransaction();
                db.close();
            }
        }
    }


    public List<CustomerBroadcast> getAllTodayCustomerBroadcastsBeforeTimestamp(long now) {
        SQLiteDatabase db = null;
        List<CustomerBroadcast> cbList = new ArrayList<CustomerBroadcast>();

        try {
            db = getReadableDatabase();
            Calendar startOfToday = Calendar.getInstance();
            startOfToday.set(Calendar.HOUR_OF_DAY, 0);
            startOfToday.set(Calendar.MINUTE, 0);
            startOfToday.set(Calendar.SECOND, 1);


            String whereClause = String.format("%s <= %d AND %s > %d AND %s = '%s'", KEY_BROADCAST_TIME, now, KEY_BROADCAST_TIME, startOfToday.getTimeInMillis(), KEY_STATUS, Constants.STATUS_READY);
            String orderBy = KEY_BROADCAST_TIME + " ASC";
            String query = String.format("SELECT * FROM %s WHERE %s ORDER BY %s", TABLE_CUSTOMER_BROADCAST, whereClause, orderBy);
            Cursor cursor = db.rawQuery(query, null);
            if (cursor != null && cursor.moveToFirst()){
                do {
                    CustomerBroadcast cb = populateCustomerBroadcast(cursor, db);
                    cbList.add(cb);

                } while (cursor.moveToNext());
            }

            for (CustomerBroadcast cb : cbList){

                //if broadcast type is onDemand, get the customers' phones
                //for the other types, we'll re-run the query to get the most updated customer list
                if (Constants.BROADCAST_TYPE_SCHEDULED_FREE_FORM.equals(cb.getType())){

                    List<String> recipientPhones = getPhoneNumByRecipientListId(db, cb.getRecipientListId());

                    cb.setRecipientPhoneNumbers(recipientPhones);
                }
            }
        } finally {
            if (db != null){
                db.close();
            }
        }

        return cbList;
    }

    @NonNull
    private CustomerBroadcast populateCustomerBroadcast(Cursor cursor, SQLiteDatabase db) {
        long timestamp = cursor.getLong(cursor.getColumnIndex(KEY_BROADCAST_TIME));
        int recList = cursor.getInt(cursor.getColumnIndex(KEY_RECIPIENT_LIST_ID));
        String msg = cursor.getString(cursor.getColumnIndex(KEY_BROADCAST_MESSAGE));
        String type = cursor.getString(cursor.getColumnIndex(KEY_BROADCAST_TYPE));
        String promoName = cursor.getString(cursor.getColumnIndex(KEY_PROMO_NAME));
        String status = cursor.getString(cursor.getColumnIndex(KEY_STATUS));
        CustomerBroadcast cb = new CustomerBroadcast(timestamp, recList, msg, type, promoName);
        cb.setStatus(status);

        List<String> phoneList = getPhoneNumByRecipientListId(db, cb.getRecipientListId());
        cb.setRecipientPhoneNumbers(phoneList);

        return cb;
    }

    public CustomerBroadcast getCustomerBroadcastById(int id){
        SQLiteDatabase db = null;
        CustomerBroadcast cb = null;

        try {
            db = getReadableDatabase();
            String query = MessageFormat.format("Select * from {0} where {1} = {2}", TABLE_CUSTOMER_BROADCAST, KEY_RECIPIENT_LIST_ID, id);
            Cursor c = db.rawQuery(query, null);
            if (c != null && c.moveToFirst()){
                cb = populateCustomerBroadcast(c, db);
            }
        } catch (Exception e){

        } finally {
            if (db != null){
                db.close();
            }
        }
        return cb;
    }

    @NonNull
    private List<String> getPhoneNumByRecipientListId(SQLiteDatabase db, int id) {
        String whereClause;
        Cursor cursor;
        whereClause = String.format("%s = %d", KEY_RECIPIENT_LIST_ID, id);
        cursor = db.query(TABLE_RECIPIENT_LIST_CUSTOMER, new String[] {KEY_CUSTOMER_ID}, whereClause, null, null, null, null);
        List<String> recipientPhones = new ArrayList<String>();
        if (cursor.moveToFirst()){
            do {
                recipientPhones.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        return recipientPhones;
    }

    public void updateBroadcastStatusById(int broadcastId, String status) {
        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();
            String update = String.format("UPDATE %s SET %s = '%s' WHERE %s = %d ", TABLE_CUSTOMER_BROADCAST, KEY_STATUS, status, KEY_RECIPIENT_LIST_ID, broadcastId);
            db.execSQL(update);
        } finally {
            if (db != null){
                db.close();
            }
        }
    }

    public List<Customer> getCustomersForCreditReminder() {
        List<Customer> cList = new ArrayList<Customer>();
        SQLiteDatabase db = null;

        try {

            String selectQuery = getSelectQuery(Constants.DRINK_REMINDER_LAST_VISIT_MIN, Constants.DRINK_REMINDER_LAST_VISIT_MAX,
                    Constants.DRINK_REMINDER_LAST_TEXTED_MIN, Constants.DRINK_REMINDER_LAST_TEXTED_MAX,
                    Constants.DRINK_REMINDER_CREDIT_MIN, Constants.DRINK_REMINDER_CREDIT_MAX, KEY_TOTAL_CREDIT, "asc", Constants.EXISTING_PROMO_REQUIREMENT.IGNORE);

            db = getReadableDatabase();

            Cursor cursor = db.rawQuery(selectQuery, null);
            if (cursor != null && cursor.moveToFirst()){
                do {
                    String customerId = cursor.getString(0);
                    double totalCredit = cursor.getDouble(5);
                    Customer c = new Customer();
                    c.setCustomerId(customerId);
                    c.setPurchaseCredit(totalCredit);
                    cList.add(c);
                } while (cursor.moveToNext());
            }
        } finally {
            if (db != null){
                db.close();
            }
        }

        return cList;
    }

    /**
     * get the inactive customers without promo or free credit claim
     * @return
     */
    public List<String> getInactiveCustomerPhoneNumbersWithoutPromo() {

        List<String> phoneList = new ArrayList<String>();
        SQLiteDatabase db = null;
        try {
            /*
                CustomerIds with existing promo or free credit claim:
                (Select customerId from customerClaimCode)
             */
            String customerIdsWithPromo = getSelectQueryForCustomerIdHavingPromoRequirement(Constants.EXISTING_PROMO_REQUIREMENT.HAS_PROMO_OR_FREE_DRINK);

            /*
              Select customerID, (purchaseCredit + referralCredit) as totalCredit from customer c
                WHERE {selectCondition}
                AND c.isOptIn = 1
                AND c.customerID not IN {customerIds with existing promo}
             */
            String selectColumns = MessageFormat.format("Select {0}, ({1} + {2}) as {3} from {4} WHERE ",
                            KEY_CUSTOMER_ID, KEY_PURCHASE_CREDIT, KEY_REFERRAL_CREDIT, KEY_TOTAL_CREDIT, TABLE_CUSTOMER);

            String selectCondition = getSelectCondition(Constants.INACTIVE_LAST_VISIT_MIN, Constants.INACTIVE_LAST_VISIT_MAX,
                                        Constants.INACTIVE_LAST_TEXTED_MIN, Constants.INACTIVE_LAST_TEXTED_MAX,
                                        Constants.INACTIVE_CREDIT_MIN, Constants.INACTIVE_CREDIT_MAX);

            String joinCondition = MessageFormat.format(" AND {0} = 1 AND {1} NOT IN({2})"
                                        , KEY_IS_OPT_IN, KEY_CUSTOMER_ID, customerIdsWithPromo);

            String query = selectColumns + selectCondition + joinCondition;

            db = getReadableDatabase();

            Cursor cursor = db.rawQuery(query, null);
            if (cursor != null && cursor.moveToFirst()){
                do {
                    phoneList.add(cursor.getString(0));
                } while (cursor.moveToNext());
            }

        } finally {
            if (db != null){
                db.close();
            }
        }

        return phoneList;
    }

    @NonNull
    private String getSelectQueryForCustomerIdHavingPromoRequirement(Constants.EXISTING_PROMO_REQUIREMENT promo_requirement) {
        switch (promo_requirement){
            case HAS_EXISTING_PROMO_ONLY:
                return MessageFormat.format("Select {0} from {1} where {2} = {3}",
                        KEY_CUSTOMER_ID, TABLE_CUSTOMER_CLAIM_CODE, KEY_CLAIM_CODE_TYPE, Constants.CLAIM_CODE_TYPE_PROMOTION);
            case HAS_PROMO_OR_FREE_DRINK:
                return MessageFormat.format("Select {0} from {1}", KEY_CUSTOMER_ID, TABLE_CUSTOMER_CLAIM_CODE);
            default:
                return MessageFormat.format("Select {0} from {1}", KEY_CUSTOMER_ID, TABLE_CUSTOMER_CLAIM_CODE);
        }
    }

    public List<CustomerClaimCode> getCustomerClaimCodeWithPromoForInactiveUsers(){
        List<CustomerClaimCode> customerClaimCodes = new ArrayList<CustomerClaimCode>();
        SQLiteDatabase db = null;

        try {

            /*
              select c.customerId, ccc.promoName, ccc.claimCode from Customer c, customerClaimCode ccc
              where {selectCondition}
              and c.customerId = ccc.customerId and (ccc.claimCodeType = 2)
             */
            String selectColumns = MessageFormat.format("Select {0}.{1}, {2}.{3}, {2}.{4}, ({0}.{7} + {0}.{8}) as {9} from {5} {0}, {6} {2} WHERE ",
                    "c", KEY_CUSTOMER_ID, "ccc", KEY_PROMO_NAME, KEY_CLAIM_CODE,
                    TABLE_CUSTOMER, TABLE_CUSTOMER_CLAIM_CODE, KEY_PURCHASE_CREDIT, KEY_REFERRAL_CREDIT, KEY_TOTAL_CREDIT);

            String selectCondition = getSelectCondition(Constants.INACTIVE_LAST_VISIT_MIN, Constants.INACTIVE_LAST_VISIT_MAX,
                    Constants.INACTIVE_LAST_TEXTED_MIN, Constants.INACTIVE_LAST_TEXTED_MAX,
                    Constants.INACTIVE_CREDIT_MIN, Constants.INACTIVE_CREDIT_MAX);

            String joinCondition = MessageFormat.format(" AND {0}.{1} = {2}.{1} AND ({2}.{3} = {4}) AND {0}.{5} = 1",
                    "c", KEY_CUSTOMER_ID, "ccc", KEY_CLAIM_CODE_TYPE, Constants.CLAIM_CODE_TYPE_PROMOTION, KEY_IS_OPT_IN);

            String query = selectColumns + selectCondition + joinCondition;

            db = getReadableDatabase();

            Cursor cursor = db.rawQuery(query, null);
            if (cursor != null && cursor.moveToFirst()){
                do {
                    String customerId = cursor.getString(0);
                    String promoName = cursor.getString(1);
                    String claimCode = cursor.getString(2);

                    CustomerClaimCode ccc = new CustomerClaimCode(customerId, claimCode, null, promoName);
                    customerClaimCodes.add(ccc);
                } while (cursor.moveToNext());
            }

        } finally {
            if (db != null){
                db.close();
            }
        }

        return customerClaimCodes;
    }

    public CustomerBroadcast[] getAllCustomerBroadcastByStatus(String broadcastStatus) {
        SQLiteDatabase db = null;
        List<CustomerBroadcast> cbList = new ArrayList<CustomerBroadcast>();

        try {
            db = getReadableDatabase();
            String query = MessageFormat.format("Select {0}, {1}, {2}, {3} from {4} ", KEY_RECIPIENT_LIST_ID, KEY_BROADCAST_TIME, KEY_BROADCAST_TYPE, KEY_STATUS, TABLE_CUSTOMER_BROADCAST);

            if (!broadcastStatus.isEmpty()){
                String condition = MessageFormat.format("WHERE {0} = ''{1}'' ", KEY_STATUS, broadcastStatus);
                query += condition;
            }

            String orderBy = MessageFormat.format("ORDER BY {0} DESC", KEY_BROADCAST_TIME);
            query += orderBy;

            Cursor cursor = db.rawQuery(query, null);
            if (cursor != null && cursor.moveToFirst()){
                do {
                    int recList = cursor.getInt(0);
                    long timestamp = cursor.getLong(1);
                    String type = cursor.getString(2);
                    String status = cursor.getString(3);
                    String msg = "";
                    CustomerBroadcast cb = new CustomerBroadcast(timestamp, recList, msg, type, "");
                    cb.setStatus(status);

                    List<String> phoneList = getPhoneNumByRecipientListId(db, cb.getRecipientListId());
                    cb.setRecipientPhoneNumbers(phoneList);

                    cbList.add(cb);

                } while (cursor.moveToNext());
            }
        } finally {
            if (db != null){
                db.close();
            }
        }

        return cbList.toArray(new CustomerBroadcast[0]);
    }


}
