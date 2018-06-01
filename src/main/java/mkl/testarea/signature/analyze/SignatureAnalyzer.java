package mkl.testarea.signature.analyze;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.cms.ContentInfo;
import org.bouncycastle.asn1.ocsp.OCSPResponse;
import org.bouncycastle.asn1.ocsp.ResponderID;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.ocsp.BasicOCSPResp;
import org.bouncycastle.cert.ocsp.OCSPException;
import org.bouncycastle.cert.ocsp.OCSPResp;
import org.bouncycastle.cert.ocsp.SingleResp;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerId;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.operator.DigestCalculator;
import org.bouncycastle.operator.DigestCalculatorProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.bc.BcDigestCalculatorProvider;
import org.bouncycastle.tsp.TSPException;
import org.bouncycastle.tsp.TimeStampToken;
import org.bouncycastle.tsp.TimeStampTokenInfo;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Selector;
import org.bouncycastle.util.Store;

/**
 * This class is meant to eventually become a tool for analyzing signatures.
 * More and more tests shall be added to indicate the issues of the upcoming
 * test signatures.
 * 
 * @author mklink
 */
public class SignatureAnalyzer
{
    private DigestCalculatorProvider digCalcProvider = new BcDigestCalculatorProvider();
    /**
     * @throws IOException 
     * @throws TSPException 
     * @throws OperatorCreationException 
     * 
     */
    public SignatureAnalyzer(byte[] signatureData) throws CMSException, IOException, TSPException, OperatorCreationException
    {
        signedData = new CMSSignedData(signatureData);
        
        Store certificateStore = signedData.getCertificates();
        if (certificateStore == null || certificateStore.getMatches(selectAny).isEmpty())
            System.out.println("\nCertificates: none");
        else
        {
            System.out.println("\nCertificates:");
            for (X509CertificateHolder certificate : (Collection<X509CertificateHolder>) certificateStore.getMatches(selectAny))
            {
                System.out.printf("- Subject: %s\n  Issuer: %s\n  Serial: %s\n", certificate.getSubject(), certificate.getIssuer(), certificate.getSerialNumber());
            }
        }
        
        Store attributeCertificateStore = signedData.getAttributeCertificates();
        if (attributeCertificateStore == null || attributeCertificateStore.getMatches(selectAny).isEmpty())
            System.out.println("\nAttribute Certificates: none");
        else
        {
            System.out.println("\nAttribute Certificates: TODO!!!");
        }
        
        Store crls = signedData.getCRLs();
        if (crls == null || crls.getMatches(selectAny).isEmpty())
            System.out.println("\nCRLs: none");
        else
        {
            System.out.println("\nCRLs: TODO!!!");
        }

        for (SignerInformation signerInfo : (Collection<SignerInformation>)signedData.getSignerInfos().getSigners())
        {
            System.out.printf("\nSignerInfo: %s / %s\n", signerInfo.getSID().getIssuer(), signerInfo.getSID().getSerialNumber());

            Store certificates = signedData.getCertificates();
            Collection certs = certificates.getMatches(new SignerId(signerInfo.getSID().getIssuer(), signerInfo.getSID().getSerialNumber()));
            
            System.out.print("Certificate: ");
            
            if (certs.size() != 1)
            {
                System.out.printf("Could not identify, %s candidates\n", certs.size());
            }
            else
            {
                X509CertificateHolder cert = (X509CertificateHolder) certs.iterator().next();
                System.out.printf("%s\n", cert.getSubject());
            }

            Map<ASN1ObjectIdentifier, ?> attributes = signerInfo.getSignedAttributes().toHashtable();

            for (Map.Entry<ASN1ObjectIdentifier, ?> attributeEntry : attributes.entrySet())
            {
                System.out.printf("Signed attribute %s", attributeEntry.getKey());
                
                if (attributeEntry.getKey().equals(ADBE_REVOCATION_INFO_ARCHIVAL))
                {
                    System.out.println(" (Adobe Revocation Information Archival)");
                    Attribute attribute = (Attribute) attributeEntry.getValue();
                    
                    for (ASN1Encodable encodable : attribute.getAttrValues().toArray())
                    {
                        ASN1Sequence asn1Sequence = (ASN1Sequence) encodable;
                        for (ASN1Encodable taggedEncodable : asn1Sequence.toArray())
                        {
                            ASN1TaggedObject asn1TaggedObject = (ASN1TaggedObject) taggedEncodable; 
                            switch (asn1TaggedObject.getTagNo())
                            {
                            case 0:
                            {
                                ASN1Sequence crlSeq = (ASN1Sequence) asn1TaggedObject.getObject();
                                for (ASN1Encodable crlEncodable : crlSeq.toArray())
                                {
                                    System.out.println(" CRL " + crlEncodable.getClass());
                                }

                                break;
                            }
                            case 1:
                            {
                                ASN1Sequence ocspSeq = (ASN1Sequence) asn1TaggedObject.getObject();
                                for (ASN1Encodable ocspEncodable : ocspSeq.toArray())
                                {
                                    OCSPResponse ocspResponse = OCSPResponse.getInstance(ocspEncodable);
                                    OCSPResp ocspResp = new OCSPResp(ocspResponse);
                                    int status = ocspResp.getStatus();
                                    BasicOCSPResp basicOCSPResp;
                                    try
                                    {
                                        basicOCSPResp = (BasicOCSPResp) ocspResp.getResponseObject();
                                        System.out.printf(" OCSP Response status %s - %s - %s\n", status, basicOCSPResp.getProducedAt(), ((ResponderID)basicOCSPResp.getResponderId().toASN1Object()).getName());
                                        for (X509CertificateHolder certificate : basicOCSPResp.getCerts())
                                        {
                                            System.out.printf("  Cert w/ Subject: %s\n          Issuer: %s\n           Serial: %s\n", certificate.getSubject(), certificate.getIssuer(), certificate.getSerialNumber());
                                        }
                                        for (SingleResp singleResp : basicOCSPResp.getResponses())
                                        {
                                            System.out.printf("  Response %s for ", singleResp.getCertStatus());
                                            X509CertificateHolder issuer = null;
                                            for (X509CertificateHolder certificate : basicOCSPResp.getCerts())
                                            {
                                                if (singleResp.getCertID().matchesIssuer(certificate, digCalcProvider))
                                                    issuer = certificate;
                                            }
                                            if (issuer == null)
                                            {
                                                System.out.printf("Serial %s and (hash algorithm %s) name %s / key %s\n", singleResp.getCertID().getSerialNumber(), singleResp.getCertID().getHashAlgOID(), toHex(singleResp.getCertID().getIssuerNameHash()), toHex(singleResp.getCertID().getIssuerKeyHash()));
                                            }
                                            else
                                            {
                                                System.out.printf("Issuer: %s Serial: %s\n", issuer.getSubject(), singleResp.getCertID().getSerialNumber());
                                            }
                                        }
                                    }
                                    catch (OCSPException e)
                                    {
                                        System.out.printf(" !! Failure parsing OCSP response object: %s\n", e.getMessage());
                                    }
                                }
                                break;
                            }
                            case 2:
                            {
                                ASN1Sequence otherSeq = (ASN1Sequence) asn1TaggedObject.getObject();
                                for (ASN1Encodable otherEncodable : otherSeq.toArray())
                                {
                                    System.out.println(" Other " + otherEncodable.getClass());
                                }
                                break;
                            }
                            default:
                                break;
                            }
                        }
                    }
                }
                else if (attributeEntry.getKey().equals(PKCSObjectIdentifiers.pkcs_9_at_contentType))
                {
                    System.out.println(" (PKCS 9 - Content Type)");
                }
                else if (attributeEntry.getKey().equals(PKCSObjectIdentifiers.pkcs_9_at_messageDigest))
                {
                    System.out.println(" (PKCS 9 - Message Digest)");
                }
                else if (attributeEntry.getKey().equals(PKCSObjectIdentifiers.id_aa_signingCertificateV2))
                {
                    System.out.println(" (Signing certificate v2)");
                }
                else
                {
                    System.out.println();
                }

                System.out.println();
            }            

            AttributeTable attributeTable = signerInfo.getUnsignedAttributes();
            if (attributeTable != null)
            {
                attributes = attributeTable.toHashtable();
                
                for (Map.Entry<ASN1ObjectIdentifier, ?> attributeEntry : attributes.entrySet())
                {
                    System.out.printf("Unsigned attribute %s", attributeEntry.getKey());
                    
                    if (attributeEntry.getKey().equals(/*SIGNATURE_TIME_STAMP_OID*/PKCSObjectIdentifiers.id_aa_signatureTimeStampToken))
                    {
                        System.out.println(" (Signature Time Stamp)");
                        Attribute attribute = (Attribute) attributeEntry.getValue();
                        
                        for (ASN1Encodable encodable : attribute.getAttrValues().toArray())
                        {
                            ContentInfo contentInfo = ContentInfo.getInstance(encodable);
                            TimeStampToken timeStampToken = new TimeStampToken(contentInfo);
                            TimeStampTokenInfo tstInfo = timeStampToken.getTimeStampInfo();

                            System.out.printf("Authority/SN %s / %s\n", tstInfo.getTsa(), tstInfo.getSerialNumber());
                            
                            DigestCalculator digCalc = digCalcProvider .get(tstInfo.getHashAlgorithm());

                            OutputStream dOut = digCalc.getOutputStream();

                            dOut.write(signerInfo.getSignature());
                            dOut.close();

                            byte[] expectedDigest = digCalc.getDigest();
                            boolean matches =  Arrays.constantTimeAreEqual(expectedDigest, tstInfo.getMessageImprintDigest());
                            
                            System.out.printf("Digest match? %s\n", matches);

                            System.out.printf("Signer %s / %s\n", timeStampToken.getSID().getIssuer(), timeStampToken.getSID().getSerialNumber());
                            
                            Store tstCertificates = timeStampToken.getCertificates();
                            Collection tstCerts = tstCertificates.getMatches(new SignerId(timeStampToken.getSID().getIssuer(), timeStampToken.getSID().getSerialNumber()));
                            
                            System.out.print("Certificate: ");
                            
                            if (tstCerts.size() != 1)
                            {
                                System.out.printf("Could not identify, %s candidates\n", tstCerts.size());
                            }
                            else
                            {
                                X509CertificateHolder tstCert = (X509CertificateHolder) tstCerts.iterator().next();
                                System.out.printf("%s\n", tstCert.getSubject());
                                
                                int version = tstCert.toASN1Structure().getVersionNumber();
                                System.out.printf("Version: %s\n", version);
                                if (version != 3)
                                    System.out.println("Error: Certificate must be version 3 to have an ExtendedKeyUsage extension.");
                                
                                Extension ext = tstCert.getExtension(Extension.extendedKeyUsage);
                                if (ext == null)
                                    System.out.println("Error: Certificate must have an ExtendedKeyUsage extension.");
                                else
                                {
                                    if (!ext.isCritical())
                                    {
                                        System.out.println("Error: Certificate must have an ExtendedKeyUsage extension marked as critical.");
                                    }
                                    
                                    ExtendedKeyUsage    extKey = ExtendedKeyUsage.getInstance(ext.getParsedValue());
                                    if (!extKey.hasKeyPurposeId(KeyPurposeId.id_kp_timeStamping) || extKey.size() != 1)
                                    {
                                        System.out.println("Error: ExtendedKeyUsage not solely time stamping.");
                                    }                             
                                }
                            }
                        }
                    }
                    else
                        System.out.println();
                }
            }
        } 
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

    final Selector selectAny = new Selector()
    {
        @Override
        public boolean match(Object obj)
        {
            return true;
        }

        @Override
        public Object clone()
        {
            return this;
        }
    };

    static final ASN1ObjectIdentifier adobe = new ASN1ObjectIdentifier("1.2.840.113583");
    static final ASN1ObjectIdentifier acrobat = adobe.branch("1");
    static final ASN1ObjectIdentifier security = acrobat.branch("1");
    static final ASN1ObjectIdentifier ADBE_REVOCATION_INFO_ARCHIVAL = security.branch("8");
    
    final CMSSignedData signedData;
    final static ASN1ObjectIdentifier SIGNATURE_TIME_STAMP_OID = new ASN1ObjectIdentifier("1.2.840.113549.1.9.16.2.14");
}
