package com.srtp.diffpositioncalculator;

import android.content.Context;

import com.srtp.diffpositioncalculator.Utils.FileUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

import static java.lang.Math.abs;
import static java.lang.Math.atan;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.tan;

public class SinglePositioning {

    private static final double C_light=299792458.458;
    private static final double  PI=3.14159265359;
    private static int n=0;

    public static boolean Can_POS(int satNum,ArrayList<Sat> sats){
        int legalSat=0;
        for (int i = 0; i < sats.size(); i++) {
            Sat sat = sats.get(i);
            if (sat.equals(Sat.SatType.BDS,5))continue;
            legalSat++;
        }
        return legalSat>5;
    }

    public static void Get_SPP_POS(Epoch epoch, CMatrix XYZ,boolean use_PRC)
    {

        boolean hasGPS = false;
        boolean hasBDS = false;

        //存储BL矩阵信息，不超过40颗卫星
        CMatrix BL=new CMatrix(40, 5);

        int sat_index = 0;

        for (int i = 0; i < epoch.sat.size(); i++)
        {

            Sat tempSat =epoch.sat.get(i);
            //补齐矩阵最后一列
            if (tempSat.sattype == Sat.SatType.GPS){
                hasGPS=true;
                BL.p[sat_index][4]=1.0;
            }
            if (tempSat.sattype == Sat.SatType.BDS){
                hasBDS=true;
                BL.p[sat_index][4]=2.0;
            }

            //计算卫星到基站距离
            double Length = CalculateHeightAngle(XYZ, BL, sat_index, tempSat);

            //误差计算
            double error_left = CalculateErrorLeft(XYZ, tempSat, Length,0)+tempSat.PRC*(use_PRC?1:0);

            BL.p[sat_index][3] = error_left;

            sat_index++;
        }

        //大于5颗表示可以定位
        if (sat_index > 5) {
            //定义B矩阵，存储高度角余弦值
            int B_col;
            if (hasBDS && hasGPS) B_col = 5;
            else
                B_col = 4;
            CMatrix B = new CMatrix(sat_index, B_col);
            //定义L矩阵,存储残差
            CMatrix L = new CMatrix(sat_index, 1);
            //定义Delta XYZ矩阵,存储定位迭代增量
            CMatrix Delta_XYZ;

            for (int i = 0; i < sat_index; i++) {
                B.p[i][0] = BL.p[i][0];
                B.p[i][1] = BL.p[i][1];
                B.p[i][2] = BL.p[i][2];

                L.p[i][0] = BL.p[i][3];

                //补齐钟差参数列，补1
                if (tempEpoch.sat.get(i).sattype== Sat.SatType.GPS)B.p[i][3] = 1;
                else B.p[i][3] = 0;
                if (hasBDS && hasGPS) {
                    if (tempEpoch.sat.get(i).sattype== Sat.SatType.BDS)B.p[i][4] = 1;
                    else B.p[i][4] = 0;
                }
            }
            //最小二乘法求解delta xyz矩阵
            Delta_XYZ = ((B.T()).Multiple(B)).InvertGaussJordan().Multiple(B.T()).Multiple(L);

            XYZ.p[0][0] += Delta_XYZ.p[0][0];
            XYZ.p[1][0] += Delta_XYZ.p[1][0];
            XYZ.p[2][0] += Delta_XYZ.p[2][0];
        }
    }

    private static double CalculateErrorLeft(CMatrix XYZ, Sat tempSat, double length,double error_tropDelay) {
        double error_satClock = tempSat.Sat_clock * C_light;
        double error_relativity = tempSat.xdl_t;
        double we = 7.2921151467e-5;
        if(tempSat.sattype== Sat.SatType.BDS) we=7.292115e-5;
        tempSat.Sagnac = we * (tempSat.POS_X * XYZ.p[1][0] - tempSat.POS_Y * XYZ.p[0][0]) / C_light;
        double error_sagnac = tempSat.Sagnac;

        //总误差
        //notice:-error_satClock
        double total_error = -error_satClock - error_relativity + error_sagnac + error_tropDelay;

        //残余误差
        return -(length - tempSat.pseudo + total_error);
    }

