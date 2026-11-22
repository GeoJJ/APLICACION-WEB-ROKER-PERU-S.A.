param(
    [string]$TomcatHome = $env:CATALINA_HOME,
    [ValidateRange(1024, 65535)][int]$Puerto = 8080
)

$ErrorActionPreference = 'Stop'
$null = & (Join-Path $PSScriptRoot 'seleccionar-jdk.ps1')
$proyectoRoker = Split-Path -Parent $PSScriptRoot
$warRoker = Join-Path $proyectoRoker 'backend/target/ROOT.war'
if (-not (Test-Path $warRoker)) { throw 'Primero ejecutar scripts/generar-war.ps1.' }
if (-not $TomcatHome -or -not (Test-Path (Join-Path $TomcatHome 'bin/catalina.bat'))) {
    throw 'Indica -TomcatHome con la carpeta de Apache Tomcat 11 o define CATALINA_HOME.'
}
if (-not (Select-String -Path (Join-Path $TomcatHome 'RELEASE-NOTES') -Pattern 'Apache Tomcat Version 11\.')) {
    throw 'Este proyecto requiere Tomcat 11, compatible con Servlet 6.1.'
}
if (Get-NetTCPConnection -LocalPort $Puerto -State Listen -ErrorAction SilentlyContinue) {
    throw "El puerto $Puerto está ocupado. Detén el backend anterior o elige otro puerto."
}
if (-not $env:SPRING_DATASOURCE_PASSWORD) {
    $claveRoker = Read-Host 'Contraseña de PostgreSQL para Roker' -AsSecureString
    $env:SPRING_DATASOURCE_PASSWORD = [System.Net.NetworkCredential]::new('', $claveRoker).Password
}
if (-not $env:JWT_SECRET) {
    $bytesRoker = New-Object byte[] 32
    $rngRoker = [System.Security.Cryptography.RandomNumberGenerator]::Create()
    $rngRoker.GetBytes($bytesRoker)
    $rngRoker.Dispose()
    $env:JWT_SECRET = [BitConverter]::ToString($bytesRoker).Replace('-', '')
    Write-Host 'Se generó una clave JWT temporal; los tokens anteriores no serán válidos.'
}

# Instancia dedicada: no sobrescribe las aplicaciones del Tomcat instalado.
$baseRoker = [IO.Path]::GetFullPath((Join-Path $proyectoRoker '.local/tomcat'))
New-Item -ItemType Directory -Path $baseRoker -Force | Out-Null
if (-not (Test-Path (Join-Path $baseRoker 'conf'))) {
    Copy-Item -LiteralPath (Join-Path $TomcatHome 'conf') -Destination $baseRoker -Recurse
}
foreach ($carpetaRoker in @('webapps', 'logs', 'temp', 'work')) {
    New-Item -ItemType Directory -Path (Join-Path $baseRoker $carpetaRoker) -Force | Out-Null
}
$servidorRoker = Join-Path $baseRoker 'conf/server.xml'
[xml]$xmlRoker = Get-Content -LiteralPath $servidorRoker
$xmlRoker.Server.SetAttribute('port', '-1')
$conectorRoker = $xmlRoker.SelectSingleNode('//Connector[not(@SSLEnabled="true")]')
$conectorRoker.SetAttribute('port', [string]$Puerto)
$conectorRoker.SetAttribute('address', '127.0.0.1')
$xmlRoker.Save($servidorRoker)
Copy-Item -LiteralPath $warRoker -Destination (Join-Path $baseRoker 'webapps/ROOT.war') -Force
(Get-Item -LiteralPath (Join-Path $baseRoker 'webapps/ROOT.war')).LastWriteTimeUtc = [DateTime]::UtcNow
$env:CATALINA_HOME = [IO.Path]::GetFullPath($TomcatHome)
$env:CATALINA_BASE = $baseRoker
Write-Host "Desplegando Roker en http://localhost:$Puerto/. Para detenerlo: Ctrl+C."
& (Join-Path $env:CATALINA_HOME 'bin/catalina.bat') run
exit $LASTEXITCODE
