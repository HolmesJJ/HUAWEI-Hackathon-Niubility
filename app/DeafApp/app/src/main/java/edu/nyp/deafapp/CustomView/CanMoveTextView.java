package edu.nyp.deafapp.CustomView;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import edu.nyp.deafapp.Interface.ControlOnTouch;

/**
 * @author Administrator
 * @des ${TODO}
 * @verson $Rev$
 * @updateAuthor $Author$
 * @updateDes ${TODO}
 */

public class CanMoveTextView extends AppCompatTextView {

    private float lastX, lastY;

    private ControlOnTouch mControlOnTouch;
    private View mView;
    private View currentInView;

    public CanMoveTextView(Context context) {
        super(context);
        mView = this;
    }

    public CanMoveTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mView = this;
    }

    public CanMoveTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mView = this;
    }

    public void setControlOnTouch(ControlOnTouch mControlOnTouch) {
        this.mControlOnTouch = mControlOnTouch;
    }

    public View getCurrentInView() {
        return currentInView;
    }

    public void setCurrentInView(View currentInView) {
        this.currentInView = currentInView;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                lastX = event.getRawX();
                lastY = event.getRawY();
                return true;
            case MotionEvent.ACTION_UP:
                if (mControlOnTouch != null) {
                    mControlOnTouch.touchUp(getX(), getY(), mView);
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                //  不要直接用getX和getY,这两个获取的数据已经是经过处理的,容易出现图片抖动的情况

                float distanceX = lastX - event.getRawX();
                float distanceY = lastY - event.getRawY();

                float nextY = getY() - distanceY;
                float nextX = getX() - distanceX;

                // 不能移出屏幕
                if (nextY < 0) {
                    nextY = 0;
                } else if (nextY > (((ViewGroup) getParent()).getHeight() - getHeight())) {
                    nextY = (((ViewGroup) getParent()).getHeight() - getHeight());
                }
                if (nextX < 0) {
                    nextX = 0;
                }
                else if (nextX > (((ViewGroup) getParent()).getWidth() - getWidth())) {
                    nextX = (((ViewGroup) getParent()).getWidth() - getWidth());
                }

                // 属性动画移动
                ObjectAnimator y = ObjectAnimator.ofFloat(this, "y", getY(), nextY);
                ObjectAnimator x = ObjectAnimator.ofFloat(this, "x", getX(), nextX);

                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.playTogether(x, y);
                animatorSet.setDuration(0);
                animatorSet.start();

                lastX = event.getRawX();
                lastY = event.getRawY();
        }
        return false;
    }
}
