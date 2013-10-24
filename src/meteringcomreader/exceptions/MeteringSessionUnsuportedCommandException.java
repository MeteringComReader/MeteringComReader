/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package meteringcomreader.exceptions;

/**
 * Polecenie nieobsługiwane, funkcjonalnie w tym urządzeniu może być zablokowane 
 * ale jest rozpoznane kod 6
 * @author Juliusz Jezierski
 */
public class MeteringSessionUnsuportedCommandException extends MeteringSessionException{
    public MeteringSessionUnsuportedCommandException(){
        this (I18nHelper.getI18nMessage("UnsupportedCommand"));                
    }
    public MeteringSessionUnsuportedCommandException(String msg){
        super(msg);
    }
    public MeteringSessionUnsuportedCommandException(Throwable ex){
        super(ex);
    }
    
}
