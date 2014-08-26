package com.nullin.testrail.client;

/**
 * TODO: javadocs
 *
 * @author nullin
 */
public class ClientException extends Exception
{
	public ClientException(String message)
	{
		super(message);
	}

    public ClientException(String message, Exception ex)
   	{
   		super(message, ex);
   	}
}
