$ErrorActionPreference = 'Stop'
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
Write-Output $javaMayorRoker
