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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
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
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import edu.nyp.deafapp.Comparator.SplitLineComparator;
import edu.nyp.deafapp.CustomView.CustomMarkerView;
import edu.nyp.deafapp.CustomView.CustomToast;
import edu.nyp.deafapp.CustomView.RangeSeekBar;
import edu.nyp.deafapp.Demo.DemoMode;
import edu.nyp.deafapp.HttpAsyncTask.Get;
import edu.nyp.deafapp.HttpAsyncTask.GetAuth;
import edu.nyp.deafapp.HttpAsyncTask.Post;
import edu.nyp.deafapp.HttpAsyncTask.PostAuth;
import edu.nyp.deafapp.IPAddress;
import edu.nyp.deafapp.Interface.OnTaskCompleted;
import edu.nyp.deafapp.LoginActivity;
import edu.nyp.deafapp.Model.AudioUploadResult;
import edu.nyp.deafapp.Player.IMediaPlayer;
import edu.nyp.deafapp.Player.SlackAudioPlayer;
import edu.nyp.deafapp.R;
import edu.nyp.deafapp.SharedPreferences.GetSetSharedPreferences;
import edu.nyp.deafapp.Token.TokenHelper;
import me.yokeyword.fragmentation.SupportFragment;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
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
public class ResultLineChart extends SupportFragment implements OnChartValueSelectedListener, OnTaskCompleted {

    private static final String AUDIO_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DeafApp/Record";
    private static final String ENGLISH_ID = "englishId";
    private static final String SYLLABLES_ID = "syllables";

    private static final int UPLOAD_RESULT_ID = 1;
    private static final int CORRECT_DEMO_MODE_ID = 3;
    private static final int WRONG_DEMO_MODE_ID = 4;
    private static final int REFRESH_TOKEN_TASK_ID = 100;

    private Context context;
    private LineChart resultLineChart;
    private ImageView playBtn;
    private ImageView upBtn;
    private ImageView downBtn;
    private ProgressBar mProgressView;

    private List<Double> resultDataList = new ArrayList<>();
    private List<Integer> resultSplitLineList = new ArrayList<>();
    private List<Integer> resultWordPositionList = new ArrayList<>();
    private List<String> resultSplitWordList = new ArrayList<>();
    private List<Double> scoreList = new ArrayList<>();

    private int resultLineChartWidth;
    private String mAudioPath;
    private int userId;
    private int englishId;
    private String syllables;
    private String[] syllablesArr;
    private int finalAvgScore;

    private RangeSeekBar mRangSeekBar;
    private IMediaPlayer mMediaPlayer;
    private boolean mIsPlaying = false;
    private String token = "";

    private OkHttpClient client;

    public static ResultLineChart newInstance(int englishId, String syllables) {
        Bundle args = new Bundle();
        ResultLineChart fragment = new ResultLineChart();
        args.putInt(ENGLISH_ID, englishId);
        args.putString(SYLLABLES_ID, syllables);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.result_line_chart, container, false);

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

        rootView.post(new Runnable() {
            @Override
            public void run() {
                resultLineChartWidth = resultLineChart.getWidth();
            }
        });

        resultDataList.clear();
        resultSplitLineList.clear();
        resultWordPositionList.clear();
        resultSplitWordList.clear();
        scoreList.clear();

