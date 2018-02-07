
# State Machine Learning and Vulnerability Detection PoC.

State Machine Learning and Vulnerability Detection consist of two parts:
1. Learning state machines from Android applications;
    1. Creating the input alphabet
    2. Create a hypothesis model based on interactions with the Android application (SUT) on the physical phone
    3. Test for equivalence between the hypothesis model and SUT
2. Detecting vulnerabilities in the inferred models.
    1. Exploring application info (which Android activities are present, which activities are callable, etc.)
    2. Enriching the learned model
        1. Collecting information per state (corresponding activities, text per state)
        2. Collecting information per transition (network requests)

## System Requirements
The project depends on a number of programs to be pre-installed or preconfigured. This will be stated here. The following needs to be installed before the project is able to run.
**Learning DFAs**
1. Java (of course)
2. Maven CLI (for program execution
2. [Appium](http://appium.io/) (but install this with homebrew and not with npm)
3. ADB, basically make sure you have the most recent version of Android Studio installed. You need to be able to interact with a physical mobile phone, so as a first step, see if adb shell is working and you are able to execute commands on your phone.

**Postprocessing**
4. `aapt` needs to be installed. Also, make sure a copy of the apk file is stored in the `/apk` directory.
5. A virtual emulator, which I named `Nexus5X`. Other names might be applicable, but this must be changed in the config.
6. The Android app also needs to be installed on the virtual emulator.
7. The emulator needs to have a PIN security code, equal to `0000`.
8. The emulator needs to have installed a mitmproxy CA certificate (see https://mitmproxy.org/).

**Configurations**
After the requirements are met, the following settings must be adjusted:
1. `/src/main/config/androidConfig.properties` ==> Provide device and SUT information.
2. `/src/main/config/config.properties` ==> Specify learning algorithm and equivalence oracle.
3. Check if the hardcoded adb commands in `Main.runAlphabetScript()` work. This method collects UI elements from the physical device UI, but 'pulling' the screenshots differs per installation of Android Studio.

## Program execution
The program can be initiated with the following two commands:
#### Create alphabet
1. Start adb server (make sure device is connected)
2. `mvn exec:java -Dexec.mainClass="com.bunq.main.Main" -Dexec.args="alphabet:create"`
3. Follow instructions by the program.

#### Learning DFA
1. Start appium server with `appium`. For easy debugging and confirmation that it works, do not start appium as a background process.
2. launch project: `mvn exec:java -Dexec.mainClass="com.bunq.main.Main" -Dexec.args="learn"`

#### Vulnerability detection
1. Emulator will start, unlock itself (`emulator -avd Nexus5X -http-proxy 127.0.0.1:8889`)
2. Manually launch the appium server with another *backtrack port* for the emulator: `appium -p 4724 -bp 4725`

## Tips for starting
Make sure you can create a valid input alphabet. The .txt files in the /alphabet directory need to contain some Xpaths. To start out, start with a small input alphabet size (i.e. only 1 element). This leads to a trivial DFA, but you want to start small as everything consumes time. Also only focus on DFA learning in the first part. Vulnerability detection can be enabled and disabled by uncommenting `this.postLearning(learner);` in `Main.learn()`.

Vulnerability detection first needs to enrich the inferred model. Some enrichment activities are straightforward, such as collecting the presented text per state. Some enrichment activities are rather complex, such as collecting network requests per transition. Start easy (by enabling one vulnerability detection algorithm at the time) and slowly enable more complex detection algorithms.

## Repo directory explained
`alphabet` ⇒ folder to store the alphabets  
`apk` ⇒ folder to store the Android applications (SUT)  
`graphs` ⇒ contains learned graphs, original graphs and shortened graphs (for a better view)  
`hyp` ⇒ stores intermediate hypothesis machines. This is useful to observe hypothesis refinement  
`scripts` ⇒ contains helping scripts  
`src` ⇒ contains the project source code  

## Helpful system commands

* Find application package installed on phone (example duck): `adb shell 'pm list packages -f' | grep duck`
*> package:/data/app/com.duckduckgo.mobile.android-1/base.apk=com.duckduckgo.mobile.android*
* Extracting apk file from the device `adb pull /data/app/com.duckduckgo.mobile.android-1/base.apk`
*> 1017 KB/s (5310837 bytes in 5.099s)*
* Renaming the base apk `mv base.apk duckduckgo.apk`
* Retrieving launchable activities from apk: `aapt dump badging duckduckgo.apk  | grep launchable-activity`
*> launchable-activity: name='com.duckduckgo.mobile.android.activity.DuckDuckGo'  label='DuckDuckGo' icon=''*

## Further reading
[This master thesis report](../../docs/MSc._Thesis_Wesley_van_der_Lee.pdf) contains the most up to date information active learning that is combined with the learning tool. Thorough information about model enrichment and vulnerability detection is also presented in this work.
[This bachelor thesis report](http://resolver.tudelft.nl/uuid:37e87645-09a3-4ace-b9b2-dad897292ac9) discusses architectural concepts of the tool in a more excessive way. A summary of the tool is available [on this Github repository](https://github.com/TUDelft-CS4110/2016-sre-crew/blob/master/Report.md) (*second part*).


*Wesley, 2018*
