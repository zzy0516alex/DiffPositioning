package com.srtp.diffpositioncalculator.GnssCalculator;

//GPS和BDS星历的数据结构（文件名后缀***.**p）
public class NavigationMessageRaw
{
        int PRN;
        int year;
        int month;
        int day;
        int hour;
        int minute;
        double second;
        double GPSTIME;
        double TTLSEC;
        double a0;
        double a1;
        double a2;
        double IDOE;
        double Crs;
        double delta_n;
        double M0;
        double Cuc;
        double e;
        double Cus;
        double sqrtA;
        double TOE;
        double Cic;
        double OMEGA;
        double Cis;
        double i0;
        double Crc;
        double w;
        double OMEGA_DOT;
        double i_DOT;
        double code_L2;
        double gps_week;
        double mark_code_L2;
        double pre_sat;//精度
        double hel_sat;//健康状态
        double TGD;
        double IODC;
        double time_sig_send;//电文发送时刻
        int doy;  //年积日（备用）
        double PRC;//改正数

        public NavigationMessageRaw(int PRN, int year, int month, int day, int hour, int minute, double second, double GPSTIME, double a0, double a1, double a2, double IDOE, double crs, double delta_n, double m0, double cuc, double e, double cus, double sqrtA, double TOE, double cic, double OMEGA, double cis, double i0, double crc, double w, double OMEGA_DOT, double i_DOT, double PRC) {
                this.PRN = PRN;
                this.year = year;
                this.month = month;
                this.day = day;
                this.hour = hour;
                this.minute = minute;
                this.second = second;
                this.GPSTIME = GPSTIME;
                this.a0 = a0;
                this.a1 = a1;
                this.a2 = a2;
                this.IDOE = IDOE;
                Crs = crs;
                this.delta_n = delta_n;
                M0 = m0;
                Cuc = cuc;
                this.e = e;
                Cus = cus;
                this.sqrtA = sqrtA;
                this.TOE = TOE;
                Cic = cic;
                this.OMEGA = OMEGA;
                Cis = cis;
                this.i0 = i0;
                Crc = crc;
                this.w = w;
                this.OMEGA_DOT = OMEGA_DOT;
                this.i_DOT = i_DOT;
                this.PRC = PRC;
        }
};
