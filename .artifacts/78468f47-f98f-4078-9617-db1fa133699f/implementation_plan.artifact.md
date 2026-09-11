# Mejora del Flujo de Guardado de Direcciones

Este plan aborda la falta de respuesta al intentar guardar una dirección en el perfil, mejorando la validación de campos y proporcionando retroalimentación visual al usuario en caso de error.

## Cambios Propuestos

### Componente de UI

#### [MODIFY] [AddressesScreen.kt](file:///C:/Users/USUARIO/AndroidStudioProjects/GALY/app/src/main/java/com/galyxnovatec/tienda/ui/screens/AddressesScreen.kt)

- Añadir un estado `isSaving` para evitar clics múltiples y mostrar carga.
- Mejorar la validación del botón "GUARDAR EN MI PERFIL" (requerir dirección y ciudad).
- Tratamiento de campos vacíos como `null` antes de enviarlos al `AddressManager`.
- Mostrar un `Toast` informativo en caso de que la operación de guardado falle.

## Verificación Plan

### Manual Verification
- Intentar guardar una dirección sin completar la ciudad (el botón debe estar deshabilitado).
- Completar los campos y guardar. Si falla por red o servidor, debe aparecer un Toast indicando el error.
- Verificar que el diálogo se cierre y la lista se actualice tras un guardado exitoso.
