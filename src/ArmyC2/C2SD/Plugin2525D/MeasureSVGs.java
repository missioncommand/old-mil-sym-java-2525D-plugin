/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ArmyC2.C2SD.Plugin2525D;

import ArmyC2.C2SD.Utilities.ErrorLogger;
import com.kitfox.svg.Group;
import com.kitfox.svg.SVGElement;
import com.kitfox.svg.SVGException;
import java.awt.geom.Rectangle2D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Michael.Spinelli
 */
public class MeasureSVGs {
    
    public static void measureFiles()
    {
        try
        {
            List<String> measurements = new ArrayList<String>();
            
           
            List<String> keyList = SVGLookup.getAllKeys();
            SVGElement temp = null;
            Group tg = null;
            Rectangle2D bbox = null;
            String mstring = null;
            //measure all SVGs in lookup
            for(String key: keyList)
            {
                temp = SVGLookup.getInstance().getSVGElement(key);
                if(temp instanceof Group)
                {
                    tg = (Group)temp;
                    tg.toString();
                    bbox = tg.getBoundingBox();
                    mstring = key + "," + String.valueOf(bbox.getX()) + "," + String.valueOf(bbox.getY()) + "," + String.valueOf(bbox.getWidth()) + "," + String.valueOf(bbox.getHeight());
                    measurements.add(mstring);    
                    System.out.println(mstring);
                    System.out.println(tg.toString());
                }
                
            }
            
            //write to file
            writeToFile(measurements);
            
            
        }
        catch(Exception exc)
        {
            System.out.println(exc.getMessage());
        }
    }
    
    public static void measureFiles2()
    {
        String xmlPathD = "ArmyC2/C2SD/Plugin2525D/SVG/2525D.svg";
        
        
        //InputStream xmlStreamD = this.getClass().getClassLoader().getResourceAsStream(xmlPathD);
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(xmlPathD);
        
        Map<String,String> SVG = new HashMap<String,String>();
        
        try
        {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(is);

                //NodeList nl = doc.getElementsByTagName("g");
                NodeList nl = doc.getElementsByTagName("SVG");
                nl = nl.item(0).getChildNodes();
                //NodeList nl = doc.getChildNodes();
                int nodeCount = nl.getLength();
                for(int i = 0; i < nodeCount; i++)
                {
                    if(nl.item(i) instanceof  Element){
                        Element e = (Element) nl.item(i);
                        String id = e.getAttribute("id");
                        //String svg = e.getNodeValue();
                        String svg = nodeToString(e);

                        System.out.println("id : " + id);
                        System.out.println("SVG : " + svg);

                        if(id != null && svg != null)
                            SVG.put(id, svg);
                    }
                }
        }
        catch(Exception foo)
        {
            
        }
                       

        
        try
        {
            List<String[]> measurements = new ArrayList<String[]>();
            
            String delimiter = "~";
            List<String> keyList = SVGLookup.getAllKeys();
            SVGElement temp = null;
            Group tg = null;
            String strSVG = null;
            Rectangle2D bbox = null;
            String mstring = null;
            //measure all SVGs in lookup
            for(String key: keyList)
            {
                try
                {
                    temp = SVGLookup.getInstance().getSVGElement(key);
                    if(temp != null && temp instanceof Group)
                    {
                        tg = (Group)temp;
                        try
                        {
                            bbox = tg.getBoundingBox();
                        }
                        catch(SVGException svge)
                        {
                            System.err.println("KitFox ERROR");
                            System.out.println(key);
                            System.out.println(svge.getMessage());
                            ErrorLogger.LogException("MeasureSVGs", "MeasureFiles2", svge);
                        }
                        
                        if(bbox != null)
                        {
                            strSVG = SVG.get(key);
                            strSVG = strSVG.replace("\n", "");
                            strSVG = strSVG.replace("\r", "");
                            strSVG = strSVG.replace("\t", "");
                            mstring = key + delimiter + String.valueOf(bbox.getX()) + delimiter + String.valueOf(bbox.getY()) + delimiter + String.valueOf(bbox.getWidth()) + delimiter + String.valueOf(bbox.getHeight());// + delimiter + strSVG;
                            String[] array = {mstring,strSVG};
                            measurements.add(array);    
                            //System.out.println(mstring);
                            //System.out.println(tg.toString());
                        }
                    }
                    else if(temp == null)
                    {
                        System.out.println("Missing Element: " + key);
                    }    
                    temp = null;
                    bbox = null;
                }
                catch(Exception error)
                {
                    System.err.println("ERROR");
                    System.out.println(key);
                    System.out.println(error.getMessage());
                    ErrorLogger.LogException("MeasureSVGs", "MeasureFiles2", error);
                }
                
            }
            
            //write to file
            writeToFile2(measurements);
        }
        catch(Exception exc)
        {
            System.out.println(exc.getMessage());
        }
    }
    
        /**
     *
     * @param node
     * https://stackoverflow.com/questions/4412848/xml-node-to-string-in-java
     */
    private static String nodeToString(Node node)
    {
        StringWriter sw = new StringWriter();
        try
        {
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            t.transform(new DOMSource(node), new StreamResult(sw));
        }
        catch(TransformerException te)
        {
            System.out.println(te.getMessage());
        }
        return sw.toString();
    }
    
    private static void writeToFile(List<String> measurements)
    {
        try
        {
            File fout = new File("C:\\temp\\measurements.txt");
            FileOutputStream fos = new FileOutputStream(fout);
            
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
            
            for(String measure: measurements)
            {
                bw.write(measure);
                bw.newLine();
            }
            bw.close();
            
        }
        catch(IOException ioexc)
        {
            System.out.println(ioexc.getMessage());
        }
    }
    
        private static void writeToFile2(List<String[]> measurements)
    {
        try
        {
            File fout = new File("C:\\temp\\measurements.txt");
            FileOutputStream fos = new FileOutputStream(fout);
            
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
            
            for(String[] measure: measurements)
            {
                bw.write(measure[0]);
                bw.newLine();
                bw.write(measure[1]);
                bw.newLine();
            }
            bw.close();
            
        }
        catch(IOException ioexc)
        {
            System.out.println(ioexc.getMessage());
        }
    }
}
