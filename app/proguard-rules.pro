# Retrofit
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.barrettotte.fishtank.data.model.** { *; }

# Gson
-keep class com.google.gson.** { *; }

# Strip all Android log calls in release builds
-assumenosideeffects class android.util.Log {
    public static int d(...);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
}
