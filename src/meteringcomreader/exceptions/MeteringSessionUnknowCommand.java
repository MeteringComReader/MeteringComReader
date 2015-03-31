/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package meteringcomreader.exceptions;

/**
 * Polecenie nieznane, nie ma tym urządzeniu takiego rozkazu, kod 7
 * @author Juliusz Jezierski
 */
public class MeteringSessionUnknowCommand extends MeteringSessionException{
    public MeteringSessionUnknowCommand(){
        this (I18nHelper.getI18nMessage("UnknowCommand"));                    
    }
        public MeteringSessionUnknowCommand(String msg){
        super(msg);
    }
        public MeteringSessionUnknowCommand(Throwable ex){
        super(ex);
    }
    
}
