package cn.ac.iscas.oncecloudshare.service.filestorage.advance.security;

import javax.annotation.Resource;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Component;
import org.springside.modules.utils.Encodes;

import cn.ac.iscas.oncecloudshare.service.filestorage.advance.security.exceptions.EncryptionException;
import cn.ac.iscas.oncecloudshare.service.service.common.ConfigService;

@Component
public class DefaultCipherProvider implements CipherProvider {
	private static final String CIPHER_KEY = "filestorage.advance.entrance.k";
	private static final String CIPHER_IV = "filestorage.advance.entrance.v";
	private static final String DEFAULT_KEY_ALGORITHM = "DES";
	private static final String DEFAULT_CIPHER_ALGORITHM = "DES/CBC/PKCS5Padding";
	private static final String DEFAULT_PROVIDER = "SunJCE";

	@Resource(name="globalConfigService")
	private ConfigService<?> configService;

	@Override
	public Cipher getCipher(Mode mode) {
		return getCipher(mode, DEFAULT_KEY_ALGORITHM, DEFAULT_CIPHER_ALGORITHM,
				DEFAULT_PROVIDER);
	}

	public Cipher getCipher(Mode mode, String keyAlgorithm,
			String cipherAlgorithm, String provider) {
		try {
			byte[] initializationVector = Encodes.decodeBase64(configService
					.getConfig(CIPHER_IV, null));
			IvParameterSpec initializationVectorSpec = new IvParameterSpec(
					initializationVector);
			byte[] key = Encodes.decodeBase64(configService.getConfig(CIPHER_KEY,
					null));
			final SecretKeySpec skey = new SecretKeySpec(key, keyAlgorithm);
			final Cipher cipher = Cipher.getInstance(cipherAlgorithm, provider);
			if (mode == null) {
				throw new EncryptionException("invalid cipher mode; mode="
						+ mode);
			}
			switch (mode) {
			case ENCRYPT:
				cipher.init(Cipher.ENCRYPT_MODE, skey, initializationVectorSpec);
				break;
			case DECRYPT:
				cipher.init(Cipher.DECRYPT_MODE, skey, initializationVectorSpec);
				break;
			default:
				throw new EncryptionException("invalid cipher mode; mode="
						+ mode);
			}
			return cipher;
		} catch (Exception e) {
			throw new EncryptionException("get cipher error");
		}
	}
}
