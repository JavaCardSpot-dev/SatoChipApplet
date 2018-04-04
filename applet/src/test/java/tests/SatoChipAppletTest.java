package tests;

public class SatoChipAppletTest {

    private static byte pin_tries_0 = 0x10;
    private static byte ublk_tries_0 = 0x10;
    //        private static byte[] pin_0 = {0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38};
    private static byte[] pin_0 = {0x4D, 0x75, 0x73, 0x63, 0x6C, 0x65, 0x30, 0x30}; //same as default
    private static byte[] ublk_0 = {0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38};
    private static byte pin_tries_1 = 0x10;
    private static byte ublk_tries_1 = 0x10;
    private static byte[] pin_1 = {0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38};
    private static byte[] ublk_1 = {0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38};
    private static short memsize = 0x1000;
    private static short memsize2 = 0x1000;
    private static byte create_object_ACL = 0x01;
    private static byte create_key_ACL = 0x01;
    private static byte create_pin_ACL = 0x01;
    private static short option_flags = 0;
    private static byte[] hmacsha160_key = null;
    private static long amount_limit = 0;


    public static byte[] createSetupData() throws Exception {

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
}
