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

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import meteringcomreader.HubSessionNetManager;
import meteringcomreader.SessionStreamInserter;
import meteringcomreader.exceptions.MeteringSessionException;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.LoggerFactory;
/**
 *
 * @author Juliusz
 */
public class NetServer extends Thread{

    private Socket socket;
    private HubSessionNetManager hbs;
    private static final org.slf4j.Logger lgr = LoggerFactory.getLogger(NetServer.class);
    
    public static void main(String[] args) throws IOException {
        PropertyConfigurator.configure(HubSessionNetManager.class.getResource("log4j.properties"));
        ServerSocket serverSocket = null;
        boolean listening = true;
        HubSessionNetManager hbs = null;
 
        try {
            hbs = HubSessionNetManager.getHubSessionManager();
            hbs.startHubSessionManager();
            hbs.addShutdownHook();
            
            try {
                Thread.sleep(1000*20);
            } catch (InterruptedException ex) {; }
            lgr.info("net server started");
            serverSocket = new ServerSocket(12124);
        } catch (MeteringSessionException ex) {
            lgr.error("err"+ex);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 12124.");
            System.exit(-1);
        }
 
        while (listening)
            new NetServer(serverSocket.accept(), hbs).start();
        serverSocket.close();
    }



    private NetServer(Socket accept, HubSessionNetManager hbs) {
        this.socket=accept;
        this.hbs=hbs;
    }

    @Override
    public void run() {
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            hbs.addNetInserterForAllHubs(out);            
        } catch (MeteringSessionException ex) {
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

 
}

