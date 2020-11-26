package com.srtp.diffpositioncalculator.GnssCalculator;

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

    public static void inti_Sat(Sat tempSat)
    {
        tempSat.numofsat = 0;
        tempSat.pseudo=0;
        tempSat.GPSTIME = 0;
        tempSat.ttlsec = 0;
        tempSat.TGD = 0;
        tempSat.a0 = 0;
        tempSat.a1 = 0;
        tempSat.a2 = 0;
        tempSat.tk = 0;
        tempSat.deltt = 0.0;
        tempSat.POS_X = 0.0;
        tempSat.POS_Y = 0.0;
        tempSat.POS_Z = 0.0;
        tempSat.r = 0;
        tempSat.A = 0;
        tempSat.E = 0;
        //误差改正
        tempSat.judge_use = 0;
        tempSat.Sat_clock = 0;
        tempSat.Trop_Delay = 0;
        tempSat.Trop_Map = 0;
        tempSat.Relat = 0;
        tempSat.Sagnac = 0;
        tempSat.Tide_Effect = 0;
        tempSat.Antenna_Height = 0;
        tempSat.Sat_Antenna = 0;
        tempSat.OffsetL1 = 0;
        tempSat.OffsetL2 = 0;
        tempSat.Windup = 0;
    }

    public static Boolean Get_SPP_POS(Obs_epoch tepoch, CMatrix XYZ)
    {

        boolean GPS_Y = false;

        //存储BL矩阵信息，不超过40颗卫星
        CMatrix BL=new CMatrix(40, 5);

        int tw = 0;

        for (int i = 0; i < tepoch.sat_num; i++)
        {
            Sat tempSat = new Sat() ;
            tempSat=tepoch.sat.get(i);
            GPS_Y = true;
            BL.p[tw][4] = 1.0;

            double Length = Math.sqrt(Math.pow(tempSat.POS_X - XYZ.p[0][0], 2) + Math.pow(tempSat.POS_Y - XYZ.p[1][0], 2) + Math.pow(tempSat.POS_Z - XYZ.p[2][0], 2));

            double Cof1 = (tempSat.POS_X - XYZ.p[0][0]) / Length;
            double Cof2 = (tempSat.POS_Y - XYZ.p[1][0]) / Length;
            double Cof3 = (tempSat.POS_Z - XYZ.p[2][0]) / Length;

            BL.p[tw][0] = -Cof1;
            BL.p[tw][1] = -Cof2;
            BL.p[tw][2] = -Cof3;

            //参考站1-非参考卫星
            double error_satclock1 = tempSat.Sat_clock * C_light;
            double error_relat1 = tempSat.xdl_t;
            double we = 7.2921151467e-5;
            tempSat.Sagnac = we * (tempSat.POS_X * XYZ.p[1][0] - tempSat.POS_Y * XYZ.p[0][0]) / C_light;

            //Sagnac效应也需要改正
            double error_sagnac1 = tempSat.Sagnac;
            double error_tgd = tempSat.TGD;
            double error_antenna_height1 = tempSat.Antenna_Height;
            double error_sat_antenna1 = tempSat.Sat_Antenna;
            double test_geo1 = -error_satclock1 - error_relat1 + error_sagnac1 + error_tgd - error_sat_antenna1;

            double test_P11;
            test_P11 = -(Length - tempSat.pseudo + test_geo1);
            BL.p[tw][3] = test_P11;

            tw++;
        }

        //大于5颗表示可以定位
        if (tw > 5)
        {
            //定义B矩阵
            int col_num = 4;
            CMatrix B=new CMatrix(tw, col_num);
            CMatrix L=new CMatrix(tw, 1);
            CMatrix X=new CMatrix(col_num, 1);

            for (int i = 0; i < tw; i++)
            {
                B.p[i][0] = BL.p[i][0];
                B.p[i][1] = BL.p[i][1];
                B.p[i][2] = BL.p[i][2];
                L.p[i][0] = BL.p[i][3];

                if (Math.abs(BL.p[i][4] - 1.0) < 0.1)
                    B.p[i][3] = 1.0;
            }

            //X = (B.T()*B).InvertGaussJordan() * B.T() * L;
            X = ((B.T()).Multiple(B))   .InvertGaussJordan() .Multiple( B.T() )  .Multiple( L );

            XYZ.p[0][0] += X.p[0][0];
            XYZ.p[1][0] += X.p[1][0];
            XYZ.p[2][0] += X.p[2][0];

            return true;
        }
        else
            return true;
    }

    //函数功能：初步单点定位功能（考虑高度角定权）
    public static Boolean Get_SPP_POS_Ele(Obs_epoch tepoch, CMatrix XYZ)
    {
        int gps_num = 0;

        boolean GPS_Y = false;
        boolean BDS_Y = false;

        //存储BL矩阵信息，不超过40颗卫星
        CMatrix BL=new CMatrix(40, 6);

        int tw = 0;

        for (int i = 0; i < tepoch.sat_num; i++)
        {
            Sat tempSat = new Sat() ;
            tempSat=tepoch.sat.get(i);

            if (tempSat.E < 15.0)
                continue;

                GPS_Y = true;
                gps_num++;
                BL.p[tw][4] = 1.0;

            BL.p[tw][5] = tempSat.E;

            double Length = Math.sqrt(Math.pow(tempSat.POS_X - XYZ.p[0][0], 2) + Math.pow(tempSat.POS_Y - XYZ.p[1][0], 2) + Math.pow(tempSat.POS_Z - XYZ.p[2][0], 2));

            double Cof1 = (tempSat.POS_X - XYZ.p[0][0]) / Length;
            double Cof2 = (tempSat.POS_Y - XYZ.p[1][0]) / Length;
            double Cof3 = (tempSat.POS_Z - XYZ.p[2][0]) / Length;

            BL.p[tw][0] = -Cof1;
            BL.p[tw][1] = -Cof2;
            BL.p[tw][2] = -Cof3;

            //参考站1-非参考卫星
            double error_satclock1 = tempSat.Sat_clock * C_light;
            double error_relat1 = tempSat.xdl_t;
            double error_trop1 = tempSat.Trop_Delay;

            double we = 7.2921151467e-5;

            tempSat.Sagnac = we * (tempSat.POS_X * XYZ.p[1][0] - tempSat.POS_Y * XYZ.p[0][0]) / C_light;

            //Sagnac效应也需要改正
            double error_sagnac1 = tempSat.Sagnac;
            double error_tgd = tempSat.TGD;
            double error_antenna_height1 = tempSat.Antenna_Height;
            double error_sat_antenna1 = tempSat.Sat_Antenna;
            double test_geo1 = -error_satclock1 + error_trop1 - error_relat1 + error_sagnac1 + error_tgd - error_sat_antenna1;

            double test_P11;
            test_P11 = -(Length - tempSat.pseudo + test_geo1);

            BL.p[tw][3] = test_P11;

            tw++;
        }

        //大于5颗表示可以定位
        if (tw > 5)
        {
            //定义B矩阵
            int col_num = 4;

            CMatrix B=new CMatrix(tw, col_num);
            CMatrix L=new CMatrix(tw, 1);
            CMatrix R=new CMatrix(tw, tw);
            CMatrix X=new CMatrix(col_num, 1);

            for (int i = 0; i < tw; i++)
            {
                B.p[i][0] = BL.p[i][0];
                B.p[i][1] = BL.p[i][1];
                B.p[i][2] = BL.p[i][2];

                L.p[i][0] = BL.p[i][3];

                double elp = Math.sin(BL.p[i][5] / 180.0*PI) * Math.sin(BL.p[i][5] / 180.0*PI);
                R.p[i][i] = 0.09 + 0.09 / elp;

                if (Math.abs(BL.p[i][4] - 1.0) < 0.1 && GPS_Y)
                    B.p[i][3] = 1.0;
            }

            CMatrix P = R.InvertGaussJordan();
            //X = (B.T()*P*B).InvertGaussJordan() * B.T() * P * L;
            X = ((B.T()).Multiple(B))   .InvertGaussJordan() .Multiple( B.T() )  .Multiple( L );
            XYZ.p[0][0] += X.p[0][0];
            XYZ.p[1][0] += X.p[1][0];
            XYZ.p[2][0] += X.p[2][0];

            return true;
        }
        else
            return false;
    }
