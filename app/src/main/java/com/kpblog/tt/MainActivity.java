package com.kpblog.tt;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;

import com.kpblog.tt.adapter.AddressAdapter;
import com.kpblog.tt.dao.DatabaseHandler;
import com.kpblog.tt.model.Customer;
import com.kpblog.tt.util.Constants;
import com.kpblog.tt.util.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * issues:
 * 1. only update the opt-in, opt-out date when the value was changed from previous value (don't update every time) [DONE]
 * 2. don't show the opt-in if user already opted in(we'll have an unsubscribe button on the admin tab to opt-out) [DONE]
 * 3. update missing credit when Phone/previousCredit/todayCredit is updated [DONE]
 *
 * 4. tab to claim discount:
 *      a. if previous credit = 10, go to discount tab with phone number filled in
 *      b. if previous credit + todayCredit = 10, pressing confirm will take user to discount tab with phone number filled in
 *
 * 5. Admin tab
 *      a. opt-out
 *      b. add test user
 *      c. export/import db
 *      d. raffle/promotion
 *
 * 6. Add customer purchase table & populate [DONE]
 */
public class MainActivity extends AppCompatActivity implements  Fragment_Customer.OnFragmentInteractionListener,
                                                                Fragment_Claim.OnFragmentInteractionListener,
                                                                Fragment_RegisterOrUpdate.OnFragmentInteractionListener,
                                                                Fragment_Admin.OnFragmentInteractionListener,
                                                                Fragment_Dashboard.OnFragmentInteractionListener,
                                                                Fragment_Transactions.OnFragmentInteractionListener,
                                                                Fragment_Text.OnFragmentInteractionListener{

    public static final int FREE_DRINK_THRESHOLD = 10;
    public static final int TODAY_CREDIT_LIMIT = 10; //number of drinks that can be purchased at 1 time (to avoid typo)
    public static final String AT_LEAST_ONE_DIGIT_REGEXP = "[0-9]+";
    private EditText phone, todayCredit, previousCredit, missingCredit, receiptNum;
    private Button confirmBtn, cancelBtn;
    private CheckBox optIn;
    private DatabaseHandler handler;
    private ListView listView;
    private AddressAdapter addressAdapter;
    public List<Customer> list;
    private Customer customer;

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    boolean permissionRequestedOnStart = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        if (!permissionRequestedOnStart){
            //need to do this on parent activity so that the scheduled db backup process can have read/write permission
            requestReadWritePermission_onStart();
        }

        Util.setNextDbBackupAlarm(getApplicationContext(), Util.getNightlyDbBackupTime());

        //uncomment to see the db entries on screen
        /*listView = (ListView) findViewById(R.id.addressListView);
        list = handler.getAllAddress();
        addressAdapter = new AddressAdapter(this);
        listView.setAdapter(addressAdapter);*/

    }


    private final int REQUEST_CODE_EXTERNAL_STORAGE_ONSTART = 567;
    private void requestReadWritePermission_onStart() {
        permissionRequestedOnStart = true;

        int smsPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS);
        int writePermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

        // check permission is given
        if (writePermission != PackageManager.PERMISSION_GRANTED ||
                readPermission != PackageManager.PERMISSION_GRANTED ||
                smsPermission != PackageManager.PERMISSION_GRANTED) {
            // request permission (see result in onRequestPermissionsResult() method)
            requestPermissions(Constants.PERMISSIONS_STORAGE_SMS, REQUEST_CODE_EXTERNAL_STORAGE_ONSTART);
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new Fragment_RegisterOrUpdate(), getString(R.string.tab_registration));
        adapter.addFragment(new Fragment_Claim(), getString(R.string.tab_claim));
        adapter.addFragment(new Fragment_Customer(), getString(R.string.tab_customer));
        adapter.addFragment(new Fragment_Admin(), getString(R.string.tab_admin));
        adapter.addFragment(new Fragment_Dashboard(), getString(R.string.tab_dashboard));
        adapter.addFragment(new Fragment_Text(), getString(R.string.tab_text));
        adapter.addFragment(new Fragment_Transactions(), getString(R.string.tab_transactions));
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }


    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            new AlertDialog.Builder(this, R.style.AlertDialogTheme)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Exit App?")
                    .setMessage("Do you want to exit the app?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            MainActivity.super.onBackPressed();
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();

        } else {
            MainActivity.super.onBackPressed();
        }
    }
}
