adb -s emulator-5554 shell input swipe 500 1000 500 50
sleep 4
adb -s emulator-5554 shell input text 0000
sleep 4
adb -s emulator-5554 shell input keyevent 66

