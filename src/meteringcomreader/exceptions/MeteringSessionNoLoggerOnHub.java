/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package meteringcomreader.exceptions;

/**
 *
 * @author Juliusz
 */
public class MeteringSessionNoLoggerOnHub extends MeteringSessionException{
      public MeteringSessionNoLoggerOnHub(){
        this (I18nHelper.getI18nMessage("NoLoggerOnHub"));        
    }
        public MeteringSessionNoLoggerOnHub(String msg){
        super(msg);
    }
        public MeteringSessionNoLoggerOnHub(Throwable ex){
        super(ex);
    }    
}
