param([string]$UrlBasePruebas = 'jdbc:postgresql://localhost:5432/roker_bd_integracion_tests')

$ErrorActionPreference = 'Stop'
$javaMayorRoker = & (Join-Path $PSScriptRoot 'seleccionar-jdk.ps1')
$proyectoRoker = Split-Path -Parent $PSScriptRoot
$urlAnteriorRoker = $env:SPRING_DATASOURCE_URL
$jwtAnteriorRoker = $env:JWT_SECRET
$env:SPRING_DATASOURCE_URL = $UrlBasePruebas
if (-not $env:SPRING_DATASOURCE_PASSWORD) {
    $claveRoker = Read-Host 'Contraseña de PostgreSQL para la base de pruebas' -AsSecureString
    $env:SPRING_DATASOURCE_PASSWORD = [System.Net.NetworkCredential]::new('', $claveRoker).Password
}
if (-not $env:JWT_SECRET) {
    $bytesRoker = New-Object byte[] 32
    $rngRoker = [System.Security.Cryptography.RandomNumberGenerator]::Create()
    $rngRoker.GetBytes($bytesRoker)
    $rngRoker.Dispose()
    $env:JWT_SECRET = [BitConverter]::ToString($bytesRoker).Replace('-', '')
}
Push-Location (Join-Path $proyectoRoker 'backend')
try {
    & .\mvnw.cmd "-Djava.version=$javaMayorRoker" clean package
    if ($LASTEXITCODE -ne 0) { throw 'Falló la compilación o alguna prueba. No desplegar el paquete.' }
    $warRoker = Join-Path (Get-Location) 'target/ROOT.war'
    $hashRoker = (Get-FileHash $warRoker -Algorithm SHA256).Hash.ToLowerInvariant()
    "$hashRoker  ROOT.war" | Set-Content 'target/ROOT.war.sha256' -Encoding ASCII
    Write-Host "WAR generado: $warRoker"
} finally {
    Pop-Location
    $env:SPRING_DATASOURCE_URL = $urlAnteriorRoker
    $env:JWT_SECRET = $jwtAnteriorRoker
}
