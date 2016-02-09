package edu.gatech.inatural.robotcontrol;

public class CheckMacAddress {
	
	//Check to make sure the string is a mac address
	public static boolean checkMac(String address){
		String delims = "[:]+";					//using colon as the delimiter for the string parse
		String[] parts = address.split(delims);	//parses string into a String array
		
		//if the string has too many or too few parts then return false
		if(parts.length != 6){
			return false;
		} 
		
		//if any of the parts has more than 2 parts
		for(int i=0 ; i<6 ; i++){
			if (parts[i].length() != 2) {
				return false;
			}
		}
		 	
		//if any of the parts has a character not hexadecimal
		//matches to make sure that the string matches what's inside the parens
		//the string inside matches is a REGex to say hexadecimal
		for(int i=0 ; i<6 ; i++){
			if(!parts[i].matches("[0-9A-Fa-f]+")){
				return false;
			}
		}
		
		//if none of the previous if's returned false then return true
		return true;
	}
}
