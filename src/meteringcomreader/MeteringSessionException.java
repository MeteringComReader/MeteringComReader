/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package meteringcomreader;

/**
 *
 * @author Juliusz
 */
public class MeteringSessionException extends Exception {
    
    public MeteringSessionException(String msg){
        super(msg);
    }
        public MeteringSessionException(Throwable ex){
        super(ex);
    }
}
