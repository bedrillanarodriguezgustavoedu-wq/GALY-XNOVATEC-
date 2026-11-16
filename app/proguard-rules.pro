# Reglas R8 de la app GALY. La plantilla base está en src/main/keepRules/rules.keep y AGP
# la combina con este archivo, así que aquí solo se declara lo que el proyecto usa realmente.

# Gson (2.8.5, transitivo de converter-gson 2.9.0) y Retrofit leen firmas genéricas,
# anotaciones y nombres de campos por reflejo; sin esto el JSON llega vacío en release.
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations, AnnotationDefault

# Gson 2.8.5 no trae reglas propias e instancia modelos sin constructor vacío vía
# sun.misc.Unsafe, clase que no está en el android.jar de compilación.
-dontwarn sun.misc.**
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# Campos anotados con @SerializedName: R8 full mode los elimina si parecen no usados.
-keepclassmembers,allowobfuscation,allowoptimization class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Modelos de data/model y DTOs de data/network: varios campos (CartItem, OrderRequest,
# ManualPaymentMethod) se serializan por su nombre exacto, y el carrito y la sesión se
# guardan como JSON en SharedPreferences, por lo que renombrarlos invalida los datos que
# ya escribieron versiones instaladas.
-keep class com.galyxnovatec.tienda.data.model.** { *; }
-keep class com.galyxnovatec.tienda.data.network.** { *; }
-keep interface com.galyxnovatec.tienda.data.network.** { *; }

# Servicio de push declarado en el AndroidManifest: el sistema lo instancia por reflejo.
-keep class com.galyxnovatec.tienda.data.service.MyFirebaseMessagingService { *; }
