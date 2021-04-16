/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ArmyC2.C2SD.Plugin2525D;

import ArmyC2.C2SD.Utilities.MilStdAttributes;
import com.kitfox.svg.Group;
import com.kitfox.svg.SVGCache;
import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGElement;
import com.kitfox.svg.SVGUniverse;
import com.kitfox.svg.animation.AnimationElement;
import com.kitfox.svg.app.beans.SVGIcon;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//SVG space 612x792
//anchor point
//Point2D.Double[305.0, 394.5]
//dimensions
//1.5 = 360
//1 = 240

/**
 *
 * @author User
 */
public class SVGLookup {
    
    private static SVGLookup _instance = null;
    private static Map<String, String> _SVG = null;
    private static Map<String, SVGElement> _SVGE = null;
    
    private SVGLookup()
    {
        init();
    }
    
    public static synchronized SVGLookup getInstance()
    {
        if(_instance == null)
            _instance = new SVGLookup();

        return _instance;
    }
    
    private void init()
    {
        //load svg xml into MAP
        //ArmyC2\C2SD\Plugin2525D\SVG
        
        _SVG = new HashMap<String, String>();
        _SVGE = new HashMap<String, SVGElement>();
        
        
        //String xmlPathD = "/SVG/2525D.svg";
        String xmlPathD = "ArmyC2/C2SD/Plugin2525D/SVG/2525D.svg";
        
        try
        {
            //InputStream xmlStreamD = this.getClass().getClassLoader().getResourceAsStream(xmlPathD);
            InputStream xmlStreamD = Thread.currentThread().getContextClassLoader().getResourceAsStream(xmlPathD);
       
            //clear old data
            SVGCache.getSVGUniverse().clear();
            //load based SVG xml
            StringReader reader = new StringReader(makeDynamicSVG());
            URI uriSymbol = SVGCache.getSVGUniverse().loadSVG(xmlStreamD, "SVG"); 
        
            SVGUniverse svgu = SVGCache.getSVGUniverse();
            SVGDiagram sd = svgu.getDiagram(uriSymbol);
            List<SVGElement> sl = new ArrayList<SVGElement>();
            svgu.getElement(uriSymbol).getChildren(sl);
            
            for(int i = 0; i < sl.size(); i++)
            {
                SVGElement se = sl.get(i);
                _SVGE.put(se.getId(), se);
            }
                    
////////////////////////////////////////////////////////////////////////////////////////////////
        /*InputStream xmlStreamDF = Thread.currentThread().getContextClassLoader().getResourceAsStream("ArmyC2/C2SD/Plugin2525D/SVG/2525D.txt");
        BufferedReader in = new BufferedReader(new InputStreamReader(xmlStreamDF));
        String line = null;
        while((line = in.readLine()) != null)
        {
            String id = getStringID(line);
            _SVG.put(id, line);
        }//*/
////////////////////////////////////////////////////////////////////////////////////////////////
            
/*            InputStream xmlStreamDF = Thread.currentThread().getContextClassLoader().getResourceAsStream("ArmyC2/C2SD/Plugin2525D/SVG/2525D.txt");
            
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlStreamDF);
            
            doc.normalizeDocument();
            //doc.normalize();
            //System.out.println(InputStreamToString(xmlStreamD));
            System.out.println(doc.getTextContent());
            System.out.println(doc.getFirstChild().getTextContent());
            
            NodeList nl = doc.getFirstChild().getChildNodes();
            
            for(int i = 0; i < nl.getLength(); i++)
            {
                Node nNode = nl.item(i);
                
                System.out.println("\nCurrent Element :" + nNode.getLocalName());
                
                if(nNode.getNodeType() == Node.ELEMENT_NODE)
                {
                    Element eElement = (Element) nNode;
                    
                    System.out.println(nNode.getTextContent());
                    System.out.println(eElement.getTextContent());
                    System.out.println(getElementText(eElement));
                }
                
            }//*/
        }
        catch(Exception exc)
        {
            System.out.println(exc.getMessage());
            System.out.println(Utilities.getStackTrace(exc));
        }

        
        
    }
    
