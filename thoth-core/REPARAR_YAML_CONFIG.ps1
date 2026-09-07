# PowerShell Script - Reparar archivos YAML
# Ubicación: C:\cursos\Proyectos\thoth-core\thoth-core\
# Ejecutar: powershell.exe -ExecutionPolicy Bypass -File .\REPARAR_YAML_CONFIG.ps1

Write-Host ""
Write-Host "========================================================================" -ForegroundColor Cyan
Write-Host "THOTH C.O.R.E - REPARAR ARCHIVOS YAML" -ForegroundColor Cyan
Write-Host "========================================================================" -ForegroundColor Cyan
Write-Host ""

# Verificar ubicación
if (-not (Test-Path "build.gradle")) {
    Write-Host "ERROR: No esta en la carpeta correcta!" -ForegroundColor Red
    exit 1
}

Write-Host "Creando archivos YAML correctos..." -ForegroundColor Yellow
Write-Host ""

# 1. application.yml (COMUN - Sin datasource)
Write-Host "1. Creando application.yml..." -ForegroundColor Cyan

$applicationYml = @"
spring:
  application:
    name: thoth-core

  jackson:
    default-property-inclusion: non_null
    serialization:
      write-dates-as-timestamps: false
      indent-output: true

  jpa:
    open-in-view: false

server:
  port: 8080
  servlet:
    context-path: /api
  error:
    include-message: always
    include-binding-errors: always

logging:
  level:
    root: INFO
    com.thoth: DEBUG

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always

springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
    enabled: true

app:
  name: THOTH C.O.R.E
  version: 1.0.0
"@

$applicationYml | Out-File -Encoding UTF8 "src\main\resources\application.yml" -Force
Write-Host "   OK" -ForegroundColor Green

# 2. application-dev.yml (H2)
Write-Host "2. Creando application-dev.yml..." -ForegroundColor Cyan

$applicationDevYml = @"
spring:
  datasource:
    url: jdbc:h2:mem:thoth_core_dev
    username: sa
    password: 
    driver-class-name: org.h2.Driver
    hikari:
      maximum-pool-size: 10

  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.H2Dialect
        format_sql: true

  h2:
    console:
      enabled: true
      path: /h2-console

logging:
  level:
    root: WARN
    com.thoth: DEBUG
    org.springframework.web: DEBUG
    org.hibernate.SQL: DEBUG
"@

$applicationDevYml | Out-File -Encoding UTF8 "src\main\resources\application-dev.yml" -Force
Write-Host "   OK" -ForegroundColor Green

# 3. application-test.yml (H2)
Write-Host "3. Creando application-test.yml..." -ForegroundColor Cyan

$applicationTestYml = @"
spring:
  datasource:
    url: jdbc:h2:mem:thoth_core_test
    username: sa
    password: 
    driver-class-name: org.h2.Driver
    hikari:
      maximum-pool-size: 5

  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.H2Dialect
        format_sql: true

logging:
  level:
    root: WARN
    com.thoth: INFO
"@

$applicationTestYml | Out-File -Encoding UTF8 "src\main\resources\application-test.yml" -Force
Write-Host "   OK" -ForegroundColor Green

# 4. application-prod.yml (PostgreSQL)
Write-Host "4. Creando application-prod.yml..." -ForegroundColor Cyan

$applicationProdYml = @"
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/thoth_core
    username: postgres
    password: root
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5

  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
        jdbc:
          batch_size: 20

logging:
  level:
    root: WARN
    com.thoth: INFO
  file:
    name: logs/thoth-core.log
    max-size: 10MB
    max-history: 30
"@

$applicationProdYml | Out-File -Encoding UTF8 "src\main\resources\application-prod.yml" -Force
Write-Host "   OK" -ForegroundColor Green

Write-Host ""
Write-Host "========================================================================" -ForegroundColor Green
Write-Host "LISTO: Archivos YAML corregidos" -ForegroundColor Green
Write-Host "========================================================================" -ForegroundColor Green
Write-Host ""
Write-Host "Archivos creados:" -ForegroundColor Yellow
Write-Host "  - src/main/resources/application.yml" -ForegroundColor Yellow
Write-Host "  - src/main/resources/application-dev.yml" -ForegroundColor Yellow
Write-Host "  - src/main/resources/application-test.yml" -ForegroundColor Yellow
Write-Host "  - src/main/resources/application-prod.yml" -ForegroundColor Yellow
Write-Host ""
Write-Host "Ahora ejecuta:" -ForegroundColor Yellow
Write-Host "  gradle clean" -ForegroundColor Yellow
Write-Host "  gradle build" -ForegroundColor Yellow
Write-Host "  gradle test" -ForegroundColor Yellow
Write-Host ""

Read-Host "Presione Enter para salir"
