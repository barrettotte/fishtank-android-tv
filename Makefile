# Load .env file if it exists
ifneq (,$(wildcard .env))
    include .env
    export
endif

.PHONY: build release lint deploy test clean debug debug_emu emulate avd_create

APK_DEBUG = app/build/outputs/apk/debug/app-debug.apk
VERSION   = $(shell grep 'app.versionName' gradle.properties | cut -d= -f2)
PACKAGE   = com.barrettotte.fishtank
ACTIVITY  = .MainActivity
AVD_NAME  = FishtankTV

build:
	./gradlew assembleDebug

release: clean
	./gradlew assembleRelease
	mkdir -p out
	cp app/build/outputs/apk/release/app-release.apk out/fishtank-android-tv-v$(VERSION).apk
	@echo "Release APK: out/fishtank-android-tv-v$(VERSION).apk"

lint:
	./gradlew lintDebug

deploy: build
ifdef FIRE_TV_IP
	adb connect $(FIRE_TV_IP):5555
	adb -s $(FIRE_TV_IP):5555 install -r $(APK_DEBUG)
	adb -s $(FIRE_TV_IP):5555 shell am start -n $(PACKAGE)/$(ACTIVITY)
else
	adb install -r $(APK_DEBUG)
	adb shell am start -n $(PACKAGE)/$(ACTIVITY)
endif

test:
	./gradlew test

clean:
	./gradlew clean

debug:
ifdef FIRE_TV_IP
	adb -s $(FIRE_TV_IP):5555 logcat | grep --line-buffered "Fishtank\."
else
	adb -s emulator-5554 logcat | grep --line-buffered "Fishtank\."
endif

debug_emu:
	adb -s emulator-5554 logcat | grep --line-buffered "Fishtank\."

emulate: build
	adb start-server
	nohup emulator @$(AVD_NAME) -gpu host -no-snapshot > /dev/null 2>&1 &
	adb -s emulator-5554 wait-for-device
	adb -s emulator-5554 shell 'while [[ "$$(getprop sys.boot_completed)" != "1" ]]; do sleep 1; done'
	adb -s emulator-5554 install -r $(APK_DEBUG)
	adb -s emulator-5554 shell am start -n $(PACKAGE)/$(ACTIVITY)

avd_create:
	avdmanager create avd -n $(AVD_NAME) \
		-k "system-images;android-34;android-tv;x86" \
		-d "tv_1080p"
