[CmdletBinding()]
param(
    [string] $ProjectRoot
)

$ErrorActionPreference = 'Stop'
if ([string]::IsNullOrWhiteSpace($ProjectRoot)) {
    if ([string]::IsNullOrWhiteSpace($PSScriptRoot)) { $ProjectRoot = 'C:\PhilcoOS' }
    else { $ProjectRoot = Split-Path -Parent $PSScriptRoot }
}
$ProjectRoot = [IO.Path]::GetFullPath($ProjectRoot)
$baseline = [IO.Path]::GetFullPath((Join-Path $ProjectRoot 'build\system_stage'))
$target = [IO.Path]::GetFullPath((Join-Path $ProjectRoot 'build\system_stage_slim'))
$manifestPath = Join-Path $ProjectRoot 'analysis\system_manifest.json'
$tvBuildInfoPath = Join-Path $ProjectRoot 'analysis\tv_build_info.txt'
$tvRuntimeInfoPath = Join-Path $ProjectRoot 'analysis\tv_info.txt'
$firmwareKernelPath = Join-Path $ProjectRoot 'extracted\factory_baseline\firmware\package4\vmlinux.develop.android.kk.rtd298x.tv001.emmc.bin'
$statePath = Join-Path $ProjectRoot 'analysis\slim_stage_state.json'
$reportPath = Join-Path $ProjectRoot 'analysis\slim_stage_report.md'
$apk = Join-Path $ProjectRoot 'source\philco-home\build\outputs\apk\release\philco-home-release.apk'
$knownGoodSha256 = '8D555DB8AA3F2BDEC8CF549DD2626A66F1E515135867ABDD5213E12B47A78CC4'
$zipalign = Join-Path $ProjectRoot '.toolchain\android-sdk\build-tools\35.0.0\zipalign.exe'

if (-not (Test-Path -LiteralPath $baseline -PathType Container)) { throw "Baseline missing: $baseline" }
if (-not (Test-Path -LiteralPath $manifestPath -PathType Leaf)) { throw "System manifest missing: $manifestPath" }
if (-not (Test-Path -LiteralPath $apk -PathType Leaf)) { throw "Release APK missing: $apk" }
if (-not (Test-Path -LiteralPath $zipalign -PathType Leaf)) { throw "zipalign missing: $zipalign" }

# These nine APKs are the only removals authorized by the evidence-based Tier 1 list.
$safeApks = @(
    'app\BasicDreams.apk', 'app\Galaxy4.apk', 'app\LiveWallpapersPicker.apk',
    'app\NoiseField.apk', 'app\PhotoTable.apk', 'app\SelfTest.apk',
    'priv-app\OneClickCleaner.apk', 'priv-app\QjyBrowser.apk', 'priv-app\aptoide.apk'
)
# The audit found no static DT_NEEDED references for these four codecs. Keep them listed separately.
$safeLibs = @(
    'lib\libwechatvoicereco.so', 'lib\libwechatvoicesilk.so',
    'lib\libwechatvoicesilk_v5.so', 'lib\libwechatvoicesilk_v7a.so'
)
$criticalApks = @(
    'app\LancherProject2995.apk', 'app\SourceMenu.apk', 'app\TSB_Tv.apk',
    'app\com.cvte.keyservice.apk', 'app\DownloadProviderUi.apk', 'app\KeyChain.apk',
    'app\PackageInstaller.apk', 'app\TelephonyProvider.apk', 'app\Provision.apk',
    'priv-app\DefaultContainerService.apk', 'priv-app\DownloadProvider.apk',
    'priv-app\ExternalStorageProvider.apk', 'priv-app\InputDevices.apk',
    'priv-app\Keyguard.apk', 'priv-app\MediaProvider.apk', 'priv-app\RTKSettings.apk',
    'priv-app\SettingsProvider.apk', 'priv-app\SystemUI.apk'
)

if (-not (Test-Path -LiteralPath $target -PathType Container)) {
    [IO.Directory]::CreateDirectory($target) | Out-Null
    $state = [ordered]@{ schema = 1; status = 'copying'; baseline = $baseline; target = $target; created_utc = [DateTime]::UtcNow.ToString('o') }
    $state | ConvertTo-Json | Set-Content -LiteralPath $statePath -Encoding UTF8
} else {
    if (-not (Test-Path -LiteralPath $statePath -PathType Leaf)) {
        throw "Slim target exists without its state file; refusing to overwrite or infer provenance: $target"
    }
    $state = Get-Content -LiteralPath $statePath -Raw | ConvertFrom-Json
    if ($state.baseline -ne $baseline -or $state.target -ne $target) { throw 'Slim state belongs to a different baseline or target.' }
}

