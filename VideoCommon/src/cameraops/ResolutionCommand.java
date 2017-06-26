/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cameraops;

import cameraops.ConfigCommand;

/**
 *
 * @author pjsanfil
 */
public class ResolutionCommand extends ConfigCommand<ResolutionCommand.Resolution> {
    public enum Resolution {RES_160x120, RES_640x480, RES_800x448, RES_752x416, RES_800x600, RES_960x544, RES_960x720, RES_1024x576, RES_1280x960;
        // this changes the way the enum shows up when you print it out so it
        // looks nice in the gui selection dodad
        @Override
        public String toString() {
            return this.name().substring(4);
        }
        
        /**
         * You cannot override valueOf so to change the friendly String produced
         * by toString back into an enum this function is needed.
         * @param value A friendly String representing the enum such as that
         * produced by toString().
         * @return 
         */
        public static Resolution getEnum(String value) {
            return Resolution.valueOf("RES_" + value);
        }
    }
    public double getWidth() {
        return resolutionVals[m_cmd.ordinal()][0];
    }
    public double getHeight() {
        return resolutionVals[m_cmd.ordinal()][1];
    }
    private static final double[][] resolutionVals = new double[][]{ {160,120},
                                                                     {640,480},
                                                                     {800,448},
                                                                     {752,416},
                                                                     {800,600},
                                                                     {960,544},
                                                                     {960,720},
                                                                     {1024,576},
                                                                    {1280,960}};
}
