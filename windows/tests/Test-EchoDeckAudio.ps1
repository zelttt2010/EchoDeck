param([string]$ProbePath = "$PSScriptRoot\..\..\build\Release\EchoDeckCaptureProbe.exe")
$ErrorActionPreference = 'Stop'
Write-Host 'EchoDeck Audio validation' -ForegroundColor Cyan
$dev = Get-PnpDevice -PresentOnly | Where-Object { $_.FriendlyName -like '*EchoDeck Combined Microphone*' }
if (-not $dev) { Write-Warning 'Combined endpoint não encontrado. O driver WDK não está instalado.'; exit 10 }
if (($dev | Where-Object Status -ne 'OK')) { Write-Error 'Endpoint existe, mas o estado PnP não é OK.'; exit 11 }
Write-Host "PnP: OK ($($dev.Count) dispositivo(s))" -ForegroundColor Green
if (-not (Test-Path $ProbePath)) { Write-Error "CaptureProbe não encontrado: $ProbePath"; exit 12 }
& $ProbePath
if ($LASTEXITCODE -ne 0) { Write-Error 'O endpoint não entregou frames de captura com sinal.'; exit $LASTEXITCODE }
Write-Host 'Captura, formato e sinal: OK' -ForegroundColor Green
Write-Host 'Troca de dispositivo, reinstalação e desinstalação devem ser executadas pelo cenário de integração documentado.'
