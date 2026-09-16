param([switch]$Uninstall)
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$driver = Join-Path $root 'driver\EchoDeckVirtualAudio.inf'
if (-not ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)) { throw 'Execute como Administrador somente para instalar ou remover o componente de áudio.' }
if ($Uninstall) {
  Write-Host 'O pacote de driver assinado precisa estar presente para remoção PnP.'
  Get-WindowsDriver -Online | Where-Object {$_.OriginalFileName -like '*EchoDeck*'} | ForEach-Object { pnputil.exe /delete-driver $_.PublishedName /uninstall /force }
  exit $LASTEXITCODE
}
if (-not (Test-Path $driver)) { throw 'Pacote EchoDeckVirtualAudio.inf/.sys/.cat assinado não encontrado. Nenhum dispositivo falso será criado.' }
$signature = Get-AuthenticodeSignature $driver
if ($signature.Status -ne 'Valid') { throw 'O pacote do componente não possui assinatura válida. Instalação interrompida.' }
pnputil.exe /add-driver $driver /install
if ($LASTEXITCODE -ne 0) { throw 'O Windows recusou a instalação do componente de áudio.' }
Write-Host 'Componente de áudio EchoDeck instalado. Reinicie o Audio Assistant para validar o endpoint.'