    private static double CalculateHeightAngle(CMatrix XYZ, CMatrix BL, int sat_index, Sat tempSat) {
        double Length = Math.sqrt(Math.pow(tempSat.POS_X - XYZ.p[0][0], 2) + Math.pow(tempSat.POS_Y - XYZ.p[1][0], 2) + Math.pow(tempSat.POS_Z - XYZ.p[2][0], 2));

        double CosX = (tempSat.POS_X - XYZ.p[0][0]) / Length;
        double CosY = (tempSat.POS_Y - XYZ.p[1][0]) / Length;
        double CosZ = (tempSat.POS_Z - XYZ.p[2][0]) / Length;

        BL.p[sat_index][0] = -CosX;
        BL.p[sat_index][1] = -CosY;
        BL.p[sat_index][2] = -CosZ;
        return Length;
    }

    //函数功能：初步单点定位功能（考虑高度角定权）
    public static void Get_SPP_POS_Ele(Epoch epoch, CMatrix XYZ,boolean use_PRC)
    {
        boolean hasGPS = false;
        boolean hasBDS = false;

        //存储BL矩阵信息，不超过40颗卫星
        CMatrix BL=new CMatrix(40, 6);

        int sat_index = 0;

        for (int i = 0; i < epoch.sat.size(); i++)
        {
            Sat tempSat =epoch.sat.get(i);
            //高度角小于15度不算
            if (tempSat.E < 15.0){
                epoch.sat.remove(tempSat);
                continue;
            }
            //补齐矩阵最后一列
            if (tempSat.sattype == Sat.SatType.GPS){
                hasGPS=true;
                BL.p[sat_index][4]=1.0;
            }
            if (tempSat.sattype == Sat.SatType.BDS){
                hasBDS=true;
                BL.p[sat_index][4]=2.0;
            }

            //计算卫星到基站距离
            double Length = CalculateHeightAngle(XYZ, BL, sat_index, tempSat);

            //误差计算
            double error_left = CalculateErrorLeft(XYZ, tempSat, Length,tempSat.Trop_Delay)+tempSat.PRC*(use_PRC?1:0);

            BL.p[sat_index][3] = error_left;

            //存入高度角
            BL.p[sat_index][5]=tempSat.E;

            sat_index++;
        }

        //大于5颗表示可以定位
        if (sat_index > 5) {
            //定义B矩阵，存储高度角余弦值
            int B_col;
            if (hasBDS && hasGPS) B_col = 5;
            else
                B_col = 4;
            int rows=epoch.sat.size();
            CMatrix B = new CMatrix(rows, B_col);
            //定义L矩阵,存储残差
            CMatrix L = new CMatrix(rows, 1);
            //定义R矩阵,为对角阵
            CMatrix R=new CMatrix(rows,rows);
            //定义Delta XYZ矩阵,存储定位迭代增量
            CMatrix Delta_XYZ;

            for (int i = 0; i < epoch.sat.size(); i++) {
                B.p[i][0] = BL.p[i][0];
                B.p[i][1] = BL.p[i][1];
                B.p[i][2] = BL.p[i][2];

                L.p[i][0] = BL.p[i][3];

                double elp = Math.pow(Math.sin(Math.toRadians(BL.p[i][5])),2);
                R.p[i][i] = 0.09 + 0.09 / elp;
                epoch.sat.get(i).E_weight =elp;
                //补齐钟差参数列，补1
                if (epoch.sat.get(i).sattype== Sat.SatType.GPS)B.p[i][3] = 1;
                else B.p[i][3] = 0;
                if (hasBDS && hasGPS) {
                    if (epoch.sat.get(i).sattype== Sat.SatType.BDS)B.p[i][4] = 1;
                    else B.p[i][4] = 0;
                }
            }
            //最小二乘法求解delta xyz矩阵
            CMatrix P = R.InvertGaussJordan();
            Delta_XYZ = ((B.T()).Multiple(P).Multiple(B)).InvertGaussJordan().Multiple(B.T()).Multiple(P).Multiple(L);
            //Delta_XYZ = ((B.T()).Multiple(B)).InvertGaussJordan().Multiple(B.T()).Multiple(L);


            XYZ.p[0][0] += Delta_XYZ.p[0][0];
            XYZ.p[1][0] += Delta_XYZ.p[1][0];
            XYZ.p[2][0] += Delta_XYZ.p[2][0];
        }

    }
//传入的MessageRaw为特定GPSTime的一系列卫星的星历文件,GnssData为当前GPSTime的手机和基站共同观测到的卫星数据（包括伪距）
    private static Epoch tempEpoch;
    private static Context context;

    public static void setContext(Context context) {
        SinglePositioning.context = context;
    }

