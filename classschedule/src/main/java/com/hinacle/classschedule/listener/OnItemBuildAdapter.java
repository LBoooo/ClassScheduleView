package com.hinacle.classschedule.listener;

import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hinacle.classschedule.model.Schedule;

/**
 * Item构建监听器的默认实现.
 */

public class OnItemBuildAdapter implements ISchedule.OnItemBuildListener {
    @Override
    public String getItemText(Schedule schedule, boolean isThisWeek) {
        if (schedule == null || TextUtils.isEmpty(schedule.getName())) return "未命名";
        if (schedule.getRoom() == null) {
            if (!isThisWeek)
                return "[非本周]" + schedule.getName();
            return schedule.getName();
        }

        String r = schedule.getName() + "@" + schedule.getRoom();
        if (!isThisWeek) {
            r = "[非本周]" + r;
        }
        return r;
    }

    @Override
    public void onItemUpdate(FrameLayout layout, TextView textView, TextView countTextView, Schedule schedule, GradientDrawable gd) {
    }

    @Override
    public void onItemUpdate(LinearLayout layout, TextView textView, ImageView tagView, ImageView remarkView, Schedule schedule, GradientDrawable gd) {

    }
}
