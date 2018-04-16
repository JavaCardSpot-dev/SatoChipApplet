package tests;

import SatoChipClient.CardDataParser;
import applet.CardEdge;
import cardTools.CardManager;
import cardTools.RunConfig;
import cardTools.Util;

import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import java.util.ArrayList;
import java.util.Arrays;

import javax.xml.bind.DatatypeConverter;

import com.google.bitcoin.crypto.DeterministicKey;

import static org.junit.Assert.assertArrayEquals;

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
            // setupCommand();
            // testCardPIN();
            // testCardGetStatus();
            // testCardBip32ImportSeed();
            testCardBip32GetAuthentiKey();
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
                (byte) (10 >> 8), (byte) (10 & 0x00ff),
                (byte) (10 >> 8), (byte) (10 & 0x00ff),
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
        if (response.getSW() != 0x9000) {
            System.out.println("Error: setup card!");
            return response;
        }

        byte[] pin = {0x4D, 0x75, 0x73, 0x63, 0x6C, 0x65, 0x30, 0x30};
        byte[] ublk = {0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38};

        // 2. Verify pin
        System.out.println("cardVerifyPIN");
        byte[] verifData = new byte[pin.length];
        short verifBase = 0;
        for (int i = 0; i < pin.length; i++) {
            verifData[verifBase++] = pin[i];
        }
        response = cardMngr.transmit(new CommandAPDU(0xB0, 0x42, 0x00, 0x00, verifData, 0x00));
        System.out.println(response);

        // 3. try create pin + ublk
        System.out.println("cardCreatePIN");
        byte[] pinData = new byte[1 + pin.length + 1 + ublk.length];
        short pinBase = 0;
        pinData[pinBase++] = (byte) pin.length;
        for (int i = 0; i < pin.length; i++) {
            pinData[pinBase++] = pin[i];
        }
        pinData[pinBase++] = (byte) ublk.length;
        for (int i = 0; i < ublk.length; i++) {
            pinData[pinBase++] = ublk[i];
        }
        try {
            response = cardMngr.transmit(new CommandAPDU(0xB0, 0x40, 2, 3, pinData, 0x00));
            System.out.println(response);
        } catch (Exception ex) {
            if (response.getSW() == 0x9C10)
                System.out.println("PIN exists already!");
            else
                throw ex;
        }

        // 4. Change pin
        System.out.println("cardChangePIN");
        byte[] new_pin = {33, 33, 33, 33};
        byte[] changeData = new byte[1 + pin.length + 1 + new_pin.length];
        short changeBase = 0;
        changeData[changeBase++] = (byte) pin.length;
        for (int i = 0; i < pin.length; i++) {
            changeData[changeBase++] = pin[i];
        }
        changeData[changeBase++] = (byte) new_pin.length;
        for (int i = 0; i < new_pin.length; i++) {
            changeData[changeBase++] = new_pin[i];
        }
        response = cardMngr.transmit(new CommandAPDU(0xB0, 0x44, 2, 0x00, changeData, 0x00));
        System.out.println(response);
        if (response.getSW() != 0x9000) {
            System.out.println("Error: change pin!");
            return response;
        }

        // 5. Verify new pin
        System.out.println("cardVerifyPIN (new PIN)");
        byte[] verif2Data = new byte[pin.length];
        short verif2Base = 0;
        for (int i = 0; i < pin.length; i++) {
            verif2Data[verif2Base++] = pin[i];
        }
        response = cardMngr.transmit(new CommandAPDU(0xB0, 0x42, 0x00, 0x00, verif2Data, 0x00));
        System.out.println(response);
        if (response.getSW() != 0x9000) {
            System.out.println("Error: verify new pin!");
            return response;
        }

        // 6. Back to old pin
        System.out.println("cardChangePIN (back to old PIN)");
        byte[] changeBackData = new byte[1 + pin.length + 1 + new_pin.length];
        short changeBackBase = 0;
        changeBackData[changeBackBase++] = (byte) new_pin.length;
        for (int i = 0; i < new_pin.length; i++) {
            changeBackData[changeBackBase++] = new_pin[i];
        }
        changeBackData[changeBackBase++] = (byte) pin.length;
        for (int i = 0; i < pin.length; i++) {
            changeBackData[changeBackBase++] = pin[i];
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

    public static ResponseAPDU testObjectManager() throws Exception {
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
        byte[] setupData = SatoChipAppletTest.createSetupData();
        ResponseAPDU response = cardMngr.transmit(new CommandAPDU(0xB0, 0x2A, 0x00, 0x00, setupData, 0x00));
        System.out.println(response);
        if (response.getSW() != 0x9000) {
            System.out.println("Error: setup card!");
            return response;
        }

        byte[] pin = {0x4D, 0x75, 0x73, 0x63, 0x6C, 0x65, 0x30, 0x30};

        // 2. Verify pin
        System.out.println("cardVerifyPIN");
        byte[] verifData = new byte[pin.length];
        short verifBase = 0;
        for (int i = 0; i < pin.length; i++) {
            verifData[verifBase++] = pin[i];
        }
        response = cardMngr.transmit(new CommandAPDU(0xB0, 0x42, 0x00, 0x00, verifData, 0x00));
        System.out.println(response);

        // 3. create 1024 size object
        System.out.println("cardCreateObject");
        final int objSize = 1024;
        byte[] objData = new byte[objSize];
        Arrays.fill(objData, (byte) 0x00);

        byte cla = (byte) 0xB0;
        byte ins = (byte) 0x5A;
        byte p1 = 0x00;
        byte p2 = 0x00;
        byte[] data = new byte[14];
        byte le = 0x00;
        byte[] objACL = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

        int objId = 123456;

        {
            int offset = 0;
            data[offset++] = (byte) ((objId >>> 24) & 0xff);
            data[offset++] = (byte) ((objId >>> 16) & 0xff);
            data[offset++] = (byte) ((objId >>> 8) & 0xff);
            data[offset++] = (byte) ((objId) & 0xff);
            data[offset++] = (byte) ((objSize >>> 24) & 0xff);
            data[offset++] = (byte) ((objSize >>> 16) & 0xff);
            data[offset++] = (byte) ((objSize >>> 8) & 0xff);
            data[offset++] = (byte) ((objSize) & 0xff);
            System.arraycopy(objACL, 0, data, 8, objACL.length);

            response = cardMngr.transmit(new CommandAPDU(cla, ins, p1, p2, data, le));
        }

        // 4. write to object, write 200B of data
        System.out.println("cardWriteObject");
        byte[] objWriteData = new byte[200];
        Arrays.fill(objWriteData, (byte) 0x01);

        ins = 0x54; // INS for write
        data = new byte[4 + 4 + 1 + objWriteData.length];

        {
            int offset = 0;
            data[offset++] = (byte) ((objId >>> 24) & 0xff);
            data[offset++] = (byte) ((objId >>> 16) & 0xff);
            data[offset++] = (byte) ((objId >>> 8) & 0xff);
            data[offset++] = (byte) ((objId) & 0xff);
            data[offset++] = (byte) ((0 >>> 24) & 0xff);
            data[offset++] = (byte) ((0 >>> 16) & 0xff);
            data[offset++] = (byte) ((0 >>> 8) & 0xff);
            data[offset++] = (byte) ((0) & 0xff);
            data[offset++] = (byte) objWriteData.length;
            System.arraycopy(objWriteData, 0, data, offset, objWriteData.length);

            response = cardMngr.transmit(new CommandAPDU(cla, ins, p1, p2, data, le));
        }

        // 5. delete object with id objId
        System.out.println("cardDeleteObject");
        ins = 0x52;
        p2 = (byte) 0x01;   // secure erasure

        data = new byte[4];

        {
            int offset = 0;
            data[offset++] = (byte) ((objId >>> 24) & 0xff);
            data[offset++] = (byte) ((objId >>> 16) & 0xff);
            data[offset++] = (byte) ((objId >>> 8) & 0xff);
            data[offset++] = (byte) ((objId) & 0xff);

            response = cardMngr.transmit(new CommandAPDU(cla, ins, p1, p2, data, le));
        }

        return response;
    }

    public static ResponseAPDU testCardGetStatus() throws Exception {
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

        // 1. Setup data
        byte[] setupData = SatoChipAppletTest.createSetupData();
        ResponseAPDU res = cardMngr.transmit(new CommandAPDU(0xB0, 0x2A, 0x00, 0x00, setupData, 0x00));
        System.out.println(res);
        if (res.getSW() != 0x9000) {
            System.out.println("Error: setup card!");
            return res;
        }

        byte[] pin = {0x4D, 0x75, 0x73, 0x63, 0x6C, 0x65, 0x30, 0x30};

        // 2. Verify pin
        System.out.println("cardVerifyPIN");
        byte[] verifData = new byte[pin.length];
        short verifBase = 0;
        for (int i = 0; i < pin.length; i++) {
            verifData[verifBase++] = pin[i];
        }
        res = cardMngr.transmit(new CommandAPDU(0xB0, 0x42, 0x00, 0x00, verifData, 0x00));
        System.out.println(res);
        if (res.getSW() != 0x9000) {
            System.out.println("Error: verify pin!");
            return res;
        }

        // 3. get card status
        System.out.println("cardGetStatus");
        byte cla = (byte) 0xB0;
        byte ins = (byte) 0x3C;
        byte p1 = 0x00;
        byte p2 = 0x00;
        byte[] data = null;
        byte le = 0x10; // 16 bytes expected?

        ResponseAPDU response = cardMngr.transmit(new CommandAPDU(cla, ins, p1, p2, data, le));
        if (response.getSW() == 0x9c04) {
            System.out.println("Required setup is not not done");
            return response;
        }
        if (response.getSW() == 0x9c02) {
            System.out.println("Entered PIN is not correct");
            return response;
        }
        if (response.getSW() != 0x9000) {
            System.out.println("Error: get card status!");
            return response;
        }

        CardDataParser.CardStatus parser = new CardDataParser.CardStatus(response.getData());
        System.out.println(parser.toString());

        return response;
    }

    public static ResponseAPDU testCardBip32ImportSeed() throws Exception {
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

        // 1. Setup data
        byte[] setupData = SatoChipAppletTest.createSetupData();
        ResponseAPDU res = cardMngr.transmit(new CommandAPDU(0xB0, 0x2A, 0x00, 0x00, setupData, 0x00));
        System.out.println(res);
        if (res.getSW() != 0x9000) {
            System.out.println("Error: setup card!");
            return res;
        }

        byte[] pin = {0x4D, 0x75, 0x73, 0x63, 0x6C, 0x65, 0x30, 0x30};

        // 2. Verify pin
        System.out.println("cardVerifyPIN");
        byte[] verifData = new byte[pin.length];
        short verifBase = 0;
        for (int i = 0; i < pin.length; i++) {
            verifData[verifBase++] = pin[i];
        }
        res = cardMngr.transmit(new CommandAPDU(0xB0, 0x42, 0x00, 0x00, verifData, 0x00));
        System.out.println(res);
        if (res.getSW() != 0x9000) {
            System.out.println("Error: verify pin!");
            return res;
        }

        // 3. bip32 import seed
        System.out.println("cardBip32ImportSeed");

        String strseed = "31323334353637383132333435363738";// ascii for 1234567812345678
        byte[] authentikey = null;
        DeterministicKey masterkey = null;

        // import seed to HWchip
        long startTime = System.currentTimeMillis();
        byte[] seed = DatatypeConverter.parseHexBinary(strseed);
        byte[] seed_ACL = {0x00, 0x01, 0x00, 0x01, 0x00, 0x01}; //{0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        // byte[] response= cc.cardBip32ImportSeed(seed_ACL, seed);

        byte cla = (byte) 0xB0;
        byte ins = (byte) 0x6C;
        byte p1 = 0x00;
        byte p2 = 0x00;
        byte[] data = new byte[seed_ACL.length + 1 + seed.length];
        byte le = 0x00;
        short base = 0;

        System.arraycopy(seed_ACL, 0, data, base, seed_ACL.length);
        base += seed_ACL.length;
        data[base++] = (byte) seed.length;
        System.arraycopy(seed, 0, data, base, seed.length);

        // send apdu (contains sensitive data!)
        ResponseAPDU response = cardMngr.transmit(new CommandAPDU(cla, ins, p1, p2, data, le));
        if (response.getSW() == 0x9c04) {
            System.out.println("Required setup is not not done");
            return response;
        }
        if (response.getSW() == 0x9c02) {
            System.out.println("Entered PIN is not correct");
            return response;
        }
        if (response.getSW() != 0x9000) {
            System.out.println("Error: card bip32 import seed!");
            return response;
        }

        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        System.out.println("elapsed time: " + elapsedTime);

        CardDataParser.PubKeyData parser = new CardDataParser.PubKeyData();
        authentikey = parser.parseBip32ImportSeed(response.getData()).authentikey;
        if (authentikey == null) {
            System.out.println("Create authentikey failed");
            return response;
        }
        System.out.println("authentikey: " + CardDataParser.toHexString(authentikey));

        return response;
    }

    public static ResponseAPDU testCardBip32GetAuthentiKey() throws Exception {
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

        // 1. Setup data
        byte[] setupData = SatoChipAppletTest.createSetupData();
        ResponseAPDU res = cardMngr.transmit(new CommandAPDU(0xB0, 0x2A, 0x00, 0x00, setupData, 0x00));
        System.out.println(res);
        if (res.getSW() != 0x9000) {
            System.out.println("Error: setup card!");
            return res;
        }

        byte[] pin = {0x4D, 0x75, 0x73, 0x63, 0x6C, 0x65, 0x30, 0x30};

        // 2. Verify pin
        System.out.println("cardVerifyPIN");
        byte[] verifData = new byte[pin.length];
        short verifBase = 0;
        for (int i = 0; i < pin.length; i++) {
            verifData[verifBase++] = pin[i];
        }
        res = cardMngr.transmit(new CommandAPDU(0xB0, 0x42, 0x00, 0x00, verifData, 0x00));
        System.out.println(res);
        if (res.getSW() != 0x9000) {
            System.out.println("Error: verify pin!");
            return res;
        }

        // 3. Initialize seed (from previous test)
        ResponseAPDU resAuthKey = testCardBip32ImportSeed();
        if (resAuthKey.getSW() != 0x9000) {
            System.out.println("Error: initialize seed (from test)");
            return resAuthKey;
        }

        // 4. Card BIP32 get auth. key
        System.out.println("cardBip32GetAuthentiKey");

        byte cla= (byte) 0xB0;
        byte ins= 0x73;
        byte p1= 0x00;
        byte p2= 0x00;
        byte[] data= null;
        byte le= 0x00;
        short base=0;

        // send apdu
        ResponseAPDU response = cardMngr.transmit(new CommandAPDU(cla, ins, p1, p2, data, le));

        CardDataParser.PubKeyData pubkeydata = new CardDataParser.PubKeyData();
        byte[] recoveredkey= pubkeydata.parseBip32GetAuthentikey(response.getData()).authentikey;
        if (recoveredkey == null) {
            System.out.println("Create recoveredkey failed");
            return response;
        }
        System.out.println("recoveredkey: "+CardDataParser.toHexString(recoveredkey));

//        // 5. test equals keys
//        CardDataParser.PubKeyData parser = new CardDataParser.PubKeyData();
//        byte[] authentikey = parser.parseBip32ImportSeed(resAuthKey.getData()).authentikey;
//
//        assertArrayEquals(recoveredkey, authentikey);

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
    public static ResponseAPDU sendCommandWithInitSequence(CardManager cardMngr, String command, ArrayList<String> initCommands) throws CardException {
        if (initCommands != null) {
            for (String cmd : initCommands) {
                cardMngr.getChannel().transmit(new CommandAPDU(Util.hexStringToByteArray(cmd)));
            }
        }

        final ResponseAPDU resp = cardMngr.getChannel().transmit(new CommandAPDU(Util.hexStringToByteArray(command)));
        return resp;
    }

}
