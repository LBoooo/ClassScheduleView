package com.hinacle.classschedule.operater

import android.annotation.SuppressLint
import android.content.Context
import com.hinacle.classschedule.TimetableView
import android.view.LayoutInflater
import com.hinacle.classschedule.R
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.*
import com.hinacle.classschedule.model.*
import com.hinacle.classschedule.utils.ScreenUtils
import com.hinacle.classschedule.utils.ColorUtils
import java.util.ArrayList

/**
 * 课表业务操作者，TimetableView中只涉及属性的设置，方法的具体实现在这里.
 * 常用的方法也就四个，如下
 *
 * @see SimpleOperater.changeWeek
 * @see SimpleOperater.showView
 * @see SimpleOperater.updateDateView
 * @see SimpleOperater.updateSlideView
 */
open class SimpleOperater : AbsOperater() {
    protected var mView: TimetableView? = null
    protected var context: Context? = null

    //保存点击的坐标
    protected var x = 0f
    protected var y = 0f

    //布局转换器
    protected var inflater: LayoutInflater? = null
    protected var weekPanel //侧边栏
            : LinearLayout? = null
    protected var data: Array<MutableList<Schedule>?> = arrayOfNulls(7) //每天的课程
    protected var panels: Array<FrameLayout?>? = arrayOfNulls(7) //每天的面板
    protected var containerLayout //根布局
            : LinearLayout? = null
    protected var dateLinearLayout: LinearLayout? = null //根布局、日期栏容器
    protected var flagLinearLayout: LinearLayout? = null //旗标布局
    protected var scheduleConfig: ScheduleConfig? = null
    override fun init(context: Context, attrs: AttributeSet, view: TimetableView) {
        this.context = context
        mView = view
        inflater = LayoutInflater.from(context)
        inflater?.inflate(R.layout.timetable_layout, mView)
        containerLayout = mView!!.findViewById(R.id.id_container)
        dateLinearLayout = mView!!.findViewById(R.id.id_datelayout)
        mView!!.monthWidthDp(40)
        initAttr(attrs)
        scheduleConfig = ScheduleConfig(context)
    }

    /**
     * 获取自定义属性
     *
     * @param attrs
     */
    protected fun initAttr(attrs: AttributeSet?) {
        if (attrs == null) return
        val ta = context!!.obtainStyledAttributes(attrs, R.styleable.TimetableView)
        val curWeek = ta.getInteger(R.styleable.TimetableView_cur_week, 1)
        val curTerm = ta.getString(R.styleable.TimetableView_cur_term)
        val defMarTop = context!!.resources.getDimension(R.dimen.weekItemMarTop).toInt()
        val defMarLeft = context!!.resources.getDimension(R.dimen.weekItemMarLeft).toInt()
        val defItemHeight = context!!.resources.getDimension(R.dimen.weekItemHeight).toInt()
        val marTop = ta.getDimension(R.styleable.TimetableView_mar_top, defMarTop.toFloat())
            .toInt()
        val marLeft = ta.getDimension(R.styleable.TimetableView_mar_left, defMarLeft.toFloat())
            .toInt()
        val itemHeight =
            ta.getDimension(R.styleable.TimetableView_item_height, defItemHeight.toFloat())
                .toInt()
        val thisWeekCorner = ta.getDimension(R.styleable.TimetableView_thisweek_corner, 5f).toInt()
        val nonWeekCorner = ta.getDimension(R.styleable.TimetableView_nonweek_corner, 5f).toInt()
        val maxSlideItem = ta.getInteger(R.styleable.TimetableView_max_slide_item, 12)
        val isShowNotWeek = ta.getBoolean(R.styleable.TimetableView_show_notcurweek, true)
        ta.recycle()
        mView!!.curWeek(curWeek)
            .curTerm(curTerm)
            .marTop(0)
            .marLeft(marLeft)
            .itemHeight(itemHeight)
            .corner(thisWeekCorner, true)
            .corner(nonWeekCorner, false)
            .maxSlideItem(maxSlideItem)
            .isShowNotCurWeek(isShowNotWeek)
    }

