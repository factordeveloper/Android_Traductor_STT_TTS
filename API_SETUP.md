# 🌐 Configuración de API de Traducción

Esta aplicación utiliza servicios de traducción para convertir texto entre diferentes idiomas. Por defecto, usa **LibreTranslate** (gratuito), pero puedes configurar **Google Translate API** para mayor precisión.

## 🆓 LibreTranslate (Por Defecto)

La app está configurada para usar LibreTranslate, que es:
- ✅ **Completamente gratuito**
- ✅ **Sin límites de uso**
- ✅ **No requiere API key**
- ✅ **Código abierto**
- ⚠️ Menos preciso que Google Translate

**No necesitas hacer nada para usar LibreTranslate - ya está funcionando.**

## 🏆 Google Translate API (Opcional)

Para mayor precisión en las traducciones, puedes configurar Google Translate API:

### Paso 1: Crear proyecto en Google Cloud
1. Ve a [Google Cloud Console](https://console.cloud.google.com/)
2. Crea un nuevo proyecto o selecciona uno existente
3. En el menú lateral, ve a "APIs y servicios" > "Biblioteca"
4. Busca "Cloud Translation API" y habilítala

### Paso 2: Crear API Key
1. Ve a "APIs y servicios" > "Credenciales"
2. Haz clic en "Crear credenciales" > "Clave de API"
3. Copia la API key generada
4. **Importante**: Restringe la API key solo a "Cloud Translation API"

### Paso 3: Configurar en la app
1. En la app, toca el ícono de configuración ⚙️ en la esquina superior derecha
2. Pega tu API key en el campo correspondiente
3. Guarda la configuración

### Paso 4: Modificar código (opcional)
Si quieres usar Google Translate por defecto, cambia en `ApiConfig.kt`:
```kotlin
const val USE_LIBRE_TRANSLATE = false // Cambiar a false
```

## 💰 Costos de Google Translate API

Google Translate API tiene un modelo de precios:
- **Primeros 500,000 caracteres/mes**: GRATIS
- **Después**: $20 USD por millón de caracteres

Para uso personal normal, es muy probable que no excedas el límite gratuito.

## 🔧 Idiomas Soportados

### LibreTranslate
- Español (es)
- Inglés (en)
- Francés (fr)
- Alemán (de)
- Italiano (it)
- Portugués (pt)
- Chino (zh)
- Japonés (ja)

### Google Translate
- Más de 100 idiomas soportados
- Detección automática de idioma
- Mayor precisión en traducciones

## 🛡️ Seguridad

**IMPORTANTE**: 
- Nunca compartas tu API key públicamente
- En una app de producción, usa un backend para ocultar la API key
- Configura restricciones en Google Cloud Console

## 🔄 Cambiar entre servicios

Puedes cambiar entre LibreTranslate y Google Translate en cualquier momento:
1. Abre la configuración ⚙️
2. Ingresa o elimina tu API key
3. La app automáticamente usará el servicio apropiado

## 📞 Soporte

Si tienes problemas:
1. Verifica que la API key sea correcta
2. Asegúrate de que Cloud Translation API esté habilitada
3. Revisa las restricciones de la API key en Google Cloud Console 