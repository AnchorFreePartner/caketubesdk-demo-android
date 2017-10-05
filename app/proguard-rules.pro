# Add project specific ProGuard rules here.
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-dontwarn android.security.**
-dontwarn com.quantcast.**
-dontwarn com.squareup.okhttp.**
-dontwarn javax.mail.**
-dontwarn okio.**
-dontwarn retrofit.**
-keep class ch.qos.** { *; }
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient { com.google.android.gms.ads.identifier.AdvertisingIdClient$Info getAdvertisingIdInfo(android.content.Context); }
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient$Info { java.lang.String getId(); boolean isLimitAdTrackingEnabled(); }
-keep class com.google.gson.** { *; }
-keep class com.google.inject.* { *; }
-keep class com.northghost.** { *; }
-keep class com.quantcast.** { *; }
-keep class com.squareup.okhttp.** { *; }
-keep class de.blinkt.openvpn.** { *; }
-keep class javax.inject.* { *; }
-keep class org.apache.http.* { *; }
-keep class org.apache.http.**
-keep class org.apache.james.mime4j.* { *; }
-keep class org.slf4j.** { *; }
-keep class retrofit.** { *; }
-keep class sun.misc.Unsafe { *; }
-keep interface com.northghost.**
-keep interface com.squareup.okhttp.** { *; }
-keep public class com.google.android.gms.ads.identifier.** { *; }
-keepattributes *
-keepattributes *Annotation*
-keepattributes InnerClasses
-keepattributes Signature
-keepattributes Exceptions
-keepclasseswithmembers class * { @retrofit.http.* <methods>; }
