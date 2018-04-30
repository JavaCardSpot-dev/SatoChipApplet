package tests;

import SatoChipClient.CardDataParser;
import org.junit.Assert;
import org.testng.annotations.*;

import javax.smartcardio.ResponseAPDU;

import static org.junit.Assert.assertArrayEquals;

/**
 * Example test class for the applet
 * Note: If simulator cannot be started try adding "-noverify" JVM parameter
 *
 * @author xsvenda, Dusan Klinec (ph4r05)
 */
public class AppletTest {

    public AppletTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    @Test
    public void setup() throws Exception {
        final ResponseAPDU responseAPDU = SimpleAPDU.testSetupCommand(true);
        Assert.assertNotNull(responseAPDU);
        Assert.assertEquals(0x9000, responseAPDU.getSW());
        Assert.assertNotNull(responseAPDU.getBytes());
    }

    @Test
    public void cardPIN() throws Exception {
        final ResponseAPDU responseAPDU = SimpleAPDU.testCardPIN(true);
        Assert.assertNotNull(responseAPDU);
        Assert.assertEquals(0x9000, responseAPDU.getSW());
        Assert.assertNotNull(responseAPDU.getBytes());
    }

    @Test
    public void createObject() throws Exception {
        final ResponseAPDU responseAPDU = SimpleAPDU.testObjectManager(true);
        Assert.assertNotNull(responseAPDU);
        Assert.assertEquals(0x9000, responseAPDU.getSW());
        Assert.assertNotNull(responseAPDU.getBytes());
    }

    @Test
    public void getCardStatus() throws Exception {
        final ResponseAPDU responseAPDU = SimpleAPDU.testCardGetStatus(true);
        Assert.assertNotNull(responseAPDU);
        Assert.assertEquals(0x9000, responseAPDU.getSW());
        Assert.assertNotNull(responseAPDU.getBytes());
    }

    @Test
    public void CardBip32ImportSeed() throws Exception {
        final ResponseAPDU responseAPDU = SimpleAPDU.testCardBip32ImportSeed(true);
        Assert.assertNotNull(responseAPDU);
        Assert.assertEquals(0x9000, responseAPDU.getSW());
        Assert.assertNotNull(responseAPDU.getBytes());
    }

    @Test
    public void CardBip32GetAythentiKey() throws Exception {
        final ResponseAPDU responseAPDU = SimpleAPDU.testCardBip32GetAuthentiKey(true);
        Assert.assertNotNull(responseAPDU);
        Assert.assertEquals(0x9000, responseAPDU.getSW());
        Assert.assertNotNull(responseAPDU.getBytes());
    }

    @Test
    public void testEqualsKeys() throws Exception {
        ResponseAPDU resAuthKey = SimpleAPDU.testCardBip32ImportSeed(true);
        Assert.assertNotNull(resAuthKey);
        Assert.assertEquals(0x9000, resAuthKey.getSW());
        Assert.assertNotNull(resAuthKey.getBytes());
        CardDataParser.PubKeyData parser = new CardDataParser.PubKeyData();
        byte[] authentikey = parser.parseBip32ImportSeed(resAuthKey.getData()).authentikey;

        ResponseAPDU resRecKey = SimpleAPDU.testCardBip32GetAuthentiKey(true);
        Assert.assertNotNull(resRecKey);
        Assert.assertEquals(0x9000, resRecKey.getSW());
        Assert.assertNotNull(resRecKey.getBytes());
        CardDataParser.PubKeyData pubkeydata = new CardDataParser.PubKeyData();
        byte[] recoveredkey= pubkeydata.parseBip32GetAuthentikey(resRecKey.getData()).authentikey;

        assertArrayEquals(recoveredkey, authentikey);
    }

    @Test
    public void CardImportKey() throws Exception {
        final ResponseAPDU responseAPDU = SimpleAPDU.testCardImportKey(true);
        Assert.assertNotNull(responseAPDU);
        Assert.assertEquals(0x9000, responseAPDU.getSW());
        Assert.assertNotNull(responseAPDU.getBytes());
    }

    @Test
    public void GetPublicKeyFromPrivate() throws Exception {
        final ResponseAPDU responseAPDU = SimpleAPDU.testGetPublicKeyFromPrivate((byte) 0x06, true);
        Assert.assertNotNull(responseAPDU);
        Assert.assertEquals(0x9000, responseAPDU.getSW());
        Assert.assertNotNull(responseAPDU.getBytes());
    }

    @Test
    public void CardSignMessage() throws Exception {
        String strmsg= "abcdefghijklmnopqrstuvwxyz0123456789";
        byte std_keynbr= 0x06;
        final ResponseAPDU responseAPDU = SimpleAPDU.testCardSignMessage(strmsg, std_keynbr, true);
        Assert.assertNotNull(responseAPDU);
        Assert.assertEquals(0x9000, responseAPDU.getSW());
        Assert.assertNotNull(responseAPDU.getBytes());
    }

    @Test
    public void CardSignEmptyMessage() throws Exception {
        String strmsg_long="";
        byte std_keynbr= 0x06;
        final ResponseAPDU responseAPDU = SimpleAPDU.testCardSignMessage(strmsg_long, std_keynbr, true);
        Assert.assertNotNull(responseAPDU);
        Assert.assertEquals(0x9000, responseAPDU.getSW());
        Assert.assertNotNull(responseAPDU.getBytes());
    }
}