    public static double[] Position_Calculator(ArrayList<NavigationMessageRaw> navigationMessageRaws, ArrayList<SatSentMsg>satSentMsgs, double gpstime, int sat_num,boolean use_PRC)
    {
        //存储GPS和BDS观测数据类型：BDS-2与BDS-3一致
        double[] XYZ = { 0,0,0 };

        //初始化概率坐标
        ReceiverInfo receiverPOS=new ReceiverInfo();

        //文件头概率坐标
        XYZ[0] = receiverPOS.APP_X;
        XYZ[1] = receiverPOS.APP_Y;
        XYZ[2] = receiverPOS.APP_Z;

        receiverPOS.LEAPSEC = 1;

        //迭代初值
//        double init_X = 0;
//        double init_Y = 0;
//        double init_Z = 0;

        //初始化当前历元
        tempEpoch=new Epoch();
        tempEpoch.GPSTIME = gpstime;
        tempEpoch.sat_num = sat_num;

        //将卫星存入当前历元
        for (int k = 0; k<tempEpoch.sat_num; k++)
        {
            Sat tempsat=new Sat();
            SatSentMsg currentSatMsg=satSentMsgs.get(k);
            for(int j=0;j<navigationMessageRaws.size();j++)
            {
                NavigationMessageRaw currentNavMsg=navigationMessageRaws.get(j);
                if (currentSatMsg.equals(currentNavMsg)) {
                    tempsat.pseudo = currentSatMsg.getPseudorange();
                    tempsat.PRN=currentNavMsg.PRN-((currentNavMsg.PRN<100)?0:100);
                    tempsat.sattype=Sat.getSatType(currentSatMsg.getSVType());
                    tempsat.PRC=currentNavMsg.PRC;
                    if (tempsat.equals(Sat.SatType.BDS,3))continue;
                    double SpreadTime = tempsat.pseudo / C_light;
                    //补全tempSat[posX,posY,posZ,sat_clock,xdl,sagnac]
                    Calc_Eph_GCEJ(tempEpoch.GPSTIME, SpreadTime, tempsat, XYZ, navigationMessageRaws.get(j));
                    //将卫星存入当前历元
                    tempEpoch.sat.add(tempsat);
                    break;
                }
            }
        }

        //初始历元进行伪距单点定位获得概略坐标，更新XYZ:

        //用于暂存上一次的计算值
        double LastX;
        double LastY;
        double LastZ;

        //建立一个存储xyz坐标的3维向量
        CMatrix Pos=new CMatrix(3, 1);

        //从第二个历元开始，不用第一次迭代的结果
//            Pos.p[0][0] = init_X;
//            Pos.p[1][0] = init_Y;
//            Pos.p[2][0] = init_Z;
            Pos.p[0][0] = XYZ[0];
            Pos.p[1][0] = XYZ[1];
            Pos.p[2][0] = XYZ[2];

            LastX = Pos.p[0][0];
            LastY = Pos.p[1][0];
            LastZ = Pos.p[2][0];

        //判断该历元是否满足SPP条件（获取POS_IF）
        boolean canDoPOS = Can_POS(tempEpoch.sat_num,tempEpoch.sat);

        if (/*Math.abs(LastX) < 1.0 && Math.abs(LastY) < 1.0 && Math.abs(LastZ) < 1.0 && */canDoPOS)
        {
            //进行第一次单点定位，求粗略坐标
            do
            {
                LastX = Pos.p[0][0];
                LastY = Pos.p[1][0];
                LastZ = Pos.p[2][0];

                Get_SPP_POS(tempEpoch, Pos,use_PRC);

            } while (Math.abs(LastX - Pos.p[0][0]) >= 0.1 || Math.abs(LastY - Pos.p[1][0]) >= 0.1 || Math.abs(LastZ - Pos.p[2][0]) >= 0.1);
        }

        //表示无法进行单点定位，也就无法获得初始坐标，暂时标为不可用
        if (!canDoPOS)
        {
            //将初始坐标置为0
            Pos.p[0][0] = 0;
            Pos.p[1][0] = 0;
            Pos.p[2][0] = 0;

            tempEpoch.ZHD = 0;

            //用新的坐标更新高度角、方位角、对流层等信息
            for (int k = 0; k < tempEpoch.sat.size(); k++)
            {
                //全部置0
                Sat tempsat;
                tempsat = tempEpoch.sat.get(k);
                tempsat.E = 0;
                tempsat.A = 0;
                tempsat.Trop_Delay = 0;
                tempsat.Trop_Map = 0;
                tempsat.Sagnac = 0;
                tempEpoch.sat.set(k,tempsat);
            }
        }

        else
        {
            //更新经纬度,放入第一次粗略计算坐标
            XYZ[0] = LastX;
            XYZ[1] = LastY;
            XYZ[2] = LastZ;

            double []BLH = new double [3];
            OnXYZtoBLH(LastX, LastY, LastZ, BLH);

            receiverPOS.B = BLH[0];
            receiverPOS.L = BLH[1];
            receiverPOS.DH = BLH[2];
            CMatrix HH=new CMatrix(3, 3);

            HH.p[0][0] = -Math.sin(receiverPOS.B)*Math.cos(receiverPOS.L);
            HH.p[0][1] = -Math.sin(receiverPOS.B)*Math.sin(receiverPOS.L);
            HH.p[0][2] = Math.cos(receiverPOS.B);
            HH.p[1][0] = -Math.sin(receiverPOS.L);
            HH.p[1][1] = Math.cos(receiverPOS.L);
            HH.p[1][2] = 0;
            HH.p[2][0] =Math.cos(receiverPOS.B)*Math.cos(receiverPOS.L);
            HH.p[2][1] = Math.cos(receiverPOS.B)*Math.sin(receiverPOS.L);
            HH.p[2][2] = Math.sin(receiverPOS.B);

            //考虑对流层再进行一次坐标解算,添加了高度角,方位角,对流层改正,更新了sagnac改正
            for (int k = 0; k < tempEpoch.sat.size(); k++)
            {
                Sat tempsat;
                tempsat = tempEpoch.sat.get(k);

                //计算高度角和方位角
                CalSatEA(HH, XYZ, tempsat);

                //计算对流层改正
                tempsat.Trop_Delay= TroposphereModel.tropomodel(BLH,tempsat.E*PI/180.0,0.5);
                tempsat.Trop_Map=0;

                //计算Sagnac效应改正
                double we = 7.2921151467e-5;
                tempsat.Sagnac = we * (tempsat.POS_X * XYZ[1] - tempsat.POS_Y * XYZ[0]) / C_light;
                tempEpoch.sat.set(k,tempsat);
            }

            //考虑高度角方位角，重新计算坐标
            do
            {
                LastX = Pos.p[0][0];
                LastY = Pos.p[1][0];
                LastZ = Pos.p[2][0];

                Get_SPP_POS_Ele(tempEpoch, Pos,use_PRC);

            } while (Math.abs(LastX - Pos.p[0][0]) >= 0.1 || Math.abs(LastY - Pos.p[1][0]) >= 0.1 || Math.abs(LastZ - Pos.p[2][0]) >= 0.1);

            XYZ[0] = Pos.p[0][0];
            XYZ[1] = Pos.p[1][0];
            XYZ[2] = Pos.p[2][0];

            //用新的坐标更新高度角、方位角、对流层等信息
            for (int k = 0; k < tempEpoch.sat.size(); k++)
            {
                Sat tempsat;
                tempsat = tempEpoch.sat.get(k);
                //计算高度角和方位角
                CalSatEA(HH, XYZ, tempsat);

                //计算对流层
                tempsat.Trop_Delay= TroposphereModel.tropomodel(BLH,tempsat.E*PI/180.0,0.5);
                tempsat.Trop_Map=0;
                //计算Sagnac效应改正
                double we = 7.2921151467e-5;
                tempsat.Sagnac = we * (tempsat.POS_X * XYZ[1] - tempsat.POS_Y * XYZ[0]) / C_light;
                tempEpoch.sat.set(k,tempsat);
            }
        }
        LastX = Pos.p[0][0];
        LastY = Pos.p[1][0];
        LastZ = Pos.p[2][0];
        double []final_BLH = new double [3];
        OnXYZtoBLH(LastX,LastY,LastZ,final_BLH);

        double []BLHinDegree=new double[3];
        BLHinDegree[0]=final_BLH[0]/PI*180;
        BLHinDegree[1]=final_BLH[1]/PI*180;
        BLHinDegree[2]=final_BLH[2];

        //输出记录`
        StringBuilder logger=new StringBuilder("====================== GPS_TIME  "+tempEpoch.GPSTIME+"======================\n\n");
        FileUtil.table_writer(logger,"%-22s","PRN","类型","伪距","X","Y","Z","高度角定权","钟差","相对论","自转","对流层");
        for (Sat sat :
                tempEpoch.sat) {
            if(sat.E < 15)continue;
            FileUtil.table_writer(logger,"%-22s",
                    String.valueOf(sat.PRN),
                    Sat.satType_toString(sat.sattype),
                    String.valueOf(sat.pseudo),
                    String.valueOf(sat.POS_X),
                    String.valueOf(sat.POS_Y),
                    String.valueOf(sat.POS_Z),
                    String.valueOf(sat.E_weight),
                    String.valueOf(sat.Sat_clock*C_light),
                    String.valueOf(sat.xdl_t),
                    String.valueOf(sat.Sagnac),
                    String.valueOf(sat.Trop_Delay));
        }
        logger.append(String.format("\n B = %s  L = %s  H = %s \n",String.valueOf(BLHinDegree[0]),String.valueOf(BLHinDegree[1]),String.valueOf(BLHinDegree[2])));
        if (context!=null)FileUtil.WriteTXT(context.getExternalFilesDir(null)+"/PosData.txt",logger.toString(),true);
        return BLHinDegree;
    }

