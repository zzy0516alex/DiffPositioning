package com.srtp.diffpositioncalculator;

import android.location.GnssStatus;

public class Sat {
        public enum SatType{GPS,BDS}
        double pseudo;                   //卫星的伪距
        SatType sattype;                  //卫星的类型
        int PRN;                    //卫星的序号（PRN）

        double POS_X;                    //卫星位置X
        double POS_Y;                    //卫星位置Y
        double POS_Z;                    //卫星位置Z

        double r;                        //卫星与测站间距离
        double A;                        //方位角
        double E;                        //高度角


        double xdl_t;                    //相对论效应的影响（时间 s）
        double trop;                     //对流层延迟1

        //误差项
        double Sat_clock;                //卫星钟差
        double Trop_Delay;               //对流层延迟2
        double Trop_Map;                 //对流层湿延迟投影
        double Sagnac;                   //地球自转
        double E_weight =0;
        double PRC;

        public boolean equals(SatType type,int prn){
            return this.sattype==type && this.PRN==prn;
        }

        public static SatType getSatType(int svTypeNum){
                switch(svTypeNum){
                        case GnssStatus.CONSTELLATION_GPS:
                                return Sat.SatType.GPS;
                        case GnssStatus.CONSTELLATION_BEIDOU:
                                return Sat.SatType.BDS;
                        default:
                                return SatType.GPS;
                }
        }
        public static String satType_toString(SatType satType){
                switch(satType){
                        case GPS:return "G";
                        case BDS:return "B";
                    default:return "null";
                }
        }

    }
