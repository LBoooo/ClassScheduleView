package com.hinacle.classscheduleview

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
//import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.hinacle.classschedule.TimetableView
import com.hinacle.classschedule.listener.ISchedule
import com.hinacle.classschedule.listener.IWeekView
import com.hinacle.classschedule.listener.OnSlideBuildAdapter
import com.hinacle.classschedule.model.MySubject
import com.hinacle.classschedule.model.Schedule
import com.hinacle.classschedule.view.WeekView


class MainActivity : Activity() {

    var mySubjects: List<MySubject>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mySubjects = loadDefaultSubjects()
        initTimetableView()
        hideNonThisWeek()
        showTime()
        hideWeekends()
        setMonthWidth()
    }

    lateinit var mWeekView: WeekView
    lateinit var mTimetableView: TimetableView

    fun initTimetableView() {

        //获取控件
        mWeekView = findViewById(R.id.id_weekview)
        mTimetableView = findViewById(R.id.id_timetableView)

        //设置周次选择属性

        //设置周次选择属性
        mWeekView.source(mySubjects)
            .curWeek(1)
            .callback { week ->
                val cur: Int = mTimetableView.curWeek()
                //更新切换后的日期，从当前周cur->切换的周week
                mTimetableView.onDateBuildListener()
                    .onUpdateDate(cur, week)
                mTimetableView.changeWeekOnly(week)
            }
            .callback(IWeekView.OnWeekLeftClickedListener { onWeekLeftLayoutClicked() })
            .isShow(false) //设置隐藏，默认显示
            .showView()

        mTimetableView.source(mySubjects)
            .curWeek(1)
            .curTerm("大三下学期")
            .maxSlideItem(10)
            .monthWidthDp(30) //透明度
            //日期栏0.1f、侧边栏0.1f，周次选择栏0.6f
            //透明度范围为0->1，0为全透明，1为不透明
            //                .alpha(0.1f, 0.1f, 0.6f)
            .callback(ISchedule.OnItemClickListener { v, scheduleList ->
                display(scheduleList)
            })
            .callback(ISchedule.OnItemLongClickListener { v, day, start ->
                Toast.makeText(
                    this@MainActivity,
                    "长按:周" + day + ",第" + start + "节",
                    Toast.LENGTH_SHORT
                ).show()
            })
            .callback(ISchedule.OnWeekChangedListener { curWeek ->

                Toast.makeText(this , "第" + curWeek + "周",Toast.LENGTH_SHORT).show()
            }) //旗标布局点击监听
            .callback(ISchedule.OnFlaglayoutClickListener { day, start ->
                mTimetableView.hideFlaglayout()
                Toast.makeText(
                    this@MainActivity,
                    "点击了旗标:周" + (day + 1) + ",第" + start + "节",
                    Toast.LENGTH_SHORT
                ).show()
            })
            .showView()

    }

    /**
     * 更新一下，防止因程序在后台时间过长（超过一天）而导致的日期或高亮不准确问题。
     */
    override fun onStart() {
        super.onStart()
        mTimetableView.onDateBuildListener()
            .onHighLight()
    }


    //记录切换的周次，不一定是当前周
    var target = -1

    /**
     * 周次选择布局的左侧被点击时回调<br></br>
     * 对话框修改当前周次
     */
    protected fun onWeekLeftLayoutClicked() {
        val items = arrayOfNulls<String>(20)
        val itemCount = mWeekView.itemCount()
        for (i in 0 until itemCount) {
            items[i] = "第" + (i + 1) + "周"
        }
        target = -1
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("设置当前周")
        builder.setSingleChoiceItems(
            items,
            mTimetableView.curWeek() - 1
        ) { dialogInterface, i -> target = i }
        builder.setPositiveButton("设置为当前周") { dialog, which ->
            if (target != -1) {
                mWeekView.curWeek(target + 1).updateView()
                mTimetableView.changeWeekForce(target + 1)
            }
        }
        builder.setNegativeButton("取消", null)
        builder.create().show()
    }

    /**
     * 显示内容
     *
     * @param beans
     */
    protected fun display(beans: List<Schedule?>) {
        var str = ""
        for (bean in beans) {
            bean?.let {
                str += bean.getName().toString() + "," + bean.getWeekList()
                    .toString() + "," + bean.getStart() + "," + bean.getStep() + "\n"
            }
        }
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show()
    }

    fun loadDefaultSubjects(): List< MySubject> {
       return (0..10).map {
           MySubject().apply {
                term = "2017-2018学年秋"
                name = "计算机组成原理"
                room = "203"
                teacher = "jiaoshi"+it
                weekList = listOf(1,2)
                start = 1
                step = 1
                day = 2
            }
        }
    }

    /**
     * 显示时间
     * 设置侧边栏构建监听，TimeSlideAdapter是控件实现的可显示时间的侧边栏
     */
    protected fun showTime() {
        val times = arrayOf(
            "8:00", "9:00", "10:00", "11:00",
            "12:00", "13:00", "14:00", "15:00",
            "16:00", "17:00", "18:00", "19:00"
            , "20:00", "21:00", "22:00"
        )
        val listener: OnSlideBuildAdapter =
            mTimetableView.onSlideBuildListener() as OnSlideBuildAdapter
        listener.setTimes(times)
            .setTimeTextColor(Color.BLACK)
            .hideNumberTextView()
        mTimetableView.updateSlideView()

        setMaxItem(times.size)
    }

    /**
     * 隐藏周末
     */
    private fun hideWeekends() {
        mTimetableView.isShowWeekends(false).updateView()
    }

    /**
     * 设置月份宽度
     */
    private fun setMonthWidth() {
        mTimetableView.monthWidthDp(50).updateView()
    }

    /**
     * 设置侧边栏最大节次，只影响侧边栏的绘制，对课程内容无影响
     *
     * @param num
     */
    protected fun setMaxItem(num: Int) {
        mTimetableView.maxSlideItem(num).updateSlideView()
    }

    /**
     * 隐藏非本周课程
     * 修改了内容的显示，所以必须更新全部（性能不高）
     * 建议：在初始化时设置该属性
     *
     *
     * updateView()被调用后，会重新构建课程，课程会回到当前周
     */
    protected fun hideNonThisWeek() {
        mTimetableView.isShowNotCurWeek(false).updateView()
    }
}