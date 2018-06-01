// $Id$
package mkl.testarea.signature.misc;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x509.X509Extension;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.bc.BcX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.SignerId;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationVerifier;
import org.bouncycastle.cms.SignerInformationVerifierProvider;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.cms.jcajce.JcaSignerInfoVerifierBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.bc.BcDigestCalculatorProvider;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.util.Store;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class RsaSsaPss
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "signature");

    static String              signDN = null; 
    static KeyPair             signKP = null;
    static X509Certificate     signCert = null;

    static String              origDN = null;
    static KeyPair             origKP = null;
    static X509Certificate     origCert = null;

    //
    //
    // Initialization
    //
    //
    /**
     * The certificate creation has been copied from {@link CreateSignedMail#main(String[])}.
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
        Security.addProvider(new BouncyCastleProvider());

        //
        // set up our certs
        //
        KeyPairGenerator    kpg  = KeyPairGenerator.getInstance("RSA", "BC");

        kpg.initialize(1024, new SecureRandom());

        //
        // cert that issued the signing certificate
        //
        signDN = "O=Bouncy Castle, C=AU";
        signKP = kpg.generateKeyPair();
        signCert = makeCertificate(signKP, signDN, signKP, signDN);
        Files.write(new File(RESULT_FOLDER, "CA.crt").toPath(), signCert.getEncoded());

        //
        // cert we sign against
        //
        origDN = "CN=Eric H. Echidna, E=eric@bouncycastle.org, O=Bouncy Castle, C=AU";
        origKP = kpg.generateKeyPair();
        origCert = makeCertificate(origKP, origDN, signKP, signDN);
        Files.write(new File(RESULT_FOLDER, "User.crt").toPath(), origCert.getEncoded());
    }

    //
    // helper methods copied from {@link CreateSignedMail}.
    //
    /**
     * create a basic X509 certificate from the given keys
     */
    static X509Certificate makeCertificate(
        KeyPair subKP,
        String  subDN,
        KeyPair issKP,
        String  issDN)
        throws GeneralSecurityException, IOException, OperatorCreationException
    {
        PublicKey  subPub  = subKP.getPublic();
        PrivateKey issPriv = issKP.getPrivate();
        PublicKey  issPub  = issKP.getPublic();
        
        X509v3CertificateBuilder v3CertGen = new JcaX509v3CertificateBuilder(new X500Name(issDN), BigInteger.valueOf(serialNo++), new Date(System.currentTimeMillis()), new Date(System.currentTimeMillis() + (1000L * 60 * 60 * 24 * 100)), new X500Name(subDN), subPub);

        v3CertGen.addExtension(
            X509Extension.subjectKeyIdentifier,
            false,
            createSubjectKeyId(subPub));

        v3CertGen.addExtension(
            X509Extension.authorityKeyIdentifier,
            false,
            createAuthorityKeyId(issPub));

        return new JcaX509CertificateConverter().setProvider("BC").getCertificate(v3CertGen.build(new JcaContentSignerBuilder("MD5withRSA").setProvider("BC").build(issPriv)));
    }

    //
    // certificate serial number seed.
    //
    static int  serialNo = 1;

    static AuthorityKeyIdentifier createAuthorityKeyId(
        PublicKey pub) 
        throws IOException
    {
        SubjectPublicKeyInfo info = SubjectPublicKeyInfo.getInstance(pub.getEncoded());

        return new AuthorityKeyIdentifier(info);
    }

    static SubjectKeyIdentifier createSubjectKeyId(
        PublicKey pub) 
        throws IOException
    {
        SubjectPublicKeyInfo info = SubjectPublicKeyInfo.getInstance(pub.getEncoded());

        return new BcX509ExtensionUtils().createSubjectKeyIdentifier(info);
    }

    //
    //
    // test methods
    //
    //
    /**
     * For some tests I needed SHA256withRSAandMGF1 CMS signatures.
     */
    @Test
    public void testCreateSimpleSignatureContainer() throws CMSException, GeneralSecurityException, OperatorCreationException, IOException
    {
        byte[] message = "SHA256withRSAandMGF1".getBytes();
        CMSTypedData msg = new CMSProcessableByteArray(message);

        List<X509Certificate> certList = new ArrayList<X509Certificate>();
        certList.add(origCert);
        certList.add(signCert);
        Store certs = new JcaCertStore(certList);

        CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
        ContentSigner sha1Signer = new JcaContentSignerBuilder("SHA256withRSAandMGF1").setProvider("BC").build(signKP.getPrivate());

        gen.addSignerInfoGenerator(
                  new JcaSignerInfoGeneratorBuilder(
                       new JcaDigestCalculatorProviderBuilder().setProvider("BC").build())
                       .build(sha1Signer, signCert));

        gen.addCertificates(certs);

        CMSSignedData sigData = gen.generate(msg, false);
        
        
        Files.write(new File(RESULT_FOLDER, "simpleMessageSHA256withRSAandMGF1.bin").toPath(), message);
        Files.write(new File(RESULT_FOLDER, "simpleMessageSHA256withRSAandMGF1.p7s").toPath(), sigData.getEncoded());
        
        boolean verifies = sigData.verifySignatures(new SignerInformationVerifierProvider()
        {
            @Override
            public SignerInformationVerifier get(SignerId sid) throws OperatorCreationException
            {
                if (sid.getSerialNumber().equals(origCert.getSerialNumber()))
                {
                    System.out.println("SignerInformationVerifier requested for OrigCert");
                    return new JcaSignerInfoVerifierBuilder(new BcDigestCalculatorProvider()).build(origCert);
                }
                if (sid.getSerialNumber().equals(signCert.getSerialNumber()))
                {
                    System.out.println("SignerInformationVerifier requested for SignCert");
                    return new JcaSignerInfoVerifierBuilder(new BcDigestCalculatorProvider()).build(signCert);
                }
                System.out.println("SignerInformationVerifier requested for unknown " + sid);
                return null;
            }
        });
        
        System.out.println("Verifies? " + verifies);
    }

    /**
     * This specific doesn't verify in combination with its document, so
     * I wanted to look at its contents. As RSASSA-PSS does not allow to
     * read the original hash from the decrypted signature bytes, this
     * did not help at all.
     */
    @Test
    public void testDecryptSLMBC_PSS_Test1() throws IOException, CMSException, GeneralSecurityException
    {
        Cipher cipherNoPadding = Cipher.getInstance("RSA/ECB/NoPadding");
        KeyFactory rsaKeyFactory = KeyFactory.getInstance("RSA");

        try (   InputStream resource = getClass().getResourceAsStream("SLMBC-PSS-Test1.cms")    )
        {
            CMSSignedData cmsSignedData = new CMSSignedData(resource);
            for (SignerInformation signerInformation : (Iterable<SignerInformation>)cmsSignedData.getSignerInfos().getSigners())
            {
                Collection<X509CertificateHolder> x509CertificateHolders = cmsSignedData.getCertificates().getMatches(signerInformation.getSID());
                if (x509CertificateHolders.size() != 1)
                {
                    Assert.fail("Cannot uniquely determine signer certificate.");
                }
                X509CertificateHolder x509CertificateHolder = x509CertificateHolders.iterator().next();
                PublicKey publicKey = rsaKeyFactory.generatePublic(new X509EncodedKeySpec(x509CertificateHolder.getSubjectPublicKeyInfo().getEncoded()));
                cipherNoPadding.init(Cipher.DECRYPT_MODE, publicKey);
                byte[] bytes = cipherNoPadding.doFinal(signerInformation.getSignature());

                Files.write(new File(RESULT_FOLDER, "SLMBC-PSS-Test1-signature-decoded").toPath(), bytes);
            }
        }
    }
}
