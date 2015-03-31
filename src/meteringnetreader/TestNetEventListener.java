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
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Juliusz
 */
public class TestNetEventListener implements NetEventListener{
    InputStream is;
    OutputStream os;

    public TestNetEventListener(InputStream is, OutputStream os) {
        this.is = is;
        this.os = os;
    }

    
    
    @Override
    public void netEvent() {
        int i;
        try {
            while( ( i = is.read())!=-1){
                System.out.print((char)i);
                os.write(i);
            }
        os.flush();
        } catch (IOException ex) {
            Logger.getLogger(TestNetEventListener.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    static public void main (String[] args) throws IOException{
         ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
         serverSocketChannel.socket().bind(new InetSocketAddress(10007));

        SocketChannel socketChannel =
            serverSocketChannel.accept();
        
        SocketChannelWrapper scw = new SocketChannelWrapper(socketChannel);
        NetEventListener nel = new TestNetEventListener(scw.getIs(), scw.getOs());
        scw.registerNetEventListner(nel);
        
        
        
    }
}
