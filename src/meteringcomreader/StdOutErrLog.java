/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package meteringcomreader;

import java.io.PrintStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Juliusz
 */
public class StdOutErrLog {
    private static final Logger lgr = LoggerFactory.getLogger(StdOutErrLog.class);
    public static void tieSystemOutAndErrToLog() {
        System.setOut(createLoggingProxy(System.out, false));
        System.setErr(createLoggingProxy(System.err, true));
    }

    public static PrintStream createLoggingProxy(final PrintStream realPrintStream, boolean isErr) {
        if (isErr)
            return new PrintStream(realPrintStream) {
                @Override
                public void print(final String string) {
                    //realPrintStream.print(string); 
                    lgr.error(string);
                }
            };
        else
            return new PrintStream(realPrintStream) {
                @Override
                public void print(final String string) {
                    //realPrintStream.print(string);
                    lgr.info(string);
                }
            };            
    }
    
}