    override fun getDateLayout(): LinearLayout {
        return dateLinearLayout!!
    }

    /**
     * 获取旗标布局,需要在showView方法执行后执行
     *
     * @return
     */
    override fun getFlagLayout(): LinearLayout {
        return flagLinearLayout!!
    }

    /**
     * 构建侧边栏
     *
     * @param slideLayout 侧边栏的容器
     */
    private fun newSlideView(slideLayout: LinearLayout?) {
        if (slideLayout == null) return
        slideLayout.removeAllViews()
        val listener = mView!!.onSlideBuildListener()
        listener.onInit(slideLayout, mView!!.slideAlpha())
        for (i in 0 until mView!!.maxSlideItem()) {
            val view = listener.getView(i, inflater, mView!!.itemHeight(), mView!!.marTop())
            slideLayout.addView(view)
        }
    }

    /**
     * 将数据data添加到layout的布局上
     *
     * @param layout  容器
     * @param data    某一天的数据集合
     * @param curWeek 当前周
     */
    private fun addToLayout(layout: FrameLayout?, data: List<Schedule>?, curWeek: Int) {
        if (layout == null || data == null || data.isEmpty()) return
        layout.removeAllViews()

        //遍历
        val filter = flitSchedule(data, curWeek, mView!!.isShowNotCurWeek)

        for (i in filter.indices) {
            val view = inflater!!.inflate(R.layout.layout_test, null, false)
            layout.addView(view)
        }

        for (i in filter.indices) {
            newItem(layout, filter[i], i, filter)
        }
    }

    private fun newItem(
        layout: FrameLayout,
        subject: Schedule,
        index: Int,
        data: List<Schedule>
    ) {
        var lastHeight = 0
        val padding = ScreenUtils.dip2px(context, 5f)
        //宽高
        val width = LinearLayout.LayoutParams.MATCH_PARENT
        val height = mView!!.itemHeight() * subject.step + mView!!.marTop() * (subject.step - 1)
        //边距
        val left = mView!!.marLeft() / 2
        val right = mView!!.marLeft() / 2
        val top = (subject.start - 1) * mView!!.itemHeight() + mView!!.marTop() * subject.start

        val view = layout.getChildAt(index)
        val ll = view.findViewById<LinearLayout>(R.id.itemLayout)
        val lp = FrameLayout.LayoutParams(width, height)
        val tv = view.findViewById<TextView>(R.id.textView)
        val gd = GradientDrawable()
        val overlappingTagView = view.findViewById<ImageView>(R.id.overlappingTagView)
        val remarkView = view.findViewById<ImageView>(R.id.remarkView)
        val colorMap = mView!!.colorPool().colorMap
        if (!(colorMap.isEmpty() || !colorMap.containsKey(subject.name))) {
            gd.setColor(ColorUtils.alphaColor(colorMap[subject.name]!!, mView!!.itemAlpha()))
        } else {
            gd.setColor(
                mView!!.colorPool().getColorAutoWithAlpha(subject.colorRandom, mView!!.itemAlpha())
            )
        }
        gd.cornerRadius = mView!!.corner(true).toFloat()
        tv.text = subject.name

        lp.setMargins(left, top, right, 0)
        ll.layoutParams = lp
        ll.background = gd

        var space = 0
        for (si in 0 until subject.step){
            val tempData = data.mapIndexed { i, t ->
                if (i > index) {
                    si + subject.start in t.start until t.start + t.step
                }else{
                    false
                }
            }.contains(true)
            if (!tempData){
                space = si
                break
            }
        }

        if (space != 0) {
            lastHeight = (space * mView!!.itemHeight()) + (mView!!.marTop() * (space))
        }
        ll.setPadding(padding, lastHeight + padding, padding, padding)
        mView!!.onItemBuildListener()
            .onItemUpdate(ll, tv, overlappingTagView, remarkView, subject, gd)
        ll.setOnClickListener {
            mView!!.onItemClickListener().onItemClick(
                it, getAssociateData(
                    data, subject,
                    mutableListOf()
                )
            )
        }
    }

