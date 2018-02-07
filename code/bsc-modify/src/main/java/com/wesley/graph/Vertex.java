package com.wesley.graph;

import java.util.ArrayList;
import java.util.List;

import com.bunq.util.Property;
import com.bunq.util.StaticText;

/*
 * RETRIEVED FROM: http://www.vogella.com/tutorials/JavaAlgorithmsDijkstra/article.html
 */

public class Vertex {

	/*
	 * New attributes should be ensured in the copy
	 */
	final private String id;
	final private String name;

	private String activity;
	private List<String> screenText;
	private boolean error;
	private boolean isLogin;

	
	public String visualization(){
		return String.format(" { \"id\":%s, \"name\" : \"%s\" , \"activity\":\"%s\", \"screenText\":\"%s\", \"error\":\"%s\" , \"isLogin\":\"%s\" }",id,name,nicifyActivity(),screenText.toString(),error,isLogin);
	}
	
	public String nicifyActivity(){
		Property p = Property.getInstance();
		return this.activity.replace(p.get("appPackage"),"");
	}
	
	public Vertex(String id, String name) {
		this.id = id;
		this.name = name;
		this.screenText = new ArrayList<String>();
		
		this.error = false; //default
		this.isLogin = false;
	}

	public Vertex copy() {
		Vertex v = new Vertex(this.id, this.name);
		v.setActivity(new String(this.activity));
		v.addScreenText(this.screenText);
		return v;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setActivity(String _activity) {
		this.activity = _activity;
	}

	public String getActivity() {
		return this.activity;
	}

	public void addScreenText(List<String> _ScreenText) {
		this.screenText.addAll(_ScreenText);
		/*
		 * Processes an array of strings which represent screen text element. We
		 * want to know if it contains a 1. login 2. error
		 */
		this.determineIfLoginState();
		this.determineIfErrorState();
	}

	public void determineIfLoginState() {
		for (String item : this.screenText) {
			for (String term : StaticText.loginText) {

				if (item.toLowerCase().contains(term)) {
					this.isLogin = true;
					return;
				}
			}
		}
	}

	public void determineIfErrorState() {
		for (String item : this.screenText) {
			for (String term : StaticText.errorText) {
				if (item.toLowerCase().contains(term)) {
					this.error = true;
					return;
				}
			}
		}
	}

	public List<String> getScreenText() {
		return this.screenText;
	}

	public boolean hasError() {
		return this.error;
	}

	public boolean isLogin() {
		return this.isLogin;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Vertex other = (Vertex) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return name;
	}
	
	public String PrettyString() {
		return name + " "+ id + " " + screenText + " " + error + isLogin;
	}

}