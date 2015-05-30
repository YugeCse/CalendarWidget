package com.mrper.calendarwidget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Mrper on 2015/5/30.
 * 日历视图控件
 */
public class CalendarView extends LinearLayout {

    public class DayInfo{
        public int day;
        public DayType dayType;
        @Override
        public String toString() {
            return String.valueOf(day);
        }
    }

    public enum DayType{
        DAY_TYPE_NONE(0),
        DAY_TYPE_FORE(1),
        DAY_TYPE_NOW(2),
        DAY_TYPE_NEXT(3);
        private int value;
        private DayType(int value){ this.value = value; }
        public int getValue(){ return value; }
    }

    private Context context;//上下文对象
    private GridView dateGrid;//日期表格
    private final Calendar calendar = Calendar.getInstance();
    private int nowYear = 0,nowMonth = 0,nowDay = 0,centry = 0;//年、月、日、世纪-1参数
    private static final int MAX_DAY_COUNT = 42;//最大格子数量
    private DayInfo[] dayInfos = new DayInfo[MAX_DAY_COUNT];//每月应该有的天数，36为最大格子数

    public CalendarView(Context context) {
        super(context);
        init(context);//初始化程序
        showCalendar(calendar);//显示日历数据
    }

    public CalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);//初始化程序
        showCalendar(calendar);//显示日历数据
    }

    /**  初始化程序  **/
    private void init(Context context){
        this.context = context;
        View rootView = View.inflate(context,R.layout.widget_calendar,null);
        dateGrid = (GridView)rootView.findViewById(R.id.widgetCalendar_calendar);
        this.setOrientation(VERTICAL);//设置布局方向
        this.addView(rootView);//添加根视图
    }

    /**  显示日历数据  **/
    private void showCalendar(Calendar calendar){
        int year = calendar.get(Calendar.YEAR);//获得年份
        int month = calendar.get(Calendar.MONTH)+1;//获取月份
//        int day = calendar.get(Calendar.DATE);//获取天数
        int centry = Integer.valueOf(String.valueOf(year).substring(0,2));//取年份前两位作为世纪数,世纪数-1
        int tmpYear = Integer.valueOf(String.valueOf(year).substring(2,4));//取年份后两位
        if(month == 1||month == 2){//该年的1、2月看作为前一年的13月，14月
            tmpYear -= 1;
            month += 12;
        }
        //计算该月的第一天是星期几
        int firstOfWeek = (tmpYear + (tmpYear/4) + centry/4-2*centry+26*(month+1)/10)%7;
        if(firstOfWeek<=0) firstOfWeek = 7 + firstOfWeek;//处理星期的显示
        //计算第一天所在的索引值,如果该天为星期一，则做换行处理
        final int firstDayIndex = firstOfWeek == 1?7 : firstOfWeek - 1;
        final int dayCount = getDayCount(year,month);//获取该月的天数
        //处理本月的数据
        for(int i = firstDayIndex;i< firstDayIndex + dayCount;i++){
            if(dayInfos[i] == null)
                dayInfos[i] = new DayInfo();
            dayInfos[i].day = i - firstDayIndex + 1;
            dayInfos[i].dayType = DayType.DAY_TYPE_NOW;
        }
        //处理前一个月的数据
        calendar.add(Calendar.MONTH,-1);//前一个月
        year = calendar.get(Calendar.YEAR);//获得年份
        month = calendar.get(Calendar.MONTH)+1;//获取月份
        final int foreDayCount = getDayCount(year,month);//获得前一个月的天数
        for(int i = 0;i<firstDayIndex;i++){
            if(dayInfos[i] == null)
                dayInfos[i] = new DayInfo();
            dayInfos[i].day = foreDayCount - firstDayIndex + i  + 1;
            dayInfos[i].dayType = DayType.DAY_TYPE_FORE;
        }
        //处理下一个月的数据
        for(int i = 0;i<MAX_DAY_COUNT - dayCount - firstDayIndex;i++){
            if(dayInfos[firstDayIndex + dayCount+i] == null)
                dayInfos[firstDayIndex + dayCount+i] = new DayInfo();
            dayInfos[firstDayIndex + dayCount+i].day = i+1;
            dayInfos[firstDayIndex + dayCount+i].dayType = DayType.DAY_TYPE_NEXT;
        }
        dateGrid.setAdapter(new CalendarAdapter(context,dayInfos));
    }

    /** 是否是平年 **/
    private boolean isLeapYear(int year){
        return !((year%4==0 && year%100!=0) || year%400==0);
    }

    /**
     * 获取某年的某月有多少天
     * @param year
     * @param month
     * @return
     */
    private int getDayCount(int year,int month){
        switch(month){
            case 1:
            case 3:
            case 5:
            case 7:
            case 8:
            case 10:
            case 12:
            case 13:
                return 31;
            case 2:
                return isLeapYear(year)?28:29;
            case 4:
            case 6:
            case 9:
            case 11:
                return 30;
        }
        return 0;
    }

    /**  日历数据适配器  **/
    public class CalendarAdapter extends BaseAdapter{

        private Context context;
        private List<DayInfo> dayInfos = new ArrayList<>();

        public CalendarAdapter(Context context, DayInfo[] dayInfos){
            this.context = context;
            if(dayInfos!=null && dayInfos.length > 0){
                this.dayInfos.addAll(Arrays.asList(dayInfos));
            }
        }

        @Override
        public int getCount() {
            return dayInfos == null?0:dayInfos.size();
        }

        @Override
        public Object getItem(int position) {
            return dayInfos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null){
                convertView = new TextView(context);
                AbsListView.LayoutParams cellLayoutParams = new AbsListView.LayoutParams(
                        AbsListView.LayoutParams.MATCH_PARENT,
                        AbsListView.LayoutParams.MATCH_PARENT);
                convertView.setLayoutParams(cellLayoutParams);
                TextView txtCell = ((TextView) convertView);
                txtCell.setGravity(Gravity.CENTER);
                txtCell.setPadding(10,15,10,15);
                txtCell.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
                txtCell.getPaint().setFakeBoldText(true);
                txtCell.setTextSize(TypedValue.COMPLEX_UNIT_DIP,17f);
            }
            System.out.println("---------->position="+position+",value="+dayInfos.get(position).toString());
            ((TextView)convertView).setText(dayInfos.get(position).toString());
            return convertView;
        }
    }

}
