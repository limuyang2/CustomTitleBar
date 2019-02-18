package top.limuyang2.customtitlebar.widget;

import android.support.annotation.NonNull;
import android.view.View;
import top.limuyang2.customtitlebar.R;
import top.limuyang2.customtitlebar.utils.UIResHelper;


class UIAlphaViewHelper {

    private View mTarget;

    /**
     * 设置是否要在 press 时改变透明度
     */
    private boolean mChangeAlphaWhenPress = true;

    /**
     * 设置是否要在 disabled 时改变透明度
     */
    private boolean mChangeAlphaWhenDisable = true;

    private float mNormalAlpha = 1f;
    private float mPressedAlpha = .5f;
    private float mDisabledAlpha = .5f;

    UIAlphaViewHelper(@NonNull View target) {
        mTarget = target;
        float getPressedAlpha = UIResHelper.getAttrFloatValue(target.getContext(), R.attr.alpha_pressed);
        System.out.println("------> getPressedAlpha : " + getPressedAlpha);
        mPressedAlpha = getPressedAlpha == 0f ? .5f : getPressedAlpha;

        float getDisabledAlpha = UIResHelper.getAttrFloatValue(target.getContext(), R.attr.alpha_disabled);
        mDisabledAlpha = getDisabledAlpha == 0f ? .5f : getDisabledAlpha;
    }

    void onPressedChanged(View target, boolean pressed) {
        if (mTarget.isEnabled()) {
            mTarget.setAlpha(mChangeAlphaWhenPress && pressed && target.isClickable()? mPressedAlpha : mNormalAlpha);
        } else {
            if (mChangeAlphaWhenDisable) {
                target.setAlpha(mDisabledAlpha);
            }
        }
    }

    void onEnabledChanged(View target, boolean enabled) {
        float alphaForIsEnable;
        if (mChangeAlphaWhenDisable) {
            alphaForIsEnable = enabled ? mNormalAlpha : mDisabledAlpha;
        } else {
            alphaForIsEnable = mNormalAlpha;
        }
        target.setAlpha(alphaForIsEnable);
    }

    /**
     * 设置是否要在 press 时改变透明度
     *
     * @param changeAlphaWhenPress 是否要在 press 时改变透明度
     */
    void setChangeAlphaWhenPress(boolean changeAlphaWhenPress) {
        mChangeAlphaWhenPress = changeAlphaWhenPress;
    }

    /**
     * 设置是否要在 disabled 时改变透明度
     *
     * @param changeAlphaWhenDisable 是否要在 disabled 时改变透明度
     */
    void setChangeAlphaWhenDisable(boolean changeAlphaWhenDisable) {
        mChangeAlphaWhenDisable = changeAlphaWhenDisable;
        onEnabledChanged(mTarget, mTarget.isEnabled());
    }

}
