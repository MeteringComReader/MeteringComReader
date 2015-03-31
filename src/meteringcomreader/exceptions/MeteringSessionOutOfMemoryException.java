/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package meteringcomreader.exceptions;

/**
 * Pamięć pełna, za dużo elementów, kod 9
 * @author Juliusz Jezierski
 */
public class MeteringSessionOutOfMemoryException extends MeteringSessionException{
        public MeteringSessionOutOfMemoryException(){
            this (I18nHelper.getI18nMessage("OutOfMemory"));                    
        }
        public MeteringSessionOutOfMemoryException(String msg){
        super(msg);
    }
        public MeteringSessionOutOfMemoryException(Throwable ex){
        super(ex);
    }
    
}
