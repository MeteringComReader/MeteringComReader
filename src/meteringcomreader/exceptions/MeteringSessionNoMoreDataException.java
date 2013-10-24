/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package meteringcomreader.exceptions;

/**
 * Nie ma nowych danych do przesłania, nie ma więcej danych, kod F
 * @author Juliusz Jezierski
 */
public class MeteringSessionNoMoreDataException extends MeteringSessionException { 
    public MeteringSessionNoMoreDataException(){
        this (I18nHelper.getI18nMessage("NoMoreData"));                    
    }
    public MeteringSessionNoMoreDataException(String msg){
        super(msg);
    }
        public MeteringSessionNoMoreDataException(Throwable ex){
        super(ex);
        }
    
}
