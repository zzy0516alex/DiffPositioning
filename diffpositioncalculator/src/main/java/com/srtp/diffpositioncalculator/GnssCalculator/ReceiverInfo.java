package com.srtp.diffpositioncalculator.GnssCalculator;

public class ReceiverInfo {
    String marker_name;              //测站名称
    double APP_X = -2605410.3610;                    //文件头中的概率坐标X
    double APP_Y= 4742126.8360;                    //文件头中的概率坐标Y
    double APP_Z= 3365840.4640;                    //文件头中的概率坐标Z

    //经纬度及高程
    double B;
    double L;
    double DH;

//    String ANT_TYPE;                 //接收机天线类型
//    double []PCO1=new double[3];                  //根据接收机天线类型通过搜索天线列表匹配的POC改正（频率1）
//    double []PCO2=new double[3];                  //根据接收机天线类型通过搜索天线列表匹配的POC改正（频率2）
//    String REC_TYPE;                 //接收机类型
//    double H, E, N;                  //文件头中的天线偏置
//    double []FIRST_TIME=new double[6];            //开始时刻
//    double []LAST_TIME=new double[6];             //结束时刻
//    double INTERVAL;                 //采样间隔
    int LEAPSEC;                     //跳秒
}
