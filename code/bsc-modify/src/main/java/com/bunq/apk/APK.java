package com.bunq.apk;

import java.util.ArrayList;

import com.bunq.util.Property;

public class APK {
	public String APKname;
	public String launchableActivity;
	public ArrayList<String> activities;
	public ArrayList<Integer> callables;
	private Property properties ;

	
	
	public APK(String name, String launchableActivity, ArrayList<String> activities){
		this.APKname = name;
		this.launchableActivity = launchableActivity;
		this.activities = activities;
		this.properties = Property.getInstance();
	}

	
	public void setCallables(ArrayList<Integer> in){
		this.callables = in;
	}
	
	public ArrayList<String> getCallableActivities(){
		ArrayList<String> callableActivities = new ArrayList<String>();
		if(this.callables == null){
			System.out.println("Callables not set yet.");
			return callableActivities;
		}else{
			for(Integer index : this.callables){
				callableActivities.add(activities.get(index));
			}
			return callableActivities;
		}
	}
	
	public ArrayList<String> getCallableActivitiesMinusStart(){
		ArrayList<String> calls = getCallableActivities();
		calls.remove(this.launchableActivity);
		return calls;
	}
	
	public String toString(){
		String res = "[APK: " + this.APKname + ", launchable-activity: " + this.launchableActivity + "\n";
		res += "Activities:\n";
		int indexCounter =0;
		for(String activity : activities){
			res += "\t" + activity  ;
			if(callables.contains(new Integer(indexCounter)))
				res += "\t\t => Callable";
			res += "\n";
			indexCounter++;
		}
		res += "]";
		return res;
	}
}
