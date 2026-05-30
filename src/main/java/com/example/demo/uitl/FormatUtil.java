package com.example.demo.uitl;


import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class FormatUtil {
    private static final Pattern MAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");//邮箱格式

    private static final Pattern IP_PATTERN = Pattern.compile("([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}");//ip格式


    public boolean isMail(String mail) {
        if(mail==null)
            return false;
        return MAIL_PATTERN.matcher(mail).matches();
    }
    public boolean isIp(String ip) {
        return IP_PATTERN.matcher(ip).matches();
    }
    public String getFileformat(String fileName){
        if(fileName==null)
            return null;
        String[] formatNames=fileName.split("\\.");
        if(formatNames.length<=1)
            return null;
        String format="."+formatNames[formatNames.length-1];
        return format;
    }

    public  boolean checkNull(String... strs){
        for(String str:strs){
           if(str==null||"".equals( str))
               return false;
        }
        return true;
    }
    public boolean checkObjectNull(Object... objs) {
        for (Object obj : objs) {
            if (obj == null) {
                return false;
            }
        }
        return true;
    }
    public boolean checkPositive(Integer... numbers) {
        for (Integer number : numbers) {
            if (number <= 0) {
                return false;
            }
        }
        return true;
    }


}
