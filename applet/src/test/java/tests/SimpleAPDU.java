package tests;

import applet.CardEdge;
import cardTools.CardManager;
import cardTools.RunConfig;
import cardTools.Util;

import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Test class.
 * Note: If simulator cannot be started try adding "-noverify" JVM parameter
 *
 * @author Petr Svenda, Dusan Klinec (ph4r05)
 */
public class SimpleAPDU {
    private static String APPLET_AID = "5361746F4368697000";
    private static byte APPLET_AID_BYTE[] = Util.hexStringToByteArray(APPLET_AID);

    private static final String STR_APDU_DUMMY = "00C00000080000000000000000";

    /**
     * Main entry point.
     *
     * @param args
     */
    public static void main(String[] args) {
        try {
            demoSingleCommand();
        } catch (Exception ex) {
            System.out.println("Exception : " + ex);
        }
    }

    public static ResponseAPDU demoSingleCommand() throws Exception {
        final CardManager cardMngr = new CardManager(true, APPLET_AID_BYTE);
        final RunConfig runCfg = RunConfig.getDefaultConfig();

        // Running on physical card
        runCfg.setTestCardType(RunConfig.CARD_TYPE.PHYSICAL);

        // Running in the simulator
        //runCfg.setAppletToSimulate(CardEdge.class)
        //        .setTestCardType(RunConfig.CARD_TYPE.JCARDSIMLOCAL)
        //        .setbReuploadApplet(true)
        //        .setInstallData(new byte[8]);

        System.out.print("Connecting to card...");
        if (!cardMngr.Connect(runCfg)) {
            return null;
        }
        System.out.println(" Done.");

        //final ResponseAPDU response = sendCommandWithInitSequence(cardMngr, STR_APDU_DUMMY, null);
        //System.out.println(response);
        
        byte[] pin_init = {(byte) 'M', (byte) 'u', (byte) 's', (byte) 'c', (byte) 'l', (byte) 'e', (byte) '0', (byte) '0'};
        System.out.println( pin_init.length );
        System.out.println(Arrays.toString(pin_init));
        
        byte[] data = {
            //Initial pin length + initial pin
            (byte) 8, (byte) 'M', (byte) 'u', (byte) 's', (byte) 'c', (byte) 'l', (byte) 'e', (byte) '0', (byte) '0',
            //pin tries + ublk pin tries
            (byte) 4, (byte) 4,
            //pin length + pin    
            (byte) 4, (byte) 'M', (byte) 'M', (byte) 'M', (byte) 'M',
            //ublk pin length + ublk pin
            (byte) 4, (byte) 'M', (byte) 'M', (byte) 'M', (byte) 'M',
            //Again for pin[1] and ublk[1]
            //pin tries + ublk pin tries
            (byte) 4, (byte) 4,
            //pin length + pin    
            (byte) 4, (byte) 'M', (byte) 'M', (byte) 'M', (byte) 'M',
            //ublk pin length + ublk pin
            (byte) 4, (byte) 'M', (byte) 'M', (byte) 'M', (byte) 'M'
        };
        System.out.println("Data sent: " + Arrays.toString(data));
        final ResponseAPDU response = cardMngr.transmit(new CommandAPDU(0xB0, 0x2A, 0x00, 0x00, data, 0x00));
        System.out.println(response);
        
        return response;
    }

    /**
     * Sending command to the card.
     * Enables to send init commands before the main one.
     *
     * @param cardMngr
     * @param command
     * @param initCommands
     * @return
     * @throws CardException
     */
    public static ResponseAPDU sendCommandWithInitSequence(CardManager cardMngr, String command, ArrayList<String>  initCommands) throws CardException {
        if (initCommands != null) {
            for (String cmd : initCommands) {
                cardMngr.getChannel().transmit(new CommandAPDU(Util.hexStringToByteArray(cmd)));
            }
        }

        final ResponseAPDU resp = cardMngr.getChannel().transmit(new CommandAPDU(Util.hexStringToByteArray(command)));
        return resp;
    }

}
