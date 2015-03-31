/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package meteringcomreader;

import meteringcomreader.exceptions.MeteringSessionException;

/**
 *
 * @author Juliusz
 */
public interface SessionInserter {
        public void close() throws MeteringSessionException;
        public int  mainThread() throws MeteringSessionException;
}
