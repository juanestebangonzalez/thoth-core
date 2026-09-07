# PASO 6: LIMPIAR ARCHIVOS CON BOM Y EJECUTAR FINAL
# Ejecutar: powershell.exe -ExecutionPolicy Bypass -File .\LIMPIAR_Y_CREAR.ps1

Write-Host ""
Write-Host "PASO 6: LIMPIEZA Y CREACION (VERSION FINAL)" -ForegroundColor Cyan
Write-Host ""

$basePath = "src\main\java\com\thoth"

# Limpiar archivos anteriores con BOM
Write-Host "Limpiando archivos anteriores con BOM..." -ForegroundColor Yellow

$pathsToClean = @(
    "$basePath\adapter\in\rest\controller\EquipmentController.java",
    "$basePath\adapter\in\rest\controller\MaintenanceController.java",
    "$basePath\adapter\in\rest\dto\request\*.java",
    "$basePath\adapter\in\rest\dto\response\*.java",
    "$basePath\adapter\in\rest\exception\*.java",
    "$basePath\config\SpringDocOpenApiConfig.java"
)

foreach ($path in $pathsToClean) {
    if (Test-Path $path) {
        Remove-Item -Path $path -Force -Verbose
    }
}

Write-Host ""
Write-Host "Ejecutando script de creacion final..." -ForegroundColor Yellow
Write-Host ""

# Ejecutar el script final
& ".\CREAR_PASO_06_FINAL.ps1"

Write-Host ""
Write-Host "Completado. Ahora ejecuta:" -ForegroundColor Green
Write-Host "  gradle clean" -ForegroundColor Gray
Write-Host "  gradle build" -ForegroundColor Gray
Write-Host ""
