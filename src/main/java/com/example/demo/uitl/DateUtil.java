package com.example.demo.uitl;

import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;


@Component
public class DateUtil {
    /**
     * 获取当前时间
     * @return
     */
    public LocalDateTime getCurrentTime(){
        LocalDateTime now=LocalDateTime.now();
        return now;
    }
    /**
     * 格式化时间
     * @param localDateTime
     * @return
     */
    public String printTime(LocalDateTime localDateTime){
       SimpleDateFormat sdf=new SimpleDateFormat("yyyy-mm-dd HH:mm:ss");
       return sdf.format( localDateTime);

    }

     public Calendar getCalendar(int y, int m, int d){
        Calendar calendar=Calendar.getInstance();
        calendar.set(y,m-1,d);
        return calendar;
    }
}
