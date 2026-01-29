# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in the Android SDK tools.

# Keep shared library models
-keep class de.idrinth.habitevaluator.shared.model.** { *; }
-keep class de.idrinth.habitevaluator.shared.service.** { *; }

# Keep Gson classes
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
