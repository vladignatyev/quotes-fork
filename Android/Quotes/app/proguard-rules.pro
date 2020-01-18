# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# ====== BEGIN Project level ======
-keep class com.quote.mosaic.** { *; }
-keepnames class com.quote.mosaic.ParcelableArg
-keepnames class com.quote.mosaic.SerializableArg
-keepnames class com.quote.mosaic.EnumArg

# ====== END Project level ======

# ====== BEGIN OkHttp ======
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
# ====== END OkHttp ======

# ====== BEGIN Retrofit2 ======
-dontwarn okio.**
-dontwarn javax.annotation.**
# ====== END Retrofit2 ======

# ====== BEGIN Jackson ======
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.reflect.jvm.internal.**
-keep class com.fasterxml.jackson.annotation.** { *; }
-dontwarn org.w3c.dom.bootstrap.DOMImplementationRegistry
-dontwarn com.fasterxml.jackson.databind.**
# ====== END Jackson ======

# ====== BEGIN Dagger 2 ======
-dontwarn com.google.errorprone.annotations.*
# ====== END Dagger 2 ======

# ====== BEGIN Glide ======
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
# ====== END Glide ======

# ====== BEGIN Misc ======
-dontwarn java.beans.**
-keepattributes SourceFile,LineNumberTable
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
# ====== END Misc ======

# ===== BEGIN Kotlin ======
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
}
# ===== BEGIN Kotlin ======

# ===== BEGIN Dagger ======
-keepclassmembers,allowobfuscation class * extends androidx.lifecycle.AndroidViewModel {
    <init>(android.app.Application);
}
# ===== END Dagger ======
