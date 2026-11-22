param(
    [ValidateSet('Backend', 'Frontend')]
    [string]$Componente = 'Backend'
)

$ErrorActionPreference = 'Stop'
$proyectoRoker = Split-Path -Parent $PSScriptRoot
if ($Componente -eq 'Frontend') {
    Push-Location (Join-Path $proyectoRoker 'frontend')
    try {
        if (-not (Test-Path 'node_modules')) {
            & npm.cmd ci --no-audit --no-fund
            if ($LASTEXITCODE -ne 0) { throw 'No se pudieron instalar las dependencias.' }
        }
        & npm.cmd run dev -- --host 127.0.0.1
        exit $LASTEXITCODE
    } finally { Pop-Location }
}

$javaMayorRoker = & (Join-Path $PSScriptRoot 'seleccionar-jdk.ps1')
if (-not $env:SPRING_DATASOURCE_PASSWORD) {
    $claveRoker = Read-Host 'Contraseña de PostgreSQL para esta ejecución' -AsSecureString
    $env:SPRING_DATASOURCE_PASSWORD = [System.Net.NetworkCredential]::new('', $claveRoker).Password
}
$puertoRoker = if ($env:SERVER_PORT) { $env:SERVER_PORT } else { '8080' }
Write-Host "Iniciando Tomcat integrado con Java $javaMayorRoker. Puerto: $puertoRoker"
Push-Location (Join-Path $proyectoRoker 'backend')
try {
    # Evita mezclar clases compiladas con distintas versiones de Java.
    & .\mvnw.cmd "-Djava.version=$javaMayorRoker" '-Dspring-boot.run.profiles=local' '-Dfrontend.skip=true' clean spring-boot:run
    exit $LASTEXITCODE
} finally { Pop-Location }
