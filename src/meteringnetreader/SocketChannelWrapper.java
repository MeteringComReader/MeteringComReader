package meteringnetreader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Juliusz
 */
 public class SocketChannelWrapper implements Runnable{
    
    protected SocketChannel wrappedSC;
    protected Selector selector = Selector.open();
    protected NetEventListener netEventListener;
    private boolean opened=true;
    protected InputStream is = new NetInputStream(this);
    protected OutputStream os = new NetOutputStream(this);
    
    public SocketChannelWrapper(SocketChannel wrappedSC) throws IOException{
        this.wrappedSC=wrappedSC;
        wrappedSC.configureBlocking(false);
        wrappedSC.register(selector, SelectionKey.OP_READ);
        (new Thread(this)).start();
    }

    public void registerNetEventListner(NetEventListener netEventListener){
        this.netEventListener=netEventListener;
    }

    public SocketChannel getWrappedSC() {
        return wrappedSC;
    }

    public InputStream getIs() {
        return is;
    }

    public OutputStream getOs() {
        return os;
    }
    
    public void close()  {
        try {
            is.close();
        } catch (IOException ex) {
            Logger.getLogger(SocketChannelWrapper.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            os.close();
        } catch (IOException ex) {
            Logger.getLogger(SocketChannelWrapper.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            wrappedSC.close();
        } catch (IOException ex) {
            Logger.getLogger(SocketChannelWrapper.class.getName()).log(Level.SEVERE, null, ex);
        }
        opened=false;
        selector.wakeup();
    }
    
    @Override
    public void run() {
        while(opened){
            try {
                int readyChannels = selector.select();                                
                if(readyChannels == 0) 
                    continue;
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
                while(keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();                
                    if (key.isReadable()){
                        if (netEventListener!=null)
                            netEventListener.netEvent();
                    }                    
                    keyIterator.remove();
                }
                
            } catch (IOException ex) {
                Logger.getLogger(SocketChannelWrapper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    
}
