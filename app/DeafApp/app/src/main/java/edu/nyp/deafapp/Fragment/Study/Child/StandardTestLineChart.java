package edu.nyp.deafapp.Fragment.Study.Child;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.Legend.LegendForm;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.components.YAxis.AxisDependency;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import edu.nyp.deafapp.Comparator.SplitLineComparator;
import edu.nyp.deafapp.CustomView.CustomMarkerView;
import edu.nyp.deafapp.Demo.DemoMode;
import edu.nyp.deafapp.HttpAsyncTask.Get;
import edu.nyp.deafapp.IPAddress;
import edu.nyp.deafapp.Interface.OnTaskCompleted;
import edu.nyp.deafapp.LoginActivity;
import edu.nyp.deafapp.R;
import edu.nyp.deafapp.SharedPreferences.GetSetSharedPreferences;
import edu.nyp.deafapp.Token.TokenHelper;
import me.yokeyword.fragmentation.SupportFragment;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * @author Administrator
 * @des ${TODO}
 * @verson $Rev$
 * @updateAuthor $Author$
 * @updateDes ${TODO}
 */
public class StandardTestLineChart extends SupportFragment implements OnChartValueSelectedListener, OnTaskCompleted {

    private static final String AUDIO_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DeafApp/Record";
    private static final String ENGLISH_ID = "englishId";
    private static final String SYLLABLES_ID = "syllables";
    private static final int CORRECT_DEMO_MODE_ID = 1;
    private static final int WRONG_DEMO_MODE_ID = 2;

    private Context context;
    private LineChart standardLineChart;
    private LineChart testLineChart;
    private ProgressBar mProgressView;
    private String mAudioPath;
    private int userId;
    private int englishId;
    private String syllables;
    private String[] syllablesArr;
    private String token = "";

    private OkHttpClient client;

    private List<Double> standardDataList = new ArrayList<>();
    private List<Integer> standardSplitLineList = new ArrayList<>();
    private List<Integer> standardWordPositionList = new ArrayList<>();
    private List<String> standardSplitWordList = new ArrayList<>();

    private List<Double> testDataList = new ArrayList<>();
    private List<Integer> testSplitLineList = new ArrayList<>();
    private List<Integer> testWordPositionList = new ArrayList<>();
    private List<String> testSplitWordList = new ArrayList<>();

    public static StandardTestLineChart newInstance(int englishId, String syllables) {
        Bundle args = new Bundle();
        StandardTestLineChart fragment = new StandardTestLineChart();
        args.putInt(ENGLISH_ID, englishId);
        args.putString(SYLLABLES_ID, syllables);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.standard_test_line_chart, container, false);

        userId = Integer.valueOf(GetSetSharedPreferences.getDefaults("userId", _mActivity.getApplicationContext()));
        token = GetSetSharedPreferences.getDefaults("token", _mActivity.getApplicationContext());

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
        initView(rootView);

        standardDataList.clear();
        standardSplitLineList.clear();
        standardWordPositionList.clear();
        standardSplitWordList.clear();
        testDataList.clear();
        testSplitLineList.clear();
        testWordPositionList.clear();
        testSplitWordList.clear();

        mAudioPath = AUDIO_DIR + "/record.mp3";

        showProgress(true);
        if(DemoMode.isDemoMode()) {
            if(DemoMode.isCorrectPronunciationDemo()) {
                getCorrectDemo();
            } else {
                getWrongDemo();
            }
        } else {
            uploadAudio();
        }

