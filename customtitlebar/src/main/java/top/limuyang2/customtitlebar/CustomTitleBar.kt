package top.limuyang2.customtitlebar

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.annotation.DrawableRes
import android.support.annotation.IntRange
import android.support.annotation.StringRes
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.*
import top.limuyang2.customtitlebar.utils.ScreenHelper
import top.limuyang2.customtitlebar.utils.UIDrawableHelper
import top.limuyang2.customtitlebar.utils.UIResHelper
import top.limuyang2.customtitlebar.utils.UIViewHelper
import top.limuyang2.customtitlebar.widget.UIAlphaImageButton
import java.util.*

@Suppress("unused", "MemberVisibilityCanBePrivate")
class CustomTitleBar @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = R.attr.CustomTitleBarStyle
) : RelativeLayout(context, attrs, defStyleAttr) {
    private var mLeftLastViewId: Int = DEFAULT_VIEW_ID // 左侧最右 view 的 id
    private var mRightLastViewId: Int = DEFAULT_VIEW_ID // 右侧最左 view 的 id

    // 中间的 View
    private var mCenterView: View? = null
    // 包裹 title 和 subTitle 的容器
    private val mTitleContainerView: LinearLayout by lazy {
        val ll = LinearLayout(context)
        // 垂直，后面要支持水平的话可以加个接口来设置
        ll.orientation = LinearLayout.VERTICAL
        ll.gravity = Gravity.CENTER
        ll.tag = TITLE_CONTAINER_VIEW_TAG
        ll.setPadding(mTitleContainerPaddingHor, 0, mTitleContainerPaddingHor, 0)
        addView(ll, generateTitleContainerViewLp())
        return@lazy ll
    }
    // 显示 title 文字的 TextView
    private val mTitleView: TextView by lazy {
        val titleView = TextView(context)
        titleView.gravity = Gravity.CENTER
        titleView.setSingleLine(true)
        titleView.ellipsize = TextUtils.TruncateAt.MIDDLE
        titleView.setTextColor(mTitleTextColor)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            titleView.letterSpacing = 0.12f
        }
        titleView.tag = TITLE_VIEW_TAG

        return@lazy titleView
    }
    // 显示 subTitle 文字的 TextView
    private val mSubTitleView: TextView by lazy {
        val titleView = TextView(context)
        titleView.gravity = Gravity.CENTER
        titleView.setSingleLine(true)
        titleView.ellipsize = TextUtils.TruncateAt.MIDDLE
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mSubTitleTextSize.toFloat())
        titleView.setTextColor(mSubTitleTextColor)
        titleView.tag = SUBTITLE_VIEW_TAG

        return@lazy titleView
    }

    private var mLeftViewList: MutableList<View> = ArrayList()
    private var mRightViewList: MutableList<View> = ArrayList()

    private var mTitleBarDividerColor: Int = 0
    private var mTitleBarBgColor: Int = 0
    private var mTitleBarDividerHeight: Int = 0

    private var mTitleBarBgWithDividerDrawableCache: Drawable? = null

    private var mTitleGravity: Int = Gravity.START
    private var mLeftBackDrawableRes: Int = 0
    private var topBarHeight = -1
    private var mTitleTextSize: Int = 0
    private var mTitleTextSizeWithSubTitle: Int = 0
    private var mSubTitleTextSize: Int = 0
    private var mTitleTextColor: Int = 0
    private var mSubTitleTextColor: Int = 0
    private var mTitleMarginHorWhenNoBtnAside: Int = 0
    private var mTitleContainerPaddingHor: Int = 0
    private var mTopBarImageBtnWidth: Int = 0
    private var mTopBarImageBtnHeight: Int = 0
    private var mTopBarTextBtnPaddingHor: Int = 0
    private var mTopBarTextBtnTextColor: ColorStateList? = null
    private var mTopBarTextBtnTextSize: Int = 0
    private var mTitleBarImageBtnWidth = -1
    private var mTitleBarImageBtnHeight = -1
    private var mTitleBarTextBtnPaddingHorizontal = -1
    private val mTitleContainerRect: Rect by lazy { Rect() }

    val titleContainerRect: Rect
        get() {
            if (!isTitleContainerViewAdd()) {
                mTitleContainerRect.set(0, 0, 0, 0)
            } else {
                UIViewHelper.getDescendantRect(this, mTitleContainerView, mTitleContainerRect)
            }
            return mTitleContainerRect
        }

    private val topBarImageBtnWidth: Int
        get() {
            if (mTitleBarImageBtnWidth == -1) {
                mTitleBarImageBtnWidth = mTopBarImageBtnWidth
            }
            return mTitleBarImageBtnWidth
        }

    private val topBarImageBtnHeight: Int
        get() {
            if (mTitleBarImageBtnHeight == -1) {
                mTitleBarImageBtnHeight = mTopBarImageBtnHeight
            }
            return mTitleBarImageBtnHeight
        }

    private val topBarTextBtnPaddingHorizontal: Int
        get() {
            if (mTitleBarTextBtnPaddingHorizontal == -1) {
                mTitleBarTextBtnPaddingHorizontal = mTopBarTextBtnPaddingHor
            }
            return mTitleBarTextBtnPaddingHorizontal
        }

    /**
     * TopBar 的标题
     */
    var title: CharSequence?
        get() = mTitleView.text
        set(value) {
            if (!isTitleViewAdd()) {
                updateTitleViewStyle()
                val titleLp = generateTitleViewAndSubTitleViewLp()
                mTitleContainerView.addView(mTitleView, titleLp)
            }
            mTitleView.text = value
            if (value.isNullOrEmpty()) {
                mTitleView.visibility = View.GONE
            } else {
                mTitleView.visibility = View.VISIBLE
            }
        }

    /**
     * TopBar 的副标题
     */
    var subTitle: CharSequence?
        get() = mSubTitleView.text
        set(value) {
            if (!isSubTitleViewAdd()) {
                val titleLp = generateTitleViewAndSubTitleViewLp()
                titleLp.topMargin = ScreenHelper.dp2px(context, 1)
                mTitleContainerView.addView(mSubTitleView, titleLp)
            }
            mSubTitleView.text = value
            if (value.isNullOrEmpty()) {
                mSubTitleView.visibility = View.GONE
            } else {
                mSubTitleView.visibility = View.VISIBLE
            }
            // 更新 titleView 的样式（因为有没有 subTitle 会影响 titleView 的样式）
            updateTitleViewStyle()
        }

    var titleRes: Int = 0
        set(@StringRes value) {
            title = context.getString(value)
            field = value
        }

    var subTitleRes: Int = 0
        set(@StringRes value) {
            subTitle = context.getString(value)
            field = value
        }

    var showTitleView: Boolean
        get() {
            return mTitleView.visibility == View.VISIBLE
        }
        set(value) {
            mTitleView.visibility = if (value) View.VISIBLE else View.GONE
        }

    /**
     * TopBar 的 gravity，用于控制 title 和 subtitle 的对齐方式
     */
    var titleGravity: Int
        get() = mTitleGravity
        set(value) {
            mTitleGravity = value
            if (isTitleViewAdd()) {
                (mTitleView.layoutParams as LinearLayout.LayoutParams).gravity = value
                if (value == Gravity.CENTER || value == Gravity.CENTER_HORIZONTAL) {
                    mTitleView.setPadding(paddingLeft, paddingTop, paddingLeft, paddingBottom)
                }
            }
            if (isSubTitleViewAdd()) {
                (mSubTitleView.layoutParams as LinearLayout.LayoutParams).gravity = value
            }
            requestLayout()
        }

    init {
        initVar()
        init(context, attrs, defStyleAttr)
    }

    private fun initVar() {
        mLeftLastViewId = DEFAULT_VIEW_ID
        mRightLastViewId = DEFAULT_VIEW_ID
    }

    private fun init(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        val array = getContext().obtainStyledAttributes(attrs, R.styleable.CustomTitleBar, defStyleAttr, 0)
        if (array != null) {
            mTitleBarDividerColor = array.getColor(
                    R.styleable.CustomTitleBar_titlebar_divider_color,
                    ContextCompat.getColor(context, R.color.config_color_divider)
            )
            mTitleBarDividerHeight = array.getDimensionPixelSize(R.styleable.CustomTitleBar_titlebar_divider_height, 1)
            mTitleBarBgColor = array.getColor(R.styleable.CustomTitleBar_titlebar_bg_color, Color.WHITE)
            val showDivider = array.getBoolean(R.styleable.CustomTitleBar_titlebar_show_divider, true)
            getCommonFieldFormTypedArray(context, array)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val elevationValue = array.getDimension(R.styleable.CustomTitleBar_titlebar_elevation, 0f)
                if (elevationValue != 0f) {
                    elevation = elevationValue
                }
            }

            array.recycle()
            setBackgroundDividerEnabled(showDivider)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        var parent = parent
        while (parent is View) {
            parent = parent.getParent()
        }
    }

    private fun getCommonFieldFormTypedArray(context: Context, array: TypedArray) {
        topBarHeight =
                array.getDimensionPixelOffset(R.styleable.CustomTitleBar_titlebar_height, ScreenHelper.dp2px(context, 56))

        mLeftBackDrawableRes = array.getResourceId(
                R.styleable.CustomTitleBar_titlebar_left_back_drawable_id,
                R.drawable.icon_titlebar_back
        )
        mTitleGravity = array.getInt(R.styleable.CustomTitleBar_titlebar_title_gravity, mTitleGravity)
        mTitleTextSize = array.getDimensionPixelSize(
                R.styleable.CustomTitleBar_titlebar_title_text_size,
                ScreenHelper.sp2px(context, 17)
        )
        mTitleTextSizeWithSubTitle = array.getDimensionPixelSize(
                R.styleable.CustomTitleBar_titlebar_title_text_size,
                ScreenHelper.sp2px(context, 16)
        )
        mSubTitleTextSize = array.getDimensionPixelSize(
                R.styleable.CustomTitleBar_titlebar_subtitle_text_size,
                ScreenHelper.sp2px(context, 11)
        )
        mTitleTextColor = array.getColor(
                R.styleable.CustomTitleBar_titlebar_title_color,
                UIResHelper.getAttrColor(context, R.attr.config_color_gray_1)
        )
        mSubTitleTextColor = array.getColor(
                R.styleable.CustomTitleBar_titlebar_subtitle_color,
                UIResHelper.getAttrColor(context, R.attr.config_color_gray_4)
        )
        mTitleMarginHorWhenNoBtnAside = array.getDimensionPixelSize(
                R.styleable.CustomTitleBar_titlebar_title_margin_horizontal_when_no_btn_aside,
                0
        )
        mTitleContainerPaddingHor =
                array.getDimensionPixelSize(R.styleable.CustomTitleBar_titlebar_title_container_padding_horizontal, 0)
        mTopBarImageBtnWidth = array.getDimensionPixelSize(
                R.styleable.CustomTitleBar_titlebar_image_btn_width,
                ScreenHelper.dp2px(context, 48)
        )
        mTopBarImageBtnHeight = array.getDimensionPixelSize(
                R.styleable.CustomTitleBar_titlebar_image_btn_height,
                ScreenHelper.dp2px(context, 48)
        )
        mTopBarTextBtnPaddingHor = array.getDimensionPixelSize(
                R.styleable.CustomTitleBar_titlebar_text_btn_padding_horizontal,
                ScreenHelper.dp2px(context, 12)
        )
        mTopBarTextBtnTextColor = array.getColorStateList(R.styleable.CustomTitleBar_titlebar_text_btn_color_state_list)
        mTopBarTextBtnTextSize = array.getDimensionPixelSize(
                R.styleable.CustomTitleBar_titlebar_text_btn_text_size,
                ScreenHelper.sp2px(context, 16)
        )
    }

    /**
     * 设置是否要 TitleBar 底部的分割线
     */
    fun setBackgroundDividerEnabled(enabled: Boolean) {
        if (enabled) {
            if (mTitleBarBgWithDividerDrawableCache == null) {
                mTitleBarBgWithDividerDrawableCache = UIDrawableHelper.createItemSeparatorBg(
                        mTitleBarDividerColor,
                        mTitleBarBgColor,
                        mTitleBarDividerHeight,
                        false
                )
            }
            UIViewHelper.setBackgroundKeepingPadding(this, mTitleBarBgWithDividerDrawableCache)
        } else {
            UIViewHelper.setBackgroundColorKeepPadding(this, mTitleBarBgColor)
        }
    }

    /**
     * 在 TopBar 的中间添加 View，如果此前已经有 View 通过该方法添加到 TopBar，则旧的View会被 remove
     *
     * @param view 要添加到TopBar中间的View
     */
    fun setCenterView(view: View) {
        if (mCenterView == view) {
            return
        }
        if (mCenterView != null) {
            removeView(mCenterView)
        }
        mCenterView = view
        var params: RelativeLayout.LayoutParams? = mCenterView!!.layoutParams as? RelativeLayout.LayoutParams
        if (params == null) {
            params = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            )
        }
        params.addRule(RelativeLayout.CENTER_IN_PARENT)
        addView(view, params)
    }

    /**
     * 更新 titleView 的样式（因为有没有 subTitle 会影响 titleView 的样式）
     */
    private fun updateTitleViewStyle() {
        if (!isSubTitleViewAdd() || mSubTitleView.text.isNullOrEmpty()) {
            mTitleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTitleTextSize.toFloat())
        } else {
            mTitleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTitleTextSizeWithSubTitle.toFloat())
        }
    }


    private fun isTitleViewAdd(): Boolean {
        return findViewWithTag<TextView>(TITLE_VIEW_TAG) != null
    }

    private fun isSubTitleViewAdd(): Boolean {
        return findViewWithTag<TextView>(SUBTITLE_VIEW_TAG) != null
    }

    private fun isTitleContainerViewAdd(): Boolean {
        return findViewWithTag<TextView>(TITLE_CONTAINER_VIEW_TAG) != null
    }

    // ========================= leftView、rightView 相关的方法

    /**
     * 生成 TitleContainerView 的 LayoutParams。
     * 左右有按钮时，该 View 在左右按钮之间；
     * 没有左右按钮时，该 View 距离 TopBar 左右边缘有固定的距离
     */
    private fun generateTitleContainerViewLp(): RelativeLayout.LayoutParams {
        return RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, topBarHeight)
    }

    /**
     * 生成 titleView 或 subTitleView 的 LayoutParams
     */
    private fun generateTitleViewAndSubTitleViewLp(): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            // 对齐方式
            gravity = mTitleGravity
        }
    }

    /**
     * 在TopBar的左侧添加View，如果此前已经有View通过该方法添加到TopBar，则新添加进去的View会出现在已有View的右侧
     *
     * @param view   要添加到 TopBar 左边的 View
     * @param viewId 该按钮的id，可在ids.xml中找到合适的或新增。手工指定viewId是为了适应自动化测试。
     */
    fun addLeftView(view: View, viewId: Int) {
        val viewLayoutParams = view.layoutParams
        val layoutParams: RelativeLayout.LayoutParams
        layoutParams = if (viewLayoutParams is RelativeLayout.LayoutParams) {
            viewLayoutParams
        } else {
            RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            )
        }
        this.addLeftView(view, viewId, layoutParams)
    }

    /**
     * 在TopBar的左侧添加View，如果此前已经有View通过该方法添加到TopBar，则新添加进去的View会出现在已有View的右侧。
     *
     * @param view         要添加到 TopBar 左边的 View。
     * @param viewId       该按钮的 id，可在 ids.xml 中找到合适的或新增。手工指定 viewId 是为了适应自动化测试。
     * @param layoutParams 传入一个 LayoutParams，当把 Button addView 到 TopBar 时，使用这个 LayoutParams。
     */
    fun addLeftView(view: View, viewId: Int, layoutParams: RelativeLayout.LayoutParams) {
        if (mLeftLastViewId == DEFAULT_VIEW_ID) {
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
        } else {
            layoutParams.addRule(RelativeLayout.RIGHT_OF, mLeftLastViewId)
        }
        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL)
        layoutParams.alignWithParent = true // alignParentIfMissing
        mLeftLastViewId = viewId
        view.id = viewId
        mLeftViewList.add(view)
        addView(view, layoutParams)
    }

    /**
     * 在 TopBar 的右侧添加 View，如果此前已经有 iew 通过该方法添加到 TopBar，则新添加进去的View会出现在已有View的左侧
     *
     * @param view   要添加到 TopBar 右边的View
     * @param viewId 该按钮的id，可在 ids.xml 中找到合适的或新增。手工指定 viewId 是为了适应自动化测试。
     */
    fun addRightView(view: View, viewId: Int) {
        val viewLayoutParams = view.layoutParams
        val layoutParams: RelativeLayout.LayoutParams = if (viewLayoutParams is RelativeLayout.LayoutParams) {
            viewLayoutParams
        } else {
            RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            )
        }
        this.addRightView(view, viewId, layoutParams)
    }

    /**
     * 在 TopBar 的右侧添加 View，如果此前已经有 View 通过该方法添加到 TopBar，则新添加进去的 View 会出现在已有View的左侧。
     *
     * @param view         要添加到 TopBar 右边的 View。
     * @param viewId       该按钮的 id，可在 ids.xml 中找到合适的或新增。手工指定 viewId 是为了适应自动化测试。
     * @param layoutParams 生成一个 LayoutParams，当把 Button addView 到 TopBar 时，使用这个 LayoutParams。
     */
    fun addRightView(view: View, viewId: Int, layoutParams: RelativeLayout.LayoutParams) {
        if (mRightLastViewId == DEFAULT_VIEW_ID) {
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
        } else {
            layoutParams.addRule(RelativeLayout.LEFT_OF, mRightLastViewId)
        }
        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL)
        layoutParams.alignWithParent = true // alignParentIfMissing
        mRightLastViewId = viewId
        view.id = viewId
        mRightViewList.add(view)
        addView(view, layoutParams)
    }

    /**
     * 生成一个 LayoutParams，当把 Button addView 到 TopBar 时，使用这个 LayoutParams
     */
    fun generateTopBarImageButtonLayoutParams(): RelativeLayout.LayoutParams {
//        lp.topMargin = Math.max(0, (getTopBarHeight() - getTopBarImageBtnHeight()) / 2);
        return LayoutParams(topBarImageBtnWidth, topBarImageBtnHeight)
    }

    /**
     * 根据 resourceId 生成一个 TopBar 的按钮，并 add 到 TopBar 的右侧
     *
     * @param drawableResId 按钮图片的 resourceId
     * @param viewId        该按钮的 id，可在 ids.xml 中找到合适的或新增。手工指定 viewId 是为了适应自动化测试。
     * @return 返回生成的按钮
     */
    fun addRightImageButton(@DrawableRes drawableResId: Int, viewId: Int): UIAlphaImageButton {
        val rightButton = generateTopBarImageButton(drawableResId)
        rightButton.setChangeAlphaWhenPress(true)
        this.addRightView(rightButton, viewId, generateTopBarImageButtonLayoutParams())
        return rightButton
    }

    fun addRightImageButton(@DrawableRes drawableResId: Int): UIAlphaImageButton {
        val rightButton = generateTopBarImageButton(drawableResId)
        rightButton.setChangeAlphaWhenPress(true)
        this.addRightView(rightButton, rightButton.hashCode(), generateTopBarImageButtonLayoutParams())
        return rightButton
    }

    /**
     * 根据 resourceId 生成一个 TopBar 的按钮，并 add 到 TopBar 的左边
     *
     * @param drawableResId 按钮图片的 resourceId
     * @param viewId        该按钮的 id，可在ids.xml中找到合适的或新增。手工指定 viewId 是为了适应自动化测试。
     * @return 返回生成的按钮
     */
    fun addLeftImageButton(@DrawableRes drawableResId: Int, viewId: Int): UIAlphaImageButton {
        val leftButton = generateTopBarImageButton(drawableResId)
        this.addLeftView(leftButton, viewId, generateTopBarImageButtonLayoutParams())
        return leftButton
    }

    fun addLeftImageButton(@DrawableRes drawableResId: Int): UIAlphaImageButton {
        val leftButton = generateTopBarImageButton(drawableResId)
        this.addLeftView(leftButton, leftButton.hashCode(), generateTopBarImageButtonLayoutParams())
        return leftButton
    }

    /**
     * 生成一个LayoutParams，当把 Button addView 到 TopBar 时，使用这个 LayoutParams
     */
    fun generateTopBarTextButtonLayoutParams(): RelativeLayout.LayoutParams {
//        lp.topMargin = Math.max(0, (getTopBarHeight() - getTopBarImageBtnHeight()) / 2);
        return LayoutParams(LayoutParams.WRAP_CONTENT, topBarImageBtnHeight)
    }

    /**
     * 在 TopBar 左边添加一个 Button，并设置文字
     *
     * @param stringResId 按钮的文字的 resourceId
     * @param viewId      该按钮的id，可在 ids.xml 中找到合适的或新增。手工指定 viewId 是为了适应自动化测试。
     * @return 返回生成的按钮
     */
    fun addLeftTextButton(@StringRes stringResId: Int, viewId: Int): Button {
        return addLeftTextButton(resources.getString(stringResId), viewId)
    }

    /**
     * 在 TopBar 左边添加一个 Button，并设置文字
     *
     * @param stringResId 按钮的文字的 resourceId
     * @param viewId      该按钮的id，可在 ids.xml 中找到合适的或新增。手工指定 viewId 是为了适应自动化测试。
     * @return 返回生成的按钮
     */
    fun addLeftTextButton(@StringRes stringResId: Int, viewId: Int, textColor: ColorStateList, textSizePX: Float): Button {
        return addLeftTextButton(resources.getString(stringResId), viewId, textColor, textSizePX)
    }

    /**
     * 在 TopBar 左边添加一个 Button，并设置文字
     *
     * @param buttonText 按钮的文字
     * @param viewId     该按钮的 id，可在 ids.xml 中找到合适的或新增。手工指定 viewId 是为了适应自动化测试。
     * @return 返回生成的按钮
     */
    fun addLeftTextButton(buttonText: String, viewId: Int, textColor: ColorStateList, textSizePX: Float): Button {
        val button = generateTopBarTextButton(buttonText, textColor, textSizePX)
        this.addLeftView(button, viewId, generateTopBarTextButtonLayoutParams())
        return button
    }

    /**
     * 在 TopBar 左边添加一个 Button，并设置文字
     *
     * @param buttonText 按钮的文字
     * @param viewId     该按钮的 id，可在 ids.xml 中找到合适的或新增。手工指定 viewId 是为了适应自动化测试。
     * @return 返回生成的按钮
     */
    fun addLeftTextButton(buttonText: String, viewId: Int): Button {
        val button = generateTopBarTextButton(buttonText)
        this.addLeftView(button, viewId, generateTopBarTextButtonLayoutParams())
        return button
    }

    fun addLeftTextButton(buttonText: String): Button {
        val button = generateTopBarTextButton(buttonText)
        this.addLeftView(button, button.hashCode(), generateTopBarTextButtonLayoutParams())
        return button
    }

    /**
     * 在 TopBar 右边添加一个 Button，并设置文字
     *
     * @param stringResId 按钮的文字的 resourceId
     * @param viewId      该按钮的id，可在 ids.xml 中找到合适的或新增。手工指定 viewId 是为了适应自动化测试。
     * @return 返回生成的按钮
     */
    fun addRightTextButton(@StringRes stringResId: Int, viewId: Int): Button {
        return addRightTextButton(resources.getString(stringResId), viewId)
    }

    /**
     * 在 TopBar 右边添加一个 Button，并设置文字
     *
     * @param stringResId 按钮的文字的 resourceId
     * @param viewId      该按钮的id，可在 ids.xml 中找到合适的或新增。手工指定 viewId 是为了适应自动化测试。
     * @return 返回生成的按钮
     */
    fun addRightTextButton(@StringRes stringResId: Int, viewId: Int, textColor: ColorStateList, textSizePX: Float): Button {
        return addRightTextButton(resources.getString(stringResId), viewId, textColor, textSizePX)
    }

    fun addRightTextButton(@StringRes stringResId: Int): Button {
        return addRightTextButton(resources.getString(stringResId))
    }

    /**
     * 在 TopBar 右边添加一个 Button，并设置文字
     *
     * @param buttonText 按钮的文字
     * @param viewId     该按钮的 id，可在 ids.xml 中找到合适的或新增。手工指定 viewId 是为了适应自动化测试。
     * @return 返回生成的按钮
     */
    fun addRightTextButton(buttonText: String, viewId: Int, textColor: ColorStateList, textSizePX: Float): Button {
        val button = generateTopBarTextButton(buttonText, textColor, textSizePX)
        this.addRightView(button, viewId, generateTopBarTextButtonLayoutParams())
        return button
    }

    /**
     * 在 TopBar 右边添加一个 Button，并设置文字
     *
     * @param buttonText 按钮的文字
     * @param viewId     该按钮的 id，可在 ids.xml 中找到合适的或新增。手工指定 viewId 是为了适应自动化测试。
     * @return 返回生成的按钮
     */
    fun addRightTextButton(buttonText: String, viewId: Int): Button {
        val button = generateTopBarTextButton(buttonText)
        this.addRightView(button, viewId, generateTopBarTextButtonLayoutParams())
        return button
    }

    fun addRightTextButton(buttonText: String): Button {
        val button = generateTopBarTextButton(buttonText)
        this.addRightView(button, button.hashCode(), generateTopBarTextButtonLayoutParams())
        return button
    }

    /**
     * 生成一个文本按钮，并设置文字
     *
     * @param text 按钮的文字
     * @return 返回生成的按钮
     */
    private fun generateTopBarTextButton(text: String,
                                         textColor: ColorStateList = mTopBarTextBtnTextColor
                                                 ?: context.resources.getColorStateList(R.color.titlebar_text_color),
                                         textSizePX: Float = mTopBarTextBtnTextSize.toFloat()): Button {
        val button = Button(context)
        button.setBackgroundResource(0)
        button.minWidth = 0
        button.minHeight = 0
        button.minimumWidth = 0
        button.minimumHeight = 0
        val paddingHorizontal = topBarTextBtnPaddingHorizontal
        button.setPadding(paddingHorizontal, 0, paddingHorizontal, 0)
        button.setTextColor(textColor)
        button.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSizePX)
        button.gravity = Gravity.CENTER
        button.text = text
        return button
    }

    /**
     * 生成一个图片按钮，配合 {[.generateTopBarImageButtonLayoutParams] 使用
     *
     * @param imageResourceId 图片的 resId
     */
    private fun generateTopBarImageButton(imageResourceId: Int): UIAlphaImageButton {
        val backButton = UIAlphaImageButton(context)
        backButton.setBackgroundColor(Color.TRANSPARENT)
        backButton.scaleType = ImageView.ScaleType.CENTER_INSIDE
        backButton.setImageResource(imageResourceId)
        return backButton
    }

    /**
     * 便捷方法，在 TopBar 左边添加一个返回图标按钮
     *
     * @return 返回按钮
     */
    fun addLeftBackImageButton(): UIAlphaImageButton {
        return addLeftImageButton(mLeftBackDrawableRes, R.id.titlebar_item_left_back)
    }

    /**
     * 移除 TopBar 左边所有的 View
     */
    fun removeAllLeftViews() {
        for (leftView in mLeftViewList) {
            removeView(leftView)
        }
        mLeftLastViewId = DEFAULT_VIEW_ID
        mLeftViewList.clear()
    }

    /**
     * 移除 TopBar 右边所有的 View
     */
    fun removeAllRightViews() {
        for (rightView in mRightViewList) {
            removeView(rightView)
        }
        mRightLastViewId = DEFAULT_VIEW_ID
        mRightViewList.clear()
    }

    /**
     * 移除 TopBar 的 centerView 和 titleView
     */
    fun removeCenterViewAndTitleView() {
        if (mCenterView != null) {
            if (mCenterView!!.parent == this) {
                removeView(mCenterView)
            }
            mCenterView = null
        }

        if (isTitleViewAdd()) {
            if (mTitleView.parent == this) {
                removeView(mTitleView)
            }
        }
    }

    // ======================== TopBar自身相关的方法

    /**
     * 设置 TopBar 背景的透明度
     *
     * 值范围：[0, 255]，255表示不透明
     */
    var backgroundAlpha: Int
        set(@IntRange(from = 0, to = 255) value) {
            this.background.alpha = value
        }
        get() = this.background.alpha

    /**
     * 根据当前 offset、透明度变化的初始 offset 和目标 offset，计算并设置 bar 的透明度
     *
     * @param currentOffset     当前 offset
     * @param alphaBeginOffset  透明度开始变化的offset，即当 currentOffset == alphaBeginOffset 时，透明度为0
     * @param alphaTargetOffset 透明度变化的目标offset，即当 currentOffset == alphaTargetOffset 时，透明度为1
     */
    fun computeAndSetBackgroundAlpha(currentOffset: Int, alphaBeginOffset: Int, alphaTargetOffset: Int): Int {
        var alpha = ((currentOffset - alphaBeginOffset).toDouble() / (alphaTargetOffset - alphaBeginOffset))
        alpha = Math.max(0.0, Math.min(alpha, 1.0)) // from 0 to 1
        val alphaInt = (alpha * 255).toInt()
        this.backgroundAlpha = alphaInt
        return alphaInt
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val mHeightMeasureSpec: Int = View.MeasureSpec.makeMeasureSpec(topBarHeight, View.MeasureSpec.EXACTLY)
        super.onMeasure(widthMeasureSpec, mHeightMeasureSpec)

        if (isTitleContainerViewAdd()) {
            // 计算左侧 View 的总宽度
            var leftViewWidth = 0
            for (leftView in mLeftViewList) {
                if (leftView.visibility != View.GONE) {
                    leftViewWidth += leftView.measuredWidth
                }
            }
            // 计算右侧 View 的总宽度
            var rightViewWidth = 0
            for (rightView in mRightViewList) {
                if (rightView.visibility != View.GONE) {
                    rightViewWidth += rightView.measuredWidth
                }
            }
            // 计算 titleContainer 的最大宽度
            val titleContainerWidth: Int
            if (mTitleGravity and Gravity.HORIZONTAL_GRAVITY_MASK == Gravity.CENTER_HORIZONTAL) {
                if (leftViewWidth == 0 && rightViewWidth == 0) {
                    // 左右没有按钮时，title 距离 TopBar 左右边缘的距离
                    val titleMarginHorizontalWithoutButton = mTitleMarginHorWhenNoBtnAside
                    leftViewWidth += titleMarginHorizontalWithoutButton
                    rightViewWidth += titleMarginHorizontalWithoutButton
                }

                // 标题水平居中，左右两侧的占位要保持一致
                titleContainerWidth = View.MeasureSpec.getSize(widthMeasureSpec) - Math.max(
                        leftViewWidth,
                        rightViewWidth
                ) * 2 - paddingLeft - paddingRight
            } else {
                // 标题非水平居中，左右没有按钮时，间距分别计算
                if (leftViewWidth == 0) {
                    leftViewWidth += mTitleMarginHorWhenNoBtnAside
                }
                if (rightViewWidth == 0) {
                    rightViewWidth += mTitleMarginHorWhenNoBtnAside
                }

                // 标题非水平居中，左右两侧的占位按实际计算即可
                titleContainerWidth =
                        View.MeasureSpec.getSize(widthMeasureSpec) - leftViewWidth - rightViewWidth - paddingLeft - paddingRight
            }
            val titleContainerWidthMeasureSpec =
                    View.MeasureSpec.makeMeasureSpec(titleContainerWidth, View.MeasureSpec.EXACTLY)
            mTitleContainerView.measure(titleContainerWidthMeasureSpec, mHeightMeasureSpec)
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        if (isTitleContainerViewAdd()) {
            val titleContainerViewWidth = mTitleContainerView.measuredWidth
            val titleContainerViewHeight = mTitleContainerView.measuredHeight
            val titleContainerViewTop = (b - t - mTitleContainerView.measuredHeight) / 2
            var titleContainerViewLeft = paddingLeft
            if (mTitleGravity and Gravity.HORIZONTAL_GRAVITY_MASK == Gravity.CENTER_HORIZONTAL) {
                // 标题水平居中
                titleContainerViewLeft = (r - l - mTitleContainerView.measuredWidth) / 2
            } else {
                // 标题非水平居中
                // 计算左侧 View 的总宽度
                for (leftView in mLeftViewList) {
                    if (leftView.visibility != View.GONE) {
                        titleContainerViewLeft += leftView.measuredWidth
                    }
                }

                if (mLeftViewList.isEmpty()) {
                    //左侧没有按钮，标题离左侧间距
                    titleContainerViewLeft += mTitleMarginHorWhenNoBtnAside
                }
            }
            mTitleContainerView.layout(
                    titleContainerViewLeft,
                    titleContainerViewTop,
                    titleContainerViewLeft + titleContainerViewWidth,
                    titleContainerViewTop + titleContainerViewHeight
            )
        }
    }

    companion object {
        private const val DEFAULT_VIEW_ID = -1
        private const val TITLE_VIEW_TAG = 112
        private const val SUBTITLE_VIEW_TAG = 113
        private const val TITLE_CONTAINER_VIEW_TAG = 114
    }
}