//传入的MessageRaw为特定GPSTime的一系列卫星的星历文件,GnssData为当前GPSTime的手机和基站共同观测到的卫星数据（包括伪距）
    public static double[] Position_Calculator(ArrayList<NavigationMessageRaw> MessageRaw, ArrayList<GNSSData>data, double gpstime, int sat_num)
    {
        //存储GPS和BDS观测数据类型：BDS-2与BDS-3一致
        double[] XYZ = { 0,0,0 };

        //开始读取o文件中内容
        ReceiverInfo temp_o=new ReceiverInfo();

        //文件头概率坐标
        XYZ[0] = temp_o.APP_X;
        XYZ[1] = temp_o.APP_Y;
        XYZ[2] = temp_o.APP_Z;
            //天线类型
//            if (strs.substr(60, 12) == "ANT # / TYPE")
//            {
//                temp_o.ANT_TYPE = strs.substr(20, 20);
//                fstream infileAnt;
//                infileAnt.open("antenna.txt", ios::in);
//                if (!infileAnt)
//                {
//                    printf("天线文件打开失败");
//                    _ASSERT(false);
//                }
//
//                temp_o.PCO1[0] = 0;
//                temp_o.PCO1[1] = 0;
//                temp_o.PCO1[2] = 0;
//
//                temp_o.PCO2[0] = 0;
//                temp_o.PCO2[1] = 0;
//                temp_o.PCO2[2] = 0;
//
//                string stra;
//                while (getline(infileAnt, stra))
//                {
//                    if (temp_o.ANT_TYPE == stra.substr(0, 20))
//                    {
//                        temp_o.PCO1[0] = atof(stra.substr(20,10).c_str());
//                        temp_o.PCO1[1] = atof(stra.substr(30,10).c_str());
//                        temp_o.PCO1[2] = atof(stra.substr(40,10).c_str());
//
//                        temp_o.PCO2[0] = atof(stra.substr(50,10).c_str());
//                        temp_o.PCO2[1] = atof(stra.substr(60,10).c_str());
//                        temp_o.PCO2[2] = atof(stra.substr(70,10).c_str());
//                        break;
//                    }
//                }//如果找到相同的天线
//                infileAnt.close();
//                printf("天线文件读取完成！\n");
//                continue;
//            }

//            if (strs.substr(60, 8) == "INTERVAL")
//            {
//                str2 = strs.substr(0, 10);
//                temp_o.INTERVAL = atof(str2.c_str());
//                continue;
//            }

//            if (strs.substr(60, 17) == "TIME OF FIRST OBS")
//            {
//                for (int i = 0; i < 5; i++)
//                {
//                    str2 = strs.substr((0 + 6 * i), 6);
//                    temp_o.FIRST_TIME[i] = atof(str2.c_str());
//                }
//                str2 = strs.substr(30, 13);
//                temp_o.FIRST_TIME[5] = atof(str2.c_str());
//                continue;
//            }
//
//            if (strs.substr(60, 16) == "TIME OF LAST OBS")
//            {
//                for (int i = 0; i<5; i++)
//                {
//                    str2 = strs.substr((0 + 6 * i), 6);
//                    temp_o.LAST_TIME[i] = atof(str2.c_str());
//                }
//                str2 = strs.substr(30, 13);
//                temp_o.LAST_TIME[5] = atof(str2.c_str());
//                continue;
//            }

        temp_o.LEAPSEC = 1;

        //读取观测文件部分
        double X_POS = 0;
        double Y_POS = 0;
        double Z_POS = 0;

        Obs_epoch temp_oe=new Obs_epoch();
        //TODO
        temp_oe.GPSTIME = gpstime;
        //对流层改正模型用
        //TODO
        //double JD = TROP.Julday(temp_oe.year, temp_oe.month, temp_oe.day, temp_oe.hour, temp_oe.minute, temp_oe.second);
        temp_oe.sat_num = sat_num;

        for (int k = 0; k<temp_oe.sat_num; k++)
        {
            Sat tempsat=new Sat();
            inti_Sat(tempsat);
            for(int find_k=0;find_k<=k;find_k++)
            {
                if (data.get(find_k).getPRN()==MessageRaw.get(k).PRN) {
                    tempsat.pseudo = data.get(find_k).getPseudorange();
                    break;
                }
            }
            double SpreadTime = tempsat.pseudo / C_light;
            Calc_Eph_GCEJ(temp_oe.GPSTIME, SpreadTime, tempsat, XYZ, MessageRaw.get(k));
            temp_oe.sat.add(tempsat);
        }

        boolean POS_IF ;

        //初始历元进行伪距单点定位获得概略坐标，更新XYZ:
        double dCoordX1 ;
        double dCoordY1 ;
        double dCoordZ1 ;

        CMatrix Pos=new CMatrix(3, 1);

        //从第二个历元开始，不用第一次迭代的结果
            Pos.p[0][0] = X_POS;
            Pos.p[1][0] = Y_POS;
            Pos.p[2][0] = Z_POS;

            dCoordX1 = Pos.p[0][0];
            dCoordY1 = Pos.p[1][0];
            dCoordZ1 = Pos.p[2][0];

        //以下两句仅用于判断该历元是否满足SPP条件（获取POS_IF）
        POS_IF = Get_SPP_POS(temp_oe, Pos);

        if (Math.abs(dCoordX1) < 1.0 && Math.abs(dCoordY1) < 1.0 && Math.abs(dCoordZ1) < 1.0)
        {
            do
            {
                dCoordX1 = Pos.p[0][0];
                dCoordY1 = Pos.p[1][0];
                dCoordZ1 = Pos.p[2][0];

                Get_SPP_POS(temp_oe, Pos);
                if (!POS_IF)
                    break;

            } while (Math.abs(dCoordX1 - Pos.p[0][0]) >= 0.1 || Math.abs(dCoordY1 - Pos.p[1][0]) >= 0.1 || Math.abs(dCoordZ1 - Pos.p[2][0]) >= 0.1);
        }

        //表示无法进行单点定位，也就无法获得初始坐标，暂时标为不可用
        if (!POS_IF)
        {
            //将初始坐标置为0
            Pos.p[0][0] = 0;
            Pos.p[1][0] = 0;
            Pos.p[2][0] = 0;

            temp_oe.ZHD = 0;

            //用新的坐标更新高度角、方位角、对流层等信息
            for (int k = 0; k < temp_oe.sat_num; k++)
            {
                Sat tempsat;
                tempsat = temp_oe.sat.get(k);

                if (tempsat.sattype.equals("G"))
                {
                    tempsat.E = 0;
                    tempsat.A = 0;
                    tempsat.Trop_Delay = 0;
                    tempsat.Trop_Map = 0;
                    tempsat.Sagnac = 0;
                    temp_oe.sat.set(k,tempsat);
                }
            }
        }

        else
        {
            //更新经纬度
            XYZ[0] = dCoordX1;
            XYZ[1] = dCoordY1;
            XYZ[2] = dCoordZ1;

            double []BLH = new double [3];
            BLH[0]=0;
            BLH[1]=0;
            BLH[2]=0;

            OnXYZtoBLH(dCoordX1, dCoordY1, dCoordZ1, BLH);

            temp_o.B = BLH[0];
            temp_o.L = BLH[1];
            temp_o.DH = BLH[2];
            CMatrix HH=new CMatrix(3, 3);

            HH.p[0][0] = -Math.sin(temp_o.B)*Math.cos(temp_o.L);
            HH.p[0][1] = -Math.sin(temp_o.B)*Math.sin(temp_o.L);
            HH.p[0][2] = Math.cos(temp_o.B);
            HH.p[1][0] = -Math.sin(temp_o.L);
            HH.p[1][1] = Math.cos(temp_o.L);
            HH.p[1][2] = 0;
            HH.p[2][0] =Math.cos(temp_o.B)*Math.cos(temp_o.L);
            HH.p[2][1] = Math.cos(temp_o.B)*Math.sin(temp_o.L);
            HH.p[2][2] = Math.sin(temp_o.B);

            //考虑对流层再进行一次坐标解算
            for (int k = 0; k < temp_oe.sat_num; k++)
            {
                Sat tempsat;
                tempsat = temp_oe.sat.get(k);

                if (tempsat.sattype.equals("G"))
                {
                    //计算高度角和方位角
                    CalSatEA(HH, XYZ, tempsat);

                    //计算对流层
                    tempsat.Trop_Delay=stratosphere.tropmodel(BLH,tempsat.E*PI/180.0,0.5);
                    tempsat.Trop_Map=0;

                    //计算Sagnac效应改正
                    double we = 7.2921151467e-5;
                    tempsat.Sagnac = we * (tempsat.POS_X * XYZ[1] - tempsat.POS_Y * XYZ[0]) / C_light;
                    temp_oe.sat.set(k,tempsat);
                }
            }

            //重新计算坐标
            do
            {
                dCoordX1 = Pos.p[0][0];
                dCoordY1 = Pos.p[1][0];
                dCoordZ1 = Pos.p[2][0];

                Get_SPP_POS_Ele(temp_oe, Pos);

            } while (Math.abs(dCoordX1 - Pos.p[0][0]) >= 0.1 || Math.abs(dCoordY1 - Pos.p[1][0]) >= 0.1 || Math.abs(dCoordZ1 - Pos.p[2][0]) >= 0.1);

            XYZ[0] = Pos.p[0][0];
            XYZ[1] = Pos.p[1][0];
            XYZ[2] = Pos.p[2][0];

            //用新的坐标更新高度角、方位角、对流层等信息
            for (int k = 0; k < temp_oe.sat_num; k++)
            {
                Sat tempsat;
                tempsat = temp_oe.sat.get(k);

                if (tempsat.sattype.equals("G"))
                {
                    //计算高度角和方位角
                    CalSatEA(HH, XYZ, tempsat);

                    //计算对流层
                    tempsat.Trop_Delay=stratosphere.tropmodel(BLH,tempsat.E*PI/180.0,0.5);
                    tempsat.Trop_Map=0;
                    //计算Sagnac效应改正
                    double we = 7.2921151467e-5;
                    tempsat.Sagnac = we * (tempsat.POS_X * XYZ[1] - tempsat.POS_Y * XYZ[0]) / C_light;
                    temp_oe.sat.set(k,tempsat);
                }
            }
        }
        dCoordX1 = Pos.p[0][0];
        dCoordY1 = Pos.p[1][0];
        dCoordZ1 = Pos.p[2][0];
        double []final_BLH = new double [3];
        OnXYZtoBLH(dCoordX1,dCoordY1,dCoordZ1,final_BLH);
        return final_BLH;
    }

    private static void OnXYZtoBLH(double X, double Y, double Z, double[] BLH)
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
//            if (DEBUG) {
//                throw new AssertionError("Assertion failed");
//            }
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
    //函数功能：进行接收机天线改正
