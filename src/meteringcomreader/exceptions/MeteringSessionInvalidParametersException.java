/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package meteringcomreader.exceptions;

/**
 * Niewłaściwe parametry, zła ilość lub zakres wartości, kod 8
 * @author Juliusz
 */
public class MeteringSessionInvalidParametersException extends MeteringSessionException{
    public MeteringSessionInvalidParametersException(){
                this (I18nHelper.getI18nMessage("InvalidParameters"));        
    }
     public MeteringSessionInvalidParametersException(String msg){
        super(msg);
    }
        public MeteringSessionInvalidParametersException(Throwable ex){
        super(ex);
    }
    
}
