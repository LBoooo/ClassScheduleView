package com.hinacle.classschedule.model


fun flitSchedule(data: List<Schedule>?, curWeek: Int, isShowNotCurWeek: Boolean): List<Schedule> {
    if (data == null) return emptyList()

    val filter: MutableList<Schedule> = mutableListOf()
    if (!isShowNotCurWeek) {
        for (i in data.indices) {
            val s = data[i]
            if (ScheduleSupport.isThisWeek(s, curWeek)) filter.add(s)
        }
    }

    filter.sortWith(compareBy({ it.start + it.step }, { it.step }))
//        filter .reverse()
    return filter
}

/**
 * 递归查找符合条件的数据
 */
fun getAssociateData(
    data: List<Schedule>,
    currData: Schedule,
    tempList: MutableList<Schedule>
): List<Schedule> {
    return if (data.isEmpty()) {
        tempList
    } else {
        val temp = data.find {
            currData.start in it.start until it.start + it.step ||
                    (currData.start + currData.step) in (it.start + it.step) until it.start ||
                    it.start in currData.start until currData.start + currData.step ||
                    (it.start + it.step) in (currData.start + currData.step) until currData.start
        }
        if (temp == null) {
            tempList
        } else {
            tempList.add(temp)
            getAssociateData(data - temp, temp, tempList)
        }
    }
}

