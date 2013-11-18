package com.ohadr.butke.token;

import java.security.PrivateKey;
import java.security.PublicKey;

public interface KeystoreService
{
	public PrivateKey getPrivateKey();

	public PublicKey getPublicKey();
}
