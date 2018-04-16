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
        final ResponseAPDU responseAPDU = SimpleAPDU.setupCommand();
        Assert.assertNotNull(responseAPDU);
        Assert.assertEquals(0x9000, responseAPDU.getSW());
        Assert.assertNotNull(responseAPDU.getBytes());
    }

    @Test
    public void cardPIN() throws Exception {
        final ResponseAPDU responseAPDU = SimpleAPDU.testCardPIN();
        Assert.assertNotNull(responseAPDU);
        Assert.assertEquals(0x9000, responseAPDU.getSW());
        Assert.assertNotNull(responseAPDU.getBytes());
    }

    @Test
    public void createObject() throws Exception {
        final ResponseAPDU responseAPDU = SimpleAPDU.testObjectManager();
        Assert.assertNotNull(responseAPDU);
        Assert.assertEquals(0x9000, responseAPDU.getSW());
        Assert.assertNotNull(responseAPDU.getBytes());
    }

    @Test
    public void getCardStatus() throws Exception {
        final ResponseAPDU responseAPDU = SimpleAPDU.testCardGetStatus();
        Assert.assertNotNull(responseAPDU);
        Assert.assertEquals(0x9000, responseAPDU.getSW());
        Assert.assertNotNull(responseAPDU.getBytes());
    }

    @Test
    public void CardBip32ImportSeed() throws Exception {
        final ResponseAPDU responseAPDU = SimpleAPDU.testCardBip32ImportSeed();
        Assert.assertNotNull(responseAPDU);
        Assert.assertEquals(0x9000, responseAPDU.getSW());
        Assert.assertNotNull(responseAPDU.getBytes());
    }

    @Test
    public void CardBip32GetAythentiKey() throws Exception {
        final ResponseAPDU responseAPDU = SimpleAPDU.testCardBip32GetAuthentiKey();
        Assert.assertNotNull(responseAPDU);
        Assert.assertEquals(0x9000, responseAPDU.getSW());
        Assert.assertNotNull(responseAPDU.getBytes());
    }

    @Test
    public void testEqualsKeys() throws Exception {
        ResponseAPDU resAuthKey = SimpleAPDU.testCardBip32ImportSeed();
        Assert.assertNotNull(resAuthKey);
        Assert.assertEquals(0x9000, resAuthKey.getSW());
        Assert.assertNotNull(resAuthKey.getBytes());
        CardDataParser.PubKeyData parser = new CardDataParser.PubKeyData();
        byte[] authentikey = parser.parseBip32ImportSeed(resAuthKey.getData()).authentikey;

        ResponseAPDU resRecKey = SimpleAPDU.testCardBip32GetAuthentiKey();
        Assert.assertNotNull(resRecKey);
        Assert.assertEquals(0x9000, resRecKey.getSW());
        Assert.assertNotNull(resRecKey.getBytes());
        CardDataParser.PubKeyData pubkeydata = new CardDataParser.PubKeyData();
        byte[] recoveredkey= pubkeydata.parseBip32GetAuthentikey(resRecKey.getData()).authentikey;

        assertArrayEquals(recoveredkey, authentikey);
    }

    @Test
    public void CardImportKey() throws Exception {
        final ResponseAPDU responseAPDU = SimpleAPDU.testCardImportKey();
        Assert.assertNotNull(responseAPDU);
        Assert.assertEquals(0x9000, responseAPDU.getSW());
        Assert.assertNotNull(responseAPDU.getBytes());
    }
}
