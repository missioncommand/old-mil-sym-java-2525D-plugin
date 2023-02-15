/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ArmyC2.C2SD.Plugin2525D;

import ArmyC2.C2SD.RendererPluginInterface.ISinglePointInfo;
import ArmyC2.C2SD.RendererPluginInterface.SinglePointInfo;
import ArmyC2.C2SD.Utilities.ErrorLogger;
import ArmyC2.C2SD.Utilities.SymbolUtilities;
import ArmyC2.C2SD.Utilities.MilStdAttributes;
import ArmyC2.C2SD.Utilities.ModifiersTG;
import ArmyC2.C2SD.Utilities.ModifiersUnits;
import ArmyC2.C2SD.Utilities.PointConversion;
import ArmyC2.C2SD.Utilities.RendererException;
import ArmyC2.C2SD.Utilities.RendererSettings;
import ArmyC2.C2SD.Utilities.ShapeInfo;
import ArmyC2.C2SD.Utilities.SinglePointFont;
import ArmyC2.C2SD.Utilities.SymbolDraw;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 *
 * @author michael.spinelli
 */
public class ModifierRendererD {
    
    private static ModifierRendererD _instance = null;
    private static String _className = "ModifierRendererD";
    
    private static RendererSettings RS = RendererSettings.getInstance();
    private static Font _modifierFont = RS.getLabelFont();
    private static BufferedImage _buffer = null;
    private static FontRenderContext _fontRenderContext = null;
    private static FontRenderContext _frc = null;
    private static float _modifierFontHeight = 10f;
    private static float _modifierFontDescent = 2f;
            
    
    private static int tgTextModifierKeys[] = {2,3,4,5,6,9,10,11,12,13,14,15};
    
        PointConversion _PointConverter = null;
    //private static ArrayList<String> _ModifierNamesUnit = null;
    private static ArrayList<String> _ModifierNamesTG = null;

    private static Font _SinglePointFont = null;//SinglePointFont.getInstance().getSPFont(100);
    private static Font _UnitFont = null;//SinglePointFont.getInstance().getUnitFont(100);
    private static Font _ModifierFont = null;

    private final Object _SinglePointFontMutex = new Object();
    private final Object _UnitFontMutex = new Object();
    private final Object _ModifierFontMutex = new Object();


    //Unit 2525C sizes
    public static final int UnitSizeMedium = 40;
    public static final int UnitSizeSmall = 30;
    public static final int UnitSizeLarge = 50;
    public static final int UnitSizeXL = 60;

