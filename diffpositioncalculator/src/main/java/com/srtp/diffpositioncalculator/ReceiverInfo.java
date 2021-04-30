package com.srtp.diffpositioncalculator;

public class ReceiverInfo {
    double APP_X = -2605410.3610;                    //文件头中的概率坐标X
    double APP_Y= 4742126.8360;                    //文件头中的概率坐标Y
    double APP_Z= 3365840.4640;                    //文件头中的概率坐标Z

    //经纬度及高程
    double B;
    double L;
    double DH;

    int LEAPSEC;                     //跳秒
}
