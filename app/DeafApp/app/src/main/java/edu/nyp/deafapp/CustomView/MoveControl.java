package edu.nyp.deafapp.CustomView;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;

import edu.nyp.deafapp.HttpAsyncTask.Post;
import edu.nyp.deafapp.HttpAsyncTask.PostAuth;
import edu.nyp.deafapp.IPAddress;
import edu.nyp.deafapp.Interface.ControlOnTouch;
import edu.nyp.deafapp.Interface.OnTaskCompleted;
import edu.nyp.deafapp.LoginActivity;
import edu.nyp.deafapp.Model.Ipa;
import edu.nyp.deafapp.Model.ViewLocalRectangle;
import edu.nyp.deafapp.R;
import edu.nyp.deafapp.SharedPreferences.GetSetSharedPreferences;
import edu.nyp.deafapp.Token.TokenHelper;
import edu.nyp.deafapp.Utils.RSAUtils;
import edu.nyp.deafapp.Utils.ScreenUtils;
import pl.droidsonroids.gif.GifImageView;

import static edu.nyp.deafapp.Model.IpaList.ipaList;

/**
 * @author Administrator
 * @des ${TODO}
 * @verson $Rev$
 * @updateAuthor $Author$
 * @updateDes ${TODO}
 */

public class MoveControl implements ControlOnTouch, OnTaskCompleted {

    private static final int UPDATE_GAME_LEADERBOARD = 1;
    private static final int REFRESH_TOKEN_TASK_ID = 100;

    private float width, bigWidth;
    private boolean isViewCorrect[] = new boolean[] {false, false ,false};
    public boolean isGameStarted = false;
    public int score = 0;

    private Context context;
    private View view;
    private View mTopLeftCube;
    private View mTopMiddleCube;
    private View mTopRightCube;

    private View mBottomLeftCube;
    private View mBottomMiddleCube;
    private View mBottomRightCube;

    private RelativeLayout gifContainer;
    private GifImageView wellDone;
    private GifImageView tryAgain;

    private ArrayList<View> viewLeftLocalRectangles = new ArrayList<>();
    private ArrayList<View> viewMiddleLocalRectangles = new ArrayList<>();
    private ArrayList<View> viewRightLocalRectangles = new ArrayList<>();

    private CanMoveTextView canMoveTextViewLeft;
    private CanMoveTextView canMoveTextViewMiddle;
    private CanMoveTextView canMoveTextViewRight;

    private Button startBtn;
    private TextView scoreTxt;
    private TextView wordTxt;
    private TextView timerTxt;
    private Handler handler = new Handler();
    private CountDownTimer timer;

    private int userId;
    private String token = "";
    private String publicKey;

    public MoveControl(Context context, View view) {
        this.context = context;
        this.view = view;
        width = ScreenUtils.dp2px(context, 60);
        bigWidth = ScreenUtils.dp2px(context, 70);
        intView();

        publicKey = GetSetSharedPreferences.getDefaults("publicKey", context.getApplicationContext());
        userId = Integer.valueOf(GetSetSharedPreferences.getDefaults("userId", context.getApplicationContext()));
        token = GetSetSharedPreferences.getDefaults("token", context.getApplicationContext());
    }

