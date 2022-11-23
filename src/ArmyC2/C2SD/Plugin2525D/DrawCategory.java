/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ArmyC2.C2SD.Plugin2525D;

/**
 *
 * @author Michael.Spinelli
 */
public class DrawCategory {
    
     /**
     * Just a category in the milstd hierarchy.
     * Not something we draw.
     * WILL NOT RENDER
     */
    static public final int DONOTDRAW = 0;

    /**
     * A polyline, a line with n number of points.
     * 0 control points
     */
    static public final int LINE = 1;

    /**
     * An animated shape, uses the animate function to draw.
     * 0 control points (every point shapes symbol)
     */
    static public final int AUTOSHAPE = 2;

    /**
     * An enclosed polygon with n points
     * 0 control points
     */
    static public final int POLYGON = 3;
    /**
     * A polyline with n points (entered in reverse order)
     * 0 control points
     */
    static public final int ARROW = 4;
    /**
     * A graphic with n points whose last point defines the width of the graphic.
     * 1 control point
     */
    static public final int ROUTE = 5;
    /**
     * A line defined only by 2 points, and cannot have more.
     * 0 control points
     */
    static public final int TWOPOINTLINE = 6;
    /**
     * Shape is defined by a single point
     * 0 control points
     */
    static public final int POINT = 8;
    /**
     * A polyline with 2 points (entered in reverse order).
     * 0 control points
     */
    static public final int TWOPOINTARROW = 9;
    /**
     * An animated shape, uses the animate function to draw. Super Autoshape draw
     * in 2 phases, usually one to define length, and one to define width.
     * 0 control points (every point shapes symbol)
     *
     */
    static public final int SUPERAUTOSHAPE = 15;
     /**
     * Symbol has unique drawing approach
     * Like a Circle that requires 1 AM modifier value.
     * See ModifiersTG.java for modifier descriptions and constant key strings.
     */
    static public final int CIRCULAR_PARAMETERED_AUTOSHAPE = 16;
    /**
     * Rectangle that requires 2 AM modifier values and 1 AN value.";
     * See ModifiersTG.java for modifier descriptions and constant key strings.
     */
    static public final int RECTANGULAR_PARAMETERED_AUTOSHAPE = 17;
    /**
     * Requires 2 AM values and 2 AN values per sector.  
     * The first sector can have just one AM value although it is recommended 
     * to always use 2 values for each sector.  X values are not required
     * as our rendering is only 2D for the Sector Range Fan symbol.
     * See ModifiersTG.java for modifier descriptions and constant key strings.
     */
    static public final int SECTOR_PARAMETERED_AUTOSHAPE = 18;
    /**
     *  Requires at least 1 distance/AM value"
     *  See ModifiersTG.java for modifier descriptions and constant key strings.
     */
    static public final int CIRCULAR_RANGEFAN_AUTOSHAPE = 19;
    /**
     * Requires 1 AM value.
     * See ModifiersTG.java for modifier descriptions and constant key strings.
     */
    static public final int TWO_POINT_RECT_PARAMETERED_AUTOSHAPE = 20;
    
    /**
     * 3D airspace, not a milstd graphic.
     */
    static public final int THREE_DIMENSIONAL_AIRSPACE = 40;
    
    /**
     * UNKNOWN.
     */
    static public final int UNKNOWN = 99;

    
}
