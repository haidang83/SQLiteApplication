package com.kpblog.tt.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.util.Log;

import com.kpblog.tt.model.Customer;
import com.kpblog.tt.model.CustomerClaimCode;
import com.kpblog.tt.model.CustomerPurchase;
import com.kpblog.tt.util.Constants;
import com.kpblog.tt.util.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.Date;
import java.util.ArrayList;
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
    private static final String KEY_DATE_ISSUED = "dateIssued";

    private static final String TABLE_ADMIN = "admin";



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
        String CREATE_CUSTOMER_PURCHASE_TABLE = "CREATE TABLE %s (%s TEXT, %s REAL, %s INTEGER, %s INTEGER, %s TEXT)";
        sqLiteDatabase.execSQL(String.format(CREATE_CUSTOMER_PURCHASE_TABLE, TABLE_CUSTOMER_PURCHASE, KEY_CUSTOMER_ID, KEY_QUANTITY, KEY_RECEIPT_NUM, KEY_PURCHASE_DATE, KEY_NOTES));

        //this table has the outstanding claim code for the customer, only at most 1 outstanding code per customer
        String CREATE_CUSTOMER_CLAIM_CODE_TABLE = "CREATE TABLE %s (%s TEXT PRIMARY KEY, %s TEXT, %s INTEGER)";
        sqLiteDatabase.execSQL(String.format(CREATE_CUSTOMER_CLAIM_CODE_TABLE, TABLE_CUSTOMER_CLAIM_CODE, KEY_CUSTOMER_ID, KEY_CLAIM_CODE, KEY_DATE_ISSUED));

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

        values.put(KEY_DATE_ISSUED, cc.getIssuedDate().getTime());

        int id = (int) db.insertWithOnConflict(TABLE_CUSTOMER_CLAIM_CODE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        if (id == -1) {
            //row already exists by Primary key(Phone Num)
            db.update(TABLE_CUSTOMER_CLAIM_CODE, values, KEY_CUSTOMER_ID + "=?", new String[] {cc.getCustomerId()});
        }

        db.close();
    }

    public String getClaimCodeByCustomerId(String customerId) {
        String code = "";
        SQLiteDatabase db = this.getReadableDatabase();

        // Select All Query
        Cursor cursor = db.query(TABLE_CUSTOMER_CLAIM_CODE, new String[] {KEY_CLAIM_CODE}, KEY_CUSTOMER_ID + "=?", new String[] { customerId },
                null, null, null, null);

        // Getting the address list which we already into our database
        if (cursor != null && cursor.moveToFirst()) {
            code = cursor.getString(0);
        }

        db.close();
        return code;
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

    public void deleteClaimCodeForCustomerId(String customerId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CUSTOMER_CLAIM_CODE, KEY_CUSTOMER_ID + "=?", new String []{customerId});
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

    public List<Customer> searchCustomerByLastVisitAndText(int lastVisitOrDrinkCreditMin,
                                                           int lastVisitOrDrinkCreditMax,
                                                           int lastTextMinDayInt,
                                                           int lastTextMaxDayInt,
                                                           double drinkCreditMinDouble, double drinkCreditMaxDouble,
                                                           String sortByDbColumn,
                                                           String sortOrder) {

        List<Customer> customerList = new ArrayList<Customer>();
        SQLiteDatabase db = null;
        try {

            String selectQuery = getSelectQuery(lastVisitOrDrinkCreditMin, lastVisitOrDrinkCreditMax,
                                    lastTextMinDayInt, lastTextMaxDayInt, drinkCreditMinDouble, drinkCreditMaxDouble, sortByDbColumn, sortOrder);

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
                                  double drinkCreditMinDouble, double drinkCreditMaxDouble, String sortByDbColumn, String sortOrder) {

        Calendar today = new GregorianCalendar();
        long todayInMillis = today.getTimeInMillis();

        /**
         * SELECT customerId, lastVisitDate, totalCredit, lastContactDate
         FROM table_name
         WHERE (lastContactDate IS NULL OR (lastContactDate >= lastTextStartDate AND lastContactDate <= lastTextEndDate))
         AND (lastVisitDate >= lastVisitStartDate and lastVisitDate =< lastVisitEndDate)
         */
        String selectClauseFormat = String.format("SELECT %s, %s, %s, %s, %s, (%s + %s) as %s, %s FROM %s ", KEY_CUSTOMER_ID, KEY_LAST_VISIT_DATE, KEY_PURCHASE_CREDIT, KEY_LAST_CONTACTED_DATE,
                                KEY_REFERRAL_CREDIT, KEY_PURCHASE_CREDIT, KEY_REFERRAL_CREDIT, KEY_TOTAL_CREDIT, KEY_IS_OPT_IN, TABLE_CUSTOMER);

        String selectCondition = "";

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
}