    private void intView() {
        mTopLeftCube = view.findViewById(R.id.top_left_cube);
        mTopRightCube = view.findViewById(R.id.top_right_cube);
        mTopMiddleCube = view.findViewById(R.id.top_middle_cube);
        mBottomLeftCube = view.findViewById(R.id.bottom_left_cube);
        mBottomRightCube = view.findViewById(R.id.bottom_right_cube);
        mBottomMiddleCube = view.findViewById(R.id.bottom_middle_cube);
        canMoveTextViewLeft = view.findViewById(R.id.can_move_text_view_left);
        canMoveTextViewLeft.setControlOnTouch(this);
        canMoveTextViewMiddle = view.findViewById(R.id.can_move_text_view_middle);
        canMoveTextViewMiddle.setControlOnTouch(this);
        canMoveTextViewRight = view.findViewById(R.id.can_move_text_view_right);
        canMoveTextViewRight.setControlOnTouch(this);

        startBtn = (Button) view.findViewById(R.id.start_btn);
        scoreTxt = (TextView) view.findViewById(R.id.score_txt);
        wordTxt = (TextView) view.findViewById(R.id.word_txt);
        timerTxt = (TextView) view.findViewById(R.id.timer_txt);
        gifContainer = (RelativeLayout) view.findViewById(R.id.gif_container);
        wellDone = (GifImageView) view.findViewById(R.id.well_done);
        tryAgain = (GifImageView) view.findViewById(R.id.try_again);
    }

    public void showAllCubes() {
        canMoveTextViewLeft.setVisibility(View.VISIBLE);
        canMoveTextViewMiddle.setVisibility(View.VISIBLE);
        canMoveTextViewRight.setVisibility(View.VISIBLE);
    }

    public void hideAllCubes() {
        canMoveTextViewLeft.setVisibility(View.GONE);
        canMoveTextViewMiddle.setVisibility(View.GONE);
        canMoveTextViewRight.setVisibility(View.GONE);
    }

    public void setCubeLeft(String ipa) {
        canMoveTextViewLeft.setText(ipa);
    }

    public void setCubeMiddle(String ipa) {
        canMoveTextViewMiddle.setText(ipa);
    }

    public void setCubeRight(String ipa) {
        canMoveTextViewRight.setText(ipa);
    }

