# R8 Optimization and Obfuscation Rules for OmniTools Release

# Keep only Android Manifest entry points
-keep public class com.example.MainActivity { *; }
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# Allow R8 to optimize & obfuscate internal app, ads, and QR/ZXing packages
# (Google Play Services Ads AAR supplies its own consumer ProGuard rules)
-dontwarn com.google.android.gms.ads.**
-dontwarn com.google.zxing.**

# Reflection-sensitive Compose bridge class
-keep class androidx.compose.ui.platform.AndroidCompositionLocals_androidKt {
    public static androidx.compose.runtime.ProvidableCompositionLocal getLocalLifecycleOwner();
}

# Maximize DEX obfuscation & compact stack traces
-repackageclasses 'o'
-allowaccessmodification
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod,SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
