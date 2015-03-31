
package meteringcomreader.exceptions;

/**
 * Pamięć pusta, nie ma elementów kod A
 * @author Juliusz Jezierski
 */
public class MeteringSessionEmptyMemoryException extends MeteringSessionException{
    public MeteringSessionEmptyMemoryException(){
        this (I18nHelper.getI18nMessage("EmptyMemory"));        
    }
    public MeteringSessionEmptyMemoryException(String msg){
        super(msg);
    }
        public MeteringSessionEmptyMemoryException(Throwable ex){
        super(ex);
    }
    
}