    public static void OnXYZtoBLH(double X, double Y, double Z, double[] BLH)
    {
        double a = 6378137.000, e = 0.00669437999014132;
        double R = sqrt(X*X + Y*Y);
        double B0 = atan2(Z, R);
        double N;
        double B, L, H;

        L = atan2(Y, X);

        while (true)
        {
            N = a / sqrt(1 - e * sin(B0) * sin(B0));
            B = atan2(Z + N * e * sin(B0), R);
            H = R / cos(B) - N;

            if (abs(B - B0) < 1e-12)
                break;
            B0 = B;
        }

        BLH[0] = B;
        BLH[1] = L;
        BLH[2] = H;

    }
    //函数功能：获取卫星的高度角和方位角
    private static void CalSatEA(CMatrix HH, double[] StaXYZ, Sat sat)
    {
        if (HH.Row != 3 || HH.Col != 3) {
            throw new IllegalArgumentException("输入矩阵行列必须为3");
        }

        //计算高度角和方位角
        CMatrix DeltXYZ;
        CMatrix Sta2Sat;
        Sta2Sat=new CMatrix(3, 1);

        Sta2Sat.setP_singleNum(0,0,sat.POS_X - StaXYZ[0]);
        Sta2Sat.setP_singleNum(1,0,sat.POS_Y - StaXYZ[1]);
        Sta2Sat.setP_singleNum(2,0,sat.POS_Z - StaXYZ[2]);

        DeltXYZ = HH.Multiple(Sta2Sat) ;
        sat.A = atan2(DeltXYZ.GetNum(1,0), DeltXYZ.GetNum(0,0));
        sat.A = sat.A * 180 / PI;

        if (sat.A < 0)
        {
            sat.A = sat.A + 360;
        }

        sat.E = atan(DeltXYZ.GetNum(2,0) / sqrt(pow(DeltXYZ.GetNum(0,0), 2) + pow(DeltXYZ.GetNum(1,0), 2)));
        sat.E = sat.E * 180 / PI;
    }

