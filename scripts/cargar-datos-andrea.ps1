param([string]$ApiUrl = 'http://localhost:8080/api')

$ErrorActionPreference = 'Stop'
$correoAnteriorRoker = $env:ROKER_ADMIN_EMAIL
$claveAnteriorRoker = $env:ROKER_ADMIN_PASSWORD
$urlAnteriorRoker = $env:ROKER_API_URL
try {
    $env:ROKER_API_URL = $ApiUrl
    if (-not $env:ROKER_ADMIN_EMAIL) { $env:ROKER_ADMIN_EMAIL = Read-Host 'Correo del administrador local' }
    if (-not $env:ROKER_ADMIN_PASSWORD) {
        $claveRoker = Read-Host 'Contraseña del administrador local' -AsSecureString
        $env:ROKER_ADMIN_PASSWORD = [System.Net.NetworkCredential]::new('', $claveRoker).Password
    }
    & node (Join-Path $PSScriptRoot 'cargar-datos-andrea.mjs')
    if ($LASTEXITCODE -ne 0) { throw 'La carga se interrumpió. Revisar el error antes de continuar.' }
} finally {
    $env:ROKER_ADMIN_EMAIL = $correoAnteriorRoker
    $env:ROKER_ADMIN_PASSWORD = $claveAnteriorRoker
    $env:ROKER_API_URL = $urlAnteriorRoker
}
