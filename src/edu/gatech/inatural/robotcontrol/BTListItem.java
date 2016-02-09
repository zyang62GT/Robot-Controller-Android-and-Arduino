package edu.gatech.inatural.robotcontrol;

public class BTListItem {
	
	//member variables
	public String data;
	public String cancelText;
	
	//Constructors
	public BTListItem(){
		super();
	}
	
	public BTListItem(String data, String cancelText){
		super();
		this.data = data;
		this.cancelText = cancelText;
	}
}