        // Player
        mAudioPath = AUDIO_DIR + "/record.mp3";
        mRangSeekBar.setOnRangeChangedListener(mOnRangeChangedListener);
        updatePlayInfo();
        initMusicFile();

        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mIsPlaying) {
                    mIsPlaying = false;
                    mMediaPlayer.pause();
                } else {
                    mIsPlaying = true;
                    mMediaPlayer.start();
                }
                updatePlayInfo();
            }
        });

        downBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 减速播放
                CustomToast.show(_mActivity, "Down");
            }
        });

        upBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 加速播放
                CustomToast.show(_mActivity, "Up");
            }
        });

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
        resultLineChart = rootView.findViewById(R.id.result_line_chart);
        mRangSeekBar = (RangeSeekBar) rootView.findViewById(R.id.audio_play_seekbar);
        playBtn = (ImageView) rootView.findViewById(R.id.play_btn);
        downBtn = (ImageView) rootView.findViewById(R.id.down_btn);
        upBtn = (ImageView) rootView.findViewById(R.id.up_btn);
        mProgressView = rootView.findViewById(R.id.loading_progress);
    }

    private void initResultLineChart(int xRange) {
        resultLineChart.setOnChartValueSelectedListener(this);

        resultLineChart.getDescription().setEnabled(false);
        resultLineChart.setDrawGridBackground(false);
        resultLineChart.setBackgroundColor(Color.TRANSPARENT);

        resultLineChart.setDragEnabled(true);
        resultLineChart.setScaleEnabled(true);
        resultLineChart.setPinchZoom(true);
        resultLineChart.setHighlightPerDragEnabled(true);
        resultLineChart.animateX(1000);

        resultLineChart.setTouchEnabled(true);
        resultLineChart.setDragDecelerationEnabled(true);
        resultLineChart.setDragDecelerationFrictionCoef(0.9f);

        Legend l = resultLineChart.getLegend();
        l.setEnabled(false);

        XAxis xAxis = resultLineChart.getXAxis();
        xAxis.setTextColor(Color.rgb(0, 0, 0));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(10f);
        xAxis.setDrawLabels(true);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularityEnabled(true);

        YAxis leftAxis = resultLineChart.getAxisLeft();
        leftAxis.setTextColor(Color.rgb(0, 0, 0));
        leftAxis.setTextSize(10f);
        leftAxis.setAxisMaximum(20f);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setDrawGridLines(false);
        leftAxis.setGranularityEnabled(true);

        YAxis rightAxis = resultLineChart.getAxisRight();
        rightAxis.setEnabled(false);

        setData(xRange);
    }

    private void setData(int xRange) {
        ArrayList<Entry> resultData = new ArrayList<>();
        ArrayList<Entry> baseData = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<>();

        for (int i = 0; i < resultDataList.size(); i++) {
            baseData.add(new Entry(i, 6, ""));
            resultData.add(new Entry(i, resultDataList.get(i).floatValue(), String.valueOf(i) + "F"));
            if (resultDataList.get(i) > 6) {
                if(i > 0 && resultDataList.get(i-1) <= 6) {
                    colors.set(i-1, Color.RED);
                }
                colors.add(Color.RED);
            }
            else {
                colors.add(Color.GREEN);
            }
        }

        ArrayList<Entry> resultSplitLine = new ArrayList<>();
        ArrayList<Entry> resultWordPosition = new ArrayList<>();

        for (int i = 0; i < resultSplitLineList.size(); i++) {
            if(i % 2 == 0) {
                resultSplitLine.add(new Entry(resultSplitLineList.get(i), 21f));
                resultSplitLine.add(new Entry(resultSplitLineList.get(i), -1f));
            }
            else {
                resultSplitLine.add(new Entry(resultSplitLineList.get(i), -1f));
                resultSplitLine.add(new Entry(resultSplitLineList.get(i), 21f));
            }
        }

        for (int i = 0; i < resultSplitLineList.size(); i++) {
            if(i > 0 && i % 2 == 1) {
                int position = (resultSplitLineList.get(i-1) + resultSplitLineList.get(i)) / 2;
                resultWordPosition.add(new Entry(position, 15f));
                resultWordPositionList.add(position);

                double actualScore = 0;
                double totalScore = 0;
                for (int j = resultSplitLineList.get(i-1); j < resultSplitLineList.get(i); j++) {
                    totalScore++;
                    if(resultDataList.get(j) <= 6) {
                        actualScore++;
                    }
                }
                scoreList.add(actualScore/totalScore);
            }
        }

        System.out.println("=========score============");
        double finalSumScore = 0;
        for (int i = 0; i < scoreList.size(); i++) {
            System.out.println(scoreList.get(i));
            finalSumScore = finalSumScore + scoreList.get(i);
        }
        finalAvgScore = (int) Math.round(finalSumScore / scoreList.size() * 100);

        LineDataSet resultDataSet, resultSplitLineSet, baseDataSet, resultWordPositionSet;
        resultDataSet = new LineDataSet(resultData, "Standard Line");
        resultDataSet.setAxisDependency(AxisDependency.LEFT);
        resultDataSet.setColors(colors);
        resultDataSet.setLineWidth(1f);
        resultDataSet.setDrawCircles(false);

        resultSplitLineSet = new LineDataSet(resultSplitLine, "Split Line");
        resultSplitLineSet.setColor(Color.rgb(0, 255, 255));
        resultSplitLineSet.setLineWidth(1f);
        resultSplitLineSet.setDrawCircles(false);
        resultSplitLineSet.setDrawValues(false);

        baseDataSet = new LineDataSet(baseData, "Base Line");
        baseDataSet.setColor(Color.rgb(255, 208, 0));
        baseDataSet.setLineWidth(1f);
        baseDataSet.setDrawCircles(false);
        baseDataSet.setDrawValues(false);

        resultWordPositionSet = new LineDataSet(resultWordPosition, "");
        resultWordPositionSet.setColor(Color.TRANSPARENT);
        resultWordPositionSet.setLineWidth(1f);
        resultWordPositionSet.setCircleColor(Color.BLUE);
        resultWordPositionSet.setCircleHoleColor(Color.WHITE);
        resultWordPositionSet.setCircleRadius(4f);

        LineData data = new LineData(resultDataSet, resultSplitLineSet, baseDataSet, resultWordPositionSet);
        resultLineChart.setData(data);

        for (int i = 0; i < syllablesArr.length; i++) {
            resultSplitWordList.add(syllablesArr[i] + ":" + String.format("%.2f", scoreList.get(i)*100) + "%");
        }
        CustomMarkerView mv = new CustomMarkerView(_mActivity, R.layout.marker_view, resultWordPositionList, resultSplitWordList, 15f);
        mv.setChartView(resultLineChart);
        resultLineChart.setMarker(mv);

        XAxis xAxis = resultLineChart.getXAxis();
        xAxis.setValueFormatter((value, axis) -> String.valueOf(resultData.get((int)value).getData()));

        resultLineChart.notifyDataSetChanged();
        resultLineChart.setVisibleXRangeMaximum(xRange);

        if(!AudioUploadResult.isUploadResult()) {
            uploadAudioResult();
            AudioUploadResult.setUploadResult(true);
        }
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

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

        Request request = new Request.Builder()
                .url(IPAddress.getIpAddress() + "/api/upload")
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
                System.out.println("onResponse: " + response);
                int statusCode = response.code();
                String result = response.body().string();
                _mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(statusCode == HttpURLConnection.HTTP_OK) {
                            try {
                                JSONObject jsonObject = new JSONObject(result);

                                JSONArray arrDiff = jsonObject.getJSONArray("diff");
                                for(int i = 0; i < arrDiff.length(); i++) {
                                    resultDataList.add(arrDiff.getDouble(i));
                                }
                                JSONArray resultSplitLine = jsonObject.getJSONArray("stepDiff");
                                for(int i = 0; i < resultSplitLine.length(); i++) {
                                    resultSplitLineList.add(resultSplitLine.getInt(i));
                                }

                                Comparator cmp = new SplitLineComparator();
                                Collections.sort(resultSplitLineList, cmp);

                                initResultLineChart(resultDataList.size());

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
        Get getCorrectDemoTask = new Get(ResultLineChart.this, CORRECT_DEMO_MODE_ID);
        getCorrectDemoTask.execute(IPAddress.getIpAddress() + "/api/correctDemo");
    }

    private void getWrongDemo(){
        Get getWrongDemoTask = new Get(ResultLineChart.this, WRONG_DEMO_MODE_ID);
        getWrongDemoTask.execute(IPAddress.getIpAddress() + "/api/wrongDemo");
    }

    private void uploadAudioResult(){
        String parameters = getParameters();
        System.out.println("parameters: " + parameters);
        PostAuth postUploadAudioResultTask = new PostAuth(ResultLineChart.this, UPLOAD_RESULT_ID);
        postUploadAudioResultTask.execute(IPAddress.getIpAddress() + "/api/finishProgress", parameters, token);
    }

    private String getParameters() {
        String parameters = "";
        try {
            parameters = "userId=" + URLEncoder.encode(String.valueOf(userId), "UTF-8") + "&englishId=" + URLEncoder.encode(String.valueOf(englishId), "UTF-8") + "&grade="  + URLEncoder.encode(String.valueOf(finalAvgScore), "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return parameters;
    }

    @Override
    public void onTaskCompleted(String response, int requestId) {
        if(response.equals("FORBIDDEN")) {
            TokenHelper.refreshToken(_mActivity, this::onTaskCompleted, REFRESH_TOKEN_TASK_ID);
        }
        else if(response.equals("UNAUTHORIZED")) {
            TokenHelper.refreshToken(_mActivity, this::onTaskCompleted, REFRESH_TOKEN_TASK_ID);
        }
        else {
            if (requestId == UPLOAD_RESULT_ID) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String Status = jsonObject.getString("status");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else if ((requestId == CORRECT_DEMO_MODE_ID) || (requestId == WRONG_DEMO_MODE_ID)) {
                try {
                    JSONObject jsonObject = new JSONObject(response);

                    JSONArray arrDiff = jsonObject.getJSONArray("diff");
                    for(int i = 0; i < arrDiff.length(); i++) {
                        resultDataList.add(arrDiff.getDouble(i));
                    }
                    JSONArray resultSplitLine = jsonObject.getJSONArray("stepDiff");
                    for(int i = 0; i < resultSplitLine.length(); i++) {
                        resultSplitLineList.add(resultSplitLine.getInt(i));
                    }

                    Comparator cmp = new SplitLineComparator();
                    Collections.sort(resultSplitLineList, cmp);

                    initResultLineChart(resultDataList.size());

                } catch (Exception e) {
                    e.printStackTrace();
                }
                showProgress(false);
            }
            else if(requestId == REFRESH_TOKEN_TASK_ID) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if(jsonObject.getString("status").equals("Succeeded")) {
                        String newToken = jsonObject.getString("token");
                        GetSetSharedPreferences.setDefaults("token", newToken, _mActivity.getApplicationContext());
                        token = GetSetSharedPreferences.getDefaults("token", _mActivity.getApplicationContext());
                        uploadAudio();
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

    private void initMusicFile() {
        File music = new File(mAudioPath);
        try {
            mMediaPlayer = new SlackAudioPlayer(_mActivity);
            mMediaPlayer.setDataSource(music.getAbsolutePath());
            mMediaPlayer.setOnMusicDurationListener(mMusicDurationListener);
            mMediaPlayer.setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(IMediaPlayer mp) {
                    _mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mIsPlaying = false;
                            updatePlayInfo();
                        }
                    });
                }
            });
            mMediaPlayer.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
                @Override
                public void onError(IMediaPlayer mp, @IMediaPlayer.AudioPlayError int what, String msg) {
                    Log.e("slack", "Error, what: " + what + " msg: " + msg);
                }
            });
            mMediaPlayer.prepareAsync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private IMediaPlayer.OnMusicDurationListener mMusicDurationListener = new IMediaPlayer.OnMusicDurationListener() {
        @Override
        public void onMusicDuration(IMediaPlayer mp, final float duration) {
            _mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mRangSeekBar.setRange(0, duration);
                }
            });
        }
    };

    private RangeSeekBar.OnRangeChangedListener mOnRangeChangedListener = new RangeSeekBar.OnRangeChangedListener() {
        @Override
        public void onRangeChanged(RangeSeekBar view, float min, float max, boolean isFromUser, boolean changeFinished) {
            if(isFromUser && changeFinished) {
                mMediaPlayer.updateRange(min, max);
            }
        }
    };

    private void updatePlayInfo() {
        if(mIsPlaying) {
            playBtn.setImageResource(R.drawable.ic_pause);
        } else {
            playBtn.setImageResource(R.drawable.ic_play);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mIsPlaying = false;
        mMediaPlayer.pause();
        updatePlayInfo();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mIsPlaying = false;
        mMediaPlayer.release();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mIsPlaying = false;
        mMediaPlayer.release();
    }
}
