package edu.nyp.deafapp.CustomView;

import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

import java.util.List;

import edu.nyp.deafapp.R;

/**
 * @author Administrator
 * @des ${TODO}
 * @verson $Rev$
 * @updateAuthor $Author$
 * @updateDes ${TODO}
 */
public class CustomMarkerView extends MarkerView {

    private TextView tvContent;
    private List<Integer> wordPositionList;
    private List<String> splitWordList;
    private float baseLine;

    public CustomMarkerView(Context context, int layoutResource, List<Integer> wordPositionList, List<String> splitWordList, float baseLine) {
        super(context, layoutResource);
        tvContent = (TextView) findViewById(R.id.tvContent);
        this.wordPositionList = wordPositionList;
        this.splitWordList = splitWordList;
        this.baseLine = baseLine;
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        int count = 0;
        for (int i = 0; i < wordPositionList.size(); i++) {
            if(e.getY() == baseLine && wordPositionList.get(i) == e.getX()) {
                tvContent.setText(splitWordList.get(i));
                count++;
                break;
            }
        }
        if(count == 0) {
            tvContent.setText("");
        }
        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffset() {
        return new MPPointF(-(getWidth() / 2), -getHeight());
    }
}