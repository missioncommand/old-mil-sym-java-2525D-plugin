/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ArmyC2.C2SD.Plugin2525D;

import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 *
 * @author michael.spinelli
 */
public class ShapeUtilities {
    
    public static void grow(Rectangle2D rect, int size)
    {
            rect.setFrame(rect.getX() - size, rect.getY() - size, rect.getWidth() + (size*2), rect.getHeight() + (size*2));
    }
    
    public static void offset(Rectangle2D rect, int offsetX, int offsetY)
    {
        rect.setRect(rect.getX() + offsetX, rect.getY() + offsetY, rect.getWidth(), rect.getHeight());
    }
    
    public static void offset(Point2D point, int offsetX, int offsetY)
    {
        point.setLocation(point.getX() + offsetX, point.getY() + offsetY);
    }
    
    public static void offset(Path2D path, int offsetX, int offsetY)
    {
        path.transform(AffineTransform.getTranslateInstance(offsetX, offsetY));
    }
    
}
