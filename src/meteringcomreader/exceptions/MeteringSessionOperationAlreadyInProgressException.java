
package meteringcomreader.exceptions;

/**
 * Polecenie już jest wykonane, np. gdy rozpoczynamy sesje która już trwa,
 * kod 1
 * @author Juliusz Jezierski
 */
public class MeteringSessionOperationAlreadyInProgressException extends MeteringSessionException{
   public MeteringSessionOperationAlreadyInProgressException(){
            this (I18nHelper.getI18nMessage("OperationAlreadyInProgress"));        
        }
   public MeteringSessionOperationAlreadyInProgressException(Throwable ex) {
        super(ex);
    }

    public MeteringSessionOperationAlreadyInProgressException (String msg) {
        super(msg);
    }
}
