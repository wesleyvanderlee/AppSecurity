package com.bunq.learner;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;

import com.bunq.learner.reset.SulReset;
import com.bunq.teacher.FsmTeacher;
import com.bunq.util.Property;

import de.learnlib.api.SUL;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
/**
 * This class is the link between the learner and the teacher part. The learner
 * part does only 'see' and use this class. The SulAdapter sends the input it
 * gets from the FsmLearner class to the FsmTeacher class, and returns the
 * output it gets from the FsmTeacher to the FsmLearner.
 * 
 * @author tom
 *
 */
public class SulAdapter implements SUL<String, String> {

	private static final String FILENAME = "/home/wesley/bs/fsm-learner/loginout.txt";
	BufferedWriter bw = null;
	FileWriter fw = null;
	File file = null;
	private int countt;

	// system under learning
	private FsmTeacher fsmTeacher;
	private Property properties;

	// indicates if the application has to do anything to get back to the
	// 'overview' screen
	private boolean softReset = false;
	// indicates if the application has to re-login via the main activity
	private boolean semiSoftReset = false;
	// indicates if the database needs to be reset and the app has to be
	// restarted
	private boolean hardReset = true;

	private boolean fastForward;

	private SulReset sulReset;

	private ArrayList<String> softResetList;
	private ArrayList<String> semiSoftResetList;
	private ArrayList<String> hardResetList;

	private Query currentQuery;
	private QueryCache cache;
	private long lastKeepAlive;
	
	int noncachecount;
	int cachecount;

	boolean supercachequery = false;
	
	boolean currentCacheQuery = false;
	HashMap<Integer, Integer> actionLengthStatistics;
	
	int ccc = 0;
	int frie = 0;
	public HashMap<Integer,Integer> hmcache;
	/**
	 * Constructor. Initializes all attributes. Starts the fsmTeacher.
	 */
	public SulAdapter() {
		hmcache = new HashMap<Integer,Integer>();
		cache = new QueryCache();
		fsmTeacher = new FsmTeacher();
		fsmTeacher.start();
		properties = Property.getInstance();
		hardResetList = properties.getList("hardResetWords");
		semiSoftResetList = properties.getList("semiSoftResetWords");
		softResetList = properties.getList("softResetWords");
		lastKeepAlive = new java.util.Date().getTime();

		// normally, one would add an own implementation of SULReset here:
		sulReset = null;

		countt = 0;
		noncachecount =0;
		cachecount =0;
		actionLengthStatistics = new HashMap<Integer, Integer>();
	}
	
	
	/*
	 * Constructor minus appium
	 */
	public void conflictReset(){
		cache = new QueryCache();
//		fsmTeacher = new FsmTeacher();
		fsmTeacher.start();
		properties = Property.getInstance();
		hardResetList = properties.getList("hardResetWords");
		semiSoftResetList = properties.getList("semiSoftResetWords");
		softResetList = properties.getList("softResetWords");
		lastKeepAlive = new java.util.Date().getTime();

		// normally, one would add an own implementation of SULReset here:
		sulReset = null;


		countt = 0;
		noncachecount =0;
		cachecount =0;
	}
	
	
	public QueryCache getQueryCache(){
		if(this.cache == null){
			System.out.println("Problemos problemos");
		}
		
		return this.cache;
	}

	
	public String nicefy(String in){
		String _action = in.split("%")[0];
		String split = properties.get("appPackage");
//		String split = "android";
		try{
			String[] ret = in.split(split + ":id");
			return _action+ " : " + ret[ret.length -1];
		}catch(Exception e){
			System.out.println("Symsalabim");
			return in;
			
		}
	}
	
	private void addToHashStatic(int size){
		if(actionLengthStatistics.get(size)!= null){
			actionLengthStatistics.put(size, actionLengthStatistics.get(size) + 1);
		}else{
			actionLengthStatistics.put(size, 1);
		}
	}
	
	public HashMap<Integer,Integer> getActionLengthStatistics(){
		return this.actionLengthStatistics;
	}
	
	
	/**
	 * Execute the given input on the SUL. After that, it updates the reset
	 * fields and then returns the output it got from the FsmTeacher.
	 * 
	 * @param in
	 *            the inputword that has to be send to the SUL.
	 * @return the output from the SUL
	 */
	public void addToMap(){
		Integer hc = currentQuery.hashCode();
		if (!hmcache.containsKey(hc)) {
			hmcache.put(hc, 0);
		}else{
			Integer val = hmcache.get(hc) + (currentQuery.getWords().size());
			hmcache.put(hc, val);
		}
	}
	
	public int getSum(){
		int r = 0;
		for(Integer key : hmcache.keySet()){
			Integer v = hmcache.get(key);
			if(v > 0){
				r += v;
			}
		}
		return r;
	}
	public int getNum(){
		int r = 0;
		for(Integer key : hmcache.keySet()){
			Integer v = hmcache.get(key);
			if(v > 0){
				r++;
			}
		}
		return r;
	}
	
	public String step(String in) {

		if("".equals(in))
			return properties.get("failureMsg");
		
		String output = "";
		currentQuery = currentQuery.addWord(in);
		
		try {
			output = cacheStep();
			cachecount++;

		} catch (Exception e) {
			output = nonCacheStep();
			noncachecount++;
			if (output == null || output.contains("null") ){
				output = properties.get("failureMsg");
			}
			currentQuery.addResult(output);
			cache.addQuery(currentQuery);
		}
		updateFastForward(output);
		System.out.println(cachecount+noncachecount + ": ["+cachecount +", " + noncachecount + "]\t" + output + "\t"+ nicefy(in));
		return output;
	}

