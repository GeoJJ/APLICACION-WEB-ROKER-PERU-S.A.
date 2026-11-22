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

# Usa JAVA_HOME o el JDK de mayor versión instalado en las carpetas habituales.
$jdkRoker = $env:JAVA_HOME
if (-not $jdkRoker) {
    $candidatosRoker = @()
    foreach ($carpetaRoker in @((Join-Path $env:USERPROFILE '.jdks'), (Join-Path $env:ProgramFiles 'Java'))) {
        if (Test-Path $carpetaRoker) {
            $candidatosRoker += Get-ChildItem $carpetaRoker -Directory | ForEach-Object {
                $releaseRoker = Join-Path $_.FullName 'release'
                if ((Test-Path $releaseRoker) -and (Test-Path (Join-Path $_.FullName 'bin/javac.exe'))) {
                    $versionRoker = Select-String -Path $releaseRoker -Pattern '^JAVA_VERSION="(\d+)'
                    if ($versionRoker) {
                        [pscustomobject]@{ Ruta = $_.FullName; Mayor = [int]$versionRoker.Matches[0].Groups[1].Value }
                    }
                }
            }
        }
    }
    $jdkRoker = ($candidatosRoker | Where-Object Mayor -ge 17 | Sort-Object Mayor -Descending | Select-Object -First 1).Ruta
}
if (-not $jdkRoker -or -not (Test-Path (Join-Path $jdkRoker 'bin/javac.exe'))) {
    throw 'Configura JAVA_HOME con la carpeta de un JDK 17 o superior.'
}
$releaseRoker = Select-String -Path (Join-Path $jdkRoker 'release') -Pattern '^JAVA_VERSION="(\d+)'
$javaMayorRoker = [int]$releaseRoker.Matches[0].Groups[1].Value
if ($javaMayorRoker -lt 17) { throw 'Spring Boot requiere Java 17 o superior.' }
$env:JAVA_HOME = $jdkRoker
$env:Path = "$(Join-Path $jdkRoker 'bin');$env:Path"
if (-not $env:SPRING_DATASOURCE_PASSWORD) {
    $claveRoker = Read-Host 'Contraseña de PostgreSQL para esta ejecución' -AsSecureString
    $env:SPRING_DATASOURCE_PASSWORD = [System.Net.NetworkCredential]::new('', $claveRoker).Password
}
$puertoRoker = if ($env:SERVER_PORT) { $env:SERVER_PORT } else { '8080' }
Write-Host "Iniciando Tomcat integrado con Java $javaMayorRoker. Puerto: $puertoRoker"
Push-Location (Join-Path $proyectoRoker 'backend')
try {
    # Evita mezclar clases compiladas con distintas versiones de Java.
    & .\mvnw.cmd "-Djava.version=$javaMayorRoker" '-Dspring-boot.run.profiles=local' clean spring-boot:run
    exit $LASTEXITCODE
} finally { Pop-Location }
