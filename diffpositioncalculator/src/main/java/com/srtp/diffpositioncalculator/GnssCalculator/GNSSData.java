package com.srtp.diffpositioncalculator.GnssCalculator;

import android.location.GnssClock;
import android.location.GnssMeasurement;
import android.location.GnssStatus;
import android.util.Log;

import java.math.BigDecimal;

public class GNSSData {
    private GnssMeasurement gnssMeasurement;
    private GnssClock gnssClock;
    private double Pseudorange;//伪距
    private double TRxNanos;
    private int PRN;
    private int SVType;
    private double WeekTimeNanos;//当前周秒余数
    private static final double c=299792458E-9;//光速 单位：m/ns
    private static final double WEEK_SEC = 604800;//周化秒
    private static final double DAY_SEC=86400;//日化秒

    public GNSSData(GnssMeasurement gnssMeasurement, GnssClock gnssClock) {
        this.gnssMeasurement = gnssMeasurement;
        this.gnssClock = gnssClock;
    }

    public double getTRxNanos() {
        double bias=0;
        if (gnssClock.hasBiasNanos())
            bias+=gnssClock.getBiasNanos();
        if (gnssClock.hasFullBiasNanos())
            bias+=gnssClock.getFullBiasNanos();
        if (gnssClock.hasLeapSecond())
            bias+=gnssClock.getLeapSecond()*1000_000_000;
        TRxNanos =gnssClock.getTimeNanos()-bias;
        return TRxNanos;
    }

    public double getPseudorange() {
        double tTx=gnssMeasurement.getReceivedSvTimeNanos()+gnssMeasurement.getTimeOffsetNanos();
        Log.d("receive time","tTx= "+tTx);
        BigDecimal tRx=new BigDecimal("0");

        int SvType=gnssMeasurement.getConstellationType();
        BigDecimal decimal_tRxNanos=BigDecimal.valueOf(getTRxNanos());
        BigDecimal decimal_weekNanos=BigDecimal.valueOf(WEEK_SEC*1e9);
        BigDecimal decimal_dayNanos=BigDecimal.valueOf(DAY_SEC*1e9);
        switch(SvType){
            case GnssStatus.CONSTELLATION_GPS:
                tRx=decimal_tRxNanos.remainder(decimal_weekNanos);//GPS系统
                break;
            case GnssStatus.CONSTELLATION_GLONASS:
                tRx=decimal_tRxNanos.remainder(decimal_dayNanos).add(BigDecimal.valueOf((3*3600)*1E9));//GLONASS系统
                break;
            case GnssStatus.CONSTELLATION_BEIDOU:
                tRx=decimal_tRxNanos.remainder(decimal_weekNanos).subtract(BigDecimal.valueOf(14E9));//BeiDou系统
                break;
            default:tRx=BigDecimal.valueOf(-1);//未定义的卫星类型
        }

        Log.d("receive time","tRx= "+tRx);
        BigDecimal decimal_deltaTime=tRx.subtract(BigDecimal.valueOf(tTx));
        Pseudorange=decimal_deltaTime.abs().multiply(BigDecimal.valueOf(c)).doubleValue();
        Log.d("receive time","pseu= "+Pseudorange);
        if (Pseudorange<1e8 && Pseudorange>1e6)return Pseudorange;//单位：米
        else return -1;
    }

    public int getPRN() {
        PRN=gnssMeasurement.getSvid();
        return PRN;
    }

    public int getSVType() {
        SVType=gnssMeasurement.getConstellationType();
        return SVType;
    }

    public static double getGPSTime(GnssClock gnssClock) {
        BigDecimal decimal_FullTimeNanos=BigDecimal.valueOf(GNSSData.getTRxNanos(gnssClock));
        BigDecimal decimal_weekNanos=BigDecimal.valueOf(WEEK_SEC*1e9);
        return Math.abs(decimal_FullTimeNanos.remainder(decimal_weekNanos).divide(BigDecimal.valueOf(1e9)).doubleValue());//获取GPStime
    }

    public static double getTRxNanos(GnssClock gnssClock) {
        double bias=0;
        if (gnssClock.hasBiasNanos())
            bias+=gnssClock.getBiasNanos();
        if (gnssClock.hasFullBiasNanos())
            bias+=gnssClock.getFullBiasNanos();
        if (gnssClock.hasLeapSecond())
            bias+=gnssClock.getLeapSecond()*1000_000_000;
        return gnssClock.getTimeNanos()-bias;
    }
    public double getDayTimeNanos() {
        double weekNumber=Math.floor((Math.abs(gnssClock.getFullBiasNanos())*1e-9)/DAY_SEC);
        return weekNumber*(DAY_SEC*1e9);
    }

}
