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

//    filter.sortWith(compareBy({it.start},{it.step}))

    filter.sortWith { o1, o2 ->
        if (o1.start == o2.start) {
            o2.step.compareTo(o1.step)
        } else {
            o1.start.compareTo(o2.start)
        }
    }
    return filter
//    filter.sortWith(compareBy({ it.start + it.step }, { it.step }))
//    return filter.asReversed()
}

/**
 * 递归查找符合条件的数据
 */
tailrec fun getAssociateData(
    data: List<Schedule>,
    currData: Schedule,
    tempList: HashSet<Schedule>
): List<Schedule> {
    return if (data.isEmpty()) {
        tempList.toList()
    } else {
        val temp = data.filter {
            currData.start in it.start until it.start + it.step ||
                    (currData.start + currData.step) in (it.start + it.step) until it.start ||
                    it.start in currData.start until currData.start + currData.step ||
                    (it.start + it.step) in (currData.start + currData.step) until currData.start
        }
        if (temp.isEmpty()) {
            tempList.toList()
        } else {
            tempList.addAll(temp)
            getAssociateData(data - temp[0], temp[0], tempList)
        }
    }
}

