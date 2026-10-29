# Keep kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep data classes used with serialization
-keep,includedescriptorclasses class app.dsiacademy.academy.**$$serializer { *; }
-keepclassmembers class app.dsiacademy.academy.** {
    *** Companion;
}
-keepclasseswithmembers class app.dsiacademy.academy.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
