package com.srtp.diffpositioncalculator;

import java.util.ArrayList;

public class Epoch {
    //历元数据结构
        double GPSTIME;                  //GPS时间
        int sat_num;                     //此历元卫星数量
        ArrayList<Sat> sat=new ArrayList<>();
        double ZHD;                      //天顶对流层延迟（干）
}
