/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package meteringnetreader;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import meteringcomreader.DataPacketDTO;

/**
 *
 * @author Juliusz
 */
public class NetClient {
    public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException{
        Socket socket = new Socket("localhost", 12124);
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        while(true){
            DataPacketDTO packet= (DataPacketDTO) in.readObject();
            System.out.println(packet);
        }
    }
    
}
