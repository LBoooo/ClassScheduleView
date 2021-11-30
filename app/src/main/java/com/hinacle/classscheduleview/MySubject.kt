package com.hinacle.classscheduleview

import com.hinacle.classschedule.model.Schedule
import com.hinacle.classschedule.model.ScheduleEnable


/**
 * 自定义实体类需要实现ScheduleEnable接口并实现getSchedule()
 *
 * @see ScheduleEnable.getSchedule
 */
open class MySubject : ScheduleEnable {
    var id = 0

    /**
     * 课程名
     */
    var name: String? = null

    //无用数据
    var time: String? = null

    /**
     * 教室
     */
    var room: String? = null

    /**
     * 教师
     */
    var teacher: String? = null

    /**
     * 第几周至第几周上
     */
    var weekList: List<Int>? = null

    /**
     * 开始上课的节次
     */
    var start = 0

    /**
     * 上课节数
     */
    var step = 0

    /**
     * 周几上
     */
    var day = 0
    var term: String? = null

    /**
     * 一个随机数，用于对应课程的颜色
     */
    var colorRandom = 0
    var url: String? = null

    constructor() {
        // TODO Auto-generated constructor stub
    }

    constructor(
        term: String?,
        name: String?,
        room: String?,
        teacher: String?,
        weekList: List<Int>?,
        start: Int,
        step: Int,
        day: Int,
        colorRandom: Int,
        time: String?
    ) : super() {
        this.term = term
        this.name = name
        this.room = room
        this.teacher = teacher
        this.weekList = weekList
        this.start = start
        this.step = step
        this.day = day
        this.colorRandom = colorRandom
        this.time = time
    }

    override fun getSchedule(): Schedule {
        val schedule = Schedule()
        schedule.day = day
        schedule.name = name
        schedule.room = room
        schedule.start = start
        schedule.step = step
        schedule.teacher = teacher
        schedule.weekList = weekList
        schedule.colorRandom = 2
        schedule.putExtras(EXTRAS_ID, id)
        schedule.putExtras(EXTRAS_AD_URL, url)
        return schedule
    }

    companion object {
        const val EXTRAS_ID = "extras_id"
        const val EXTRAS_AD_URL = "extras_ad_url"
    }
}