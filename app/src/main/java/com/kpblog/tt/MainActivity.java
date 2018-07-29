package com.kpblog.tt;

import android.Manifest;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.kpblog.tt.adapter.AddressAdapter;
import com.kpblog.tt.dao.DatabaseHandler;
import com.kpblog.tt.model.Customer;
import com.kpblog.tt.model.CustomerPurchase;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
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
                                                                Fragment_Dashboard.OnFragmentInteractionListener{

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

        //uncomment to see the db entries on screen
        /*listView = (ListView) findViewById(R.id.addressListView);
        list = handler.getAllAddress();
        addressAdapter = new AddressAdapter(this);
        listView.setAdapter(addressAdapter);*/

    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new Fragment_RegisterOrUpdate(), getString(R.string.tab_registration));
        adapter.addFragment(new Fragment_Claim(), getString(R.string.tab_claim));
        adapter.addFragment(new Fragment_Admin(), getString(R.string.tab_admin));
        adapter.addFragment(new Fragment_Dashboard(), getString(R.string.tab_dashboard));
        adapter.addFragment(new Fragment_Customer(), "Customer");
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


}
