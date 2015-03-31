/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package meteringcomreader.exceptions;

/**
 *
 * @author Juliusz
 */
public class MeteringSessionFlashLoggerTransException extends MeteringSessionException{
    public MeteringSessionFlashLoggerTransException(){
        this (I18nHelper.getI18nMessage("FlashLoggerTrans"));                
    }
    public MeteringSessionFlashLoggerTransException(String msg){
        super(msg);
    }
        public MeteringSessionFlashLoggerTransException(Throwable ex){
        super(ex);
    }    
}
