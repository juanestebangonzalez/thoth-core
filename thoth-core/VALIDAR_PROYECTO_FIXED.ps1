# THOTH C.O.R.E - Script de Validacion (PowerShell - Version Simplificada)
# Ejecutar: powershell.exe -ExecutionPolicy Bypass -File .\VALIDAR_PROYECTO.ps1

Write-Host ""
Write-Host "========================================================================" -ForegroundColor Cyan
Write-Host "THOTH C.O.R.E - VALIDACION AUTOMATICA" -ForegroundColor Cyan
Write-Host "========================================================================" -ForegroundColor Cyan
Write-Host ""

# PASO 1: Verificar ubicacion
Write-Host "[1/7] Verificando ubicacion del proyecto..." -ForegroundColor Yellow

if (-not (Test-Path "build.gradle")) {
    Write-Host "ERROR: No esta en la carpeta correcta!" -ForegroundColor Red
    Write-Host "Debe estar en: C:\cursos\Proyectos\thoth-core\thoth-core" -ForegroundColor Red
    Write-Host "Carpeta actual: $(Get-Location)" -ForegroundColor Red
    Read-Host "Presione Enter para salir"
    exit 1
}
Write-Host "OK: Ubicacion correcta" -ForegroundColor Green

# PASO 2: Verificar estructura
Write-Host ""
Write-Host "[2/7] Verificando estructura de carpetas..." -ForegroundColor Yellow
Write-Host ""

$directories = @(
    "src\main\java\com\thoth",
    "src\main\java\com\thoth\domain\model",
    "src\main\java\com\thoth\domain\valueobject",
    "src\test\java\com\thoth\domain",
    "src\main\resources"
)

$missing = $false

foreach ($dir in $directories) {
    if (Test-Path $dir) {
        Write-Host "OK: $dir" -ForegroundColor Green
    } else {
        Write-Host "ERROR: Falta $dir" -ForegroundColor Red
        $missing = $true
    }
}

if (Test-Path "src\main\resources\application.yml") {
    Write-Host "OK: src\main\resources\application.yml" -ForegroundColor Green
} else {
    Write-Host "ERROR: Falta application.yml" -ForegroundColor Red
    $missing = $true
}

if ($missing) {
    Write-Host ""
    Write-Host "ERROR: Faltan carpetas/archivos importantes!" -ForegroundColor Red
    Read-Host "Presione Enter para salir"
    exit 1
}

# PASO 3: Verificar archivos Java
Write-Host ""
Write-Host "[3/7] Verificando archivos Java..." -ForegroundColor Yellow
Write-Host ""

$javaFiles = @(
    "src\main\java\com\thoth\ThothCoreApplication.java",
    "src\main\java\com\thoth\domain\model\Equipment.java",
    "src\main\java\com\thoth\domain\valueobject\Location.java",
    "src\main\java\com\thoth\domain\valueobject\EquipmentStatus.java",
    "src\main\java\com\thoth\domain\valueobject\EquipmentCategory.java",
    "src\test\java\com\thoth\domain\model\EquipmentTest.java",
    "src\test\java\com\thoth\domain\valueobject\LocationTest.java"
)

$missing = $false

foreach ($file in $javaFiles) {
    if (Test-Path $file) {
        Write-Host "OK: $(Split-Path $file -Leaf)" -ForegroundColor Green
    } else {
        Write-Host "ERROR: Falta $(Split-Path $file -Leaf)" -ForegroundColor Red
        $missing = $true
    }
}

if ($missing) {
    Write-Host ""
    Write-Host "ERROR: Faltan archivos Java!" -ForegroundColor Red
    Read-Host "Presione Enter para salir"
    exit 1
}

# PASO 4: Verificar Gradle
Write-Host ""
Write-Host "[4/7] Verificando Gradle..." -ForegroundColor Yellow
Write-Host ""

if (-not (Test-Path "gradlew.bat")) {
    Write-Host "ERROR: No existe gradlew.bat" -ForegroundColor Red
    Read-Host "Presione Enter para salir"
    exit 1
}
Write-Host "OK: gradlew.bat" -ForegroundColor Green

if (-not (Test-Path "build.gradle")) {
    Write-Host "ERROR: No existe build.gradle" -ForegroundColor Red
    Read-Host "Presione Enter para salir"
    exit 1
}
Write-Host "OK: build.gradle" -ForegroundColor Green

Write-Host ""
Write-Host "Ejecutando: gradle --version" -ForegroundColor Cyan
& .\gradlew.bat --version
Write-Host ""

# PASO 5: gradle clean
Write-Host "[5/7] Limpiando build anterior..." -ForegroundColor Yellow
Write-Host ""

& .\gradlew.bat clean
if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "ERROR: gradle clean fallo" -ForegroundColor Red
    Read-Host "Presione Enter para salir"
    exit 1
}
Write-Host ""
Write-Host "OK: gradle clean exitoso" -ForegroundColor Green

# PASO 6: gradle build
Write-Host ""
Write-Host "[6/7] Compilando proyecto..." -ForegroundColor Yellow
Write-Host ""

& .\gradlew.bat test -x test
if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "ERROR: gradle build fallo" -ForegroundColor Red
    Write-Host "Ver logs arriba para detalles" -ForegroundColor Red
    Read-Host "Presione Enter para salir"
    exit 1
}
Write-Host ""
Write-Host "OK: gradle build exitoso" -ForegroundColor Green

# PASO 7: gradle test
Write-Host ""
Write-Host "[7/7] Ejecutando tests unitarios..." -ForegroundColor Yellow
Write-Host ""

& .\gradlew.bat test
if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "ERROR: Algunos tests fallaron" -ForegroundColor Red
    Write-Host "Ver reporte en: build\reports\tests\test\index.html" -ForegroundColor Red
    Read-Host "Presione Enter para salir"
    exit 1
}

# RESUMEN FINAL
Write-Host ""
Write-Host "========================================================================" -ForegroundColor Green
Write-Host "VALIDACION COMPLETADA" -ForegroundColor Green
Write-Host "========================================================================" -ForegroundColor Green
Write-Host ""
Write-Host "Proyecto:        THOTH C.O.R.E v1.0.0-MVP" -ForegroundColor Green
Write-Host "Estado:          100 Porciento Funcional" -ForegroundColor Green
Write-Host "Compilacion:     OK - Exitosa" -ForegroundColor Green
Write-Host "Tests:           OK - Pasando" -ForegroundColor Green
Write-Host "Estructura:      OK - Correcta" -ForegroundColor Green
Write-Host "Dependencias:    OK - Completas" -ForegroundColor Green
Write-Host ""
Write-Host "PROXIMOS PASOS:" -ForegroundColor Yellow
Write-Host "1. gradle bootRun --args='--spring.profiles.active=dev'" -ForegroundColor Yellow
Write-Host "2. http://localhost:8080/api/actuator/health" -ForegroundColor Yellow
Write-Host "3. PASO 4: Puertos y Casos de Uso" -ForegroundColor Yellow
Write-Host ""
Write-Host "========================================================================" -ForegroundColor Green
Write-Host ""

Read-Host "Presione Enter para salir"
