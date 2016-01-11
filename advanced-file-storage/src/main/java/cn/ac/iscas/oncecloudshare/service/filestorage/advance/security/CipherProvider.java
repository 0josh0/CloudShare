package cn.ac.iscas.oncecloudshare.service.filestorage.advance.security;

import javax.crypto.Cipher;

public interface CipherProvider {
	Cipher getCipher(Mode mode);
}
