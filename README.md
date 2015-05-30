# CalendarWidget
Android Calendar Project！
在日历中，显示日期的格子有42个，则可以认定为一个数组，长度为42.写日历则就是如何在里面填值罢了!

一、项目中使用了蔡勒公式：
    日期格式 y-m-d，y为年份数(y>1582)，m为月份数（0<m<13），d为日数（0<d<28、29、30、31）。y、m、d为整数。例如2008-8-1，各变量分别是y=2008，m=8，d=1。 
    1、常用公式

           W = [y-1] + [(y-1)/4] - [(y-1)/100] + [(y-1)/400] + D

          式中变量说明：W为星期数，y为年份数，D为该日期在该年中的排序数；[X]为对X取整，下同。

    2、蔡勒（Zeller）公式

           W=Y+[Y/4]+[C/4]-2C+[26(M+1)/10]+d-1

        公式中的符号含义如下：

        W为星期数；C为世纪；Y为年（两位数）； M为月数（M=m（当m>2）；M=m+12（m<3））；d为日。

        相比于通用通用计算公式而言，蔡勒（Zeller）公式大大降低了计算的复杂度。
   
二、重点是如何计算上个月的那几天和下个月的那几天
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
    
