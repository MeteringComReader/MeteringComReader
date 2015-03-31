/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
 
public class ServiceDiscoveryProvider implements Runnable{
        protected boolean stop=false;
        public final static int BROADCAST_PORT = 13131;
        public final static String BROADCAST_GROUP = "230.12.12.12";
 
    public static void main(String[] args) throws IOException, InterruptedException {
 
        ServiceDiscoveryProvider nsp = new ServiceDiscoveryProvider();
        nsp.start();
        
//        Thread.sleep(1000*60);
    }
    private Thread thread;

    public void start(){
        thread = new Thread(this);
        stop=false;
        thread.start();       
    }
    
    public void stop(){
        stop=true;
        thread.interrupt();       
    }
    
    @Override
    public void run() {
        MulticastSocket socket=null;
        InetAddress address = null;
        byte[] buf = new byte[256];
        DatagramPacket resPacket;
        try {
            socket = new MulticastSocket(BROADCAST_PORT);
            address = InetAddress.getByName(BROADCAST_GROUP);
            socket.joinGroup(address);
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            
            while (!stop || Thread.interrupted()){
                socket.receive(packet);
                InetAddress clientAddr = packet.getAddress();
                int clientPort = packet.getPort();
                resPacket = new DatagramPacket(buf, buf.length, clientAddr, clientPort);
                socket.send(packet);
                
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        finally{
            try {
                socket.leaveGroup(address);            
                socket.close();
            } catch (IOException ex) {
                
            }
        }
    }
 
}