	public int getCounts(){
		return cachecount+noncachecount;
	}
	
	private String cacheStep() throws QueryCacheMissException {
		String output = cache.getResultForQuery(currentQuery);
		currentQuery.addResult(output);
		addToHashStatic(currentQuery.size());
		return output;
	}

	private String nonCacheStep() {
		handleReset();
		String output = "";
		if (fastForward) {
			return properties.get("failureMsg");
		} else {
			// now make up for all the possible actions that should be done
			// to get in the state for executing the word that did not have a
			// query in the cache:
			addToHashStatic(currentQuery.size());

			for (String word : currentQuery.getWords()) {
				try{
					Thread.sleep(1000);
				}catch(Exception e){
					
				}
				// System.out.println("Word: " + word);
				output = fsmTeacher.handleAction(word);
				if (output.equals(properties.get("failureMsg"))) {
					return output;
				}
				// check if a reset is needed:
				if (output.equals(properties.get("successMsg"))) {
					updateReset(word);
					lastKeepAlive = new java.util.Date().getTime();
				}
				


			}
		}
		return output;
	}

	/**
	 * Actions to execute after each input has been sent. Even though it is
	 * currently empty, it can't be removed due to the SUL interface
	 */
	public void post() {
//		if(!supercachequery){
//			handlePostReset();
//		}
//		supercachequery = false;
	}

	/**
	 * Contains actions that are to be executed before each input has been sent.
	 * Resets the application and database when indicated by the reset flags.
	 * Also updates the fastForward
	 */
	public void pre() {
		//handlePreReset(); //replaced with handlePostReset 
		sendKeepAlive();
		currentQuery = new Query();
		fastForward = false;

		// System.out.println("New query starts now.");
	}

	private void handlePreReset() {
		boolean sendReset = hardReset || semiSoftReset || softReset;

		if (sendReset) {
			sendReset();
		}

		if (hardReset) {
			// in this clause, the custom SulReset implementation should be
			// invoked,
			// like: sulReset.reset(true);
			// System.out.println("Hard reset");
			fsmTeacher.handleAction("reset%finish_hard_reset");
		}
		// hardReset = false;
		semiSoftReset = false;
		softReset = false;
	}
	
	private void handleReset() {
		boolean sendReset = hardReset || semiSoftReset || softReset;

		if (sendReset) {
			sendReset();
		}

		if (hardReset) {
			// in this clause, the custom SulReset implementation should be
			// invoked,
			// like: sulReset.reset(true);
			// System.out.println("Hard reset");
			
			fsmTeacher.handleAction("reset%finish_hard_reset");
			try {
				Thread.sleep(2500);
//				System.out.println("aaaaa");
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// hardReset = false;
		semiSoftReset = false;
		softReset = false;

	}

	/**
	 * Sends the reset command to the learner which can be either a hard, soft
	 * or semisoft reset.
	 */
	private void sendReset() {
		if (hardReset) {
			fsmTeacher.handleAction("reset%hard_reset");
			// System.out.println("hard reset");
		} else if (semiSoftReset) {
			System.out.println("semi soft reset");
			fsmTeacher.handleAction("reset%semi_soft_reset");
		} else if (softReset) {
			fsmTeacher.handleAction("reset%soft_reset");
			System.out.println("soft reset");
		}
	}

	/**
	 * Makes sure the application does not log out due to inactivity. This
	 * functions sends a tap assignment. The coordinates are those of the middle
	 * of the bunq logo in the overview screen as specified in the config files
	 */
	private void sendKeepAlive() {
		long now = new java.util.Date().getTime() / 1000;
		if (now - lastKeepAlive > properties.getInt("keepAliveInterval")) {
			fsmTeacher.handleAction("tap%" + properties.get("keepAliveX") + "#" + properties.get("keepAliveY"));
			lastKeepAlive = now;
			System.out.println("Kept alive.");
		}
	}

	/**
	 * This function checks if the given word is in the hard-reset or soft-reset
	 * list; it then updates the xxxReset fields.
	 *
	 * @param word
	 *            the word we are going to check
	 */
	private void updateReset(String word) {
		if (hardResetList.contains(word)) {
			hardReset = true;
		} else if (semiSoftResetList.contains(word)) {
			semiSoftReset = true;
		} else if (softResetList.contains(word)) {
			softReset = true;
		}
	}

	/**
	 * This function checks if the fastForward boolean should be set to true.
	 * The point is that appium is very slow. Learnlib does not need to know
	 * what happens when a button has not been found in the rest of the query,
	 * because that means that path is 'dead'. Fastforward thus means: return
	 * immediately that something has not been found after one 1-NOTFOUND (or
	 * 2-ERROR) message in a query.
	 */
	private void updateFastForward(String out) {
		if (out.equals(properties.get("errorMsg")) || out.equals(properties.get("failureMsg"))) {
			fastForward = true;
		}
	}

	/**
	 * Stops the FsmTeacher.
	 */
	public void stop() {
		fsmTeacher.stop();
	}
	
	
	
}