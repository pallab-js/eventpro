# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers class * extends androidx.room.RoomDatabase { abstract *; }

# Hilt
-keep class dagger.hilt.** { *; }
-keep @dagger.hilt.android.HiltAndroidApp class *

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keep,includedescriptorclasses class com.eventpro.admin.**$$serializer { *; }
-keepclassmembers class com.eventpro.admin.** {
    *** Companion;
}

# Vico
-keep class com.patrykandpatrick.vico.** { *; }

# Obfuscation
-obfuscationdictionary /dev/null
-repackageclasses ''

# Keep Compose
-keep class androidx.compose.** { *; }

# Keep nothing extra - R8 handles icon shrinking
