# Café Aroma ☕

App Android (Jetpack Compose) de pedidos de café con **backend en Docker**, **PostgreSQL 16** y
**API pública en Render**. El login, el registro y la sesión funcionan contra esa base de datos;
si no hay internet, la app valida contra la copia local (Room) y lo indica en pantalla (nunca se cae).

```
cafe-aroma/
├── backend/                  API Express + PostgreSQL (Docker / Render)
├── cafe/                     App Android (Gradle)
├── render.yaml               Despliegue de un clic en Render
├── .github/workflows/ci.yml  Tests + APK automático en cada push
└── README.md
```

---

## 1. Usuarios sembrados

| Rol           | Correo                   | Contraseña    |
|---------------|--------------------------|---------------|
| Administrador | `admin@cafearoma.com`    | `admin123`    |
| Cliente       | `cliente@cafearoma.com`  | `cliente123`  |

La API solo guarda hashes **bcrypt**; nunca devuelve contraseñas. Se crean solos al arrancar.

---

## 2. Poner la API pública en Render (5 minutos)

1. Sube este repositorio a GitHub (ver §4).
2. Entra a [render.com](https://render.com) con tu cuenta de GitHub → **New + → Blueprint**.
3. Apunta el blueprint a tu repo: Render leerá el `render.yaml` de la raíz y creará
   `cafe-aroma-api` (web) + `cafe-aroma-db` (PostgreSQL gratis).
4. Presiona **Apply / Deploy** y espera a que el build termine.
5. Tu API queda en:

```
https://cafe-aroma-api.onrender.com/api/health
```

> El nombre del servicio **debe ser `cafe-aroma-api`** para que coincida con la URL por defecto
> de la app. Si eliges otro nombre, cámbialo dentro de la app en **Ajustes → Servidor**.

**Notas del plan gratis**

- La base de datos gratis de Render expira a los 90 días: cuando pase, crea una gratis en
  [Neon](https://neon.tech) o [Supabase](https://supabase.com) y pega su `DATABASE_URL` en la
  variable de entorno del servicio (Render → Environment).
- El servicio se duerme tras 15 min sin uso; el primer pedido tarda ~40 s (la app muestra
  "Comprobando servidor..." y luego responde).

Verificar:

```powershell
Invoke-RestMethod https://cafe-aroma-api.onrender.com/api/health
```

---

## 3. App Android

### Descargar el APK

Cada push a `main` compila y publica el APK como **artifact** en la pestaña *Actions* del repo.
Para publicarlo como descarga directa, crea una etiqueta:

```powershell
git tag v1.0.0
git push origin v1.0.0
```

La workflow `CI` sube el APK a la **Release** asociada (pestaña *Releases* del repo).

### URL del servidor

La app trae por defecto `https://cafe-aroma-api.onrender.com/api/`, pero es configurable
**sin reinstalar** en **Ajustes → Servidor**:

- Pega la URL de tu API y toca **Guardar y probar**
- Se aceptan formatos `https://dominio.com`, `192.168.1.50:8080` (auto `http://`) o la URL completa
- El chip naranja/verde te dice si responde o si estás en **modo local**

Si el servidor no responde, el login sigue funcionando contra la base local (Room) y la app
muestra *"Sin conexión · modo local"* en lugar de bloquearse.

### Compilar

```powershell
cd cafe
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug                       # usa la URL pública por defecto
.\gradlew.bat assembleDebug "-PAPI_BASE_URL=http://10.0.2.2:8080/api/"   # contra tu PC
```

---

## 4. Repositorio en GitHub + CI

El repo ya está inicializado. Para subirlo:

```powershell
cd C:\Users\Fernando\AndroidStudioProjects\cafe
git add -A
git commit -m "Cafe Aroma: API en Docker + login con PostgreSQL + CI"
gh repo create cafe-aroma --public --source=. --push
```

La workflow `.github/workflows/ci.yml` corre en cada push:

| Job      | Qué hace                                              |
|----------|-------------------------------------------------------|
| backend  | `npm test` (validadores, bcrypt, JWT)                 |
| android  | `gradlew testDebugUnitTest` + `assembleDebug` + APK   |
| release  | Al etiquetar `v*`, publica el APK en la Release       |

---

## 5. Desarrollo local con Docker (independiente de KIVO)

```powershell
cd backend
docker compose up -d --build      # API 8080 + PostgreSQL 5433
Invoke-RestMethod http://localhost:8080/api/health
docker compose down               # conserva datos
docker compose down -v            # borra todo y vuelve a sembrar usuarios
```

Puertos **no** chocan con tu proyecto KIVO:

| Servicio        | Contenedor          | Puerto |
|-----------------|---------------------|--------|
| API Café Aroma  | `cafe-aroma-api`    | 8080   |
| PostgreSQL      | `cafe-aroma-db`     | 5433   |
| *KIVO*          | `backend-backend-1` | 3000   |
| *KIVO*          | `backend-db-1`      | 5432   |

Para usarla desde el celular en la misma Wi-Fi, en **Ajustes → Servidor** escribe
`http://LA-IP-DE-TU-PC:8080` (y permite el puerto 8080 en el firewall de Windows).

---

## 6. Endpoints

| Método | Ruta                  | Auth       | Descripción                              |
|--------|-----------------------|------------|------------------------------------------|
| GET    | `/api/health`         | –          | Estado de API y base de datos            |
| POST   | `/api/auth/register`  | –          | Crea usuario (bcrypt + JWT)              |
| POST   | `/api/auth/login`     | –          | Devuelve `token` + `user`                |
| GET    | `/api/auth/me`        | Bearer     | Valida el token                          |
| PATCH  | `/api/users/me`       | Bearer     | Perfil, puntos, récord                   |
| DELETE | `/api/users/me`       | Bearer     | Elimina la cuenta                        |
| GET    | `/api/users`          | Bearer admin | Listado de usuarios                   |

Errores: `{ "error": "mensaje", "code": "INVALID_CREDENTIALS" }`
(400 validación · 401 credenciales/token · 409 correo duplicado · 429 intentos).

---

## 7. Tests

```powershell
cd backend; npm install; npm test     # 11 tests (node --test)
cd ..\cafe; .\gradlew.bat testDebugUnitTest
```

---

## 8. Solución de problemas

| Síntoma | Solución |
|---------|----------|
| "Sin conexión · modo local" | Revisa la URL en **Ajustes → Servidor** y que la API responda (`/api/health`) |
| Render tarda en responder | El plan gratis se despierta en ~40 s; vuelve a tocar **Probar conexión** |
| `Bind for 0.0.0.0:5432 failed` | KIVO usa ese puerto; este proyecto corre en **5433** |
| El login no entra con el token viejo | Cierra sesión: los JWT viven 7 días |
| Quiero reiniciar la base local | `docker compose down -v && docker compose up -d --build` |
