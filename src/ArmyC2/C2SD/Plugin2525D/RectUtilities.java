/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ArmyC2.C2SD.Plugin2525D;

import java.awt.geom.Rectangle2D;
import java.awt.Rectangle;

/**
 *
 * @author Michael.Spinelli
 */
public class RectUtilities {
    
    public static Rectangle2D makeRect(int x, int y, int w, int h)
    {
            return new Rectangle2D.Double(x, y, x + w, y + h);
    }

    public static Rectangle2D makeRect(float x, float y, float w, float h)
    {
            return new Rectangle2D.Double((int)x, (int)y, (int)(w+0.5f), (int)(h+0.5f));
    }

    public static Rectangle2D makeRectF(float x, float y, float w, float h)
    {
            return new Rectangle2D.Float(x, y, x + w, y + h);
    }

    public static Rectangle2D makeRectF(Rectangle2D rect)
    {
            return new Rectangle2D.Double((int)rect.getX(), (int)rect.getY(), (int)(rect.getWidth()+0.5), (int)(rect.getHeight()+0.5));
    }

    public static void grow(Rectangle2D rect, int size)
    {
            rect.setRect(rect.getX() - size, rect.getY() - size, rect.getWidth() + size, rect.getHeight() + size);
            //return new Rectangle2D.Double(rect.left - size, rect.top - size, rect.right + size, rect.bottom + size);
    }


    public static void shift(Rectangle2D rect, int x, int y)
    {
        rect.setRect(rect.getX() + x, rect.getY() + y, rect.getWidth(), rect.getHeight());
    }

    public static int getCenterX(Rectangle2D rect)
    {
        return (int)Math.round(rect.getCenterX());
    }

    public static int getCenterY(Rectangle2D rect)
    {
        return (int)Math.round(rect.getCenterY());
    }

    public static void shiftBR(Rectangle2D rect, int x, int y)
    {
        rect.setRect(rect.getX(), rect.getY(), rect.getWidth() + x, rect.getHeight() + y);
    }

    public static Rectangle toRectangle(Rectangle2D b) 
    {
        if (b == null) {
            return null;
        }/*from w ww . j a  va 2s . c o  m*/
        if (b instanceof Rectangle) {
            return (Rectangle) b;
        } else {
            return new Rectangle((int) b.getX(), (int) b.getY(),
                    (int) b.getWidth(), (int) b.getHeight());
        }
    }
}
