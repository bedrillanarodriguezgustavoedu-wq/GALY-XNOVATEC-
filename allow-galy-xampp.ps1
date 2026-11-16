$ErrorActionPreference = 'Stop'

$principal = New-Object Security.Principal.WindowsPrincipal(
    [Security.Principal.WindowsIdentity]::GetCurrent()
)

if (-not $principal.IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)) {
    Write-Host 'Este script necesita permisos de administrador.' -ForegroundColor Yellow
    Write-Host 'Cierra esta ventana, haz clic derecho sobre el archivo y selecciona:' -ForegroundColor Yellow
    Write-Host '"Ejecutar con PowerShell" como administrador.' -ForegroundColor Yellow
    Read-Host 'Presiona Enter para salir'
    exit 1
}

$ruleName = 'GALY XAMPP HTTP 80'
$rule = Get-NetFirewallRule -DisplayName $ruleName -ErrorAction SilentlyContinue

if (-not $rule) {
    New-NetFirewallRule `
        -DisplayName $ruleName `
        -Direction Inbound `
        -Action Allow `
        -Protocol TCP `
        -LocalPort 80 `
        -Profile Private | Out-Null
    Write-Host 'Regla del Firewall creada.' -ForegroundColor Green
} else {
    Write-Host 'La regla del Firewall ya existia.' -ForegroundColor Green
}

$apache = Get-Service -Name Apache2.4 -ErrorAction SilentlyContinue
if ($apache -and $apache.Status -ne 'Running') {
    Start-Service -Name Apache2.4
}

try {
    $response = Invoke-WebRequest `
        -UseBasicParsing `
        -Uri 'http://localhost/galy-admin/admin_web.php?section=dashboard' `
        -TimeoutSec 10
    Write-Host "Panel local disponible: HTTP $($response.StatusCode)" -ForegroundColor Green
} catch {
    Write-Host 'Apache no responde en localhost. Inicia Apache desde XAMPP.' -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    exit 1
}

Write-Host ''
Write-Host 'Ahora prueba en el emulador Android:' -ForegroundColor Cyan
Write-Host 'http://10.0.2.2/galy-admin/admin_web.php?section=dashboard' -ForegroundColor Cyan
Read-Host 'Presiona Enter para cerrar'
