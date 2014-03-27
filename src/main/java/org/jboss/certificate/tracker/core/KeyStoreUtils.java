
package org.jboss.certificate.tracker.core;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.KeyStoreSpi;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.crypto.Cipher;

import org.apache.commons.io.FileUtils;
import org.jboss.logging.Logger;



/**
 * This class provides methods for KeyStore(s) handling.
 * 
 * @author Filip Bogyai
 */
public class KeyStoreUtils {

	private final static Logger LOGGER = Logger.getLogger(KeyStoreUtils.class);
	
	/**
	 * Returns array of supported KeyStores
	 * 
	 * @return String array with supported KeyStore implementation names
	 */
	public static SortedSet<String> getKeyStores() {
		final Set<String> tmpKeyStores = java.security.Security.getAlgorithms("KeyStore");
		return new TreeSet<String>(tmpKeyStores);
	}

	/**
	 * Loads certificate names (aliases) from the given initialized keystore
	 * 
	 * @return array of certificate aliases
	 */
	public static String[] getCertAliases(KeyStore tmpKs) {
		if (tmpKs == null)
			return null;
		final List<String> tmpResult = new ArrayList<String>();
		try {
			final Enumeration<String> tmpAliases = tmpKs.aliases();
			while (tmpAliases.hasMoreElements()) {
				final String tmpAlias = tmpAliases.nextElement();
                // if (tmpKs.isCertificateEntry(tmpAlias)) {
					tmpResult.add(tmpAlias);
                // }
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return tmpResult.toArray(new String[tmpResult.size()]);
	}

	/**
	 * Loads certificate names (aliases) from the given keystore
	 * 
	 * @param aKsType
	 * @param aKsFile
	 * @param aKsPasswd
	 * @return array of certificate aliases
	 */
	public static String[] getCertAliases(String aKsType, String aKsFile, String aKsPasswd) {
		return getCertAliases(loadKeyStore(aKsType, aKsFile, aKsPasswd));
	}

	/**
	 * Opens given keystore.
	 * 
	 * @param aKsType
	 * @param aKsFile
	 * @param aKsPasswd
	 * @return
	 */
	public static KeyStore loadKeyStore(String aKsType, final String aKsFile, final String aKsPasswd) {
		char[] tmpPass = null;
		if (aKsPasswd != null) {
			tmpPass = aKsPasswd.toCharArray();
		}
		return loadKeyStore(aKsType, aKsFile, tmpPass);
	}

	/**
	 * Creates empty JKS keystore..
	 * 
	 * @return new JKS keystore
	 * @throws KeyStoreException
	 * @throws IOException
	 * @throws CertificateException
	 * @throws NoSuchAlgorithmException
	 */
	public static KeyStore createKeyStore() throws KeyStoreException, NoSuchAlgorithmException, CertificateException,
			IOException {
		final KeyStore newKeyStore = KeyStore.getInstance("JKS");
		newKeyStore.load(null, null);
		return newKeyStore;
	}

	/**
	 * Copies certificates from one keystore to another (both keystore has to be
	 * initialized.
	 * 
	 * @param fromKeyStore
	 * @param toKeyStore
	 * @return
	 */
	public static boolean copyCertificates(KeyStore fromKeyStore, KeyStore toKeyStore) {
		if (fromKeyStore == null || toKeyStore == null) {
			return false;
		}

		try {
			for (String alias : getCertAliases(fromKeyStore)) {
				toKeyStore.setCertificateEntry(alias, fromKeyStore.getCertificate(alias));
			}
			return true;
		} catch (KeyStoreException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Opens given keystore.
	 * 
	 * @param aKsType
	 * @param aKsFile
	 * @param aKsPasswd
	 * @return
	 */
	public static KeyStore loadKeyStore(String aKsType, final String aKsFile, final char[] aKsPasswd) {

        if (aKsType.isEmpty() && aKsFile.isEmpty()) {
			return loadCacertsKeyStore(null);
		}

        if (aKsType.isEmpty()) {
			aKsType = KeyStore.getDefaultType();
		}

		KeyStore tmpKs = null;
		InputStream tmpIS = null;
		try {
			tmpKs = KeyStore.getInstance(aKsType);
            if (!aKsFile.isEmpty()) {
				
				tmpIS = new FileInputStream(aKsFile);
			}
			tmpKs.load(tmpIS, aKsPasswd);
			fixAliases(tmpKs);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (tmpIS != null)
				try {
					tmpIS.close();
				} catch (Exception e) {
				}
		}
		return tmpKs;
	}

	/**
	 * Loads the default root certificates at
	 * &lt;java.home&gt;/lib/security/cacerts.
	 * 
	 * @param provider
	 *            the provider or <code>null</code> for the default provider
	 * @return a <CODE>KeyStore</CODE>
	 */
	public static KeyStore loadCacertsKeyStore(String provider) {
		File file = new File(System.getProperty("java.home"), "lib");
		file = new File(file, "security");
		file = new File(file, "cacerts");
		FileInputStream fin = null;
		try {
			fin = new FileInputStream(file);
			KeyStore k;
			if (provider == null)
				k = KeyStore.getInstance("JKS");
			else
				k = KeyStore.getInstance("JKS", provider);
			k.load(fin, null);
			return k;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				if (fin != null) {
					fin.close();
				}
			} catch (Exception ex) {
			}
		}
	}

	/**
	 * Loads a {@link X509Certificate} from the given path. Returns null if the
	 * certificate can't be loaded.
	 * 
	 * @param filePath
	 * @return
	 */
	public static X509Certificate loadCertificate(final String filePath) {
        if (filePath.isEmpty()) {
			LOGGER.debug("Empty file path");
			return null;
		}
		FileInputStream inStream = null;
		X509Certificate cert = null;
		try {
			final CertificateFactory certFac = CertificateFactory.getInstance("X.509"); // X.509
			inStream = FileUtils.openInputStream(new File(filePath));
			cert = (X509Certificate) certFac.generateCertificate(inStream);
		} catch (Exception e) {
			LOGGER.debug("Unable to load certificate", e);
		} finally {
            try {
                inStream.close();
            } catch (IOException ex) {
                // OK
            }
		}
		return cert;
	}

    /**
     * Loads a {@link X509Certificate} from binary representation. Returns null
     * if the certificate can't be loaded.
     * 
     * @param filePath
     * @return
     */
    public static X509Certificate loadBinaryCertificate(byte[] source) {

        ByteArrayInputStream binStream = null;
        X509Certificate cert = null;
        try {
            final CertificateFactory certFac = CertificateFactory.getInstance("X.509"); // X.509
            binStream = new ByteArrayInputStream(source);
            cert = (X509Certificate) certFac.generateCertificate(binStream);
        } catch (Exception e) {
            // LOGGER.debug("Unable to load certificate", e);
            System.out.println("chyba binarneho certifikatu");
        } finally {
            try {
                binStream.close();
            } catch (IOException ex) {
                // OK
            }
        }
        return cert;

    }

	/**
	 * Returns true if the given certificate can be used for encryption, false
	 * otherwise.
	 * 
	 * @param cert
	 * @return
	 */
	public static boolean isEncryptionSupported(final Certificate cert) {
		boolean result = false;
		if (cert != null) {
			try {
				Cipher.getInstance(cert.getPublicKey().getAlgorithm());
				result = true;
			} catch (Exception e) {
				LOGGER.debug("Not possible to encrypt with the certificate", e);
			}
		}
		return result;
	}

	public static KeyStore createTrustStore() throws KeyStoreException, NoSuchAlgorithmException, CertificateException,
			IOException {
		final KeyStore trustStore = createKeyStore();

		char SEP = File.separatorChar;
		final File dir = new File(System.getProperty("java.home") + SEP + "lib" + SEP + "security");
		final File file = new File(dir, "cacerts");
		if (file.canRead()) {
			final KeyStore ks = KeyStore.getInstance("JKS");
			final InputStream in = new FileInputStream(file);
			try {
				ks.load(in, null);
			} finally {
				in.close();
			}
			copyCertificates(ks, trustStore);
		}
		return trustStore;
	}

	/**
	 * For WINDOWS-MY keystore fixes problem with non-unique aliases
	 * 
	 * @param keyStore
	 */
	@SuppressWarnings("unchecked")
	private static void fixAliases(final KeyStore keyStore) {
		Field field;
		KeyStoreSpi keyStoreVeritable;
		final Set<String> tmpAliases = new HashSet<String>();
		try {
			field = keyStore.getClass().getDeclaredField("keyStoreSpi");
			field.setAccessible(true);
			keyStoreVeritable = (KeyStoreSpi) field.get(keyStore);

			if ("sun.security.mscapi.KeyStore$MY".equals(keyStoreVeritable.getClass().getName())) {
				Collection<Object> entries;
				String alias, hashCode;
				X509Certificate[] certificates;

				field = keyStoreVeritable.getClass().getEnclosingClass().getDeclaredField("entries");
				field.setAccessible(true);
				entries = (Collection<Object>) field.get(keyStoreVeritable);

				for (Object entry : entries) {
					field = entry.getClass().getDeclaredField("certChain");
					field.setAccessible(true);
					certificates = (X509Certificate[]) field.get(entry);

					hashCode = Integer.toString(certificates[0].hashCode());

					field = entry.getClass().getDeclaredField("alias");
					field.setAccessible(true);
					alias = (String) field.get(entry);
					String tmpAlias = alias;
					int i = 0;
					while (tmpAliases.contains(tmpAlias)) {
						i++;
						tmpAlias = alias + "-" + i;
					}
					tmpAliases.add(tmpAlias);
					if (!alias.equals(hashCode)) {
						field.set(entry, tmpAlias);
					}
				}
			}
		} catch (Exception exception) {
			// nothing to do here
		}
	}

}
