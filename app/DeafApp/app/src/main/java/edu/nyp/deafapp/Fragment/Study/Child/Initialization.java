package edu.nyp.deafapp.Fragment.Study.Child;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import edu.nyp.deafapp.CustomView.CustomToast;
import edu.nyp.deafapp.R;
import me.yokeyword.fragmentation.SupportFragment;

/**
 * @author Administrator
 * @des ${TODO}
 * @verson $Rev$
 * @updateAuthor $Author$
 * @updateDes ${TODO}
 */
public class Initialization extends SupportFragment {

    private static final String PACKAGE_NAME = "xiao.haung.ren.cc.com";
    private static final String ENGLISH_ID = "englishId";
    private static final String SYLLABLES_ID = "syllables";
    private int englishId;
    private String syllables;
    private String[] syllablesArr;

    private ImageButton arPlay;

    public static Initialization newInstance(int englishId, String syllables) {
        Bundle args = new Bundle();
        Initialization fragment = new Initialization();
        args.putInt(ENGLISH_ID, englishId);
        args.putString(SYLLABLES_ID, syllables);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.initialization, container, false);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            englishId = bundle.getInt(ENGLISH_ID, 0);
            syllables = bundle.getString(SYLLABLES_ID);
            try {
                syllablesArr = syllables.split(",");
            } catch (Exception e) {
                syllablesArr[0] = syllables;
            }
        }

        arPlay = (ImageButton) rootView.findViewById(R.id.ar_play);
        arPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent launchIntent = _mActivity.getPackageManager().getLaunchIntentForPackage(PACKAGE_NAME);
                if(launchIntent != null) {
                    _mActivity.startActivity(launchIntent);
                }
                else {
                    CustomToast.show(_mActivity, "Launch failed");
                }
            }
        });

        return rootView;
    }
}
