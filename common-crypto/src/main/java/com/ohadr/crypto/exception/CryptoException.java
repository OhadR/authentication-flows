package com.ohadr.crypto.exception;


public class CryptoException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public CryptoException()
	{
		super();
	}

	public CryptoException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public CryptoException(String message)
	{
		super(message);
	}

	public CryptoException(Throwable cause)
	{
		super(cause);
	}

}
