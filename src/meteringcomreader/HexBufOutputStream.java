/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package meteringcomreader;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import static meteringcomreader.HexOutputStream.byte2chars;

/**
 *
 * @author Juliusz
 */
public class HexBufOutputStream extends BufferedOutputStream{

    static final int asciiBufSize=62;
    private boolean messageStarted=false;
            
    public HexBufOutputStream(OutputStream out) {
        super(out, 2048);
    }

    @Override
    public synchronized void write(int byteIn) throws IOException {
        if (!messageStarted){
            messageStarted=true;
        }                    
        byte[] chars=byte2chars(byteIn);        
        for (int i=0; i<chars.length; i++){
            super.buf[super.count]=chars[i]; 
            super.count++;
        }
    }

    @Override
    public void write(byte[] bytes, int offset, int len) throws IOException {
        for (int i=offset; i<offset+len; i++){
            this.write(bytes[i]);
        }        
    }

    @Override
    public synchronized void flush() throws IOException {
        if (messageStarted){
            int spacesCount=(super.count+4)%asciiBufSize;
            if (spacesCount>0)
                spacesCount=asciiBufSize-spacesCount;
                                    
            byte[] newBuf=new byte[super.count+4+spacesCount];
            newBuf[0]=HexInputStream.HEADER1;
            newBuf[1]=HexInputStream.HEADER2;            
            newBuf[newBuf.length-2]=HexInputStream.FOOTER1;
            newBuf[newBuf.length-1]=HexInputStream.FOOTER2;
            for (int i=0; i<spacesCount; i++)
                newBuf[i+2]=HexInputStream.space;
            for (int i=0; i<super.count; i++)
                newBuf[i+2+spacesCount]=super.buf[i];
            super.count+=4+spacesCount;
            super.buf=newBuf;
            messageStarted=false;
        }
        super.flush(); 
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        write(bytes, 0, bytes.length);
    }
    
    
}
