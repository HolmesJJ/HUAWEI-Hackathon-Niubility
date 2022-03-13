package edu.nyp.deafapp.Fragment.Game;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import edu.nyp.deafapp.CustomView.CustomToast;
import edu.nyp.deafapp.CustomView.MoveControl;
import edu.nyp.deafapp.Fragment.MainFragment;
import edu.nyp.deafapp.R;
import me.yokeyword.fragmentation.SupportFragment;

/**
 * @author Administrator
 * @des ${TODO}
 * @verson $Rev$
 * @updateAuthor $Author$
 * @updateDes ${TODO}
 */
public class GameFragment extends SupportFragment {

    private static final String PACKAGE_NAME = "com.example.hp";

    private Toolbar mToolbar;
    private Button startBtn;
    private TextView leaderboard;
    private TextView scoreTxt;
    private ImageView physicalCube;
    private MoveControl moveControl;

    public static GameFragment newInstance() {
        GameFragment fragment = new GameFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_game, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        mToolbar = (Toolbar) view.findViewById(R.id.toolbar);
        startBtn = (Button) view.findViewById(R.id.start_btn);
        leaderboard = (TextView) view.findViewById(R.id.leaderboard);
        scoreTxt = (TextView) view.findViewById(R.id.score_txt);
        physicalCube = (ImageView) view.findViewById(R.id.physical_cube);
        moveControl = new MoveControl(_mActivity, view);
        mToolbar.setTitle(R.string.fragment_cube);
    }

    @Override
    public void onEnterAnimationEnd(Bundle savedInstanceState) {
        super.onEnterAnimationEnd(savedInstanceState);
        initDelayView();
    }

    private void initDelayView() {
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moveControl.score = 0;
                if(!moveControl.isGameStarted) {
                    scoreTxt.setText(String.valueOf(moveControl.score));
                    moveControl.startGame();
                }
                else {
                    scoreTxt.setText(String.valueOf(moveControl.score));
                    moveControl.stopGame();
                }
            }
        });

        leaderboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainFragment) getParentFragment()).startBrotherFragment(GameLeaderboardFragment.newInstance());
            }
        });

        physicalCube.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent launchIntent = _mActivity.getPackageManager().getLaunchIntentForPackage(PACKAGE_NAME);
                if(launchIntent != null) {
                    _mActivity.startActivity(launchIntent);
                }
                else {
                    CustomToast.show(_mActivity, "Launch failed");
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
