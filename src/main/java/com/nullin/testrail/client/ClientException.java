package com.nullin.testrail.client;

/**
 * Exception raised something goes wrong communicating with TestRail server
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
