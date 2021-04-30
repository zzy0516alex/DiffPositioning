package com.srtp.diffpositioncalculator;

public class JudgeEphemeris {
    static  int[] dinmth= { 0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };

//    public static void main([] args) {
//        obs_epoch epo=new obs_epoch();
//        GetGPSTime(epo);
//    }

    public static double GetGPSTime(NavigationMessageRaw epoch)//()
    {
        int dayofw, yr, ttlday, m, weekno;
        double gpstime;
        double dayofy = 0;

        //异常年份处理
        if (epoch.year < 2000)
            epoch.year = epoch.year + 2000;

        if (epoch.year > 4000)
            epoch.year = epoch.year - 2000;

        if (epoch.year < 1981 || epoch.month < 1 || epoch.month > 12 || epoch.day < 1 || epoch.day > 31)
            weekno = 0;

        if (epoch.month == 1)
            dayofy = epoch.day;
        else
        {
            dayofy = 0;
            for (m = 1; m <= (epoch.month - 1); m++)
            {
                dayofy += dinmth[m];
                if (m == 2)
                {
                    if (epoch.year % 4 == 0 && epoch.year % 100 != 0 || epoch.year % 400 == 0)
                        dayofy += 1;
                }
            }
            dayofy += epoch.day;
        }

        ttlday = 360;
        for (yr = 1981; yr <= (epoch.year - 1); yr++)
        {
            ttlday += 365;
            if (yr % 4 == 0 && yr % 100 != 0 || yr % 400 == 0)
                ttlday += 1;
        }
        ttlday += dayofy;
        weekno = ttlday / 7;                                                 //整周数
        dayofw = ttlday - 7 * weekno;                                        //不足一周的天数
        gpstime = epoch.hour * 3600 + epoch.minute * 60 + epoch.second + dayofw * 86400;       //距离周日零时的秒数
        return gpstime;
    }
}

