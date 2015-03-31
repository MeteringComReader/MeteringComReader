/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package meteringnetreader;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 *
 * @author Juliusz
 */
public class NetOutputStream extends OutputStream{

    protected SocketChannelWrapper scw;
    protected ByteBuffer buf = ByteBuffer.allocate(1024);

    public NetOutputStream(SocketChannelWrapper scw){
        this.scw=scw;
        buf.clear();
    }
    
    @Override
    public void write(int i) throws IOException {
        buf.put( (byte)(i & 0xFF) );
        if (!buf.hasRemaining())
            flush();
    }
    
    @Override
    public void flush() throws IOException{
        buf.flip();
        while(buf.hasRemaining()) {
            scw.getWrappedSC().write(buf);
        } 
        buf.clear();
    }
}
