
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
1. ADB, basically make sure you have the most recent version of Android Studio (including its SDK) installed. You need to be able to interact with a physical mobile phone or an emulator, so as a first step, see if adb shell is working and you are able to execute commands on your device.
2. Java (of course)
3. IDE for Java project (only if you intend to modify the bsc-modify project code)
4. Maven CLI (for program execution)
5. [Appium](http://appium.io/) (Install this with homebrew and not with npm. On Windows, simply download the executable)

**Postprocessing -- vulnerability detection**

4. `aapt` needs to be installed. Also, make sure a copy of the apk file is stored in the `/apk` directory.

5. A virtual emulator, which I named `Nexus5X`. Other names might be applicable, but this must be changed in the config.

6. The Android app also needs to be installed on the virtual emulator.

7. The emulator needs to have a PIN security code, equal to `0000`.

8. The emulator needs to have installed a mitmproxy CA certificate (see https://mitmproxy.org/).

**Configurations**
After the requirements are met, the following settings must be adjusted:
1. `/src/main/config/androidConfig.properties` ==> Provide device and SUT information.
    
    Example: 
   ```
    deviceName=Nexus5x
    platformVersion=8.1.0

    emulatorName=Nexus5x
    emulatorPlatformVersion=8.1.0

    apkname=insecurebankv2.apk
    appPackage=com.android.insecurebankv2
    appActivity=com.android.insecurebankv2.LoginActivity
    ```
    
  Here, deviceName is the physical device or the emulator used to test the apk; emulatorName is the name of the AVD used as the learner; apkname is the name of the apk to test; appPackage is the package name of the apk; appActivity is the main launchable activity. In order to find the apk activity related information, use aapt (see Helpful system commands)
    
2. `/src/main/config/config.properties` ==> Specify learning algorithm and equivalence oracle.
    
    Example:
    ``` 
        learningAlgorithm=lstar

        EquivMethod=RandomWalk

        alphabetFile=insec
    ```
        
  alphabetFile is the file inside `~/alphabet/` where the alphabets were stored after running the Create alphabet section commands. (refers to `/alphabet_file_name/` see later)
    
3. [optional] Check if the hardcoded adb commands in `Main.runAlphabetScript()` work. This method collects UI elements from the physical device UI, but 'pulling' the screenshots differs per installation of Android Studio. 

Specifically, `adb shell uiautomator dump` dumps an screenshot of the activity in an xml file and `adb pull /sdcard/window_dump.xml alphabet/window_dumps/alphabet_file_name/file_name.xml` 'pulls' that xml file from the emulator's sdcard or local storage to `~/alphabet/window_dumps/alphabet_file_name/file_name.xml`. (In case you do not see the alphabet file (.txt) created, make sure the OS is allowed to make `/windows_dump/` and `/alphabet_file_name/ ` folders)

## Program execution
The program can be initiated with the following two commands:
#### Create alphabet
1. Start adb server (make sure device is connected)
2. Go to the root directory of the project (same level where pom.xml is placed)
3. Execute `mvn exec:java -Dexec.mainClass="com.bunq.main.Main" -Dexec.args="alphabet:create"` on command line. (Do not get deceived by the bunq package name, it is the package name where Main.java is located)
3. Follow instructions by the program. 

    a) Enter an alphabet name (see above: `/alphabet_file_name/`), 
    
    b) then open the activity on the device of which you want to build the state machine, 
    
    c) enter the name of the activity (see above: `file_name.xml`). 
    
    You can follow b) and c) multiple times for several activities. Type `stop` once done and hit enter.

#### Learning DFA
1. Start appium server with `appium`. For easy debugging and confirmation that it works, do not start appium as a background process.
2. Launch project: `mvn exec:java -Dexec.mainClass="com.bunq.main.Main" -Dexec.args="learn"`
3. You will see the app automatically starting up on your device and perform several actions. It is `appium` being the virtual and automatic entity performing actions on the app.
4. Once the learning is done, a dot file will be created in the project root directory `~/graphs/` folder. Again, if you are on Windows, make sure you create this folder in advance, or give the project permissions to create folders if they don't exist.
5. You can view the graph using the command `dot -Tps [dot file] -o [output file name].ps` on Linux, or any alternatives for other OSes.

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