    //TG & unit 2525B sizes
    public static final int SymbolSizeMedium = 80;
    public static final int SymbolSizeSmall = 60;
    public static final int SymbolSizeLarge = 100;
    public static final int SymbolSizeXL = 120;
    
    
    private ModifierRendererD()
    {
        try
        {
            
            _SinglePointFont = SinglePointFont.getInstance().getSPFont(SymbolSizeSmall);
            _UnitFont = SinglePointFont.getInstance().getUnitFont(UnitSizeMedium);
            _ModifierFont = RendererSettings.getInstance().getLabelFont();
            

            _ModifierNamesTG = ModifiersTG.GetModifierList();
            //_ModifierNamesUnit = ModifiersUnits.GetModifierList();
            _className = this.getClass().getName();

            if(_SinglePointFont == null)
            {
                ErrorLogger.LogException(this.getClass().getName() ,"SinglePointRenderer()",
                    new RendererException("SinglePointRenderer failed to initialize - _SinglePointFont didn't load.", null));
            }
            if(_UnitFont == null)
            {
                ErrorLogger.LogException(this.getClass().getName() ,"SinglePointRenderer()",
                    new RendererException("SinglePointRenderer failed to initialize - _UnitFont didn't load.", null));
            }


            //trying to use just 1 image all the time
            //and one FontRenderContext
            if(_buffer == null)
            {
                _buffer = new BufferedImage(8,8,BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = (Graphics2D)_buffer.createGraphics();
                _fontRenderContext = g2d.getFontRenderContext();
                _frc = _fontRenderContext;
                TextInfo temp = new TextInfo("Jj",0,0,_modifierFont,_frc);
                _modifierFontDescent = (float)temp.getDescent();
                _modifierFontHeight = (float)temp.getTextBounds().getHeight();
            }
        }
        catch(Exception exc)
        {
            ErrorLogger.LogException(_className, "SinglePointRenderer", exc);
        }
    }
    
    /**
     * Instance of the JavaRenderer
     * @return the instance
     */
    public static synchronized ModifierRendererD getInstance()
    {
        if(_instance == null)
            _instance = new ModifierRendererD();

        return _instance;
    }
    
    public static ISinglePointInfo processUnitDisplayModifiers(String symbolID, ISinglePointInfo pi, Map<String, String> modifiers, boolean hasTextModifiers, FontRenderContext frc)
    {
        ISinglePointInfo newii = null;
        Rectangle2D symbolBounds = (Rectangle2D)pi.getSymbolBounds().clone();
        Rectangle2D imageBounds = new Rectangle2D.Double(0, 0, pi.getImage().getWidth(), pi.getImage().getHeight());
        Point2D centerPoint = (Point2D)pi.getSymbolCenterPoint().clone();
        TextInfo tiEchelon = null;
        TextInfo tiAM = null;
        Rectangle2D echelonBounds = null;
        Rectangle2D amBounds = null;
        Color textColor = Color.BLACK;
        Color textBackgroundColor = null;
        float strokeWidth = 3.0f;
        float strokeWidthNL = 3.0f;
        int buffer = 0;
        //ctx = null;
        int offsetX = 0;
        int offsetY = 0;
        RendererSettings RS = RendererSettings.getInstance();
        int symStd = RS.getSymbologyStandard();
        if (modifiers.containsKey(MilStdAttributes.SymbologyStandard))
        {
            symStd = Integer.parseInt(modifiers.get(MilStdAttributes.SymbologyStandard));
        }
        if (modifiers.containsKey(MilStdAttributes.TextColor))
        {
            textColor = SymbolUtilities.getColorFromHexString(modifiers.get(MilStdAttributes.TextColor));
        }
        if (modifiers.containsKey(MilStdAttributes.TextBackgroundColor))
        {
            textBackgroundColor = SymbolUtilities.getColorFromHexString(modifiers.get(MilStdAttributes.TextBackgroundColor));
        }

        // <editor-fold defaultstate="collapsed" desc="Build Mobility Modifiers">
        Rectangle2D mobilityBounds = null;
        
        
        List<Path2D> shapes = new ArrayList<Path2D>();
        
        int ad = SymbolID.getAmplifierDescriptor(symbolID);
        Path2D mobilityPath = null;
        Path2D mobilityPathFill = null;
        if (ad > 30 && ad < 70)
        {

            //Draw Mobility
            int fifth = (int) ((symbolBounds.getWidth() * 0.2) + 0.5f);
            int sixth = (int) ((symbolBounds.getWidth() * (1/6)) + 0.5f);
            mobilityPath = new Path2D.Double();
            int x = 0;
            int y = 0;
            int centerX = 0;
            int bottomY = 0;
            int height = 0;
            int width = 0;
            int middleY = 0;
            int wheelOffset = 2;
            int wheelSize = fifth;//10;
            int rrHeight = fifth;//10;
            int rrArcWidth = (int) ((fifth * 1.5) + 0.5f);//16;

            String mobility = symbolID.substring(10, 12);
            x = (int) symbolBounds.getX();// + 1;
            y = (int) symbolBounds.getY();
            height = (int)Math.round(symbolBounds.getHeight());
            width = (int)Math.round(symbolBounds.getWidth());
            bottomY = y + height + 2;

            if (ad > 30 && ad < 60)//mobility
            {
                bottomY = y + height + 2;

                //wheelSize = width / 7;
                //rrHeight = width / 7;
                //rrArcWidth = width / 7;
                if (ad == SymbolID.Mobility_WheeledLimitedCrossCountry)//MO
                {
                    //line
                    mobilityPath.append(new Line2D.Double(x,bottomY,x + width, bottomY), false);
                    //left circle
                    mobilityPath.append(new Ellipse2D.Double(x, bottomY + wheelOffset, wheelSize, wheelSize), false);
                    //right circle
                    mobilityPath.append(new Ellipse2D.Double(x + width - wheelSize, bottomY + wheelOffset, wheelSize, wheelSize), false);
                }
                else if (ad == SymbolID.Mobility_WheeledCrossCountry)//MP
                {
                    //line
                    mobilityPath.append(new Line2D.Double(x,bottomY,x + width, bottomY), false);
                    //left circle
                    mobilityPath.append(new Ellipse2D.Double(x, bottomY + wheelOffset, wheelSize, wheelSize), false);
                    //right circle
                    mobilityPath.append(new Ellipse2D.Double(x + width - wheelSize, bottomY + wheelOffset, wheelSize, wheelSize), false);
                    //center wheel
                    mobilityPath.append(new Ellipse2D.Double(x + (width/2)-(wheelSize/2), bottomY + wheelOffset, wheelSize, wheelSize), false);
                }
                else if (ad == SymbolID.Mobility_Tracked)//MQ
                {
                    //round rectangle
                    mobilityPath.append(new RoundRectangle2D.Double(x, bottomY, width, rrHeight, rrArcWidth, rrHeight),false);

                }
                else if (ad == SymbolID.Mobility_Wheeled_Tracked)//MR
                {
                    //round rectangle
                    mobilityPath.append(new RoundRectangle2D.Double(x, bottomY, width, rrHeight, rrArcWidth, rrHeight),false);
                    //left circle
                    mobilityPath.append(new Ellipse2D.Double(x - wheelSize - wheelSize, bottomY, wheelSize, wheelSize), false);
                }
                else if (ad == SymbolID.Mobility_Towed)//MS
                {
                    //line
                    mobilityPath.append(new Line2D.Double(x + wheelSize,bottomY + (wheelSize/2),x + width - wheelSize, bottomY + (wheelSize/2)), false);
                    //left circle
                    mobilityPath.append(new Ellipse2D.Double(x, bottomY, wheelSize, wheelSize), false);
                    //right circle
                    mobilityPath.append(new Ellipse2D.Double(x + width - wheelSize, bottomY, wheelSize, wheelSize), false);
                }
                else if (ad == SymbolID.Mobility_Rail)//MT
                {
                    //line
                    mobilityPath.append(new Line2D.Double(x,bottomY,x + width, bottomY), false);
                    //left circle
                    mobilityPath.append(new Ellipse2D.Double(x + wheelSize, bottomY + wheelOffset, wheelSize, wheelSize), false);
                    //left circle2
                    mobilityPath.append(new Ellipse2D.Double(x, bottomY + wheelOffset, wheelSize, wheelSize), false);
                    //right circle
                    mobilityPath.append(new Ellipse2D.Double(x + width - wheelSize, bottomY + wheelOffset, wheelSize, wheelSize), false);
                    //right circle2
                    mobilityPath.append(new Ellipse2D.Double(x + width - wheelSize - wheelSize, bottomY + wheelOffset, wheelSize, wheelSize), false);

                }
                else if (ad == SymbolID.Mobility_OverSnow)//MU
                {
                    mobilityPath.moveTo(x, bottomY);
                    mobilityPath.lineTo(x + 5, bottomY + 5);
                    mobilityPath.lineTo(x + width, bottomY + 5);   
                }
                else if (ad == SymbolID.Mobility_Sled)//MV
                {
                    mobilityPath.moveTo(x, bottomY);
                    mobilityPath.curveTo(x, bottomY, x-rrArcWidth, bottomY+3, x, bottomY+rrHeight);
                    mobilityPath.lineTo(x + width, bottomY + rrHeight);
                    mobilityPath.curveTo(x + width, bottomY + rrHeight, x+ width + rrArcWidth, bottomY+3, x + width, bottomY);
                }
                else if (ad == SymbolID.Mobility_PackAnimals)//MW
                {
                    centerX = (int)symbolBounds.getCenterX();
                    mobilityPath.moveTo(centerX, bottomY + rrHeight+2);
                    mobilityPath.lineTo(centerX - 3, bottomY);
                    mobilityPath.lineTo(centerX - 6, bottomY + rrHeight+2);
                    mobilityPath.moveTo(centerX, bottomY + rrHeight+2);
                    mobilityPath.lineTo(centerX + 3, bottomY);
                    mobilityPath.lineTo(centerX + 6, bottomY + rrHeight+2);
                }
                else if (ad == SymbolID.Mobility_Barge)//MX
                {
                    centerX = (int)symbolBounds.getCenterX();
                    double quarterX = (centerX - x)/2;
                    double quarterY = (((bottomY + rrHeight) - bottomY)/2);
                    mobilityPath.moveTo(x+width, bottomY);
                    mobilityPath.lineTo(x, bottomY);
                    mobilityPath.curveTo(x+quarterX, bottomY+rrHeight, centerX + quarterX, bottomY + rrHeight, x + width, bottomY);
                }
                else if (ad == SymbolID.Mobility_Amphibious)//MY
                {
                    double incrementX = width / 7;
                    middleY = (((bottomY + rrHeight) - bottomY)/2);
                    
                    mobilityPath.append(new Arc2D.Double(x, bottomY + middleY, incrementX, rrHeight, 0, 180, Arc2D.OPEN),false);
                    mobilityPath.append(new Arc2D.Double(x + incrementX, bottomY + middleY, incrementX, rrHeight, 0, -180, Arc2D.OPEN),false);
                    mobilityPath.append(new Arc2D.Double(x + incrementX*2, bottomY + middleY, incrementX, rrHeight, 0, 180, Arc2D.OPEN),false);
                    mobilityPath.append(new Arc2D.Double(x + incrementX*3, bottomY + middleY, incrementX, rrHeight, 0, -180, Arc2D.OPEN),false);
                    mobilityPath.append(new Arc2D.Double(x + incrementX*4, bottomY + middleY, incrementX, rrHeight, 0, 180, Arc2D.OPEN),false);
                    mobilityPath.append(new Arc2D.Double(x + incrementX*5, bottomY + middleY, incrementX, rrHeight, 0, -180, Arc2D.OPEN),false);
                    mobilityPath.append(new Arc2D.Double(x + incrementX*6, bottomY + middleY, incrementX, rrHeight, 0, 180, Arc2D.OPEN),false);
                }

            }
            //Draw Towed Array Sonar
            else if (ad > 60 && ad < 70)//towed array
            {

                int boxHeight = (int) ((rrHeight * 0.5f) + 0.5f);
                if(boxHeight < 5)
                    strokeWidthNL = 1f;
                bottomY = y + height + (boxHeight / 7);
                mobilityPathFill = new Path2D.Double();
                offsetY = boxHeight / 7;//1;
                centerX = (int)symbolBounds.getCenterX();
                int squareOffset = Math.round(boxHeight * 0.5f);
                middleY = ((boxHeight / 2) + bottomY) + offsetY;//+1 for offset from symbol
                if (ad == SymbolID.Mobility_ShortTowedArray)
                {
                    //subtract 0.5 becase lines 1 pixel thick get aliased into
                    //a line two pixels wide.
                    //line
                    mobilityPath.append(new Line2D.Double(centerX,bottomY - 2,centerX, bottomY + rrHeight + 1), false);
                    //PathUtilties.addLine(mobilityPath, centerX - 1, bottomY - 1, centerX - 1, bottomY + boxHeight + offsetY);

                    //line
                    mobilityPath.append(new Line2D.Double(x, middleY, x + width, middleY), false);
                    //PathUtilties.addLine(mobilityPath, x, middleY, x + width, middleY);
                    
                    //square
                    mobilityPathFill.append(new Rectangle2D.Double(x - squareOffset, bottomY + offsetY, boxHeight, boxHeight), false);
                    //mobilityPathFill.addRect(PathUtilties.makeRectF(x - squareOffset, bottomY + offsetY, boxHeight, boxHeight), Direction.CW);
                    
                    //square
                    mobilityPathFill.append(new Rectangle2D.Double(Math.round(centerX - squareOffset), bottomY + offsetY, boxHeight, boxHeight), false);
                    //mobilityPathFill.addRect(PathUtilties.makeRectF(Math.round(centerX - squareOffset), bottomY + offsetY, boxHeight, boxHeight), Direction.CW);
                    
                    //square
                    mobilityPathFill.append(new Rectangle2D.Double(x + width - squareOffset, bottomY + offsetY, boxHeight, boxHeight), false);
                    //mobilityPathFill.addRect(PathUtilties.makeRectF(x + width - squareOffset, bottomY + offsetY, boxHeight, boxHeight), Direction.CW);
                }
                else if (ad == SymbolID.Mobility_LongTowedArray)
                {
                    int leftX = x + (centerX - x) / 2,
                            rightX = centerX + (x + width - centerX) / 2;

                    //line vertical left
                    mobilityPath.append(new Line2D.Double(leftX, bottomY - 1, leftX, bottomY + offsetY + boxHeight + offsetY), false);
                    //PathUtilties.addLine(mobilityPath, leftX, bottomY - 1, leftX, bottomY + offsetY + boxHeight + offsetY);
                    
                    //line vertical right
                    mobilityPath.append(new Line2D.Double(rightX, bottomY - 1, rightX, bottomY + offsetY + boxHeight + offsetY), false);
                    //PathUtilties.addLine(mobilityPath, rightX, bottomY - 1, rightX, bottomY + offsetY + boxHeight + offsetY);
                    
                    //line horizontal
                    mobilityPath.append(new Line2D.Double(x, middleY, x + width, middleY), false);
                    //PathUtilties.addLine(mobilityPath, x, middleY, x + width, middleY);
                    
                    //square left
                    mobilityPathFill.append(new Rectangle2D.Double(x - squareOffset, bottomY + offsetY, boxHeight, boxHeight), false);
                    //mobilityPathFill.addRect(PathUtilties.makeRectF(x - squareOffset, bottomY + offsetY, boxHeight, boxHeight), Direction.CW);
                    
                    //square middle
                    mobilityPathFill.append(new Rectangle2D.Double(centerX - squareOffset, bottomY + offsetY, boxHeight, boxHeight), false);
                    //mobilityPathFill.addRect(PathUtilties.makeRectF(centerX - squareOffset, bottomY + offsetY, boxHeight, boxHeight), Direction.CW);
                    
                    //square right
                    mobilityPathFill.append(new Rectangle2D.Double(x + width - squareOffset, bottomY + offsetY, boxHeight, boxHeight), false);
                    //mobilityPathFill.addRect(PathUtilties.makeRectF(x + width - squareOffset, bottomY + offsetY, boxHeight, boxHeight), Direction.CW);
                    
                    //square middle left
                    mobilityPathFill.append(new Rectangle2D.Double(leftX - squareOffset, bottomY + offsetY, boxHeight, boxHeight), false);
                    //mobilityPathFill.addRect(PathUtilties.makeRectF(leftX - squareOffset, bottomY + offsetY, boxHeight, boxHeight), Direction.CW);
                    
                    //square middle right
                    mobilityPathFill.append(new Rectangle2D.Double(rightX - squareOffset, bottomY + offsetY, boxHeight, boxHeight), false);
                    //mobilityPathFill.addRect(PathUtilties.makeRectF(rightX - squareOffset, bottomY + offsetY, boxHeight, boxHeight), Direction.CW);
                }
            }
            //build mobility bounds
            mobilityBounds = mobilityPath.getBounds2D();

            if (mobilityPathFill != null)
            {
                Rectangle2D mobilityFillBounds = mobilityPathFill.getBounds2D();
                mobilityBounds = mobilityBounds.createUnion(mobilityFillBounds);
            }

            //grow bounds to handle strokeWidth
            if(ad == SymbolID.Mobility_ShortTowedArray || ad == SymbolID.Mobility_LongTowedArray)
                ShapeUtilities.grow(mobilityBounds, (int)Math.ceil((strokeWidthNL/2)));
            else
                ShapeUtilities.grow(mobilityBounds, (int)Math.ceil((strokeWidth/2)));
            
            imageBounds = imageBounds.createUnion(mobilityBounds);

        }
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Build Echelon">
        //Draw Echelon
        SymbolID.getAmplifierDescriptor(symbolID);
        int intEchelon = SymbolID.getAmplifierDescriptor(symbolID);// SymbolUtilities.getEchelon(symbolID);//symbolID.substring(11, 12);
        String strEchelon = null;
        if (intEchelon > 10 && intEchelon < 29)
        {
            strEchelon = SymbolUtilitiesD.getEchelonText(intEchelon);
        }
        if (strEchelon != null && SymbolUtilitiesD.isInstallation(symbolID) == false
                && SymbolUtilitiesD.hasModifier(symbolID, ModifiersD.B_ECHELON))
        {
            
            int echelonOffset = 2,
                    outlineOffset = RS.getTextOutlineWidth();
            Font modifierFont = RS.getMPLabelFont();
            tiEchelon = new TextInfo(strEchelon, 0, 0, modifierFont,frc);
            echelonBounds = tiEchelon.getTextBounds();

            int y = (int)Math.round(symbolBounds.getY() - echelonOffset);
            int x = (int)(Math.round(symbolBounds.getX()) + (symbolBounds.getWidth() / 2) - (echelonBounds.getWidth() / 2));
            tiEchelon.setLocation(x, y);

            //There will never be lowercase characters in an echelon so trim that fat.    
            //Remove the descent from the bounding box.
            //needed?
            //tiEchelon.getTextOutlineBounds();//.shiftBR(0,Math.round(-(echelonBounds.height()*0.3)));                         

            //make echelon bounds a little more spacious for things like nearby labels and Task Force.
            ShapeUtilities.grow(echelonBounds, outlineOffset);
            //tiEchelon.getTextOutlineBounds();
//                RectUtilities.shift(echelonBounds, x, -outlineOffset);
            //echelonBounds.shift(0,-outlineOffset);// - Math.round(echelonOffset/2));
            tiEchelon.setLocation(x, y - outlineOffset);

            imageBounds = imageBounds.createUnion(echelonBounds);
            
        }
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Build Affiliation Modifier">
        //Draw Echelon
        //not needed for 2525D because built into the SVG files.
        String affiliationModifier = null;
        
        if (RS.getDrawAffiliationModifierAsLabel() == false)
        {
            affiliationModifier = SymbolUtilities.getUnitAffiliationModifier(symbolID, symStd);
        }
        if (affiliationModifier != null)
        {

            int amOffset = 2;
            int outlineOffset = RS.getTextOutlineWidth();

            tiAM = new TextInfo(affiliationModifier, 0, 0, RS.getLabelFont(), frc);
            amBounds = tiAM.getTextBounds();

            int x, y;

            if (echelonBounds != null
                    && ((echelonBounds.getMinX() + echelonBounds.getHeight() > symbolBounds.getMinX() + symbolBounds.getHeight())))
            {
                y = (int)Math.round(symbolBounds.getMinY() - amOffset);
                x = (int)(echelonBounds.getMinX() + echelonBounds.getHeight());
            }
            else
            {
                y = (int)Math.round(symbolBounds.getMinY() - amOffset);
                x = (int)(Math.round(symbolBounds.getMinX() + symbolBounds.getHeight()));
            }
            tiAM.setLocation(x, y);

            //adjust for outline.
            ShapeUtilities.grow(amBounds, outlineOffset);
            ShapeUtilities.offset(amBounds, 0, -outlineOffset);
            tiAM.setLocation(x, y - outlineOffset);

            imageBounds = imageBounds.createUnion(amBounds);
        }//*/
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Build Task Force">
        Rectangle2D tfBounds = null;
        Rectangle2D tfRectangle = null;
        int hqtfd = SymbolID.getHQTFD(symbolID);
        if (SymbolUtilities.isTaskForce(symbolID))
        {
            if (echelonBounds != null)
            {
                tfRectangle = new Rectangle2D.Double(echelonBounds.getX(),
                        echelonBounds.getY(),// + outlineOffset,
                        echelonBounds.getWidth(),
                        symbolBounds.getY()-1);
                tfBounds = (Rectangle2D)tfRectangle.clone();
            }
            else
            {
                int height = (int)Math.round(symbolBounds.getHeight() / 4);
                int width = (int)Math.round(symbolBounds.getHeight() / 3);

                tfRectangle = new Rectangle2D.Double((int) (symbolBounds.getX() + width),
                        (int)(symbolBounds.getY() - height),
                        width,
                        height);

                tfBounds = new Rectangle2D.Double((int)(tfRectangle.getX() + -1),
                        (int)(tfRectangle.getX() - 1),
                        (int)(tfRectangle.getHeight() + 2),
                        (int)(tfRectangle.getHeight() + 2));

            }
            imageBounds = imageBounds.createUnion(tfBounds);
        }
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Build Feint Dummy Indicator">
        Rectangle2D fdiBounds = null;
        Point2D fdiTop = null;
        Point2D fdiLeft = null;
        Point2D fdiRight = null;

        if (SymbolUtilities.isFeintDummy(symbolID)
                || SymbolUtilities.isFeintDummyInstallation(symbolID))
        {
            //create feint indicator /\
            fdiLeft = new Point2D.Double(symbolBounds.getX(), symbolBounds.getY());
            fdiRight = new Point2D.Double((symbolBounds.getX() + symbolBounds.getHeight()), symbolBounds.getY());

            char affiliation = symbolID.charAt(1);
            if (affiliation == ('F')
                    || affiliation == ('A')
                    || affiliation == ('D')
                    || affiliation == ('M')
                    || affiliation == ('J')
                    || affiliation == ('K'))
            {
                fdiTop = new Point2D.Double(Math.round(symbolBounds.getCenterX()), Math.round(symbolBounds.getY() - (symbolBounds.getHeight()* .75f)));
            }
            else
            {
                fdiTop = new Point2D.Double(Math.round(symbolBounds.getCenterX()), Math.round(symbolBounds.getY() - (symbolBounds.getHeight() * .54f)));
            }

            fdiBounds = new Rectangle2D.Double(fdiLeft.getX(), fdiLeft.getY(), 1, 1);
            fdiBounds.createUnion(new Rectangle2D.Double(fdiTop.getX(), fdiTop.getY(),1,1));
            fdiBounds.createUnion(new Rectangle2D.Double(fdiRight.getX(), fdiRight.getY(),1,1));

            if (echelonBounds != null)
            {
                int shiftY = (int)Math.round(symbolBounds.getY() - echelonBounds.getHeight() - 2);
                fdiLeft.setLocation(fdiLeft.getX(), fdiLeft.getY() + shiftY);
                //fdiLeft.offset(0, shiftY);
                fdiTop.setLocation(fdiTop.getX(), fdiTop.getY() + shiftY);
                //fdiTop.offset(0, shiftY);
                fdiRight.setLocation(fdiRight.getX(), fdiRight.getY() + shiftY);
                //fdiRight.offset(0, shiftY);
                fdiBounds.setRect(fdiBounds.getX(), fdiBounds.getY(), fdiBounds.getWidth(), fdiBounds.getHeight());
                //fdiBounds.offset(0, shiftY);
            }

            imageBounds = imageBounds.createUnion(fdiBounds);

        }
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Build Installation">
        Rectangle2D instRectangle = null;
        Rectangle2D instBounds = null;
        if (SymbolUtilities.hasInstallationModifier(symbolID))
        {//the actual installation symbols have the modifier
            //built in.  everything else, we have to draw it.
            //
            ////get indicator dimensions////////////////////////////////
            int width;
            int height;
            char affiliation = symbolID.charAt(1);//SymbolUtilities.getAffiliation(symbolID);

            if (affiliation == 'F'
                    || affiliation == 'A'
                    || affiliation == 'D'
                    || affiliation == 'M'
                    || affiliation == 'J'
                    || affiliation == 'K')
            {
                //4th height, 3rd width
                height = (int)Math.round(symbolBounds.getHeight() / 4);
                width = (int)Math.round(symbolBounds.getHeight() / 3);
            }
            else if (affiliation == 'H' || affiliation == 'S')//hostile,suspect
            {
                //6th height, 3rd width
                height = (int)Math.round(symbolBounds.getHeight() / 6);
                width = (int)Math.round(symbolBounds.getHeight() / 3);
            }
            else if (affiliation == 'N' || affiliation == 'L')//neutral,exercise neutral
            {
                //6th height, 3rd width
                height = (int)Math.round(symbolBounds.getHeight() / 6);
                width = (int)Math.round(symbolBounds.getHeight() / 3);
            }
            else if (affiliation == 'P'
                    || affiliation == 'U'
                    || affiliation == 'G'
                    || affiliation == 'W')
            {
                //6th height, 3rd width
                height = (int)Math.round(symbolBounds.getHeight() / 6);
                width = (int)Math.round(symbolBounds.getHeight() / 3);
            }
            else
            {
                //6th height, 3rd width
                height = (int)Math.round(symbolBounds.getHeight() / 6);
                width = (int)Math.round(symbolBounds.getHeight() / 3);
            }

//                    if(width * 3 < symbolBounds.getHeight())
//                        width++;
            //set installation position/////////////////////////////////
            //set position of indicator
            if (affiliation == 'F'
                    || affiliation == 'A'
                    || affiliation == 'D'
                    || affiliation == 'M'
                    || affiliation == 'J'
                    || affiliation == 'K'
                    || affiliation == 'N'
                    || affiliation == 'L')
            {
                instRectangle = (Rectangle2D)new Rectangle2D.Double((symbolBounds.getX() + width),
                        (symbolBounds.getY() - height),
                        width,
                        height);
            }
            else if (affiliation == 'H' || affiliation == 'S')//hostile,suspect
            {
                instRectangle = new Rectangle2D.Double((int) symbolBounds.getX() + width,
                        Math.round((int) symbolBounds.getY() - (height * 0.15f)),
                        width,
                        height);
            }
            else if (affiliation == 'P'
                    || affiliation == 'U'
                    || affiliation == 'G'
                    || affiliation == 'W')
            {
                instRectangle = new Rectangle2D.Double((int) symbolBounds.getX() + width,
                        Math.round(symbolBounds.getY() - (height * 0.3f)),
                        width,
                        height);
            }
            else
            {
                instRectangle = new Rectangle2D.Double((int) symbolBounds.getX() + width,
                        Math.round(symbolBounds.getY() - (height * 0.3f)),
                        width,
                        height);
            }

            //instRectangle = new SO.Rectangle(symbolBounds.getX() + width,
            //symbolBounds.getY() - height,
            //width,
            //height);
            //generate installation bounds//////////////////////////////
            instBounds = new Rectangle2D.Double(instRectangle.getX() + -1,
                    instRectangle.getY() - 1,
                    instRectangle.getHeight() + 2,
                    instRectangle.getHeight() + 2);

            imageBounds = imageBounds.createUnion(instBounds);

        }
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Build HQ Staff">
        Point2D pt1HQ = null;
        Point2D pt2HQ = null;
        Rectangle2D hqBounds = null;
        //Draw HQ Staff
        if (SymbolUtilities.isHQ(symbolID))
        {

            char affiliation = symbolID.charAt(1);
            //get points for the HQ staff
            if (affiliation == ('F')
                    || affiliation == ('A')
                    || affiliation == ('D')
                    || affiliation == ('M')
                    || affiliation == ('J')
                    || affiliation == ('K')
                    || affiliation == ('N')
                    || affiliation == ('L'))
            {
                pt1HQ = new Point2D.Double(symbolBounds.getX() + 1,
                        (symbolBounds.getY() + symbolBounds.getHeight()));
                pt2HQ = new Point2D.Double(pt1HQ.getX(), (pt1HQ.getY() + symbolBounds.getHeight()));
            }
            else
            {
                pt1HQ = new Point2D.Double((int) symbolBounds.getX() + 1,
                        (int) (symbolBounds.getY() + (symbolBounds.getHeight() / 2)));
                pt2HQ = new Point2D.Double(pt1HQ.getX(), (pt1HQ.getY() + symbolBounds.getHeight()));
            }

            //create bounding rectangle for HQ staff.
            hqBounds = new Rectangle2D.Double(pt1HQ.getX(), pt1HQ.getY(), 2, pt2HQ.getY() - pt1HQ.getY());
            //adjust the image bounds accordingly.
            imageBounds = imageBounds.createUnion(new Rectangle2D.Double(pt1HQ.getX(), pt1HQ.getY(), pt2HQ.getX() - pt1HQ.getX(), pt2HQ.getY() - pt1HQ.getY()));
            //RectUtilities.shiftBR(imageBounds, 0, (int) (pt2HQ.y - imageBounds.bottom));
            //imageBounds.shiftBR(0,pt2HQ.y-imageBounds.bottom);
            //adjust symbol center
            centerPoint.setLocation(pt2HQ.getX(), pt2HQ.getY());
        }

        // </editor-fold>         
        // <editor-fold defaultstate="collapsed" desc="Build DOM Arrow">
        Point2D[] domPoints = null;
        Rectangle2D domBounds = null;
        if (modifiers.containsKey(ModifiersD.Q_DIRECTION_OF_MOVEMENT))
        {
        	String strQ = modifiers.get(ModifiersD.Q_DIRECTION_OF_MOVEMENT);
        	
        	if(strQ != null && SymbolUtilities.isNumber(strQ))
        	{
	            float q = Float.valueOf(strQ);
	
	            boolean isY = (modifiers.containsKey(ModifiersD.Y_LOCATION));
	
	            domPoints = createDOMArrowPoints(symbolID, symbolBounds, centerPoint, q, isY,frc);
	
	            domBounds = new Rectangle2D.Double(domPoints[0].getX(), domPoints[0].getY(), 1, 1);
	
	            Point2D temp = null;
	            for (int i = 1; i < 6; i++)
	            {
	                temp = domPoints[i];
	                if (temp != null)
	                {
	                    domBounds = domBounds.createUnion(new Rectangle2D.Double(temp.getX(), temp.getY(),1,1));
	                }
	            }
	            imageBounds = imageBounds.createUnion(domBounds);
        	}
        }

        // </editor-fold>
        // <editor-fold defaultstate="collapsed" desc="Build Operational Condition Indicator">
        Rectangle2D ociBounds = null;
        int ociOffset = 4;
        if (mobilityBounds != null)
        {
            ociOffset = (int)Math.round((mobilityBounds.getY() + mobilityBounds.getHeight()) - (symbolBounds.getY() + symbolBounds.getHeight())) + 4;
        }
        Rectangle2D ociShape = processOperationalConditionIndicator(symbolID, symbolBounds, ociOffset);
        if (ociShape != null)
        {
            Rectangle2D temp = (Rectangle2D)ociShape.clone();
            ShapeUtilities.grow(temp, 1);
            ociBounds = temp;
            imageBounds = imageBounds.createUnion(ociBounds);
        }

        // </editor-fold>
        // 
        // <editor-fold defaultstate="collapsed" desc="Shift Modifiers">
        //adjust points if necessary
        if (imageBounds.getX() < 0 || imageBounds.getY() < 0)
        {
            int shiftX = (int)Math.abs(imageBounds.getX());
            int shiftY = (int)Math.abs(imageBounds.getY());

            if (hqBounds != null)
            {
                ShapeUtilities.offset(pt1HQ, shiftX, shiftY);
                ShapeUtilities.offset(pt2HQ, shiftX, shiftY);
            }
            if (echelonBounds != null)
            {
                tiEchelon.setLocation((int)tiEchelon.getLocation().getX() + shiftX, (int)tiEchelon.getLocation().getY() + shiftY);
            }
            if (amBounds != null)
            {
                tiAM.setLocation((int)tiAM.getLocation().getX() + shiftX, (int)tiAM.getLocation().getY() + shiftY);
            }
            if (tfBounds != null)
            {
                ShapeUtilities.offset(tfRectangle, shiftX, shiftY);
                ShapeUtilities.offset(tfBounds, shiftX, shiftY);
            }
            if (instBounds != null)
            {
                ShapeUtilities.offset(instRectangle, shiftX, shiftY);
                ShapeUtilities.offset(instBounds, shiftX, shiftY);
            }
            if (fdiBounds != null)
            {
                ShapeUtilities.offset(fdiBounds, shiftX, shiftY);
                ShapeUtilities.offset(fdiLeft, shiftX, shiftY);
                ShapeUtilities.offset(fdiTop, shiftX, shiftY);
                ShapeUtilities.offset(fdiRight, shiftX, shiftY);
            }
            if (ociBounds != null)
            {
                ShapeUtilities.offset(ociBounds, shiftX, shiftY);
                ShapeUtilities.offset(ociShape, shiftX, shiftY);
            }
            if (domBounds != null)
            {
                for (int i = 0; i < 6; i++)
                {
                    Point2D temp = domPoints[i];
                    if (temp != null)
                    {
                        ShapeUtilities.offset(temp, shiftX, shiftY);
                    }
                }
                ShapeUtilities.offset(domBounds, shiftX, shiftY);
            }
            if (mobilityBounds != null)
            {
                //shift mobility points
                ShapeUtilities.offset(mobilityPath, shiftX, shiftY);
                if (mobilityPathFill != null)
                {
                    ShapeUtilities.offset(mobilityPathFill, shiftX, shiftY);
                }
                ShapeUtilities.offset(mobilityBounds, shiftX, shiftY);
            }

            ShapeUtilities.offset(centerPoint, shiftX, shiftY);
            ShapeUtilities.offset(symbolBounds, shiftX, shiftY);
            ShapeUtilities.offset(imageBounds, shiftX, shiftY);
        }
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Draw Modifiers">
//        if(useBuffer.equalstrue)
//         {
//         buffer = _bufferDisplayModifiers;
//         ctx = buffer.getContext('2d');
//         ctx.clearRect(0,0,250,250);
//         }
//         else
//         {
//         buffer = this.createBuffer(imageBounds.getWidth(),imageBounds.getHeight());
//         ctx = buffer.getContext('2d');
//         //}
        BufferedImage bmp = new BufferedImage((int)Math.ceil(imageBounds.getWidth()), (int)Math.ceil(imageBounds.getHeight()), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bmp.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (echelonBounds != null || amBounds != null)
        {
            g2d.setFont(RS.getLabelFont());
            g2d.drawString(tiEchelon.getText(), (int)tiEchelon.getLocation().getX(), (int)tiEchelon.getLocation().getY());
            
            //draw text
            //   ctx.font = RendererSettings.getModifierFont();
        }

        //render////////////////////////////////////////////////////////

        Stroke stroke = new BasicStroke(strokeWidth,BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);
        g2d.setColor(Color.BLACK);

        if (hqBounds != null)
        {
            g2d.setStroke(stroke);
            g2d.drawLine((int)pt1HQ.getX(), (int)pt1HQ.getY(), (int)pt2HQ.getX(), (int)pt2HQ.getY());
        }

        if (tfBounds != null)
        {
            g2d.draw(tfRectangle);
        }

        if (instBounds != null)
        {
            g2d.fill(instRectangle);
        }

        if (echelonBounds != null)
        {
            TextInfo[] aTiEchelon =
            {
                tiEchelon
            };
            renderText(g2d, aTiEchelon, textColor, textBackgroundColor);

            echelonBounds = null;
            tiEchelon = null;
        }

        if (amBounds != null)
        {
            TextInfo[] aTiAM =
            {
                tiAM
            };
            renderText(g2d, aTiAM, textColor, textBackgroundColor);
            amBounds = null;
            tiAM = null;
        }

        if (fdiBounds != null)
        {
            float[] dashArray = {6f,4f};
            
            g2d.setColor(Color.BLACK);

            if (symbolBounds.getHeight() < 20)
            {
                dashArray[0] = 5f;
                dashArray[1] = 3f;
            }
                
            stroke = new BasicStroke(2,BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f,dashArray,0.0f);
            g2d.setStroke(stroke);
            Path2D fdiPath = new Path2D.Double();

            fdiPath.moveTo(fdiLeft.getX(), fdiLeft.getY());
            fdiPath.lineTo(fdiTop.getX(), fdiTop.getY());
            fdiPath.lineTo(fdiRight.getX(), fdiRight.getY());
            g2d.draw(fdiPath);

            fdiBounds = null;

        }

        if (mobilityBounds != null)
        {
            stroke = new BasicStroke(strokeWidth,BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);
            g2d.setColor(Color.BLACK);
            
            //ctx.lineCap = "butt";
            //ctx.lineJoin = "miter";
            if (ad > 30 && ad < 60)//mobility
            {
                //g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            }
            else //NS or NL
            {
                stroke = new BasicStroke(strokeWidthNL,BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);
                //mobilityPaint.setAntiAlias(true);
            }

            g2d.setStroke(stroke);
            g2d.draw(mobilityPath);

            if (mobilityPathFill != null)
            {
                g2d.fill(mobilityPathFill);
            }

            mobilityBounds = null;

        }

        if (ociBounds != null)
        {

            Color statusColor = null;
            int status = SymbolID.getStatus(symbolID);
            
            switch (status) {
                //Fully Capable
                case SymbolID.Status_Present_FullyCapable:
                    statusColor = Color.green;
                    break;
                //Damaged
                case SymbolID.Status_Present_Damaged:
                    statusColor = Color.yellow;
                    break;
                //Destroyed
                case SymbolID.Status_Present_Destroyed:
                    statusColor = Color.red;
                    break;
                //full to capacity(hospital)
                case SymbolID.Status_Present_FullToCapacity:
                    statusColor = Color.blue;
                    break;
                default:
                    break;
            }

            g2d.setColor(Color.black);
            g2d.fillRect((int)ociBounds.getX(), (int)ociBounds.getY(), (int)ociBounds.getWidth(), (int)ociBounds.getHeight());
            g2d.setColor(statusColor);
            g2d.fillRect((int)ociShape.getX(), (int)ociShape.getY(), (int)ociShape.getWidth(), (int)ociShape.getHeight());

            ociBounds = null;
            ociShape = null;
        }

        //draw original icon.        
        //ctx.drawImage(ii.getImage(),symbolBounds.getX(), symbolBounds.getY());
        
        g2d.drawImage(pi.getImage(), (int)symbolBounds.getX(),(int)symbolBounds.getY(),null);// drawBitmap(pi.getImage(), null, symbolBounds, null);

        if (domBounds != null)
        {
            drawDOMArrow(g2d, domPoints);

            domBounds = null;
            domPoints = null;
        }

        // </editor-fold>
        if(bmp != null)
            newii = new SinglePointInfo(bmp, centerPoint, symbolBounds);

        // <editor-fold defaultstate="collapsed" desc="Cleanup">
        // </editor-fold>
        //return newii;    
        if (newii != null)
        {
            return newii;
        }
        else
        {
            return null;
        }
        //*/
        //return null;
    }
    
    /**
     *
     * @param {type} symbolID
     * @param {type} bounds symbolBounds SO.Rectangle
     * @param {type} center SO.Point Location where symbol is centered.
     * @param {type} angle in degrees
     * @param {Boolean} isY Boolean.
     * @returns {Array} of SO.Point. First 3 items are the line. Last three are
     * the arrowhead.
     */
    private static Point2D[] createDOMArrowPoints(String symbolID, Rectangle2D bounds, Point2D center, float angle, boolean isY, FontRenderContext frc)
    {
        Point2D[] arrowPoints = new Point2D.Double[6];
        Point2D pt1 = null;
        Point2D pt2 = null;
        Point2D pt3 = null;

        int length = 40;
        if (SymbolID.isTacticalGraphic(symbolID))//isNBC(symbolID))
        {
            length = (int)Math.round(bounds.getHeight() / 2);
        }
        else
        {
            length = (int)bounds.getHeight();
        }

        //get endpoint
        int dx2, dy2,
                x1, y1,
                x2, y2;

        x1 = (int)Math.round(center.getX());
        y1 = (int)Math.round(center.getY());

        pt1 = new Point2D.Double(x1, y1);

        if (SymbolUtilities.isNBC(symbolID)
                || (SymbolUtilities.isWarfighting(symbolID) && symbolID.charAt(2) == ('G')))
        {
            y1 = (int)(bounds.getY() + bounds.getHeight());
            pt1 = new Point2D.Double(x1, y1);

            if (isY == true && SymbolID.isTacticalGraphic(symbolID))//SymbolUtilities.isNBC(symbolID))//make room for y modifier
            {
                TextInfo ti = new TextInfo("YL", 0, 0, RendererSettings.getInstance().getLabelFont(), frc);
                
                int yModifierOffset = (int) ti.getTextBounds().getHeight();

                yModifierOffset += RendererSettings.getInstance().getTextOutlineWidth();//RS.getTextOutlineWidth();

                ShapeUtilities.offset(pt1, 0, yModifierOffset);
                //pt1.offset(0, yModifierOffset);
            }

            y1 = y1 + length;
            pt2 = new Point2D.Double(x1, y1);
        }

	    //get endpoint given start point and an angle
        //x2 = x1 + (length * Math.cos(radians)));
        //y2 = y1 + (length * Math.sin(radians)));
        angle = angle - 90;//in java, east is zero, we want north to be zero
        double radians = 0;
        radians = (angle * (Math.PI / 180));//convert degrees to radians

        dx2 = x1 + (int) (length * Math.cos(radians));
        dy2 = y1 + (int) (length * Math.sin(radians));
        x2 = Math.round(dx2);
        y2 = Math.round(dy2);

        //create arrowhead//////////////////////////////////////////////////////
        float arrowWidth = 16.0f,//8.0f,//6.5f;//7.0f;//6.5f;//10.0f//default
                theta = 0.423f;//higher value == shorter arrow head//*/

        if (length < 50)
        {
            theta = 0.55f;
        }
        /*float arrowWidth = length * .09f,// 16.0f,//8.0f,//6.5f;//7.0f;//6.5f;//10.0f//default
         theta = length * .0025f;//0.423f;//higher value == shorter arrow head
         if(arrowWidth < 8)
         arrowWidth = 8f;//*/

        int[] xPoints = new int[3];//3
        int[] yPoints = new int[3];//3
        int[] vecLine = new int[2];//2
        int[] vecLeft = new int[2];//2
        double fLength;
        double th;
        double ta;
        double baseX, baseY;

        xPoints[0] = x2;
        yPoints[0] = y2;

        //build the line vector
        vecLine[0] = (xPoints[0] - x1);
        vecLine[1] = (yPoints[0] - y1);

        //build the arrow base vector - normal to the line
        vecLeft[0] = -vecLine[1];
        vecLeft[1] = vecLine[0];

        //setup length parameters
        fLength = Math.sqrt(vecLine[0] * vecLine[0] + vecLine[1] * vecLine[1]);
        th = arrowWidth / (2.0 * fLength);
        ta = arrowWidth / (2.0 * (Math.tan(theta) / 2.0) * fLength);

        //find base of the arrow
        baseX = (xPoints[0] - ta * vecLine[0]);
        baseY = (yPoints[0] - ta * vecLine[1]);

        //build the points on the sides of the arrow
        xPoints[1] = (int) Math.round(baseX + th * vecLeft[0]);
        yPoints[1] = (int) Math.round(baseY + th * vecLeft[1]);
        xPoints[2] = (int) Math.round(baseX - th * vecLeft[0]);
        yPoints[2] = (int) Math.round(baseY - th * vecLeft[1]);

        //line.lineTo((int)baseX, (int)baseY);
        pt3 = new Point2D.Double(Math.round(baseX), Math.round(baseY));

        //arrowHead = new Polygon(xPoints, yPoints, 3);
        arrowPoints[0] = pt1;
        arrowPoints[1] = pt2;
        arrowPoints[2] = pt3;
        arrowPoints[3] = new Point2D.Double(xPoints[0], yPoints[0]);
        arrowPoints[4] = new Point2D.Double(xPoints[1], yPoints[1]);
        arrowPoints[5] = new Point2D.Double(xPoints[2], yPoints[2]);

        return arrowPoints;

    }

    private static void drawDOMArrow(Graphics2D g2d, Point2D[] domPoints)
    {
        Stroke stroke = new BasicStroke(3,BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);

        Path2D domPath = new Path2D.Double() ;
        
        domPath.moveTo(domPoints[0].getX(), domPoints[0].getY());
        if (domPoints[1] != null)
        {
            domPath.lineTo(domPoints[1].getX(), domPoints[1].getY());
        }
        if (domPoints[2] != null)
        {
            domPath.lineTo(domPoints[2].getX(), domPoints[2].getY());
        }
        g2d.setStroke(stroke);
        g2d.setColor(Color.BLACK);
        g2d.draw(domPath);

        domPath.reset();
        
        domPath.moveTo(domPoints[3].getX(), domPoints[3].getY());
        domPath.lineTo(domPoints[4].getX(), domPoints[4].getY());
        domPath.lineTo(domPoints[5].getX(), domPoints[5].getY());
        g2d.fill(domPath);
    }
    
    private static Rectangle2D processOperationalConditionIndicator(String symbolID, Rectangle2D symbolBounds, int offsetY)
    {
        // <editor-fold defaultstate="collapsed" desc="Operational Condition Indicator">
        //create Operational Condition Indicator
        //set color
        Rectangle2D bar = null;
        int status;
        //Color statusColor;
        int barSize = 0;
        int pixelSize = (int)symbolBounds.getHeight();

        status = SymbolID.getStatus(symbolID);//  symbolID.charAt(3);
        

        /*if(_statusColorMap[status] !== undefined)
         statusColor = _statusColorMap[status];
         else
         statusColor = null;*/
        if (status == SymbolID.Status_Present_FullyCapable || 
                status == SymbolID.Status_Present_Damaged || 
                status == SymbolID.Status_Present_Destroyed || 
                status == SymbolID.Status_Present_FullToCapacity)
        {
            if (pixelSize > 0)
            {
                barSize = Math.round(pixelSize / 5);
            }

            if (barSize < 2)
            {
                barSize = 2;
            }

            offsetY += Math.round(symbolBounds.getY() + symbolBounds.getHeight());

            bar = new Rectangle2D.Double(symbolBounds.getX() + 2, offsetY, Math.round(symbolBounds.getWidth()) - 3, barSize);
            //bar = ShapeUtilities.makeRect(symbolBounds.getX() + 2, offsetY, Math.round(symbolBounds.getWidth()) - 3, barSize);
            
//bar = new SO.Rectangle(symbolBounds.getX()+1, offsetY, Math.round(symbolBounds.getWidth())-2,barSize);
            /*ctx.lineColor = '#000000';
             ctx.lineWidth = 1;
             ctx.fillColor = statusColor;
             bar.fill(ctx);
             bar.grow(1);
             bar.stroke(ctx);
            
             imageBounds.union(bar.getBounds());//*/
        }

        return bar;

        // </editor-fold>
    }
    
    public static ISinglePointInfo processUnitTextModifiers(String symbolCode, ISinglePointInfo pi, Map<String, String> modifiers)
    {
        ISinglePointInfo piNew = null;
        {
            try
            {
                Rectangle2D symbolBounds = pi.getSymbolBounds();
                BufferedImage biCore = pi.getImage();
                Rectangle2D imageBounds = new Rectangle2D.Double(0,0,biCore.getWidth(),biCore.getHeight());
                Point2D centerPoint = pi.getSymbolCenterPoint();
                
                int ss = SymbolID.getSymbolSet(symbolCode);
                if(ss >= 10 && ss <=20)
                    piNew = processGroundUnitTextModifiers(pi, symbolCode, modifiers);
                       
            }
            catch(Exception exc)
            {
                ErrorLogger.LogException("ModifierRenderer", "processUnitTextModifiers", exc);
            }
            
        }
        return piNew;
    }
    
    /*private static ISinglePointInfo processGroundUnitTextModifiers(String symbolCode, ISinglePointInfo pi, Map<String, String> modifiers, boolean hasTextModifiers)
    {
        ISinglePointInfo piNew = null;
        {
            try
            {
                Rectangle2D symbolBounds = pi.getSymbolBounds();
                BufferedImage biCore = pi.getImage();
                Rectangle2D imageBounds = new Rectangle2D.Double(0,0,biCore.getWidth(),biCore.getHeight());
                Point2D centerPoint = pi.getSymbolCenterPoint();
            }
            catch(Exception exc)
            {
                ErrorLogger.LogException("ModifierRenderer", "processUnitTextModifiers", exc);
            }
        }
        return piNew;
    }//*/
    
    public static ISinglePointInfo processGroundUnitTextModifiers(ISinglePointInfo ii, String symbolID, Map<String, String> modifiers)
    {

        int bufferXL = 5;
        int bufferXR = 5;
        int bufferY = 2;
        int bufferText = 2;
        int x = 0;
        int y = 0;//best y
        int cpofNameX = 0;
        ISinglePointInfo newii = null;
        int alpha = -1;
        
        Color textColor = Color.BLACK;
        Color textBackgroundColor = null;

        ArrayList<TextInfo> tiArray = new ArrayList<TextInfo>(modifiers.size());


        int symStd = RS.getSymbologyStandard();
        if (modifiers.containsKey(MilStdAttributes.SymbologyStandard))
        {
            symStd = Integer.parseInt(modifiers.get(MilStdAttributes.SymbologyStandard));
        }
        if (modifiers.containsKey(MilStdAttributes.Alpha))
        {
            alpha = Integer.parseInt(modifiers.get(MilStdAttributes.Alpha));
        }

        Rectangle2D labelBounds = null;
        int labelWidth, labelHeight;

        Rectangle2D bounds = (Rectangle2D)(ii.getSymbolBounds().clone());
        Rectangle2D symbolBounds = (Rectangle2D)(ii.getSymbolBounds().clone());
        Point2D centerPoint = ii.getSymbolCenterPoint();
        Rectangle2D imageBounds = new Rectangle(0,0,ii.getImage().getWidth(), ii.getImage().getHeight());
        Rectangle2D imageBoundsOld = (Rectangle2D)imageBounds.clone();

        String echelon = SymbolUtilities.getEchelon(symbolID);
        String echelonText = SymbolUtilities.getEchelonText(echelon);
        String amText = SymbolUtilities.getUnitAffiliationModifier(symbolID, symStd);

        //make room for echelon & mobility.
        if (modifiers.containsKey(ModifiersUnits.Q_DIRECTION_OF_MOVEMENT) || SymbolUtilitiesD.hasModifierUnit(symbolID, ModifiersUnits.Q_DIRECTION_OF_MOVEMENT)==false)
        {
            //if no DOM, we can just use the image bounds
            bounds = new Rectangle2D.Double(imageBounds.getX(), symbolBounds.getY(), imageBounds.getWidth(), symbolBounds.getHeight());
        }
        else //dom exists so we need to change our math
        {
            if (echelonText != null || amText != null)
            {
                bounds = new Rectangle2D.Double(imageBounds.getX(), bounds.getY(),
                        imageBounds.getWidth(), bounds.getHeight());
            }
            else if (symbolID.substring(10, 12).equals("MR"))
            {
                x = (int)-(Math.round((symbolBounds.getWidth() - 1) / 7) * 2);
                if (x < bounds.getX())
                {
                    bounds.setRect(x, 0, bounds.getWidth(), bounds.getHeight());
                    //bounds.shiftTL(x,0);
                }
            }
        }

        cpofNameX = (int)(bounds.getX() + bounds.getWidth() + bufferXR);

        //check if text is too tall:
        boolean byLabelHeight = false;
        labelHeight = (int) (_modifierFontHeight + 0.5);/* RendererUtilities.measureTextHeight(RendererSettings.getModifierFontName(),
         RendererSettings.getModifierFontSize(),
         RendererSettings.getModifierFontStyle()).fullHeight;*/

        int maxHeight = (int)(bounds.getHeight());
        if ((labelHeight * 3) > maxHeight)
        {
            byLabelHeight = true;
        }

        //Affiliation Modifier being drawn as a display modifier
        String affiliationModifier = null;
        if (RS.getDrawAffiliationModifierAsLabel() == true)
        {
            affiliationModifier = SymbolUtilities.getUnitAffiliationModifier(symbolID, symStd);
        }
        if (affiliationModifier != null)
        {   //Set affiliation modifier
            modifiers.put(ModifiersUnits.E_FRAME_SHAPE_MODIFIER, affiliationModifier);
            //modifiers[ModifiersUnits.E_FRAME_SHAPE_MODIFIER] = affiliationModifier;
        }//*/

        //Check for Valid Country Code
        if (SymbolUtilities.hasValidCountryCode(symbolID))
        {
            modifiers.put(ModifiersUnits.CC_COUNTRY_CODE, symbolID.substring(12, 14));
            //modifiers[ModifiersUnits.CC_COUNTRY_CODE] = symbolID.substring(12,14);
        }

        //            int y0 = 0;//W            E/F
        //            int y1 = 0;//X/Y          G
        //            int y2 = 0;//V/AD/AE      H/AF
        //            int y3 = 0;//T            M CC
        //            int y4 = 0;//Z            J/K/L/N/P
        //
        //            y0 = bounds.y - 0;
        //            y1 = bounds.y - labelHeight;
        //            y2 = bounds.y - (labelHeight + (int)bufferText) * 2;
        //            y3 = bounds.y - (labelHeight + (int)bufferText) * 3;
        //            y4 = bounds.y - (labelHeight + (int)bufferText) * 4;
        // <editor-fold defaultstate="collapsed" desc="Build Modifiers">
        String modifierValue = null;
        TextInfo tiTemp = null;
        
        //if(ModifiersUnits.C_QUANTITY in modifiers 
        if (modifiers.containsKey(ModifiersUnits.C_QUANTITY)
                && SymbolUtilitiesD.hasModifierUnit(symbolID, ModifiersUnits.C_QUANTITY))
        {
            String text = modifiers.get(ModifiersUnits.C_QUANTITY);
            if(text != null)
            {
	            //bounds = armyc2.c2sd.renderer.utilities.RendererUtilities.getTextOutlineBounds(_modifierFont, text, new SO.Point(0,0));
	            tiTemp = new TextInfo(text, 0, 0, _modifierFont, _frc);
	            labelBounds = RectUtilities.toRectangle(tiTemp.getTextBounds());
	            labelWidth = (int)labelBounds.getWidth();
	            x = (int)Math.round((symbolBounds.getX() + (symbolBounds.getWidth() * 0.5f)) - (labelWidth * 0.5f));
	            y = (int)Math.round(symbolBounds.getY() - bufferY - tiTemp.getDescent());
	            tiTemp.setLocation(x, y);
	            tiArray.add(tiTemp);
            }
        }

        //if(ModifiersUnits.X_ALTITUDE_DEPTH in modifiers || ModifiersUnits.Y_LOCATION in modifiers)
        if (modifiers.containsKey(ModifiersUnits.X_ALTITUDE_DEPTH) || modifiers.containsKey(ModifiersUnits.Y_LOCATION))
        {
            modifierValue = null;

            String xm = null,
                    ym = null;

            if (modifiers.containsKey(ModifiersUnits.X_ALTITUDE_DEPTH) && SymbolUtilitiesD.hasModifierUnit(symbolID, ModifiersUnits.X_ALTITUDE_DEPTH))
            {
                xm = modifiers.get(ModifiersUnits.X_ALTITUDE_DEPTH);// xm = modifiers.X;
            }
            if (modifiers.containsKey(ModifiersUnits.Y_LOCATION))
            {
                ym = modifiers.get(ModifiersUnits.Y_LOCATION);// ym = modifiers.Y;
            }
            if (xm == null && ym != null)
            {
                modifierValue = ym;
            }
            else if (xm != null && ym == null)
            {
                modifierValue = xm;
            }
            else if (xm != null && ym != null)
            {
                modifierValue = xm + "  " + ym;
            }

            if(modifierValue != null)
            {
	            tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, _frc);
	            labelBounds = tiTemp.getTextBounds();
	            labelWidth = (int)labelBounds.getWidth();
	
	            if (!byLabelHeight)
	            {
	                x = (int)Math.round(bounds.getX() - labelBounds.getWidth() - bufferXL);
	                y = (int)Math.round(bounds.getY() + labelHeight - tiTemp.getDescent());
	            }
	            else
	            {
	                x = (int)(bounds.getX() - labelBounds.getWidth() - bufferXL);
	
	                y = (int)(bounds.getHeight());
	                y = (int) ((y * 0.5) + (labelHeight * 0.5));
	
	                y = y - ((labelHeight + bufferText));
	                y = (int)(bounds.getY() + y);
	            }
	
	            tiTemp.setLocation(x, y);
	            tiArray.add(tiTemp);
            }
        }

        if (modifiers.containsKey(ModifiersUnits.G_STAFF_COMMENTS) && SymbolUtilitiesD.hasModifierUnit(symbolID, ModifiersUnits.G_STAFF_COMMENTS))
        {
            modifierValue = modifiers.get(ModifiersUnits.G_STAFF_COMMENTS);

            if(modifierValue != null)
            {
	            tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, _frc);
	            labelBounds = tiTemp.getTextBounds();
	            labelWidth = (int)labelBounds.getWidth();
	
	            x = (int)(bounds.getX() + bounds.getWidth() + bufferXR);
	            if (!byLabelHeight)
	            {
	                y = (int)(bounds.getY() + labelHeight - tiTemp.getDescent());
	            }
	            else
	            {
	                y = (int)(bounds.getHeight());
	                y = (int) ((y * 0.5) + (labelHeight * 0.5));
	
	                y = y - ((labelHeight + bufferText));
	                y = (int)(bounds.getY() + y);
	            }
	
	            tiTemp.setLocation(x, y);
	            tiArray.add(tiTemp);
	
	            //Concession for cpof name label
	            if ((x + labelWidth + 3) > cpofNameX)
	            {
	                cpofNameX = x + labelWidth + 3;
	            }
            }
        }

        if ((modifiers.containsKey(ModifiersUnits.V_EQUIP_TYPE)) ||
                (modifiers.containsKey(ModifiersUnits.AD_PLATFORM_TYPE)) ||
                (modifiers.containsKey(ModifiersUnits.AE_EQUIPMENT_TEARDOWN_TIME)))
        {
            String vm = null,
                    adm = null,
                    aem = null;

            if (modifiers.containsKey(ModifiersUnits.V_EQUIP_TYPE) && SymbolUtilitiesD.hasModifierUnit(symbolID, ModifiersUnits.V_EQUIP_TYPE))
            {
                vm = modifiers.get(ModifiersUnits.V_EQUIP_TYPE);
            }
            if (modifiers.containsKey(ModifiersUnits.AD_PLATFORM_TYPE) && SymbolUtilitiesD.hasModifierUnit(symbolID, ModifiersUnits.AD_PLATFORM_TYPE))
            {
                adm = modifiers.get(ModifiersUnits.AD_PLATFORM_TYPE);
            }
            if (modifiers.containsKey(ModifiersUnits.AE_EQUIPMENT_TEARDOWN_TIME) && SymbolUtilitiesD.hasModifierUnit(symbolID, ModifiersUnits.AE_EQUIPMENT_TEARDOWN_TIME))
            {
                aem = modifiers.get(ModifiersUnits.AE_EQUIPMENT_TEARDOWN_TIME);
            }

            modifierValue = "";
            if(vm != null && vm.equals("") == false)
                modifierValue = vm;
            if(adm != null && adm.equals("") == false)
                modifierValue += " " + adm;
            if(aem != null && aem.equals("") == false)
                modifierValue += " " + aem;

            if(modifierValue != null)
                modifierValue = modifierValue.trim();
            if(modifierValue != null && modifierValue.equals("") == false)
            {
	            tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, _frc);
	            labelBounds = tiTemp.getTextBounds();
	            labelWidth = (int)labelBounds.getWidth();
	
	            x = (int)(bounds.getX() - labelBounds.getWidth() - bufferXL);
	
	            y = (int)(bounds.getHeight());
	            y = (int) ((y * 0.5f) + ((labelHeight - tiTemp.getDescent()) * 0.5f));
	            y = (int)bounds.getY() + y;
	
	            tiTemp.setLocation(x, y);
	            tiArray.add(tiTemp);
            }
        }

        if (modifiers.containsKey(ModifiersUnits.H_ADDITIONAL_INFO_1) || modifiers.containsKey(ModifiersUnits.AF_COMMON_IDENTIFIER))
        {
            modifierValue = "";
            String hm = "",
                    afm = "";

            hm = modifiers.get(ModifiersUnits.H_ADDITIONAL_INFO_1);
            if (modifiers.containsKey(ModifiersUnits.H_ADDITIONAL_INFO_1))
            {
                hm = modifiers.get(ModifiersUnits.H_ADDITIONAL_INFO_1);
            }
            if (modifiers.containsKey(ModifiersUnits.AF_COMMON_IDENTIFIER) && SymbolUtilitiesD.hasModifierUnit(symbolID, ModifiersUnits.AF_COMMON_IDENTIFIER))
            {
                afm = modifiers.get(ModifiersUnits.AF_COMMON_IDENTIFIER);
            }

            modifierValue = hm + " " + afm;
            modifierValue = modifierValue.trim();

            if(modifierValue != null && modifierValue.equals("") == false)
            {
	            tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, _frc);
	            labelBounds = tiTemp.getTextBounds();
	            labelWidth = (int)labelBounds.getWidth();
	
	            x = (int)(bounds.getX() + bounds.getWidth() + bufferXR);
	
	            y = (int)(bounds.getHeight());
	            y = (int) ((y * 0.5) + ((labelHeight - tiTemp.getDescent()) * 0.5));
	            y = (int)bounds.getY() + y;
	
	            tiTemp.setLocation(x, y);
	            tiArray.add(tiTemp);
	
	            //Concession for cpof name label
	            if ((x + labelWidth + 3) > cpofNameX)
	            {
	                cpofNameX = x + labelWidth + 3;
	            }
            }
        }

