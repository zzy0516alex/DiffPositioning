package com.srtp.diffpositioncalculator.GnssCalculator;

import java.util.ArrayList;

public class Obs_epoch {
    //O文件历元数据结构
        int liyuan_num;                  //历元序号
        double GPSTIME;                  //GPS时间
        double ttlsec;                   //距离GPS时间原点的总时间
        int dayofy;                      //年积日，在对流层计算的时候用到
        int sat_num;                     //此历元卫星数量
        ArrayList<Sat> sat;
        double ZHD;                      //天顶对流层延迟（干）
        double []RClk;                  //接收机钟差，G/C/R/E/S/J 8
        int []JClk;                     //判断接收机钟差，G/C/R/E/S/J 8
        double posX;
        double posY;
        double posZ;

        boolean ifOK;                       //表征历元初始单点定位是否解算成功

}
