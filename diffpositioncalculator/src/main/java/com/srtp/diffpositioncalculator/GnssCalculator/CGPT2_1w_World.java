package com.srtp.diffpositioncalculator.GnssCalculator;

public class CGPT2_1w_World {
    public CGPT2_1w_World() {
        double[][] pgrid, Tgrid, Qgrid, dTgrid, u, Hs, ahgrid, awgrid, lagrid, Tmgrid;
        pgrid = new double[64800][5];
        Tgrid = new double[64800][5];
        Qgrid = new double[64800][5];
        dTgrid = new double[64800][5];
        u = new double[64800][5];
        Hs = new double[64800][5];
        ahgrid = new double[64800][5];
        awgrid = new double[64800][5];
        lagrid = new double[64800][5];
        Tmgrid = new double[64800][5];
    }
    int sign2(double x)
    {
        if(x>0)
            return 1;
        else
            return -1;
    }

    void init_trop()
    {
//        double []vel=new double[44];
//        int n =0;
//            for(int i=0;i<5;i++)
//            {
//                pgrid[n][i]  = vel[i+2];
//                double temp = pgrid[n][i];
//                Tgrid[n][i]  = vel[i+7];
//                Qgrid[n][i]  = vel[i+12]/1000;
//                dTgrid[n][i] = vel[i+17]/1000;
//                ahgrid[n][i] = vel[i+24]/1000;
//                awgrid[n][i] = vel[i+29]/1000;
//                lagrid[n][i] = vel[i+34];
//                Tmgrid[n][i] = vel[i+39];
//            }
//            u[n]  = vel[22];
//            Hs[n] = vel[23];
//            n++;
        }
    }

