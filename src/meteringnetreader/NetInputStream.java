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
