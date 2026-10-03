# Seguridad de autenticación

El backend no usa contraseñas hardcodeadas ni guarda el access token en el navegador.

## Variables obligatorias

Configurar estas variables en el secret manager del proveedor (Render/Railway), no en un archivo versionado:

- `ADMIN_USERNAME`: usuario administrador.
- `ADMIN_PASSWORD_HASH`: hash BCrypt de una contraseña nueva. Es preferible a `ADMIN_PASSWORD`.
- `JWT_SECRET`: valor Base64 generado, por ejemplo con `openssl rand -base64 48`.
- `CORS_ALLOWED_ORIGINS`: dominios exactos del frontend, separados por comas.
- `JWT_COOKIE_SECURE=true` y `JWT_COOKIE_SAME_SITE=None` para frontend y backend en sitios distintos.

`ADMIN_PASSWORD` existe solo como compatibilidad temporal para bootstrap local. Si se usa, debe tener al menos 12 caracteres y conviene migrarla a `ADMIN_PASSWORD_HASH`.

Para desarrollo HTTP local, usar `JWT_COOKIE_SECURE=false` y `JWT_COOKIE_SAME_SITE=Lax`. En producción deben ser `true` y `None` si frontend y backend están en sitios distintos.

El ejemplo de variables está en [.env.example](.env.example). Los archivos `.env*` y `application-local.properties` están ignorados.

## Flujo

1. `POST /api/auth/login` valida BCrypt y devuelve un access JWT corto (15 minutos por defecto).
2. El refresh token es opaco, se almacena solo como SHA-256 en `refresh_tokens`, y se entrega en una cookie `HttpOnly`, `Secure`, `SameSite` y `Path=/api/auth`.
3. `POST /api/auth/refresh` rota el refresh token. Hay una gracia de 10 segundos para evitar que dos pestañas se invaliden entre sí; reutilizar un token fuera de esa ventana invalida la familia de sesión.
4. Las peticiones admin envían `Authorization: Bearer <access-token>`.
5. `POST /api/auth/logout` revoca la sesión y borra la cookie.

El cliente React mantiene el access token solo en memoria y renueva una sola vez ante un `401` concurrente.

## Usuarios del panel

El panel trabaja con usuarios registrados en la tabla `app_users` (correo + hash
BCrypt con coste 12). La contraseña en claro solo existe durante la petición: se
transforma con BCrypt y nunca se persiste, se registra ni se devuelve en la API.

- `POST /api/admin/users` crea un usuario. Exige `ROLE_ADMIN`, así que solo un
  administrador autenticado puede dar de alta cuentas.
- `GET /api/admin/users` lista los usuarios sin exponer hashes.
- `GET /api/auth/password-policy` publica las reglas de contraseña para que el
  formulario no duplique la política.

Política de contraseña: entre 12 y 256 caracteres, con al menos una minúscula,
una mayúscula, un número y un carácter especial, sin espacios y sin contener el
propio correo. El backend valida siempre; la lista del formulario es solo vista
previa.

Cada cuenta creada tiene `ROLE_ADMIN`, el mismo acceso que la sesión que la
creó. Si se necesita un rol de solo lectura hay que definir primero la matriz de
permisos por endpoint.

El refresh token identifica al usuario dueño de la sesión a través del registro
almacenado (se busca por su hash SHA-256), no por un identificador que envíe el
navegador. Si una cuenta se desactiva, sus sesiones dejan de renovar.

`ADMIN_USERNAME`/`ADMIN_PASSWORD_HASH` se conservan como cuenta de rescate por
variables de entorno: siguen funcionando aunque la tabla de usuarios no esté
disponible.

## Política de endpoints

- Lecturas de catálogo y operations públicas de checkout/raspa y gana: públicas.
- Mutaciones, `/api/admin/**`, `/api/images-cloud/**` y `PUT /header-messages`: `ROLE_ADMIN`.
- Cualquier ruta desconocida: denegada.
- CORS no es un control de autorización; el backend exige el token.

## Rotación urgente

Si los valores antiguos estuvieron en Git, en un bundle o en un entorno compartido, rotar inmediatamente:

- contraseña administrativa;
- contraseña/usuario de base de datos;
- credenciales de Cloudinary;
- tokens de WhatsApp/Meta y cualquier otra credencial expuesta.

Eliminar un secreto del código no invalida el valor que ya pudo ser copiado.
