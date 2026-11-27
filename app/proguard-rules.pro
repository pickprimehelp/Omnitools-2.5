# Safe ProGuard / R8 Keep Rules for OmniTools

# Android Manifest entry points & FileProvider
-keep public class com.example.MainActivity { *; }
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep class androidx.core.content.FileProvider { *; }

# Keep AdMob SDK & reflection-accessed classes intact if R8 is ever enabled
-keep class com.google.android.gms.ads.** { *; }
-keep class com.example.ads.** { *; }

# Reflection-sensitive Compose bridge class
-keep class androidx.compose.ui.platform.AndroidCompositionLocals_androidKt {
    public static androidx.compose.runtime.ProvidableCompositionLocal getLocalLifecycleOwner();
}

-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod,SourceFile,LineNumberTable

