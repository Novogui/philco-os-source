[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string] $TargetSystemRoot,
    [switch] $Apply,
    [string] $ReleaseApk
)

$ErrorActionPreference = 'Stop'
$projectRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$script:ResolvedReleaseApk = if ([string]::IsNullOrWhiteSpace($ReleaseApk)) {
    Join-Path $projectRoot 'source\philco-home\build\outputs\apk\release\philco-home-release.apk'
} else { $ReleaseApk }
$target = [IO.Path]::GetFullPath($TargetSystemRoot)
$protectedRoots = @(
    [IO.Path]::GetFullPath((Join-Path $projectRoot 'build\system_stage')),
    [IO.Path]::GetFullPath((Join-Path $projectRoot 'original')),
    [IO.Path]::GetFullPath((Join-Path $projectRoot 'extracted\factory_baseline'))
)
if ($Apply -and ($protectedRoots | Where-Object { $target.Equals($_, [StringComparison]::OrdinalIgnoreCase) -or
        $target.StartsWith($_ + [IO.Path]::DirectorySeparatorChar, [StringComparison]::OrdinalIgnoreCase) -or
        $_.StartsWith($target.TrimEnd([IO.Path]::DirectorySeparatorChar) + [IO.Path]::DirectorySeparatorChar,
            [StringComparison]::OrdinalIgnoreCase) })) {
    throw "Refusing to integrate into protected firmware/baseline path: $target"
}
if (-not (Test-Path -LiteralPath $target -PathType Container)) {
    throw "Target must be an existing disposable system-root copy: $target"
}
$apk = [IO.Path]::GetFullPath($script:ResolvedReleaseApk)
if (-not (Test-Path -LiteralPath $apk -PathType Leaf)) { throw "Release APK not found: $apk" }
$zipalign = Join-Path $projectRoot '.toolchain\android-sdk\build-tools\35.0.0\zipalign.exe'
if (Test-Path -LiteralPath $zipalign -PathType Leaf) {
    & $zipalign -c -v 4 $apk | Out-Null
    if ($LASTEXITCODE -ne 0) { throw 'Release APK is not zip aligned.' }
} else { throw "zipalign is unavailable: $zipalign" }
$appRoot = Join-Path $target 'app'
$privRoot = Join-Path $target 'priv-app'
if (Test-Path -LiteralPath $appRoot -PathType Container) { $partitionAppRoot = $appRoot }
elseif (Test-Path -LiteralPath $privRoot -PathType Container) { $partitionAppRoot = $privRoot }
else { throw 'Target has neither app nor priv-app; refusing to guess the firmware layout.' }

$launcher = Get-ChildItem -LiteralPath $partitionAppRoot -File -ErrorAction SilentlyContinue |
    Where-Object { $_.Name -match '^(Lancher|Launcher).*\.apk$' } | Select-Object -First 1
$destinationDir = Join-Path $partitionAppRoot 'PhilcoUltra777'
$destination = Join-Path $destinationDir 'PhilcoUltra777.apk'
$backup = if ($launcher) { $launcher.FullName + '.philcoos.bak' } else { $null }
$hash = (Get-FileHash -LiteralPath $apk -Algorithm SHA256).Hash

Write-Output 'DRY-RUN integration plan (Unix file mode for APK: 0644; directory: 0755)'
Write-Output "APK: $apk"
Write-Output "SHA256: $hash"
Write-Output "Destination: $destination"
if ($launcher) { Write-Output "OEM launcher backup: $backup" }
else { Write-Output 'OEM launcher: not found in selected app directory; no backup will be made.' }
if (-not $Apply) { Write-Output 'No files changed. Pass -Apply only with a disposable integration copy.'; return }

if ($launcher -and -not (Test-Path -LiteralPath $backup)) {
    Copy-Item -LiteralPath $launcher.FullName -Destination $backup
} elseif ($launcher -and (Get-FileHash -LiteralPath $launcher.FullName -Algorithm SHA256).Hash -ne
        (Get-FileHash -LiteralPath $backup -Algorithm SHA256).Hash) {
    throw "Existing OEM backup differs; refusing to overwrite: $backup"
}
if (-not (Test-Path -LiteralPath $destinationDir -PathType Container)) {
    New-Item -ItemType Directory -Path $destinationDir | Out-Null
}
$needsCopy = -not (Test-Path -LiteralPath $destination) -or
    (Get-FileHash -LiteralPath $destination -Algorithm SHA256).Hash -ne $hash
if ($needsCopy) { Copy-Item -LiteralPath $apk -Destination $destination -Force }
Write-Output "Integrated APK at $destination (SHA256 $hash)."
Write-Output 'When repacked, the existing repacker defaults new APK files to mode 0644 and directories to 0755.'
