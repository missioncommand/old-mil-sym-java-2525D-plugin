/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ArmyC2.C2SD.Plugin2525D;

/**
 *
 * @author User
 */
public class Utilities {
    
        /**
     * Takes a throwable and puts it's stacktrace into a string.
     * @param thrown
     * @return
     */
    public static String getStackTrace(Throwable thrown)
    {
        try
        {
            /*
            Writer writer = new StringWriter();
            PrintWriter printWriter = new PrintWriter(writer);
            thrown.printStackTrace(printWriter);
            return writer.toString();*/
            String eol = System.getProperty("line.separator");
            StringBuilder sb = new StringBuilder();
            sb.append(thrown.toString());
            sb.append(eol);
            for(StackTraceElement element : thrown.getStackTrace())
            {
                sb.append("        at ");
                sb.append(element);
                sb.append(eol);
            }
            return sb.toString();
        }
        catch(Exception exc)
        {
            System.out.println(exc.getMessage());
            return "Failed to get Stack Trace.";
        }
        
    }
    
}
