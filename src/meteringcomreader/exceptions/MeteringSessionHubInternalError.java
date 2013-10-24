
package meteringcomreader.exceptions;

/**
    * Bład wewnętrzny koncentratora, 
 * bliżej nie zidentyfikowana przyczyna, uzyty zawsze w przypadku nie wybrania 
	odpowiedniej opcji switch->case, kod 5
 * @author Juliusz Jezierski
 */
public class MeteringSessionHubInternalError extends MeteringSessionException{
    public MeteringSessionHubInternalError(){
        this (I18nHelper.getI18nMessage("HubInternalError"));                
    }
    public MeteringSessionHubInternalError(String msg){
        super(msg);
    }
        public MeteringSessionHubInternalError(Throwable ex){
        super(ex);
    }
}
