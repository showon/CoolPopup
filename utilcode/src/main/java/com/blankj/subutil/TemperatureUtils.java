package com.blankj.subutil;

/**
 * <pre>
 *     author: Faramarz Afzali
 *     time  : 2020/09/05
 *
 *    desc ：此类用于将温度转换为不同的单位。
 *    C 指摄氏单位
 *    F 指华氏单位
 *    K 指开尔文单位
 * </pre>
 */
public final class TemperatureUtils {

    public static float cToF(float temp) {
        return (temp * 9) / 5 + 32;
    }

    public static float cToK(float temp) {
        return temp + 273.15f;
    }


    public static float fToC(float temp) {
        return (temp - 32) * 5 / 9;
    }

    public static float fToK(float temp) {
        return temp + 255.3722222222f;
    }


    public static float kToC(float temp) {
        return temp - 273.15f;
    }

    public static float kToF(float temp) {
        return temp - 459.67f;
    }
}
