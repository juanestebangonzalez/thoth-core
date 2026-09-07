# PowerShell Script - Reparar archivo application-test.yml
# Ubicación: C:\cursos\Proyectos\thoth-core\thoth-core\
# Ejecutar: powershell.exe -ExecutionPolicy Bypass -File .\REPARAR_TEST_YML.ps1

Write-Host ""
Write-Host "========================================================================" -ForegroundColor Cyan
Write-Host "THOTH C.O.R.E - REPARAR application-test.yml" -ForegroundColor Cyan
Write-Host "========================================================================" -ForegroundColor Cyan
Write-Host ""

# Verificar ubicación
if (-not (Test-Path "build.gradle")) {
    Write-Host "ERROR: No esta en la carpeta correcta!" -ForegroundColor Red
    exit 1
}

Write-Host "Reparando application-test.yml..." -ForegroundColor Yellow
Write-Host ""

# Crear application-test.yml SIMPLIFICADO
Write-Host "Creando application-test.yml (versión simplificada)..." -ForegroundColor Cyan

$applicationTestYml = @"
spring:
  datasource:
    url: jdbc:h2:mem:thoth_core_test
    username: sa
    password: 
    driver-class-name: org.h2.Driver

  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: false
    database-platform: org.hibernate.dialect.H2Dialect
    open-in-view: false

logging:
  level:
    root: OFF
    com.thoth: INFO
"@

$applicationTestYml | Out-File -Encoding UTF8 "src\main\resources\application-test.yml" -Force
Write-Host "   OK: application-test.yml creado" -ForegroundColor Green

Write-Host ""
Write-Host "========================================================================" -ForegroundColor Green
Write-Host "LISTO: application-test.yml reparado" -ForegroundColor Green
Write-Host "========================================================================" -ForegroundColor Green
Write-Host ""
Write-Host "Ahora ejecuta:" -ForegroundColor Yellow
Write-Host "  gradle clean" -ForegroundColor Yellow
Write-Host "  gradle build" -ForegroundColor Yellow
Write-Host "  gradle test" -ForegroundColor Yellow
Write-Host ""

Read-Host "Presione Enter para salir"
