# HAN FOOD Ombor - ProGuard/R8 rules

# Room
-keep class androidx.room.** { *; }
-keep @androidx.room.Entity class * { *; }

# Kotlinx serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# ML Kit barcode scanning
-keep class com.google.mlkit.vision.barcode.** { *; }

# Keep our data models (entities, enums) fully — used by Room + serialization
-keep class com.hanfood.warehouse.data.local.entity.** { *; }