    /**
     * 点击panel时的事件响应
     */
    protected fun onPanelClicked(view: View, y: Float) {
        if (mView!!.isShowFlaglayout) {
            flagLinearLayout!!.visibility = View.VISIBLE
        } else {
            flagLinearLayout!!.visibility = View.GONE
        }

        //周几，0：周一，6：周日
        var day = 0

        // 先找到点击的是周几的panel
        for (i in panels!!.indices) {
            if (view === panels!![i]) {
                day = i
                break
            }
        }
        if (day == -1) return

        // 判断点击的是第几节课，1：第1节
        val start = Math.ceil((y / (mView!!.itemHeight() + mView!!.marTop())).toDouble())
            .toInt()
        if (!checkPosition(day, start)) {
            mView!!.onSpaceItemClickListener().onSpaceItemClick(day, start)
        }
        val finalDay = day
        flagLinearLayout!!.setOnClickListener {
            mView!!.onFlaglayoutClickListener().onFlaglayoutClick(finalDay, start)
        }
    }

    /**
     * 判断位置是否有课
     *
     * @param day
     * @param start
     * @return true：有课，false：无课
     */
    protected fun checkPosition(day: Int, start: Int): Boolean {
        val list: List<Schedule>
        list = if (mView!!.isShowNotCurWeek) {
            ScheduleSupport.getAllSubjectsWithDay(mView!!.dataSource(), day)
        } else {
            ScheduleSupport.getHaveSubjectsWithDay(mView!!.dataSource(), mView!!.curWeek(), day)
        }
        var isHave = false
        for (item in list) {
            if (start == item.start || start >= item.start && start <= item.start + item.step - 1) {
                isHave = true
            }
        }
        return isHave
    }

    /**
     * 初始化panel并为panel设置事件监听
     */
    @SuppressLint("ClickableViewAccessibility")
    protected fun initPanel() {
        for (i in panels!!.indices) {
            panels!![i] = mView!!.findViewById(R.id.weekPanel_1 + i)
            data[i] = ArrayList()
            /**
             * 点击空白格子时才会触发这个事件
             */
            panels!![i]!!.setOnTouchListener { arg0: View, arg1: MotionEvent ->
                when (arg1.action) {
                    MotionEvent.ACTION_DOWN -> {
                        x = arg1.x
                        y = arg1.y
                    }
                    MotionEvent.ACTION_UP -> {
                        val x2 = arg1.x
                        val y2 = arg1.y
                        if (x2 == x && y2 == y) onPanelClicked(arg0, arg1.y)
                    }
                    else -> {}
                }
                true
            }
        }
    }

    /**
     * 实现ScrollView的替换,只有在初始化时替换一次
     */
    fun replaceScrollView() {
        if (mView!!.findViewById<View?>(R.id.id_scrollview) == null) {
            val view = mView!!.onScrollViewBuildListener().getScrollView(inflater)
            containerLayout!!.addView(view)
            //初始化
            weekPanel = mView!!.findViewById(R.id.weekPanel_0)
            flagLinearLayout = mView!!.findViewById(R.id.id_flaglayout)
            initPanel()
        }
    }

    /**
     * 设置旗标布局的配置
     * mView!!.monthWidth()
     */
    fun applyFlagLayoutConf() {
        mView!!.hideFlaglayout()
        val lp =
            RelativeLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        weekPanel!!.layoutParams = lp
        flagLinearLayout!!.setBackgroundColor(mView!!.flagBgcolor())
        val perWidth = perWidth
        mView!!.onSpaceItemClickListener().onInit(
            flagLinearLayout, mView!!.monthWidth(),
            Math.round(perWidth), mView!!.itemHeight(), mView!!.marTop(),
            Math.round(mView!!.marLeft() / 2.0f)
        )
    }

