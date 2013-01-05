/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package meteringcomreader;

/**
 *
 * @author Juliusz
 */
public class CRC16DN502 {

    protected int crcReg = 0xFFFF;
    protected static int CRC16_POLY = 0x8005;

    public CRC16DN502() {
        super();
    }
    
    public void init(){
        crcReg= 0xFFFF;
    }
    
    public int getChecksum(){
        return crcReg;
    }
    
    public void update(byte[] aBytes){
        for (int i=0; i< aBytes.length; i++){
            update(aBytes[i]);            
        }
    }

    public void update(byte aByte) {
        int crcData=((int)aByte)&0xFF;
        int i;
        for (i = 0; i < 8; i++) {
            if ((((crcReg & 0x8000) >>> 8) 
                    ^ (crcData & 0x80))!=0) {
                crcReg = (crcReg << 1) ^ CRC16_POLY;
//System.out.println("First "+( Integer.toHexString(crcReg&0XFFFF)));                
            }
            else {
                crcReg = (crcReg << 1);
//System.out.println("Second "+( Integer.toHexString(crcReg&0XFFFF)));                
            }
            crcReg&=0XFFFF;
//   System.out.println(( Integer.toHexString(crcReg)));
            crcData <<= 1;
        }
//        return crcReg;
    }

    public static void main(String[] args) {
        byte[] txBuffer = {(byte)0X5C,(byte)0X93,(byte)0XC4,(byte)0X50,(byte)0X0C,(byte)0XCE,(byte)0XE0,
            (byte)0XC,(byte)0XCE,(byte)0XE0,(byte)0X0C,(byte)0XCE,(byte)0XE0,0X16};
        CRC16DN502 checksum = new CRC16DN502();
        for (int i=0; i< txBuffer.length; i++){
            checksum.update(txBuffer[i]);            
        }
            System.out.println(( Integer.toHexString(checksum.getChecksum())));
         byte[] rCRC={0x56,0x7E};
         System.out.println(( Long.toHexString(Utils.bytes2long(rCRC, 2))));
    }
 
}
