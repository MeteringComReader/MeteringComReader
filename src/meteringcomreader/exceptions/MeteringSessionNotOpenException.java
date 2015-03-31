/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package meteringcomreader.exceptions;

/**
 * Nie ma otwartej sesji, kod 3
 * @author Juliusz
 */
public class MeteringSessionNotOpenException extends MeteringSessionException{
    public MeteringSessionNotOpenException(){
        this (I18nHelper.getI18nMessage("SessionNotOpen"));                
    }
    public MeteringSessionNotOpenException(String msg){
        super(msg);
    }
        public MeteringSessionNotOpenException(Throwable ex){
        super(ex);
    }
    
}
