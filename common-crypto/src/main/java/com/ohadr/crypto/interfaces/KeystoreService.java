package com.ohadr.crypto.interfaces;

import java.security.PrivateKey;
import java.security.PublicKey;

public interface KeystoreService
{
	public PrivateKey getPrivateKey();

	public PublicKey getPublicKey();
}