    private String getStringID(String element)
    {
        int start = element.indexOf("id=");
        start = start + 4;
        int end = element.indexOf("\"", start);
        String id = element.substring(start, end);
        return id;
    }
    
    private String InputStreamToString(InputStream is)
    {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        try
        {
            String read = br.readLine();
            while(read != null)
            {
                sb.append(read);
                read = br.readLine();
            }
        }
        catch(Exception exc)
        {
            System.out.println();
        }
        
        return sb.toString();
        /*String xml = sb.toString();
        xml = xml.replace("(?s)<text[^>]*>.*?</text>","REMOVED");
        return xml;//*/
    }
    
    private String getElementText(Element e)
    {
        StringBuilder buf = new StringBuilder();
        for(Node n = e.getFirstChild(); n != null; n=n.getNextSibling())
        {
            if(n.getNodeType() == Node.ELEMENT_NODE)
                buf.append(n.getNodeValue());
        }
        return buf.toString();
    }
    
    private SVGElement cloneSVGElement(SVGElement original)
    {
        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(original);
            
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            return (SVGElement) ois.readObject();
        }
        catch(Exception exc)
        {
            return null;
        }
    }
    
    private void getSVGImageFromSymbolID(String symbolID, Map<String,String> attributes)
    {
        try
        {
            int size = 35;
            if(attributes.containsKey(MilStdAttributes.PixelSize))
                size = Integer.valueOf(attributes.get(MilStdAttributes.PixelSize));
            String lineColor = null;
            if(attributes.containsKey(MilStdAttributes.LineColor))
                lineColor = attributes.get(MilStdAttributes.LineColor);
            String fillColor = null;
            if(attributes.containsKey(MilStdAttributes.FillColor))
                fillColor = attributes.get(MilStdAttributes.FillColor);
            Boolean keepUnitRatio = true;
            if(attributes.containsKey(MilStdAttributes.KeepUnitRatio))
                keepUnitRatio = Boolean.valueOf(attributes.get(MilStdAttributes.KeepUnitRatio));
            
            
            String strFrame = _SVG.get(SVGLookup.getFrameID(symbolID));
            String strMain = _SVG.get(SVGLookup.getMainIconID(symbolID));
            String strOctagon = _SVG.get("octagon");
            
            InputStream isOctagon = new ByteArrayInputStream(strOctagon.getBytes(StandardCharsets.UTF_8));
            InputStream isFrame = new ByteArrayInputStream(strFrame.getBytes(StandardCharsets.UTF_8));
            InputStream isSymbol = new ByteArrayInputStream(strMain.getBytes(StandardCharsets.UTF_8));
            
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

            //set default transforms and fill
            gTranslate.addAttribute("transform", AnimationElement.AT_XML, "translate(0,0)");
            gScale.addAttribute("transform", AnimationElement.AT_XML, "scale(1)");
            gSymbol.addAttribute("fill", AnimationElement.AT_CSS, "#000000");

            //load SVG data from files
            URI uriFrame = svgu.loadSVG(isFrame, "frame");
            URI uriOctagon = svgu.loadSVG(isOctagon, "octagon");
            URI uriMain = svgu.loadSVG(isSymbol, "main");
            //get diagram & group objects that lets us work with that data
            SVGDiagram svgdo = svgu.getDiagram(uriOctagon);
            SVGDiagram svgdf = svgu.getDiagram(uriFrame);
            SVGDiagram svgdm = svgu.getDiagram(uriMain);
            Group frame = (Group)svgdf.getElement("frame");
            Group octagon = (Group)svgdo.getElement("octagon");
            Group main = (Group)svgdm.getElement("main");

            //bbox of frame
            Rectangle2D bbox = frame.getBoundingBox();
            Rectangle2D bboxO = octagon.getBoundingBox();
            Point2D centerPoint = new Point2D.Double(bboxO.getCenterX(),bboxO.getCenterY());
            System.out.println("Frame bbox: " + bbox.toString());
            System.out.println("Octagon bbox: " + bboxO.toString());
            System.out.println("Center point: " + centerPoint.toString());
            //if(HQ staff, will need to get new center point)

            //determine transformations needed.
            double pixelSize = 50;
            double scale = 1.0;
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
            System.out.println("scaled bbox: " + bbox.toString());
            transX = bbox.getX() * -1;
            transY = bbox.getY() * -1;


            //apply transformations.
            String strScale = "scale(" + String.valueOf(scale) + ")";
            gScale.setAttribute("transform", AnimationElement.AT_XML, strScale);
            String translatef = "translate(" + String.valueOf(transX) + "," + String.valueOf(transY) + ")";
            gTranslate.setAttribute("transform", AnimationElement.AT_XML, translatef);


            //work done, add all groups to SVG.

            sd.getRoot().loaderAddChild(null, gTranslate);
            gTranslate.loaderAddChild(null, gScale);
            gScale.loaderAddChild(null, gSymbol);
            gSymbol.loaderAddChild(null, frame);
            gSymbol.loaderAddChild(null, main);


            //tring to clip
            /*bbox.setRect(bbox.getCenterX(), bbox.getY(), bbox.getWidth()/2, bbox.getHeight());
            Rectangle2D vr = sd.getViewRect();
            sd.setIgnoringClipHeuristic(true);
            sd.setDeviceViewport(bbox.getBounds());//*/

            SVGIcon icon = new SVGIcon();
            icon.setAntiAlias(true);
            icon.setSvgURI(uriSymbol);

            BufferedImage foo = new BufferedImage(400,400,BufferedImage.TYPE_4BYTE_ABGR);


            //form graphics object
            Graphics2D g2d = (Graphics2D)foo.getGraphics();

            //graphics for BufferedImage
            Graphics2D gfoo = (Graphics2D)foo.createGraphics();
            //draw test frame
            gfoo.setColor(Color.red);
            gfoo.drawRect(1, 1, 48, 48);
            //draw SVG to image
            sd.render(gfoo);

            //draw BufferedImage to form.
            g2d.setColor(Color.blue);
            g2d.drawRect(10, 30, 50, 50);


            //icon.setScaleToFit(true);
            icon.paintIcon(null, gfoo, 0, 0);

            g2d.drawImage(foo, 10, 30, null);



            double width = bbox.getWidth();
            double height = bbox.getHeight();
        }
        catch(Exception exc)
        {
            System.out.println(exc.getMessage());
            System.out.println(Utilities.getStackTrace(exc));
        }
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
    
    public String getSVG(String id)
    {
        if(_SVG.containsKey(id))
            return "<svg>" + _SVG.get(id) + "</svg>";
        else
            return null;
    }
    
    public SVGElement getSVGElement(String id)
    {
        if(_SVGE.containsKey(id))
            return _SVGE.get(id);
        else
            return null;
    }
    
    public static String getFillID(String symbolID)
    {
        return "";
    }
    
    public static String getFrameID(String symbolID)
    {
        //SIDC positions 3_456_7
        String frameID = symbolID.charAt(2) + "_" + symbolID.substring(3, 6) + "_" + symbolID.charAt(6);
        return frameID;
    }
    
    public static String getMainIconID(String symbolID)
    {
        //SIDC positions 5-6 + 11-16
        String mainIconID = symbolID.substring(4, 6) + symbolID.substring(10, 16);
        return mainIconID;
    }
    
    public static String getMod1ID(String symbolID)
    {
        //SIDC positions 5-6 + 17-18 + "1"
        String mod1ID = symbolID.substring(4, 6) + symbolID.substring(16, 18) + "1";
        return mod1ID;
    }
    
    public static String getMod2ID(String symbolID)
    {
        //SIDC positions 5-6 + 19-20 + "2"
        String mod2ID = symbolID.substring(4, 6) + symbolID.substring(18, 20) + "2";
        return mod2ID;
    }
}
