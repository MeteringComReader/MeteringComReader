/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package meteringcomreader;

import java.io.IOException;

/**
 *
 * @author Juliusz
 */
class MeteringSessionSPException extends MeteringSessionException{

    public MeteringSessionSPException(Throwable ex) {
        super(ex);
    }

    public MeteringSessionSPException(String msg) {
        super(msg);
    }


    
}
