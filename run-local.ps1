<#
  Arranca el backend en local cargando las variables de .env en la sesion de
  PowerShell actual. Spring Boot NO lee .env por si mismo: sin esto, las
  variables quedan como ${MYSQLHOST} literales y falla la conexion a MySQL.

  Uso:
    .\run-local.ps1
    .\run-local.ps1 -SkipEnv     # si las variables ya estan en la sesion
#>
param(
    [switch]$SkipEnv
)

$ErrorActionPreference = 'Stop'
Set-Location -Path $PSScriptRoot

if (-not $SkipEnv) {
    $envFile = Join-Path $PSScriptRoot '.env'
    if (-not (Test-Path $envFile)) {
        throw "No existe .env. Copia .env.example a .env y completa los valores."
    }

    # ADMIN_PASSWORD_HASH tiene prioridad; si estan ambas, el backend rechaza
    # arrancar ("Configura solo ADMIN_PASSWORD_HASH o ADMIN_PASSWORD, no ambos").
    $pairs = @{}
    foreach ($line in Get-Content $envFile) {
        if ($line -match '^\s*#' -or $line -notmatch '=') { continue }
        $key = $line.Split('=')[0].Trim()
        $value = $line.Substring($line.IndexOf('=') + 1).Trim().Trim('"').Trim("'")
        if ($key) { $pairs[$key] = $value }
    }

    if ($pairs.ContainsKey('ADMIN_PASSWORD_HASH') -and -not [string]::IsNullOrWhiteSpace($pairs['ADMIN_PASSWORD_HASH'])) {
        $pairs.Remove('ADMIN_PASSWORD')
    }

    foreach ($key in $pairs.Keys) {
        # Solo en el proceso actual: nada se persiste en el equipo.
        Set-Item -Path "Env:$key" -Value $pairs[$key]
    }

    $required = 'MYSQLHOST', 'MYSQLDATABASE', 'MYSQLUSER', 'MYSQLPASSWORD', 'JWT_SECRET'
    $missing = $required | Where-Object { [string]::IsNullOrWhiteSpace($pairs[$_]) }
    if ($missing) {
        throw "Faltan en .env: $($missing -join ', ')"
    }

    # Nunca se imprime el valor: solo confirma que la variable existe.
    Write-Host "Variables cargadas desde .env: $($pairs.Count)" -ForegroundColor Green
    foreach ($key in $required) {
        Write-Host ("  {0,-16} {1}" -f $key, $pairs[$key].Length) -ForegroundColor DarkGray
    }
}

Write-Host "Arrancando el backend..." -ForegroundColor Cyan
& .\mvnw.cmd spring-boot:run