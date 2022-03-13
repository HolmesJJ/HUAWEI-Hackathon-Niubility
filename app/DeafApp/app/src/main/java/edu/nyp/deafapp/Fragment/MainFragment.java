package edu.nyp.deafapp.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import edu.nyp.deafapp.CustomView.CustomToast;
import edu.nyp.deafapp.Fragment.Game.GameFragment;
import edu.nyp.deafapp.Fragment.Task.TaskFragment;
import edu.nyp.deafapp.R;
import edu.nyp.deafapp.SharedPreferences.GetSetSharedPreferences;
import edu.nyp.deafapp.UI.BottomBar;
import edu.nyp.deafapp.UI.BottomBarTab;
import me.yokeyword.fragmentation.SupportFragment;

/**
 * @author Administrator
 * @des ${TODO}
 * @verson $Rev$
 * @updateAuthor $Author$
 * @updateDes ${TODO}
 */
public class MainFragment extends SupportFragment {

    private static final String PACKAGE_NAME = "com.debug_version.TiaoYiTiao";
    public static final int FIRST = 0;
    public static final int SECOND = 1;
    private SupportFragment[] mFragments = new SupportFragment[2];
    private BottomBar mBottomBar;

    private int prePrePosition;

    public static MainFragment newInstance() {
        Bundle args = new Bundle();
        MainFragment fragment = new MainFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        initView(view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        SupportFragment firstFragment = findFragment(TaskFragment.class);
        if (firstFragment == null) {

            mFragments[FIRST] = TaskFragment.newInstance();
            mFragments[SECOND] = GameFragment.newInstance();

            loadMultipleRootFragment(R.id.fl_main_container, FIRST,
                    mFragments[FIRST],
                    mFragments[SECOND]
            );

        } else {
            mFragments[FIRST] = firstFragment;
            mFragments[SECOND] = findFragment(GameFragment.class);
        }
    }

    private void initView(View view) {
        mBottomBar = (BottomBar) view.findViewById(R.id.bottomBar);

        mBottomBar.addItem(new BottomBarTab(_mActivity, R.drawable.ic_task, getString(R.string.fragment_task)))
                .addItem(new BottomBarTab(_mActivity, R.drawable.ic_cube, getString(R.string.fragment_cube)))
                .addItem(new BottomBarTab(_mActivity, R.drawable.ic_jump, getString(R.string.fragment_jump)))
                .addItem(new BottomBarTab(_mActivity, R.drawable.ic_logout, getString(R.string.fragment_logout)));

        mBottomBar.setOnTabSelectedListener(new BottomBar.OnTabSelectedListener() {
            @Override
            public void onTabSelected(int position, int prePosition) {
                if((position != 2) && (position != 3)) {
                    if((prePosition == 2) || (prePosition == 3)) {
                        prePosition = prePrePosition;
                    }
                    showHideFragment(mFragments[position], mFragments[prePosition]);
                }
                else {
                    if((prePosition != 2) && (prePosition != 3)) {
                        prePrePosition = prePosition;
                    }
                    if(position == 2) {
                        Intent launchIntent = _mActivity.getPackageManager().getLaunchIntentForPackage(PACKAGE_NAME);
                        if(launchIntent != null) {
                            _mActivity.startActivity(launchIntent);
                        }
                        else {
                            CustomToast.show(_mActivity, "Launch failed");
                        }
                    }
                    else {
                        CustomToast.show(_mActivity, "Click again to exit");
                    }
                }
            }

            @Override
            public void onTabUnselected(int position) {

            }

            @Override
            public void onTabReselected(int position) {
                if(position == 3) {
                    GetSetSharedPreferences.removeDefaults("userId", _mActivity);
                    _mActivity.finish();
                    System.exit(0);
                }
            }
        });
    }

    public void startBrotherFragment(SupportFragment targetFragment) {
        start(targetFragment);
    }

    public void changeTab(int position) {
        mBottomBar.setCurrentItem(0);
    }

    @Override
    public boolean onBackPressedSupport() {
        if(mBottomBar.getCurrentItemPosition() == 0) {
            return false;
        }
        else {
            mBottomBar.setCurrentItem(0);
            return true;
        }
    }
}
