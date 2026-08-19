# Sistema de Gestión de Préstamos - Laboratorio UTEZ 🏫

Proyecto desarrollado para la gestión eficiente de recursos (laptops, proyectores y accesorios) en el laboratorio de la **Universidad Tecnológica del Estado de Morelos (UTEZ)**.

## 🚀 Características Principales

*   **Conexión a la Nube:** Integración total con **Oracle Cloud Autonomous Database**.
*   **Seguridad:** Conexión mediante Oracle Wallet (TCPS).
*   **Lógica de Negocio:**
    *   Cálculo automático de fechas de entrega (5 días hábiles).
    *   Sistema de sanciones visuales para alumnos con préstamos vencidos o equipos dañados.
    *   Validación de disponibilidad de recursos en tiempo real.
*   **Interfaz Moderna:** Desarrollada en Java Swing con estilos personalizados e iconos intuitivos.

## 🛠️ Tecnologías Utilizadas

*   **Lenguaje:** Java 26 (JDK 26).
*   **Base de Datos:** Oracle Cloud Infrastructure.
*   **Gestión de Dependencias:** Maven.
*   **Librerías:** JDBC, Oracle PKI, JCalendar.

## 📦 Estructura del Proyecto

*   `src/main/java`: Código fuente de la aplicación.
*   `pom.xml`: Configuración de dependencias de Maven.
*   `README.md`: Documentación del proyecto.
*   *Nota: La carpeta `Wallet/` ha sido excluida del repositorio por razones de seguridad de la base de datos.*

## 👥 Desarrolladores
*   Equipo de Laboratorio UTEZ - Agosto 2026.
