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
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Mrper on 2015/5/30.
 * 日历视图控件
 */
public class CalendarView extends LinearLayout {

    /** 日期信息实体类 **/
    public class DayInfo{
        public int day;
        public DayType dayType;
        @Override
        public String toString() {
            return String.valueOf(day);
        }
    }
    /** 日期类型 **/
    public enum DayType{
        DAY_TYPE_NONE(0),
        DAY_TYPE_FORE(1),
        DAY_TYPE_NOW(2),
        DAY_TYPE_NEXT(3);
        private int value;
        DayType(int value){ this.value = value; }
        public int getValue(){ return value; }
    }

    private Context context;//上下文对象
    private TextView txtTitle;//标题文字
    private GridView dateGrid;//日期表格
    private final Calendar calendar = Calendar.getInstance();
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
        txtTitle = (TextView)rootView.findViewById(R.id.widgetCalendar_txtTitle);
        rootView.findViewById(R.id.widgetCalendar_imgForeYear)
                .setOnClickListener(navigatorClickListener);
        rootView.findViewById(R.id.widgetCalendar_imgForeMonth)
                .setOnClickListener(navigatorClickListener);
        rootView.findViewById(R.id.widgetCalendar_imgNextMonth)
                .setOnClickListener(navigatorClickListener);
        rootView.findViewById(R.id.widgetCalendar_imgNextYear)
                .setOnClickListener(navigatorClickListener);
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
        calendar.add(Calendar.MONTH,1);//还原月份数据
        txtTitle.setText(new SimpleDateFormat("yyyy年MM月").format(calendar.getTime()));//设置日历显示的标题
        dateGrid.setAdapter(new CalendarAdapter(context,dayInfos));
    }

    /** 导航按钮点击事件 **/
    private View.OnClickListener navigatorClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.widgetCalendar_imgForeMonth://上一月
                    calendar.add(Calendar.MONTH,-1);
                    break;
                case R.id.widgetCalendar_imgForeYear://上一年
                    calendar.add(Calendar.YEAR,-1);
                    break;
                case R.id.widgetCalendar_imgNextMonth://下一月
                    calendar.add(Calendar.MONTH,1);
                    break;
                case R.id.widgetCalendar_imgNextYear://下一年
                    calendar.add(Calendar.YEAR,1);
                    break;
            }
            showCalendar(calendar);//显示日历数据
        }
    };

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
            case 13://其实是1月，当作上一年的13月看待
                return 31;
            case 2:
            case 14://其实是2月，当作上一年的14月看
                return isLeapYear(year)?28:29;
            case 4:
            case 6:
            case 9:
            case 11:
                return 30;
        }
        return 0;
    }

    /**
     * 判断两个Calendar中的日期是否相等
     * @param calendar
     * @param calendar1
     * @return
     */
    private boolean isDateEqual(Calendar calendar,Calendar calendar1){
        return (calendar.get(Calendar.YEAR) == calendar1.get(Calendar.YEAR)
                && calendar.get(Calendar.MONTH) == calendar1.get(Calendar.MONTH)
                && calendar.get(Calendar.DATE) == calendar1.get(Calendar.DATE));
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
            final DayInfo item = dayInfos.get(position);
            if(convertView == null){
                convertView = new TextView(context);
                AbsListView.LayoutParams cellLayoutParams = new AbsListView.LayoutParams(
                        AbsListView.LayoutParams.MATCH_PARENT,
                        AbsListView.LayoutParams.MATCH_PARENT);
                convertView.setLayoutParams(cellLayoutParams);
                TextView txtCell = ((TextView) convertView);
                txtCell.setGravity(Gravity.CENTER);
                txtCell.setPadding(10,15,10,15);
                txtCell.getPaint().setFakeBoldText(true);
                txtCell.setTextSize(TypedValue.COMPLEX_UNIT_DIP,17f);
            }
            TextView txtItem = ((TextView)convertView);
            txtItem.setText(item.toString());
            if(item.dayType == DayType.DAY_TYPE_FORE || item.dayType == DayType.DAY_TYPE_NEXT){//标识文字颜色
                txtItem.setTextColor(Color.DKGRAY);
            }else{
                txtItem.setTextColor(Color.BLACK);
            }
            Calendar tmpCalendar = Calendar.getInstance();
            tmpCalendar.setTimeInMillis(calendar.getTimeInMillis());
            tmpCalendar.set(Calendar.DAY_OF_MONTH,item.day);
            if(isDateEqual(Calendar.getInstance(),tmpCalendar) && item.dayType == DayType.DAY_TYPE_NOW){//判断是不是今天
                txtItem.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#66aaff")));
            }else if(item.dayType == DayType.DAY_TYPE_NOW){
                txtItem.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
            }else{
                txtItem.setBackgroundDrawable(new ColorDrawable(Color.LTGRAY));
            }
            View.OnClickListener listener = new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    switch(item.dayType){//根据类型判断应该处理的事件
                        case DAY_TYPE_FORE://跳转至前一个月
                            calendar.add(Calendar.MONTH,-1);
                            showCalendar(calendar);//显示日历数据
                            Toast.makeText(context,item.toString(),1000).show();
                            break;
                        case DAY_TYPE_NOW:
                            Toast.makeText(context,item.toString(),1000).show();
                            break;
                        case DAY_TYPE_NEXT://跳转至下一个月
                            calendar.add(Calendar.MONTH,1);
                            showCalendar(calendar);//显示日历数据
                            Toast.makeText(context,item.toString(),1000).show();
                            break;
                    }
                }
            };
            txtItem.setOnClickListener(listener);//设置日期点击事件
            return convertView;
        }
    }

}
