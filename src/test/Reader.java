/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import gnu.io.SerialPort;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import meteringcomreader.exceptions.MeteringSessionException;
import static test.TestPerf.initComPort;
import static test.TestPerf.printTime;

/**
 *
 * @author Juliusz
 */
public class Reader {
    static public void main(String[] args) throws MeteringSessionException, IOException, InterruptedException{
        byte [] flashHubSession ={0x6a, 0x55, 0x30, 0x32, 0x30, 0x32, 0x46, 0x46, 0x46, 0x46, 0x46, 0x46, 0x46, 0x46};
        SerialPort port = initComPort("COM23");
        InputStream inputStream = port.getInputStream();
        
        long timer=System.nanoTime();          
        inputStream.read(flashHubSession);
        timer=printTime("Read:\t", timer);
        
        inputStream.close();
        port.close();
        
     }
}