    //函数功能：根据匹配到的星历进行单个卫星位置计算（GPS和BDS）

    /**
     *
     * @param GPSTIME
     * @param rclk 传播时间
     * @param sat 当前卫星
     * @param XYZ 接收机坐标,用于计算sagnac效应
     * @param EpochNG 当前卫星星历
     */
    private static void Calc_Eph_GCEJ(double GPSTIME, double rclk, Sat sat, double[] XYZ, NavigationMessageRaw EpochNG)
    {
        switch(sat.sattype){
            case GPS:
                break;
            case BDS:
                GPSTIME-=14;
                break;
            default:
        }

        double we = 7.2921151467e-5;
        double GM = 3.9860050e14;//万有引力常数*地球质量

        BigDecimal n0_BD,n_BD, delta_t_BD, Mk_BD, ek1_BD, ek2_BD, Ek_BD, Vk_BD, fk_BD;
        BigDecimal delta_u_BD, delta_r_BD, delta_i_BD, uk_BD, rk_BD, ik_BD, xk_BD, yk_BD, Lk_BD;
        double n0, n, tk, Mk, ek1, ek2, Ek, Vk, fk;
        double deltau, deltar, deltai, uk, rk, ik, xk, yk, Lk;
        //星历时间，15min更新
        EpochNG.GPSTIME=JudgeEphemeris.GetGPSTime(EpochNG);

        //计算平均角速度
        BigDecimal sqrtA_BD=new BigDecimal(String.valueOf(EpochNG.sqrtA));
        BigDecimal delta_n_BD=new BigDecimal(String.valueOf(EpochNG.delta_n));
        BigDecimal sqrtGM_BD=new BigDecimal(String.valueOf(sqrt(GM)));
        n0_BD=sqrtGM_BD.divide(sqrtA_BD.pow(3),16, RoundingMode.HALF_EVEN);
        n_BD=n0_BD.add(delta_n_BD);
        //n0 = sqrt(GM) / pow(EpochNG.sqrtA, 3);
        //n = n0 + EpochNG.delta_n;

        //delta t
        tk = GPSTIME - EpochNG.GPSTIME;
        if (tk > 302400) tk -= 604800.0;
        else if (tk < -302400) tk += 604800.0;
        delta_t_BD=new BigDecimal(String.valueOf(tk));

        //sat clock
        BigDecimal a0=new BigDecimal(String.valueOf(EpochNG.a0));
        BigDecimal a1=new BigDecimal(String.valueOf(EpochNG.a1));
        BigDecimal a2=new BigDecimal(String.valueOf(EpochNG.a2));
        BigDecimal sat_clock_BD=a0.add(a1.multiply(delta_t_BD)).add(a2.multiply(delta_t_BD.pow(2)));
        sat.Sat_clock=sat_clock_BD.doubleValue();
        //sat.Sat_clock = EpochNG.a0 + EpochNG.a1 * tk + EpochNG.a2 * pow(tk, 2);

        delta_t_BD=delta_t_BD.subtract(sat_clock_BD).subtract(new BigDecimal(String.valueOf(rclk)));
        //tk = tk - sat.Sat_clock - rclk;

        //求平近点角
        BigDecimal M0_BD=new BigDecimal(String.valueOf(EpochNG.M0));
        Mk_BD=M0_BD.add(delta_t_BD.multiply(n_BD));
        //Mk = EpochNG.M0 + n*tk;

        //计算偏近点角
        BigDecimal e_BD=new BigDecimal(String.valueOf(EpochNG.e));
        ek1_BD=Mk_BD;
        //ek1 = Mk;
        do
        {
            BigDecimal sin_ek1=new BigDecimal(String.valueOf(sin(ek1_BD.doubleValue())));
            ek2_BD=Mk_BD.add(e_BD.multiply(sin_ek1));
            //ek2 = Mk + EpochNG.e*sin(ek1);
            if (abs(ek2_BD.doubleValue() - ek1_BD.doubleValue()) <= 1.0e-13)  break;
            ek1_BD=ek2_BD;
            //ek1 = ek2;
        } while (true);

        //计算真近点角
        Ek_BD=ek1_BD;
        //Ek = ek1;
        //Vk_BD=
        Vk = 2 * atan(sqrt((1.0 + EpochNG.e) / (1.0 - EpochNG.e))*tan(Ek_BD.doubleValue() / 2));
        fk = Vk + EpochNG.w;//计算升交距角

        //摄动改正改正角
        deltau = EpochNG.Cuc * cos(2.0*fk) + EpochNG.Cus * sin(2.0*fk);
        deltar = EpochNG.Crc * cos(2.0*fk) + EpochNG.Crs * sin(2.0*fk);
        deltai = EpochNG.Cic * cos(2.0*fk) + EpochNG.Cis * sin(2.0*fk);

        //继续修正一次
        uk = fk + deltau;
        rk = EpochNG.sqrtA * EpochNG.sqrtA * (1.0 - EpochNG.e*cos(Ek_BD.doubleValue())) + deltar;
        ik = EpochNG.i0 + deltai + EpochNG.i_DOT * tk;

        //卫星在轨道面坐标系中的坐标
        xk = rk * cos(uk);
        yk = rk * sin(uk);

        //升交点经度
        Lk = EpochNG.OMEGA + (EpochNG.OMEGA_DOT - we)*tk - we* EpochNG.TOE;

        sat.POS_X = xk * cos(Lk) - yk * cos(ik) * sin(Lk);
        sat.POS_Y = xk * sin(Lk) + yk * cos(ik) * cos(Lk);
        sat.POS_Z = yk * sin(ik);

        sat.xdl_t = -2 * sqrt(GM) * EpochNG.e * EpochNG.sqrtA * sin(Ek_BD.doubleValue()) / C_light;

        //计算地球自转效应:粗略解算
        sat.Sagnac = we * (sat.POS_X * XYZ[1] - sat.POS_Y * XYZ[0]) / C_light;
    }
}
