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
public class Writer {
    static public void main(String[] args) throws MeteringSessionException, IOException, InterruptedException{
        byte [] flashHubSession ={0x6a, 0x55, 0x30, 0x32, 0x30, 0x32, 0x46, 0x46, 0x46, 0x46, 0x46, 0x46, 0x46, 0x46};
        SerialPort port = initComPort("COM22");
        OutputStream outputstream=port.getOutputStream();
        
        long timer=System.nanoTime();                
        outputstream.write(flashHubSession);
        timer=printTime("Write:\t", timer);

        outputstream.flush();
        timer=printTime("Flush:\t", timer);
        outputstream.close();
        port.close();
                
                
     }
}
