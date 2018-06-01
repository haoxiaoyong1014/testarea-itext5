package mkl.testarea.signature.misc;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.spec.ECGenParameterSpec;
import java.util.Calendar;
import java.util.Date;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.x509.X509V1CertificateGenerator;
import org.junit.Test;

/**
 * @author mkl
 */
public class KeyAlgorithmName
{

    /**
     * <a href="http://stackoverflow.com/questions/33788331/why-does-key-getalgorithm-return-a-different-result-after-saving-and-reloading-t">
     * Why does Key.getAlgorithm return a different result after saving and reloading the KeyStore
     * </a>
     * <p>
     * Just as the OP claims, the first output is "ECDSA", the second "EC".
     * </p>
     */
    @Test
    public void testNameChangeAfterReload() throws GeneralSecurityException, IOException
    {
        String PROVIDER = "BC";
        String KEY_ALGORITHM = "ECDSA";
        String SIGNATURE_ALGORITHM = "SHA1WITHECDSA";
        String ALIAS = "TestAlias";
        char [] PASSWORD = "password".toCharArray();
        String KEYSTORE = "c:/temp/keystore.p12";

        Security.addProvider(new BouncyCastleProvider());

        // Generate the key
        Calendar calNow = Calendar.getInstance();
        Calendar calLater = Calendar.getInstance();
        calLater.set(Calendar.YEAR, calLater.get(Calendar.YEAR) + 25);
        Date startDate = new Date(calNow.getTimeInMillis());
        Date expiryDate = new Date(calLater.getTimeInMillis());

        ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp192r1");
        KeyPairGenerator g = KeyPairGenerator.getInstance(KEY_ALGORITHM, PROVIDER);
        g.initialize(ecSpec, new SecureRandom());
        KeyPair keyPair = g.generateKeyPair();

        X509V1CertificateGenerator certGen = new X509V1CertificateGenerator();
        X500Principal dnName = new X500Principal("CN=Test");
        certGen.setSerialNumber(new BigInteger(8, new SecureRandom()));
        certGen.setIssuerDN(dnName);
        certGen.setNotBefore(startDate);
        certGen.setNotAfter(expiryDate);
        certGen.setSubjectDN(dnName); // note: same as issuer
        certGen.setPublicKey(keyPair.getPublic());
        certGen.setSignatureAlgorithm(SIGNATURE_ALGORITHM);
        X509Certificate cert = certGen.generate(keyPair.getPrivate(), PROVIDER);

        // Save the keystore
        KeyStore exportStore = KeyStore.getInstance("PKCS12", PROVIDER);
        exportStore.load(null, null);
        exportStore.setKeyEntry(ALIAS, keyPair.getPrivate(), PASSWORD, new Certificate[] { cert });
        FileOutputStream out = new FileOutputStream(KEYSTORE);
        exportStore.store(out, PASSWORD);
        out.flush();
        out.close();

        // print the info from the keystore 
        Key keyA = exportStore.getKey(ALIAS, PASSWORD);
        System.out.println(keyA.getAlgorithm());

        // Reload the keystore
        FileInputStream in = new FileInputStream(KEYSTORE);
        exportStore.load(in, PASSWORD);
        in.close();

        // print the info from the reloaded keystore 
        Key keyB = exportStore.getKey(ALIAS, PASSWORD);
        System.out.println(keyB.getAlgorithm());
    }

}