    /**
     * 开始装填数据
     */
    fun startTodo() {
        //清空、拆分数据
        for (i in 0..6) {
            data[i]?.clear()
        }
        val source = mView!!.dataSource()
        for (i in source.indices) {
            val bean = source[i]
            if (bean.day != -1) data[bean.day - 1]?.add(bean)
        }

        //排序、填充课程
        ScheduleSupport.sortList(data)
        for (i in panels!!.indices) {
            panels!![i]!!.removeAllViews()
            addToLayout(panels!![i], data[i], mView!!.curWeek())
        }
    }

    /**
     * 设置宽度
     */
    fun applyWidthConfig() {
        setWeekendsVisiable(mView!!.isShowWeekends)
    }

    /**
     * 绘制课程表
     */
    override fun showView() {
        if (mView == null || mView!!.dataSource() == null) return
        checkConfig()
        replaceScrollView()
        Log.d(TAG, "showView: $flagLinearLayout")
        applyFlagLayoutConf()
        applyWidthConfig()

        //更新日期
        updateDateView()
        updateSlideView()
        startTodo()
    }

    /**
     * 本地配置的加载
     */
    private fun checkConfig() {
        if (mView == null || mView!!.onConfigHandleListener() == null) return
        if (mView!!.onConfigHandleListener() !== scheduleConfig!!.onConfigHandleListener) {
            scheduleConfig!!.onConfigHandleListener = mView!!.onConfigHandleListener()
        }
        scheduleConfig!!.setConfigName(mView!!.configName())
        scheduleConfig!!.use(mView)
    }

    /**
     * 切换周次
     *
     * @param week
     * @param isCurWeek 是否强制设置为当前周
     */
    override fun changeWeek(week: Int, isCurWeek: Boolean) {
        for (i in panels!!.indices) {
            addToLayout(panels!![i], data[i], week)
        }
        if (isCurWeek) {
            mView!!.curWeek(week)
        } else {
            mView!!.onWeekChangedListener().onWeekChanged(week)
        }
    }

    protected val perWidth: Float
        get() {
            val perWidth = if (mView!!.isShowWeekends) {
                ((ScreenUtils.getWidthInPx(context) - mView!!.monthWidth()) / 7).toFloat()
            } else {
                ((ScreenUtils.getWidthInPx(context) - mView!!.monthWidth()) / 5).toFloat()
            }
            return perWidth
        }

    /**
     * 更新日期栏
     */
    override fun updateDateView() {
        dateLinearLayout!!.removeAllViews()
        val perWidth = perWidth
        val height = context!!.resources.getDimensionPixelSize(R.dimen.headHeight)
        //		//日期栏
        val listener = mView!!.onDateBuildListener()
        listener.onInit(dateLinearLayout, mView!!.dateAlpha())
        val views = mView!!.onDateBuildListener()
            .getDateViews(inflater, mView!!.monthWidth().toFloat(), perWidth, height)
        for (v in views) {
            if (v != null) {
                dateLinearLayout!!.addView(v)
            }
        }
        mView!!.onDateBuildListener().onUpdateDate(mView!!.curWeek(), mView!!.curWeek())
        mView!!.onDateBuildListener().onHighLight()
    }

    /**
     * 侧边栏更新
     */
    override fun updateSlideView() {
        newSlideView(weekPanel)
    }

    /**
     * 设置周末的可见性
     */
    override fun setWeekendsVisiable(isShow: Boolean) {
        if (isShow) {
            if (panels != null && panels!!.size > 6) {
                panels!![5]!!.visibility = View.VISIBLE
                panels!![6]!!.visibility = View.VISIBLE
            }
        } else {
            if (panels != null && panels!!.size > 6) {
                panels!![5]!!.visibility = View.GONE
                panels!![6]!!.visibility = View.GONE
            }
        }
    }

    companion object {
        private const val TAG = "SimpleOperater"
    }
}