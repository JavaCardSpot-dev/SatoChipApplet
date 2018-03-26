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
            // demoSingleCommand2();
            setupCommand();
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

    public static ResponseAPDU setupCommand() throws Exception {
        final CardManager cardMngr = new CardManager(true, APPLET_AID_BYTE);
        final RunConfig runCfg = RunConfig.getDefaultConfig();

        // Running on physical card
//        runCfg.setTestCardType(RunConfig.CARD_TYPE.PHYSICAL);

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

        byte pin_tries_0 = 0x10;
        byte ublk_tries_0 = 0x10;
//        byte[] pin_0 = {0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38};
        byte[] pin_0 = {0x4D, 0x75, 0x73, 0x63, 0x6C, 0x65, 0x30, 0x30}; //same as default
        byte[] ublk_0 = {0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38};
        byte pin_tries_1 = 0x10;
        byte ublk_tries_1 = 0x10;
        byte[] pin_1 = {0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38};
        byte[] ublk_1 = {0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38};
        short secmemsize = 0x1000;
        short memsize = 0x1000;
        byte create_object_ACL = 0x01;
        byte create_key_ACL = 0x01;
        byte create_pin_ACL = 0x01;

        byte[] data = createSetupData(pin_tries_0, ublk_tries_0, pin_0, ublk_0,
                pin_tries_1, ublk_tries_1, pin_1, ublk_1,
                secmemsize, memsize,
                create_object_ACL, create_key_ACL, create_pin_ACL,
                (short)0, null, 0);

        final ResponseAPDU response = cardMngr.transmit(new CommandAPDU(0xB0, 0x2A, 0x00, 0x00, data, 0x00));
        System.out.println(response);

        return response;
    }

    public static byte[] createSetupData(
            byte pin_tries_0, byte ublk_tries_0,
            byte[] pin_0, byte[] ublk_0,
            byte pin_tries_1, byte ublk_tries_1,
            byte[] pin_1, byte[] ublk_1,
            short memsize, short memsize2,
            byte create_object_ACL, byte create_key_ACL, byte create_pin_ACL,
            short option_flags,
            byte[] hmacsha160_key, long amount_limit) throws Exception {

        // to do: check pin sizes < 256
        byte[] pin={0x4D, 0x75, 0x73, 0x63, 0x6C, 0x65, 0x30, 0x30}; // default pin

        // data=[pin_length(1) | pin |
        //       pin_tries0(1) | ublk_tries0(1) | pin0_length(1) | pin0 | ublk0_length(1) | ublk0 |
        //       pin_tries1(1) | ublk_tries1(1) | pin1_length(1) | pin1 | ublk1_length(1) | ublk1 |
        //       memsize(2) | memsize2(2) | ACL(3) |
        //       option_flags(2) | hmacsha160_key(20) | amount_limit(8)]
        int optionsize= ((option_flags==0)?0:2) + (((option_flags&0x8000)==0x8000)?28:0);
        int datasize= 16+pin.length+pin_0.length+pin_1.length+ublk_0.length+ublk_1.length+optionsize;
        byte[] data= new byte[datasize];
        short base=0;
        //initial PIN check
        data[base++]=(byte)pin.length;

        for (int i=0; i<pin.length; i++){
            data[base++]=pin[i]; // default PIN
        }
        //pin0+ublk0
        data[base++]=pin_tries_0;
        data[base++]=ublk_tries_0;
        data[base++]=(byte)pin_0.length;
        for (int i=0; i<pin_0.length; i++){
            data[base++]=pin_0[i];
        }
        data[base++]=(byte)ublk_0.length;
        for (int i=0; i<ublk_0.length; i++){
            data[base++]=ublk_0[i];
        }
        //pin1+ublk1
        data[base++]=pin_tries_1;
        data[base++]=ublk_tries_1;
        data[base++]=(byte)pin_1.length;
        for (int i=0; i<pin_1.length; i++){
            data[base++]=pin_1[i];
        }
        data[base++]=(byte)ublk_1.length;
        for (int i=0; i<ublk_1.length; i++){
            data[base++]=ublk_1[i];
        }
        // 2bytes
        data[base++]= (byte)(memsize>>8);
        data[base++]= (byte)(memsize&0x00ff);
        // mem_size
        data[base++]= (byte)(memsize2>>8);
        data[base++]= (byte)(memsize2&0x00ff);
        // acl
        data[base++]= create_object_ACL;
        data[base++]= create_key_ACL;
        data[base++]= create_pin_ACL;
        // option_flags
        if (option_flags!=0){
            data[base++]= (byte)(option_flags>>8);
            data[base++]= (byte)(option_flags&0x00ff);
            // hmacsha1_key
            System.arraycopy(hmacsha160_key, 0, data, base, 20);
            base+=20;
            // amount_limit
            for (int i=56; i>=0; i-=8){
                data[base++]=(byte)((amount_limit>>i)&0xff);
            }
        }
        return data;
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
