/*
 * Copyright (C) 2015 Juliusz Jezierski
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
