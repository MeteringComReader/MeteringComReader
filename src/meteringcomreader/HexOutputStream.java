/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package meteringcomreader;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import static meteringcomreader.HexBufOutputStream.asciiBufSize;

/**
 *
 * @author Juliusz
 */
public class HexOutputStream extends FilterOutputStream{
    
    protected boolean messageStarted=false;
    protected int bytesCount=0;
    static final int asciiBufSize=62;


    
    public HexOutputStream(OutputStream out) {
        super(out);
    }

    static byte[] byte2chars(int inByte) {
        byte[] ret=new byte[2];
        byte bits=(byte) (inByte&0x0F);
        
        if (bits<10)
           bits+=HexInputStream.char0;
        else
           bits+=HexInputStream.charA-10;        
        ret[1]=bits;
        
        bits=(byte) ((inByte&0xF0)>>4);
        
        if (bits<10)
           bits+=HexInputStream.char0;
        else
           bits+=HexInputStream.charA-10;        
        ret[0]=bits;
        
        return ret;        
    }
    
    @Override
    public void write(int byteIn) throws IOException {
        if (!messageStarted){
            super.write(HexInputStream.HEADER1);
            super.write(HexInputStream.HEADER2);
            messageStarted=true;
        }            
        byte[] chars=byte2chars(byteIn);
        for (int i=0; i<chars.length; i++)
            super.write(chars[i]); 
        bytesCount+=2;
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        write(bytes, 0, bytes.length); 
    }

    @Override
    public void write(byte[] bytes, int offset, int len) throws IOException {
        for (int i=offset; i<offset+len; i++){
            this.write(bytes[i]);
        }        
    }

    @Override
    public void flush() throws IOException {
         if (messageStarted){
            int spacesCount=(bytesCount+4)%asciiBufSize;
            if (spacesCount>0)
                spacesCount=asciiBufSize-spacesCount;
            for(int i=0; i< spacesCount; i++)
                super.write(HexInputStream.space);
            super.write(HexInputStream.FOOTER1);
            super.write(HexInputStream.FOOTER2);
            bytesCount=0;
            messageStarted=false;
         }
        super.flush(); 
    }
    
    
}
