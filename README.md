# fishtank-android-tv

A basic app for watching Fishtank.live on Android TV (unofficial).

Port of the [Roku app](https://github.com/barrettotte/fishtank-roku-app) to Android TV / Fire TV.

## Install (Sideload)

Download the latest APK from [Releases](https://github.com/barrettotte/fishtank-android-tv/releases).

> **Note:** This app is not available on the Google Play Store or Amazon Appstore. It uses an unofficial, reverse-engineered API and is not affiliated with Fishtank.live. Sideloading is the only way to install it.

### Fire TV Stick

1. On the Fire TV Stick: **Settings > My Fire TV > Developer Options**
   - Enable **ADB Debugging**
   - Enable **Apps from Unknown Sources**
2. Note the IP from **Settings > My Fire TV > About > Network**
3. From a computer on the same network with [ADB](https://developer.android.com/tools/adb) installed:

```sh
adb connect <fire-tv-ip>:5555
adb install fishtank-android-tv-v0.1.0.apk
```

Or install the [Downloader](https://www.amazon.com/dp/B01N0BP507) app on the Fire TV and enter the APK download URL from GitHub releases directly.

> **Fire TV Stick 2nd Gen:** Developer Options is hidden by default. Go to **Settings > My Fire TV > About** and click on **Fire TV Stick** 7 times to unlock it.

### Fire TV (Cube, Smart TV, etc.)

1. **Settings > My Fire TV > Developer Options**
   - Enable **ADB Debugging**
   - Enable **Apps from Unknown Sources**
2. Install via ADB from a computer or use the Downloader app (same as above)

### Android TV (Google TV, Sony, Nvidia Shield, etc.)

1. **Settings > Device Preferences > About** and click **Build** 7 times to enable Developer Options
2. **Settings > Device Preferences > Developer Options**
   - Enable **USB Debugging** (or **Network Debugging** for wireless ADB)
   - Enable **Install from Unknown Sources**
3. Install via ADB:

```sh
adb connect <tv-ip>:5555
adb install fishtank-android-tv-v0.1.0.apk
```

Or use a file manager app to open the APK from a USB drive.

## Controls

### Camera Grid

| Button | Action |
|--------|--------|
| D-pad | Navigate cameras |
| OK | Open camera stream |
| Menu | Log out |

### Video Player

| Button | Action |
|--------|--------|
| OK | Show camera name, quality, and server info |
| Up | Open camera switcher |
| Down | Open stream settings (quality and server) |
| Back | Close overlay or return to grid |

## Developer Setup

### Prerequisites

- JDK 17
- Android SDK (cmdline-tools, platform-tools, build-tools 34.0.0, API 34-35)
- A Fire TV Stick on the same network as your dev machine. Tested with Fire TV Stick (2nd Gen) - Fire OS 5.2.9.5, Fire TV Home 6450033.1.

### Install

```sh
git clone https://github.com/barrettotte/fishtank-android-tv.git
cd fishtank-android-tv
cp .env.example .env
```

### Android SDK Setup (Arch Linux)

```sh
sudo pacman -S jdk17-openjdk

mkdir -p $HOME/Android/Sdk/cmdline-tools
cd /tmp
curl -sLO "https://dl.google.com/android/repository/commandlinetools-linux-14742923_latest.zip"
unzip commandlinetools-linux-14742923_latest.zip
mv cmdline-tools $HOME/Android/Sdk/cmdline-tools/latest
rm commandlinetools-linux-14742923_latest.zip

# Add to .zshrc or .bashrc
export ANDROID_HOME=$HOME/Android/Sdk
export ANDROID_AVD_HOME=$HOME/.config/.android/avd
export PATH="$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$PATH"

# Install SDK packages
sdkmanager "platform-tools" "platforms;android-34" "platforms;android-35" \
    "build-tools;34.0.0" "emulator" "system-images;android-34;android-tv;x86"
yes | sdkmanager --licenses
```

### Enable ADB on Fire TV Stick

1. Go to **Settings > My Fire TV > Developer Options**
2. Enable **ADB Debugging**
3. Enable **Apps from Unknown Sources**
4. Note the IP from **Settings > My Fire TV > About > Network**
5. Run `adb connect <ip>:5555` - accept the "Allow USB debugging?" dialog on the TV screen and check "Always allow from this computer"

> **Fire TV Stick 2nd Gen:** Developer Options is hidden by default. Go to **Settings > My Fire TV > About** and click on **Fire TV Stick** 7 times to unlock it.

6. Edit `.env` with your device IP:
   ```
   FIRE_TV_IP=192.168.1.100
   ```

### Build and Deploy

```sh
make deploy
```

This builds the debug APK and installs it onto the Fire TV Stick.

### Emulator

Uses the `android-tv` system image which provides the Android TV launcher with Leanback home screen and D-pad navigation.

```sh
# one-time setup
make avd_create

# build, launch emulator, install, and run
make emulate
```

> **If emulator keys aren't working:** The AVD may have `hw.keyboard=no` by default. Edit `$ANDROID_AVD_HOME/FishtankTV.avd/config.ini` and set `hw.keyboard=yes`, then also set `hw.keyboard = true` in `hardware-qemu.ini` in the same directory. Kill the emulator and relaunch.

### Debugging

```sh
# view app logs
make debug

# view emulator logs
make debug_emu
```

### Other Commands

```sh
make lint     # run Android lint
make test     # run unit tests
make clean    # delete build output
```

## References

- [Compose for TV](https://developer.android.com/training/tv/playback/compose)
- [Leanback Library](https://developer.android.com/training/tv/start/start)
- [ExoPlayer / Media3](https://developer.android.com/media/media3/exoplayer)
- [Retrofit](https://square.github.io/retrofit/)
- [Fire TV Development](https://developer.amazon.com/docs/fire-tv/getting-started-developing-apps-and-games.html)
