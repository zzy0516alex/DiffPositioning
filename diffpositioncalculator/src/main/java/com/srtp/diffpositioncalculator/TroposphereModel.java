package com.srtp.diffpositioncalculator;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.exp;
import static java.lang.Math.pow;

public class TroposphereModel {
    /**
     * 对流层校正
     * @param pos
     * @param azel
     * @param humi
     * @return
     */
        static public double tropomodel(double []pos, double azel, double humi)
        {
	        final double temp0 = 15.0; /* temparature at sea level */
            double hgt, pres, temp, e, z, trph, trpw;

            if (pos[2] < -100.0 || 1E4 < pos[2] || azel <= 0) return 0.0;

            /* standard atmosphere */
            hgt = Math.max(pos[2], 0.0);

            pres = 1013.25*pow(1.0 - 2.2557E-5*hgt, 5.2568);
            temp = temp0 - 6.5E-3*hgt + 273.16;
            e = 6.108*humi*exp((17.15*temp - 4684.0) / (temp - 38.45));

            /* saastamoninen model */
            z = PI / 2.0 - azel;
            trph = 0.0022768*pres / (1.0 - 0.00266*cos(2.0*pos[0]) - 0.00028*hgt / 1E3) / cos(z);
            trpw = 0.002277*(1255.0 / temp + 0.05)*e / cos(z);
            return trph + trpw;
        }
    }