        if (modifiers.containsKey(ModifiersUnits.T_UNIQUE_DESIGNATION_1))
        {
            modifierValue = modifiers.get(ModifiersUnits.T_UNIQUE_DESIGNATION_1);

            if(modifierValue != null)
            {
	            tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, _frc);
	            labelBounds = tiTemp.getTextBounds();
	            labelWidth = (int)labelBounds.getWidth();
	
	            if (!byLabelHeight)
	            {
	                x = (int)bounds.getX() - labelWidth - bufferXL;
	                y = (int)(bounds.getY() + bounds.getHeight());
	            }
	            else
	            {
	                x = (int)(bounds.getX() - labelWidth - bufferXL);
	
	                y = (int)(bounds.getHeight());
	                y = (int) ((y * 0.5) + (labelHeight * 0.5));
	
	                y = (int)(y + ((labelHeight + bufferText) - tiTemp.getDescent()));
	                y = (int)(bounds.getY() + y);
	            }
	
	            tiTemp.setLocation(x, y);
	            tiArray.add(tiTemp);
            }
        }

        if (modifiers.containsKey(ModifiersUnits.M_HIGHER_FORMATION) || modifiers.containsKey(ModifiersUnits.CC_COUNTRY_CODE))
        {
            modifierValue = "";

            if (modifiers.containsKey(ModifiersUnits.M_HIGHER_FORMATION) && SymbolUtilitiesD.hasModifierUnit(symbolID, ModifiersUnits.M_HIGHER_FORMATION))
            {
                modifierValue += modifiers.get(ModifiersUnits.M_HIGHER_FORMATION);
            }
            if (modifiers.containsKey(ModifiersUnits.CC_COUNTRY_CODE))
            {
                if (modifierValue.length() > 0)
                {
                    modifierValue += " ";
                }
                modifierValue += modifiers.get(ModifiersUnits.CC_COUNTRY_CODE);
            }

            if(modifierValue.equals("")==false)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, _frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                x = (int)(bounds.getX() + bounds.getWidth() + bufferXR);
                if (!byLabelHeight)
                {
                    y = (int)(bounds.getY() + bounds.getHeight());
                }
                else
                {
                    y = (int)(bounds.getHeight());
                    y = (int) ((y * 0.5) + (labelHeight * 0.5));

                    y = (int)(y + ((labelHeight + bufferText - tiTemp.getDescent())));
                    y = (int)bounds.getY() + y;
                }

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

                //Concession for cpof name label
                if ((x + labelWidth + 3) > cpofNameX)
                {
                    cpofNameX = x + labelWidth + 3;
                }
            }
        }

        if (modifiers.containsKey(ModifiersUnits.Z_SPEED) && SymbolUtilitiesD.hasModifierUnit(symbolID, ModifiersUnits.Z_SPEED))
        {
            modifierValue = modifiers.get(ModifiersUnits.Z_SPEED);

            if(modifierValue != null)
            {
	            tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, _frc);
	            labelBounds = tiTemp.getTextBounds();
	            labelWidth = (int)labelBounds.getWidth();
	
	            x = (int)(bounds.getX() - labelWidth - bufferXL);
	            if (!byLabelHeight)
	            {
	                y = (int)(Math.round(bounds.getY() + bounds.getHeight()+ labelHeight + bufferText));
	            }
	            else
	            {
	                y = (int)(bounds.getHeight());
	                y = (int) ((y * 0.5) + (labelHeight * 0.5));
	
	                y = (int)(y + ((labelHeight + bufferText) * 2) - (tiTemp.getDescent() * 2));
	                y = (int)Math.round(bounds.getY() + y);
	            }
	
	            tiTemp.setLocation(x, y);
	            tiArray.add(tiTemp);
            }
        }

        if (modifiers.containsKey(ModifiersUnits.J_EVALUATION_RATING) 
                || modifiers.containsKey(ModifiersUnits.K_COMBAT_EFFECTIVENESS)//
                || modifiers.containsKey(ModifiersUnits.L_SIGNATURE_EQUIP)//
                || modifiers.containsKey(ModifiersUnits.N_HOSTILE)//
                || modifiers.containsKey(ModifiersUnits.P_IFF_SIF))//
        {
            modifierValue = null;

            String jm = null,
                    km = null,
                    lm = null,
                    nm = null,
                    pm = null;

            if (modifiers.containsKey(ModifiersUnits.J_EVALUATION_RATING))
            {
                jm = modifiers.get(ModifiersUnits.J_EVALUATION_RATING);
            }
            if (modifiers.containsKey(ModifiersUnits.K_COMBAT_EFFECTIVENESS) && SymbolUtilitiesD.hasModifierUnit(symbolID, ModifiersUnits.K_COMBAT_EFFECTIVENESS))
            {
                km = modifiers.get(ModifiersUnits.K_COMBAT_EFFECTIVENESS);
            }
            if (modifiers.containsKey(ModifiersUnits.L_SIGNATURE_EQUIP) && SymbolUtilitiesD.hasModifierUnit(symbolID, ModifiersUnits.L_SIGNATURE_EQUIP))
            {
                lm = modifiers.get(ModifiersUnits.L_SIGNATURE_EQUIP);
            }
            if (modifiers.containsKey(ModifiersUnits.N_HOSTILE) && SymbolUtilitiesD.hasModifierUnit(symbolID, ModifiersUnits.N_HOSTILE))
            {
                nm = modifiers.get(ModifiersUnits.N_HOSTILE);
            }
            if (modifiers.containsKey(ModifiersUnits.P_IFF_SIF) && SymbolUtilitiesD.hasModifierUnit(symbolID, ModifiersUnits.P_IFF_SIF))
            {
                pm = modifiers.get(ModifiersUnits.P_IFF_SIF);
            }

            modifierValue = "";
            if (jm != null && jm.equals("") == false)
            {
                modifierValue = modifierValue + jm;
            }
            if (km != null && km.equals("") == false)
            {
                modifierValue = modifierValue + " " + km;
            }
            if (lm != null && lm.equals("") == false)
            {
                modifierValue = modifierValue + " " + lm;
            }
            if (nm != null && nm.equals("") == false)
            {
                modifierValue = modifierValue + " " + nm;
            }
            if (pm != null && pm.equals("") == false)
            {
                modifierValue = modifierValue + " " + pm;
            }

            if (modifierValue.length() > 2 && modifierValue.charAt(0) == ' ')
            {
                modifierValue = modifierValue.substring(1);
            }

            modifierValue = modifierValue.trim();

            if(modifierValue.equals("")==false)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, _frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                x = (int)(bounds.getX() + bounds.getWidth() + bufferXR);
                if (!byLabelHeight)
                {
                    y = (int)(Math.round(bounds.getY() + bounds.getHeight()+ labelHeight + bufferText));
                }
                else
                {
                    y = (int)(bounds.getHeight());
                    y = (int) ((y * 0.5) + (labelHeight * 0.5));

                    y = (int)(y + ((labelHeight + bufferText) * 2) - (tiTemp.getDescent() * 2));
                    y = (int)Math.round(bounds.getY() + y);
                }

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

                //Concession for cpof name label
                if ((x + labelWidth + 3) > cpofNameX)
                {
                    cpofNameX = x + labelWidth + 3;
                }
            }

        }

        if (modifiers.containsKey(ModifiersUnits.W_DTG_1))
        {
            modifierValue = modifiers.get(ModifiersUnits.W_DTG_1);

            if(modifierValue != null)
            {
	            tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, _frc);
	            labelBounds = tiTemp.getTextBounds();
	            labelWidth = (int)labelBounds.getWidth();
	
	            if (!byLabelHeight)
	            {
	                x = (int)(bounds.getX() - labelWidth - bufferXL);
	                y = (int)(bounds.getY() - bufferY - tiTemp.getDescent());
	            }
	            else
	            {
	                x = (int)(bounds.getX() - labelWidth - bufferXL);
	
	                y = (int)(bounds.getHeight());
	                y = (int) ((y * 0.5) + (labelHeight * 0.5));
	
	                y = y - ((labelHeight + bufferText) * 2);
	                y = (int)bounds.getY() + y;
	            }
	
	            tiTemp.setLocation(x, y);
	            tiArray.add(tiTemp);
            }
        }

        if (modifiers.containsKey(ModifiersUnits.F_REINFORCED_REDUCED) || modifiers.containsKey(ModifiersUnits.E_FRAME_SHAPE_MODIFIER))
        {
            modifierValue = null;
            String E = null,
                    F = null;

            if (modifiers.containsKey(ModifiersUnits.E_FRAME_SHAPE_MODIFIER))
            {
                E = modifiers.get(ModifiersUnits.E_FRAME_SHAPE_MODIFIER);
                modifiers.remove(ModifiersUnits.E_FRAME_SHAPE_MODIFIER);
            }
            if (modifiers.containsKey(ModifiersUnits.F_REINFORCED_REDUCED) && SymbolUtilitiesD.hasModifierUnit(symbolID, ModifiersUnits.F_REINFORCED_REDUCED))
            {
                F = modifiers.get(ModifiersUnits.F_REINFORCED_REDUCED);
            }

            if (E != null && E.equals("") == false)
            {
                modifierValue = E;
            }

            if (F != null && F.equals("") == false)
            {
                if (F.toUpperCase(Locale.US) == ("R"))
                {
                    F = "(+)";
                }
                else if (F.toUpperCase(Locale.US) == ("D"))
                {
                    F = "(-)";
                }
                else if (F.toUpperCase(Locale.US) == ("RD"))
                {
                    F = "(" + (char) (177) + ")";
                }
            }

            if (F != null && F.equals("") == false)
            {
                if (modifierValue != null && modifierValue.equals("") == false)
                {
                    modifierValue = modifierValue + " " + F;
                }
                else
                {
                    modifierValue = F;
                }
            }

            if(modifierValue != null)
            {
	            tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, _frc);
	            labelBounds = tiTemp.getTextBounds();
	            labelWidth = (int)labelBounds.getWidth();
	
	            if (!byLabelHeight)
	            {
	                x = (int)(bounds.getX() + bounds.getWidth() + bufferXR);
	                y = (int)(bounds.getY() - bufferY - tiTemp.getDescent());
	            }
	            else
	            {
	                x = (int)(bounds.getX() + bounds.getWidth() + bufferXR);
	
	                y = (int)(bounds.getHeight());
	                y = (int) ((y * 0.5) + (labelHeight * 0.5));
	
	                y = y - ((labelHeight + bufferText) * 2);
	                y = (int)bounds.getY() + y;
	            }
	
	            tiTemp.setLocation(x, y);
	            tiArray.add(tiTemp);
	
	            //Concession for cpof name label
	            if ((x + labelWidth + 3) > cpofNameX)
	            {
	                cpofNameX = x + labelWidth + 3;
	            }
            }
        }

        if (modifiers.containsKey(ModifiersUnits.AA_SPECIAL_C2_HQ) && SymbolUtilitiesD.hasModifierUnit(symbolID, ModifiersUnits.AA_SPECIAL_C2_HQ))
        {
            modifierValue = modifiers.get(ModifiersUnits.AA_SPECIAL_C2_HQ);

            if(modifierValue != null)
            {
	            tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, _frc);
	            labelBounds = tiTemp.getTextBounds();
	            labelWidth = (int)labelBounds.getWidth();
	
	            x = (int) ((symbolBounds.getX() + (symbolBounds.getWidth() * 0.5f)) - (labelWidth * 0.5f));
	
	            y = (int)(symbolBounds.getHeight());//checkpoint, get box above the point
	            y = (int) ((y * 0.5) + ((labelHeight - tiTemp.getDescent()) * 0.5));
	            y = (int)symbolBounds.getY() + y;
	
	            tiTemp.setLocation(x, y);
	            tiArray.add(tiTemp);
            }
        }
        
        if (modifiers.containsKey(ModifiersUnits.SCC_SONAR_CLASSIFICATION_CONFIDENCE) && SymbolUtilitiesD.hasModifierUnit(symbolID, ModifiersUnits.SCC_SONAR_CLASSIFICATION_CONFIDENCE))
        {
        	int scc = 0;
            modifierValue = modifiers.get(ModifiersUnits.SCC_SONAR_CLASSIFICATION_CONFIDENCE);
            
            if(modifierValue != null && SymbolUtilities.isNumber(modifierValue) && SymbolUtilities.hasModifier(symbolID, ModifiersUnits.SCC_SONAR_CLASSIFICATION_CONFIDENCE))
            {
	            tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, _frc);
	            labelBounds = tiTemp.getTextBounds();
	            labelWidth = (int)labelBounds.getWidth();
	
	            x = (int) ((symbolBounds.getX() + (symbolBounds.getWidth() * 0.5f)) - (labelWidth * 0.5f));
	
	            double yPosition = getYPositionForSCC(symbolID);
	            y = (int)(bounds.getHeight());//checkpoint, get box above the point
                y = (int)(((y * yPosition) + ((labelHeight-tiTemp.getDescent()) * 0.5)));
                y = (int)bounds.getY() + y;
	
	            tiTemp.setLocation(x, y);
	            tiArray.add(tiTemp);
            }
        }

        if (modifiers.containsKey(ModifiersUnits.CN_CPOF_NAME_LABEL))
        {
            modifierValue = modifiers.get(ModifiersUnits.CN_CPOF_NAME_LABEL);

            if(modifierValue != null)
            {
	            tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, _frc);
	            labelBounds = tiTemp.getTextBounds();
	            labelWidth = (int)labelBounds.getWidth();
	
	            x = cpofNameX;
	
	            y = (int)(bounds.getHeight());//checkpoint, get box above the point
	            y = (int) ((y * 0.5) + (labelHeight * 0.5));
	            y = (int)bounds.getY() + y;
	
	            tiTemp.setLocation(x, y);
	            tiArray.add(tiTemp);
            }
        }

        // </editor-fold>
        // <editor-fold defaultstate="collapsed" desc="Shift Points and Draw">
        Rectangle2D modifierBounds = null;
        if (tiArray != null && tiArray.size() > 0)
        {

            //build modifier bounds/////////////////////////////////////////
            modifierBounds = tiArray.get(0).getTextOutlineBounds();
            int size = tiArray.size();
            TextInfo tempShape = null;
            for (int i = 1; i < size; i++)
            {
                tempShape = tiArray.get(i);
                Rectangle2D.union(modifierBounds, tempShape.getTextOutlineBounds(), modifierBounds);
            }

        }

        if (modifierBounds != null)
        {

            Rectangle2D.union(imageBounds, modifierBounds, imageBounds);

            //shift points if needed////////////////////////////////////////
            if (imageBounds.getX() < 0 || imageBounds.getY() < 0)
            {
                int shiftX = (int)Math.round(Math.abs(imageBounds.getX())),
                        shiftY = (int)Math.round(Math.abs(imageBounds.getY()));

                //shift mobility points
                int size = tiArray.size();
                TextInfo tempShape = null;
                for (int i = 0; i < size; i++)
                {
                    tempShape = tiArray.get(i);
                    tempShape.shift(shiftX, shiftY);
                }
                RectUtilities.shift(modifierBounds, shiftX, shiftY);
                //modifierBounds.shift(shiftX,shiftY);

                //shift image points
                centerPoint.setLocation(centerPoint.getX() + shiftX, centerPoint.getY() + shiftY);
                RectUtilities.shift(symbolBounds, shiftX, shiftY);
                RectUtilities.shift(imageBounds, shiftX, shiftY);
                RectUtilities.shift(imageBoundsOld, shiftX, shiftY);
                /*centerPoint.shift(shiftX, shiftY);
                 symbolBounds.shift(shiftX, shiftY);
                 imageBounds.shift(shiftX, shiftY);
                 imageBoundsOld.shift(shiftX, shiftY);//*/
            }

            BufferedImage bmp = new BufferedImage((int)imageBounds.getWidth(), (int)Math.round(imageBounds.getHeight()), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = (Graphics2D)bmp.getGraphics();
            //old
            //Bitmap bmp = Bitmap.createBitmap(imageBounds.getWidth(), imageBounds.height(), Config.ARGB_8888);
            //Canvas ctx = new Canvas(bmp);
            //old

            //render////////////////////////////////////////////////////////
            //draw original icon with potential modifiers.
            g2d.drawImage((Image)ii.getImage(), (int)imageBoundsOld.getX(), (int)imageBoundsOld.getY(), null);
            //ctx.drawBitmap(ii.getImage(), imageBoundsOld.getX(), imageBoundsOld.getY(), null);
            //ctx.drawImage(ii.getImage(),imageBoundsOld.getX(),imageBoundsOld.getY());
            
            
            if (modifiers.containsKey(MilStdAttributes.TextColor))
            {
                textColor = SymbolUtilities.getColorFromHexString(modifiers.get(MilStdAttributes.TextColor));
                if(alpha > -1)
                    textColor = new Color(textColor.getRed(), textColor.getGreen(), textColor.getBlue(), alpha);
            }
            if (modifiers.containsKey(MilStdAttributes.TextBackgroundColor))
            {
                textBackgroundColor = SymbolUtilities.getColorFromHexString(modifiers.get(MilStdAttributes.TextBackgroundColor));
                if(alpha > -1)
                    textBackgroundColor = new Color(textColor.getRed(), textColor.getGreen(), textColor.getBlue(), alpha);
            }

            renderText(g2d, tiArray, textColor, textBackgroundColor);

            newii = new SinglePointInfo(bmp, centerPoint, symbolBounds);

        }
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Cleanup">
        tiArray = null;
        tiTemp = null;
        //tempShape = null;
        imageBoundsOld = null;
        //ctx = null;
        //buffer = null;
        // </editor-fold>

        return newii;
    }
    
    private static void renderText(Graphics2D g2d, ArrayList<TextInfo> tiArray, Color color, Color backgroundColor)
    {
        renderText(g2d, (TextInfo[]) tiArray.toArray(new TextInfo[0]), color, backgroundColor);
    }

    /**
     * 
     * @param g2d
     * @param tiArray
     * @param color
     * @param backgroundColor 
     */
    public static void renderText(Graphics2D g2d, TextInfo[] tiArray, Color color, Color backgroundColor)
    {
        /*for (TextInfo textInfo : tiArray) 
         {
         ctx.drawText(textInfo.getText(), textInfo.getLocation().x, textInfo.getLocation().y, _modifierFont);	
         }*/

        int size = tiArray.length;

        int tbm = RendererSettings.getInstance().getTextBackgroundMethod();
        int outlineWidth = RendererSettings.getInstance().getTextOutlineWidth();

        if (color == null)
        {
            color = Color.BLACK;
        }

        Color outlineColor = null;
        
        if(backgroundColor != null)
            outlineColor = backgroundColor;
        else
            outlineColor = SymbolDraw.getIdealTextBackgroundColor(color);
        
        g2d.setFont(RendererSettings.getInstance().getLabelFont());
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        if (tbm == RendererSettings.TextBackgroundMethod_OUTLINE_QUICK)
        {
            //draw text outline
            //_modifierFont.setStyle(Style.FILL);
//            _modifierFont.setStrokeWidth(RS.getTextOutlineWidth());
//            _modifierFont.setColor(outlineColor.toInt());
            if (outlineWidth > 0)
            {
                g2d.setColor(outlineColor);
                for (int i = 0; i < size; i++)
                {
                    TextInfo textInfo = tiArray[i];
                    if (outlineWidth > 0)
                    {
                        for (int j = 1; j <= outlineWidth; j++)
                        {
                            if (j % 2 == 1)
                            {
                                g2d.drawString(textInfo.getText(), (float)textInfo.getLocation().getX() - j, (float)textInfo.getLocation().getY());
                                g2d.drawString(textInfo.getText(), (float)textInfo.getLocation().getX() + j, (float)textInfo.getLocation().getY());
                                g2d.drawString(textInfo.getText(), (float)textInfo.getLocation().getX(), (float)textInfo.getLocation().getY() + j);
                                g2d.drawString(textInfo.getText(), (float)textInfo.getLocation().getX(), (float)textInfo.getLocation().getY() - j);
                            }
                            else
                            {
                                g2d.drawString(textInfo.getText(), (float)textInfo.getLocation().getX() - j, (float)textInfo.getLocation().getY() - j);
                                g2d.drawString(textInfo.getText(), (float)textInfo.getLocation().getX() + j, (float)textInfo.getLocation().getY() - j);
                                g2d.drawString(textInfo.getText(), (float)textInfo.getLocation().getX() - j, (float)textInfo.getLocation().getY() + j);
                                g2d.drawString(textInfo.getText(), (float)textInfo.getLocation().getX() + j, (float)textInfo.getLocation().getY() + j);
                            }
                        }
                    }
                }
            }
            //draw text
            g2d.setColor(color);

            for (int j = 0; j < size; j++)
            {
                TextInfo textInfo = tiArray[j];
                g2d.drawString(textInfo.getText(), (float)textInfo.getLocation().getX(), (float)textInfo.getLocation().getY());
            }
        }
        else if (tbm == RendererSettings.TextBackgroundMethod_OUTLINE)
        {
            /*
        	//draw text outline
            //draw text outline
            _modifierFont.setStyle(Style.STROKE);
            _modifierFont.setStrokeWidth(RS.getTextOutlineWidth());
            _modifierFont.setColor(outlineColor.toInt());
            g2d.setColor(outlineColor);
            
            if (outlineWidth > 0)
            {
                Stroke stroke = new BasicStroke(outlineWidth,BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);
                for (int i = 0; i < size; i++)
                {
                    TextInfo textInfo = tiArray[i];
                    textInfo.
                    ctx.drawText(textInfo.getText(), textInfo.getLocation().x, textInfo.getLocation().y, _modifierFont);
                }
            }
            //draw text
            _modifierFont.setColor(color.toInt());
            _modifierFont.setStyle(Style.FILL);
            for (int j = 0; j < size; j++)
            {
                TextInfo textInfo = tiArray[j];
                ctx.drawText(textInfo.getText(), textInfo.getLocation().x, textInfo.getLocation().y, _modifierFont);
            }//*/
            g2d.setColor(color);
            for (int j = 0; j < size; j++)
            {
                TextInfo textInfo = tiArray[j];
                g2d.drawString(textInfo.getText(), (float)textInfo.getLocation().getX(), (float)textInfo.getLocation().getY());
            }
        }
        else if (tbm == RendererSettings.TextBackgroundMethod_COLORFILL)
        {
            g2d.setColor(outlineColor);

            //draw rectangle
            for (int k = 0; k < size; k++)
            {
                TextInfo textInfo = tiArray[k];
                
                g2d.fill(textInfo.getTextOutlineBounds());
            }
            //draw text
            g2d.setColor(color);
            
            for (int j = 0; j < size; j++)
            {
                TextInfo textInfo = tiArray[j];
                g2d.drawString(textInfo.getText(), (float)textInfo.getLocation().getX(), (float)textInfo.getLocation().getY());
            }
        }
        else if (tbm == RendererSettings.TextBackgroundMethod_NONE)
        {
            g2d.setColor(color);
            for (int j = 0; j < size; j++)
            {
                TextInfo textInfo = tiArray[j];
                g2d.drawString(textInfo.getText(), (float)textInfo.getLocation().getX(), (float)textInfo.getLocation().getY());
            }
        }
    }
    
    public static boolean hasDisplayModifiers(String symbolID, Map<String,String> modifiers)
    {
        boolean hasModifiers = false;
        
        int status = SymbolID.getStatus(symbolID);
        int si = SymbolID.getStandardIdentity(symbolID);
        int ss = SymbolID.getSymbolSet(symbolID);
        

        if(ss == SymbolID.SymbolSet_ControlMeasure || (ss >= 45 && ss <= 47))
        {
            //if hasQ and is CBRN event
        }
        else 
        {
            if(si > 9 && si < 30)//affiliation modifier
            {
                hasModifiers = true;
            }
            else if(status == SymbolID.Status_Present_FullyCapable || 
                    status == SymbolID.Status_Present_Damaged || 
                    status == SymbolID.Status_Present_Destroyed || 
                    status == SymbolID.Status_Present_FullToCapacity)
            {
                hasModifiers = true;
            }
            else if(SymbolID.getHQTFD(symbolID) > 0)
            {
                hasModifiers = true;
            }
            else if(SymbolID.getAmplifierDescriptor(symbolID) > 10)//echelon and mobility
            {
                hasModifiers = true;
            }
            else if(modifiers.containsKey(ModifiersD.Q_DIRECTION_OF_MOVEMENT) && ss >= 50 && ss <= 54)//has Q and isn't SigInt
            {
                hasModifiers = true;
            }
        }
        
        return hasModifiers;
    }
    
    public static boolean hasTextModifiers(String symbolID, Map<String,String> modifiers, Map<String,String> attributes)
    {
        boolean hasTextModifiers = false;
        
        return hasTextModifiers;
    }
    
    /*
    Might not be necessary in 2525D
    */
    private static double getYPositionForSCC(String symbolID)
    {
        double yPosition = 0.32;
        String temp = symbolID.substring(4, 10);
        char affiliation = symbolID.charAt(1);

        if(temp.equals("WMGC--"))//GROUND (BOTTOM) MILCO
        {
            if(affiliation == 'H' || 
                    affiliation == 'S')//suspect
                yPosition = 0.29;
            else if(affiliation == 'N' ||
                    affiliation == 'L')//exercise neutral
                yPosition = 0.32;
            else if(affiliation == 'F' ||
                    affiliation == 'A' ||//assumed friend
                    affiliation == 'D' ||//exercise friend
                    affiliation == 'M' ||//exercise assumed friend
                    affiliation == 'K' ||//faker
                    affiliation == 'J')//joker
                yPosition = 0.32;
            else
                yPosition = 0.34;
        }
        else if(temp.equals("WMMC--"))//MOORED MILCO
        {
            if(affiliation == 'H' || 
                    affiliation == 'S')//suspect
                yPosition = 0.29;
            else if(affiliation == 'N' ||
                    affiliation == 'L')//exercise neutral
                yPosition = 0.32;
            else if(affiliation == 'F' ||
                    affiliation == 'A' ||//assumed friend
                    affiliation == 'D' ||//exercise friend
                    affiliation == 'M' ||//exercise assumed friend
                    affiliation == 'K' ||//faker
                    affiliation == 'J')//joker
                yPosition = 0.32;
            else
                yPosition = 0.34;
        }
        else if(temp.equals("WMFC--"))//FLOATING MILCO
        {
            if(affiliation == 'H' || 
                    affiliation == 'S')//suspect
                yPosition = 0.29;
            else if(affiliation == 'N' ||
                    affiliation == 'L')//exercise neutral
                yPosition = 0.32;
            else if(affiliation == 'F' ||
                    affiliation == 'A' ||//assumed friend
                    affiliation == 'D' ||//exercise friend
                    affiliation == 'M' ||//exercise assumed friend
                    affiliation == 'K' ||//faker
                    affiliation == 'J')//joker
                yPosition = 0.32;
            else
                yPosition = 0.34;
        }
        else if(temp.equals("WMC---"))//GENERAL MILCO
        {
            if(affiliation == 'H' || 
                    affiliation == 'S')//suspect
                yPosition = 0.35;
            else if(affiliation == 'N' ||
                    affiliation == 'L')//exercise neutral
                yPosition = 0.39;
            else if(affiliation == 'F' ||
                    affiliation == 'A' ||//assumed friend
                    affiliation == 'D' ||//exercise friend
                    affiliation == 'M' ||//exercise assumed friend
                    affiliation == 'K' ||//faker
                    affiliation == 'J')//joker
                yPosition = 0.39;
            else
                yPosition = 0.39;
        }
        
        return yPosition;
    }
    
            
}
