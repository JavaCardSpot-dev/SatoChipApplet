package tests;

import applet.CardEdge;
import cardTools.CardManager;
import cardTools.RunConfig;
import cardTools.Util;

import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import java.util.Arrays;

import javax.xml.bind.DatatypeConverter;

import com.google.bitcoin.core.ECKey;
import com.google.bitcoin.crypto.DeterministicKey;

import SatoChipClient.CardDataParser;
import static SatoChipClient.CardDataParser.toHexString;
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

    /**
     * Main entry point.
     *
     * @param args
     */
    public static void main(String[] args) {
        try {
            // testSetupCommand();
            // testCardPIN();
            // testCardGetStatus();
            // testCardBip32ImportSeed();
            // testCardBip32GetAuthentiKey();
            // testCardImportKey();
            // testGetPublicKeyFromPrivate((byte) 0x06);
            testCardSignMessage();
        } catch (Exception ex) {
            System.out.println("Exception : " + ex);
        }
    }

    public static ResponseAPDU testSetupCommand() throws Exception {
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
            //return response;
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
            // return response;
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
            // return res;
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
            // return res;
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
        System.out.println("authentikey: " + toHexString(authentikey));

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
            // return res;
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

        byte cla = (byte) 0xB0;
        byte ins = 0x73;
        byte p1 = 0x00;
        byte p2 = 0x00;
        byte[] data = null;
        byte le = 0x00;
        short base = 0;

        // send apdu
        ResponseAPDU response = cardMngr.transmit(new CommandAPDU(cla, ins, p1, p2, data, le));

        CardDataParser.PubKeyData pubkeydata = new CardDataParser.PubKeyData();
        byte[] recoveredkey = pubkeydata.parseBip32GetAuthentikey(response.getData()).authentikey;
        if (recoveredkey == null) {
            System.out.println("Create recoveredkey failed");
            return response;
        }
        System.out.println("recoveredkey: " + toHexString(recoveredkey));

//        // 5. test equals keys
//        CardDataParser.PubKeyData parser = new CardDataParser.PubKeyData();
//        byte[] authentikey = parser.parseBip32ImportSeed(resAuthKey.getData()).authentikey;
//
//        assertArrayEquals(recoveredkey, authentikey);

        return response;
    }

    public static ResponseAPDU testCardImportKey() throws Exception {
        // Card import key
        System.out.println("cardImportKey");

        testImportKey((byte) 4, (byte) 0x01, (short) 512);
        testImportKey((byte) 5, (byte) 0x02, (short) 512);
        testImportKey((byte) 4, (byte) 0x03, (short) 512);
        testImportKey((byte) 12, (byte) 0x06, (short) 256);

        testGetPublicKeyFromPrivate((byte) 0x06);

        testImportKey((byte) 3, (byte) 0x0A, (short) 64);
        testImportKey((byte) 3, (byte) 0x0B, (short) 128);
        testImportKey((byte) 3, (byte) 0x0C, (short) 192);
        testImportKey((byte) 3, (byte) 0x0D, (short) 128);
        testImportKey((byte) 3, (byte) 0x0E, (short) 192);
        testImportKey((byte) 3, (byte) 0x0F, (short) 256);

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
            // return res;
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

        // list key
        byte cla = (byte) 0xB0;
        byte ins = (byte) 0x3C;
        byte p1 = 0x00;
        byte p2 = 0x00;
        byte[] data = null;
        byte le = 0x10; // 16 bytes expected?

        ResponseAPDU response = cardMngr.transmit(new CommandAPDU(cla, ins, p1, p2, data, le));

        CardDataParser.CardStatus cardstatus = new CardDataParser.CardStatus(response.getData());
        System.out.println(cardstatus.toString());
        return response;
    }

    public static ResponseAPDU testImportKey(byte key_type, byte key_nbr, short key_size) throws Exception {
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
            // return res;
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

        byte key_encoding = 0x00; //plain
        byte[] key_ACL = {0x00, 0x01, 0x00, 0x01, 0x00, 0x01}; //{0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        String stralg = "";
        String strkey = "";
        short keysize = key_size;
        if (key_type == (byte) 12) {
            stralg = "ECpriv";
            keysize = 256;
            strkey = "0020" + "7bb8bfeb2ebc1401f9a14585032df07126ddf634ca641b7fa223b44b1e861548";//pycoin ku P:toporin
        } else if (key_type == (byte) 11) {
            stralg = "ECpub";
            keysize = 256;
            strkey = "0041" //short blob size (0x41=65)
                    + "04" //uncompressed
                    + "8d68936ac800d3fc1cf999bfe0a3af4ead4cf9ad61d3cb377c3e5626b5bfa9e8" // coordx
                    + "d682abeb1337c9b97d114f757bdd81e0207ad673d736eb6b4a84890be5f92335";// coordy
        } else if (key_type == (byte) 4) {
            stralg = "RSApub";
            keysize = 512;
            strkey = "0040"// 0x40=64 modsize (byte)
                    + "88d8b1c3ac39311ac82af63d6aeb3ea9cd05a28975cbc30203be81339f1341dac60e8afda1130e25e83e64e3112b9fb43c2e1ee47b8f6e164204c526bd7621e5" //mod
                    + "0003" // expsize
                    + "010001"; // exponent
        } else if (key_type == (byte) 5) {
            stralg = "RSApriv";
            keysize = 512;
            strkey = "0040"// 0x40=64 modsize (byte)
                    + "88d8b1c3ac39311ac82af63d6aeb3ea9cd05a28975cbc30203be81339f1341dac60e8afda1130e25e83e64e3112b9fb43c2e1ee47b8f6e164204c526bd7621e5" //mod
                    + "0040" // expsize
                    + "60da7d762ffe8a729a194e0e4a0e155bb86fb489f585318fcb76999b1f8b519fa41e55ba3c6294b5eaf1dc333191299ea10f5ca8507c3f120111396686554641";
        } else if (key_type == (byte) 6) {
            stralg = "RSA-CRTpriv";
            keysize = 512;
            strkey = "0020"
                    + "f07c528f200b28b8e8ff4d73079730179bcec63b61a3012b849434ee4de389af"//P
                    + "0020"
                    + "91acbf0d2dc68b213b6dad87cddc580901f646401eee8c1946d395d44c45f6ab"//Q
                    + "0020"
                    + "264034c60f9b06db8721d655eacb8708ae68533f310b31cc879c16227857abdb"//Qinv
                    + "0020"
                    + "b6350bfc8343d133e0dd66da0bdb4245f0f846fbc0eb573c98b40e32ac7304e3"//DP1
                    + "0020"
                    + "1907511bf68d7242176fd4accc95db1a5117fb21f12e932b949badd677f45d59";//DQ1
        } else if (key_type == (byte) 15 && key_size == 128) {
            stralg = "AES-128";
            keysize = 128;
            strkey = "0010" + "000102030405060708090a0b0c0d0e0f";//0x10=16
        } else if (key_type == (byte) 15 && key_size == 192) {
            stralg = "AES-192";
            keysize = 192;
            strkey = "0018" + "000102030405060708090a0b0c0d0e0f0001020304050607";//0x18=24
        } else if (key_type == (byte) 15 && key_size == 256) {
            stralg = "AES-256";
            keysize = 256;
            strkey = "0020" + "000102030405060708090a0b0c0d0e0f000102030405060708090a0b0c0d0e0f";//0x20=32
        } else if (key_type == (byte) 3 && key_size == 64) {
            stralg = "DES-64";
            keysize = 64;
            strkey = "0008" + "0001020304050607";//0x08=8
        } else if (key_type == (byte) 3 && key_size == 128) {
            stralg = "DES-128";
            keysize = 128;
            strkey = "0010" + "000102030405060708090a0b0c0d0e0f";//0x10=16
        } else if (key_type == (byte) 3 && key_size == 192) {
            stralg = "DES-192";
            keysize = 192;
            strkey = "0018" + "000102030405060708090a0b0c0d0e0f0001020304050607";//0x18=24
        } else {
            System.out.println("ERROR: key type not supported!");
            return null;
        }

        byte[] keyblob = DatatypeConverter.parseHexBinary(strkey);

        System.out.println("TestImportKey(key=" + stralg + ", nb=" + (int) key_nbr + ", keysize=" + keysize + ")"); // jcop-ko);
        if (keyblob.length > 242) {
            System.out.println("Invalid data size (>242)");
            return null;
        }

        //data=[ key_encoding(1) | key_type(1) | key_size(2) | key_ACL(6) | key_blob(n)]
        byte cla = (byte) 0xB0;
        byte ins = 0x32;
        byte p1 = key_nbr;
        byte p2 = 0x00;
        byte[] data = new byte[1 + 1 + 2 + 6 + keyblob.length];
        byte le = 0x00;
        short base = 0;

        data[base++] = key_encoding;
        data[base++] = key_type;
        data[base++] = (byte) (key_size >> 8);//most significant byte
        data[base++] = (byte) (key_size & 0x00FF);//least significant byte
        System.arraycopy(key_ACL, 0, data, base, (byte) 6);
        base += 6;
        System.arraycopy(keyblob, 0, data, base, keyblob.length);
        base += keyblob.length;

        // import key command (data taken from imported object)
        ResponseAPDU response = cardMngr.transmit(new CommandAPDU(cla, ins, p1, p2, data, le));

        return response;
    }

    public static ResponseAPDU testGetPublicKeyFromPrivate(byte keynbr) throws Exception {
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
            // return res;
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

        // 3. import key
        res = testImportKey((byte) 12, (byte) 0x06, (short) 256);
        if (res.getSW() != 0x9000) {
            System.out.println("Error: import key!");
            return res;
        }

        // 4. get public key
        System.out.println("GetPublicKey");

        byte cla = (byte) 0xB0;
        byte ins = 0x35;
        byte p1 = keynbr;
        byte p2 = 0x00;
        byte[] data = null;
        byte le = 0x00;

        // send apdu
        ResponseAPDU response = cardMngr.transmit(new CommandAPDU(cla, ins, p1, p2, data, le));
        if (response.getSW() != 0x9000) {
            System.out.println("Error: get public key!");
            return response;
        }

        CardDataParser.PubKeyData parser = new CardDataParser.PubKeyData();
        byte[] pubkey = parser.parseGetPublicKeyFromPrivate(response.getData()).pubkey;
        if (pubkey == null) {
            System.out.println("Create pubkey failed");
            return response;
        }
        System.out.println("pubkey: " + toHexString(pubkey));

        return response;
    }

    public static void testCardSignMessage() throws Exception {
        System.out.println("cardSignMessage");

        String strmsg = "abcdefghijklmnopqrstuvwxyz0123456789";
        String strmsg_long = "";
        byte std_keynbr = 0x06;//0x00;

        testCardSignMessage(strmsg, std_keynbr);
        testCardSignMessage(strmsg_long, std_keynbr);

        // todo sign message test for long message
    }

    public static ResponseAPDU testCardSignMessage(String strmsg, byte keynbr) throws Exception {
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
            // return res;
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

        // 3. import key
        res = testImportKey((byte) 12, (byte) 0x06, (short) 256);
        if (res.getSW() != 0x9000) {
            System.out.println("Error: import key!");
            return res;
        }

        // 4. recover pubkey
        ResponseAPDU response = testGetPublicKeyFromPrivate(keynbr);
        if (response.getSW() != 0x9000) {
            System.out.println("Error: get private key!");
            return response;
        }
        CardDataParser.PubKeyData parser = new CardDataParser.PubKeyData();
        byte[] pubkey = parser.parseGetPublicKeyFromPrivate(response.getData()).pubkey;
        if (pubkey == null) {
            System.out.println("Create pubkey failed");
            return response;
        }
        System.out.println("signing pubkey: " + toHexString(pubkey));

        // 5. sign message
        byte[] msg = strmsg.getBytes();
        // for message less than one chunk in size
        byte cla = (byte) 0xB0;
        byte ins = 0x72;
        byte p1 = keynbr; // oxff=>BIP32 otherwise STD
        byte p2 = 0x00;
        byte[] data = new byte[msg.length + 2];
        byte le = 0x00;
        short base = 0;

        data[0] = (byte) (msg.length >> 8 & 0xFF);
        data[1] = (byte) (msg.length & 0xFF);
        base += 2;
        System.arraycopy(msg, 0, data, base, msg.length);
        base += msg.length;

        // send apdu
        ResponseAPDU resSign = cardMngr.transmit(new CommandAPDU(cla, ins, p1, p2, data, le));
        byte[] signature = resSign.getData();

        // 6. parse signature
        System.out.println("signature: " + toHexString(signature));
        CardDataParser.PubKeyData pubkeydata = new CardDataParser.PubKeyData();
        String strsignature64 = pubkeydata.parseMessageSigning(signature, pubkey, strmsg).compactsig_b64_str;
        System.out.println("signature in base64: " + strsignature64);

        // 7. verify with bitcoinj
        ECKey eckey = ECKey.signedMessageToKey(strmsg, strsignature64);
        System.out.println("recovered pubkey: " + toHexString(eckey.getPubKey()));
        assertArrayEquals(pubkey, eckey.getPubKey());

        return response;
    }
}
