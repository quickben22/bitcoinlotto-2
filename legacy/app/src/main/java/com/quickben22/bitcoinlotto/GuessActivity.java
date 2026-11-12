package com.quickben22.bitcoinlotto;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.tabs.TabLayout;
import com.quickben22.bitcoinlotto.fragments.OneFragment;
import com.quickben22.bitcoinlotto.fragments.TwoFragment;
import com.quickben22.bitcoinlotto.fragments.ThreeFragment;

public class GuessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guess);

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        // Ensure shared data is initialized
        if (CryptoClass.cl == null) {
            CryptoClass.cl = new SqliteClass(this);
        }
        if (CryptoClass.keysD == null) {
            java.util.ArrayList<String> data = CryptoClass.cl.GetSearchData();
            if (data.size() >= 14) {
                CryptoClass.keysD = new KeysData(
                        data.get(1), "", "", "", String.valueOf(data.get(1).length()),
                        data.get(0), data.get(2), "0", "", "",
                        "", data.get(3), "", data.get(4), "", data.get(5),
                        "", data.get(6), "", data.get(7), "", data.get(8),
                        "", data.get(9), "", data.get(10), "", data.get(11),
                        "", data.get(12), data.get(13),
                        "0", ""
                );
            } else {
                // Default initialization for a new app install
                CryptoClass.keysD = new KeysData(
                        "0000000000000000000000000000000000000000000000000000000000000001", 
                        "", "", "", "64", "0", "0", "0", "", "",
                        "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "0", ""
                );
            }
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0: return new OneFragment();
                case 1: return new TwoFragment();
                case 2: return new ThreeFragment();
                default: return null;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0: return "STATS";
                case 1: return "SOLVER";
                case 2: return "RIDDLE";
            }
            return null;
        }
    }
}
