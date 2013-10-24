/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package meteringcomreader.exceptions;

import java.util.ResourceBundle;

/**
 *
 * @author Juliusz Jezierski
 */
public class I18nHelper {
    private static ResourceBundle rb;
    public static String getI18nMessage(String message){
        return rb.getString(message);
    }
static{
    rb = ResourceBundle.getBundle("meteringcomreader.exceptions.messages");
}    
    
}
