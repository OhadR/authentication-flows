package com.ohadr.authentication.utils;

public class XSSValidator
{
	private static char[] blackChars = {'<', '>', ';', '(', ')'};
	
	public static boolean containsBlackChars(String input)
	{
		for(char ch : blackChars)
		{
			if(input.indexOf(ch) != -1)
			{
				return true;
			}
		}
		return false;
	}

}
