package gov.ca.dmv.jwt.auth;

import gov.ca.dmv.WSIException;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;

import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.Verification;

public class JWTUtility {
	private RSAPublicKey publicKey=null;
	private Algorithm algorithm=null;
	public JWTUtility(String file,String alg) throws WSIException {
		try {
			file="/var/mqsi/WSIGateway/publickey.pem";
			publicKey=(RSAPublicKey) getPublicKeyFromPEM(new FileReader(file));
			switch(alg) {
			case "RSA256": {
				algorithm = Algorithm.RSA256(publicKey, null);
				break;
			}
			default: {
				throw new WSIException("Algorithm for public key not supported");
			}
			}
		} catch (IOException e) {
			throw new WSIException(e.getMessage());
		}
	}
	public Verification getBaseVerification() {
	    return JWT.require(algorithm);
	}
	public Verification acceptExpresAt(Verification v,long l) {
		return v.acceptExpiresAt(l);
	}
	public Verification acceptIssuedAt(Verification v,long l) {
		return v.acceptIssuedAt(l);
	}
	public Verification acceptLeeway(Verification v,long l) {
		return v.acceptLeeway(l);
	}
	public Verification acceptNotBefore(Verification v,long l) {
		return v.acceptNotBefore(l);
	}
	public Verification withIssuer(Verification v,String ...l) {
		return v.withIssuer(l);
	}
	public Verification withSubject(Verification v,String l) {
		return v.withSubject(l);
	}
	public DecodedJWT validateToken(String token) {
		token = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6InNlcnZlciJ9.eyJhdWQiOiJmODNmMzJlOS0yMGZlLTQ4ZWEtOTU5Ny1jZjA2NTE5NWE5N2YiLCJncmFudF90eXBlIjoicmVzb3VyY2Vfb3duZXIiLCJncmFudF9pZCI6ImY0ZDA1NjViLWU3ZmQtNDU0NS04NWNmLTkyMWMxMmI4YzYzOCIsInVzZXJUeXBlIjoicmVndWxhciIsImdyb3VwX0lkcyI6WyJhZG1pbiIsImxvdmF0dG9yZ19ucGkxX2xvYzEiLCJsb3ZhdHRvcmdfbnBpMSIsImxvdmF0dG9yZyIsIjEyOjU2Nzpsb2MxIiwiMTI6NTY3IiwiMTIiXSwidW5pcXVlU2VjdXJpdHlOYW1lIjoiNjUyMDAwM0ZGUiIsInNjb3BlIjoiIiwianRpIjoiQ2VteFZtRGs5TTZESkpYUFNqUURUaFVjblpUV21tIiwiY2xpZW50X2lkIjoiZjgzZjMyZTktMjBmZS00OGVhLTk1OTctY2YwNjUxOTVhOTdmIiwicmVhbG1OYW1lIjoiY2xvdWRJZGVudGl0eVJlYWxtIiwiaXNzIjoiaHR0cHM6Ly9kZXYtbWN3ZWIudmVyaWZ5LmlibS5jb20vb2lkYy9lbmRwb2ludC9kZWZhdWx0IiwicHJlZmVycmVkX3VzZXJuYW1lIjoiYnJvb2subG92YXR0MkBpYm0uY29tIiwic3ViIjoiNjUyMDAwM0ZGUiIsImlhdCI6MTYwMDQzOTQ3NiwiZXhwIjoxNjAwNDQ2Njc2fQ.PMJcBXQKn_Bk1822qNjfRo4vjFNF7xVYuv0ZSJ9ZUb-LfHR0asrV3w7GCdg5vmd0MVxIU0tlR0xeTVD4xagnqKckcyFHKDX_s21OU7gwgrfQN2KU-yFV55MxnfKi4AmhCNnWV72haZvS4PMoif9bJxm_THgK1sdJmet6hx9JvgzOlU9NcOR_9YbgrUHkDES_KEjz8DTvvC4RgLiEJN_9Vgj6sGzgdhnLZ_j22r53yllMeUWdWb-SvzRWBxTXeqAp8T1epXcrgoRMPlnM70-nWX9KpfgphTtCzfyzHURYUpr4tbJqRhYMCm3msPqN9McT8Gd3vUMYpjgpqi9VBvKzUQ";
	    //JWTVerifier verifier = JWT.require(algorithm).build(); //Reusable verifier instance
	    DecodedJWT jwt = getBaseVerification().build().verify(token);
	    return jwt;
	}
	private static PublicKey getPublicKeyFromPEM(Reader reader) throws IOException {
	    PublicKey key;
	    PEMParser pem=null;
	    try {
	    	pem = new PEMParser(reader);
	    	JcaPEMKeyConverter jcaPEMKeyConverter = new JcaPEMKeyConverter();
	    	Object pemContent = pem.readObject();
	    	if (pemContent instanceof PEMKeyPair) {
	    		PEMKeyPair pemKeyPair = (PEMKeyPair) pemContent;
	    		KeyPair keyPair = jcaPEMKeyConverter.getKeyPair(pemKeyPair);
	    		key = keyPair.getPublic();
	    	} else if (pemContent instanceof SubjectPublicKeyInfo) {
	    		SubjectPublicKeyInfo keyInfo = (SubjectPublicKeyInfo) pemContent;
	    		key = jcaPEMKeyConverter.getPublicKey(keyInfo);
	    	} else if (pemContent instanceof X509CertificateHolder) {
	    		X509CertificateHolder cert = (X509CertificateHolder) pemContent;
	    		key = jcaPEMKeyConverter.getPublicKey(cert.getSubjectPublicKeyInfo());
	    	} else {
	    		throw new IllegalArgumentException("Unsupported public key format '" +
	    				pemContent.getClass().getSimpleName() + '"');
	    	}
	    } finally {
	    	pem.close();
	    }
	    return key;
	}
}
