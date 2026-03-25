# Load .env file if it exists
ifneq (,$(wildcard .env))
    include .env
    export
endif

.PHONY: build lint deploy test clean debug emulate avd-create

APK_DEBUG = app/build/outputs/apk/debug/app-debug.apk
PACKAGE   = com.barrettotte.fishtank
ACTIVITY  = .MainActivity
AVD_NAME  = FishtankTV

build:
	./gradlew assembleDebug

lint:
	./gradlew lintDebug

deploy: build
ifdef FIRE_TV_IP
	adb connect $(FIRE_TV_IP):5555
endif
	adb install -r $(APK_DEBUG)
	adb shell am start -n $(PACKAGE)/$(ACTIVITY)

test:
	./gradlew test

clean:
	./gradlew clean

debug:
	adb logcat -s "Fishtank"

emulate: build
	adb start-server
	nohup emulator @$(AVD_NAME) -gpu host -no-snapshot > /dev/null 2>&1 &
	adb wait-for-device
	adb shell 'while [[ "$$(getprop sys.boot_completed)" != "1" ]]; do sleep 1; done'
	adb install -r $(APK_DEBUG)
	adb shell am start -n $(PACKAGE)/$(ACTIVITY)

avd-create:
	avdmanager create avd -n $(AVD_NAME) \
		-k "system-images;android-34;android-tv;x86" \
		-d "tv_1080p"
