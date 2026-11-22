# LogisticaCD_GUI

Sistema de escritorio para registrar productos, organizar la cola de camiones y despachar entregas desde un centro de distribución.

## Ejecución en NetBeans

1. Abrir la carpeta del proyecto desde **Archivo > Abrir proyecto**.
2. Ejecutar **LogisticaCD_GUI** con **F6**. La clase principal es `logistica.Main`.
3. Para editar el formulario, abrir `src/logistica/Main.java` y seleccionar **Diseño**.

El proyecto usa Java 8 o posterior, Swing y Ant. Los datos se conservan durante la sesión.

## Registro de una entrega

Registrar un producto en **Inventario**, agregar las ciudades en **Rutas** y encolar al menos un vehículo en **Camiones**. En **Despachos**, ingresar el código, la cantidad y el destino. Cada entrega confirmada descuenta stock, asigna el primer camión de la cola y aparece en **Historial**.
