/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ArmyC2.C2SD.Plugin2525D;

import ArmyC2.C2SD.RendererPluginInterface.ISinglePointInfo;
import ArmyC2.C2SD.RendererPluginInterface.ISinglePointRenderer;
import ArmyC2.C2SD.RendererPluginInterface.SinglePointInfo;
import ArmyC2.C2SD.Utilities.ErrorLogger;
import ArmyC2.C2SD.Utilities.MilStdAttributes;
import com.kitfox.svg.Group;
import com.kitfox.svg.SVGCache;
import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGElement;
import com.kitfox.svg.SVGUniverse;
import com.kitfox.svg.animation.AnimationElement;
import com.kitfox.svg.app.beans.SVGIcon;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 *
 * @author User
 */
public class SinglePoint2525DRenderer implements ISinglePointRenderer{

    public static final String RENDERER_ID = "2525D";
    private BufferedImage _buffer = null;
    private FontRenderContext _fontRenderContext = null;
    
    public SinglePoint2525DRenderer()
    {
        init();
    }
    
    private void init()
    {
        try
        {
            SVGLookup.getInstance();
            ModifierRendererD.getInstance();
            
            if(_buffer == null)
            {
                _buffer = new BufferedImage(8,8,BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = (Graphics2D)_buffer.createGraphics();
                _fontRenderContext = g2d.getFontRenderContext();
            }
        }
        catch(Exception exc)
        {
            System.out.println(exc.getMessage());
            System.out.println(Utilities.getStackTrace(exc));
        }
    }
    
    @Override
    public String getRendererID() {
        return RENDERER_ID;
    }

    @Override
    public Boolean canRender(String string, Map<String, String> map) {
        return true;
    }

    @Override
    public ISinglePointInfo render(String symbolID, Map<String, String> modifiers) {
        ISinglePointInfo pi = null;//new SinglePointInfo(null, x, y);
        
        
        if(modifiers == null)
        {
            modifiers = new HashMap<String, String>();
        }
        
        if(SymbolID.isTacticalGraphic(symbolID))
        {
            //30022500001310010000
            pi = this.renderSPTG(symbolID, modifiers);
        }
        else
        {
            //30020100001107000000
            pi = this.renderUnit(symbolID, modifiers);
        }    

        return pi;
    }
    
    private ISinglePointInfo renderUnit(String symbolID, Map<String, String> map) {
        ISinglePointInfo pi = null;//new SinglePointInfo(null, x, y);
        
        try
        {
            
            double pixelSize = 50;
            double scale = 1.0;
            String lineColor = null;//SymbolUtilitiesD.getLineColorOfAffiliation(symbolID);
            String fillColor = null;
            
            boolean keepUnitRatio = true;
            boolean asIcon = false;
            boolean hasDisplayModifiers = false;
            boolean hasTextModifiers = false;
            
            if(map != null)
            {
                if(map.containsKey(MilStdAttributes.PixelSize))
                    pixelSize = Double.valueOf(map.get(MilStdAttributes.PixelSize));
                if(map.containsKey(MilStdAttributes.LineColor))
                    lineColor = map.get(MilStdAttributes.LineColor);
                if(map.containsKey(MilStdAttributes.FillColor))
                    fillColor = map.get(MilStdAttributes.FillColor);
                if(map.containsKey("ICON"))
                    asIcon = Boolean.parseBoolean(map.get("ICON"));
            }
            
            
            
            //lineColor = "#00FF00";
            //fillColor = "#FF0000";
            //stroke-opacity
            //fill-opacity="0.4"
            //opacity
            
            SVGElement eFrame = SVGLookup.getInstance().getSVGElement(SVGLookup.getFrameID(symbolID));
            SVGElement eMain = SVGLookup.getInstance().getSVGElement(SVGLookup.getMainIconID(symbolID));
            SVGElement eMod1 = SVGLookup.getInstance().getSVGElement(SVGLookup.getMod1ID(symbolID));
            SVGElement eMod2 = SVGLookup.getInstance().getSVGElement(SVGLookup.getMod2ID(symbolID));
            SVGElement eOctagon = SVGLookup.getInstance().getSVGElement("octagon");
            

            //Set Line and Fill Colors
            SVGElement epath = null;
            if(eFrame != null)
            {
                epath = eFrame.getChild(0);
            }
            else
            {
                ErrorLogger.LogMessage("Couldn't find frame for: " + SVGLookup.getFrameID(symbolID) + " for symbol " + symbolID, Level.SEVERE, false);
                //return null;
            }
            if(lineColor != null && epath.hasAttribute("stroke", AnimationElement.AT_XML))
                epath.setAttribute("stroke", AnimationElement.AT_XML, lineColor);
            if(fillColor != null && epath.hasAttribute("fill", AnimationElement.AT_XML))
                epath.setAttribute("fill", AnimationElement.AT_XML, fillColor);//*/
                      
            //Set eFrame to epath
            //This is because we only want the core symbol.  
            //None of the modifiers like J,K,X,X that sometimes appears to the 
            //top right of the symbol.
            //eFrame = new Group(). .epath;
            
            //clear old data
            SVGCache.getSVGUniverse().clear();
            //load based SVG xml
            StringReader reader = new StringReader(makeDynamicSVG());
            URI uriSymbol = SVGCache.getSVGUniverse().loadSVG(reader, "myImage"); 
        
            SVGUniverse svgu = SVGCache.getSVGUniverse();
            SVGDiagram sd = svgu.getDiagram(uriSymbol);

            //create groups to add to SVG
            Group gScale = new Group();
            Group gTranslate = new Group();
            Group gSymbol = new Group();
            
            //set default transforms
            gTranslate.addAttribute("transform", AnimationElement.AT_XML, "translate(0,0)");
            gScale.addAttribute("transform", AnimationElement.AT_XML, "scale(1)");
            gSymbol.addAttribute("fill", AnimationElement.AT_CSS, "#000000");

            
            Group frame = (Group)eFrame;
            Group octagon = (Group)eOctagon;
            Group main = (Group)eMain;
            Group mod1 = null;
            Group mod2 = null;
            if(eMod1 != null)
                mod1 = (Group)eMod1;
            if(eMod2 != null)
                mod2 = (Group)eMod2;
            //echelon
            //amplifier (mobility/towed array)
            //GQ/TF/FD
            //OCA
            
            //bbox of frame
            Rectangle2D bbox = null;
            if(SymbolID.isMETOC(symbolID))
                bbox = main.getBoundingBox();
            else if(frame != null)
                bbox = frame.getBoundingBox();
            else
                bbox = octagon.getBoundingBox();
            Rectangle2D bboxO = octagon.getBoundingBox();
            Point2D centerPoint = new Point2D.Double(bboxO.getCenterX(),bboxO.getCenterY());
            System.out.println("Size: " + String.valueOf(pixelSize));
            System.out.println("Frame bbox: " + bbox.toString());
            System.out.println("Octagon bbox: " + bboxO.toString());
            System.out.println("Center point: " + centerPoint.toString());
            //if(HQ staff, will need to get new center point)
            
            //determine transformations needed.
            double x = bbox.getX();
            double y = bbox.getY();
            double w = bbox.getWidth();
            double h = bbox.getHeight();
            //determine scale
            if(w>h)
            {
                scale = pixelSize/w;
            }
            else
            {
                scale = pixelSize/h;
            }
            //determine translation
            double transX = 0;
            double transY = 0;
            Path2D path = new Path2D.Double(bbox);
            path.transform(AffineTransform.getScaleInstance(scale, scale));
            bbox = path.getBounds2D();
            System.out.println("Scaled Frame bbox: " + bbox.toString());
            transX = bbox.getX() * -1;
            transY = bbox.getY() * -1;
            bbox.setRect(0, 0, bbox.getWidth(), bbox.getHeight());
            //get centerpoint of scaled octagon
            Path2D pathO = new Path2D.Double(bboxO);
            pathO.transform(AffineTransform.getScaleInstance(scale, scale));
            bboxO = pathO.getBounds2D();
            Point2D center = new Point2D.Double(bboxO.getWidth()/2 + bboxO.getX() + transX,bboxO.getHeight()/2 + bboxO.getY() + transY);
            //Math.ceil(bbox.getX())-bbox.get
            System.out.println("Translated Scaled Frame bbox: " + bbox.toString());
            System.out.println("Scaled Octagon bbox: " + bboxO.toString());
            System.out.println("Scaled Center point: " + center.toString());
            
            
            //apply transformations.
            String strScale = "scale(" + String.valueOf(scale) + ")";
            gScale.setAttribute("transform", AnimationElement.AT_XML, strScale);
            String translatef = "translate(" + String.valueOf(transX) + "," + String.valueOf(transY) + ")";
            gTranslate.setAttribute("transform", AnimationElement.AT_XML, translatef);
            

            //work done, add all groups to SVG.
            sd.getRoot().loaderAddChild(null, gTranslate);
            gTranslate.loaderAddChild(null, gScale);
            gScale.loaderAddChild(null, gSymbol);
            if(frame != null)
                gSymbol.loaderAddChild(null, frame);
            gSymbol.loaderAddChild(null, main);//*/
            if(mod1 != null)
                gSymbol.loaderAddChild(null, mod1);
            if(mod2 != null)
                gSymbol.loaderAddChild(null, mod2);

            
            //trying to clip
            /*bbox.setRect(bbox.getCenterX(), bbox.getY(), bbox.getWidth()/2, bbox.getHeight());
            Rectangle2D vr = sd.getViewRect();
            sd.setIgnoringClipHeuristic(true);
            sd.setDeviceViewport(bbox.getBounds());//*/
            
            SVGIcon icon = new SVGIcon();
            icon.setAntiAlias(true);
            icon.setInterpolation(SVGIcon.INTERP_BICUBIC);
            icon.setSvgURI(uriSymbol);
            icon.setPreferredSize(new Dimension((int)pixelSize,(int)pixelSize));
            
            //BufferedImage foo = new BufferedImage(400,400,BufferedImage.TYPE_4BYTE_ABGR);
            double imageWidth = bbox.getWidth();
            double imageHeight = bbox.getHeight();
            if(pixelSize != imageWidth)
                imageWidth = Math.ceil(imageWidth);
            if(pixelSize != imageHeight)
                imageHeight = Math.ceil(imageHeight);
            
            BufferedImage foo = new BufferedImage((int)imageWidth,(int)imageHeight,BufferedImage.TYPE_4BYTE_ABGR);
            System.out.println("BufferedImage: x=" + String.valueOf(foo.getWidth()) + ", y=" + String.valueOf(foo.getHeight()));
            System.out.println("--------------------------------");
            
            
            //graphics for BufferedImage
            Graphics2D gfoo = (Graphics2D)foo.createGraphics();
            gfoo.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            gfoo.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            //draw test frame
            //gfoo.setColor(Color.red);
            //gfoo.drawRect(1, 1, 48, 48);
            
            //draw SVG to image
            sd.render(gfoo);
            
            //icon.setScaleToFit(true);
            icon.paintIcon(null, gfoo, 0, 0);
                        
            double width = bbox.getWidth();
            double height = bbox.getHeight();
            
            pi = new SinglePointInfo(foo, center, bbox);
            
            ISinglePointInfo piNew = null;            
               
            ////////////////////////////////////////////////////////////////////
            hasDisplayModifiers = ModifierRendererD.hasDisplayModifiers(symbolID, map);
            //hasTextModifiers = ModifierRendererD.hasTextModifiers(symbolID, map);
            //process display modifiers
            if (hasDisplayModifiers)
            {
                piNew = ModifierRendererD.processUnitDisplayModifiers(symbolID, pi, map, hasDisplayModifiers, _fontRenderContext);
            }

            if (piNew != null)
            {
                pi = piNew;
            }
            piNew = null;

            //process test modifiers
            //if (hasTextModifiers)
            //{
                piNew = ModifierRendererD.processUnitTextModifiers(symbolID, pi, map);
            //}

            if (piNew != null)
            {
                pi = piNew;
            }
            piNew = null;
                
            
        }
        catch(Exception exc)
        {
            System.out.println(exc.getMessage());
            System.out.println(Utilities.getStackTrace(exc));
        }
        
        return pi;
    }
    
    private ISinglePointInfo renderSPTG(String symbolID, Map<String, String> map)
    {
        double pixelSize = 50;
        double scale = 1.0;
        String lineColor = null;//SymbolUtilitiesD.getLineColorOfAffiliation(symbolID);
        String fillColor = null;

        boolean keepUnitRatio = true;
        boolean asIcon = false;
        boolean hasDisplayModifiers = false;
        boolean hasTextModifiers = false;
        
        ISinglePointInfo pi = null;

        if(map != null)
        {
            if(map.containsKey(MilStdAttributes.PixelSize))
                pixelSize = Double.valueOf(map.get(MilStdAttributes.PixelSize));
            if(map.containsKey(MilStdAttributes.LineColor))
                lineColor = map.get(MilStdAttributes.LineColor);
            if(map.containsKey(MilStdAttributes.FillColor))
                fillColor = map.get(MilStdAttributes.FillColor);
            if(map.containsKey("ICON"))
                asIcon = Boolean.parseBoolean(map.get("ICON"));
        }
        try
        {
            lineColor = SymbolUtilitiesD.colorToHexString(SymbolUtilitiesD.getLineColorOfAffiliation(symbolID), false);
            //fillColor = "#FF0000";
            //stroke-opacity
            //fill-opacity="0.4"
            //opacity
            
            SVGElement eMain = SVGLookup.getInstance().getSVGElement(SVGLookup.getMainIconID(symbolID));
            

            //Set Line and Fill Colors
            SVGElement epath = null;
            if(eMain != null)
            {
                epath = eMain.getChild(0);
            }
            else
            {
                ErrorLogger.LogMessage("Couldn't find icon for: " + symbolID, Level.SEVERE, false);
                return null;
            }
            if(lineColor != null && epath.hasAttribute("stroke", AnimationElement.AT_XML))
                epath.setAttribute("stroke", AnimationElement.AT_XML, lineColor);
            if(fillColor != null && epath.hasAttribute("fill", AnimationElement.AT_XML))
                epath.setAttribute("fill", AnimationElement.AT_XML, fillColor);//*/
            
            
            
            //Set eFrame to epath
            //This is because we only want the core symbol.  
            //None of the modifiers like J,K,X,X that sometimes appears to the 
            //top right of the symbol.
            //eFrame = new Group(). .epath;
            
            //clear old data
            SVGCache.getSVGUniverse().clear();
            //load based SVG xml
            StringReader reader = new StringReader(makeDynamicSVG());
            URI uriSymbol = SVGCache.getSVGUniverse().loadSVG(reader, "myImage"); 
        
            SVGUniverse svgu = SVGCache.getSVGUniverse();
            SVGDiagram sd = svgu.getDiagram(uriSymbol);

            //create groups to add to SVG
            Group gScale = new Group();
            Group gTranslate = new Group();
            Group gSymbol = new Group();
            
            //set default transforms
            gTranslate.addAttribute("transform", AnimationElement.AT_XML, "translate(0,0)");
            gScale.addAttribute("transform", AnimationElement.AT_XML, "scale(1)");
            gSymbol.addAttribute("fill", AnimationElement.AT_CSS, "#000000");

            Group main = (Group)eMain;

            //echelon
            //amplifier (mobility/towed array)
            //GQ/TF/FD
            //OCA
            
            //bbox of frame
            Rectangle2D bbox = main.getBoundingBox();
            Point2D centerPoint = new Point2D.Double(bbox.getCenterX(), bbox.getCenterY());//SymbolDimensions;///////////////////////////
            System.out.println("Size: " + String.valueOf(pixelSize));
            System.out.println("Frame bbox: " + bbox.toString());
            System.out.println("Center point: " + centerPoint.toString());
            //if(HQ staff, will need to get new center point)
            
            //determine transformations needed.
            double x = bbox.getX();
            double y = bbox.getY();
            double w = bbox.getWidth();
            double h = bbox.getHeight();
            //determine scale
            if(w>h)
            {
                scale = pixelSize/w;
            }
            else
            {
                scale = pixelSize/h;
            }
            //determine translation
            double transX = 0;
            double transY = 0;
            Path2D path = new Path2D.Double(bbox);
            path.transform(AffineTransform.getScaleInstance(scale, scale));
            bbox = path.getBounds2D();
            System.out.println("Scaled Frame bbox: " + bbox.toString());
            transX = bbox.getX() * -1;
            transY = bbox.getY() * -1;
            bbox.setRect(0, 0, bbox.getWidth(), bbox.getHeight());
            centerPoint = SymbolUtilitiesD.getCMCenterPoint(symbolID, bbox.getWidth(), bbox.getHeight());

            System.out.println("Translated Scaled Frame bbox: " + bbox.toString());
            System.out.println("Scaled Center point: " + centerPoint.toString());
            
            
            //apply transformations.
            String strScale = "scale(" + String.valueOf(scale) + ")";
            gScale.setAttribute("transform", AnimationElement.AT_XML, strScale);
            String translatef = "translate(" + String.valueOf(transX) + "," + String.valueOf(transY) + ")";
            gTranslate.setAttribute("transform", AnimationElement.AT_XML, translatef);
            

            //work done, add all groups to SVG.
            sd.getRoot().loaderAddChild(null, gTranslate);
            gTranslate.loaderAddChild(null, gScale);
            gScale.loaderAddChild(null, gSymbol);
            gSymbol.loaderAddChild(null, main);//*/
            

            SVGIcon icon = new SVGIcon();
            icon.setAntiAlias(true);
            icon.setInterpolation(SVGIcon.INTERP_BICUBIC);
            icon.setSvgURI(uriSymbol);
            icon.setPreferredSize(new Dimension((int)pixelSize,(int)pixelSize));
            
            //BufferedImage foo = new BufferedImage(400,400,BufferedImage.TYPE_4BYTE_ABGR);
            double imageWidth = bbox.getWidth();
            double imageHeight = bbox.getHeight();
            if(pixelSize != imageWidth)
                imageWidth = Math.ceil(imageWidth);
            if(pixelSize != imageHeight)
                imageHeight = Math.ceil(imageHeight);
            
            BufferedImage foo = new BufferedImage((int)imageWidth,(int)imageHeight,BufferedImage.TYPE_4BYTE_ABGR);
            System.out.println("BufferedImage: x=" + String.valueOf(foo.getWidth()) + ", y=" + String.valueOf(foo.getHeight()));
            System.out.println("--------------------------------");
            
            
            //graphics for BufferedImage
            Graphics2D gfoo = (Graphics2D)foo.createGraphics();
            gfoo.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            gfoo.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            //draw test frame
            //gfoo.setColor(Color.red);
            //gfoo.drawRect(1, 1, 48, 48);
            
            //draw SVG to image
            sd.render(gfoo);
            icon.paintIcon(null, gfoo, 0, 0);
                        
            double width = bbox.getWidth();
            double height = bbox.getHeight();
            
            pi = new SinglePointInfo(foo, centerPoint, bbox);
            
            ISinglePointInfo piNew = null;            
               
            ////////////////////////////////////////////////////////////////////
            hasDisplayModifiers = ModifierRendererD.hasDisplayModifiers(symbolID, map);
            //hasTextModifiers = ModifierRendererD.hasTextModifiers();
            //process display modifiers
            if (hasDisplayModifiers)
            {
                piNew = ModifierRendererD.processUnitDisplayModifiers(symbolID, pi, map, hasTextModifiers, _fontRenderContext);
            }

            if (piNew != null)
            {
                pi = piNew;
            }
            piNew = null;

            //process test modifiers
            if (hasTextModifiers)
            {
                //piNew = ModifierRendererD.processUnitTextModifiers(symbolID, pi, map, hasTextModifiers);
            }

            if (piNew != null)
            {
                pi = piNew;
            }
            piNew = null;
        }
        catch(Exception exc)
        {
            ErrorLogger.LogException("SinglePoint2525DRenderer", "RenderSPTG", exc);
            return null;
        }
        return pi;
    }
    
    private String makeDynamicSVG()
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        
        pw.println("<svg width=\"35\" height=\"35\" style=\"fill:none;stroke-width:16\">");
//        pw.println("    <g id=\"txfm\" >");
//        pw.println("        <g id=\"scale\" >");
//        pw.println("            <g id=\"symbol\" fill=\"#000000\"> ");
//        pw.println("            </g>");
//        pw.println("        </g>");
//        pw.println("    </g>");
        pw.println("</svg>");
        
        pw.close();
        return sw.toString();
    }
    
    private Rectangle2D getSVGSymbolBounds(Group frame, 
                                            Group amplifier,
                                            Group echelon,
                                            Group HQTFFD,
                                            Group OCA)
    {
        Rectangle2D bbox = null;
        try
        {
            if(frame != null)
            {
                bbox = frame.getBoundingBox();

                if(amplifier != null)
                    bbox = bbox.createUnion(amplifier.getBoundingBox());
                if(echelon != null)
                    bbox = bbox.createUnion(echelon.getBoundingBox());
                if(HQTFFD != null)
                    bbox = bbox.createUnion(HQTFFD.getBoundingBox());
                if(OCA != null)
                    bbox = bbox.createUnion(OCA.getBoundingBox());
            }
        }
        catch(Exception exc)
        {
            System.out.println(exc.getMessage());
            System.out.println(Utilities.getStackTrace(exc));
        }
        return bbox;
    }
    
}
