# DriveSelect - Gestión y Renta de Vehículos

**DriveSelect** es una aplicación móvil nativa para Android desarrollada en Kotlin y Jetpack Compose, diseñada tanto para la gestión administrativa de agencias
de alquiler de autos como para el autoservicio de reservaciones de clientes. Permite consultar el catálogo interactivo de la flota, validar disponibilidad de
fechas en tiempo real, procesar checklists de entrega presencial, liquidar contratos con cálculo automático de mora y sincronizar operaciones mediante Firebase.

------

## Enlaces del Proyecto

* **Prototipo Interactivo en Figma:** [Enlace a Figma de DriveSelect]
* **Sitio Web para descargar App:** [Enlace de Descarga](
* **Repositorio GitHub:** [https://github.com/eledwinin/DriveSelect](https://github.com/eledwinin/DriveSelect)

---

## Tecnologías y Arquitectura

* **Lenguaje:** Kotlin
* **Interfaz de Usuario:** Jetpack Compose (Material Design 3 con paleta personalizada Dark/Gold)
* **Arquitectura:** MVVM (Model-View-ViewModel) + Repository Pattern
* **Autenticación:** Firebase Authentication (Correo/Contraseña)
* **Base de Datos:** Firebase Cloud Firestore (Sincronización en tiempo real)
* **Carga de Imágenes:** Coil Compose
* **Asincronía y Estado:** Corrutinas de Kotlin, `State`, `StateFlow` y `mutableStateOf`

----

## Manual de Usuario

### 1. Acceso y Registro
* **Inicio de Sesión:** Ingrese su correo electrónico y contraseña. El sistema detectará automáticamente el rol asignado (Cliente o Administrador) y lo redirigirá al panel respectivo.
* **Recuperación de Cuenta:** Si olvidó su contraseña, utilice la opción *¿Olvidaste tu contraseña?* para recibir un enlace de restablecimiento directo en su correo.
* **Registro de Cliente:** Presione *Crea tu Cuenta*, complete sus datos personales (Nombre, Teléfono, DUI/Pasaporte, Licencia) y confirme su contraseña para habilitar el acceso.

---

### 2. Módulo de Administración (Personal de Sucursal)

* **Inventario y Filtro de Vehículos:** En la pantalla principal, consulte el catálogo completo con el estado de cada auto en tiempo real (*Disponible* en verde, *Alquilado en Proceso* en naranja y *Alquilado en Uso* en rojo). Filtre por marcas o verifique rangos de fechas disponibles.
* **Renta Inmediata en Sucursal:**
  1. Seleccione un vehículo disponible y presione **Rentar**.
  2. Ingrese los datos obligatorios del cliente (Nombre, Teléfono de 8 dígitos, DUI/Pasaporte, N° Licencia y Correo). Si el cliente ya tiene una cuenta registrada en la app, la renta se asociará automáticamente a su historial.
  3. Seleccione la fecha pactada de devolución.
  4. Presione **Validar y Entregar Vehículo**.
  5. En el checklist modal, verifique los 3 requisitos físicos (*Documentos verificados*, *Contrato firmado*, *Pago/Depósito recibido*) y confirme la entrega de llaves para pasar el vehículo a estado **EN USO**.
* **Gestión de Solicitudes y Entregas:**
  1. Ingrese a la pestaña de **Pendientes** en el panel de administración.
  2. Revise las reservas programadas para hoy o fechas futuras.
  3. Para las entregas del día, presione **Entregar Vehículo**, valide el checklist y apruebe la salida; o presione **Rechazar** si no procede.
* **Monitoreo de Unidades y Recepción:**
  1. En la pestaña **En Uso**, supervise los autos en circulación clasificados en: *Vencidos con Mora*, *Devoluciones de Hoy* y *Contratos Vigentes*.
  2. Al recibir las llaves de regreso, presione **Recibir Vehículo**.
  3. Si la entrega se realiza a tiempo, libere el auto a estado **DISPONIBLE**. Si presenta días de atraso, el sistema desglosará automáticamente la mora acumulada y el costo total liquidado antes de liberar la unidad.

---

### 3. Módulo de Clientes

* **Reservar un Vehículo:**
  1. Explore el catálogo interactivo y seleccione el auto de su preferencia.
  2. Toque los selectores de fecha de recogida y devolución (los días ocupados por otras rentas se bloquean automáticamente).
  3. Verifique el resumen de días calculados y el costo total estimado.
  4. Confirme la solicitud para enviarla a revisión de la sucursal.
* **Seguimiento en "Mis Solicitudes y Rentas" (Historial):**
  * **Todos:** Visualice el historial completo de sus transacciones.
  * **Pendientes:** Consulte las solicitudes en espera de revisión por el personal.
  * **En Uso:** Supervise sus rentas activas, fechas de devolución y alertas de recargo por mora si presenta retrasos.
  * **Finalizados:** Registro de vehículos devueltos y liquidados exitosamente.
  * **Cancelados:** Solicitudes rechazadas o canceladas.

---

### Recursos y Pasos para la Ejecución

### Requisitos Previos:
* **Android Studio:** Hedgehog (2023.1.1) o superior.
* **JDK:** Versión 17 o superior.
* **Dispositivo físico o emulador:** Android 8.0 (API Nivel 26) o superior.
* **Servicios de Google:** Archivo `google-services.json` configurado en el directorio `/app`.

### Configuración de Firebase (Para nuevos entornos):
1. Crea un proyecto en la consola de [Firebase](https://console.firebase.google.com/).
2. Habilita **Firebase Authentication** (Email/Password) y **Cloud Firestore**.
3. Descarga el archivo `google-services.json` desde la configuración del proyecto en Firebase.
4. Pega el archivo en la ruta `app/google-services.json` de este proyecto antes de compilar.

### Pasos para clonar y ejecutar:

1. **Clonar el repositorio:**
   ```bash
   git clone [https://github.com/eledwinin/DriveSelect.git](https://github.com/eledwinin/DriveSelect.git)
