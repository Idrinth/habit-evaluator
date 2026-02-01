# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in the Android SDK tools.

# Keep shared library classes used by Android
-keep class de.idrinth.habitevaluator.shared.model.** { *; }
-keep class de.idrinth.habitevaluator.shared.service.** { *; }
-keep class de.idrinth.habitevaluator.shared.api.** { *; }
-keep class de.idrinth.habitevaluator.shared.repository.** { *; }

# Keep Gson classes and ensure reflection-based serialization works
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keepclassmembers class de.idrinth.habitevaluator.shared.model.** {
    <init>();
    <fields>;
}

# Suppress warnings for server-side annotations not present on Android
-dontwarn jakarta.persistence.**
-dontwarn com.fasterxml.jackson.annotation.**

# SLF4J uses ServiceLoader; suppress warnings for missing implementations
-dontwarn org.slf4j.**
