package edu.nyp.deafapp.Fragment.Game;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import edu.nyp.deafapp.Adapter.GameLeaderboardAdapter;
import edu.nyp.deafapp.Base.BaseBackFragment;
import edu.nyp.deafapp.CustomView.CustomToast;
import edu.nyp.deafapp.HttpAsyncTask.Get;
import edu.nyp.deafapp.HttpAsyncTask.GetAuth;
import edu.nyp.deafapp.IPAddress;
import edu.nyp.deafapp.Interface.OnTaskCompleted;
import edu.nyp.deafapp.LoginActivity;
import edu.nyp.deafapp.Model.GameLeaderboard;
import edu.nyp.deafapp.R;
import edu.nyp.deafapp.SharedPreferences.GetSetSharedPreferences;
import edu.nyp.deafapp.Token.TokenHelper;

/**
 * @author Administrator
 * @des ${TODO}
 * @verson $Rev$
 * @updateAuthor $Author$
 * @updateDes ${TODO}
 */
public class GameLeaderboardFragment extends BaseBackFragment implements OnTaskCompleted {

    private static final int GET_GAME_LEADERBOARD = 1;
    private static final int REFRESH_TOKEN_TASK_ID = 100;

    private List<GameLeaderboard> mGameLeaderboardList;
    private GameLeaderboardAdapter mGameLeaderboardAdapter;

    private Toolbar mToolbar;
    private ListView gameLeaderboardListview;
    private String token = "";

    public static GameLeaderboardFragment newInstance(){
        Bundle args = new Bundle();
        GameLeaderboardFragment fragment = new GameLeaderboardFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_game_leaderboard,container,false);
        initView(view);

        token = GetSetSharedPreferences.getDefaults("token", _mActivity.getApplicationContext());

        return attachToSwipeBack(view);
    }

    private void initView(View view) {
        mToolbar = (Toolbar) view.findViewById(R.id.toolbar);
        gameLeaderboardListview = (ListView) view.findViewById(R.id.game_leaderboard_listview);
        mToolbar.setTitle("Cube Hub Leaderboard");
        initToolbarNav(mToolbar);

    }

    @Override
    public void onEnterAnimationEnd(Bundle savedInstanceState) {
        super.onEnterAnimationEnd(savedInstanceState);
        getGameLeaderboard();
    }

    private void getGameLeaderboard(){
        GetAuth getGameLeaderboardTask = new GetAuth(this, GET_GAME_LEADERBOARD);
        getGameLeaderboardTask.execute(IPAddress.getIpAddress() + "/api/cubeHubLeaderboard", token);
    }

    @Override
    public void onTaskCompleted(String response, int requestId) {
        if(response.equals("FORBIDDEN")) {
            TokenHelper.refreshToken(_mActivity, this::onTaskCompleted, REFRESH_TOKEN_TASK_ID);
        } else {
            if(requestId == GET_GAME_LEADERBOARD) {
                try {
                    mGameLeaderboardList = new ArrayList<GameLeaderboard>();

                    JSONArray jsonArray = new JSONArray(response);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        int rank = jsonObject.getInt("rank");
                        String name = jsonObject.getString("name");
                        int score = jsonObject.getInt("score");

                        mGameLeaderboardList.add(new GameLeaderboard(rank,name, score));
                    }

                    mGameLeaderboardAdapter = new GameLeaderboardAdapter(_mActivity, mGameLeaderboardList);
                    gameLeaderboardListview.setAdapter(mGameLeaderboardAdapter);

                } catch (Exception e) {
                    e.printStackTrace();
                    CustomToast.show(_mActivity, "Server error");
                }
            } else if(requestId == REFRESH_TOKEN_TASK_ID) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String Status = jsonObject.getString("status");

                    if(Status.equals("Succeeded")) {
                        String newToken = jsonObject.getString("token");
                        GetSetSharedPreferences.setDefaults("token", newToken, _mActivity.getApplicationContext());
                        token = GetSetSharedPreferences.getDefaults("token", _mActivity.getApplicationContext());
                        getGameLeaderboard();
                    } else {
                        Intent intent = new Intent(_mActivity, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                } catch (Exception e) {
                    Intent intent = new Intent(_mActivity, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }
        }
    }
}
