package edu.nyp.deafapp.Adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import edu.nyp.deafapp.Model.GameLeaderboard;
import edu.nyp.deafapp.R;

/**
 * @author Administrator
 * @des ${TODO}
 * @verson $Rev$
 * @updateAuthor $Author$
 * @updateDes ${TODO}
 */
public class GameLeaderboardAdapter extends BaseAdapter {

    private Context mContext;
    private List<GameLeaderboard> mGameLeaderboardList;

    public GameLeaderboardAdapter(Context context, List<GameLeaderboard> mGameLeaderboardList) {
        mContext = context;
        this.mGameLeaderboardList = mGameLeaderboardList;
    }

    @Override
    public int getCount() {
        return mGameLeaderboardList.size();
    }

    @Override
    public Object getItem(int position) {
        return mGameLeaderboardList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = View.inflate(mContext, R.layout.game_leaderboard_listview, null);

        TextView rankTxt = (TextView) v.findViewById(R.id.rank);
        TextView nameTxt = (TextView) v.findViewById(R.id.name);
        TextView scoreTxt = (TextView) v.findViewById(R.id.score);

        rankTxt.setText(String.valueOf(mGameLeaderboardList.get(position).getRank()));
        nameTxt.setText(mGameLeaderboardList.get(position).getName());
        scoreTxt.setText(String.valueOf(mGameLeaderboardList.get(position).getScore()));

        return v;
    }
}
