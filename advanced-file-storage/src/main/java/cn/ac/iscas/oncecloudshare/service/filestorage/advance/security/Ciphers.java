package cn.ac.iscas.oncecloudshare.service.filestorage.advance.security;

import javax.crypto.Cipher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Ciphers {

	@Autowired
	DefaultCipherProvider defaultCipherProvider;

	public Cipher defaultEncryptCipher() {
		return defaultCipher(Mode.ENCRYPT);
	}

	public Cipher defautlDecryptCipher() {
		return defaultCipher(Mode.ENCRYPT);
	}

	private Cipher defaultCipher(Mode mode) {
		return defaultCipherProvider.getCipher(mode);
	}

	public Cipher encryptCipher(Encryptions encryption) {
		return cipher(encryption, Mode.ENCRYPT);
	}

	public Cipher decryptCipher(Encryptions encryption) {
		return cipher(encryption, Mode.DECRYPT);
	}

	private Cipher cipher(Encryptions encryption, Mode mode) {
		if (encryption == null)
			return null;
		switch (encryption) {
		case DEFAULT:
			return defaultCipher(mode);
		case NULL:
			return null;
		}
		return null;
	}
}
