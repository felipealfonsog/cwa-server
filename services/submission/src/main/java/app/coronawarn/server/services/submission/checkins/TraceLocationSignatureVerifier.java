package app.coronawarn.server.services.submission.checkins;

import app.coronawarn.server.common.protocols.internal.pt.SignedTraceLocation;
import app.coronawarn.server.services.submission.config.CryptoProvider;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TraceLocationSignatureVerifier {

  private static final Logger logger = LoggerFactory.getLogger(EventCheckinDataFilter.class);

  private final CryptoProvider cryptoProvider;

  public TraceLocationSignatureVerifier(CryptoProvider cryptoProvider) {
    this.cryptoProvider = cryptoProvider;
  }

  /**
   * Verify the signature contained in the given event data structure by using the service
   * configured public key.
   */
  public boolean verify(SignedTraceLocation signedEvent) {
    byte[] signatureBytes = signedEvent.getSignature().toByteArray();
    byte[] contentBytes = signedEvent.getLocation().toByteArray();
    try {
      Signature signatureAlgorithm = Signature.getInstance(cryptoProvider.getSignatureAlgorithm());
      signatureAlgorithm.initVerify(cryptoProvider.getCertificate());
      signatureAlgorithm.update(contentBytes);
      return signatureAlgorithm.verify(signatureBytes);
    } catch (InvalidKeyException | NoSuchAlgorithmException e) {
      logger
          .error("Can not initialize Checkin trace location signature verification due to invalid "
              + "public key or algorithm. Please check service configuration.");
      return false;
    } catch (SignatureException e) {
      logger.warn("Could not verify signature of checkin trace location");
      return false;
    }
  }
}