# Complete an interrupted first copy without overwriting files in the work copy.
Get-ChildItem -LiteralPath $baseline -Force -Recurse | Where-Object { $_.PSIsContainer } | ForEach-Object {
    $relative = $_.FullName.Substring($baseline.Length).TrimStart('\')
    $destination = Join-Path $target $relative
    if (-not (Test-Path -LiteralPath $destination -PathType Container)) { [IO.Directory]::CreateDirectory($destination) | Out-Null }
}
Get-ChildItem -LiteralPath $baseline -File -Force -Recurse | ForEach-Object {
    $relative = $_.FullName.Substring($baseline.Length).TrimStart('\')
    $destination = Join-Path $target $relative
    if (-not (Test-Path -LiteralPath $destination -PathType Leaf)) {
        $parent = Split-Path -Parent $destination
        [IO.Directory]::CreateDirectory($parent) | Out-Null
        Copy-Item -LiteralPath $_.FullName -Destination $destination
    }
}

$manifest = Get-Content -LiteralPath $manifestPath -Raw | ConvertFrom-Json
$baselineApkPaths = @($manifest.applications | ForEach-Object { $_.path -replace '^system/', '' })
foreach ($path in $safeApks) {
    if ($baselineApkPaths -notcontains ($path -replace '\\','/')) { throw "Safe-removal APK is absent from manifest: $path" }
    $sourceFile = Join-Path $baseline ($path -replace '/', '\')
    $slimFile = Join-Path $target ($path -replace '/', '\')
    if (-not (Test-Path -LiteralPath $sourceFile -PathType Leaf)) { throw "Safe-removal APK is absent from baseline: $path" }
    if (Test-Path -LiteralPath $slimFile -PathType Leaf) { Remove-Item -LiteralPath $slimFile }
}
foreach ($path in $safeLibs) {
    $sourceFile = Join-Path $baseline ($path -replace '/', '\')
    $slimFile = Join-Path $target ($path -replace '/', '\')
    if (-not (Test-Path -LiteralPath $sourceFile -PathType Leaf)) { throw "Safe-removal library is absent from baseline: $path" }
    if (Test-Path -LiteralPath $slimFile -PathType Leaf) { Remove-Item -LiteralPath $slimFile }
}

& $zipalign -c -v 4 $apk | Out-Null
if ($LASTEXITCODE -ne 0) { throw 'Release APK is not zip aligned.' }
$launcherRelative = 'app\PhilcoUltra777\PhilcoUltra777.apk'
$launcherTarget = Join-Path $target $launcherRelative
[IO.Directory]::CreateDirectory((Split-Path -Parent $launcherTarget)) | Out-Null
$apkHash = (Get-FileHash -LiteralPath $apk -Algorithm SHA256).Hash
$smartTubeDexCheck = $false
Add-Type -AssemblyName System.IO.Compression.FileSystem
$apkZip = [IO.Compression.ZipFile]::OpenRead($apk)
try {
    $dexEntry = $apkZip.GetEntry('classes.dex')
    if ($null -eq $dexEntry) { throw 'Release APK has no classes.dex.' }
    $dexBuffer = [IO.MemoryStream]::new()
    $dexStream = $dexEntry.Open()
    try { $dexStream.CopyTo($dexBuffer) } finally { $dexStream.Dispose() }
    $dexText = [Text.Encoding]::UTF8.GetString($dexBuffer.ToArray())
    $smartTubeDexCheck = $dexText.Contains('org.smarttube.stable') -and $dexText.Contains('getLaunchIntentForPackage')
    $dexBuffer.Dispose()
} finally { $apkZip.Dispose() }
if (-not $smartTubeDexCheck) { throw 'Release APK does not contain the expected SmartTube launch route.' }
$sourceMainActivity = Join-Path $ProjectRoot 'source\philco-home\src\com\philco\ultra777\home\MainActivity.java'
$sourceMainActivityHash = if (Test-Path -LiteralPath $sourceMainActivity -PathType Leaf) { (Get-FileHash -LiteralPath $sourceMainActivity -Algorithm SHA256).Hash } else { 'unavailable' }
$tvBuildRaw = Get-Content -LiteralPath $tvBuildInfoPath -Raw
$tvFingerprintMatch = [regex]::Match($tvBuildRaw, '(?m)^realtek/[^\r\n]+')
$tvFingerprint = if ($tvFingerprintMatch.Success) { $tvFingerprintMatch.Value } else { 'unavailable' }
$packageFingerprintMatch = [regex]::Match((Get-Content -LiteralPath (Join-Path $baseline 'build.prop') -Raw), '(?m)^ro\.build\.fingerprint=(.+)$')
$packageFingerprint = if ($packageFingerprintMatch.Success) { $packageFingerprintMatch.Groups[1].Value } else { 'unavailable' }
$tvRuntimeRaw = Get-Content -LiteralPath $tvRuntimeInfoPath -Raw
$tvKernelMatch = [regex]::Match($tvRuntimeRaw, '(?m)^Linux version [^\r\n]+')
$tvKernel = if ($tvKernelMatch.Success) { $tvKernelMatch.Value } else { 'unavailable' }
$packageKernel = if (Test-Path -LiteralPath $firmwareKernelPath -PathType Leaf) {
    $kernelRaw = [IO.File]::ReadAllText($firmwareKernelPath, [Text.Encoding]::ASCII)
    $kernelMatch = [regex]::Match($kernelRaw, 'Linux version [^\x00\r\n]{1,220}')
    if ($kernelMatch.Success) { $kernelMatch.Value } else { 'unavailable' }
} else { 'unavailable' }
if (Test-Path -LiteralPath $launcherTarget -PathType Leaf) {
    if ((Get-FileHash -LiteralPath $launcherTarget -Algorithm SHA256).Hash -ne $apkHash) {
        throw "Existing launcher differs; refusing to overwrite: $launcherTarget"
    }
} else {
    Copy-Item -LiteralPath $apk -Destination $launcherTarget
}

$currentApks = @(Get-ChildItem -LiteralPath (Join-Path $target 'app'),(Join-Path $target 'priv-app') -File -Filter '*.apk' -Recurse)
$groups = foreach ($app in $manifest.applications) {
    $rel = $app.path -replace '^system/', ''
    $normalized = $rel -replace '/', '\'
    if ($safeApks -contains $normalized) { $classification = 'BLOATWARE_TIER1_REMOVED' }
    elseif ($criticalApks -contains $normalized) { $classification = 'CRITICAL_KEEP' }
    else { $classification = 'UNCERTAIN_KEEP' }
    $serviceList = @($app.services | ForEach-Object { [string]$_ }) -join '; '
    [pscustomobject]@{ Path = $app.path; Package = $app.package_name; Classification = $classification; Services = $serviceList; SourceBytes = [long]$app.size }
}
$sourceBytes = [long](Get-ChildItem -LiteralPath $baseline -File -Recurse | Measure-Object -Property Length -Sum).Sum
$targetBytes = [long](Get-ChildItem -LiteralPath $target -File -Recurse | Measure-Object -Property Length -Sum).Sum
$removedBytes = [long](($safeApks | ForEach-Object { (Get-Item -LiteralPath (Join-Path $baseline ($_ -replace '/', '\'))).Length }) | Measure-Object -Sum).Sum
$removedBytes += [long](($safeLibs | ForEach-Object { (Get-Item -LiteralPath (Join-Path $baseline ($_ -replace '/', '\'))).Length }) | Measure-Object -Sum).Sum
$state.status = 'complete'
$state.completed_utc = [DateTime]::UtcNow.ToString('o')
$state.release_sha256 = $apkHash
foreach ($field in @(
    @{ Name = 'mainactivity_sha256'; Value = $sourceMainActivityHash },
    @{ Name = 'smarttube_package_literal_in_apk_dex'; Value = $smartTubeDexCheck },
    @{ Name = 'installed_tv_fingerprint'; Value = $tvFingerprint },
    @{ Name = 'package_fingerprint'; Value = $packageFingerprint },
    @{ Name = 'installed_tv_kernel'; Value = $tvKernel },
    @{ Name = 'package_kernel'; Value = $packageKernel },
    @{ Name = 'complete_installed_system_backup_available'; Value = $false }
)) {
    $state | Add-Member -MemberType NoteProperty -Name $field.Name -Value $field.Value -Force
}
$state | ConvertTo-Json | Set-Content -LiteralPath $statePath -Encoding UTF8

$lines = [Collections.Generic.List[string]]::new()
$lines.Add('# PhilcoOS slim stage report')
$lines.Add('')
$lines.Add("Generated UTC: $($state.completed_utc)")
$lines.Add("Baseline: ``$baseline``")
$lines.Add("Working copy: ``$target``")
$lines.Add("Official image: ``original\PH42F10DSGWAVA8GB.img``; TAR bundle, source SHA-256 documented as ``9BCEF48B439998947C2AC64E2C127D7A83D7FBF2FC9F2EB0579ED4C8D7EAFFC0``.")
$lines.Add('Package layout: outer POSIX TAR; `package4/system.emmc.tar` is the ext4 `/system` tree payload; `config.txt` also directs raw kernel/audio/video firmware and bootloader writes. `secure_boot=0` is in config; recorded member hashes are integrity checks, not a vendor signature verification.')
$lines.Add('Recovery path identified: boot rescue kernel `0.vmlinux.rescue.bin`, then Realtek `install_a` USB installer. Installer recreates/formats partitions and can overwrite bootloader/kernel/microcode. Recovery path has not been physically tested; no recovery success is claimed.')
$lines.Add('')
$lines.Add("Baseline file bytes: $sourceBytes; slim file bytes: $targetBytes; estimated net storage reduction including injected launcher: $($sourceBytes-$targetBytes) bytes. Tier 1 removals alone: $removedBytes bytes. Runtime RAM savings: unmeasured; disk size does not establish PSS reduction.")
$lines.Add("Launcher injected: ``$launcherRelative``; SHA-256 ``$apkHash``; zipalign 4 verified; intended archive mode ``0644`` (repacker default for new regular files). OEM ``LancherProject2995.apk`` retained as fallback.")
$lines.Add("Previously stated known-good SHA-256: ``$knownGoodSha256``. Local release matches that hash: ``$($apkHash -eq $knownGoodSha256)``. The local release was independently verified as v1-signed for API 19, but this hash mismatch means it is a different artifact than the stated known-good build.")
$lines.Add("Current ``MainActivity.java`` SHA-256: ``$sourceMainActivityHash``. Current APK DEX contains ``org.smarttube.stable`` and ``getLaunchIntentForPackage``: ``$smartTubeDexCheck``. This proves the SmartTube package route is in the local artifact; it does not prove byte-for-byte reproducible build provenance for the different APK hash.")
$lines.Add('')
$lines.Add('## Installed-TV versus official-package build')
$lines.Add('')
$lines.Add("Installed-TV evidence sources: ``analysis\tv_build_info.txt`` and ``analysis\tv_info.txt``. Fingerprint: ``$tvFingerprint``. Kernel: ``$tvKernel``.")
$lines.Add("Official-package evidence source: ``build\system_stage\build.prop`` and extracted main kernel. Fingerprint: ``$packageFingerprint``. Kernel: ``$packageKernel``.")
$lines.Add('Both report Android 4.4.2 / API 19 / KOT49H / rtd298x, but the build identifiers and kernel builds differ. The captured TV build is 2017-05-31 with kernel #192; the package stage is 2017-08-16 with kernel #172. The available TV capture has no full `/system` backup or installed-package inventory, so exact file-by-file `/system` differences cannot be asserted. The stock stage remains based on the August 2017 package.')
$lines.Add('')
$lines.Add('## Runtime memory evidence')
$lines.Add('')
$lines.Add('Historical TV snapshot in `analysis/tv_info.txt`: `MemTotal 456492 kB`, `MemFree 47532 kB`, `Active(anon) 171228 kB`. Historical `docs/PHILCOOS_ULTRA_777_GLASS_AURORA.md` reports active custom launcher total PSS `11471 kB`; that is not a current post-slim measurement. Stock launcher PSS and per-process `top` data are absent. Current ADB collection could not connect to the local ADB daemon, so current before/after RAM and process rankings remain unavailable; removal sizes are storage savings, not measured RAM savings.')
$lines.Add('')
$lines.Add('## APK inventory and service classification')
$lines.Add('')
$lines.Add('| Classification | APK | Package | Declared services |')
$lines.Add('|---|---|---|---|')
foreach ($g in $groups | Sort-Object Classification,Path) {
    $services = if ([string]::IsNullOrWhiteSpace($g.Services)) { '—' } else { $g.Services -replace '\|','\\|' }
    $lines.Add("| $($g.Classification) | ``$($g.Path)`` | ``$($g.Package)`` | $services |")
}
$lines.Add('')
$lines.Add('## Removed files')
$lines.Add('')
foreach ($path in $safeApks + $safeLibs) { $lines.Add("- ``$path``") }
$lines.Add('')
$lines.Add('## Retained critical firmware components')
$lines.Add('')
$lines.Add('- Realtek TV stack: `TSB_Tv.apk`, `SourceMenu.apk`, `RTKSettings.apk`, `com.cvte.keyservice.apk`, `/system/rtk_rootfs`, `/system/vendor`, HALs, kernel modules, `etc/init.tvserver*.sh`, tuner tables and firmware.')
$lines.Add('- Core Android providers, package installer, keychain, input devices, Keyguard, SystemUI, storage/media/download providers and LatinIME remain.')
$lines.Add('- OEM fallback launcher `LancherProject2995.apk` remains unchanged.')
$lines.Add('- All other APKs and services are `UNCERTAIN_KEEP` pending runtime dependency and boot testing.')
$lines | Set-Content -LiteralPath $reportPath -Encoding UTF8
Write-Output "Slim stage ready: $target"
Write-Output "Report: $reportPath"
Write-Output "Release SHA-256: $apkHash"
Write-Output "Storage bytes baseline=$sourceBytes slim=$targetBytes; estimated reduction=$($sourceBytes-$targetBytes); RAM savings=unmeasured"