    public void initArray() {
        viewLeftLocalRectangles.clear();
        viewLeftLocalRectangles.add(mBottomLeftCube);
        viewLeftLocalRectangles.add(mBottomMiddleCube);
        viewLeftLocalRectangles.add(mBottomRightCube);

        viewMiddleLocalRectangles.clear();
        viewMiddleLocalRectangles.add(mBottomLeftCube);
        viewMiddleLocalRectangles.add(mBottomMiddleCube);
        viewMiddleLocalRectangles.add(mBottomRightCube);

        viewRightLocalRectangles.clear();
        viewRightLocalRectangles.add(mBottomLeftCube);
        viewRightLocalRectangles.add(mBottomMiddleCube);
        viewRightLocalRectangles.add(mBottomRightCube);

        //view加载完成时回调
        mTopLeftCube.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                moveViewWithAnim(canMoveTextViewLeft, mTopLeftCube);
                mTopLeftCube.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
        mTopMiddleCube.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                moveViewWithAnim(canMoveTextViewMiddle, mTopMiddleCube);
                mTopMiddleCube.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
        mTopRightCube.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                moveViewWithAnim(canMoveTextViewRight, mTopRightCube);
                mTopRightCube.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    public void startGame() {
        int randomNumber = (int) (Math.random() * 10);
        ipaList.clear();

        if (randomNumber == 0) {
            wordTxt.setText("apple");
            ipaList.add(new Ipa(1, "æ"));
            ipaList.add(new Ipa(2, "p"));
            ipaList.add(new Ipa(3, "l"));
            Collections.shuffle(ipaList);
        }
        else if (randomNumber == 1) {
            wordTxt.setText("fly");

            ipaList.add(new Ipa(1, "f"));
            ipaList.add(new Ipa(2, "l"));
            ipaList.add(new Ipa(3, "aɪ"));
            Collections.shuffle(ipaList);
        }
        else if (randomNumber == 2) {
            wordTxt.setText("cry");

            ipaList.add(new Ipa(1, "k"));
            ipaList.add(new Ipa(2, "r"));
            ipaList.add(new Ipa(3, "aɪ"));
            Collections.shuffle(ipaList);
        }
        else if (randomNumber == 3) {
            wordTxt.setText("sky");

            ipaList.add(new Ipa(1, "s"));
            ipaList.add(new Ipa(2, "k"));
            ipaList.add(new Ipa(3, "aɪ"));
            Collections.shuffle(ipaList);
        }
        else if (randomNumber == 4) {
            wordTxt.setText("sign");

            ipaList.add(new Ipa(1, "s"));
            ipaList.add(new Ipa(2, "aɪ"));
            ipaList.add(new Ipa(3, "n"));
            Collections.shuffle(ipaList);
        }
        else if (randomNumber == 5) {
            wordTxt.setText("away");

            ipaList.add(new Ipa(1, "ə"));
            ipaList.add(new Ipa(2, "w"));
            ipaList.add(new Ipa(3, "eɪ"));
            Collections.shuffle(ipaList);
        }
        else if (randomNumber == 6) {
            wordTxt.setText("okay");

            ipaList.add(new Ipa(1, "əu"));
            ipaList.add(new Ipa(2, "k"));
            ipaList.add(new Ipa(3, "eɪ"));
            Collections.shuffle(ipaList);
        }
        else if (randomNumber == 7) {
            wordTxt.setText("flow");

            ipaList.add(new Ipa(1, "f"));
            ipaList.add(new Ipa(2, "l"));
            ipaList.add(new Ipa(3, "əʊ"));
            Collections.shuffle(ipaList);
        }
        else if (randomNumber == 8) {
            wordTxt.setText("crow");

            ipaList.add(new Ipa(1, "k"));
            ipaList.add(new Ipa(2, "r"));
            ipaList.add(new Ipa(3, "əʊ"));
            Collections.shuffle(ipaList);
        }
        else {
            wordTxt.setText("allow");

            ipaList.add(new Ipa(1, "ə"));
            ipaList.add(new Ipa(2, "l"));
            ipaList.add(new Ipa(3, "aʊ"));
            Collections.shuffle(ipaList);
        }

        setCubeLeft(ipaList.get(0).getIpaContent());
        setCubeMiddle(ipaList.get(1).getIpaContent());
        setCubeRight(ipaList.get(2).getIpaContent());

        showAllCubes();
        initArray();

        startBtn.setText("STOP");
        isGameStarted = true;

        timerTxt.setVisibility(View.VISIBLE);
        timer = new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if(millisUntilFinished / 1000 - 1 > 10) {
                    timerTxt.setText("00:" + (millisUntilFinished / 1000));
                }
                else {
                    timerTxt.setText("00:0" + (millisUntilFinished / 1000));
                }
            }

            @Override
            public void onFinish() {
                timerTxt.setText("00:00");
                gifContainer.setVisibility(View.VISIBLE);
                tryAgain.setVisibility(View.VISIBLE);
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        tryAgain.setVisibility(View.GONE);
                        gifContainer.setVisibility(View.GONE);
                        stopGame();
                        handler.removeCallbacksAndMessages(null);
                    }
                };
                handler.postDelayed(runnable, 2000);
            }
        }.start();
    }

    public void stopGame() {
        if(timer != null) {
            timer.cancel();
            timer = null;
        }
        wordTxt.setText("Ready");
        timerTxt.setText("00:00");
        startBtn.setText("START");
        hideAllCubes();

        isGameStarted = false;
    }

    private void moveViewWithAnim(View currentView, View view) {
        float nextY = view.getY() + ((float) view.getHeight() - currentView.getHeight()) / 2;
        float nextX = view.getX() + ((float) view.getWidth() - currentView.getWidth()) / 2;
        ((CanMoveTextView) currentView).setCurrentInView(view);
        // 属性动画移动
        ObjectAnimator moveY = ObjectAnimator.ofFloat(currentView, "y", view.getY(), nextY);
        ObjectAnimator moveX = ObjectAnimator.ofFloat(currentView, "x", view.getX(), nextX);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(moveX, moveY);
        animatorSet.setDuration(300);
        animatorSet.start();
    }

    private void updateGameLeaderboard(){
        String parameters = getParameters();
        System.out.println("parameters: " + parameters);
        PostAuth updateGameLeaderboardTask = new PostAuth(this, UPDATE_GAME_LEADERBOARD);
        updateGameLeaderboardTask.execute(IPAddress.getIpAddress() + "/api/cubeHubScore", parameters, token);
    }

    private String getParameters() {
        String parameters = "";
        String userIdEncryption = "";
        String ScoreEncryption = "";
        try {
            userIdEncryption = RSAUtils.encryptByPublicKey(String.valueOf(userId), publicKey);
            ScoreEncryption = RSAUtils.encryptByPublicKey(String.valueOf(score), publicKey);
            String firstParameter = "userId=" + URLEncoder.encode(userIdEncryption, "UTF-8");
            String secondParameter = "score=" + URLEncoder.encode(ScoreEncryption, "UTF-8");
            parameters = firstParameter + "&" + secondParameter;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return parameters;
    }

    @Override
    public void touchUp(float lastX, float lastY, View currentView) {
        ViewLocalRectangle nowViewLocalRectangle = new ViewLocalRectangle(width, lastX, lastY);

        int position = -1;
        float maxArea = 0;
        View currentViewLeft = canMoveTextViewLeft.getCurrentInView(); // Left Cube所在框的位置
        View currentViewMiddle = canMoveTextViewMiddle.getCurrentInView(); // Middle Cube所在框的位置
        View currentViewRight = canMoveTextViewRight.getCurrentInView(); // Right Cube所在框的位置

        ArrayList<View> viewLocalRectangles; //选择对应的可移动的集合
        if(currentView.getId() == R.id.can_move_text_view_left) {
            viewLocalRectangles = viewLeftLocalRectangles;
        }
        else if(currentView.getId() == R.id.can_move_text_view_middle) {
            viewLocalRectangles = viewMiddleLocalRectangles;
        }
        else {
            viewLocalRectangles = viewRightLocalRectangles;
        }

        for (int i = 0; i < viewLocalRectangles.size(); i++) {
            ViewLocalRectangle viewLocalRectangle = new ViewLocalRectangle(bigWidth, viewLocalRectangles.get(i).getX(), viewLocalRectangles.get(i).getY());

            if (viewLocalRectangle.calculatedArea(nowViewLocalRectangle) > 0) {
                if ((position < 0) || maxArea < viewLocalRectangle.getArea()) {
                    position = i;
                    maxArea = viewLocalRectangle.getArea();
                }
            }
        }

        //在所有框框的范围内，并且A和B所在的框不能重复
        if (position > -1) {
            View nextView = viewLocalRectangles.get(position);
            if(nextView == currentViewLeft) {
                moveViewWithAnim(canMoveTextViewLeft, mTopLeftCube);
            }
            else if(nextView == currentViewMiddle) {
                moveViewWithAnim(canMoveTextViewMiddle, mTopMiddleCube);
            }
            else if(nextView == currentViewRight) {
                moveViewWithAnim(canMoveTextViewRight, mTopRightCube);
            }

            moveViewWithAnim(currentView, nextView);

            if(currentView.getId() == R.id.can_move_text_view_left) {
                if (ipaList.get(0).getIpaId() == (position + 1)) {
                    isViewCorrect[position] = true;
                }
                else {
                    isViewCorrect[position] = false;
                }
            }
            else if(currentView.getId() == R.id.can_move_text_view_middle) {
                if (ipaList.get(1).getIpaId() == (position + 1)) {
                    isViewCorrect[position] = true;
                }
                else {
                    isViewCorrect[position] = false;
                }
            }
            else {
                if (ipaList.get(2).getIpaId() == (position + 1)) {
                    isViewCorrect[position] = true;
                }
                else {
                    isViewCorrect[position] = false;
                }
            }
        }
        else {
            if(currentView.getId() == R.id.can_move_text_view_left) {
                moveViewWithAnim(currentView, mTopLeftCube);
                if (currentViewLeft == mBottomLeftCube) {
                    isViewCorrect[0] = false;
                }
                else if (currentViewLeft == mBottomMiddleCube) {
                    isViewCorrect[1] = false;
                }
                else if (currentViewLeft == mBottomRightCube) {
                    isViewCorrect[2] = false;
                }
            }
            else if(currentView.getId() == R.id.can_move_text_view_middle) {
                moveViewWithAnim(currentView, mTopMiddleCube);
            }
            else {
                moveViewWithAnim(currentView, mTopRightCube);
            }
        }

        if(((canMoveTextViewLeft.getCurrentInView() == mBottomLeftCube) || (canMoveTextViewLeft.getCurrentInView() == mBottomMiddleCube) || (canMoveTextViewLeft.getCurrentInView() == mBottomRightCube)) &&
                ((canMoveTextViewMiddle.getCurrentInView() == mBottomLeftCube) || (canMoveTextViewMiddle.getCurrentInView() == mBottomMiddleCube) || (canMoveTextViewMiddle.getCurrentInView() == mBottomRightCube)) &&
                ((canMoveTextViewRight.getCurrentInView() == mBottomLeftCube) || (canMoveTextViewRight.getCurrentInView() == mBottomMiddleCube) || (canMoveTextViewRight.getCurrentInView() == mBottomRightCube))) {

            boolean hasWrong = false;
            for (boolean x:isViewCorrect) {
                if(!x) {
                    hasWrong = true;
                    break;
                }
            }

            timer.cancel();
            timer = null;
            if(hasWrong) {
                updateGameLeaderboard();
                gifContainer.setVisibility(View.VISIBLE);
                tryAgain.setVisibility(View.VISIBLE);
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        tryAgain.setVisibility(View.GONE);
                        gifContainer.setVisibility(View.GONE);
                        score = 0;
                        scoreTxt.setText(String.valueOf(score));
                        stopGame();
                        handler.removeCallbacksAndMessages(null);
                    }
                };
                handler.postDelayed(runnable, 2000);
            }
            else {
                gifContainer.setVisibility(View.VISIBLE);
                wellDone.setVisibility(View.VISIBLE);
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        wellDone.setVisibility(View.GONE);
                        gifContainer.setVisibility(View.GONE);
                        score++;
                        scoreTxt.setText(String.valueOf(score));
                        startGame();
                        handler.removeCallbacksAndMessages(null);
                    }
                };
                handler.postDelayed(runnable, 2000);
            }

            for (int i = 0; i < isViewCorrect.length; i++) {
                isViewCorrect[i] = false;
            }
        }
    }

    @Override
    public void onTaskCompleted(String response, int requestId) {
        if(response.equals("FORBIDDEN")) {
            TokenHelper.refreshToken(context, this::onTaskCompleted, REFRESH_TOKEN_TASK_ID);
        }
        else if(response.equals("UNAUTHORIZED")) {
            TokenHelper.refreshToken(context, this::onTaskCompleted, REFRESH_TOKEN_TASK_ID);
        }
        else {
            if(requestId == UPDATE_GAME_LEADERBOARD) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String Status = jsonObject.getString("status");

                    if(Status.equals("Succeeded")) {
                        CustomToast.show(context, "Score uploaded!");
                    }
                    else {
                        CustomToast.show(context, "Server error");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    CustomToast.show(context, "Server error");
                }
            }
            else if(requestId == REFRESH_TOKEN_TASK_ID) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String Status = jsonObject.getString("status");

                    if(Status.equals("Succeeded")) {
                        String newToken = jsonObject.getString("token");
                        GetSetSharedPreferences.setDefaults("token", newToken, context.getApplicationContext());
                        token = GetSetSharedPreferences.getDefaults("token", context.getApplicationContext());
                        updateGameLeaderboard();
                    } else {
                        Intent intent = new Intent(context, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    }
                } catch (Exception e) {
                    Intent intent = new Intent(context, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            }
        }
    }
}
