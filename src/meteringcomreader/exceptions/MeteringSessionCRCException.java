/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package meteringcomreader.exceptions;

/**
 *
 * @author Juliusz
 */
public class MeteringSessionCRCException extends MeteringSessionException{
      public MeteringSessionCRCException(){
        this (I18nHelper.getI18nMessage("CRCError"));        
    }
        public MeteringSessionCRCException(String msg){
        super(msg);
    }
        public MeteringSessionCRCException(Throwable ex){
        super(ex);
    }
}
