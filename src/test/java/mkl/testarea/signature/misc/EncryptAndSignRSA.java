package mkl.testarea.signature.misc;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

import javax.crypto.Cipher;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class EncryptAndSignRSA
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "signature");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/34728665/java-equivalent-of-net-rsacryptoserviceprovider-signdata">
     * Java equivalent of .NET RSACryptoServiceProvider.SignData
     * </a>
     * <p>
     * It looks like transfer is easy but due to the padding effects, one cannot tell.
     * </p>
     */
    @Test
    public void encryptAndSignLikeDexter() throws GeneralSecurityException, IOException
    {
        BigInteger encModulus =         new BigInteger("B6-D6-7D-CA-DB-5F-F1-80-F1-4F-F5-26-A3-D7-1F-EA-12-AF-6C-22-15-50-4C-16-C6-DD-C1-55-50-AB-BF-58-E5-92-E1-2E-E7-D6-F6-23-83-0F-F3-33-26-4B-E9-5C-6B-72-E3-AA-6F-DE-54-82-17-FC-16-BF-34-AA-CF-DB-A4-BB-74-0C-D3-C1-FB-98-3F-13-D1-6B-7C-59-3A-4A-91-A7-76-2B-3C-8F-CA-97-AE-85-36-FB-6E-AC-57-98-5E-C0-38-BF-2D-29-90-DE-F4-D3-90-BF-1B-58-C7-BA-A0-9F-E0-32-EF-E6-E3-82-CA-A6-0E-09-A7-1C-B7-1D".replace("-", ""), 16);
        BigInteger encPrivateExponent = new BigInteger("69-B2-E0-2F-E8-D8-B3-65-E6-9B-61-CE-FF-C2-BA-D9-78-09-DF-CA-68-65-EE-46-F0-9A-7C-4D-15-00-E4-F2-C0-6D-44-2E-F8-AA-65-CB-2B-D6-89-B3-15-3D-A7-5F-DD-62-22-C9-82-38-96-C4-4A-97-5A-93-19-20-72-5E-BD-E1-F9-53-88-A8-43-D4-9B-BB-A4-E5-86-81-29-9C-C8-52-E3-54-35-E6-1B-8F-D6-2A-51-D2-B2-99-B3-A6-29-AA-DE-D8-DE-70-82-6E-5E-A1-41-A6-CD-96-71-44-EF-7D-0D-64-1D-4D-68-25-A6-B7-3B-4A-2D-AB-AE-85".replace("-", ""), 16);
        BigInteger encPublicExponent =  new BigInteger("010001", 16);

        BigInteger sigModulus =         new BigInteger("92-1E-29-0F-1D-A9-72-09-A2-0A-28-FA-4D-7F-A6-23-9F-BB-86-0B-87-F2-5C-8D-94-3B-9B-69-D1-E9-E1-AD-09-47-EF-8B-85-D8-30-4E-89-8E-51-A4-84-CD-0E-46-8A-E5-F6-B9-7D-ED-96-5B-65-24-7A-79-F9-FC-34-CB-B0-68-02-17-A1-4D-F3-97-BE-0D-BD-12-CC-C4-01-70-8D-DD-15-CE-B7-98-F9-48-A5-55-DB-31-3E-68-01-8D-B6-3B-33-FD-33-C9-D5-25-4A-6B-9A-2A-B5-05-3B-9A-3D-F6-AD-10-06-D4-17-02-67-0C-87-B3-9C-C8-74-87".replace("-", ""), 16);
        BigInteger sigPrivateExponent = new BigInteger("78-52-6B-BF-2D-CE-CD-C0-4E-F6-0C-DE-69-18-F7-67-98-6E-64-28-74-AF-48-35-B1-DE-0F-D1-68-F1-2E-4C-3E-3B-45-6F-E0-2C-B1-42-CB-15-2D-F7-CA-FF-CC-84-9C-76-57-E0-51-69-67-0A-25-D4-8F-22-88-8F-7D-AD-0B-28-0A-15-10-BE-BB-CD-2B-6F-3C-83-AD-73-EF-9C-E0-B0-60-0B-99-FF-81-00-0A-6F-29-49-C1-AA-1A-7B-6C-A5-0B-BB-48-D3-41-3C-80-6D-7E-05-70-70-F1-8B-63-A6-C1-AC-06-4F-D7-F2-D5-1D-33-30-47-39-C2-C1".replace("-", ""), 16);
        BigInteger sigPublicExponent =  new BigInteger("010001", 16);

        RSAPublicKeySpec encPubKS = new RSAPublicKeySpec(encModulus, encPublicExponent);
        RSAPrivateKeySpec encPrivKS = new RSAPrivateKeySpec(encModulus, encPrivateExponent);

        RSAPublicKeySpec sigPubKS = new RSAPublicKeySpec(sigModulus, sigPublicExponent);
        RSAPrivateKeySpec sigPrivKS = new RSAPrivateKeySpec(sigModulus, sigPrivateExponent);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey encPrivate = keyFactory.generatePrivate(encPrivKS);
        PublicKey encPublic = keyFactory.generatePublic(encPubKS);
        PrivateKey sigPrivate = keyFactory.generatePrivate(sigPrivKS);
        PublicKey sigPublic = keyFactory.generatePublic(sigPubKS);
        
        // initialize cipher to encrypt
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        Cipher cipherNoPadding = Cipher.getInstance("RSA/ECB/NoPadding");


        String data = "some string here";
        System.out.printf("Data : %s\n", data);
        byte[] dataBytes = data.getBytes("UTF8");
        System.out.printf("BData: %s\n", toHex(dataBytes));
        Files.write(new File(RESULT_FOLDER, "DexterDataClear.bin").toPath(), dataBytes);

        // **Step 1: encrypt data with public key**
        cipher.init(Cipher.ENCRYPT_MODE, encPublic);
        byte[] encBytes = cipher.doFinal(dataBytes);
        System.out.printf("Crypt: %s\n", toHex(encBytes));
        Files.write(new File(RESULT_FOLDER, "DexterDataEncrypted.bin").toPath(), encBytes);

//        encBytes = Files.readAllBytes(new File("c:/Temp/test-results/crypto/DexterDataEncrypted.bin").toPath());

        cipherNoPadding.init(Cipher.DECRYPT_MODE, encPrivate);
        byte[] decBytes = cipherNoPadding.doFinal(encBytes);
        System.out.printf("DeCry: %s\n", toHex(decBytes));
        Files.write(new File(RESULT_FOLDER, "DexterDataDecrypted.bin").toPath(), decBytes);

        // **Step 2: sign the encrypted data with private key**
        Signature sig = Signature.getInstance("SHA1WithRSA");
        sig.initSign(sigPrivate);
        sig.update(encBytes);
        byte[] signData = sig.sign();
        System.out.printf("Sign : %s\n", toHex(signData));
        Files.write(new File(RESULT_FOLDER, "DexterDataSigned.bin").toPath(), signData);
    }

    public static String toHex(byte[] bytes)
    {
        if (bytes == null)
            return "null";

        final char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ )
        {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