//    void AntOffsetCorrect(Obs_epoch tempData, double []PCO1, double []PCO2)
//    {
//        double c = 299792458.458;
//        double off_L1, off_L2;
//        double lambda_l1=0, lambda_l2=0;
//
//        double f_gps1 = 154 * 10.23E6;
//        double f_gps2 = 120 * 10.23E6;
//
//        double f_geo1 = 1575.42E6;
//        double f_geo2 = 1176.45E6;
//
//
//        for (int i = 0; i < tempData.sat.size(); i++)
//        {
//            if (tempData.sat.get(i).judge_use == 2)
//                continue;
//
//            if (tempData.sat.get(i).sattype.equals("G"))
//            {
//                lambda_l1 = c / f_gps1;
//                lambda_l2 = c / f_gps2;
//            }
//
//            if (tempData.sat.get(i).sattype.equals("E"))
//            {
//                lambda_l1 = c / f_geo1;
//                lambda_l2 = c / f_geo2;
//            }
//
//            double elev = tempData.sat.get(i).E / 180.0 * PI;
//            double A = tempData.sat.get(i).A / 180.0 * PI;
//
//            //表明未能获得概略坐标
//            if (elev < 0.5 && A < 0.5)
//            {
//                tempData.sat.get(i).OffsetL1 = 0;
//                tempData.sat.get(i).OffsetL2 = 0;
//                continue;
//            }
//
//            double cosel = cos(elev);
//            double sinel = sin(elev);
//            double cosaz = cos(A);
//            double sinaz = sin(A);
//
//            off_L1 = (PCO1[0] * cosel * cosaz + PCO1[1] * cosel * sinaz + PCO1[2] * sinel) / 1000.0;
//            off_L2 = (PCO2[0] * cosel * cosaz + PCO2[1] * cosel * sinaz + PCO2[2] * sinel) / 1000.0;
//
//            tempData.sat.get(i).OffsetL1 = off_L1 / lambda_l1;
//            tempData.sat.get(i).OffsetL2 = off_L2 / lambda_l2;
//        }
//    }

    //函数功能：根据匹配到的星历进行单个卫星位置计算（GPS和BDS）
    private static void Calc_Eph_GCEJ(double GPSTIME, double rclk, Sat sat, double[] XYZ, NavigationMessageRaw EpochNG)
    {
        double we = 7.2921151467e-5;
        double GM = 3.9860050e14;

        double n0, n, tk, Mk, ek1, ek2, Ek, Vk, fk;
        double deltau, deltar, deltai, uk, rk, ik, xk, yk, Lk;
        EpochNG.GPSTIME=JudgeEphemeris.GetGPSTime(EpochNG);
        n0 = sqrt(GM) / pow(EpochNG.sqrtA, 3);
        n = n0 + EpochNG.delta_n;

        tk = GPSTIME - EpochNG.GPSTIME;

        if (tk > 302400) tk -= 604800.0;
        else if (tk < -302400) tk += 604800.0;

        sat.Sat_clock = EpochNG.a0 + EpochNG.a1 * tk + EpochNG.a2 * pow(tk, 2);
        sat.TGD = EpochNG.TGD * C_light;

        tk = tk - sat.Sat_clock - rclk;

        Mk = EpochNG.M0 + n*tk;

        ek1 = Mk;
        do
        {
            ek2 = Mk + EpochNG.e*sin(ek1);
            if (abs(ek2 - ek1) <= 1.0e-13)  break;
            ek1 = ek2;
        } while (true);

        Ek = ek1;
        Vk = 2 * atan(sqrt((1.0 + EpochNG.e) / (1.0 - EpochNG.e))*tan(Ek / 2));
        fk = Vk + EpochNG.w;

        deltau = EpochNG.Cuc * cos(2.0*fk) + EpochNG.Cus * sin(2.0*fk);
        deltar = EpochNG.Crc * cos(2.0*fk) + EpochNG.Crs * sin(2.0*fk);
        deltai = EpochNG.Cic * cos(2.0*fk) + EpochNG.Cis * sin(2.0*fk);

        uk = fk + deltau;
        rk = EpochNG.sqrtA * EpochNG.sqrtA * (1.0 - EpochNG.e*cos(Ek)) + deltar;
        ik = EpochNG.i0 + deltai + EpochNG.i_DOT * tk;

        xk = rk * cos(uk);
        yk = rk * sin(uk);

        Lk = EpochNG.OMEGA + (EpochNG.OMEGA_DOT - we)*tk - we* EpochNG.TOE;
        sat.POS_X = xk * cos(Lk) - yk * cos(ik) * sin(Lk);
        sat.POS_Y = xk * sin(Lk) + yk * cos(ik) * cos(Lk);
        sat.POS_Z = yk * sin(ik);

        sat.xdl_t = -2 * sqrt(GM) * EpochNG.e * EpochNG.sqrtA * sin(Ek) / C_light;

        //计算地球自转效应:粗略解算
        sat.Sagnac = we * (sat.POS_X * XYZ[1] - sat.POS_Y * XYZ[0]) / C_light;
    }
}