        return rootView;
    }

    private void initView(View rootView) {
        context = getContext();
        standardLineChart = rootView.findViewById(R.id.standard_line_chart);
        testLineChart = rootView.findViewById(R.id.test_line_chart);
        mProgressView = rootView.findViewById(R.id.loading_progress);
    }

    private void initStandardLineChart(int xRange) {
        standardLineChart.setOnChartValueSelectedListener(this);

        standardLineChart.getDescription().setEnabled(false);
        standardLineChart.setDrawGridBackground(false);
        standardLineChart.setBackgroundColor(Color.TRANSPARENT);

        standardLineChart.setDragEnabled(true);
        standardLineChart.setScaleEnabled(true);
        standardLineChart.setPinchZoom(true);
        standardLineChart.setHighlightPerDragEnabled(true);
        standardLineChart.animateX(1000);

        standardLineChart.setTouchEnabled(true);
        standardLineChart.setDragDecelerationEnabled(true);
        standardLineChart.setDragDecelerationFrictionCoef(0.9f);

        Legend l = standardLineChart.getLegend();
        l.setForm(LegendForm.LINE);
        l.setTextColor(Color.BLACK);

        XAxis xAxis = standardLineChart.getXAxis();
        xAxis.setTextColor(Color.rgb(0, 0, 0));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(10f);
        xAxis.setDrawLabels(true);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularityEnabled(true);

        YAxis leftAxis = standardLineChart.getAxisLeft();
        leftAxis.setTextColor(Color.rgb(0, 0, 0));
        leftAxis.setTextSize(10f);
        leftAxis.setAxisMaximum(0.6f);
        leftAxis.setAxisMinimum(-0.6f);
        leftAxis.setDrawGridLines(false);
        leftAxis.setGranularityEnabled(true);

        YAxis rightAxis = standardLineChart.getAxisRight();
        rightAxis.setEnabled(false);

        setStandardData(xRange);
    }

    private void setStandardData(int xRange) {
        ArrayList<Entry> standardData = new ArrayList<>();

        for (int i = 0; i < standardDataList.size(); i++) {
            standardData.add(new Entry(i, standardDataList.get(i).floatValue(), String.valueOf(i) + "F"));
        }

        ArrayList<Entry> standardSplitLine = new ArrayList<>();
        ArrayList<Entry> standardWordPosition = new ArrayList<>();

        for (int i = 0; i < standardSplitLineList.size(); i++) {
            if(i % 2 == 0) {
                standardSplitLine.add(new Entry(standardSplitLineList.get(i), 1f));
                standardSplitLine.add(new Entry(standardSplitLineList.get(i), -1f));
            }
            else {
                standardSplitLine.add(new Entry(standardSplitLineList.get(i), -1f));
                standardSplitLine.add(new Entry(standardSplitLineList.get(i), 1f));
            }
        }

        for (int i = 0; i < standardSplitLineList.size(); i++) {
            if(i > 0 && i % 2 == 1) {
                int position = (standardSplitLineList.get(i-1) + standardSplitLineList.get(i)) / 2;
                standardWordPosition.add(new Entry(position, 0.4f));
                standardWordPositionList.add(position);
            }
        }

        LineDataSet standardDataSet, standardSplitLineSet, standardWordPositionSet;
        standardDataSet = new LineDataSet(standardData, "Standard Line");
        standardDataSet.setAxisDependency(AxisDependency.LEFT);
        standardDataSet.setColor(Color.GREEN);
        standardDataSet.setLineWidth(1f);
        standardDataSet.setDrawCircles(false);

        standardSplitLineSet = new LineDataSet(standardSplitLine, "Split Line");
        standardSplitLineSet.setColor(Color.rgb(0, 255, 255));
        standardSplitLineSet.setLineWidth(1f);
        standardSplitLineSet.setDrawCircles(false);
        standardSplitLineSet.setDrawValues(false);

        standardWordPositionSet = new LineDataSet(standardWordPosition, "");
        standardWordPositionSet.setColor(Color.TRANSPARENT);
        standardWordPositionSet.setLineWidth(1f);
        standardWordPositionSet.setCircleColor(Color.BLUE);
        standardWordPositionSet.setCircleHoleColor(Color.WHITE);
        standardWordPositionSet.setCircleRadius(4f);

        LineData data = new LineData(standardDataSet, standardSplitLineSet, standardWordPositionSet);
        standardLineChart.setData(data);

        for (int i = 0; i < syllablesArr.length; i++) {
            standardSplitWordList.add(syllablesArr[i]);
        }
        CustomMarkerView mv = new CustomMarkerView(_mActivity, R.layout.marker_view, standardWordPositionList, standardSplitWordList, 0.4f);
        mv.setChartView(standardLineChart);
        standardLineChart.setMarker(mv);

        XAxis xAxis = standardLineChart.getXAxis();
        xAxis.setValueFormatter((value, axis) -> String.valueOf(standardData.get((int)value).getData()));

        standardLineChart.notifyDataSetChanged();
        standardLineChart.setVisibleXRangeMaximum(xRange);
    }

    private void initTestLineChart(int xRange) {
        testLineChart.setOnChartValueSelectedListener(this);

        testLineChart.getDescription().setEnabled(false);
        testLineChart.setDrawGridBackground(false);
        testLineChart.setBackgroundColor(Color.TRANSPARENT);

        testLineChart.setDragEnabled(true);
        testLineChart.setScaleEnabled(true);
        testLineChart.setPinchZoom(true);
        testLineChart.setHighlightPerDragEnabled(true);
        testLineChart.animateX(1000);

        testLineChart.setTouchEnabled(true);
        testLineChart.setDragDecelerationEnabled(true);
        testLineChart.setDragDecelerationFrictionCoef(0.9f);

        Legend l = testLineChart.getLegend();
        l.setForm(LegendForm.LINE);
        l.setTextColor(Color.BLACK);

        XAxis xAxis = testLineChart.getXAxis();
        xAxis.setTextColor(Color.rgb(0, 0, 0));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(10f);
        xAxis.setDrawLabels(true);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularityEnabled(true);

        YAxis leftAxis = testLineChart.getAxisLeft();
        leftAxis.setTextColor(Color.rgb(0, 0, 0));
        leftAxis.setTextSize(10f);
        leftAxis.setAxisMaximum(0.6f);
        leftAxis.setAxisMinimum(-0.6f);
        leftAxis.setDrawGridLines(false);
        leftAxis.setGranularityEnabled(true);

        YAxis rightAxis = testLineChart.getAxisRight();
        rightAxis.setEnabled(false);

        setTestData(xRange);
    }

    private void setTestData(int xRange) {
        ArrayList<Entry> testData = new ArrayList<>();

        for (int i = 0; i < testDataList.size(); i++) {
            testData.add(new Entry(i, testDataList.get(i).floatValue(), String.valueOf(i) + "F"));
        }

        ArrayList<Entry> testSplitLine = new ArrayList<>();
        ArrayList<Entry> testWordPosition = new ArrayList<>();

        for (int i = 0; i < testSplitLineList.size(); i++) {
            if(i % 2 == 0) {
                testSplitLine.add(new Entry(testSplitLineList.get(i), 1f));
                testSplitLine.add(new Entry(testSplitLineList.get(i), -1f));
            }
            else {
                testSplitLine.add(new Entry(testSplitLineList.get(i), -1f));
                testSplitLine.add(new Entry(testSplitLineList.get(i), 1f));
            }
        }

        for (int i = 0; i < testSplitLineList.size(); i++) {
            if(i > 0 && i % 2 == 1) {
                int position = (testSplitLineList.get(i-1) + testSplitLineList.get(i)) / 2;
                testWordPosition.add(new Entry(position, 0.4f));
                testWordPositionList.add(position);
            }
        }

        LineDataSet testDataSet, testSplitLineSet, testWordPositionSet;
        testDataSet = new LineDataSet(testData, "Test Line");
        testDataSet.setAxisDependency(AxisDependency.LEFT);
        testDataSet.setColor(Color.GREEN);
        testDataSet.setLineWidth(1f);
        testDataSet.setDrawCircles(false);

        testSplitLineSet = new LineDataSet(testSplitLine, "Split Line");
        testSplitLineSet.setColor(Color.rgb(0, 255, 255));
        testSplitLineSet.setLineWidth(1f);
        testSplitLineSet.setDrawCircles(false);
        testSplitLineSet.setDrawValues(false);

        testWordPositionSet = new LineDataSet(testWordPosition, "");
        testWordPositionSet.setColor(Color.TRANSPARENT);
        testWordPositionSet.setLineWidth(1f);
        testWordPositionSet.setCircleColor(Color.BLUE);
        testWordPositionSet.setCircleHoleColor(Color.WHITE);
        testWordPositionSet.setCircleRadius(4f);

        LineData data = new LineData(testDataSet, testSplitLineSet, testWordPositionSet);
        testLineChart.setData(data);

        if(syllablesArr.length < testWordPositionList.size()) {
            for (int i = 0; i < syllablesArr.length; i++) {
                testSplitWordList.add(syllablesArr[i]);
            }
            for (int i = syllablesArr.length; i < testWordPositionList.size(); i++) {
                testSplitWordList.add("error");
            }
        }
        else if(syllablesArr.length == testWordPositionList.size()) {
            for (int i = 0; i < syllablesArr.length; i++) {
                testSplitWordList.add(syllablesArr[i]);
            }
        }
        else {
            for (int i = 0; i < testWordPositionList.size(); i++) {
                testSplitWordList.add(syllablesArr[i]);
            }
        }

        CustomMarkerView mv = new CustomMarkerView(_mActivity, R.layout.marker_view, testWordPositionList, testSplitWordList, 0.4f);
        mv.setChartView(testLineChart);
        testLineChart.setMarker(mv);

        XAxis xAxis = testLineChart.getXAxis();
        xAxis.setValueFormatter((value, axis) -> String.valueOf(testData.get((int)value).getData()));

        testLineChart.notifyDataSetChanged();
        testLineChart.setVisibleXRangeMaximum(xRange);
    }

    private void uploadAudio(){
        File file = new File(mAudioPath);
        String file_path = file.getAbsolutePath();

        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        RequestBody file_body = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        RequestBody request_body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("userId", String.valueOf(userId))
                .addFormDataPart("englishId", String.valueOf(englishId))
                .addFormDataPart("audioFile", file_path.substring(file_path.lastIndexOf("/")+1), file_body)
                .build();

        Request request = new Request.Builder().url(IPAddress.getIpAddress() + "/api/upload")
                .post(request_body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                _mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        e.printStackTrace();
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                int statusCode = response.code();
                String result = response.body().string();
                _mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(statusCode == HttpURLConnection.HTTP_OK) {
                            try {
                                JSONObject jsonObject = new JSONObject(result);

                                JSONArray arrD1 = jsonObject.getJSONArray("d1");
                                for(int i = 0; i < arrD1.length(); i++) {
                                    standardDataList.add(arrD1.getDouble(i));
                                }
                                JSONArray standardSplitLine = jsonObject.getJSONArray("step1");
                                for(int i = 0; i < standardSplitLine.length(); i++) {
                                    standardSplitLineList.add(standardSplitLine.getInt(i));
                                }

                                JSONArray arrD2 = jsonObject.getJSONArray("d2");
                                for(int i = 0; i < arrD2.length(); i++) {
                                    testDataList.add(arrD2.getDouble(i));
                                }
                                JSONArray splitLine = jsonObject.getJSONArray("step2");
                                for(int i = 0; i < splitLine.length(); i++) {
                                    testSplitLineList.add(splitLine.getInt(i));
                                }

                                Comparator cmp = new SplitLineComparator();
                                Collections.sort(standardSplitLineList, cmp);
                                Collections.sort(testSplitLineList, cmp);

                                initStandardLineChart(standardDataList.size());
                                initTestLineChart(testDataList.size());

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        showProgress(false);
                    }
                });
            }
        });
    }

    private void getCorrectDemo(){
        Get getCorrectDemoTask = new Get(StandardTestLineChart.this, CORRECT_DEMO_MODE_ID);
        getCorrectDemoTask.execute(IPAddress.getIpAddress() + "/api/correctDemo");
    }

    private void getWrongDemo(){
        Get getWrongDemoTask = new Get(StandardTestLineChart.this, WRONG_DEMO_MODE_ID);
        getWrongDemoTask.execute(IPAddress.getIpAddress() + "/api/wrongDemo");
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onTaskCompleted(String response, int requestId) {
        if ((requestId == CORRECT_DEMO_MODE_ID) || (requestId == WRONG_DEMO_MODE_ID)) {
            try {
                JSONObject jsonObject = new JSONObject(response);

                JSONArray arrD1 = jsonObject.getJSONArray("d1");
                for(int i = 0; i < arrD1.length(); i++) {
                    standardDataList.add(arrD1.getDouble(i));
                }
                JSONArray standardSplitLine = jsonObject.getJSONArray("step1");
                for(int i = 0; i < standardSplitLine.length(); i++) {
                    standardSplitLineList.add(standardSplitLine.getInt(i));
                }

                JSONArray arrD2 = jsonObject.getJSONArray("d2");
                for(int i = 0; i < arrD2.length(); i++) {
                    testDataList.add(arrD2.getDouble(i));
                }
                JSONArray splitLine = jsonObject.getJSONArray("step2");
                for(int i = 0; i < splitLine.length(); i++) {
                    testSplitLineList.add(splitLine.getInt(i));
                }

                Comparator cmp = new SplitLineComparator();
                Collections.sort(standardSplitLineList, cmp);
                Collections.sort(testSplitLineList, cmp);

                initStandardLineChart(standardDataList.size());
                initTestLineChart(testDataList.size());

            } catch (Exception e) {
                e.printStackTrace();
            }
            showProgress(false);
        }
    }
}
