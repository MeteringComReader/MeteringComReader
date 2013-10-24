/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package meteringcomreader.gui;

/**
 *
 * @author Juliusz Jezierski
 */

import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.*;
import meteringcomreader.HubSessionManager;
import meteringcomreader.exceptions.MeteringSessionException;
import meteringcomreader.StdOutErrLog;
import org.apache.log4j.PropertyConfigurator;
 
public class LaunchGui {
    /**
     * Utworzenie loggera systemowego
     */
    private static final Logger lgr = LoggerFactory.getLogger(LaunchGui.class);
    
    static private JTextArea textComponent;
    static private Image icon;
    
    public static void main(String[] args) {
        /* Use an appropriate Look and Feel */
        PropertyConfigurator.configure(LaunchGui.class.getResource("/meteringcomreader/log4j.properties"));
        StdOutErrLog.tieSystemOutAndErrToLog();
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            //UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        } catch (UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        } catch (InstantiationException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        /* Turn off metal's use of bold fonts */
        UIManager.put("swing.boldMetal", Boolean.FALSE);
        //Schedule a job for the event-dispatching thread:
        //adding TrayIcon.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
     
    private static void createAndShowGUI() {
        icon=createImage("bulb.gif", "tray icon");
        EmptyFrame frame=new EmptyFrame();
        frame.setIconImage(icon);
        frame.setVisible(true);
        DbConnectDialog dialog = new DbConnectDialog(frame, true, icon);
        
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                    //    System.exit(0);
                    }
                });
        dialog.setVisible(true);
        frame.setVisible(false);
        if (!frame.connected)
            System.exit(0);
        //Check the SystemTray support
        if (!SystemTray.isSupported()) {
            lgr.debug("SystemTray is not supported");
            return;
        }
        final PopupMenu popup = new PopupMenu();
        final TrayIcon trayIcon =
                new TrayIcon(icon);
        final SystemTray tray = SystemTray.getSystemTray();
         
        // Create a popup menu components

        MenuItem showConsole = new MenuItem("Show console");
        MenuItem exitItem = new MenuItem("Exit");
         
        //Add components to popup menu

//        popup.add(showConsole);
        popup.add(exitItem);
         
        trayIcon.setPopupMenu(popup);
         
        initMesgConsole();
                
        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            lgr.warn("TrayIcon could not be added.");
            return;
        }      
         
       trayIcon.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createAndShowConsole();            }
        });
                
        showConsole.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
//                JOptionPane.showMessageDialog(null,"consola");
                createAndShowConsole();
            }
        });
         
         
        exitItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
//                SwingAppenderUI.getInstance().getJframe().setVisible(false);
                tray.remove(trayIcon);
                try {
                    HubSessionManager.stopHubSessionManager();
                } catch (MeteringSessionException ex) {
                    //lgr.warn(null, ex);
                }
                System.exit(0);
            }
        });

        
      
//        JOptionPane.showMessageDialog(frame, "Eggs are not supposed to be green.");
        try {
            HubSessionManager.startHubSessionManager();
        } catch (MeteringSessionException ex) {
            lgr.warn(null, ex);
        }
    }
     
    //Obtain the image URL
    protected static Image createImage(String path, String description) {
        URL imageURL = LaunchGui.class.getResource(path);
         
        if (imageURL == null) {
            System.err.println("Resource not found: " + path);
            return null;
        } else {
            return (new ImageIcon(imageURL, description)).getImage();
        }
    }
        
    private static void createAndShowConsole() {
//        SwingAppenderUI.getInstance().getJframe().setVisible(true);
    }

    
    protected static void initMesgConsole(){    
        /*        
       Frame frame = SwingAppenderUI.getInstance().getJframe();
       frame.setIconImage(icon);
       frame.setVisible(false);
       */

    }
    
    static public class EmptyFrame extends JFrame{
        public  boolean connected=false;    
}
    
}

