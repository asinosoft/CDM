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
# keep GsonSerializable interface, it would be thrown away by proguard since it is empty
-keep class com.asinosoft.cdm.data.Settings

-keepclassmembers enum * { *; }

# member fields of serialized classes, including enums that implement this interface
-keepclassmembers class * implements com.asinosoft.cdm.data.Settings {
    <fields>;
}

# also keep names of these classes. not required, but just in case.
-keepnames class * implements com.asinosoft.cdm.data.Settings