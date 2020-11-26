package com.srtp.diffpositioncalculator.GnssCalculator;

public class Sat {
        double pseudo;                   //卫星的观测值 20
        String sattype="G";                  //卫星的类型
        int numofsat;                    //卫星的序号（PRN）
        double GPSTIME;
        double ttlsec;


        double TGD;                      //TGD改正
        double a0;                       //钟差改正系数1
        double a1;                       //钟差改正系数2
        double a2;                       //钟差改正系数3

        double tk;                       //距离星历节点的外推时间

        double deltt;
        double POS_X;                    //卫星位置X
        double POS_Y;                    //卫星位置Y
        double POS_Z;                    //卫星位置Z

        double r;                        //卫星与测站间距离
        double A;                        //方位角
        double E;                        //高度角

        //int posk;                        //星历历元标志
        int system;                      //所属系统
        int judge_use;                   //可用性标志

        double xdl_t;                    //相对论效应的影响（时间 s）
        double trop;                     //对流层延迟1

        //误差项
        double Sat_clock;                //卫星钟差
        double Trop_Delay;               //对流层延迟2
        double Trop_Map;                 //对流层湿延迟投影
        double Relat;                    //相对论
        double Sagnac;                   //地球自转
        double Tide_Effect;              //潮汐效应
        double Antenna_Height;
        double Sat_Antenna;              //卫星天线相位中心改正
        double OffsetL1;                 //L1相位偏差（PCO+PCV）
        double OffsetL2;                 //L2相位偏差（PCO+PCV）
        double Windup;                   //相位缠绕
    }
