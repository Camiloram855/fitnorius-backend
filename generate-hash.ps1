<#
  Genera un hash BCrypt para ADMIN_PASSWORD_HASH usando el MISMO codificador
  que usa la aplicacion (BCryptPasswordEncoder con coste 12). asi el hash
  siempre es aceptado por AdminCredentials.

  Uso:
    .\generate-hash.ps1
    .\generate-hash.ps1 -Password 'MiClave#Segura2026'

  La contraseña NO se escribe en disco ni se imprime: solo el hash.
#>
param(
    [string]$Password
)

$ErrorActionPreference = 'Stop'
Set-Location -Path $PSScriptRoot

if (-not $Password) {
    # -AsSecureString evita que quede en el historial de PowerShell.
    $secure = Read-Host "Contraseña del admin" -AsSecureString
    $Password = [System.Runtime.InteropServices.Marshal]::PtrToStringBSTR(
        [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure)
    )
    if ([string]::IsNullOrWhiteSpace($Password)) {
        throw "La contraseña no puede estar vacía."
    }
}

# El classpath real sale de Maven, para usar la misma version de Spring Security.
$cpFile = Join-Path $PSScriptRoot 'target\cp-hash.txt'
& .\mvnw.cmd -q dependency:build-classpath "-Dmdep.outputFile=target\cp-hash.txt"
if (-not (Test-Path $cpFile)) {
    throw "No se pudo construir el classpath con Maven."
}
$classpath = (Get-Content $cpFile -Raw).Trim()
Remove-Item $cpFile -Force -ErrorAction SilentlyContinue

# Longitud de la clave: no se imprime, solo si cumple el minimo del backend.
Write-Host ("Contraseña: {0} caracteres" -f $Password.Length) -ForegroundColor DarkGray

$snippet = @"
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
var enc = new BCryptPasswordEncoder(12);
var hash = enc.encode("$Password");
System.out.println("HASH=" + hash);
System.out.println("VALIDA=" + enc.matches("$Password", hash));
/exit
"@

# jshell escribe parte de su salida en stderr; con Stop PowerShell lo trataria
# como error terminatingo, asi que se relaja solo durante esta llamada.
$previousPreference = $ErrorActionPreference
$ErrorActionPreference = 'Continue'
try {
    $output = $snippet | jshell --class-path $classpath -q 2>&1
} finally {
    $ErrorActionPreference = $previousPreference
}

$text = ($output | Out-String)
$line = $text -split "`n" | Where-Object { $_ -match 'HASH=' } | Select-Object -First 1
$check = $text -split "`n" | Where-Object { $_ -match 'VALIDA=' } | Select-Object -First 1

if (-not $line -or -not $check) {
    throw "jshell no devolvio un hash. Revisa que tengas JDK 17 o superior."
}

$hash = ($line -replace '^.*?HASH=', '').Trim()
$valid = ($check -replace '^.*?VALIDA=', '').Trim()

if ($valid -ne 'true') {
    throw "El hash generado no valida. No lo uses."
}

Write-Host ""
Write-Host "ADMIN_PASSWORD_HASH (copia esto a Railway):" -ForegroundColor Green
Write-Host $hash -ForegroundColor White
Write-Host ""
Write-Host "Verificado: BCrypt coste 12, prefijo `$2, valida correctamente." -ForegroundColor DarkGray
Write-Host "Recuerda: en PowerShell usa comillas simples al asignarlo." -ForegroundColor DarkGray
Write-Host "  `$env:ADMIN_PASSWORD_HASH = '$hash'" -ForegroundColor DarkGray