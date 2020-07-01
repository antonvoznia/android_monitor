package cti.com.androidmonitor;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import cti.com.androidmonitor.fragments.LinpackTest;
import cti.com.androidmonitor.fragments.Ping;
import cti.com.androidmonitor.fragments.Processes;
import cti.com.androidmonitor.fragments.SystemInfo;
import cti.com.androidmonitor.fragments.Total;

public class Main extends AppCompatActivity {

    Fragment[] fr;

    final String[] titles = {"Total", "Processes", "Ping", "Test", "System"};

    private final int COUNT_FRAGMENTS = 5;

    private final String TAG_FRAGMENT_POS = "FRAGMENT_POS";

    ViewPager mViewPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        fr = new Fragment[COUNT_FRAGMENTS];
        fr[0] = new Total();
        fr[1] = new Processes();
        fr[2] = new Ping();
        fr[3] = new LinpackTest();
        fr[4] = new SystemInfo();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // ViewPager and its adapters use support library
        // fragments, so use getSupportFragmentManager.
        DemoCollectionPagerAdapter mDemoCollectionPagerAdapter =
                new DemoCollectionPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mViewPager.setAdapter(mDemoCollectionPagerAdapter);
        mViewPager.setSaveEnabled(false);
        mViewPager.setOffscreenPageLimit(2);

        final TabLayout tab = (TabLayout) findViewById(R.id.sliding_tabs);
        tab.addTab(tab.newTab().setText(titles[0]));
        tab.addTab(tab.newTab().setText(titles[1]));
        tab.addTab(tab.newTab().setText(titles[2]));
        tab.addTab(tab.newTab().setText(titles[3]));
        tab.addTab(tab.newTab().setText(titles[4]));
        tab.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                tab.setScrollPosition(position, positionOffset, true);
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        if (savedInstanceState != null) {
            int pos = savedInstanceState.getInt(TAG_FRAGMENT_POS);
            mViewPager.setCurrentItem(pos);
        }
    }

    // Since this is an object collection, use a FragmentStatePagerAdapter,
    // and NOT a FragmentPagerAdapter.
    public class DemoCollectionPagerAdapter extends FragmentPagerAdapter {
        public DemoCollectionPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            /*Bundle bundle = new Bundle();
            bundle.putInt(TAG_FRAGMENT_POS, i);
            fm.setArguments(bundle);
            fm.setRetainInstance(true);*/
            return fr[i];
        }

        @Override
        public int getCount() {
            return COUNT_FRAGMENTS;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(TAG_FRAGMENT_POS, mViewPager.getCurrentItem());
    }
}
