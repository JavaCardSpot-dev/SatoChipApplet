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
            //demoSingleCommand2();
            setupCommand();
            testCardPIN();
        } catch (Exception ex) {
            System.out.println("Exception : " + ex);
        }
    }

    public static ResponseAPDU demoSingleCommand() throws Exception {
        final CardManager cardMngr = new CardManager(true, APPLET_AID_BYTE);
        final RunConfig runCfg = RunConfig.getDefaultConfig();

        // Running on physical card
        //runCfg.setTestCardType(RunConfig.CARD_TYPE.PHYSICAL);

        // Running in the simulator
        runCfg.setAppletToSimulate(CardEdge.class)
                .setTestCardType(RunConfig.CARD_TYPE.JCARDSIMLOCAL)
                .setbReuploadApplet(true)
                .setInstallData(new byte[8]);

        System.out.print("Connecting to card...");
        if (!cardMngr.Connect(runCfg)) {
            return null;
        }
        System.out.println(" Done.");

        final ResponseAPDU response = sendCommandWithInitSequence(cardMngr, STR_APDU_DUMMY, null);
        System.out.println(response);

        return response;
    }

    public static ResponseAPDU demoSingleCommand2() throws Exception {
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
		
		//Won't be used, just a reference what the inside of setup data looks like
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
                (byte) 4, (byte) 'M', (byte) 'M', (byte) 'M', (byte) 'M',
				//mem_size 2bytes
				(byte)(10>>8), (byte)(10&0x00ff),
				(byte)(10>>8), (byte)(10&0x00ff),
        };
        System.out.println("Data sent: " + Arrays.toString(data));
        final ResponseAPDU response = cardMngr.transmit(new CommandAPDU(0xB0, 0x2A, 0x00, 0x00, data, 0x00));
        System.out.println(response);

        return response;
    }

    public static ResponseAPDU setupCommand() throws Exception {
        final CardManager cardMngr = new CardManager(true, APPLET_AID_BYTE);
        final RunConfig runCfg = RunConfig.getDefaultConfig();

        // Running on physical card
        // runCfg.setTestCardType(RunConfig.CARD_TYPE.PHYSICAL);

        // Running in the simulator
        runCfg.setAppletToSimulate(CardEdge.class)
                .setTestCardType(RunConfig.CARD_TYPE.JCARDSIMLOCAL)
                .setbReuploadApplet(true)
                .setInstallData(new byte[8]);

        System.out.print("Connecting to card...");
        if (!cardMngr.Connect(runCfg)) {
            return null;
        }
        System.out.println(" Done.");

        byte[] data = SatoChipAppletTest.createSetupData();
        final ResponseAPDU response = cardMngr.transmit(new CommandAPDU(0xB0, 0x2A, 0x00, 0x00, data, 0x00));
        System.out.println(response);

        return response;
    }

    public static ResponseAPDU testCardPIN() throws Exception {
        final CardManager cardMngr = new CardManager(true, APPLET_AID_BYTE);
        final RunConfig runCfg = RunConfig.getDefaultConfig();

        // Running on physical card
        // runCfg.setTestCardType(RunConfig.CARD_TYPE.PHYSICAL);

        // Running in the simulator
        runCfg.setAppletToSimulate(CardEdge.class)
                .setTestCardType(RunConfig.CARD_TYPE.JCARDSIMLOCAL)
                .setbReuploadApplet(true)
                .setInstallData(new byte[8]);

        System.out.print("Connecting to card...");
        if (!cardMngr.Connect(runCfg)) {
            return null;
        }
        System.out.println(" Done.");

        // 1. Setup data
        byte[] data = SatoChipAppletTest.createSetupData();
        ResponseAPDU response = cardMngr.transmit(new CommandAPDU(0xB0, 0x2A, 0x00, 0x00, data, 0x00));
        System.out.println(response);

        byte[] pin = {0x4D, 0x75, 0x73, 0x63, 0x6C, 0x65, 0x30, 0x30};
        byte[] ublk = {0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38};

        // 2. Verify pin
        System.out.println("cardVerifyPIN");
        byte[] verifData= new byte[pin.length];
        short verifBase=0;
        for (int i=0; i<pin.length; i++){
            verifData[verifBase++]=pin[i];
        }
        response = cardMngr.transmit(new CommandAPDU(0xB0, 0x42, 0x00, 0x00, verifData, 0x00));
        System.out.println(response);

        // 3. try create pin + ublk
        System.out.println("cardCreatePIN");
        byte[] pinData = new byte[1+pin.length+1+ublk.length];
        short pinBase=0;
        pinData[pinBase++]=(byte)pin.length;
        for (int i=0; i<pin.length; i++){
            pinData[pinBase++]=pin[i];
        }
        pinData[pinBase++]=(byte)ublk.length;
        for (int i=0; i<ublk.length; i++){
            pinData[pinBase++]=ublk[i];
        }
        try {
            response = cardMngr.transmit(new CommandAPDU(0xB0, 0x40, 2, 3, pinData, 0x00));
            System.out.println(response);
        } catch (Exception ex) {
            if (response.getSW()== 0x9C10)
                System.out.println("PIN exists already!");
            else
                throw ex;
        }

        // 4. Change pin
        System.out.println("cardChangePIN");
        byte[] new_pin = {33,33,33,33};
        byte[] changeData= new byte[1+pin.length+1+new_pin.length];
        short changeBase=0;
        changeData[changeBase++]=(byte)pin.length;
        for (int i=0; i<pin.length; i++){
            changeData[changeBase++]=pin[i];
        }
        changeData[changeBase++]=(byte)new_pin.length;
        for (int i=0; i<new_pin.length; i++){
            changeData[changeBase++]=new_pin[i];
        }
        response = cardMngr.transmit(new CommandAPDU(0xB0, 0x44, 2, 0x00, changeData, 0x00));
        System.out.println(response);
        if (response.getSW() != 0x9000) {
            System.out.println("Error: change pin!");
            return response;
        }

        // 5. Verify new pin
        System.out.println("cardVerifyPIN (new PIN)");
        byte[] verif2Data= new byte[pin.length];
        short verif2Base=0;
        for (int i=0; i<pin.length; i++){
            verif2Data[verif2Base++]=pin[i];
        }
        response = cardMngr.transmit(new CommandAPDU(0xB0, 0x42, 0x00, 0x00, verif2Data, 0x00));
        System.out.println(response);
        if (response.getSW() != 0x9000) {
            System.out.println("Error: verify new pin!");
            return response;
        }

        // 6. Back to old pin
        System.out.println("cardChangePIN (back to old PIN)");
        byte[] changeBackData= new byte[1+pin.length+1+new_pin.length];
        short changeBackBase=0;
        changeBackData[changeBackBase++]=(byte)new_pin.length;
        for (int i=0; i<new_pin.length; i++){
            changeBackData[changeBackBase++]=new_pin[i];
        }
        changeBackData[changeBackBase++]=(byte)pin.length;
        for (int i=0; i<pin.length; i++){
            changeBackData[changeBackBase++]=pin[i];
        }
        response = cardMngr.transmit(new CommandAPDU(0xB0, 0x44, 2, 0x00, changeBackData, 0x00));
        System.out.println(response);
        if (response.getSW() != 0x9000) {
            System.out.println("Error: change to original pin!");
            return response;
        }

        // 7. Verify original pin
        System.out.println("cardVerifyPIN");
        response = cardMngr.transmit(new CommandAPDU(0xB0, 0x42, 2, 0x00, verifData, 0x00));
        System.out.println(response);
        if (response.getSW() != 0x9000) {
            System.out.println("Error: verify back pin!");
            return response;
        }

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
