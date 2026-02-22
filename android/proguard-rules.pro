# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in the Android SDK tools.

# Keep shared library classes used by Android
-keep class de.idrinth.habitevaluator.shared.model.** { *; }
-keep class de.idrinth.habitevaluator.shared.service.** { *; }
-keep class de.idrinth.habitevaluator.shared.api.** { *; }
-keep class de.idrinth.habitevaluator.shared.repository.** { *; }

# Keep Android persistence classes
-keep class de.idrinth.habitevaluator.android.persistence.** { *; }

# Keep Gson classes and ensure reflection-based serialization works
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keepclassmembers class de.idrinth.habitevaluator.shared.model.** {
    <init>();
    <fields>;
}

# Kotlin metadata for reflection
-keep class kotlin.Metadata { *; }
-keepclassmembers class * {
    @kotlin.Metadata *;
}

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# Room
-keep class * extends androidx.room.RoomDatabase
-keepclassmembers class * extends androidx.room.RoomDatabase {
    abstract <methods>;
}
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao interface *

# Suppress warnings for server-side annotations not present on Android
-dontwarn jakarta.persistence.**
-dontwarn com.fasterxml.jackson.annotation.**

# SLF4J uses ServiceLoader; suppress warnings for missing implementations
-dontwarn org.slf4j.**

# SnakeYAML uses java.beans for introspection which is not available on Android
# The library works without it by falling back to field-based access
-dontwarn java.beans.**
-dontwarn org.yaml.snakeyaml.introspector.**
-keep class org.yaml.snakeyaml.** { *; }
