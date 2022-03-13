package edu.nyp.deafapp.Adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import edu.nyp.deafapp.Fragment.Study.Child.Initialization;
import edu.nyp.deafapp.Fragment.Study.Child.ResultLineChart;
import edu.nyp.deafapp.Fragment.Study.Child.StandardLineChart;
import edu.nyp.deafapp.Fragment.Study.Child.StandardTestLineChart;
import edu.nyp.deafapp.Fragment.Study.Child.TestLineChart;

/**
 * @author Administrator
 * @des ${TODO}
 * @verson $Rev$
 * @updateAuthor $Author$
 * @updateDes ${TODO}
 */
public class ChartChildAdapter extends FragmentPagerAdapter {
    private String[] mTitles;
    private int mEnglishId;
    private String mSyllables;

    public ChartChildAdapter(FragmentManager fm, int englishId, String syllables, String... titles) {
        super(fm);
        mEnglishId = englishId;
        mSyllables = syllables;
        mTitles = titles;
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return Initialization.newInstance(mEnglishId, mSyllables);
        }
        if (position == 1) {
            return StandardLineChart.newInstance(mEnglishId, mSyllables);
        }
        else if (position == 2) {
            return TestLineChart.newInstance(mEnglishId, mSyllables);
        }
        else if (position == 3) {
            return StandardTestLineChart.newInstance(mEnglishId, mSyllables);
        }
        else {
            return ResultLineChart.newInstance(mEnglishId, mSyllables);
        }
    }

    @Override
    public int getCount() {
        return mTitles.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles[position];
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }
}
