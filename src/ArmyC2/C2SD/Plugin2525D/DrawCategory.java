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
    
    /**
     * 
     * @param symbolSet
     * @param drawRule from DrawRules or MODrawRules
     */
    static public int getDrawCategoryFromRule(int symbolSet, int drawRule)
    {
        int dc = POINT;
        
        if(symbolSet == SymbolID.SymbolSet_ControlMeasure)
        {
            switch(drawRule)
            {

                case DrawRules.AREA1:
                case DrawRules.AREA2:
                case DrawRules.AREA3:
                case DrawRules.AREA4:
                case DrawRules.AREA9:
                case DrawRules.AREA10:
                case DrawRules.AREA20:
                case DrawRules.AREA23:
                case DrawRules.AREA26:
                    dc = POLYGON;
                    break;
                case DrawRules.AREA5:
                case DrawRules.AREA7:
                case DrawRules.AREA8:
                case DrawRules.AREA11:
                case DrawRules.AREA12:
                case DrawRules.AREA17:
                case DrawRules.AREA18:
                case DrawRules.AREA21:
                case DrawRules.AREA24:
                case DrawRules.AREA25:
                case DrawRules.POINT12:
                case DrawRules.LINE3:
                case DrawRules.LINE10:
                case DrawRules.LINE12:
                case DrawRules.LINE17:
                case DrawRules.LINE22:
                case DrawRules.LINE23:
                case DrawRules.LINE24:
                case DrawRules.LINE27:
                case DrawRules.LINE29://Ambush
                case DrawRules.POLYLINE1://Infiltration Lane
                case DrawRules.LINE15:
                case DrawRules.LINE11://bridge or gap
                case DrawRules.LINE16:
                    dc = SUPERAUTOSHAPE;
                    break;
                case DrawRules.AREA6:
                case DrawRules.AREA13:
                case DrawRules.AREA14:
                case DrawRules.AREA15:
                case DrawRules.AREA16:
                case DrawRules.AREA19:
                case DrawRules.LINE6://doesn't seem to be used
                case DrawRules.LINE4://?
                case DrawRules.LINE19://Fighting Position?
                case DrawRules.LINE26:
                    dc = AUTOSHAPE;
                    break;
                case DrawRules.AREA22://Basic Defense Zone (BDZ) requires AM for radius
                case DrawRules.ELLIPSE1://required AM, AM1, AN
                case DrawRules.CIRCULAR1://required AM, AM1, AN
                    dc = CIRCULAR_PARAMETERED_AUTOSHAPE;
                    break;
                case DrawRules.CIRCULAR2:
                    dc = CIRCULAR_RANGEFAN_AUTOSHAPE;
                    break;
                case DrawRules.LINE9:
                case DrawRules.LINE20:
                case DrawRules.LINE25:             
                case DrawRules.LINE28:
                    dc = TWOPOINTARROW;
                    break;
                case DrawRules.LINE1:
                case DrawRules.LINE2:
                case DrawRules.LINE7:
                case DrawRules.LINE8:
                case DrawRules.LINE13:
                case DrawRules.LINE21:
                case DrawRules.CORRIDOR1://Airspace Control Corridors
                    dc = LINE;
                    break;
                case DrawRules.LINE5:
                case DrawRules.LINE14:
                case DrawRules.LINE18:
                    dc = TWOPOINTLINE;
                    break;    
                case DrawRules.RECTANGULAR1://requires AM
                case DrawRules.RECTANGULAR2://requires AM, AM1, AN
                case DrawRules.RECTANGULAR3://requires AM ?????
                    dc = TWO_POINT_RECT_PARAMETERED_AUTOSHAPE;
                    break;
                case DrawRules.AXIS1:
                case DrawRules.AXIS2:
                    dc = ROUTE;
                    break;                
                case DrawRules.POINT17://requires AM & AM1
                    dc = RECTANGULAR_PARAMETERED_AUTOSHAPE;
                    break;
                case DrawRules.POINT18://requires AM & AN values
                case DrawRules.ARC1://requires AM & AN values
                    dc = SECTOR_PARAMETERED_AUTOSHAPE;
                    break;
                case 0://do not draw
                    dc = DONOTDRAW;
                break;
                //Rest are single points
                default:
                    dc = POINT;
            }
        }
        else if(symbolSet == SymbolID.SymbolSet_Oceanographic)
        {
            switch(drawRule)
            {
                case MODrawRules.LINE1:
                case MODrawRules.LINE2:
                case MODrawRules.LINE3:
                case MODrawRules.LINE4:
                case MODrawRules.LINE6:
                case MODrawRules.LINE7:
                case MODrawRules.LINE8:
                    dc = LINE;
                    break;
                case MODrawRules.LINE5:
                case MODrawRules.AREA1:
                case MODrawRules.AREA2:
                    dc = POLYGON;
                    break;
                case MODrawRules.POINT5:
                    dc = SUPERAUTOSHAPE;//Wind Plot???, we didn't draw it before
                    break;
                case 0://do not draw
                    dc = DONOTDRAW;
                break;
                //Rest are single points
                default:
                    dc = POINT;
            }
        }
        else if(drawRule == 0)
            dc = DONOTDRAW;
        else
            dc = POINT;
        
        return dc;
    }

    
}
