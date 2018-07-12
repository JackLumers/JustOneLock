package janeelsmur.justonelock.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class PagerAdapter extends FragmentPagerAdapter {

    private Fragment[] pages;

    public PagerAdapter(FragmentManager fm, Fragment[] pages) {
        super(fm);
        this.pages = pages;
    }

    @Override
    public Fragment getItem(int position) {
        return pages[position];
    }
    @Override
    public int getCount() {
        return pages.length;
    }
}
