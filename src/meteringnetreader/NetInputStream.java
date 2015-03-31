
package meteringnetreader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

/**
 *
 * @author Juliusz
 */
public class NetInputStream extends InputStream{

    protected SocketChannelWrapper scw;
    protected ByteBuffer buf = ByteBuffer.allocate(1024);
    int bytesRead=0;
    int counter=0;

    public NetInputStream(SocketChannelWrapper scw) {
        this.scw=scw;
//        buf.clear();
    }
        
    @Override
    public int read() throws IOException {
        if ( counter < bytesRead ){
            counter++;
            byte b =buf.get();
            return ((int)b) & 0xFF;
        }
        counter=0;
        buf.clear();
        bytesRead = scw.getWrappedSC().read(buf);
        buf.rewind();
        if (bytesRead==0){ 
            return -1;
        }
        return ((int)buf.get()) & 0xFF;        
    }
    
}
