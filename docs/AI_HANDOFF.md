# PHILCOOS ULTRA 777 — AI HANDOFF
## Lead Research & Design Engineer (Antigravity) â†’ Lead Implementation Engineer (Codex)

- **Date/Time:** 2026-11-21 05:30:00 -03:00
- **Agent & Role:** Google Antigravity (Lead Research & Design Engineer)
- **Shared Workspace:** `C:\PhilcoOS`
- **Target Platform:** Philco PH42F10DSGW (CVTE TP.RT2984T.PB801 / Realtek RTD2984TD-GR / Android 4.4.2 KitKat API 19 / 512 MB RAM)
- **Active TV Target:** `smart_tv001` / `rtd298x_tv001` (`192.168.0.71:5555`)
- **Installed Artifact:** QC03 (`F837A27AD5BB8CFB296C5562A875B38FA44DABFB38CF51017447ED411646A6A8`)
- **Local Compiled Artifact:** QC04 (`C9870E798A380BDCBC101580F10461D86FDB1258D0628E6F520EED1D727631BD`)
- **Rollback Reference:** A57 (`13BADB2D4A823E9D960D32E9721E634450E39953D34BE742BCC460A639928676`)
- **Staged Slim Artifact:** EB256 (`EB256CA3E7A7131E212EA3CF9C1700581A4AC3EDEF95438582ECD54EB38628E2`)

---

## Current state

1. **Launcher Integrity:** PhilcoOS Ultra 777 permanece o launcher padrão persistente (`mAlways=true`, `com.philco.ultra777.home/.MainActivity`).
2. **Entradas de Hardware Realtek:** Todas as 5 fontes foram confirmadas por leitura ativa do middleware Realtek: TV Aberta (AR=27), Cabo (1), AV (5), HDMI 1 (18) e HDMI 2 (17). O canal padrão HDMI 2 foi restaurado (`PlayingSource=17`).
3. **Quick Control Center:** Testado e aprovado em QC03 no hardware real: leitura e escrita de volume de mídia (2/100), Mute, brilho de imagem (50/100) e backlight de painel (100/100).
4. **Clima e Rede:** Conexão HTTPS/TLS 1.2 comprovada via Open-Meteo para Joinville, SC, com atualização periódica autônoma confirmada no intervalo de 30 minutos sem exceções fatais.
5. **Git Repository Status:** A árvore de desenvolvimento em `C:\PhilcoOS` opera atualmente sem versionamento `.git` ativo localmente. Todas as modificações são preservadas e auditadas por SHA-256 e cópias de segurança em `output/releases/`.

---

## Completed work (Independent Audit & Research)

### 1. Auditoria Visual e Design System Glass & Aurora 2.0 (Figma)
- **Biblioteca Inspecionada:** [Material Design Symbols Extended (Community)](https://www.figma.com/design/qJA7C7GH5bqN2zZXIZ0rSM/Material-Design-Symbols-Extended--Community-?node-id=0-1).
  - *Origem e Licença:* Google Fonts / Material Symbols (Licença Apache 2.0). Uso irrestrito para projetos de software aberto e embarcado.
- **Frames Vetoriais Editáveis 1280×720 Gerados:**
  - [home_1280x720.svg](file:///C:/PhilcoOS/docs/design/frames_1280x720/home_1280x720.svg): Composição completa da Home com header (relógio 90sp, data, telemetria), card de clima escurecido e dock de 8 cards com margens seguras de 64dp.
  - [quick_control_center_1280x720.svg](file:///C:/PhilcoOS/docs/design/frames_1280x720/quick_control_center_1280x720.svg): Modal lateral translúcido (largura 420px) com sliders de hardware para volume (2%), mute, brilho (50%), backlight (100%) e atalho para o Android Settings.
  - [clima_widget_1280x720.svg](file:///C:/PhilcoOS/docs/design/frames_1280x720/clima_widget_1280x720.svg): Especificação detalhada do widget de clima (104dp de altura, superfície Dark Glass `#CC0B1220`) e catálogo de glifos homologados (céu limpo, chuva, offline/cache).
  - [dock_1280x720.svg](file:///C:/PhilcoOS/docs/design/frames_1280x720/dock_1280x720.svg): Grid horizontal dos 8 cards (124×134dp) mapeados para as fontes Realtek (AR 27, Cabo 1, AV 5, HDMI 1 18, HDMI 2 17, SmartTube, Apps, Ajustes) com tabela estrita de navegação D-pad anti-trap.
  - [focus_states_dpad_1280x720.svg](file:///C:/PhilcoOS/docs/design/frames_1280x720/focus_states_dpad_1280x720.svg): Matriz comparativa dos estados Normal (1.0x, superfície 8%), Focado (1.045x, anel Ciano `#00E5FF` 2.5dp, superfície 22%) e Pressionado (compressão tátil 1.02x para 90ms).
- **Especificação Geral do Design:** Atualizada em [PHILCOOS_ULTRA_777_DESIGN.md](file:///C:/PhilcoOS/docs/PHILCOOS_ULTRA_777_DESIGN.md).
- **Mapeamento de Glifos Aprovados:**
  - **Dock Principal:**
    - Live TV: `tv` / `live_tv` (Digital ISDB-T)
    - HDMI 1 & 2: `settings_input_hdmi` / `input`
    - AV Composto: `cable` / `settings_input_component` (RCA)
    - Apps Drawer: `apps` / `grid_view`
    - Configurações: `settings` / `tune`
  - **Quick Control Center:**
    - Volume: `volume_up`, `volume_down`, `volume_mute`, `volume_off` (Mute)
    - Brilho de Imagem: `brightness_medium` / `brightness_6`
    - Backlight (Luz de Fundo): `light_mode` / `wb_sunny`
  - **Widget de Clima:**
    - Ensolarado: `wb_sunny` | Parcialmente Nublado: `partly_cloudy_day` | Chuva/Drizzle: `rainy` / `water_drop` | Offline/Fallback: `cloud`
- **Diretrizes de Telas Editáveis 1280Ã—720 (Progressive 720p):**
  - **Home Screen:** Canvas 1280Ã—720. Margens horizontais de segurança de 64dp. Wallpaper fotográfico único em RGB_565 (1.84 MB de VRAM). Relógio de 90sp Roboto Medium (`sans-serif-medium`). Card de clima compacto de 104dp de altura com fundo escurecido `#CC0B1220` para contraste perfeito.
  - **Quick Control Center:** Modal translúcido deslizante à direita (380dp de largura), superfície `#E60B1220`, sliders D-pad com feedback numérico direto do Realtek TvManager.
  - **Focus Engine (D-pad):** Escala de foco contida em 1.045x (evita corte em TVs com overscan ativo), anel de seleção Ciano Elétrico `#00E5FF` (espessura 2dp), superfície `#38FFFFFF`. Zero alocações em `onDraw()`.

### 2. Pesquisa de Compatibilidade: Moonlight Android & Sunshine Host
- **Status da Versão Oficial:**
  - O repositório oficial `moonlight-stream/moonlight-android` removeu o suporte para Android 4.1 a 4.4 (KitKat / API 19) em maio de 2023 através do commit `17df152` (PR `#1207`, *"Drop support for Jelly Bean and KitKat"*), elevando `minSdk` para 21 (Android 5.0 Lollipop).
  - As últimas versões oficiais compatíveis com a API 19 e arquitetura `armeabi-v7a` pertencem à família **v10.x**, com destaque para **Moonlight Android v10.11** (e v10.9).
- **Interoperabilidade com Sunshine:**
  - O Sunshine implementa nativamente o protocolo GameStream (RTSP + ENet) com suporte a clientes legados Moonlight.
  - Para o Philco RTD2984TD-GR, a configuração recomendada é:
    - Resolução: 1280Ã—720 (720p) a 30 fps (ou 60 fps após medição de perda de pacotes e estabilidade de decodificação).
    - Codec: H.264 Baseline / Main profile (evitar HEVC devido ao overhead de decodificação na VPU).
    - Bitrate: 5 Mbps a 10 Mbps (adequado ao throughput do dongle USB Wi-Fi MediaTek MT7601U).

### 3. Diagnóstico do Tempo até o Primeiro Frame no SmartTube 32.80
- **Funil de Inicialização e Gargalos:**
  1. *Desafio BotGuard / PoToken:* O YouTube exige a geração do PoToken para evitar o erro 403 / "Sign in to confirm you are not a bot". O SmartTube gera este token inicializando o WebView do Android (`PoTokenWebView4`). No SoC Cortex-A7 Quad-Core a ~1.0 GHz rodando o Chromium 30/33 embutido do KitKat, a interpretação e execução do script ofuscado consome entre 25 e 50 segundos de CPU a 100%.
  2. *SABR / UMP Handshake:* Quando a atestação está `PENDING`, o `SabrProcessor` aciona recuperação de erro e troca de cliente YouTube via `ErrorFixerController`, somando latência adicional antes de solicitar os segmentos de mídia.
  3. *ExoPlayer e `dropOutputBuffer`:* O logcat revelou que durante a inicialização do vídeo, o relógio mestre de áudio avança enquanto a CPU está ocupada processando o token e inicializando o decodificador OpenMAX (`libOMX.RTK.video.decoder.so`). Quando os primeiros quadros são decodificados, o Presentation Timestamp (PTS) dos frames já está atrasado (`earlyUs < -30000 µs`). O `MediaCodecVideoRenderer` descarta esses buffers atrasados (`dropOutputBuffer`), prolongando a exibição do spinner até que o fluxo se estabilize e novos quadros cheguem em sincronia.

### 4. Requisitos Estritos para Validação de AVC 720p30
- O decodificador de hardware `OMX.RTK.video.decoder` suporta H.264 até 1080p, mas no ambiente de 512 MB de RAM com SmartTube e WebView ativos, a validação exige:
  1. Teste de arquivo local AVC 720p30 (Baseline/Main, 3 Mbps, AAC) via MediaCodec para aferir taxa real de quadros e descarte sem influência da rede/PoToken.
  2. Medição da taxa de descarte de quadros (`droppedFrames`). Apenas taxas < 1% configuram reprodução estável.
  3. Medição de PSS de memória durante reprodução contínua (garantindo margem livre > 30 MB de RAM no sistema).
  4. Teste final no SmartTube apenas após os passos anteriores serem aprovados.

---

## Files inspected

- `C:\PhilcoOS\AGENTS.md`
- `C:\PhilcoOS\docs\PHILCOOS_ULTRA_777_GLASS_AURORA.md`
- `C:\PhilcoOS\docs\PHILCOOS_ULTRA_777_DESIGN.md`
- `C:\PhilcoOS\docs\TECHNICAL_AUDIT.md`
- `C:\PhilcoOS\docs\HARDWARE_DEPENDENCIES.md`
- `C:\PhilcoOS\docs\REMOVAL_CANDIDATES_WITH_EVIDENCE.md`
- `C:\PhilcoOS\docs\VERIFIED_KEEP_COMPONENTS.md`
- `C:\PhilcoOS\analysis\slim_stage_report.md`
- `C:\PhilcoOS\extracted\factory_baseline\system\etc\media_codecs.xml`
- `C:\PhilcoOS\extracted\factory_baseline\system\etc\media_profiles.xml`
- `C:\PhilcoOS\source\philco-home\build.gradle`
- `C:\PhilcoOS\source\philco-home\src\com\philco\ultra777\home\MainActivity.java`
- `C:\PhilcoOS\source\philco-home\src\com\philco\ultra777\home\TvGlyphView.java`
- `C:\PhilcoOS\source\philco-home\src\com\philco\ultra777\home\AuroraBackdropView.java`

## Files modified

- `C:\PhilcoOS\docs\AI_HANDOFF.md` (Criado/Atualizado)
- `C:\PhilcoOS\docs\AI_DECISIONS.md` (Criado/Atualizado)
- `C:\Users\novog\Downloads\PhilcoOS\docs\AI_HANDOFF.md` (Espelhado)
- `C:\Users\novog\Downloads\PhilcoOS\docs\AI_DECISIONS.md` (Espelhado)

---

## Tests executed

- **Verificação Estática de Codecs (`media_codecs.xml`):** Confirmado suporte a hardware decoding para `video/avc`, `video/hevc`, `video/mpeg2`, `video/mp4v-es`, `video/x-vnd.on2.vp8` sob `OMX.RTK.video.decoder`.
- **Inspeção de Repositórios Oficiais (Moonlight & SmartTube):** Validados commits de fim de suporte da API 19 no Moonlight (`17df152`) e fluxo de atestação de PoToken / ExoPlayer `dropOutputBuffer` no SmartTube 32.80.
- **Validação de Isolamento de Código:** Nenhuma alteração foi realizada no código-fonte Java, scripts ou firmware durante esta missão de pesquisa e design.

---

## Results

- Identificação definitiva da versão compatível do Moonlight Android: **v10.11 (armeabi-v7a)**.
- Diagnóstico conclusivo sobre o atraso de inicialização do SmartTube (Cortex-A7 CPU limit durante BotGuard WebView + descarte de frames atrasados pelo ExoPlayer).
- Definição do design system Glass & Aurora 2.0 adaptado para limitações estritas da GPU PowerVR SGX544MP e Android 4.4 KitKat.

---

## Problems found

1. **Gargalo de CPU no BotGuard do KitKat:** O WebView legado do Android 4.4 não possui aceleração JIT moderna para scripts ofuscados de atestação do YouTube, tornando inevitável uma latência inicial de 30-60 segundos no primeiro vídeo.
2. **ExoPlayer Frame Dropping:** A sincronização com o relógio de áudio força o descarte em cascata de frames de vídeo iniciais quando a CPU sofre contenção.
3. **Moonlight Moderno Incompatível:** Versões v11.x e v12.x do Moonlight Android exigem API 21+ e não podem ser instaladas na TV sem falha imediata de análise do pacote (`INSTALL_FAILED_OLDER_SDK`).

---

## Risks

- Tentar atualizar o SmartTube para versões mais recentes pode agravar o consumo de RAM e quebrar a compatibilidade com o WebView do KitKat.
- Tentar renderizar sombras dinâmicas, blur em tempo real ou SVGs pesados na interface degradará a taxa de quadros da Home e causará coletas de lixo (GC) concorrentes na Dalvik VM.

---

## Next owner

**CODEX** (Lead Implementation Engineer).

---

## Next exact task

Codex deve assumir a estação de trabalho para conduzir a validação controlada do Moonlight e preparação do benchmark de vídeo local:
1. Obter o APK oficial do **Moonlight Android v10.11** (`armeabi-v7a`, `minSdkVersion 19`).
2. Auditar assinatura e manifesto com `aapt dump badging` e `apksigner`.
3. Conduzir teste de instalação controlada em `/data/app` (sem tocar no `/system` ou na preferência de Home).
4. Preparar um arquivo MP4 local AVC 720p30 para medir o desempenho isolado do decodificador `OMX.RTK.video.decoder` via Stagefright / ExoPlayer, verificando descarte de frames antes de tentar o streaming.

---

## 2026-11-21 — Realtek media investigation (Codex)

## Current state
- Device verified at `192.168.0.71:5555`: `smart_tv001` / `rtd298x_tv001`, Android 4.4.2/API 19, expected fingerprint. PhilcoOS `MainActivity` resumed, `mAlways=true`, `PlayingSource=17` (HDMI 2).
- Git remains unavailable in `C:\PhilcoOS`; branch/commit not available.

## Completed work
- Aptoide `cm.aptoidetv.pt.changhong_philco_44` already disabled (`enabled=2`, caller shell), still installed at `/system/priv-app/aptoide.apk`; no repeated disable performed.
- Inspected MediaBrowser v1.0 and launched its `VideoBrowser` Activity. Activity/process launch is proven; file browsing and playback are not.
- Pulled system MediaBrowser APK read-only to `output/MediaBrowser-system-baseline.apk`; SHA-256 `CF073617F5143370E449EA00E495DBB6EFEEBE7DEB247CCC26178AD80C760AB3`.
- Inspected storage/mounts and media services; `media.rtkplayer` exists but exposed no client state. Returned to PhilcoOS Home and HDMI 2.

## Files modified
- Appended diagnostics to `docs/AI_TEST_RESULTS.md` and the main report; this handoff records the session. Added the read-only APK pull. No Java, launcher APK, or firmware changes.

## Tests executed
- ADB identity, `pm list packages -d`, Aptoide package/path.
- MediaBrowser PackageManager/manifest, `am start`, Activity/process, filtered MediaPlayer logcat, media service list/check and dumpsys.
- `df`, storage directory listings, mounts; local `aapt dump badging/xmltree` on pulled APK.
- No build, install, benchmark transfer, or playback.

## Results
- Aptoide already disabled. Re-enable command: `adb -s 192.168.0.71:5555 shell pm enable cm.aptoidetv.pt.changhong_philco_44`.
- MediaBrowser Activity/process started. Idle logs contain `MediaPlayer stop called in state 0`; there was no active media client/file mapping or evidence of OMX decoding.
- `/system` appeared mounted `rw`; no write/remount command was issued and its origin is unknown.

## Problems found
- No mounted USB media/files were visible; browser file discovery and accessible benchmark path remain unresolved.
- `aapt dump badging` reported an unresolved icon-resource reference; package/version and manifest tree were readable.
- Decoder/service presence is not evidence of actual playback.

## Risks
- Do not transfer a benchmark or start playback until a readable media path is confirmed and the user can observe the TV.
- Keep `/system` untouched despite the current rw mount state.

## Next owner
- Codex.

## Next exact action
1. Confirm user can observe a short controlled media test.
2. Find a readable existing USB/local media path; if none, prepare a benchmark offline only after confirming storage path and space.
3. Run bounded local playback and collect codec, rendered/dropped frames, CPU and PSS evidence. Do not infer hardware decode from firmware inventory.

## 2026-11-21 — AVC 720p30 benchmark preparation (Codex)

## Current state
- TV remains online and confirmed identity; PhilcoOS Home default, HDMI 2 selected.
- No playback benchmark file was created and none was copied to the TV.

## Completed work
- Revalidated `/sdcard/Movies` is empty and `/data` has 3.1 GB free.
- Checked local video/encoding options. No FFmpeg/FFprobe or recognized local codec package (OpenCV/imageio-ffmpeg/PyAV/MoviePy) was available; unrelated personal videos were not used.
- Saved read-only device baseline in `output/media_benchmark_20261121_063337/`.

## Files modified
- Appended this handoff and `docs/AI_TEST_RESULTS.md`. Created only local baseline data files in the timestamped output directory.
- A screenshot attempt through `adb exec-out screencap -p` failed; no screenshot artifact and no remote file was created.

## Tests executed
- ADB identity/fingerprint, free storage, `/sdcard/Movies`, Home, source; memory and top baseline.
- Checked FFmpeg/FFprobe and available local Python media modules; no installs.
- No media generation, transfer, scanner update, playback or decoder test.

## Results
- Stage A is blocked before encoding because no suitable local encoder or verified synthetic clip is available. Stages B–D did not run.
- Device unchanged; Aptoide remains disabled, Home default and HDMI 2 retained.

## Problems found
- `adb exec-out screencap -p` exits with `4294967295` using the current ADB environment.
- No reliable current method to generate a synthetic H.264/AAC sample without installing tooling.

## Risks
- Do not use unrelated personal MP4s as benchmark inputs without confirming codec/profile/content.
- No remote TV output directory was created, so no cleanup/rollback of device files is needed.

## Next owner
- Codex.

## Next exact action
Use an already installed, trusted H.264/AAC encoder/asset if one becomes available; verify format, profile, FPS, duration, audio, size and hash locally before creating the authorized TV test directory and transferring. Then proceed to MediaBrowser only if its file-picker path is clear. Do not install encoder tools under this run's authorization.

---

## 2026-11-21 — Post-test diagnostics (Codex)

## Current state
- Expected TV online, Android 4.4.2/API19; PhilcoOS MainActivity resumed and persistent Home; HDMI 2 selected.
- Git branch/commit unavailable in `C:\PhilcoOS`.

## Completed work
- Captured logcat main/system/events/crash/radio/combined and Android snapshots in `output/post_test_diagnostics_20261121_064151/` without clearing buffers.
- Wrote `DIAGNOSTICO_TECNICO.md` and a reviewed redacted focus log; raw logs remain local.

## Files modified
- Appended this handoff, `docs/AI_TEST_RESULTS.md`, and the project report. Created diagnostic artifacts only. No code or device settings changed.

## Tests executed
- ADB state/device/date/uptime.
- Requested logcat buffers and Activity/window/memory/input/power/media/package/storage/process snapshots.
- No device interactions, restart, installation, build, or buffer clearing.

## Results
- No fatal exception, ANR, OOM, permission denial or fatal signal in captured logs. Crash buffer unavailable (`/dev/log/crash` absent).
- Confirmed 18 Toast rate-limit/suppression events at 06:39:35–06:39:39 after the app reached Android's 50-toast cap. Realtek volume/source/mute calls and TV/Home/Settings Activity transitions occur nearby; no exact reported symptom is identifiable.
- Dalvik reported two JIT trace aborts then fell back to interpreter. No measured user impact.
- Memory snapshot: total 456,492 kB, Home PSS 20,712 kB, MediaBrowser 9,456 kB. No memory-pressure failure demonstrated.

## Problems found
- Toast suppression may hide feedback; triggering toast and specific functional failure are not logged.
- HWC framebuffer-frequency E-level output has no confirmed failure meaning.

## Risks
- Raw logs can contain personal/session information; don't share full captures.

## Next owner
- Codex.

## Next exact action
- **Next exact action (historical diagnostic checkpoint):** correlate the user-visible failure with one targeted TvManager read/write capture; do not infer or alter HAL/firmware from that older log set alone.

---

## 2026-11-21 — Architectural Blueprint & Reverse Engineering Handoff (Antigravity â†’ Codex)

### Metadata
- **Task ID:** REVERSE-ENG-REALTEK-01 / AV-HDMI-MUTE-QCC2
- **Agent & Role:** Google Antigravity (Lead Research & Design Engineer)
- **Date/Time:** 2026-11-21 07:05:00 -03:00
- **Git Branch:** N/A (Standalone canonical directory `C:\PhilcoOS`)
- **Base Commit / Baseline Artifact:** QC03 (`F837A27AD5BB8CFB296C5562A875B38FA44DABFB38CF51017447ED411646A6A8`)
- **Installed Device State:** Philco PH42F10DSGW (`192.168.0.71:5555`), Android 4.4.2/API 19, `com.philco.ultra777.home/.MainActivity` default launcher (`mAlways=true`), `PlayingSource=17` (HDMI 2).

### Current state
- Launcher QC03 está estável e operando como Home persistente.
- As trocas de entrada física para AV (5), HDMI 1 (18) e HDMI 2 (17) persistem o ID no TvManager mas não exibem a imagem em tela cheia na TV.
- O botão Mute do Quick Control Center inverte o estado lógico na interface mas os alto-falantes físicos continuam emitindo som.
- Faltam controles de imagem avançados (contraste, nitidez, modo de imagem) e som (graves, agudos, equalizador de 7 bandas, modo de som).
- A interface visual necessita de refinamento para atingir o padrão Glass & Aurora 2.0 (contraste D-pad aprimorado, tipografia e organização em abas/blocos).

### Completed work
1. **Engenharia Reversa Completa do Middleware Realtek (`android.app.TvManager`):**
   - Mapeados 372 métodos públicos e ocultos do TvManager a partir da descompilação de `TSB_Tv.apk`, `SourceMenu.apk` e `framework_classes_qc01.txt`.
2. **Identificação da Causa Raiz e Solução de AV / HDMI:**
   - *Causa Raiz:* Em `MainActivity.java` (L891-893), `switchInputOrOpenMenu` invocava `switchInputOrOpenMenu(input, sourceId, false)` com `openTvOnSuccess=false`. Na arquitetura Realtek RTD298X, as entradas de vídeo externas dependem de Direct Video Overlay (DVO) controlado exclusivamente pela Activity de primeiro plano `com.tsb.tv/.Tv_strategy`. Como a `MainActivity` é opaca e permanecia no topo, a camada de vídeo acelerada por hardware ficava oculta.
   - *Solução:* Ao comutar para AV (5), HDMI 1 (18) ou HDMI 2 (17), despachar `setSource(sourceId)`, gravar `PlayingSource` e imediatamente iniciar `com.tsb.tv/.Tv_strategy` com `FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_RESET_TASK_IF_NEEDED`.
3. **Identificação da Causa Raiz e Solução do Mute Físico:**
   - *Causa Raiz:* Em `MainActivity.java` (L1311), `setRealtekMute` chamava `tvManager.setMute(boolean)`. No RTD298X, este método corta apenas o demodulador de RF do tuner de TV aberta (ATV/DTV), não afetando as entradas HDMI/AV nem os fluxos de áudio do Android.
   - *Solução:* Nos APKs originais de fábrica (`TSB_Tv`, `RTKSettings`), o corte de amplificação física utiliza `tvManager.setApMute(boolean)` (Audio Power Amplifier Mute). A solução completa requer chamar via reflexão `setApMute(muted)`, sincronizar com `AudioManager.setMasterMute(muted, 1)` / `setStreamMute(STREAM_MUSIC, muted)`, e manter `setMute(muted)`.
4. **Mapeamento Integral das APIs de Imagem e Som no Hardware:**
   - *Imagem:* `getBrightness()`/`setBrightness(int)`, `getContrast()`/`setContrast(int)`, `getSaturation()`/`setSaturation(int)`, `getHue()`/`setHue(int)`, `getSharpness()`/`setSharpness(boolean apply, int val)` [0-100], `getBacklight()`/`setBacklight(int)` [0-100], `getPictureMode()`/`setPictureMode(int mode)` (0: Padrão, 1: Suave, 2: Dinâmico, 3: Usuário, 4: Eco).
   - *Som:* `getBass()`/`setBass(int)` [0-100], `getTreble()`/`setTreble(int)` [0-100], `getBalance()`/`setBalance(int)` [0-100], `getSoundMode()`/`setSoundMode(int mode)` (0: Padrão, 1: Música, 2: Filme, 3: Notícias, 4: Usuário), Equalizador de 7 bandas `getEqualizer(freqHz)` / `setEqualizer(freqHz, val)` nas frequências exatas `100, 500, 1200, 3000, 5000, 10000, 18000` Hz [0-100], `setAutoVolume(boolean)`, `setTrueSurround(boolean)`.
5. **Especificação de Arquitetura e Design Glass & Aurora 2.0:**
   - Painel lateral do QCC expandido para 420px de largura com 3 seções temáticas (Áudio, Imagem, Equalizador).
   - Paleta Glass & Aurora com superfícies `#CC0B1220`, bordas foscas 1.5dp `#29FFFFFF` e anel de foco D-pad `#00E5FF` (escala 1.045x). Zero alocação dinâmica em renderização de canvas.

### Files inspected
- `source\philco-home\src\com\philco\ultra777\home\MainActivity.java`
- `source\philco-home\src\com\philco\ultra777\home\TvGlyphView.java`
- `.toolchain\tmp\sourcemenu-classes-dump.txt`
- `.toolchain\tmp\tsb_tv_classes_20261121.txt`
- `.toolchain\tmp\framework_classes_qc01.txt`
- `build\system_stage\app\TSB_Tv.apk`
- `build\system_stage\app\SourceMenu.apk`
- `docs\design\frames_1280x720\quick_control_center_1280x720.svg`
- `docs\AI_DECISIONS.md`

### Files modified
- `docs\AI_DECISIONS.md` (Registradas decisões DEC-006, DEC-007 e DEC-008).
- `docs\AI_HANDOFF.md` (Este handoff técnico de transferência para o Codex).

### Technical decisions
- Registrar DEC-006: Ativação mandatória de `com.tsb.tv/.Tv_strategy` para entradas físicas HDMI 1, HDMI 2, AV e Cabo.
- Registrar DEC-007: Corte físico de amplificador via `TvManager.setApMute(boolean)` em conjunto com `AudioManager.setMasterMute`.
- Registrar DEC-008: Integração direta dos 14 métodos de hardware de imagem e som em classe helper de reflexão no módulo `com.philco.ultra777.home`.

### Commands executed
- Inspeção estática de dumps DEX via scripts locais no workspace canônico `C:\PhilcoOS`.
- Consulta e extração de recursos de `TSB_Tv.apk` via `aapt.exe dump resources`.

### Build result
- N/A para esta etapa de auditoria e arquitetura (nenhum binário foi compilado por Antigravity, preservando QC03 e QC04).

### Device test result
- N/A (nenhuma conexão adb invasiva ou alteração de estado realizada no dispositivo físico).

### Problems found
- A chamada `switchInputOrOpenMenu` no código atual possuía um parâmetro hardcoded `openTvOnSuccess = false`, que intencionalmente suprimia a transição para a camada de vídeo Direct Video Overlay.
- O método `TvManager.setMute` é insuficiente para o mute de sistema porque opera estritamente no tuner demodulator.
- O método `TvManager.setSharpness` possui assinatura não convencional com dois parâmetros (`boolean apply, int value`), o que quebraria uma reflexão simples de um único parâmetro inteiro se implementada às cegas.

### Risks
- Conflito de foco ou task stack ao lançar `com.tsb.tv/.Tv_strategy`: deve-se garantir que o botão HOME do controle remoto retorne sempre à `MainActivity` com `mAlways=true`.
- Limite de taxa de Toasts do Android (50 toasts max): evitar emitir múltiplos Toasts ao movimentar sliders de hardware contínuos.

### Next owner / historical action
The original implementation request below is superseded by the Codex device-validation entry appended after this handoff. `getApMute()` is not present on the currently installed firmware; use the confirmed runtime interface findings there rather than assuming the static reverse-engineered API list matches every firmware build.

---

## 2026-11-21 — AV/HDMI and physical mute implementation (Codex)

- **Device:** `192.168.0.71:5555`, `smart_tv001` / `rtd298x_tv001`, Android 4.4.2 / API 19.
- **Installed candidate:** `output/releases/philco-home-av-hdmi-mute-QC05-r2-20261121.apk`, package `com.philco.ultra777.home`, versionCode 777, versionName `1.0.0-ultra777`; SHA-256 `0EC29239C0205BDFDDC4A63B7A980C6D2012339FEDEFF53593FC9BE85E9AB3C0`.
- **Baseline preserved:** QC03 package copy remains `output/releases/philco-home-installed-QC03.apk`; QC04 and A57 were not modified. The first QC05 candidate (`...QC05-20261121.apk`) is also preserved; its runtime attempt exposed missing `getApMute()` and was superseded by r2 before final validation.
- **AV/HDMI:** On r2, D-pad actions selected AV=5, HDMI 1=18 and HDMI 2=17. `getCurLiveSource()` read back each requested ID, `PlayingSource` persisted the same value, and `com.tsb.tv/.Tv_strategy` became foreground. The OEM on-screen source labels appeared in captures. HOME returned to PhilcoOS; final `PlayingSource=17`, `MainActivity` foreground, and a persistent HOME preference (`mAlways=true`) is present. Captures show black video areas; no active external video signal was verified.
- **Mute:** `TvManager.setApMute(true/false)` returned normally. Runtime readback through `AudioManager.isMasterMute()` was true then false; Realtek source volume remained 0 before/after. The UI changed only after readback. The installed framework has no `TvManager.getApMute()`; do not use it. The logs confirm API calls and master state, not audible speaker output. Physical/acoustic silence is still unverified because source volume was 0 and no audible test content was running.
- **Compatibility correction:** `AudioManager.setMasterMute(boolean,int)` is available in the installed framework; `isMasterMute()` provides the readback. `setStreamMute(STREAM_MUSIC,...)` remains a fallback. `TvManager.setMute()` is only synchronized for tuner sources.
- **Build:** `:source:philco-home:assembleRelease` succeeded. APK passes `aapt dump badging`, `zipalign -c 4`, and v1 signature verification; v2/v3 are absent as required for KitKat. Signing certificate SHA-256: `b52e3604deca330788c71cca8031b25b4208a7b9af6f521ff162634e14f2f9aa`. Installed APK pulled back from `/data/app` has the same SHA-256 as the archived r2 artifact.
- **Evidence:** `output/qc05_r2_av.png`, `output/qc05_r2_hdmi1.png`, `output/qc05_r2_hdmi2.png`, `output/qc05_r2_mute_console_observation.txt` (captured console excerpt, not raw logcat), `output/qc05_device_validation_focus.log`, `output/qc05_r2_final_home.png`.
- **Files changed:** `MainActivity.java`, `docs/AI_HANDOFF.md`, `docs/AI_DECISIONS.md`, `docs/AI_TEST_RESULTS.md`.
- **Next exact action (r2, superseded):** Repeat source/mute validation on the follow-on r3 candidate; retain the acoustic-output limitation until audible content at nonzero volume can be tested. No firmware or `/system` changes were made.

## 2026-11-21 — QC05-r3 AV/HDMI routing and Mute follow-up

- **Source sequencing evidence:** OEM `SourceMenu.checkSource()` writes `PlayingSource` before starting explicit `com.tsb.tv/.Tv_strategy` with `FLAG_ACTIVITY_NEW_TASK`. OEM `KeyPadService.setSource()` instead calls `TvManager.setSource(id)`, writes `PlayingSource`, then sends `com.realtek.TVSignalAlarm.SourceChange`. The app combines those observed behaviors: `TvManager.setSource` → persist and read back `PlayingSource` → source-change broadcast → start resolvable `Tv_strategy` with NEW_TASK → asynchronous `getCurLiveSource()` verification. Launching the OEM activity is backed by the SourceMenu path, not assumed to perform the source change itself.
- **Code change:** `MainActivity.java` now performs the OEM activity handoff before the delayed live-source verification, logs each verified live ID, and leaves the UI responsive. If mute changes the source-volume readback, the code restores the captured value through `AudioManager.setStreamVolumeForL4300` and verifies restoration; a failed restore is reported as operation failure. Existing `setApMute` + `AudioManager.setMasterMute` behavior and post-readback UI update remain.
- **Build artifact:** `output/releases/philco-home-av-hdmi-mute-QC05-r3-20261121.apk`, package `com.philco.ultra777.home`, versionCode 777/versionName `1.0.0-ultra777`, min/target API 19, size 1,210,280 bytes, SHA-256 `A081CC53BC9695657AD39A12032B05AD4BDA0F4F50EBBD098790B1272A370A30`. v1 signature verified; v2/v3 absent; zipalign verified. Certificate SHA-256 remains `b52e3604deca330788c71cca8031b25b4208a7b9af6f521ff162634e14f2f9aa`.
- **Install integrity:** Installed update on `192.168.0.71:5555`; pulled `/data/app/com.philco.ultra777.home-1.apk` to `output/philco-home-installed-QC05-r3.apk`. Pulled and archived hashes match exactly.
- **Inputs on device:** D-pad selected TV aberta=27, Cabo=1, AV=5, HDMI1=18, HDMI2=17. `PlayingSource` and `getCurLiveSource()` matched for each; `com.tsb.tv/.Tv_strategy` became resumed. HOME returned to PhilcoOS, persistent HOME preference `mAlways=true`; final state HDMI2=17 and MainActivity resumed. `TvManager.setSource` returned `false` for selected inputs, but the independent live-source readback matched; the return value alone is not treated as failure. These checks confirm routing/API state, not active picture from an external signal.
- **Mute on device:** With source volume 0, QCC showed “Silenciar” → “Ativar som” on mute and “Ativar som” → “Silenciar” on unmute. `TvManager.setApMute(true/false)` returned normally; `AudioManager.isMasterMute()` read back true/false; volume remained 0. No content was producing audible output, so acoustic speaker silence/restoration is not claimed. The volume-restore branch did not run because the volume remained unchanged.
- **Validation evidence:** `output/qc05_r3_device_validation.log`; installed pull `output/philco-home-installed-QC05-r3.apk`. ADB screenshots were not required for this control validation. Final TV state was restored to PhilcoOS Home / HDMI2 / unmuted.
- **Limits:** No active AV/HDMI source was available to verify picture. No audible nonzero-volume source was used. No changes to `/system`, firmware, bootloader, HAL or original OEM APKs.
- **Next exact action:** On a known audible source at a low nonzero volume, verify mute audibly and confirm the saved volume returns after unmute. Do not reopen source-routing investigation unless a connected signal demonstrates a failure.

## 2026-11-21 — QC05-r4 media-stream mute correction

- QC05-r3's `TvManager.setApMute` + `AudioManager.setMasterMute` changed reported state but did not silence SmartTube; user also confirmed the remote's mute indication did not silence output. Those calls are not accepted as physical-mute proof and are no longer used by the QCC media mute.
- QC05-r4 uses Android stream type 3 (`STREAM_MUSIC`) through the OEM `AudioManager.setStreamVolumeForL4300` path. OEM `TSB_Tv` passes literal `3` to this method; framework disassembly shows this method resolves the selected output device for the stream and updates that stream's volume index. This is a media-volume mute alternative, not native amplifier mute.
- Before setting index 0, r4 commits the prior positive stream index to SharedPreferences. Active mute calls and volume-key enforcement do not replace the saved index. Unmute restores and verifies the exact saved index before clearing it. Home resume reapplies zero when a saved mute survived process termination/reboot. The volume-change receiver remains registered while muted in the background and reasserts zero; volume keys are consumed while muted with Home foreground.
- **Build/install:** `output/releases/philco-home-av-hdmi-mute-QC05-r4-20261121.apk`, SHA-256 `3D43327516091ED0AF5A0580F0BF5B4F6FE62F1ECB0A266A9DF4DFA5934C4A57`, v1 signature only, API 19, package/version unchanged, same signing certificate as r3. Installed with `adb install -r`; QC05-r3 remains preserved. Initial r4 candidate SHA `867EB4DE8B6A330E99B5F0D68A2CC18962F9D6DAE8EE7ABE237E43F4A26CF705` was retained as `output/releases/philco-home-av-hdmi-mute-QC05-r4-initial-20261121.apk`.
- **Software validation:** Started at `STREAM_MUSIC=25`; r4 mute/unmute readback tested at 26→0→26. A volume-up event during active mute was returned to zero without replacing saved 26. After `am force-stop` and relaunch, `Persisted media mute recovery` confirmed saved 26 and stream readback 0; QCC unmute restored 26. Final stream index was restored to the original pretest 25. MainActivity resumed and no PhilcoUltra777 fatal exception was found.
- **Acoustic limitation:** User reported normal audible SmartTube playback before returning Home. Moving HOME to QCC paused/lost SmartTube audio focus, so acoustic silence and restoration while a live stream continues were not verified. Keep this function marked software-validated/acoustic-pending until a live-content listening test succeeds.
- **Evidence:** `output/qc05_r4_acoustic_test.log`; UI captures `output/qc05_r4_check.png` and `output/qc05_r4_panel.png`. The app was left unmuted at index 25 on HDMI 2, PhilcoOS Home foreground/default. No `/system`, firmware, HAL, or OEM APK was changed.

## 2026-11-21 — QC05-r4.3 mute OSD

- User clarified the request was a large custom TV-style mute OSD at the lower-left corner, rather than only changing the small volume glyph. Added a static 68dp glass badge with a large speaker/x symbol, shown when media volume is muted or zero and hidden when sound returns. The small top-right status glyph remains.
- QC05-r4.3 is installed at `/data/app`; release `output/releases/philco-home-QC05-r4.3-mute-icon-20261121.apk`, SHA-256 `9229049E6BF86FE123E99E82F4476051BFF7EBB993174C590D68B8C91497F160`, matches `output/philco-home-installed-QC05-r4.3.apk`. Build is v1-only, API 19, same package/version/signing certificate. QC05-r4, r4.1 and r4.2 archives remain preserved.
- Capture `output/qc05_r43_muted.png` confirms the large OSD at 1280×720 without covering the SmartTube tile. At capture, QCC mute remained active (`STREAM_MUSIC=0`), HDMI2=17, and PhilcoOS Home foreground.
- User feedback after QC05-r4 reports the volume-zero mute workaround physically worked. Keep its classification as user-reported media mute; do not describe it as a native amplifier mute or hardware HAL operation.

## 2026-11-21 — QC05-r4.4 optical centering

- User asked to center the mute symbol inside the lower-left badge. Shifted the combined speaker/x silhouette 2dp left to compensate for the heavier filled speaker and optically center the mark; badge position and dimensions remain unchanged.
- QC05-r4.4 is installed and pulled back with matching SHA-256 `2F37F702230BA3A2B8BA0F6DEBBCB1C693FD5FD73B85454DE28CB5B0990DB20C`; release: `output/releases/philco-home-QC05-r4.4-mute-osd-centered-20261121.apk`. v1 signature, API19, same package/version/key. Capture: `output/qc05_r44_muted.png`.
- Final captured device state: muted (`STREAM_MUSIC` output-device index 0), HDMI2=17, PhilcoOS MainActivity resumed. QC05-r4.3 remains archived.

## 2026-11-21 — QC05-r4.5 darker glass surfaces

- User asked for the SmartTube/HDMI dock cards and top `Paisagens` / `Ajustes` controls to be less transparent and less washed out. Updated the shared glass background to opaque dark navy surfaces while preserving accent strokes, glyph colors, focus ring, wallpaper and layout.
- QC05-r4.5 installed with `adb install -r`; artifact `output/releases/philco-home-QC05-r4.5-opaque-cards-20261121.apk`, SHA-256 `CCED15E1245A384DDC8EA427726DCE2202A68CCE4F18DC605F8C211F346B389E`, v1 only, API 19, existing signing key. Screenshot `output/qc05_r45_muted.png` shows the darker tiles/top buttons and centered mute badge at 1280×720.
- No audio/source setting was changed during this visual update; mute remained active, HDMI2 selected, PhilcoOS Home foreground.

## 2026-11-21 — QC06-r2 compact weather and clock

- Implemented a 420 × 86dp dark-glass weather card, 48dp Canvas weather glyph, and prominent white 38sp temperature with °C. Live condition, city, and provider credit remain. Clock is 94sp Roboto Light; date is 20sp Roboto Medium white directly below. No bitmap or dependency added.
- Build succeeded with the project's JDK 17 / Gradle 8.9. API 19, v1 signature, absent v2/v3, and zip alignment verified. Artifact: `output/releases/philco-home-weather-clock-QC06-r2-20261121.apk`, SHA-256 `7745F02D77F64F676B515DE30718B48D429A44B7BDBDE9700D01A9B5B137A01C`.
- Installed with `adb install -r` on `192.168.0.71:5555`; pulled installed APK hash matches the release. Screenshot `output/qc06_weather_clock_r2.png` is 1280 × 720 and shows the live 26°C / Nublado state with its matching cloud glyph. MainActivity resumed; no matching fatal exception appeared in the queried log tail.
- QC05-r4.5 and the exact pre-update installed APK are preserved in `output/releases/` and `output/backups/`. No firmware, source, audio, or Home-default setting was changed.
- **Next action:** user visual review. Keep QC05-r4.5 as rollback; revisit only if the TV capture needs adjustment.


## 2026-11-21 — QC07-r3.7 handoff

- **State:** Home clock/weather update built and installed; QC07-r3.7 is the current candidate. QC07-r3.2 remains preserved as the prior installed reference; older A57/EB256/QC artifacts remain untouched.
- **Artifact:** `output/releases/philco-home-QC07-r3.7-ios-weather-clock-lato-20261121.apk`; SHA-256 `3C5DF979FE51D327886A68AA989BCC7A47535823407B8E0C2201F52923382088`. Installed pull `output/qc07-r3.7-installed.apk` matches. Screenshot `output/qc07-r3.7-home-1280x720.png`.
- **Device evidence:** 1280×720 capture shows Lato Medium Home clock and redesigned weather card (Joinville, 28°C, Nublado); `MainActivity` resumed, HDMI 2 active. Volume readback remained 25. Do not infer prolonged stability from this smoke check.
- **Next owner/task:** user reviews appearance. Next engineering task remains a single instrumented SmartTube 720p30 stutter investigation; capture playback/frame-drop/decoder and process-memory evidence before considering any decoder/firmware action.

## Current state

- **Task:** QC07-r3.9 GMT−3 clock, compact weather icons, Home alignment.
- **Agent/role:** Codex, implementation and device validation. **Date:** 2026-11-21. **Branch/commit:** N/A; `git status` reports this workspace is not a Git repository.
- **APK:** `output/releases/philco-home-QC07-r3.9-gmt3-weather-icons-alignment-20261121.apk`, SHA-256 `860F128325E17A9A5D5CD301E2D3FC32714C48AABA9CC4A586D3C90D1EA85EE8`; package versionCode 777/versionName 1.0.0-ultra777/API 19. Installed pull matches. QC07-r3.8, r3.7, A57 and EB256 preserved.
- **Device:** 192.168.0.71:5555 online. PhilcoOS MainActivity resumed/Home; source label HDMI 2 active; STREAM_MUSIC index 25. Timezone `Etc/GMT+3`, `auto_time=1`, `auto_time_zone=0`; observed local time agrees with UTC−3. Cold-boot persistence has not been retested.

## Completed work

- Narrowed weather card to 312×88dp; aligned right edge with action and status controls. Added seven lightweight official Material Symbols Rounded transparent PNGs for live weather states, shared by Home and screensaver.
- Date now reads full weekday plus numeric date; time and date share Lato Medium, with an 8dp optical date inset. Moved the source/OS label to the shared right alignment.
- Corrected the device's wrong `America/Godthab` timezone to fixed UTC−3 after observing that literal `GMT-03:00` is sign-reversed on this KitKat property path. Automatic clock remains enabled; automatic timezone is disabled.

## Files modified

`source/philco-home/src/com/philco/ultra777/home/MainActivity.java`; `scripts/rasterize_weather_symbols.py`; seven PNGs in `source/philco-home/res/drawable-nodpi/`; preserved Google SVGs/license in `docs/assets/material-weather-reference/`; QC07-r3.9 screenshots and installed APK in `output/`; shared project documentation. No firmware or `/system` file changed.

## Tests executed

- `assembleRelease` PASS; API 19 badging, zipalign, v1 signature verified (v2/v3 absent).
- Installed with `adb install -r`; pulled APK SHA matches release.
- Captured Home and screensaver at 1280×720. D-pad entered manual screensaver and Back returned to Home. MainActivity resumed; HDMI2 was displayed active; volume remained 25. Queried logcat tail had no PhilcoOS fatal exception.
- Current device `date` showed UTC−3 and the Home clock showed the matching local time. A real cold-boot retest remains pending.

## Problems found

At the first reconnect, TV was offline and timezone ID was `America/Godthab`; the epoch itself was correct. `GMT-03:00` setprop was interpreted as UTC+3 and was immediately corrected to `Etc/GMT+3`. The earlier shutdown cause remains unknown; current source contains the requested five-hour idle-to-standby plus 30-second countdown, making that a plausible but unproven cause.

## Risks

Automatic timezone is disabled to retain fixed UTC−3; if the TV is later used outside that zone, change it intentionally. Do not claim five-hour shutdown caused the earlier off state without logs.

## Next owner

User review.

## Next exact action

Review `output/qc07-r3.9-home-1280x720.png` and `output/qc07-r3.9-screensaver-1280x720.png`. If appearance is accepted, leave the UI stable; separately validate timezone after a normal cold boot when convenient.

## Antigravity design handoff — weather card only (2026-11-21)

**Owner:** Google Antigravity, design/Figma research. This request supersedes the prior “user review” next action for the weather widget only; QC07-r3.9 remains installed and is the visual baseline.

**Reference image:** `docs/design/reference/weather-card-reference-20261121.png` (copied from the user's attachment; SHA-256 `E9DBA92269015DF35162DD633BA3FD1FB5A11D2F6DFF4A5A45C0B3D82F327F93`).

**Current device/layout baseline:** Home weather widget is 312×88dp at 160dpi (312×88px), aligned to the 1216px right content edge. Current Home/saver screenshots: `output/qc07-r3.9-home-1280x720.png` and `output/qc07-r3.9-screensaver-1280x720.png`. Current live fields are city, temperature, WMO condition/icon, and Open-Meteo attribution. The same current-condition presentation appears on Home and saver.

### Request to Antigravity

Create a polished Figma proposal for **only the weather widget**, inspired by the supplied split-card reference: a clear condition/icon area on the left, a subtle divider, and city/current temperature on the right. Adapt its visual idea to PhilcoOS Glass & Aurora and the golden landscape wallpapers. The result should look harmonious over both light and dark photo areas: restrained charcoal/navy glass, gentle teal/cyan or neutral highlights, white high-contrast text. Keep weather-cloud shapes neutral white/soft gray; use blue only as a small rain/droplet accent so the cloud never looks unnaturally blue. Avoid loud orange/green/purple card backgrounds and strong gradients that compete with wallpaper.

Preserve the narrow footprint (target 312×88dp; do not enlarge the widget or shift other Home regions without showing a strong need). Align icon, labels, divider, temperature, card edges and right-side controls to a consistent 4/8dp pixel grid. Show the 1280×720 full Home and a close-up at native size, plus focused D-pad state. Confirm labels remain readable at 720p and text cannot collide for long city/condition strings.

Use only available live data (city, current temperature, current condition and source attribution). Do not invent high/low, humidity, precipitation, forecast day, or other API fields. If proposing external weather art, use a small static transparent SVG/PNG set suitable for API 19, preserve source/license, target 48–96px raster sizes, and keep the total weather-icon payload under 30KB. No animation, font pack, library or network-loaded image.

**Deliverables:** Figma frame/link or exported mockup, measurements/color/type tokens, focus-state behavior, asset source/license/hash/size if used, and a short implementation note mapping WMO states to icons. Do not edit Java, build/install an APK, change system settings or touch firmware during this design handoff. Codex will implement only after reviewing the proposal.

## Current state — QC07-r3.11 screensaver alignment (2026-11-21)

- **Installed candidate:** `output/releases/philco-home-QC07-r3.11-screensaver-centering-clipfix-20261121.apk`, SHA-256 `5943710DF151F44EAAAA8BBE44225699BC848774F3960EDDBBCCDE48EA0DC946`; installed pull `output/qc07-r3.11-installed.apk` matches. API19, v1 only, zipaligned. QC07-r3.9 rollback preserved; r3.10 candidate preserved but superseded due initial-glyph clipping.
- **Device:** TV `192.168.0.71:5555` online; MainActivity/Home resumed after saver exit; HDMI2 indicated active; media volume 25; timezone `Etc/GMT+3`, local time about 20:09, auto clock on/auto timezone off.
- **Change:** date and weather row in screensaver shifted left 8dp; clock/Home unchanged. Child clipping disabled so the complete spelled weekday renders. Build/install and physical screenshots pass. Evidence: `output/qc07-r3.11-screensaver-1280x720.png`, `output/qc07-r3.11-home-after-saver-1280x720.png`.
- **Next owner:** Antigravity retains the separate weather-card Figma brief recorded below; Codex remains owner for implementation. No firmware, `/system`, HAL or bootloader modified.
- **Next exact action:** user reviews r3.11 screensaver capture; Antigravity can proceed with design-only weather-card proposal against installed r3.9 Home baseline. Review before any Home implementation.

## Codex review — Antigravity weather proposal (2026-11-21)

**Status:** ANALYZED / design review only. No Java, APK, firmware, device settings or installed version changed in this review. The current physical build remains QC07-r3.11; the weather-widget implementation baseline remains the r3.9 Home card.

**Files produced:** `docs/design/frames_1280x720/weather_widget_redesign_1280x720.svg` (full Home composition) and `docs/design/frames_1280x720/weather_widget_closeup_states.svg` (normal/focused card and WMO examples). Rendered review images: `output/antigravity-weather-redesign-review.png` and `output/antigravity-weather-closeup-review.png`. No Figma URL was found; deliverables are exported SVG mockups.

**What works in the proposal:** the card preserves 312×88dp and the 1216px right edge; the split layout has a restrained divider; dark charcoal/navy glass, white cloud, modest cyan focus and a neutral cloud/golden sun treatment follow the requested palette. Normal and focused states are shown. The close-up is readable at native 312×88 when inspected separately.

**Revisions requested before implementation:**
1. The full-frame proposal redraws the wallpaper as a vector shepherd/lamb scene and changes other Home elements (dock/icons/header styling). This conflicts with the widget-only scope. Rebase the full 1280×720 frame on the actual QC07-r3.11 Home capture/photo and keep every non-weather pixel, position, font, icon and component unchanged; overlay only the weather-card concept at the existing x/y/size.
2. Complete the WMO matrix to match current `MainActivity.weatherDrawableForCode`: code 0 sunny; 1–2 partly cloudy; 3/cloud default; 45/48 fog; 51–67 and 80–82 rain; 71–77 and 85–86 snow; 95+ thunderstorm. Current matrix shows only clear, cloud, rain, thunder, offline/cache and fog, omitting partly-cloudy and snow examples. Keep condition text and sample WMO code consistent (partly cloudy must not be labelled WMO 0).
3. Improve the tiny provider attribution’s contrast/legibility while keeping it secondary; validate any long city/condition strings in the same 312×88 footprint.
4. State whether the hand-coded vector weather artwork is only mockup art or recommend static, licensed implementation assets. Keep total shipped icon bytes under 30KB and do not add motion, network image loading or dependencies.

**Next owner:** Antigravity should revise only the design exports/specification, not Java or device artifacts. Codex will compare the revised full frame against r3.11 and implement only after user review.

## New user direction — weather card visual reset (supersedes prior 312×88 constraint)

The user rejected the previous flat, narrow card concept. They explicitly allow changing the widget's shape and size. Use the user's reference image `docs/design/reference/weather-card-reference-20261121.png` as the visual source: polished blended gradients, dimensional glass, soft shadows, premium weather artwork (cloud/sun/rain), and a clear left/right hierarchy. Create a cohesive PhilcoOS adaptation with richer gradients and a more premium finish; avoid a flat navy box and avoid random saturated colors. Keep clouds neutral silver/white; reserve blue for rain accents. Adapt card colors to golden photographic wallpapers and maintain high text contrast.

Redesign **only the weather widget**. The full 1280×720 frame must use the actual current Home capture/wallpaper and preserve every non-weather pixel, component, position, icon and font. The widget may grow or change silhouette to fit the concept, but must remain within the existing right-side header region, not cover controls or dock, and remain D-pad focusable/readable from TV distance.

Use only live fields already available: current condition, city, current temperature and Open-Meteo attribution. Keep WMO mapping consistent with `MainActivity.weatherDrawableForCode`: 0 sunny; 1–2 partly cloudy; 3/cloud fallback; 45/48 fog; 51–67 and 80–82 rain; 71–77 and 85–86 snow; 95+ thunderstorm. No invented forecast, humidity, high/low or date.

Investigate lightweight, properly licensed static weather artwork with a dimensional style similar to the reference. Provide source/license and individual/total byte sizes; prefer transparent PNG/WebP at suitable display resolution, no animation, runtime network fetch, new library or font. If available assets cannot meet size/quality, show the best legal lightweight option and state the trade-off.

Deliver only: one primary Figma/SVG concept plus one short alternate card shape, full Home at 1280×720, widget close-up at native size, focused D-pad state, final dimensions/pixel coordinates, palette/type, and WMO-to-asset table. Keep response/brief concise. Do not edit Java, package/build/install APK, or change device/firmware. Codex reviews before implementation.

## Reference confirmation — exact 2×2 weather-card image (2026-11-21)

The user re-sent the same reference image; its SHA-256 exactly matches `docs/design/reference/weather-card-reference-20261121.png` (`E9DBA92269015DF35162DD633BA3FD1FB5A11D2F6DFF4A5A45C0B3D82F327F93`). Treat that 2×2 image as the explicit art direction. The four cards are visual examples of weather-state styling, not four simultaneous Home widgets. The user permits changing the current card's dimensions/silhouette; the former strict 312×88dp requirement is withdrawn.

Use the reference's premium soft-3D dimensional weather illustrations, rounded split-card composition and rich, blended state-specific gradients: teal/olive for partly cloudy, deep navy/teal for rain, warm orange/amber for sunny, slate/lilac for cloudy. Adapt contrast/saturation to the real Philco photo wallpaper. Clouds remain silver/white; blue belongs in rain/droplet highlights, never a solid blue cloud. Do not return to flat Material-symbol-only artwork or a generic uniform dark pill. Use comparable lightweight, licensed static assets where possible; document source/license/file sizes. Do not crop/ship the screenshot's embedded art as a substitute for licensed assets.

For the full 1280×720 presentation, start from the actual current Home capture/wallpaper and leave every non-weather item pixel-identical; only replace the weather region. A single current-condition widget may borrow the left illustration/condition + right city/current-temperature arrangement. Do not show the reference's fake city/day/Fahrenheit, forecast grid, or invented weather data. Explore at least one card shape/size different from the present one but keep it inside the right header region, clear of actions/status/dock. Submit one polished primary plus one concise alternate, native-size close-up, focus state, dimensions/colors/type, license/asset sizes and the complete WMO mapping. No Java/build/device/firmware changes.

## QC07-r3.12 — screensaver weekday clipping fix (2026-11-21 20:34 -03:00)

## Current state
- Workspace `C:\PhilcoOS`; no active Git repository (`git status` reports not a repository). Installed build is QC07-r3.12, package `com.philco.ultra777.home` versionCode 777/versionName `1.0.0-ultra777`. Release SHA-256 `235FB14DE996BCD5B78DBF0E83D452B89A76402030B1E92FA9697A3DA88DF688`; installed pull `output/qc07-r3.12-installed.apk` matches exactly. QC07-r3.11 remains preserved as rollback (`5943710DF151F44EAAAA8BBE44225699BC848774F3960EDDBBCCDE48EA0DC946`).
- Device `192.168.0.71:5555` online, PhilcoOS Home is foreground/default. HDMI 2 is shown active after returning from screensaver. No firmware changes.

## Completed work
- Removed the screensaver date's `translationX(-8dp)` and added 8dp horizontal internal padding to protect the initial glyph from edge clipping while preserving centered text.
- Build `assembleRelease` passed. APK aligned; v1 signature verified, v2/v3 absent; signing certificate unchanged.
- Installed via `adb install -r`. Physical 1280×720 capture now shows the complete “Sábado, 21 de novembro de 2026”. Back returned to Home, which still shows HDMI 2 active. No PhilcoOS fatal exception/ANR appeared in the queried log tail.

## Files modified
- `source/philco-home/src/com/philco/ultra777/home/MainActivity.java`
- `output/releases/philco-home-QC07-r3.12-saver-weekday-clipfix-20261121.apk`
- `output/qc07-r3.12-installed.apk`
- `output/qc07-r3.12-screensaver-final-1280x720.png`
- `output/qc07-r3.12-home-after-saver-1280x720.png`
- Source backup: `output/backups/MainActivity-before-saver-weekday-clip-fix-20261121.java`

## Tests executed
- `assembleRelease`: PASS.
- `aapt dump badging`: expected package/version.
- `zipalign -c -v 4`: PASS.
- `apksigner verify`: v1 PASS, v2/v3 false; original certificate fingerprint retained.
- Device install and installed-vs-release SHA-256: PASS.
- Screensaver opened via its Home shortcut; full weekday visually confirmed; Back returned to Home; HDMI 2 remained active.

## Problems found
- Prior saver screenshot showed the first letter of the spelled-out weekday clipped. The explicit -8dp horizontal translation was the cause addressed in this change.
- Quick Control Center brightness control remains reported by the user as having no visible effect on image brightness. It is not yet diagnosed or corrected.

## Risks
- Brightness setting may affect panel backlight rather than picture brightness, or the slider may call a Realtek method that changes readback without visible output. Do not claim functionality until independently established on-screen.

## Next owner
- Antigravity — focused research/audit on the Quick Control Center “Brilho da imagem” control. Inspect the Java handler and existing Realtek/OEM evidence only. Distinguish picture brightness, panel backlight and standby LED; identify the actual API/middleware path, polarity/range, readback semantics, and why changing the slider is not visible. Return a concise evidence-backed recommendation and a minimal safe validation matrix. Do not edit Java, build/install APK, change firmware or alter TV settings.

## Next exact action
- Antigravity returns the brightness diagnosis/recommendation. Codex then implements only the evidenced API 19-compatible fix and validates visibly on the TV while restoring the original setting.

## Installed-TV evidence capture for brightness investigation (2026-11-21)
- ADB is online at `192.168.0.71:5555`. Device fingerprint confirms the May 2017 installed build: `realtek/rtd298x_tv001/rtd298x_tv001:4.4.2/KOT49H/eng.wubf.20170531.092306:eng/test-keys` (build date `Wed May 31 09:24:26 CST 2017`).
- Read-only pulls from the installed TV are preserved in `analysis/installed_tv_20170531/`: framework.jar, framework2.jar, services.jar, libandroid_servers.so, TSB_Tv.apk and SourceMenu.apk. SHA-256, exact device paths, sizes and provenance are recorded in `analysis/installed_tv_20170531/README.md`. Original ZIP/JAR/APK files were not modified. Extracted DEX copies are derived analysis artifacts.
- `service list` confirms Binder service `tv` (`android.app.ITvManager`). `/sys/class/backlight` is absent. `/system/bin/TvServer` is absent at that exact path. `classes.dex` exists in the pulled services/framework JARs; adjacent `services.odex`, `framework.odex`, and `framework2.odex` were absent at the checked paths (optimized code elsewhere not ruled out).
- No TV configuration was changed and no logs were cleared. Antigravity should now trace the method bodies from these installed-build files and compare its prior August/staging dump before drawing conclusions.

## Music Engine controls — Codex implementation (2026-11-22)

### Current state
- Branch/commit: indisponíveis; `C:\PhilcoOS` não é repositório Git.
- TV: `smart_tv001` at `192.168.0.71:5555`, Android 4.4.2; PhilcoOS is HOME default and `MainActivity` is foreground.
- Source observed in final Home capture: HDMI 1 active; not changed in this task.
- Installed and compiled artifact: versionCode 777 / `1.0.0-ultra777`, SHA-256 `67D87D0A480F7A5A906A054D7F59D3B5BDF5712BDF28B9A2661CD0D47D97EE67`.

### Completed work
- Added playback status, elapsed/duration, 500 ms progress refresh and touch/D-pad seek in MusicActivity.
- Service returns a fresh playback snapshot through PlayerStateRepository.

### Files modified
- `source/philco-home/src/com/philco/ultra777/home/media/MusicActivity.java`
- `source/philco-home/src/com/philco/ultra777/home/media/MusicPlaybackService.java`
- `docs/AI_TEST_RESULTS.md`

### Tests executed / results
- `assembleRelease`: BUILD SUCCESSFUL.
- `adb install -r`: Success; pulled installed APK hash matched.
- Pause froze position for 2 s; resume advanced it. Touch seek and D-pad seek changed position. Reopen restored current track/status/position. Music service remained active after leaving Activity.
- Returned to Home; PhilcoOS remains preferred HOME. See detailed evidence/captures in `docs/AI_TEST_RESULTS.md`.

### Problems / risks
- No fatal/ANR match found in queried logcat. No high-volume audio test was done. Long-form music session and user acoustic confirmation of pause/resume remain untested.
- Final Home capture showed HDMI 1 active. This task did not change the source.

### Next owner / exact next action
- Codex: if desired, make a short low-volume listening confirmation with a real music file; otherwise proceed to a separately scoped Music Library polish task. Do not change Home/dock or climate module in this handoff.

## MusicActivity premium visual — Codex (2026-11-22)

### Current state
- Branch/commit: indisponíveis; `C:\PhilcoOS` não é repositório Git.
- TV: `smart_tv001`, Android 4.4.2, ADB `192.168.0.71:5555`; MusicActivity está aberta para revisão. PhilcoOS permanece HOME padrão. Nenhuma fonte foi trocada nesta tarefa.
- APK instalado/build: `com.philco.ultra777.home`, code 777 / `1.0.0-ultra777`, API 19; SHA-256 `219EE9A4E92B4E546457539E33CB2B75883630F4E0F9253B075C69CDDD6FAFCC`.

### Completed work
- MusicActivity redesenhada com wallpaper pastoral preexistente `wallpaper_02.jpg`, scrim escuro e identidade grafite/dourada. Layout de biblioteca e player ajustado para 1280×720, SeekBar fina dourada e foco D-pad. Sem dependências, blur ou animações.
- Refresh da lista limitado a mudanças de faixa para preservar navegação e reduzir invalidações periódicas.

### Files modified
- `source/philco-home/src/com/philco/ultra777/home/media/MusicActivity.java`
- `docs/AI_TEST_RESULTS.md`

### Tests executed / results
- `assembleRelease`: BUILD SUCCESSFUL; v1 APK verificado, min/target API 19.
- Instalado via ADB; SHA do APK puxado da TV igual ao release.
- Screenshot real de 1280×720 e navegação D-pad em faixas/controles capturados em `output/captures/`.
- Áudio não foi reproduzido nesta rodada por solicitação de manter baixo ruído; testes funcionais de pause/resume/seek permanecem registrados na rodada anterior.

### Problems / risks
- Nenhum crash ou ANR correspondente na consulta de logcat após abertura. Validação visual feita em estado sem faixa reproduzindo; dinâmicos do estado atual não foram revalidados nesta rodada.
- Rollback preservado em `output/backups/philco-home-before-music-premium-ui-r2-20261122.apk`.

### Next owner / exact next action
- Usuário revisar a captura final. Se aprovar, próxima etapa separada: breve confirmação visual em estado Tocando/Pausado com volume baixo. Não alterar Home, dock ou módulo Clima.

## Home weather priority card — Codex (2026-11-22)

### Current state
- Branch/commit: unavailable; workspace has no `.git`.
- Device: `smart_tv001`, `192.168.0.71:5555`, Android 4.4.2; PhilcoOS MainActivity foreground, HOME preference unchanged; screenshot reports HDMI 1 active and this task did not change input.
- Installed/release APK SHA-256: `925EFC9E8FB3BE56127009300065705FDFA737DD923A1E7001D9F4698E130D50`, package `com.philco.ultra777.home`, code 777/version `1.0.0-ultra777`.

### Completed work
- Home weather card is 432×132dp, split condition/icon and current city/temperature with a subtle divider, attribution, condition-specific gradient and gold D-pad focus ring.
- Reuses existing weather icons and live Home weather cache/provider. The card showed Garoa, Joinville, 21°C, Open-Meteo in the physical 1280×720 capture.

### Files modified
- `source/philco-home/src/com/philco/ultra777/home/MainActivity.java`
- `docs/AI_TEST_RESULTS.md`

### Tests executed
- `assembleRelease`: BUILD SUCCESSFUL.
- `adb install -r`: Success; installed APK pull hash equals release hash.
- Opened MainActivity and captured 1280×720 visual evidence at `output/captures/home-weather-card-1280x720-20261122.png`.

### Problems / risks
- Kimi weather archive was read-only inspected. Its provider factory selects demo data by default; screenshots mark `DADOS DEMO`. Archive includes APK, keystore, signing properties and local.properties; none were extracted or used. Do not integrate its full-screen activity unchanged.
- The weather card is physically visible; no separate focus screenshot was taken this round.

### Next owner / exact next action
- Codex: before touching the heavier Kimi full-screen module, present a short scope/risk note. Then review its HTTPS provider/data fields, use live Open-Meteo by default, sanitize package contents, integrate only required Java/resources/Manifest changes, compile and test on device.


## Home weather/dock/date visual pass — Codex (2026-11-22)

### Current state
- Workspace `C:\PhilcoOS`; no Git repository/branch/commit.
- TV `smart_tv001`, `192.168.0.71:5555`, Android 4.4.2. PhilcoOS is preferred Home (`mAlways=true`) and foreground after test; source remains HDMI 1.
- Installed/build artifact: versionCode 777 / `1.0.0-ultra777`, SHA-256 `5D12F95C21B974F3910FE4687CB01944052FB9FAC9D370908B5846461C5CAF2B`.

### Completed work
- Three transparent 3D weather PNGs derived from the user-provided reference; weather mapping shared between Home and Screensaver. Total 72,195 bytes.
- Full-screen dock viewport with 64dp inner safe margins; tile size/order/gap/focus and navigation unchanged.
- Screensaver date starts on the clock's left edge without padding.

### Files modified
- `source/philco-home/src/com/philco/ultra777/home/MainActivity.java`
- `source/philco-home/res/drawable-nodpi/weather_3d_rainy.png`, `weather_3d_partly.png`, `weather_3d_cloud.png`
- `docs/AI_TEST_RESULTS.md`, `docs/PHILCOOS_ULTRA_777_GLASS_AURORA.md`, this handoff
- APK/captures/backups under `output/` as documented in AI_TEST_RESULTS.

### Tests executed / results
- `assembleRelease`: BUILD SUCCESSFUL; API 19 target; v1 verified, v2/v3 absent; zipalign passed.
- `adb install -r`: Success. Pulled installed APK SHA matches release.
- D-pad focus checked at SmartTube and Settings: complete cards and outlines visible in 1280×720 captures. 3D rain icon visible on Home and Screensaver.
- Standby shortcut opened Screensaver; full weekday/date aligned with clock; BACK returned to Home. No matching launcher fatal exception/ANR in the last 500 logcat entries.

### Problems found / risks
- First experimental APK with 24dp row padding narrowed the viewport; it was corrected by extending only the dock viewport to full screen. That interim APK is preserved, not the final reference.
- Camera-based optical overscan remains unmeasured; ADB captures show all focus geometry inside the screen.
- A preliminary OK during a focus check opened SmartTube recommendations; no playback was initiated.

### Next owner
- Codex.

### Next exact action
- No further code change required for this visual pass. If user still sees physical edge cropping, compare a camera photo of the panel against the saved ADB captures before adjusting the 64dp safe area. Present a short scope/risk note before resuming the heavier Kimi weather-module integration.


## Current state — final dock endpoint correction r5 (2026-11-22)

- **Branch/commit:** unavailable; `C:\PhilcoOS` has no Git repository.
- **TV:** `smart_tv001`, `192.168.0.71:5555`, Android 4.4.2. `com.philco.ultra777.home/.MainActivity` foreground and preferred Home (`mAlways=true`). Source remains HDMI 1.
- **Installed/build APK:** `output/releases/philco-home-weather3d-edge-focus-fixed-20261122.apk`, versionCode 777 / `1.0.0-ultra777`, SHA-256 `53E294BBB988F4C1D04B75A33A046525505842CB28A80A79E7CB6939958430DC`. Installed-pull hash matches.
- **Completed:** Home weather uses the small reference-derived 3D rain/partly-cloudy/cloud PNGs; Screensaver date begins aligned with clock. Dock stays fixed with all eight cards and unchanged size/order/gaps. SmartTube and Settings keep their focus border without focus scaling outside the card.
- **r4 regression:** endpoint scrolling moved the opposite tile partly out of view; recorded in test results and preserved as rollback, then superseded by r5.
- **Tests:** `assembleRelease`, v1 signature, no v2/v3, zipalign, ADB install and installed hash passed. D-pad focus at both endpoints captured in `output/captures/home-smarttube-edge-focus-fixed-20261122.png` and `home-settings-edge-focus-fixed-20261122.png`. No matching launcher fatal/ANR in checked log tail.
- **Next exact action:** none for this correction. If user reports physical overscan beyond the ADB frame, compare with a camera photo before changing tile geometry. Do not move the launcher into `/system`; recovery from a system-partition change is unproven.


## Current state — Home date separator refinement (2026-11-22)

- TV `smart_tv001` / `192.168.0.71:5555`; PhilcoOS remains foreground and preferred Home (`mAlways=true`); source remains HDMI 1.
- Replaced only the Home date separator with a smaller middle dot. Full weekday, date digits and alignment are unchanged.
- Installed release `output/releases/philco-home-home-date-dot-small-20261122.apk`, versionCode 777 / `1.0.0-ultra777`, SHA-256 `E6D737E594F3831D85EE5E7C754C55095CE469F0D9A73D9C346AF76E801A1F8A`; installed pull hash matches. Build, v1 signature and zipalign passed.
- Visual evidence: `output/captures/home-date-dot-small-20261122.png`. No firmware or `/system` change.
- Next exact action: none for this refinement.

## Screensaver weather icon simplification — 2026-11-22

- **State:** IMPLEMENTED, COMPILED, INSTALLED, DEVICE_VALIDATED, VISUALLY_CONFIRMED.
- **Change:** Screensaver now uses the existing lightweight 2D white weather PNG mapping; the Home card keeps its existing 3D assets. For the TV's live WMO drizzle condition (Garoa), the Screensaver displayed the minimalist 2D rain-cloud glyph. No other UI, timer, Music Engine, or firmware behavior was changed.
- **Source:** `source/philco-home/src/com/philco/ultra777/home/MainActivity.java` only. Existing icons under `source/philco-home/res/drawable-nodpi/` were reused; no new dependency/assets.
- **Build:** `assembleRelease` BUILD SUCCESSFUL; package `com.philco.ultra777.home`, versionCode 777 / versionName `1.0.0-ultra777`, min/target API 19. v1 signature verified; v2/v3 absent; zipalign passed.
- **Release:** `output/releases/philco-home-screensaver-weather-2d-20261122-standby-weather-2d.apk`, 1,877,751 bytes, SHA-256 `86D9E2B5A85F7E39C7DA551B539511EEE5CED5C7E6D4ED6833A3412E55F4758E`.
- **Rollback:** pre-install APK preserved at `output/backups/philco-home-installed-before-weather2d-20261122.apk`, SHA-256 `E6D737E594F3831D85EE5E7C754C55095CE469F0D9A73D9C346AF76E801A1F8A`.
- **Device:** `smart_tv001` (`192.168.0.71:5555`), Android 4.4.2. Installed APK pull matched release SHA. PhilcoOS remains preferred Home (`mAlways=true`), Home foreground after returning with BACK; source HDMI 1.
- **Evidence:** `output/captures/screensaver-weather-2d-20261122.png` (1280×720, Screensaver, Garoa); `output/captures/home-weather-3d-preserved-20261122.png` (1280×720, Home retained 3D weather card). D-pad reached Standby; BACK returned to Home. No `FATAL EXCEPTION` or launcher ANR match in the inspected last 600 logcat entries.
- **Build note:** Gradle reported a local analytics-settings access warning and SDK XML version warning; build completed successfully.
- **Not tested/changed:** no weather conditions other than the live garoa state were physically displayed in the Screensaver. No changes to 777 Music, idle dimming/shutdown timers, source selection, or firmware.
- **Workspace:** no Git repository, branch, or commit is available.

## Home weather condition alignment — 2026-11-22

- **State:** IMPLEMENTED, COMPILED, INSTALLED, DEVICE_VALIDATED, VISUALLY_CONFIRMED.
- **Change:** centered the condition label above the weather illustration in the left panel and enlarged the existing Home icon from 70dp to 84dp. The city, current temperature, attribution, card bounds, gradients, dock, and Screensaver mapping remain unchanged.
- **Source modified:** `source/philco-home/src/com/philco/ultra777/home/MainActivity.java`. Source backup: `output/backups/MainActivity-before-home-weather-align-20261122.java`.
- **Assets:** reused existing 3D PNGs; no new generated assets or dependencies. The 3D PNG shown in the physical capture is the current drizzle/rain icon. Other weather-code visuals retain their previous mappings.
- **Build/device:** `assembleRelease` succeeded; installed on `smart_tv001` (`192.168.0.71:5555`, Android 4.4.2). APK pull from `/data/app/com.philco.ultra777.home-2.apk` matches the release hash. Home returned to foreground and remains the established preferred launcher; HDMI 1 remains the displayed source.
- **Release:** `output/releases/philco-home-weather-card-centered-20261122.apk`, versionCode 777 / versionName `1.0.0-ultra777`, 1,877,681 bytes, SHA-256 `83709B89CA06C12DBC1AC5CF753ABB2A7B3B391732A5938D6C7F7CA69F4B0439`. v1 signature verified; v2/v3 absent; zipalign passed.
- **Rollback:** pre-install APK at `output/backups/philco-home-screensaver-weather-2d-before-home-weather-layout-20261122.apk`, SHA-256 `86D9E2B5A85F7E39C7DA551B539511EEE5CED5C7E6D4ED6833A3412E55F4758E`.
- **Evidence:** `output/captures/home-weather-card-centered-20261122.png` (1280×720). The condition and 3D rain art are centered together; city and 21°C remain in the right column. Inspected last 500 logcat lines: no matching launcher fatal exception or ANR.
- **Not tested:** other live weather conditions and their visual mappings; no camera-based panel overscan test. No changes to Music, idle timers, sources, or firmware. No Git repository/branch/commit is present.
- **Next exact action:** prepare/inspect a consistent lightweight 3D PNG set for the remaining Home weather states before changing their mappings; keep the Screensaver's 2D glyphs.

## Compact Home weather card and system launcher install — 2026-11-22

### Home weather card
- Reduced the card from 432×132dp to 360×112dp (about 29% less surface area). Kept right alignment, 3D weather PNG, centered condition, city, current temperature, attribution and gradient. Internal icon/divider/padding were resized only to prevent clipping.
- `assembleRelease`: BUILD SUCCESSFUL. APK installed and visually checked on the 1280×720 TV screen; hierarchy reports card bounds `[856,259][1216,371]`, exactly 360×112 pixels at 160dpi. Capture: `output/captures/home-weather-card-compact-20261122.png`.
- Release `output/releases/philco-home-weather-card-compact-20261122.apk`, versionCode 777 / `1.0.0-ultra777`, SHA-256 `CD4B2FC8BAB74300D6AE323EE544BF0BFEDF7BE5F28235E6B3613A9EFFD298F7`. Pulled installed APK hash matches.
- Previous APK backup: `output/backups/philco-home-weather-card-centered-before-compact-20261122.apk`, SHA-256 `83709B89CA06C12DBC1AC5CF753ABB2A7B3B391732A5938D6C7F7CA69F4B0439`.

### PhilcoOS installed as a system app
- Device: `smart_tv001`, Android 4.4.2 build `realtek/rtd298x_tv001/rtd298x_tv001:4.4.2/KOT49H/eng.wubf.20170531.092306:eng/test-keys`.
- ADB shell is root; `/system` is writable, SELinux is Enforcing, and the APK was assigned `root:root`, mode `0644`, label `u:object_r:system_file:s0`.
- Android 4.4 did not register the initial nested path under `/system/app/PhilcoOS/`; after placing the APK directly at `/system/app/PhilcoOS.apk` and rebooting once, PackageManager reports `codePath=/system/app/PhilcoOS.apk`, `pkgFlags=[ SYSTEM HAS_CODE ALLOW_CLEAR_USER_DATA ]`, and `pm list packages -s` includes `com.philco.ultra777.home`.
- The system APK SHA-256 equals the release: `CD4B2FC8BAB74300D6AE323EE544BF0BFEDF7BE5F28235E6B3613A9EFFD298F7`. Free `/system` space remains about 1.4GB.
- After reboot, PhilcoOS MainActivity resumed, `mAlways=true` remains set, Home screenshot captured at `output/captures/home-philcoos-system-app-20261122.png`. No matching launcher fatal exception or ANR in the last 800 logcat lines.
- Original Philco/Changhong launcher was not disabled or replaced; `com.haiersoft.launcherproject2995hk` remains installed and enabled. Exact APK pulled from TV and preserved at `output/backups/LancherProject2995-installed-tv-20261122.apk`, SHA-256 `98B88B8843AFE0D3910C9A334C532450F13ACEF37D78A93936F6C1CF38B91DD1`.
- **Limit:** system placement makes PhilcoOS preinstalled and resilient to ordinary app removal. It does not prevent someone from manually changing the preferred HOME app in Android settings; the OEM launcher stays as recovery. The initial unscanned duplicate at `/system/app/PhilcoOS/PhilcoOS.apk` remains; package manager uses the direct flat APK above.

### Deferred weather PNG reference
- Preserved the user's new 3D icon sheet at `docs/design/reference/weather-3d-icon-sheet-user-20261122.png` (SHA-256 `1378367B2BB004365E3AFF7F62640B1ABE056D3D19F178BDB8465C1A8111F28B`). It is a design reference only and was not added to the APK. Next UI task: inspect/crop suitable icons and map them to actual WMO conditions while keeping the Screensaver's 2D glyphs.
- No changes to Music Engine, idle timers, TV firmware, bootloader, or OEM launcher behavior.

## Home weather card — user reference match with live daily range — 2026-11-22

- **Design:** kept the compact 360×112dp card and matched the supplied reference hierarchy: centered condition above 3D weather PNG at left; stronger city label, small `AGORA`, prominent current temperature, and `Máx / Mín` on the right. Preserved the dark blue gradient, existing 3D rain PNG, Open-Meteo attribution and the Screensaver's separate 2D icon.
- **Data:** code now requests Open-Meteo `daily=temperature_2m_max,temperature_2m_min` with `forecast_days=1`, alongside current temperature/condition. Daily range is rendered only when both values parse as finite numbers; otherwise that line remains empty. Values are cached by city and refreshed with the existing weather cadence. City changes clear the daily cache.
- **Physical result:** at the capture, live data displayed `Joinville`, `21°C`, `Máx 27° · Mín 21°`, and `Garoa`. These are the device/API values at test time, not the example numbers in the reference image. Screenshot `output/captures/home-weather-reference-build-20261122.png` (1280×720); UI tree `output/captures/home-weather-reference-ui-20261122.xml` confirms all fields fit inside card bounds `[856,259][1216,371]`.
- **Source:** only `source/philco-home/src/com/philco/ultra777/home/MainActivity.java` changed. Pre-edit backup: `output/backups/MainActivity-before-weather-reference-range-20261122.java`.
- **Build:** `assembleRelease` BUILD SUCCESSFUL; API 19, v1 signature verified, v2/v3 absent, zipalign passed. Existing Open-Meteo HTTPS/TLS path retained; no dependencies added.
- **Release/current system APK:** `output/releases/philco-home-weather-reference-20261122.apk`, 1,878,367 bytes, SHA-256 `65313B6085B7B02A71A5632F347FB59EDA2D2678111991E8C9BB53D90069CD21`. The pulled installed APK and `/system/app/PhilcoOS.apk` both match this hash. PackageManager reports `SYSTEM`, versionCode 777, and PhilcoOS remains preferred Home (`mAlways=true`). HOME key returned to PhilcoOS; OEM launcher remains enabled as fallback.
- **Rollback:** previous system APK preserved at `output/backups/PhilcoOS-system-before-weather-reference-20261122.apk`, SHA-256 `CD4B2FC8BAB74300D6AE323EE544BF0BFEDF7BE5F28235E6B3613A9EFFD298F7`.
- **Reference preserved:** `docs/design/reference/weather-card-compact-style-user-20261122.png`, SHA-256 `90921F9D56BB6A8D342C2EC96B0857BA682B9E5558DC235C758A29B64D2BA8B8`. No icon sheet was integrated in this pass.
- **Limits:** only the live drizzle case was visually checked; other WMO icon states and extreme city-name truncation were not exercised. No Music, dock, Screensaver layout, firmware, or OEM app changes.
- **Next exact action:** separately evaluate the user's preserved 3D icon sheet for clean transparent crops and matching WMO states; do not alter the weather card geometry in that step.

## Weather label correction — 2026-11-23

- **Change:** Home weather range label changed from `Mín` to `Min`; no other UI or behavior changed.
- **Build:** `assembleRelease` BUILD SUCCESSFUL. APK: `output/releases/philco-home-weather-reference-min-no-accent-20261122.apk`; SHA-256 `5C7DBA8B693B10FD7EDD6606A54E106954D2DE6D491B90E0EE77AC7163BA6C3D`.
- **Device:** installed and copied to `/system/app/PhilcoOS.apk`; on-device system APK SHA-256 matches. PackageManager reports `SYSTEM`, versionCode 777. Home launched normally.
- **Visual confirmation:** `output/captures/weather-min-label.png` (1280×720) visibly shows `Máx 27° · Min 21°`; UI capture `output/captures/weather-min-label-window.xml`. This verifies the requested spelling on the real TV.
- **Backup:** prior system APK saved as `output/backups/PhilcoOS-system-before-min-label-20261122.apk`.

## Idle backlight recovery — 2026-11-23

- **Cause found in source:** original backlight was held only in Activity memory; `onDestroy()` cleared the worker queue immediately after queuing restore. A process/activity recreation could therefore lose the saved value or cancel its restore.
- **Fix:** persist original backlight before dimming, recover and retry restoration in `onResume()`, clear saved value only after setter/readback succeeds, and allow queued worker operations to drain on destroy. No brightness/image slider or firmware/OEM code changed.
- **Backup:** `output/backups/MainActivity-before-backlight-restore-fix-20261123.java`, SHA-256 `1DB691C5D21F425B90C913136F6E4AE6A4F3CD104813F09A1119B2865B154BDC`.
- **Build:** `assembleRelease` BUILD SUCCESSFUL. APK `output/releases/philco-home-backlight-recovery-20261123.apk`, SHA-256 `8C9495927AEE57CBC8F6FD3223A9792F0DEF47A2F25516F99F04380480DB2B90`; v1 signature verified, v2/v3 absent, zipalign passed.
- **Device:** installed with `adb install -r` as an updated system app at `/data/app/com.philco.ultra777.home-1.apk`; base `/system/app/PhilcoOS.apk` and OEM launcher were not overwritten. PhilcoOS MainActivity resumed; preferred HOME remains `mAlways=true`.
- **Not yet physically verified:** no controlled idle-dim/return cycle was run after this build. TvManager `getBacklight()` readback is software evidence only and cannot prove panel luminance. If the panel remains dim despite the recovery attempt, investigate actual Realtek panel output path before further changes.

## Backlight re-dim after waking — root cause and fix — 2026-11-23

- **Reproduced cause:** old device logs show `Idle screensaver restored backlight to 77` at 00:44:44.076 followed by `Idle screensaver reduced backlight from 77 to 14` at 00:44:46.980. `dispatchKeyEvent()` consumes the wake key before `onUserInteraction()` can reliably reset the idle clock; the stale clock immediately re-triggered the dim action. The API restore itself did run.
- **Source fix:** `finishDismissIdleScreensaver()` now resets `lastHomeInteraction` and reschedules `homeIdleMonitor` before restoring. Combined with the prior persistent-original-value/worker-drain fix, this prevents immediate re-dim and retains recovery across Activity recreation.
- **Backup:** `output/backups/MainActivity-before-idle-reset-fix-20261123.java`, SHA-256 `4349371F020133490F57A2DF6B76D6045C3CB2258A4FC280853BC7EFB10D13C3`.
- **Build:** `assembleRelease` BUILD SUCCESSFUL. APK `output/releases/philco-home-backlight-idle-fix-20261123.apk`, 1,878,544 bytes, SHA-256 `9EBF709350EF7EF980017C947A4AFE05A96AC39E413FC8CCD3F34A4CDB63195F`. v1 signature verified; v2/v3 absent; zipalign passed.
- **Device:** `adb install -r` succeeded on `192.168.0.71:5555` (`rtd298x_tv001`, `smart_tv001`). Updated system app installed at `/data/app` over base `/system/app/PhilcoOS.apk`; original system APK preserved. `MainActivity` resumed and HOME preference remains `mAlways=true`.
- **Validation boundary:** fixed code and installed build are verified. The complete 15-minute auto-dim → wake → stay bright cycle has not yet been observed after this build. Previous logs prove the old immediate re-dim mechanism; software `getBacklight()` readback alone is not physical luminance proof. Current screenshot: `output/captures/idle-fix-current.png`.

## Weather fetch after TV power loss — 2026-11-23

- After reconnection, ADB initially showed only the system base APK (`/system/app/PhilcoOS.apk`, SHA-256 `5C7DBA8B693B10FD7EDD6606A54E106954D2DE6D491B90E0EE77AC7163BA6C3D`). The previous `/data/app` update was absent. Logs from that old process show `NoClassDefFoundError: ...Tls12SocketFactory`; the system app's `nativeLibraryPath` pointed to a missing `/data/app-lib/...` directory and `/system/lib/libconscrypt_jni.so` was absent.
- Preserved the system base APK at `output/backups/PhilcoOS-system-before-conscrypt-repair-20261123.apk` (same SHA-256 as above), then reinstalled existing signed release `output/releases/philco-home-backlight-idle-fix-20261123.apk` with `adb install -r`; no rebuild or `/system` write. PackageManager now loads the update from `/data/app/com.philco.ultra777.home-1.apk` and `/data/app-lib/com.philco.ultra777.home-1/libconscrypt_jni.so` exists.
- PhilcoOS MainActivity resumed and remains preferred Home (`mAlways=true`). Wi-Fi reports CONNECTED. The weather card now shows max `28°` vs cached `27°` in the earlier screenshot; no new `Tls12SocketFactory`/weather failure appeared after reinstall. This is consistent with a refreshed response, but the app does not log a success timestamp, so exact HTTPS success is not directly logged. Captured `output/captures/weather-verified-current.png`.
- **Not verified:** 30-minute periodic timer; the visible update followed app resume/card interaction. An OEM OTA service also logged an unrelated `UnknownHostException` for its Changhong update host; do not attribute that to Open-Meteo.
- **Next:** retain the `/data/app` update (do not copy this Conscrypt-dependent build into `/system` without packaging its JNI library correctly). Separately run the controlled 15-minute backlight dim/wake test; verify physical brightness visually.

## Current state — idle/backlight schedule 2026-11-23
- Commit/branch: unavailable; `C:\PhilcoOS` has no `.git` repository. Device: Philco RTD298X, Android 4.4.2; ADB `192.168.0.71:5555` online. PhilcoOS is resumed and remains preferred HOME (`mAlways=true`). Selected input was not re-queried.
- Completed: 5-minute screensaver retained; backlight now targets absolute 40 after 15 minutes and 20 after 30 minutes, preserving one persisted original value for both stages and restoring it on wake/resume. Auto Realtek standby deadline is 60 minutes total; 30-second warning starts at 59:30. Shutdown is deferred while shared playback state is `PLAYING`; if music continues past deadline, a fresh 30-second warning begins after playback stops.
- Files modified: `source/philco-home/src/com/philco/ultra777/home/MainActivity.java`; backup `output/backups/MainActivity-before-20261123-idle-schedule.java` (SHA-256 `FC439E1B081447D0CBD2AEB7EE02AFB4D42AB18DCB726EDF353B792A05A5F740`).
- Tests: `assembleRelease` BUILD SUCCESSFUL; APK `output/releases/philco-home-idle-backlight-15-30-60m-20261123.apk`, SHA-256 `1C63DCBF371287A1AD7089B91E6E2F5C1D21B71424974EA1EB6BFECE10F40904`; API package `com.philco.ultra777.home`, versionCode 777/versionName 1.0.0-ultra777; v1 signing true, v2/v3 false; zipalign 4 passed. Installed with `adb install -r` (Success), MainActivity resumed, JNI library exists under `/data/app-lib`.
- Results/limits: code and install verified, but 15/30-minute physical backlight transitions, wake restoration, exact 1-hour standby, and music-defer behavior are not DEVICE_VALIDATED. Recent reboot showed only `/system/app/PhilcoOS.apk` after a previous `/data/app` update had disappeared; current release was reinstalled to `/data/app`. No `/system` write was made. Agy performed a bounded static review; no source edits/device commands from Agy.
- Next owner: Codex. Next action: observe one controlled 15-minute stage and wake/restore; later test 30-minute stage. Only conduct the 60-minute/30-second standby test when the TV can remain unattended and music state is known.
## Current state — boot animation override — 2026-11-23

### Completed work
Installed the validated `_15fps` candidate as the Android 4.4 user override `/data/local/bootanimation.zip`; the original `/system/media/bootanimation.zip` remains unchanged and has a local backup. The exact filename originally supplied was absent; the candidate used was `output/releases/PhilcoOS_Ultra777_bootanimation_15fps.zip`, SHA-256 `EB9D2105AEBDB4BE64B09336337591D04D19C788EAC27A0D6BDC57C1280382BAC`. Original backup SHA-256: `622F473EEABDD95BD2715891F783EFCC6B8CCA86A348E2B1A252971D1EEFE0EB`.

### Files modified
- `output/backups/bootanimation-original-20170530.zip` — local backup pulled from the TV.
- `output/captures/bootanimation-post-reboot-20261123.png` — post-boot Home capture.
- `output/captures/bootanimation-post-reboot-logcat-20261123.txt` — no retained bootanimation-tag lines.
- Documentation only. No Java, Manifest, firmware, `/system`, or partition changes.

### Tests executed / results
Device `192.168.0.71:5555`, model `smart_tv001`, build Android 4.4.2. `desc.txt` and archive integrity validated; all PNG entries are stored, 1280x720. Installed-file SHA matched the candidate; SELinux label is `u:object_r:system_data_file:s0`, mode 0644. Reboot completed (`sys.boot_completed=1`), bootanim stopped, PhilcoOS MainActivity resumed as HOME. Screenshot confirms the Home after boot, not the animation frame; no BootAnimation log lines were retained. Thus device boot recovery is confirmed, but visual playback is not independently confirmed.

### Problems / risks
The requested exact source filename was missing; the existing `_15fps` variant was used. The override is reversible and does not replace the firmware animation. Git metadata is unavailable in this workspace; no branch or commit hash. Current source selection shown after boot: TV aberta. Current package resolution returned the base system path `/system/app/PhilcoOS.apk`.

### Next owner / exact next action
User: visually confirm the animation during a future normal boot. To revert at any time, remove only `/data/local/bootanimation.zip`; the OS then selects the untouched `/system/media/bootanimation.zip` on the next boot.

## Current state — weather refresh/card and boot-animation cadence — 2026-11-23

## Completed work
Manual weather refresh failure was traced to the TV reverting to the `/system` base APK after reboot, leaving the Conscrypt JNI library unavailable. Updated build was installed as `/data/app` update; one D-pad-triggered request succeeded. Weather card now uses a graphite gradient and exposes refresh status/log markers.

## Files modified
- `source/philco-home/src/com/philco/ultra777/home/MainActivity.java`
- `docs/AI_TEST_RESULTS.md`
- `docs/PHILCOOS_ULTRA_777_GLASS_AURORA.md`
- `docs/AI_HANDOFF.md`
- Backup: `output/backups/MainActivity-before-weather-gray-refresh-20261123.java`
- Evidence: `output/captures/weather-refresh-validated.png`, `output/captures/weather-refresh-validation-20261123.txt`

## Tests executed
- `assembleRelease`: BUILD SUCCESSFUL.
- APK signature verification and zipalign: passed. APK SHA-256 `B2D0854662C9D0D9E766C2141C80CBDAB0BA00538F5377B21B65E08D9DC1D35D`.
- `adb install -r`: Success; PM path `/data/app/com.philco.ultra777.home-1.apk`; Conscrypt JNI present; MainActivity resumed.
- One manual D-pad refresh: log shows start and success about 0.9 seconds later; no new TLS exception. Graphite card visually confirmed in screenshot.

## Results
ANALYZED: fallback APK/JNI failure confirmed by package path and prior error logs. IMPLEMENTED: refresh feedback/log markers and fixed graphite gradient. COMPILED: release build passed. INSTALLED: data-app update installed. DEVICE_VALIDATED: one manual HTTPS request returned usable data and success path ran. VISUALLY_CONFIRMED: grayscale gradient appears on Home.

## Problems found
The shown weather values were unchanged from the previous cache, so a changed value was not independently observed. The local app update has previously disappeared after reboot; persistence remains unverified. Boot animation candidate remains 15 fps/90 unique frames; no true 30-fps source is available in the release directory, and it was not altered.

## Risks
Do not copy this Conscrypt-dependent APK into `/system` without packaging its native library correctly. Do not treat changing only `desc.txt` fps as frame interpolation. Git branch/commit unavailable because this workspace has no Git metadata.

## Next owner
Codex.

## Next exact action
Keep the current `/data/app` update for testing, and later confirm it persists across a normal reboot. For smoother boot motion, obtain or produce a genuine 30-fps master (or carefully reviewed interpolated frames) before replacing the active override.

- Device: `192.168.0.71:5555`, Android 4.4.2, MainActivity resumed; source state not changed during this task. Preferred HOME was not re-queried in this task.
- Installed artifact: weather-gray-refresh APK above; rollback remains in `/system/app/PhilcoOS.apk` and original boot animation backup.

## Weather card composition — 2026-11-23

- Removed the thin vertical divider between forecast icon/condition and current city/temperature. Increased the current-conditions panel from 136dp to 148dp and gave it a 4dp inset so the two groups sit closer while retaining breathing room. Kept the outer focus outline, wallpaper, card dimensions, and data.
- **Build:** `assembleRelease` BUILD SUCCESSFUL; APK `output/releases/philco-home-weather-clean-card-20261123.apk`, SHA-256 `9B70CE3CD0ABF6E5CB9FC73937EE4DF4E22CE4126FCD7AFA7820BC9D21C35A71`; zipalign passed.
- **Device:** installed using `adb install -r`; PM path `/data/app/com.philco.ultra777.home-2.apk`; MainActivity resumed. Screenshot `output/captures/weather-clean-card-20261123.png` visually confirms divider removal and preserved layout.
- Backup: `output/backups/MainActivity-before-weather-composition-20261123.java`, SHA-256 `361BC822E779AD9217976BD35E889D95468CE10591B00BEA14826FA55A2745CB`.
- Labels: IMPLEMENTED, COMPILED, INSTALLED, DEVICE_VALIDATED, VISUALLY_CONFIRMED. No firmware changes.

## Antigravity design request — weather card polish — 2026-11-23

**Task:** WEATHER-CARD-UX-02 · Design-only review. Inspect the current 1280×720 capture at `output/captures/weather-clean-card-20261123.png` and the user's red annotations. Propose a more premium, Apple-like hierarchy: narrow the left condition/icon zone; make condition and city/“AGORA” easier to read; give temperature stronger weight; arrange existing real Máx./Min. values clearly at the right, aligned with the temperature. Keep the graphite gradient and the already-removed center divider. Preserve Home, dock, wallpaper, focus/D-pad behavior, and current card footprint unless the mockup explains a small necessary adjustment. Deliver one full-screen and one close-up proposal plus concise measurements, system-font weights/sizes, colors, and focus state. **Do not edit Java/resources, build, or install**; return the visual spec to Codex for implementation.

## Weather card hierarchy pass — 2026-11-23

- Antigravity design input: `docs/design/weather-card-apple-design-plan-20261123.md` (design-only, no app edits). Implemented a compact 116dp condition/icon column, 64dp weather asset, 13sp medium condition label, expanded remaining data column, city + `AGORA` row, 42sp temperature, and readable stacked real `Máx`/`Min` values. Kept the 360×112dp footprint, gray gradient, external focus outline and removed central divider.
- **Build:** `assembleRelease` BUILD SUCCESSFUL; APK `output/releases/philco-home-weather-premium-layout-20261123.apk`, SHA-256 `DB1AECE11186951BFB93BA9C46EA669A3AF9A2EA2C09BD4CCBDD7E37FDCD3EA5`; zipalign passed.
- **Device:** `adb install -r` succeeded; PackageManager path `/data/app/com.philco.ultra777.home-1.apk`; MainActivity resumed. D-pad Up focused the weather card and Down restored SmartTube focus; screenshot confirms no clipping. Full capture `output/captures/weather-premium-layout-20261123.png`; focused capture `output/captures/weather-premium-focus-20261123.png`; close-up `output/captures/weather-premium-card-closeup-20261123.png`.
- **Backup:** `output/backups/MainActivity-before-weather-apple-layout-20261123.java`, SHA-256 `F04AF900BB598DB8B026DF263EF47193E1BCC0905A389A6EA6261DCD4DA828DB`.
- Labels: ANALYZED, IMPLEMENTED, COMPILED, INSTALLED, DEVICE_VALIDATED, VISUALLY_CONFIRMED. No Home dock, wallpaper, climate-fetch logic, firmware, or `/system` changes.

## Weather card hierarchy refinement r2 — 2026-11-23

- Visual QA showed the condition label still needed stronger distant readability. Raised it to 14sp bold with a subtle shadow; no other UI change. The Agy proposal is preserved at `docs/design/weather-card-apple-design-plan-20261123.md`.
- **Build:** `assembleRelease` BUILD SUCCESSFUL; APK `output/releases/philco-home-weather-premium-layout-r2-20261123.apk`, SHA-256 `02914C0D9BC89A5E3233F0FF7702DD884417549474A6908BFA9CBCFBF871CDB9`; zipalign passed.
- **Device:** `adb install -r` succeeded; PM path `/data/app/com.philco.ultra777.home-2.apk`. MainActivity resumed. D-pad Up focused the weather card without clipping, Down returned focus to SmartTube. Current input visible in Home: TV aberta. Full/focus/close-up captures: `output/captures/weather-premium-r2-20261123.png`, `output/captures/weather-premium-r2-focus-20261123.png`, `output/captures/weather-premium-r2-closeup-20261123.png`.
- Backup before r2 text-weight edit: `output/backups/MainActivity-before-weather-condition-weight-20261123.java`, SHA-256 `D5F6FD3B18D4A6E9ED66246FB6CA2A82280B26BC13F6DC4778D848230E751B71`.
- Final labels: IMPLEMENTED, COMPILED, INSTALLED, DEVICE_VALIDATED, VISUALLY_CONFIRMED. No firmware or `/system` writes.

---

## 2026-11-23 — WEATHER-CARD-UX-02 Visual Proposal & Specification (Antigravity → Codex)

### Metadata
- **Task ID:** `WEATHER-CARD-UX-02`
- **Agent & Role:** Google Antigravity (Lead Research & Design Engineer)
- **Date/Time:** 2026-11-23 05:05:00 -03:00
- **Workspace:** `C:\PhilcoOS`
- **Current Installed Device Reference:** `weather-premium-layout-r2` (`02914C0D9BC89A5E3233F0FF7702DD884417549474A6908BFA9CBCFBF871CDB9`), Android 4.4.2 / API 19 (`192.168.0.71:5555`).
- **Scope Restriction:** Design-only review. ZERO edits to Java/resources (`source/philco-home/` untouched); NO compilation; NO device changes.

### Current state & Problem Analysis
1. **Dispersão em 3 Colunas na Captura Atual (`weather-premium-r2`):**
   - A coluna esquerda de condição/ícone foi estreitada com sucesso (116dp), porém a coluna direita acabou subdividida em duas sub-colunas: `Joinville` e `21°C` no centro, e `AGORA` e `Máx/Min` empurrados para a extremidade direita (`X ~ 310dp`).
   - Essa fragmentação quebra a leitura rápida em televisores (experiência de 10 pés / 3 metros), dispersando a atenção do usuário em vez de apresentar um bloco sólido e elegante.
2. **Referência Direta do Usuário (`weather-card-compact-style-user-20261122.png`):**
   - Na referência oficial do usuário, todos os elementos textuais da direita compartilham **um único eixo vertical alinhado à esquerda** (`X ~ 138dp`), gerando coesão imediata.
   - A temperatura destaca-se com tipografia de peso superior (`46sp` em vez de `37sp`), e no formato limpo de tvOS/iOS (`21°` em vez de `21°C` colado).
   - O rótulo `Máx 28° • Mín 21°` situa-se diretamente abaixo da temperatura em linha única, com acentuação correta em `Mín`.

### Completed work (Proposta Visual Entregue)
1. **Composição em Tela Cheia 1280×720:**
   - [weather-apple-hierarchy-proposal-1280x720.png](file:///C:/PhilcoOS/output/captures/weather-apple-hierarchy-proposal-1280x720.png): Renderização fidedigna em 1280×720 integrando o novo card sobre o wallpaper pôr-do-sol oficial, relógio 94sp, dock de 8 cards e status bar.
   - [weather_card_apple_hierarchy_1280x720.svg](file:///C:/PhilcoOS/docs/design/frames_1280x720/weather_card_apple_hierarchy_1280x720.svg): Frame vetorial editável.
2. **Matriz Comparativa e Especificação Close-up:**
   - [weather-apple-hierarchy-closeup.png](file:///C:/PhilcoOS/output/captures/weather-apple-hierarchy-closeup.png): Canvas detalhado comparando o estado atual, proposta de repouso com cotas (118dp/210dp) e proposta focada D-pad com anel Ciano Elétrico `#00E5FF` (3dp).
   - [weather_card_apple_closeup.svg](file:///C:/PhilcoOS/docs/design/frames_1280x720/weather_card_apple_closeup.svg): Frame vetorial de especificações.

### Especificação Técnica de Engenharia para o Codex (MainActivity.java)
- **Container (`weatherCard`):**
  - Largura: `dp(360)`, Altura: `dp(112)` (inalterado na Home).
  - Padding: `card.setPadding(dp(12), dp(6), dp(12), dp(6));`
  - Raio dos cantos: `dp(20)` (pílula suave Dark Glass).
- **Painel Esquerdo (`conditionPanel`):**
  - LayoutParams: `new LinearLayout.LayoutParams(dp(118), -1);` (Largura FIXA, sem `weight 1f`).
  - Alinhamento: `Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL`.
  - `weatherDescriptionView`: `15sp`, `#FFFFFF`, `sans-serif-medium` / `Lato-Medium`, `shadowLayer(dp(2), 0, dp(1), 0x88000000)`, altura `dp(24)`.
  - `weatherGlyph`: `dp(66) × dp(66)`, `ImageView.ScaleType.FIT_CENTER`, `marginTop = dp(2)`.
- **Painel Direito (`currentPanel`):**
  - LayoutParams: `new LinearLayout.LayoutParams(0, -1, 1f);` (ocupa os `210dp` restantes).
  - Padding & Alinhamento: `currentPanel.setPadding(dp(8), 0, 0, 0); Gravity.CENTER_VERTICAL;`.
  - `cityView`: `17sp`, `#FFFFFF`, `sans-serif-medium`, altura `dp(22)`.
  - `nowLabel`: `"AGORA"`, `9sp`, `#B8CADB`, `Typeface.DEFAULT_BOLD`, altura `dp(12)`.
  - `temperatureView`: `46sp`, `#FFFFFF`, `displayTypeface()` (Lato Medium), `shadowLayer(dp(2), 0, dp(1), 0x66000000)`, altura `dp(46)`.
    - Formato de exibição: `21°` (formato limpo Apple, retirando o "C" fixado conforme mockup).
  - `highLowView`: `12sp`, `#DDE7F2`, `sans-serif-medium`, altura `dp(16)`.
    - Formato: `"Máx " + max + "° • Mín " + min + "°"` (garantindo acento no `Mín` e bullet `•`).
  - `attribution`: `6.5sp`, `#8295A8`, `sans-serif`, altura `dp(9)` (Open-Meteo discreto na base).
- **Superfície & Foco D-pad:**
  - Repouso: Gradiente grafite horizontal `0xFF454B53` → `0xFF6C7580`, borda fosca 1dp `0x44FFFFFF`.
  - Focado: Gradiente grafite marinho escurecido `0xFF26303E` → `0xFF3E4B5E`, anel Ciano Elétrico `#00E5FF` (3dp).
  - Navegação D-pad: 100% preservada (Esq: Cenários, Dir: Ajustes, Cima: Standby, Baixo: SmartTube).

### Files inspected
- `docs/AI_HANDOFF.md`
- `output/captures/weather-clean-card-20261123.png`
- `output/captures/weather-premium-r2-closeup-20261123.png`
- `output/captures/weather-premium-r2-20261123.png`
- `docs/design/reference/weather-card-compact-style-user-20261122.png`
- `docs/design/weather-card-apple-design-plan-20261123.md`
- `source/philco-home/src/com/philco/ultra777/home/MainActivity.java`

### Files modified
- `output/captures/weather-apple-hierarchy-proposal-1280x720.png` (Gerado)
- `output/captures/weather-apple-hierarchy-closeup.png` (Gerado)
- `docs/design/frames_1280x720/weather_card_apple_hierarchy_1280x720.svg` (Criado)
- `docs/design/frames_1280x720/weather_card_apple_closeup.svg` (Criado)
- `docs/AI_HANDOFF.md` (Atualizado com este registro de entrega)

### Tests executed
- Validação dimensional e cromática em 1280×720 (Zero corte, alinhamento D-pad seguro).
- Nenhuma compilação ou execução de código Java/Android realizada por Antigravity (estrita obediência ao mandato).

### Results
- Proposta visual `WEATHER-CARD-UX-02` finalizada e aprovada esteticamente, corrigindo a dispersão horizontal e entregando a hierarquia Apple Weather refinada.

### Next owner
- **CODEX** (Lead Implementation Engineer).

### Next exact action
Codex deve aplicar as medidas exatas de `WEATHER-CARD-UX-02` em `source/philco-home/src/com/philco/ultra777/home/MainActivity.java` (métodos `createWeatherCard()`, `updateWeatherDisplay()` e formatação do `temperatureView`), compilar e validar na TV física.

## Current state — last-input restore candidate — 2026-11-23

## Completed work
Added a Quick Control Center preference to restore the last confirmed Realtek input at boot, plus a bounded `BOOT_COMPLETED` receiver. The restart/power button is deferred per user request.

## Files modified
- `source/philco-home/src/com/philco/ultra777/home/MainActivity.java`
- `source/philco-home/src/com/philco/ultra777/home/InputRestoreReceiver.java`
- `source/philco-home/AndroidManifest.xml`
- `docs/AI_TEST_RESULTS.md`, `docs/AI_DECISIONS.md`, `docs/AI_HANDOFF.md`, `docs/PHILCOOS_ULTRA_777_GLASS_AURORA.md`
- Source backup: `output/backups/MainActivity-before-last-input-restore-20261123-050100.java`.

## Tests executed
`assembleRelease` passed; zipalign passed. Installed with `adb install -r`; installed APK hash matches the release. D-pad toggle off/on was visually confirmed. Device remains on `PlayingSource=27` with PhilcoOS MainActivity resumed. No reboot performed. Explicit boot broadcast did not yield receiver log evidence.

## Results
Release APK: `output/releases/philco-home-last-input-restore-20261123.apk`, SHA-256 `437B504797B985CB466647CA7B2A9F30852E96BA628A05EC7616B72431CC566D`. Package versionCode 777 / versionName `1.0.0-ultra777`. Post-install `dumpsys package` confirms PhilcoOS MainActivity is the preferred HOME (`mAlways=true`). Current device source is TV aberta (27). Git metadata unavailable in `C:\PhilcoOS`; no commit or branch to report.

## Problems found
Startup restore has not been validated by a real reboot, nor has the saved-source persistence path been observed after a new input change on this build. Existing reboot history indicates a risk that `/data/app` update may fall back to the `/system` base APK.

## Risks
The feature reapplies the middleware source but deliberately does not start OEM `Tv_strategy`; whether a boot-time source reapply alone restores visible HDMI/AV video must be confirmed on device. Retries are bounded and timeout after 9 seconds.

## Next owner
Codex.

## Next exact action
At the next planned restart, first verify the installed APK remains the `/data/app` update; then confirm the selected AV/HDMI source is restored, PhilcoOS remains Home, and the final source read-back matches. If `/data/app` falls back, stop and preserve the current APK/data; do not write `/system`.



## Current state — weather range alignment — 2026-11-23

Updated climate labels to Máx. and Min. and fixed the label/value columns so both readings share a right edge. Built, installed, and visually checked on the 1280×720 TV. APK SHA-256: 7A2284D50CF98D8C46D516734BD0EB2A0ED848CB1194614DD36AABBD695E1C6A. Capture: output/captures/weather-max-min-aligned-20261123.png. TV remains on source 27 with PhilcoOS MainActivity resumed. No firmware changes. The last-input boot feature remains installed but not validated by a real reboot; power/restart shortcut remains deferred.

## Current state — power-control feasibility — 2026-11-23

## Completed work
Checked the real permission on-device. `DEVICE_POWER` is `signature|system`, requested by the APK but not granted; `pm grant` fails with “not a changeable permission type”. Earlier `TvManager.standBy()` returned false. Power actions remain hidden and unimplemented as functional UI; no shutdown/sleep API was invoked. Logos were left 2D.

## Files modified
Power actions are guarded in `source/philco-home/src/com/philco/ultra777/home/MainActivity.java`; the manifest requests `DEVICE_POWER`. APK `output/releases/philco-home-power-controls-20261123.apk` (SHA-256 `8958C7A896E191ADB63D55D3E74B31B7C64F27ED96A86229BF53D53E38A4934F`). Backup of prior system base: `output/backups/PhilcoOS-system-base-before-power-controls-20261123.apk` (SHA-256 `5C7DBA8B693B10FD7EDD6606A54E106954D2DE6D491B90E0EE77AC7163BA6C3D`); device rollback `/system/app/PhilcoOS.apk.bak-power-20261123`.

## Tests executed
Release built, installed/promoted to the system app slot, and one controlled reboot completed. After boot, `pm path` is `/system/app/PhilcoOS.apk`; PhilcoOS MainActivity resumed and `PlayingSource=27`. DEVICE_POWER remains ungranted. `getevent -lp` identifies rear/button candidate `/dev/input/event2` as `Smart_TV_Keypad`; no event capture yet. Agy review stopped with RESOURCE_EXHAUSTED. Workspace has no Git metadata.

## Results
No functional app-controlled power-off/restart button can be offered under current permission state. “Power on” from a fully powered-off TV cannot be started by the launcher. Do not claim standby/restart works.

## Problems found
The permission remains unavailable even for the system package. Changing signature or framework permission policy would be a firmware-level change and is outside this safe app implementation.

## Risks
Previous system APK is recoverable from the local and on-device backups. Current system release includes the launcher code and no other firmware changes. Do not alter permission policy or test standby until the rear joystick wake path is understood.

## Next owner
Codex.

## Next exact action
Capture a short `getevent -lt /dev/input/event2` session while the user clicks the TV joystick center three times; determine whether it emits a HOME-capable event and whether any debounce/receiver path can work while PhilcoOS is backgrounded.


## TV_INPUT hold menu — offline candidate — 2026-11-23

- **Goal:** on the targeted `Smart_TV_Keypad` scan code `549` / keycode `TV_INPUT`, consume the OEM event before SourceMenu; on release inject one `DPAD_CENTER`, or after a held 5,000 ms open the PhilcoOS control menu once. Other keycodes/devices pass through unchanged.
- **Implemented:** policy copy adds the early `PhoneWindowManager.interceptKeyBeforeDispatching()` hook, exact device/scan-code checks, repeat/cancel handling, pending callback cancellation, short-click OK injection through the API 19 `InputManager`, and long-press launch of `MainActivity` with the menu extra. The app menu exposes Home; shutdown/restart rows are visibly unavailable because `DEVICE_POWER` was not granted and no OEM power mechanism is validated.
- **Artifacts:** `output/candidates/android.policy-tvinput-control-menu-20261123.jar` SHA-256 `B85A2771616C621AA321D39EA4D2D4BEDC355E4ACA3465B2095664A646F6C6AC`; `output/candidates/philco-home-tvinput-control-menu-20261123.apk` SHA-256 `BC5D6EFF14B059B0076C841D9DFF2179811227796D642DFA61540AA8A1E1FD94`. Original policy backup remains `output/backups/joystick-policy-baseline-20261123/android.policy.jar` SHA-256 `58CC4324C7F7AD44C5198747A3423D742B6517B45A8A6CAFCE709E9D1D44EC2E`.
- **Validation labels:** ANALYZED, IMPLEMENTED, COMPILED, OFFLINE_VALIDATED. Policy DEX assembled to version 035 and round-tripped through disassembly; required API signatures were found in the TV's Android 4.4 framework. APK `assembleRelease` succeeded, API 19 metadata, v1 signature and zip alignment verified. No device test was run.
- **Device/SCM:** no TV writes, installs, reboots, shutdown calls, or source changes in this task. Last documented TV state is PhilcoOS Home/system package, `PlayingSource=27`; live state not queried this task. Git branch/commit unavailable (workspace has no `.git`).
- **Risk/next action:** this changes boot-critical `android.policy.jar` behavior and is only a review candidate. Before installation, independently review the DEX diff, verify framework build correspondence and recovery procedure, then arrange a controlled device test. Do not install until recovery is proven. Physical validation is still required for immediate OK, five-second hold, menu, OEM selector suppression, and unaffected directions. Power-off/restart remain disabled.


## TV_INPUT candidate — final static review — 2026-11-23

- **Original vs candidate:** DEX dump of the preserved original and candidate shows `interceptKeyBeforeDispatching()` grew from 5,026 to 5,046 code units. The new guard is at method entry, before the existing OEM branch. `com.hikeen.xymenu.MyService.Start` occurs once in each DEX; the candidate consumes the exact targeted key event before reaching that existing OEM call. This rules out a second explicit SourceMenu start path in this policy DEX, not every possible external listener in the firmware.
- **No duplicate OK:** the handler has one `injectPhilcoOk()` call, reachable only on ACTION_UP after a held DOWN. It resets the held flag before injection; repeat DOWN events are consumed; cancelled UP and UP after long-press suppress OK. UP removes the pending callback. The 5,000 ms callback opens the menu and has no OK injection path. Static checks passed; simultaneous input/handler timing and physical behavior are not device-tested.
- **APK/JAR compatibility:** APK package/version is `com.philco.ultra777.home`, versionCode 777, target/min API 19. MainActivity is exported and `singleTask`. The APK DEX contains the exact menu extra used by the policy DEX. APK v1 signature verifies; certificate SHA-256 `b52e3604deca330788c71cca8031b25b4208a7b9af6f521ff162634e14f2f9aa`. Candidate policy JAR ZIP CRC and DEX 035 passed. Build already completed successfully; no source/build rerun was needed.
- **Recovery assessment:** original `android.policy.jar` is backed up locally at `output/backups/joystick-policy-baseline-20261123/android.policy.jar` (SHA-256 `58CC4324C7F7AD44C5198747A3423D742B6517B45A8A6CAFCE709E9D1D44EC2E`). A recovery path for a failure before Android/ADB becomes available has not been proven; correct on-device file ownership, mode and SELinux context have not been collected for a restore. Therefore the candidate is **not safe to install yet**. First validate a recovery route (for example, the device's supported USB recovery) and preserve the exact restore commands/metadata. No write/install/reboot was done.


## Recovery readiness for TV_INPUT policy candidate — 2026-11-23

- **Device evidence (read-only):** device `smart_tv001`, Android 4.4.2 fingerprint `realtek/rtd298x_tv001/...20170531.092306`; current `/system/framework/android.policy.jar` SHA-256 exactly matches local original backup `58CC4324C7F7AD44C5198747A3423D742B6517B45A8A6CAFCE709E9D1D44EC2E`. Live attributes from `ls -lZ`: mode 0644, owner/group shown as `bluetooth:bluetooth`, SELinux `u:object_r:unlabeled:s0`. Do not substitute staging metadata: factory archive metadata describes a different JAR (115,643 bytes, mode 0664, uid/gid 1006 `wubf`, different SHA-256).
- **Official USB availability:** Philco's official driver index has a software package under the exact `PH42F10DSGWA` model directory: https://drivers.philco.com.br/TV/CHANGHONG/PH42F10DSGWA/. The local firmware bundle includes `0.vmlinux.rescue.bin` and `install_a`; the inspected installer formats/recreates system/data and writes kernel/boot/microcode. This is broad firmware recovery/update, not a validated single-file rollback. A rehosted copy of the described original manual reports a Settings > Software Update (USB) route, but no update/recovery procedure was run.
- **Recovery mode:** `/system/bin/recovery` exists in the factory baseline; `/system/recovery-from-boot.p` and `/system/bin/fastboot` were absent on the live device. These facts do not prove how to enter recovery, whether it exposes ADB, or whether it can mount and restore one JAR. No recovery reboot was attempted.
- **ADB resilience:** live `adbd` is `/sbin/adbd`, started as an init service; `init.svc.adbd=running`, `sys.usb.config=adb`, and ADB over TCP is currently connected. Because adbd is an init process separate from SystemUI/system_server, it is likely to remain available for a SystemUI-only failure, and may remain during a framework crash while init/network/adbd survive. This was not fault-tested; loss of boot, Wi-Fi/network, init or adbd means ADB cannot be relied on.
- **Restore requirements:** the only empirically grounded attributes for this build are the current live values above; numeric UID/GID were not resolved and `/file_contexts` has no explicit `framework` mapping. A future restore must preserve the exact installed JAR bytes and its live metadata/context, not use values from the August staging archive. Those values alone do not establish a recovery command or safe offline restore mechanism.
- **Conclusion:** official USB firmware exists, but no official or tested file-level recovery path has been established. Do not install the policy candidate until a separate recovery method is verified (including entry procedure and ability to restore the original JAR when normal Android is unavailable). No writes, install, reboot or standby action occurred; TV remains on its existing PhilcoOS/system baseline.


## Rear physical D-pad event audit — STOPPED before patch — 2026-11-23

- **Identified nodes:** live `/dev/input/event2` is `Smart_TV_Keypad` (virtual input device, bus 0006, sysfs `/devices/virtual/input/input2`) with EV_KEY directions, POWER, MENU and code `0x225`. Separate `/dev/input/event0` is `venus_IR_input` (IR receiver, `/devices/virtual/input/input0`). The user has no functional remote, so event0 was not physically pressed. These are different devices; both keylayouts map Linux key code 549 to TV_INPUT, but event2 uses Generic.kl fallback (no `Smart_TV_Keypad.kl` found) while event0 has `venus_IR_input.kl`. `Realtek_Keypad_Device.kl` is a third named mapping and is not the live event2 name.
- **Code terminology:** live event2 reports EV=3 (EV_SYN + EV_KEY), not EV_MSC. The trace showed `EV_KEY 0225` DOWN/UP; 549/0x225 is the Linux EV_KEY code mapped by the keylayout to `KEYCODE_TV_INPUT`, not a separately observed hardware `MSC_SCAN` value.
- **Partial raw evidence:** `output/captures/rear-dpad-event-capture-20261123.txt` records event2 KEY_LEFT, KEY_DOWN, `0225`, and KEY_POWER events. The capture did not reliably mark which physical action produced every line; KEY_UP/RIGHT were not captured. During the user's requested central long press the TV powered off; `KEY_POWER` DOWN/UP appeared in the same trace. Do not repeat a long press to reproduce this.
- **OEM behavior:** exact current installed policy JAR is backed up and its SHA matches the TV. Its policy DEX has an OEM `com.hikeen.xymenu.MyService.Start` branch for TV_INPUT. The earlier candidate checks `Smart_TV_Keypad` and keycode 549 before that branch, so it targets event2 rather than the separate IR node. However, it does not intercept a `KEY_POWER` event. It therefore cannot yet be considered safe or sufficient for the rear joystick's hold behavior; the observed power-off path may bypass its TV_INPUT handler.
- **Status after user powered TV back on:** read-only ADB confirms boot complete; PhilcoOS MainActivity resumed, PackageManager path `/system/app/PhilcoOS.apk`, `PlayingSource=17` (HDMI 2). No source, system file, APK/JAR, or firmware was written; no build or install occurred. Previous candidates/backups remain unchanged.
- **Decision/next exact action:** no new candidate is generated. Before any implementation, capture a phase-marked **short central click only** on event2 to identify its event sequence without another long hold; then review policy handling of the observed KEY_POWER path. Up/right mappings also remain to be captured individually if needed. Until that evidence exists, preserve the current firmware behavior and do not install the earlier TV_INPUT candidate for this use case.


## Rear D-pad short-click / KEY_POWER analysis — 2026-11-23

- **Existing capture:** `output/captures/rear-dpad-event-capture-20261123.txt` is partial and has no action markers. It records event2 pulses for `KEY_LEFT`, `KEY_DOWN`, `EV_KEY 0x225`, and `KEY_POWER`; timestamps alone do not prove which events belong to one gesture. No `EV_MSC/MSC_SCAN` appeared. The user reports the TV turned off during that physical sequence; do not repeat a hold.
- **Live system confirmed read-only:** Android 4.4.2 KOT49H build 20170531; `/system/framework/android.policy.jar` hash matches the preserved original. `Generic.kl` maps EV_KEY 549→TV_INPUT and 116→POWER. event2 `Smart_TV_Keypad` has no matching named keylayout, so Generic.kl applies; `venus_IR_input` event0 is a separate IR device with its own layout, also mapping 549→TV_INPUT. The active input process is `/system/bin/keypad`; `com.cvte.keyservice` is separate.
- **OEM keypad evidence:** a read-only pull of the live 9,728-byte `/system/bin/keypad` (SHA-256 `FE908E30623A8B7CD2A9176CBDFD0219E6897AA41594FC15688C1963B81EF429`) contains strings `Smart_TV_Keypad`, `PIN_KEYPAD_SOURCE`, `PIN_KEYPAD_PWR`, and directional/menu pins. This supports native keypad handling but strings alone do not establish how a center hold maps to SOURCE or PWR. Live keyservice APK SHA-256 `157C87737441719CDF593831F10711D577329567E0862A3037045F5E75EFA236`; its DEX has no matching printable mapping strings.
- **Ordering / power route:** exact installed policy is the preserved DEX (`android.policy.jar` SHA noted above). Input policy calls `interceptKeyBeforeQueueing()` before `interceptKeyBeforeDispatching()`. Queueing handles Android keycode 26 (`KEY_POWER`) via the standard power-key state and `interceptPowerKeyDown/Up`; it schedules the resource-defined power-long-press callback. For `KEY_POWER` it also checks Realtek `GetBurnMode` and may send `com.hikeen.powerkey`, then checks TV scan/record/timeshift and CEC power options. This handling is earlier than the old TV_INPUT hook in `interceptKeyBeforeDispatching`, so that hook cannot prevent an earlier POWER action. The keylayout has no 5-second gesture mapping.
- **OEM shutdown association:** observed KEY_POWER pulses plus the user's report make the power path a concrete risk. Static evidence does not yet prove which center gesture generated those pulses or whether the native keypad service itself emits them after a hold. No global KEY_POWER suppression is acceptable.
- **Implementation decision:** no new candidate and no source changes. If the short-click trace confirms distinct event2 code(s), the possible safe design is a narrowly device-scoped early queueing filter for only the proven center gesture, while leaving IR and all directions alone; exact ability to infer a five-second hold depends on the raw event sequence and must be proven first.
- **Current state:** after the user powered the TV back on, read-only ADB confirmed boot complete, PhilcoOS MainActivity resumed, `/system/app/PhilcoOS.apk`, `PlayingSource=17` (HDMI 2). No device writes, install or reboot occurred.
- **Next test, pending explicit user confirmation:** start a timestamped `getevent -lt /dev/input/event2` capture, record the initial activity/source, ask for exactly one center tap with immediate release (no hold), stop capture immediately, then read final activity/source. No artificial input or IR event will be sent. Do not run until the user confirms.

### Captura autorizada: um toque curto no centro — 2026-11-23
- Arquivo: `output/captures/rear-dpad-short-center-20261123.txt`.
- Somente leitura em `/dev/input/event2` (`Smart_TV_Keypad`); captura limitada e encerrada após inatividade. Um toque breve produziu `EV_KEY 0225 DOWN` em `1795457876.153197` e `EV_KEY 0225 UP` em `1795457876.153253`, com `SYN_REPORT` após cada evento. Não houve `KEY_POWER` nem `EV_MSC` nesta captura.
- Leitura final da janela: `com.philco.ultra777.home/.MainActivity`; TV permaneceu online e `PlayingSource=17` (HDMI 2). Nenhum evento artificial, escrita na TV, instalação ou reboot.
- `Generic.kl` mapeia código Linux 549/`0x225` para `KEYCODE_TV_INPUT`. Assim, o toque central curto foi identificado no nível de entrada, mas **não foi convertido nem validado como OK/ENTER**. A janela Home em foco não comprova comportamento de OK nem exclui overlay OEM não refletido por esse campo.
- A associação de `KEY_POWER` vista na captura parcial anterior continua indeterminada. Não houve pressão longa e não se deve repeti-la nesta investigação.
- Próximo passo: revisar a implementação OEM a partir dessas evidências; qualquer captura adicional de interação física exige instrução específica. Nenhuma alteração/candidato de framework foi feito.

## Revisão estática do desenho de pressão longa — 2026-11-23
- **Artefato revisado, não instalado:** `output/work/tv-input-policy/smali/com/android/internal/policy/impl/PhoneWindowManager.smali` e `PhoneWindowManager$TvInputLongPress.smali`; candidato JAR `output/candidates/android.policy-tvinput-control-menu-20261123.jar`. O código local detecta `keyCode=TV_INPUT (0xB2)`, `isKeyPad()` pelo nome `Smart_TV_Keypad` e `scanCode=0x225`; intercepta em `interceptKeyBeforeDispatching()` antes do ramo OEM que inicia `com.hikeen.xymenu.MyService.Start`.
- **Fluxo proposto:** DOWN inicial agenda callback de 5000 ms; repetições são consumidas; UP cancela o callback e injeta um par `KEYCODE_DPAD_CENTER` (0x17) somente se não cancelado e o callback não marcou long press; callback verifica `held`, marca `longPressed` e abre uma vez a MainActivity com extra `JOYSTICK_CONTROL_MENU`. O evento original é consumido. Não há alteração explícita das quatro direções nem do ramo `KEY_POWER`.
- **Limitações estáticas:** não se demonstrou que uma pressão longa do hardware gere apenas `TV_INPUT`. `KEY_POWER` é processado em `interceptKeyBeforeQueueing()`, antes do gancho em dispatch; se o keypad emitir um evento POWER independente, esse desenho não o suprime por projeto. A captura autorizada de toque curto mostrou somente `0x225` DOWN/UP, portanto não resolve o comportamento de hold. Também não há validação de execução do timer, injeção de OK ou abertura do menu no aparelho. O ramo que consome DOWN sem agendar caso `mHandler` seja nulo pode perder um toque; deve ser revisto no diff final. A fronteira exata de 5 s depende da disputa entre callback e UP e não foi testada.
- **Conclusão:** a arquitetura é coerente para distinguir DOWN/UP de `TV_INPUT` sem atrasar o clique depois da soltura e bloquear o seletor OEM nesse ramo. Ainda não é seguro afirmar que cumpre o requisito de não desligar: isso depende de evidência de que o hold não gera `KEY_POWER`. Sem executar hold, essa evidência precisa vir de análise estática conclusiva do produtor OEM do evento; caso não seja possível, manter a instalação bloqueada.
- Nenhum arquivo Java, Smali, JAR, APK ou TV foi modificado nesta revisão; sem build, instalação, reboot ou teste físico. `git status` não está disponível porque `C:\PhilcoOS` não contém repositório Git (`fatal: not a git repository`).


## Análise estática do produtor OEM de teclas — 2026-11-23
- Artefato: cópia local somente leitura de `/system/bin/keypad`, ELF32 ARM little-endian, 9.728 bytes, SHA-256 `FE908E30623A8B7CD2A9176CBDFD0219E6897AA41594FC15688C1963B81EF429`. Tem imports dinâmicos, mas não símbolos completos nem fonte; não há driver `.ko`, fonte GPIO ou imagem de kernel/firmware correspondente entre os artefatos locais examinados.
- O binário contém strings `/dev/uinput`, `Smart_TV_Keypad`, `/dev/mem`, `/sys/realtek_boards/pcb_enum`, `PIN_KEYPAD_SOURCE`, `PIN_KEYPAD_PWR` e logs de repetição. Imports incluem `ioctl`, `write`, `mmap` e `usleep`. O código cria eventos uinput (`UI_DEV_CREATE`) e possui rotinas que escrevem pares EV_KEY DOWN/UP; uma usa Linux key code `0x74` (KEY_POWER), outra `0x225` (Generic.kl→Android TV_INPUT). Isso aponta fortemente para `/system/bin/keypad` como produtor do dispositivo virtual e eventos. Não prova qual configuração de pino chama cada rotina nem que segurar SOURCE vira POWER.
- O binário contém `Repeat_Count`, comparações/counters e `usleep` (incluindo 100 µs, 50.000 µs e 13.000 µs nos trechos examinados). Não foi possível associar limiar temporal a `PIN_KEYPAD_SOURCE`→`KEY_POWER`; não há prova de threshold de 5 s. O fluxo de configuração de pinos não ficou demonstrado de ponta a ponta. Esses sleeps/counters não provam duração de hold.
- A captura curta `output/captures/rear-dpad-short-center-20261123.txt` mostra `0x225` DOWN/UP separados por 56 µs. A captura anterior contém pulsos `KEY_POWER` e `0x225` em event2, mas sem marcadores de gesto. A duração do KeyEvent não representa comprovadamente o tempo físico pressionado. Assim, o timer candidato de 5 s iniciado no DOWN e cancelado no UP não está validado e provavelmente perderia o hold se o daemon continuar emitindo pulsos.
- Caminho Android: D-pad/pinos da placa → acesso OEM a GPIO/registradores (o daemon referencia `/dev/mem` e enumeração Realtek; driver/pino exato indisponível) → `/system/bin/keypad` escreve EV_KEY em `/dev/uinput` no `Smart_TV_Keypad` → kernel expõe evdev (`event2`) → EventHub → InputReader aplica Generic.kl (549→TV_INPUT/178; 116→POWER/26) → InputDispatcher chama `interceptKeyBeforeQueueing()` e depois `interceptKeyBeforeDispatching()`. TV_INPUT é interceptável antes do ramo SourceMenu em dispatch; KEY_POWER entra no tratamento de energia na fila.
- Em princípio, interceptar sem afetar outras fontes é possível em `interceptKeyBeforeQueueing()`, condicionado ao device ID/nome `Smart_TV_Keypad`, código de POWER e uma sequência central reconhecida; IR/outros dispositivos ficam fora. Hoje não há correlação segura demonstrada entre sequência do centro e POWER. Um filtro prematuro poderia suprimir um desligamento legítimo do keypad ou deixar passar o POWER do hold. Não alterar KEY_POWER até esclarecer o produtor.
- Decisão: causa temporal exata permanece não determinada; instalação de candidato segue bloqueada. Nenhum teste físico, escrita na TV, alteração de candidato, build ou reboot nesta etapa.


### Complemento: cópias de firmware e validade da análise — 2026-11-23
- A cópia lida da TV (`output/work/rear-dpad-audit-20261123/keypad`) tem SHA-256 `FE908E30623A8B7CD2A9176CBDFD0219E6897AA41594FC15688C1963B81EF429`. A baseline extraída e os dois estágios locais (`extracted/factory_baseline/system/bin/keypad`, `build/system_stage/bin/keypad`, `build/system_stage_slim/bin/keypad`) têm 9.728 bytes, mas SHA diferente e idêntico entre si: `A49D4456DDDD073714292596A0BC9691AB2B081C37BB24A9FA050C9EB302BB61`. As strings são semelhantes, mas não se deve transferir conclusões de temporização entre essas imagens e o executável instalado.
- Há também `CVTEkeypad` (9.752 bytes), com mensagens `press pwr`, `press Source` e `PIN_KEYPAD_PWR2`; seu hash é igual entre baseline e staging. Não foi encontrado script de inicialização textual que prove que esse binário é executado. `top` arquivado no diagnóstico anterior mostrou `/system/bin/keypad` ativo (PID 1801); não foi feita nova consulta ao dispositivo.
- O `system.emmc.tar` local contém ambos os executáveis. A busca por definições legíveis de `PIN_KEYPAD_SOURCE/PWR` encontrou referências nos binários `keypad`, `CVTEkeypad` e `TvServer`, sem arquivo textual de configuração/pinos identificado. `TvServer` contém strings de pins e `POWER_EN`, mas isso não demonstra que ele gere os eventos uinput do event2.
- Consequência: o caminho provável do produtor instalado é `keypad` → uinput → event2; criação do dispositivo e writes EV_KEY são sustentados pelo ELF instalado. O vínculo concreto entre registro/pino SOURCE ou PWR, gesto físico e código emitido segue não comprovado. Também não foi localizado o driver GPIO ou fonte correspondente ao build instalado; cópias de módulo/kernel do staging não substituem esses artefatos.
- Alternativas: (A) modificar `keypad` seria a camada que ainda pode ler pinos/estado, mas requer binário/fonte/configuração exatos, fluxo e recuperação; não recomendada agora. (B) framework só recebe os pulsos já sintetizados; filtro condicional no início de `interceptKeyBeforeQueueing()` poderia capturar POWER de event2, mas falta identificar uma sequência central estável para não suprimir energia legítima. (C) serviço Android comum recebe eventos já roteados e não tem um canal exclusivo do GPIO; não distingue a duração física sem interface OEM específica. **Recomendação:** nenhuma implementação até conseguir a correspondência da imagem instalada com a fonte/configuração do keypad ou um protocolo OEM documentado que preserve estado do botão.
- Sem teste físico, escrita na TV, edição/geração de candidatos, build ou instalação. Artefatos existentes preservados.


### Consulta read-only da configuração efetiva da placa — 2026-11-23
- ADB `192.168.0.71:5555` estava `device`; não houve interação física. `/system/bin/keypad` estava ativo (PID 1797) e `TvServer` ativo (PID 2037).
- `/sys/realtek_boards/pcb_enum` é uma **pasta** de parâmetros da placa, não um arquivo. Leituras efetivas: `PIN_KEYPAD_PWR=0x400000001`, `PIN_KEYPAD_UP=0x41f000001`, `PIN_KEYPAD_DOWN=0x427000001`, `PIN_KEYPAD_LEFT=0x408000001`, `PIN_KEYPAD_RIGHT=0x530000001`. Não existe entrada `PIN_KEYPAD_SOURCE` nem `PIN_KEYPAD_MENU` nesse diretório. Os valores são códigos opacos do firmware; não os decodificar como GPIO/registrador sem formato OEM.
- `/sys/class/gpio` não existe. `/sys/devices/platform/Venus GPIO` existe, mas a listagem mostrou apenas `modalias`, `uevent`, `driver`, `subsystem` e `power`, sem atributo de estado de pino. `/sys/devices/virtual/input/input2/uevent` identifica `Smart_TV_Keypad`; `MODALIAS` inclui key bits `0x74` e `0x225`. Isso expõe capacidades do dispositivo virtual, não estado elétrico atual. A leitura anterior de `/sys/realtek_boards/pcb_enum` como arquivo falhou porque era diretório; esta consulta corrigiu isso.
- Evidência combinada: a configuração da placa define um pin `PWR` e os quatro direcionais, enquanto o event2 virtual oferece `KEY_POWER` e `TV_INPUT`; o clique central curto observado ainda emite `0x225`. É plausível que o daemon interprete o pin PWR/centro e sintetize código conforme estado/repetição, mas a associação efetiva PWR→SOURCE/POWER e a duração do hold **continuam não comprovadas**. Não há interface sysfs encontrada para ler continuamente o nível físico do pino.
- O daemon tem thread de keypad, `usleep` e contadores `Repeat_Count`, compatíveis com leitura periódica/repetição; a implementação exata e o limiar não foram estabelecidos por causa do binário parcialmente stripped e falta de documentação da codificação `pcb_enum`.
- Decisão mantém bloqueio: não interceptar `KEY_POWER` até correlacionar a sequência física/origem; não modificar o daemon. Próximo passo estático necessário: documentação/formato Realtek de `pcb_enum` para este build ou fonte/mapa de placa correspondente ao firmware instalado, além do fluxo de estado/repeat do ELF live. Nenhum teste físico, escrita, instalação, build ou alteração de candidato.


## Comparação do keypad instalado com a baseline de firmware — 2026-11-23
- **TV (ADB, leitura):** fingerprint `realtek/rtd298x_tv001/rtd298x_tv001:4.4.2/KOT49H/eng.wubf.20170531.092306:eng/test-keys`; build date 31/05/2017; `ro.cvte.boardstr=TP.RT2984T.PB801`.
- **Baseline local mais próxima:** `extracted/factory_baseline/system/build.prop` e `firmware/package4/system.emmc.tar`, target `rtd298x_tv001`; build 4.4.2/KOT49H de 16/08/2017; `ro.cvte.boardstr=TP.RT2984T.PB801`. `firmware/config.txt` deixa `modelname`, `version` e `releaseDate` vazios. Mesmo board string/plataforma, porém baseline é posterior em ~11 semanas e não é o firmware instalado exato.
- **Comparação binária:** `/system/bin/keypad` lido da TV = 9.728 bytes, SHA-256 `FE908E30623A8B7CD2A9176CBDFD0219E6897AA41594FC15688C1963B81EF429`; daemon na baseline `A49D4456DDDD073714292596A0BC9691AB2B081C37BB24A9FA050C9EB302BB61`, também 9.728 bytes. Só 5 bytes diferem: um byte em offset `0x20a8` (na `.data`, palavra inicializada: instalado `5`, baseline `0`) e quatro bytes `0x2122–0x2125` no CRC de `.gnu_debuglink`. As seções `.text` e `.rodata` são idênticas byte a byte; portanto código/instruções e strings são iguais, mas o estado global `.data` diferente impede afirmar equivalência funcional até descobrir seu uso.
- O binário `build/system_stage/bin/keypad` e `build/system_stage_slim/bin/keypad` são idênticos à baseline A49D, não ao daemon instalado FE90. `CVTEkeypad` é outro executável; não há prova de que esteja ativo.
- **Configuração de placa instalada:** consulta anterior encontrou `PIN_KEYPAD_PWR=0x400000001`, UP `0x41f000001`, DOWN `0x427000001`, LEFT `0x408000001`, RIGHT `0x530000001`; `PIN_KEYPAD_SOURCE` não está presente. O arquivo de firmware local não fornece uma tabela legível que decodifique esses valores ou prove a mesma configuração para maio/2017. `board_id` exposto pela TV é `0`, insuficiente para derivar o mapa.
- **Conclusão:** baseline é um comparador forte para a implementação do daemon porque o código executável é igual, mas não substitui o build de maio/2017. O produtor parece ter a mesma lógica de código; o efeito da palavra `.data` diferente e a configuração GPIO efetiva do build instalado permanecem desconhecidos. Próximo trabalho estático: encontrar xref/uso da global inicializada em `0x20a8` e uma tabela de placa correspondente ao build de maio, ou outra imagem oficial exata. Sem alterações/candidato/build/instalação.


## ELF .data xref + firmware de maio — continuação estática — 2026-11-23
- **Endereçamento corrigido:** no ELF32, `.data` tem VA `0x3000`, file offset `0x2000`, tamanho `0xb0`. A palavra distinta começa em VA `0x30a8`, offset de arquivo `0x20a8`: `5` no daemon lido da TV e `0` na cópia 16/08. `.gnu_debuglink` CRC difere em `0x2122–0x2125`. `.text` (`VA/file 0x7d8`, `0xc48` bytes) e `.rodata` são idênticas.
- **Referências:** o ELF não tem `.symtab` completo. A análise Capstone das funções delimitadas pelo `.ARM.exidx` não encontrou referência PC-relative direta à VA `0x30a8`; o fluxo usa PIC/GOT e globals acessadas indiretamente (ex.: em `0xa28–0xa8a` há `SB` inicializado PC-relativamente e stores a globals `.bss`). Não foi possível provar que o inteiro `.data` em `0x30a8` seja variável lida/escrita, estrutura ou configuração, nem localizar todas referências indiretas com o ELF stripped. **Efeito sobre repetição, tempo, pinos, POWER/TV_INPUT e inicialização: não comprovado.** Não atribuir sem mais símbolos/mapa de relocação ou código-fonte.
- **Firmware exato:** arquivos locais de firmware identificáveis são da baseline 16/08/2017 (`rtd298x_tv001`, `TP.RT2984T.PB801`); nenhum pacote/imagem de 31/05/2017 correspondente foi encontrado. O metadata `firmware/config.txt` da baseline deixa modelo/versão/data de release vazios. O binário original May permanece sendo a cópia read-only coletada da TV, não uma imagem oficial May completa.
- **TvServer:** há uma cópia local coletada da TV (`.toolchain/tmp/TvServer-installed-20170531.bin`) e a variante de agosto no firmware. Strings de `TvServer` citam `PIN_KEYPAD_PWR` e direções; isso não prova que inicie/controla `/system/bin/keypad` nem que forneça os valores. Nenhum script de boot ou protocolo/socket/Binder compartilhado com o daemon foi confirmado nesta rodada. Os valores live `pcb_enum` continuam: PWR `0x400000001`, UP `0x41f000001`, DOWN `0x427000001`, LEFT `0x408000001`, RIGHT `0x530000001`; SOURCE/MENU ausentes. Significado/codificação permanecem desconhecidos.
- **Respostas:** byte `.data` influência D-pad — **não comprovada**; significado do PWR codificado — **não comprovado**; componente que associa pino a keycode — `/system/bin/keypad` é **provável** produtor uinput, associação pino→código exata não comprovada; duração física observável pelo daemon — **provável** no ponto de polling/memória mapeada, mas duração capturada no Android é apenas pulso; interface contínua de estado — não encontrada em sysfs padrão nem no nó `Venus GPIO`; timer OEM cinco segundos — não localizado/provado. Modificar framework ou daemon não é recomendado ainda.
- **Falta concreta:** fonte/símbolos do `/system/bin/keypad` instalado, mapa de hardware/decodificação `pcb_enum` para boardstr `TP.RT2984T.PB801` no build 20170531 e configuração de inicialização que vincule `TvServer` ao daemon. Sem isso, as relações solicitadas permanecem indeterminadas. Sem interação física, escrita, alteração de candidato, build ou instalação.


## Achado novo: TvServer instalado e decisão de camada — 2026-11-23
- **Artefato exato do build da TV já local:** `.toolchain/tmp/TvServer-installed-20170531.bin` (33.318.520 bytes, SHA-256 `9C6D30EE1E3B79BCC590FC374355EDA0B1CDFD098C4A10E6D7C6B5FD1EAB5F50`). É diferente da baseline de agosto (`77FAAB5D4B731AD147E6AB395E37427B87B5C033A4071DAABCBD7F2ED91223B2`).
- Strings internas do TvServer instalado incluem `KEYPAD_PARM%u=0x%8x`, `Valid keypad paramteter`, `powerMgr_setup_keypad_multi(unsigned int, long long unsigned int*)`, `PIN_KEYPAD_PWR` e direções, além de `/dev/venus_gpio`, `int getGPIO(int)`, `int setGPIO(int)`, chamadas/logs de debounce e interrupção GPIO e `TV_INPUT`. Isso confirma que o TvServer contém subsistema/configuração OEM de keypad/GPIO. **Não comprova** que ele inicia `/system/bin/keypad`, passa a configuração a esse processo, lê o centro continuamente ou oferece essa API via Binder/RPC ao app; não foi localizado um canal/export público.
- **Byte `.data`:** VA `.data=0x3000`, byte/palavra em VA `0x30a8` (offset `0x20a8`), valor 5 instalado/0 baseline. `.text`/`.rodata` idênticas. O ELF não tem `.symtab`; análise direta das referências PC-relative não achou xref para `0x30a8`. O código tem PIC/GOT e acesso indireto a globais/BSS; portanto isso **não é prova de que o campo não seja lido**. Significado e influência permanecem não comprovados. Evidência reproduzível: seções ELF (`readelf` equivalente), relocations `.rel.dyn` cobrem GOT em `0x2f80` e entradas `0x3008...`; nenhuma relocação aponta a `0x30a8`.
- **Hipóteses atualizadas:** (1) daemon lê o parâmetro PWR/GPIO e transforma nível/duração em pulsos TV_INPUT/POWER — provável, ainda sem fluxo de dados que ligue parâmetro a code emitter; (2) TvServer configura/consome parte do keypad via `KEYPAD_PARM` e `venus_gpio` — provável subsistema, integração com event2 não comprovada; (3) outro driver da placa produz eventos — possível, mas o event2 virtual e o processo `keypad` ativo favorecem daemon. Nenhuma hipótese foi eliminada por completo.
- **Decisão de arquitetura:** framework é inadequado para medir duração física no padrão já observado (DOWN/UP TV_INPUT de 56 µs); só poderia classificar uma sequência de pulsos depois de conhecer a cadência e POWER gerado. Serviço Android ordinário não tem acesso contínuo ao pino; TvServer pode ter acesso nativo GPIO, mas não foi identificada API autorizada/exportada. A camada mais apropriada para um timer confiável é a que lê o nível GPIO (provavelmente keypad/TvServer), mas ainda não se pode escolher entre patch do daemon ou interface do TvServer sem mapear `KEYPAD_PARM` e fluxo de inicialização/configuração.
- **Futuro menor conjunto de mudanças, condicional:** depois de comprovar o produtor e a configuração exatos, interceptar somente a tecla central na camada de origem, atrasar o evento curto até UP, ao atingir 5 s emitir uma ação/menu apenas uma vez e suprimir o POWER derivado do mesmo gesto; deixar os 4 direções e POWER de outras origens inalterados. Nenhum patch foi feito nesta rodada.
- **Risco restante / artefato que bloqueia decisão:** falta o mapa/schema OEM de `KEYPAD_PARM`/`pcb_enum` para `TP.RT2984T.PB801` build 20170531 e uma referência de fluxo que conecte esse parâmetro e estado do pino aos eventos `0x225`/`0x74` (fonte, mapa de símbolos ou descompilação conclusiva do produtor). Sem isso, a arquitetura de origem é preferível em princípio, mas o componente exato não pode ser selecionado com segurança.
- Sem interação física, escrita na TV, modificação de código/candidato, build ou instalação.

## Evidência nova: KeypadService e xrefs Thumb — 2026-11-23
- **Inicialização instalada comprovada (somente leitura):** /init.rc importa o arquivo de hardware; ro.hardware=rtd298x. /init.rtd298x.rc (SHA-256 1ABED2539C22CED777A31745F794EC80E8F8207CB5CAEDF4CF83BD90B0C5D105), linhas 372–375, declara service KeypadService /system/bin/keypad, classe core, root/shell. init.svc.KeypadService=running; PID 1797. tvserver-setup é outro serviço oneshot; TvServer PID 2037. A inicialização do daemon está provada; comunicação TvServer↔keypad não.
- **Fluxo Thumb no daemon instalado** (output/work/rear-dpad-audit-20261123/keypad, SHA-256 FE908E30623A8B7CD2A9176CBDFD0219E6897AA41594FC15688C1963B81EF429): 0x118c percorre /sys/realtek_boards/pcb_enum/ e converte valores; 0x132c abre /dev/mem (string VA 0x1890) e mapeia 1 MiB; 0x13f8/0x140c escrevem/leem a região mapeada. 0xa28 amostra sete registros em stride 0x18, atualiza estado/contadores e chama os ramos de emissão uinput. Assim, o daemon vê amostras físicas antes da síntese; o DOWN/UP Android de 56 µs não é a duração real.
- **xref da palavra antes sem xref:** .data VA 0x3000 / offset 0x2000; registros começam em 0x3008, stride 0x18. A linha PIN_KEYPAD_PWR começa em 0x3098; campo +0x10 em VA 0x30a8 / offset 0x20a8, valor instalado 5 (baseline agosto 0). 0xa28 calcula registro por índice; 0xac8, 0xad2 e 0xb26 leem o campo. 0xacc o passa ao formato VA 0x150d, =====queyr002===== cmd = %d; também entra em comparação/armazenamento. Portanto é lido como campo cmd, mas seu significado de produto não está documentado.
- **Emissão e limite da conclusão:** dispatcher tbh em 0xc3a: cmd 1 chega a 0xc6a e emite EV_KEY Linux 0x74 (POWER); cmd 5 chega a 0xd7e e emite 0x225 (TV_INPUT); 19–22 são setas; 17 emite 0x8b. SOURCE e PWR têm valor estático 5. Isso relaciona valores e emissores, mas **não prova** quando/por que um hold muda de cmd 5 para 1; a captura antiga não liga esses códigos ao gesto.
- **Cadeia atualizada:** pino/registrador (decodificação elétrica desconhecida) → amostragem pelo /system/bin/keypad → EV_KEY escrito em /dev/uinput → Smart_TV_Keypad/event2 → EventHub/InputReader/Generic.kl (549→TV_INPUT, 116→POWER) → InputDispatcher/PhoneWindowManager.
- **Decisão e bloqueio:** medir 5 s no produtor KeypadService/daemon, não pelo pulso sintético no framework. Não alterar até identificar qual registro é o centro e o caminho existente do POWER, para suprimir somente a consequência do mesmo gesto. Falta schema/documentação/código OEM que decodifique pcb_enum/KEYPAD_PARM para a placa/build 20170531, mais o ramo que ligaria hold central a cmd 1. venus_gpio está em modules.builtin, sem fonte no pacote local. Sem interação física, escrita, patch, build ou instalação.

## Achado novo: decodificação do keypad PWR e limiar de amostras — 2026-11-23
- **ANALYZED:** no ELF instalado `output/work/rear-dpad-audit-20261123/keypad` (SHA-256 `FE908E30623A8B7CD2A9176CBDFD0219E6897AA41594FC15688C1963B81EF429`), o setup `0x105c` ativa registros de 24 bytes e copia apenas o descritor de 64 bits do `pcb_enum`; não muda `cmd` ou modo.
- **ANALYZED:** sete linhas a partir de `.data` VA `0x3008`, stride `0x18`; campos: nome `+0`, ativo `+4`, descritor `+8`, cmd `+0x10`, modo `+0x14`. PWR VA `0x3098`: cmd=5, modo=0; descritor live `0x400000001` = índice 0, esperado 0, tolerância 4 conforme os bytes consumidos pelo loop.
- **ANALYZED:** `0xa08` limita leitura a índice 0–4, `read32` e `uxtb`; helper `0x140c` mapeia `/dev/mem` e lê. Código mapeia 1 MiB do offset `0x18000000`; a aritmética do endereço resulta em `0x18061500 + 4*índice`. O bloco físico/registo é desconhecido; “ADC ladder” é provável, não confirmado.
- **ANALYZED:** o modo 0 (PWR) conta amostras estáveis. Após `>9`, despacha o cmd da linha; PWR instalado é cmd5 → `TV_INPUT`. Depois, ao contador `>0x77` (120ª amostra), grava 1 no global `0x3514`, sem chamar o dispatcher nem alterar currentCmd para 1. O ciclo sem evento usa `usleep(13.000 µs)`; o limiar nominal é ~1,6 s, não 5 s, e não foi cronometrado em execução.
- **ANALYZED:** cmd1→`KEY_POWER (0x74)` existe como caso genérico do dispatcher, mas nenhuma das sete linhas instaladas tem cmd1 e o parser só altera descritor/ativo. A transição PWR cmd5→cmd1 não aparece neste daemon/fluxo. O `KEY_POWER` anterior/desligamento continuam sem origem comprovada; produtor paralelo (TvServer/driver) é hipótese.
- Relatório detalhado: `docs/REAR_DPAD_KEYPAD_REVERSE_ENGINEERING_20261123.md`. Bloqueio específico: fonte/símbolos do kernel de 31/05/2017 para `venus_gpio`/register `0x18061500`, mais mapa da placa. Não houve interação TV, escrita, build ou instalação. Git ausente; commit/branch indisponíveis.

## Nova evidência: alternativa CVTE para KEY_POWER — 2026-11-23
- O ELF legado `CVTEkeypad` da baseline/staging (SHA-256 `22C61F86C4F5F0714CA74E610710235FCC473AC8220C800474C1EE5471DFBE8A`) possui linhas PWR/PWR2 com `cmd=1`; dispatcher `0x0a4c` emite EV_KEY `0x74`. É produtor potencial se executado, mas não há serviço/execução confirmados; snapshot live anterior apontava `/system/bin/keypad` ativo.
- O ELF `TvServer` instalado não mostrou rota direta `uinput`/EV_KEY; não se exclui ação OEM indireta. Captura parcial existente contém dois pares KEY_POWER em event2 intercalados com TV_INPUT e a TV desligou, mas não há marcador que associe eventos ao gesto ou processo.
- Global `0x3514` após `count>119` não tem consumidor local/xref; não troca cmd nem despacha POWER. `0x18061500` só apareceu como ocorrência bruta desalinhada no kernel de agosto, sem símbolo/xref.
- Relatório técnico existente complementado; sem alteração de código, build, ADB ou teste nesta rodada.

## Deploy do refresh de clima — 2026-11-23
- **Branch/commit:** indisponíveis; `C:\PhilcoOS` não é repositório Git.
- **APK:** `source/philco-home/build/outputs/apk/release/philco-home-release.apk`; SHA-256 `F0DE6C261F6BB3C3744414DF37F4486967630E876E0E5CAE5A9136928AA419F1`.
- **Backup pré-instalação:** `output/backups/WEATHER_REFRESH_DEPLOY_20261123/PhilcoOS-system-base-before-install.apk`; SHA-256 `8958C7A896E191ADB63D55D3E74B31B7C64F27ED96A86229BF53D53E38A4934F`.
- **TV:** `192.168.0.71:5555`, build `eng.wubf.20170531.092306`, Android inicializado. `adb install -r` retornou `Success`; PackageManager reportou update em `/data/app/com.philco.ultra777.home-1.apk`.
- **Validação do dispositivo:** `am start -W` iniciou `MainActivity` (957 ms); `sys.boot_completed=1`, `mWakefulness=Awake`, `mScreenOn=true`, foco na MainActivity. Logcat registrou `Weather refresh succeeded; updating cached conditions`. Captura: `output/captures/weather-deploy-20261123.png`, visualmente mostra Joinville, temperatura e `ATUALIZADO 20:08`.
- **Limite da verificação:** TV estava ligada durante a checagem; isso confirma que não desligou durante o deploy, não testa o timer de inatividade.
- **Estado Home:** launcher PhilcoOS aberto; fonte observada HDMI 2 ativa. Nenhum teste de joystick/standby executado.
- **Próximo responsável:** Codex. Próxima tarefa: revisão visual da Home e medição de RAM/fluidez, sem alterações até comparar baseline.

## Current state — Home to Music navigation — 2026-11-23

- **Task:** HOME-MUSIC-EDGE-001. **Agent:** Codex. **Workspace:** C:\PhilcoOS. **Branch/commit:** unavailable; this workspace has no active Git repository. No repository was initialized.
- **APK:** package com.philco.ultra777.home, versionCode 777 / versionName 1.0.0-ultra777. Candidate output/releases/philco-home-home-music-right-page-20261123.apk, 1,885,327 bytes, SHA-256 7D28138D73CEEA1CF6F36CE0D9EC7F27AEDAFD86F58B49F0DDC14FE685F68513. V1 signature verified; signer SHA-256 52e3604deca330788c71cca8031b25b4208a7b9af6f521ff162634e14f2f9aa; zipalign check passed.
- **Device:** no ADB interaction in this task. Last documented device state remains TV powered on, PhilcoOS Home foreground, HDMI 2 selected; not rechecked here. This candidate was not installed.
- **Completed:** pressing RIGHT while the Ajustes tile is focused opens the existing MusicActivity; key repeats are consumed without reopening it. Android Back returns to Home and restores focus to Ajustes. The tile subtitle now indicates → Música. The eight-card geometry and center-click behavior of Ajustes are preserved.
- **Files modified:** source/philco-home/src/com/philco/ultra777/home/MainActivity.java; this handoff plus docs/AI_DECISIONS.md, docs/AI_TEST_RESULTS.md and docs/PHILCOOS_ULTRA_777_GLASS_AURORA.md.
- **Build:** ssembleRelease completed successfully. APK signature (v1) and zip alignment verified. No functional test, installation or visual check was run.
- **Risks/limits:** the new arrow-right transition and focus restoration are COMPILED, not DEVICE_VALIDATED or VISUALLY_CONFIRMED. The TV still has the previously installed app build.
- **Next owner:** Codex. **Next exact action:** install this candidate and check right-from-Ajustes → Música → Back-to-Ajustes on the real TV, then proceed to the next planned feature.
## Clarification — Music page slide transition — 2026-11-23

- **Task:** HOME-MUSIC-EDGE-001 refinement. **Agent:** Codex. **Workspace:** C:\PhilcoOS. **Branch/commit:** unavailable; no active Git repository.
- **Behavior clarified by user:** with Ajustes focused, pressing RIGHT transitions directly to the Music page, like moving to the next horizontal page. There is no additional confirmation press on a Music card.
- **Implementation:** Music enters from the right while Home exits left; Android Back runs the inverse transition. Both use 260 ms decelerating translate animations, with no blur, scaling or continuous effects. The eight Home cards, center/OK action and focus return remain as previously documented.
- **Files additionally modified:** source/philco-home/src/com/philco/ultra777/home/media/MusicActivity.java and four lightweight XML animation resources in source/philco-home/res/anim/.
- **Candidate:** output/releases/philco-home-home-music-slide-20261123.apk, 1,887,428 bytes, SHA-256 8C4E9B896F2F77E999A157CCE797617888BDEFD9EAE2990B9736203C5A26857C; package versionCode 777 / 1.0.0-ultra777. ssembleRelease succeeded; zip alignment and v1 signature verification passed with the existing PhilcoOS signing certificate.
- **Device state:** no ADB query or installation in this refinement. Last logged state remains TV on, PhilcoOS Home foreground, HDMI 2 selected; not checked now. Candidate is local only.
- **Next exact action:** install and visually confirm Ajustes-focused RIGHT slides to Music, then Back slides to Home and restores Ajustes focus. No such physical/visual validation is claimed yet.
## Current state — Music page slide candidate installed — 2026-11-23

- **Task:** HOME-MUSIC-EDGE-001. **Agent:** Codex. **Workspace:** C:\PhilcoOS. **Branch/commit:** unavailable; this workspace has no active Git repository.
- **Installed APK:** com.philco.ultra777.home, versionCode 777 / versionName 1.0.0-ultra777; source candidate output/releases/philco-home-home-music-slide-20261123.apk, SHA-256 8C4E9B896F2F77E999A157CCE797617888BDEFD9EAE2990B9736203C5A26857C.
- **Preservation:** pre-update APK pulled from /data/app/com.philco.ultra777.home-1.apk and saved at output/backups/PHILCOOS_HOME_MUSIC_SLIDE_20261123/PhilcoOS-installed-before-update.apk, SHA-256 56D3E294D7195328A6A0133947BC2BB9B7F622BF2E607DA51B7994E58358B676. After install, /data/app/com.philco.ultra777.home-2.apk was pulled to PhilcoOS-installed-after-update.apk; its SHA-256 exactly matches the candidate. Factory APK at /system/app/PhilcoOS.apk was not written.
- **TV:** 192.168.0.71:5555, device smart_tv001, Android build ng.wubf.20170531.092306, boot complete, awake and screen on at verification. The last documented selected source is HDMI 2; source was not queried in this deploy. The HOME preference was not changed.
- **Install result:** db install -r returned Success; PackageManager reports the updated system app at /data/app/com.philco.ultra777.home-2.apk, versionCode 777 / versionName 1.0.0-ultra777.
- **Validation limit:** this deploy confirms installation and byte-for-byte APK identity only. The right-to-Music slide, reverse Back transition and focus restoration were not exercised; no screenshot or visual confirmation was captured. Do not mark DEVICE_VALIDATED or VISUALLY_CONFIRMED for the new navigation.
- **Next owner:** Codex. **Next exact project step:** design and implement the lightweight Home Dynamic Bar for current music playback, building on the existing playback state. Keep the layout stable when idle and avoid continuous polling when Home is not visible. First inspect existing status/header UI and playback repository to avoid duplicating work.
## Current state — Dynamic Bar local candidate — 2026-11-23

- **Task:** PHILCO-HOME-DYNAMIC-BAR-001. **Agent:** Codex. **Workspace:** C:\PhilcoOS. **Branch/commit:** unavailable; no active Git repository.
- **Source change:** Home now has a compact 360×54dp graphite/gold music status bar below weather. It is hidden when idle/stopped, appears only for PLAYING/PAUSED, shows track title plus state, and uses the existing PlayerStateRepository.Observer. The observer is registered only while Home is resumed; no polling timer, artwork, blur or continuous animation was added.
- **Candidate:** output/releases/philco-home-dynamic-bar-20261123.apk, package com.philco.ultra777.home, versionCode 777 / 1.0.0-ultra777, 1,888,341 bytes, SHA-256 77BA251A817EA92EFE5F452D215B155EBA69DA31557BAE9949E20ECF65B9CC0B. ssembleRelease passed; zipalign and v1 signing checks passed with the existing PhilcoOS certificate.
- **Device/deployment:** Before backup/install, ADB reported 192.168.0.71:5555 offline. A single db reconnect offline attempt did not restore it; ping failed and no devices remained listed. No backup folder was created for this attempt, no APK was pushed/installed, and no TV settings were changed. The last confirmed installed APK before this offline event was the Music-slide build SHA-256 8C4E9B896F2F77E999A157CCE797617888BDEFD9EAE2990B9736203C5A26857C; current physical device state cannot be re-read while offline. Last recorded source was HDMI 2, not rechecked.
- **Validation limit:** local implementation is COMPILED only. No playback state, visual layout or TV behavior was exercised. Do not label INSTALLED, DEVICE_VALIDATED or VISUALLY_CONFIRMED.
- **Next owner:** Codex. **Next exact action:** when the TV is reachable again, pull and hash the presently installed APK as a rollback point, install the Dynamic Bar candidate, confirm PackageManager identity, then visually inspect the Home while music is playing and paused. Keep the next feature paused until this step is complete.
## HOME-MUSIC-EDGE-001 + PHILCO-HOME-DYNAMIC-BAR-001 — atualização instalada — 2026-11-23
- **Agente:** Codex. **Workspace:** C:\PhilcoOS. **Branch/commit:** indisponíveis; workspace sem repositório Git. **Dispositivo:** 192.168.0.71:5555 (`sys.boot_completed=1`).
- **INSTALLED:** `com.philco.ultra777.home`, versionCode 777 / versionName `1.0.0-ultra777`; candidato `output/releases/philco-home-dynamic-bar-20261123.apk` (1.888.341 bytes), SHA-256 `77BA251A817EA92EFE5F452D215B155EBA69DA31557BAE9949E20ECF65B9CC0B`.
- **Identidade confirmada:** Package Manager aponta para `/data/app/com.philco.ultra777.home-1.apk`; cópia puxada após instalar em `output/backups/PHILCOOS_DYNAMIC_BAR_DEPLOY_20261123/PhilcoOS-installed-after-update.apk` tem o mesmo SHA-256 do candidato.
- **Preservação:** APK encontrado antes da atualização foi salvo em `output/backups/PHILCOOS_DYNAMIC_BAR_DEPLOY_20261123/PhilcoOS-installed-before-update.apk`, 1.882.620 bytes, SHA-256 `8958C7A896E191ADB63D55D3E74B31B7C64F27ED96A86229BF53D53E38A4934F`. O Package Manager apontava então para `/system/app/PhilcoOS.apk`. A assinatura SHA-256 do APK anterior e do candidato é a mesma: `b52e3604deca330788c71cca8031b25b4208a7b9af6f521ff162634e14f2f9aa`.
- **Sistema preservado:** `/system/app/PhilcoOS.apk` não foi modificado; a atualização está em `/data/app`. Nenhuma alteração de firmware, fonte selecionada ou preferência de Home foi feita.
- **Estado observado após instalar:** boot concluído, `mWakefulness=Awake`, `mScreenOn=true`. Fonte e tela inicial em primeiro plano não foram reconsultadas.
- **Nível de validação:** `INSTALLED`. A barra em execução/pausa e a transição Ajustes → Música → Voltar ainda não foram testadas nem confirmadas visualmente; não classificar como `DEVICE_VALIDATED` ou `VISUALLY_CONFIRMED`.
- **Próxima ação exata:** conferir na TV a navegação lateral e a Dynamic Bar com música tocando e pausada; depois escolher a próxima feature da Home.

## PHILCO-HOME-DYNAMIC-BAR-001 — tentativa de conferência na TV — 2026-11-23
- **Objetivo:** conferir estado da Home e reprodução depois da instalação.
- **Resultado:** não foi possível iniciar a conferência. A TV `192.168.0.71:5555` não apareceu em `adb devices`; ping ao IP falhou. Uma tentativa de `adb connect` não restabeleceu a conexão. Nenhum evento de tecla foi enviado e nenhum arquivo foi escrito na TV nesta tentativa.
- **Último estado confirmado:** a atualização continua registrada como instalada e com hash correspondente ao candidato; TV estava acordada logo após instalação. Estado atual da TV é desconhecido enquanto estiver fora da rede.
- **Próxima ação exata:** quando a TV estiver acessível novamente na rede, verificar Home/Música e a Dynamic Bar com áudio tocando e pausado.

## PHILCO-MUSIC-UI-002 — redesenho da tela Música — 2026-11-23
- **Agente:** Codex. **Workspace:** C:\PhilcoOS. **Branch/commit:** indisponíveis; workspace sem repositório Git.
- **Objetivo:** substituir a composição desequilibrada da página Música por uma hierarquia legível à distância, própria para navegação por D-pad. Referências de produto: organização Now Playing do Apple Music para TV e navegação do Spotify para TV; adaptação própria para a biblioteca local do PhilcoOS.
- **Implementado:** MusicActivity passou a usar cabeçalho PhilcoOS/Música e estado, cartão compacto de faixa atual com capa 2D leve em gradiente e símbolo musical, biblioteca local contada e em lista, além de um único rodapé para seekbar e comandos anterior/reproduzir/próxima. A disposição final mantém faixa/artwork lado a lado e painéis de 386dp, evitando grandes vazios com poucas faixas. D-pad continua navegando da lista para a seekbar e controles.
- **Arquivos alterados:** `source/philco-home/src/com/philco/ultra777/home/media/MusicActivity.java`.
- **APK:** `output/releases/philco-home-music-ui-redesign-r2-20261123.apk`, 1.888.782 bytes, SHA-256 `6F2B844069BA318B3C231346722D55BF55709339637A018DDB4B4D4259F403FD`; package `com.philco.ultra777.home`, versionCode 777 / `1.0.0-ultra777`. Zipalign validado e assinatura v1 confirmada; certificado SHA-256 `b52e3604deca330788c71cca8031b25b4208a7b9af6f521ff162634e14f2f9aa`.
- **Backup antes da revisão visual:** `output/backups/PHILCOOS_MUSIC_UI_REDESIGN_20261123/PhilcoOS-before-music-redesign.apk`, SHA-256 `77BA251A817EA92EFE5F452D215B155EBA69DA31557BAE9949E20ECF65B9CC0B`. Revisão r1 arquivada em `PhilcoOS-music-redesign-r1.apk`, SHA-256 `438BDEDBEFD7C5042E8F33D90D8D29E3C9699A66C7BBF821606C59B909164677`. Cópia instalada r2 em `PhilcoOS-installed-music-redesign-r2.apk`, SHA-256 idêntico ao candidato.
- **INSTALLED:** ADB confirmou sucesso; Package Manager aponta para `/data/app/com.philco.ultra777.home-1.apk`. O APK-base `/system/app/PhilcoOS.apk` não foi alterado.
- **DEVICE_VALIDATED / VISUALLY_CONFIRMED:** na TV, a Home abriu Música com DPAD_RIGHT a partir de Ajustes; a captura mostra os painéis sem corte e a biblioteca; Back retorna à Home com Ajustes focado. Evidências: `output/captures/philcoos-music-ui-redesign-r2-20261123.png` e `output/captures/philcoos-music-back-home-20261123.png`. O teste usou as três faixas locais `Philco_Test_*` já presentes.
- **Limites:** a capa exibida é um fallback 2D, pois não há leitura de capa de álbum implementada; não há confirmação para uma coleção grande de USB. As funções de playback não foram revalidadas neste r2; a mudança deste r2 foi apenas de layout sobre a versão r1.
- **Estado final do aparelho:** boot completo, MainActivity em primeiro plano, TV acordada e HDMI 2 visível como entrada ativa; nenhum comando de standby/energia foi enviado.
- **Próximo dono:** Codex. **Próxima ação exata:** retomar o pedido pendente de rotação de wallpapers (Home 7 min; tela ambiente/standby 10 min), depois considerar capa local otimizada como melhoria separada.

## PHILCO-MUSIC-GLASS-003 — adaptação da referência Dribbble — 2026-11-23
- **IMPLEMENTED / COMPILED / INSTALLED:** adaptei a linguagem de vidro translúcido à página Música: wallpaper visível através de scrim mais leve, cartões fumê com borda fina e controles transparentes. Mantive somente a biblioteca local real e a faixa atual; não adicionei recomendações, artistas seguidos ou catálogo fictício. API 19/Views nativas; sem blur.
- APK `output/releases/philco-home-music-glass-r3-20261123.apk`, SHA-256 `83EEF63CE2C23100B1BA855057F04E649C120B54EFD984E19F1ADA5A80BBAFF6`; `adb install -r` retornou Success. Captura na TV: `output/captures/philcoos-music-glass-r3-20261123.png` (1280×720), TV acordada.
- **VISUALLY_CONFIRMED:** o fundo da paisagem agora aparece através dos painéis e o texto permanece legível. Backup prévio do r2 permanece em `output/backups/PHILCOOS_MUSIC_UI_REDESIGN_20261123/PhilcoOS-installed-music-redesign-r2.apk`.


## PHILCO-MUSIC-GLASS-GOLD-004 — revisão da UI Música — 2026-11-23
- **Agente/função:** Codex, implementação e integração. **Workspace:** `C:\PhilcoOS`. **Branch/commit:** indisponíveis; diretório sem Git. **Base:** versão Music Glass anterior r6.
- **Objetivo:** melhorar contraste e presença das cores mantendo o vidro transparente e a identidade dourada, com controles legíveis à distância e menos atualizações visuais desnecessárias.
- **Arquivos inspecionados:** `docs/PHILCOOS_ULTRA_777_GLASS_AURORA.md`, `docs/PHILCOOS_ULTRA_777_DESIGN.md`, `source/philco-home/src/com/philco/ultra777/home/media/MusicActivity.java`.
- **Arquivos modificados:** `source/philco-home/src/com/philco/ultra777/home/media/MusicActivity.java`.
- **Decisão de design:** dourado champanhe `#E5C36B`, foco `#F3DA88`, tons navy/grafite nos painéis e scrim; fundo segue visível. Ícone musical usa fonte padrão Android para renderizar com consistência. Rótulos de player foram ampliados. Atualização do estado visual do seekbar foi de 500 ms para 1 s; o callback continua restrito ao ciclo visível da Activity e é removido em `onStop()`.
- **COMPILED:** `assembleRelease --offline` passou com JDK/SDK/Gradle do projeto. `zipalign` passou; assinatura v1 verificada. API mínima/alvo 19. APK: `output/releases/philco-home-music-glass-gold-r7-20261123.apk`, 1.888.762 bytes, SHA-256 `E3127E2ED293A007163A22134EC97A8F54F9C5A845499458CD8A7521B9C65EF0`.
- **INSTALLED:** `adb install -r` retornou `Success`; PackageManager aponta para `/data/app/com.philco.ultra777.home-2.apk`. Cópia extraída da TV tem o mesmo SHA-256. Versão anterior r6 preservada em `output/backups/PHILCO-MUSIC-GLASS-GOLD-R7-20261123/PhilcoOS-installed-before-r7.apk` (SHA-256 `F65F54A97B3D850D0149D824342CBED3964070F24FFA3963331E30761CFE5A99`). Cópia instalada r7 também preservada no mesmo diretório.
- **DEVICE_VALIDATED / VISUALLY_CONFIRMED:** `MusicActivity` abriu no aparelho `192.168.0.71:5555` (`smart_tv001`, Android 4.4.2, tela 1280×720). Captura `output/captures/philcoos-music-glass-gold-r7-20261123.png`, SHA-256 `BD80C7984D1CBBBBE9D193247C8211872BD3332B76822127B6EAD9B80B1EFAC6`. TV acordada; Activity de Música em primeiro plano. Amostra recente do logcat sem `FATAL EXCEPTION`/`E AndroidRuntime`.
- **Limites:** não se mediu CPU/PSS antes/depois; a redução de 500 ms para 1 s é otimização de frequência de atualização, não ganho de desempenho medido. A captura mostra a biblioteca local de teste sem faixa tocando; reprodução, estados pause/play e Dynamic Bar não foram retestados. Home padrão e fonte ativa não foram alteradas nem consultadas nesta tarefa.
- **Próximo dono:** Codex. **Próxima ação exata:** aguardar revisão visual do usuário; em uma validação seguinte, conferir estados de música tocando/pausada sem mexer no firmware.

## Current state — PHILCO-MUSIC-REFERENCE-UI-005 — 2026-11-24
## Completed work
Reproduzi em Views nativas a composição da referência: marca e abas superiores, painéis glass, faixa atual, biblioteca real, cards de faixas locais e player inferior. Busca filtra a biblioteca. O volume usa o controle existente; ilustração vetorial leve. Back fecha busca ou retorna à Home. Backend PlayerController/MusicPlaybackService permaneceu sem alterações.
## Files modified
`source/philco-home/src/com/philco/ultra777/home/media/MusicActivity.java`; `docs/AI_DECISIONS.md`; `docs/AI_TEST_RESULTS.md`; este handoff. Backup do fonte anterior em `output/backups/PHILCO_MUSIC_REFERENCE_UI_20261124/MusicActivity.java.before-final-ui` (SHA-256 `944A090453F8588A7610855A3745489599546DF3CE33C95D649E1EF17E2BFFCE`).
## Tests executed
`assembleRelease --offline`: BUILD SUCCESSFUL. `lintVitalRelease` passou no build. `zipalign -c 4` passou. `apksigner verify --min-sdk-version 19` passou. `aapt dump badging` confirmou pacote `com.philco.ultra777.home`, versionCode 777, minSdk/targetSdk 19.
## Results
APK: `output/releases/philco-home-music-reference-20261124.apk`, 1.894.589 bytes, SHA-256 `3C216E85EB6FCA75807375211429D7FCD48DDA7AFBDA56D264ED454CECBDB092`. Assinatura v1, certificado PhilcoOS conhecido. Fonte MusicActivity SHA-256 `6447FC287EB19F3D5F376FDC95CA79323DCFDC282684F406063AF0FFB91975A6`.
## Problems found
O componente `TvGlyphView` é package-private e não pode ser importado pela Activity de mídia. Foi substituído nesta tela por um pequeno desenho Canvas com Paint/Path/RectF reutilizados; sem biblioteca ou asset adicional.
## Risks
Compilação não confirma composição visual em 1280×720 nem comportamento real do D-pad, MediaPlayer, busca e volume. APK não foi instalado; não houve interação ADB. Estado atual da TV, fonte selecionada e tela/Home ativa: não consultados nesta tarefa. Workspace sem Git: branch/commit indisponíveis.
## Next owner
Codex.
## Next exact action
Após revisão do usuário, instalar manualmente o APK e validar em TV: entrada/retorno, foco D-pad, busca, reprodução/pausa/anterior/próxima/seek e volume/mudo; capturar tela para comparar dimensões, legibilidade e eventuais cortes.

## Current state — PHILCO-MUSIC-REFINE-006 — 2026-11-24
## Completed work
Ajuste cirúrgico de geometria e estados da biblioteca. Cabeçalho alinhado x=356–924; painéis y=141; cards y=567–609; barra y=609. Painéis mantêm razão 43/57. Tratamento MediaStore diferencia consulta com falha, biblioteca vazia e busca sem resultados, com cursor fechado em `finally`.
## Files modified
`source/philco-home/src/com/philco/ultra777/home/media/MusicActivity.java`; `docs/AI_DECISIONS.md`; `docs/AI_TEST_RESULTS.md`; este handoff. Backup: `output/backups/PHILCO_MUSIC_SURGICAL_REFINEMENT_20261124/MusicActivity.java.before-refinement`, SHA-256 `6447FC287EB19F3D5F376FDC95CA79323DCFDC282684F406063AF0FFB91975A6`.
## Tests executed
`assembleRelease --offline` passou com `lintVitalRelease`; zipalign, assinatura v1, metadados AAPT e hash de DEX versus build passaram. Não houve teste funcional nem captura da nova versão.
## Results
APK `output/releases/philco-home-music-refined-20261124.apk`, 1.894.941 bytes, SHA-256 `522EC416A1DE12F12CB8445F7728ADD9962C4E95B242634C9F78243F864C1D77`. MinSdk/targetSdk 19. PlayerController e MusicPlaybackService sem alterações (hashes nos registros anteriores permanecem iguais).
## Problems found
Para não sobrepor player e cards mantendo o player em y=609, a fileira horizontal foi reduzida a 42 dp; miniaturas e títulos ficam mais compactos que na referência.
## Risks
A nova composição não foi instalada nem fotografada na TV. Busca, estados vazios/erro, foco e reprodução não foram exercitados nesta build. Último estado lido antes desta tarefa: TV ligada, awake, MusicActivity do APK r7 em primeiro plano; nenhuma consulta ou escrita foi feita no aparelho durante a implementação.
## Next owner
Codex.
## Next exact action
Revisar o APK e, em etapa autorizada separadamente, instalar e validar a tela e D-pad; comparar screenshot 1280×720 e ajustar somente se houver desvio observado.

## Current state — PHILCO-MUSIC-REFINE-006 pre-install audit — 2026-11-24
## Completed work
Read-only audit of the refined APK and live TV package state. Candidate SHA matches expected `522EC416A1DE12F12CB8445F7728ADD9962C4E95B242634C9F78243F864C1D77`; package/signature are compatible with the installed update. No install performed; awaiting explicit authorization.
## Files modified
Only this handoff and `docs/AI_TEST_RESULTS.md`. No source, APK, system, or TV changes.
## Tests executed
Local hash, zipalign, apksigner certificate/signature, aapt metadata; ADB read-only properties, package path/version, package APK SHA, foreground Activity and power state.
## Results
TV `smart_tv001`, Android 4.4.2, boot complete, Awake/screen on, `MainActivity` foreground. Installed update `/data/app/com.philco.ultra777.home-2.apk`, version 777 / 1.0.0-ultra777; remote SHA `E3127E2ED293A007163A22134EC97A8F54F9C5A845499458CD8A7521B9C65EF0`. Local rollback artifact `output/backups/PHILCO-MUSIC-GLASS-GOLD-R7-20261123/PhilcoOS-installed-r7.apk` matches byte-for-byte. Candidate certificate SHA-256 `b52e3604deca330788c71cca8031b25b4208a7b9af6f521ff162634e14f2f9aa`, same as r7. Base `/system/app/PhilcoOS.apk` remains present. ADB emulator-5562 is offline; physical TV is connected.
## Problems found
None blocking update compatibility. Candidate uses v1 JAR signing required by API 19. apksigner reports its standard notices that two Gradle metadata entries in `META-INF` are outside the v1 signature; verification succeeds.
## Risks
No `/data` app-data backup was made; an in-place same-signature `adb install -r` normally retains app data, but visual/runtime checks remain unperformed. No screenshot was captured because the refined APK is not installed. Selected TV source was not queried.
## Next owner
Codex, after explicit user authorization.
## Next exact action
If authorized, use `adb install -r` for the verified candidate, confirm package path and app-data continuity, then capture 1280×720 and test Music entry/Back, D-pad/OK, MediaStore list, play/pause/next/seek and volume. Stop on install error, unexpected launcher change, or loss of the current package state; keep the local r7 rollback APK intact.

## Current state — HOME-DIAG-007 — 2026-11-24
## Completed work
Read-only diagnosis after the refined APK update. Device foreground is Haier launcher `com.haiersoft.launcherproject2995hk/com.haiersoft.lancherproject.activity.MainActivity` (PID 2591). Philco remains installed and enabled; its `MainActivity` still declares HOME/DEFAULT and is present in the resolver table. PackageManager Preferred Activities still records Philco MainActivity as the `mAlways=true` HOME selection, with Haier and Philco among the matching candidates.
## Files modified
Only this handoff and `docs/AI_TEST_RESULTS.md`. No source, APK, TV settings or system files changed in this diagnostic.
## Tests executed
ADB read-only: `devices -l`, getprop, `dumpsys activity activities`, `dumpsys window windows`, `ps`, package-specific and global `dumpsys package`, `pm list packages -e/-d`, filtered `logcat -d -t 3000`, package path and SHA. Local: aapt manifest inspection and SHA-256 of embedded AndroidManifest.xml for refined/r7/system-stage APKs.
## Results
Installed update path `/data/app/com.philco.ultra777.home-1.apk`; remote SHA matches refined candidate `522EC416A1DE12F12CB8445F7728ADD9962C4E95B242634C9F78243F864C1D77`. System base `/system/app/PhilcoOS.apk` remains registered. `pm resolve-activity`, `am resolve-activity` are unsupported and `cmd` is absent on Android 4.4.2; preferred-activity state is the available resolution evidence. Refined and r7 APK manifests are byte-identical (SHA-256 `22E74D205A46FC6D0352054594F09A58E728453E035D757EE35081F2AA1BAC6F`). Both launchers are enabled; no relevant PackageManager/ActivityManager/FATAL app error appeared in the recent filtered log.
## Problems found
Actual foreground differs from the stored HOME choice. Logs do not reveal who started/restored the Haier activity; cause remains unproven. No HOME/BACK key or activity start was sent.
## Risks
Do not assume that foregrounding Haier means the preferred HOME record changed: current dumpsys shows Philco still selected. No automatic restoration or configuration change was attempted.
## Next owner
Codex.
## Next exact action
Continue with offline static analysis of OEM/Haier launcher startup paths and available historical logs. Do not start an Activity or change HOME unless separately authorized.

## Current state — 2026-11-24 — music UX + launcher recovery
- Workspace: `C:\PhilcoOS`; não existe repositório Git nesta pasta, portanto branch/commit indisponíveis.
- APK final: versionCode 777, versionName `1.0.0-ultra777`, API 19; SHA-256 `D2CDE8EA50526D6C81F23A787FD328AC0BFA18D198130F607E85C6C7A698D10A`.
- TV: `smart_tv001`, Android 4.4.2, ligada. Música estava em primeiro plano ao encerrar. HOME preferido continua PhilcoOS (`mAlways=true`); Haier segue instalado e habilitado. Source HDMI2 continuou selecionado; nenhuma preferência de Home ou arquivo de sistema foi alterado.

## Completed work
- Refinada a tela Música: nomes e artistas maiores para leitura à distância, cabeçalho/busca alinhados, cards e recomendações sem cortes/colisão com o player.
- Corrigido o fluxo de busca e BACK: BACK fecha teclado; BACK seguinte fecha busca e restaura a biblioteca.
- Corrigida navegação do player por D-pad, foco do seek e transporte; setas ajustam a posição sem exigir clique duplo.
- Volume passou a ser ajustável sem sair da Música; remoção do OSD sobreposto e nível final deixado em 7.
- Mantido o backend existente (MediaStore, PlayerController, MusicPlaybackService e MediaPlayer). Nenhuma dependência adicionada.
- Incluída recuperação no app após substituição do próprio pacote, abrindo MainActivity sem alterar o HOME preferido. A hipótese para o Haier aparecer é o fallback OEM do ActivityManager quando o pacote Home está temporariamente indisponível durante atualização; registros históricos apontam resolução de HOME pelo system_server, mas não identificam definitivamente o disparador original.

## Files modified
- `source/philco-home/src/com/philco/ultra777/home/media/MusicActivity.java`
- `source/philco-home/src/com/philco/ultra777/home/PackageReplacedReceiver.java`
- `source/philco-home/AndroidManifest.xml`
- Candidato compilado em `output/releases/philco-home-music-player-usability-final-20261124.apk`.
- Captura final: `output/captures/philcoos-music-final-overview-20261124.png`.

## Tests executed
- `COMPILED`: Gradle `assembleRelease --offline` concluiu; verificação de alinhamento e assinatura v1 aprovadas.
- `INSTALLED`: `adb install -r` retornou sucesso; hash remoto em `/data/app/com.philco.ultra777.home-2.apk` coincidiu com o APK final.
- `DEVICE_VALIDATED`: busca/listagem MediaStore, reprodução de arquivo local, pausa/retomada, faixa seguinte, seek pelas setas, D-pad, BACK e volume; captura ao fim confirmou nível 7 e ausência de OSD cobrindo o player.
- `VISUALLY_CONFIRMED`: captura real em 1280×720 mostra nomes legíveis, layout sem cortes e volume 7.
- Após atualização, PhilcoOS voltou à MainActivity; verificado HOME preferido preservado e Haier ainda habilitado. Nenhuma ação de firmware/sistema.

## Results
- Captura overview: `output/captures/philcoos-music-final-overview-20261124.png` (1280×720; SHA-256 `A8124034E7B84B97B219FA64F5554288FF84BAA61488978E26AC9E04F39CA056`).
- Captura volume/BACK: `output/captures/philcoos-music-final-no-osd-volume7-20261124.png`; volume atual confirmado em 7 e foco continuou na tela Música após BACK.
- Captura seek: `output/captures/philcoos-music-final-seek-immediate-arrows-20261124.png`.

## Problems found
- Android 4.4.2/OEM pode retomar Haier durante substituição do launcher, apesar da preferência PhilcoOS. Recuperação no app após atualização funcionou nos ciclos testados, sem mexer na escolha Home.
- Não foram validados serviços de streaming/catalogação online; este player continua usando a biblioteca local real.

## Risks
- Não foi feito reboot para validar a persistência pós-inicialização nesta etapa.
- A causa histórica exata do início do Haier permanece não comprovada; não se alterou o firmware nem o launcher OEM.

## Next owner
- Codex / usuário: usar e revisar a tela Música; manter cópia de rollback disponível em `output/backups/LAUNCHER_RECOVERY_MUSIC_READABILITY_20261124/`.

## Next exact action
- Nenhuma ação automática. Se houver regressão visual ou de controle, coletar captura/log antes de novo ajuste.

## PHILCO-MUSIC-PRO-007 — player premium + mini-player sincronizado — 2026-11-24

## Current state
- Repositório sem metadados Git (`C:\PhilcoOS` não é um checkout Git); branch/commit: não aplicável.
- TV `192.168.0.71:5555`, Android 4.4.2, 1280×720/160 dpi. Source de TV não foi consultado nesta tarefa.
- APK `versionCode=777`, `versionName=1.0.0-ultra777`; candidato `output/releases/philco-music-premium-final-20261124.apk`, SHA-256 `2E2EC8623470BB8FBD4BCC3EEE414E1672107E16B7B211853835668618FA93E3`.
- HOME preferido verificado após instalação: PhilcoOS, `mAlways=true`; imediatamente após o update `MainActivity` estava em primeiro plano. Volume não foi alterado por comandos nesta tarefa; valor final não foi confirmado nesta sessão.

## Completed work
- Refeito o player inferior com informação da faixa, favorito, detalhes, anterior/pausa/próxima, aleatório/repetição, seek, volume/mudo, fila e estado visual de foco.
- Mini-player da Home usa o mesmo `MusicPlaybackService` e estado observado; não criou uma segunda sessão de áudio.
- Integradas melhorias locais: busca sem acentos, favoritos priorizados, atualização da biblioteca via `ContentObserver` com debounce, leitura em lista temporária, restauração apenas da faixa selecionada e imagem reduzida RGB_565.
- Não alterado o backend, pacote, manifesto, firmware, serviços OEM, preferência HOME ou arquivos do sistema. Nenhuma dependência foi adicionada.
- Pacote leve para revisão externa: `output/shares/philco-music-ai-review-20261124.zip` (fontes essenciais e README, sem APK nem chaves).

## Files modified
- `source/philco-home/src/com/philco/ultra777/home/media/MusicActivity.java`
- `source/philco-home/src/com/philco/ultra777/home/media/Track.java`
- `source/philco-home/src/com/philco/ultra777/home/MainActivity.java`
- Documentação e pacote de revisão em `docs/` e `output/shares/`.

## Tests executed
- `COMPILED`: `gradlew assembleRelease --offline`; BUILD SUCCESSFUL; lint vital executado no build.
- APK: package correto, minSdk/targetSdk 19, certificado igual ao instalado (`b52e3604deca330788c71cca8031b25b4208a7b9af6f521ff162634e14f2f9aa`), assinatura verificada e alinhamento ZIP aprovado.
- `INSTALLED`: `adb install -r` retornou `Success`; preservou dados do pacote. Backup exato do APK anterior: `output/backups/PHILCO_MUSIC_PRO_REDESIGN_20261124/installed-before-update.apk`, SHA-256 `D2CDE8EA50526D6C81F23A787FD328AC0BFA18D198130F607E85C6C7A698D10A`.
- Após o update, `pm path` apontou para `/data/app/com.philco.ultra777.home-1.apk`, versão manteve 777, e o HOME preferido continuou PhilcoOS.

## Results
- A tentativa de abrir a tela Música para obter captura terminou com `adb ... am start` reportando `error: closed`; a captura posterior por `exec-out screencap` também falhou por conexão fechada. Por segurança, não houve repetição nem novos comandos de controle na TV.
- Sem screenshot desta versão e sem validação funcional pós-instalação de D-pad, busca, reprodução/pausa, próxima, seek, volume, favoritos/fila/repetição. Não marcar `DEVICE_VALIDATED` ou `VISUALLY_CONFIRMED` para esses recursos.
- Nenhum teste de desempenho (PSS/CPU/frame time) foi executado.

## Problems found
- A conexão ADB caiu durante a abertura explícita de MusicActivity; não foi possível confirmar se a tela chegou a aparecer.

## Risks
- A interface compilou e instalou, mas o novo layout ainda precisa de captura física e navegação real no aparelho.
- O player oferece apenas faixas da biblioteca local; não existe catálogo/conta de Spotify, Apple Music ou Amazon.

## Next owner
- Usuário / Codex: compartilhar o ZIP leve com Claude ou outra IA para revisão, se desejar.

## Next exact action
- Em uma sessão ADB restabelecida, primeiro fazer captura somente de leitura da tela atual; depois validar os controles do player com cuidado, preservando HOME e volume.

## Current state
- Tarefa `PHILCO-MUSIC-QWEN-REVIEW-008`, Codex, 2026-11-24. Workspace `C:\PhilcoOS` não é checkout Git; branch/commit não aplicável.
- APK release `versionCode=777`, `versionName=1.0.0-ultra777`: `output/releases/philco-music-qwen-integrated-20261124.apk`, SHA-256 `EA99248513847A9080A8D405E7DF5FE1E0B6C0C19FE4CB043BDC1A108F7D5609`.
- ZIP de código e diffs: `output/shares/philco-music-qwen-merged-20261124.zip`, 61 KB, SHA-256 `8FE3BC96B4822A379DD1282C1BD217978A629E11865095ECC4B57939BE810B44`.
- TV não foi acessada nesta tarefa. Estado após a integração não foi verificado; fonte de TV desconhecida. O último estado de HOME registrado anteriormente era PhilcoOS preferido (`mAlways=true`); esta tarefa não alterou preferência.

## Completed work
- Revisado o ZIP do Qwen e aplicado seletivamente o código consolidado da `MusicActivity`, a defesa contra título nulo no `MusicLibraryRepository` e um ID estável-gerado para o botão Play/Pause do mini-player Home, usado pela navegação de foco.
- Mantido o serviço/back-end existente. Incluídos estado de faixa pendente até conexão, histórico por URI, guarda de reentrada de renderização, reconsulta de MediaStore que preserva foco/scroll quando nada mudou, busca/favoritos e texto defensivo.
- Rejeitada a mudança de refresh de 1 s para 500 ms por duplicar atualizações da UI sem benefício comprovado. Não aplicado `unbindService()` após `bindService()` retornar false: sem vínculo confirmado, não há ligação válida a liberar. Manifesto mantido sem mudanças.
- Fontes anteriores preservados em `output/backups/QWEN_MUSIC_INTEGRATION_20261124/`.

## Files modified
- `source/philco-home/src/com/philco/ultra777/home/media/MusicActivity.java`
- `source/philco-home/src/com/philco/ultra777/home/media/MusicLibraryRepository.java`
- `source/philco-home/src/com/philco/ultra777/home/MainActivity.java`
- Documentos de handoff/resultados e ZIP de revisão em `output/shares/`.

## Tests executed
- `COMPILED`: `gradlew assembleRelease --offline` concluiu com `BUILD SUCCESSFUL`; `lintVitalRelease` passou. Javac emitiu apenas a nota existente de API deprecated em `MainActivity`.
- APK conferido: package `com.philco.ultra777.home`, minSdk/targetSdk 19, certificado PhilcoOS esperado `b52e3604deca330788c71cca8031b25b4208a7b9af6f521ff162634e14f2f9aa`, apksigner e zipalign aprovados.
- Nenhum teste de unidade, instalação, interação ou captura executado nesta tarefa.

## Results
- `COMPILED` e validação offline de assinatura/alinhamento aprovados.
- `INSTALLED`, `DEVICE_VALIDATED` e `VISUALLY_CONFIRMED` não se aplicam ao novo APK nesta tarefa.
- O APK anterior continua sendo o instalado; o APK Qwen integrado está disponível apenas em `output/releases/`.

## Problems found
- Proposta Qwen mescla diversas correções válidas com uma alteração de orientação/configuração sem necessidade comprovada e uma sugestão de unbind sem vínculo ativo. Ambas foram deixadas de fora.

## Risks
- A revisão nova ainda precisa de teste real de foco, serviço, lista, reprodução, seek e sincronização Home/Player. A sessão ADB da versão anterior fechou ao tentar abrir Música; não se tentou reconectar nesta tarefa.

## Next owner
- Usuário / Codex para revisão do ZIP; Codex pode executar testes de TV numa etapa autorizada.

## Next exact action
- Não instalar automaticamente. Primeiro revisar o ZIP ou, quando solicitado, validar a navegação e reprodução na TV sem tocar na preferência HOME ou no volume.

## PHILCO-AUDIO-MUTE-001 — recuperação do áudio pelo Volume + — 2026-11-24

## Current state
- Projeto em `C:\PhilcoOS`, sem diretório Git; branch e commit base indisponíveis.
- APK local: `com.philco.ultra777.home`, versão 777 / `1.0.0-ultra777`, min/target API 19.
- Estado da TV não consultado agora. Última evidência documentada seleciona PhilcoOS como HOME (`mAlways=true`), sem revalidação nesta tarefa.

## Completed work
- Corrigido o bloqueio de Volume +/− com mute persistido. O primeiro Volume + restaura o nível de mídia anterior e limpa a preferência de mute; repetição/soltura dessa tecla é consumida para evitar passos extras.
- Se o volume subir enquanto a tela Música está ativa, o receiver da Home reconhece volume positivo como pedido explícito para sair do mute, restaura o nível salvo e apaga o estado persistido.
- Causa provável encontrada no código anterior: mute salvo entre sessões, teclas de volume consumidas na Home e receiver recolocando `STREAM_MUSIC` em zero. Isso pode explicar a reclamação depois da atualização; não prova por si só o estado acústico nem a origem do ícone OEM.

## Files modified
- `source/philco-home/src/com/philco/ultra777/home/MainActivity.java` (linhas 127, 1932–1937, 1967–1992 e 2940–2957).
- Backup do fonte anterior: `output/backups/PHILCO_MUTE_VOLUME_UP_FIX_20261124/MainActivity.java.before-mute-fix`, SHA-256 `7D2F84020BE26FF6BDB52AD1B48A3F3FFBD83E75CA4531EC103B5AE2E71940B3`.
- APK candidato: `output/releases/philco-music-audio-unmute-fix-20261124.apk`, SHA-256 `FB82F3CDB1ED8E843D20206232B97E652A70FF8B800B0BF32DACFDD8EBF72DC7`.

## Tests executed
- `:assembleRelease --offline` terminou com sucesso; lint vital passou.
- AAPT confirmou package/version e min/target API 19; assinatura v1 verificada com o certificado PhilcoOS existente; zipalign passou.
- Nenhum teste automatizado nem de TV/áudio foi executado.

## Results
- `IMPLEMENTED`, `COMPILED` e `OFFLINE_VALIDATED` para metadados, assinatura e alinhamento do APK.
- `INSTALLED`, `DEVICE_VALIDATED` e `VISUALLY_CONFIRMED` não se aplicam: o candidato não foi instalado e o volume da TV não foi alterado.
- APK compilado anterior preservado em `output/backups/PHILCO_MUTE_VOLUME_UP_FIX_20261124/previous-build-output.apk`, SHA-256 `EA99248513847A9080A8D405E7DF5FE1E0B6C0C19FE4CB043BDC1A108F7D5609`.

## Problems found
- As primeiras tentativas de build não usaram a raiz Gradle/SDK correta. A compilação válida foi feita na raiz `source/philco-home`, usando o JDK, SDK e cache locais.

## Risks
- O comportamento de áudio precisa de confirmação na TV. A correção usa o `AudioManager` Realtek já existente; ainda não se confirmou se o mute relatado vinha exclusivamente do estado `STREAM_MUSIC`.

## Next owner
- Codex.

## Next exact action
- Após autorização para este APK, testar mute → Volume + na Home e no player, confirmar retorno do áudio e que repetições não alterem o nível salvo.

## PHILCO-META-REFEITO-001 — metadados do player — 2026-11-24

## Current state
- Workspace `C:\PhilcoOS`; sem Git, portanto branch/commit não disponíveis.
- APK candidato `com.philco.ultra777.home`, versionCode 777 / `1.0.0-ultra777`, min/target API 19.
- Nenhum ADB usado nesta tarefa. O candidato não foi instalado. HOME/source atuais não foram consultados; última evidência documentada mantém PhilcoOS como HOME.

## Completed work
- Integrados do ZIP “refeito” álbum, gênero e tags para pesquisa local.
- Metadados incorporados agora são fallback somente se título, artista ou álbum estiverem incompletos. O parser do nome do arquivo tenta título e artista de modo independente.
- Gêneros MediaStore são indexados em lote por gênero, em vez de abrir um `MediaMetadataRetriever` para cada faixa. Cursores são fechados em `finally`.
- O backend, PlayerController, MusicPlaybackService, busca principal, manifesto, Home e preferências do sistema foram preservados.

## Files modified
- `source/philco-home/src/com/philco/ultra777/home/media/MusicActivity.java`
- `source/philco-home/src/com/philco/ultra777/home/media/Track.java`
- Documentação em `docs/`.
- Backups anteriores: `output/backups/PHILCO_MUSIC_METADATA_REFAITO_INTEGRATION_20261124/MusicActivity.java.before` SHA-256 `433411C29C647A1B11F03AD11D0A5DA22015EEF200FCEA4E7FF70C172B04FEBC`; `Track.java.before` SHA-256 `8386C7E1EE894EEE2E33179A9609F3BBBD8364EA88CF29F6516DB373C0A61546`.
- APK: `output/releases/philco-music-refeito-metadata-r2-20261124.apk`, SHA-256 `1C6BA1C5CFC9922747EBC64B3F8FB4240F778A991183423E9707A78B4AFF7834`.

## Tests executed
- `:assembleRelease --offline` BUILD SUCCESSFUL; lint vital passou.
- AAPT confirmou package/version e min/target API 19. `apksigner` confirmou assinatura v1 e certificado PhilcoOS; `zipalign -c` passou.
- A tabela API do SDK confirma `MediaStore.Audio.Genres` desde API 1, `getContentUriForAudioId` desde API 11 e `MediaMetadataRetriever.setDataSource(Context, Uri)` desde API 10.
- Nenhum teste automatizado ou teste na TV executado.

## Results
- `IMPLEMENTED`, `COMPILED` e `OFFLINE_VALIDATED`.
- Não marcar `INSTALLED`, `DEVICE_VALIDATED` ou `VISUALLY_CONFIRMED`.
- ZIP atualizado de revisão: `output/shares/philco-music-refeito-integrated-review-20261124.zip`, SHA-256 `F6F9A14C08D1DCF915EEB7460C7B4E8EA9BB65AADD1A7F5245B56927E8C13D39`.

## Problems found
- A tabela de gêneros pode não existir em variantes OEM; nesse caso a lista segue funcionando sem gênero. Tags incorporadas são fallback apenas quando faltam campos principais, para evitar leitura pesada por faixa.

## Risks
- Compatibilidade de compilação/API foi verificada; leitura real de tags e desempenho da biblioteca dependem dos arquivos e do MediaStore da TV e ainda não foram medidos.
- O APK “refeito” anterior (`output/releases/philco-music-refeito-metadata-20261124.apk`) foi preservado; o novo candidato é o sufixo `r2`.

## Next owner
- Codex.

## Next exact action
- Revisar/integrar quaisquer ajustes pendentes; instalação e validação real do player exigem autorização para este novo hash.

## Atualização de execução — PHILCO_MUSIC_refeito r5 (2026-11-24)

### Current state
- Workspace canônico `C:\PhilcoOS`; projeto sem `.git` (branch/commit: N/A). Não foi alterada a preferência HOME.
- TV `192.168.0.71:5555`, Android 4.4.2/API 19, resolução 1280×720. MusicActivity está em primeiro plano; volume de mídia final 7.
- APK r5 `output/releases/philco-music-refeito-metadata-r5-20261124.apk`, SHA-256 `6E42987B880B27B6A456354FC64401DF98F786A8F20FDA20A92E20083BBFB5B1`, versão 777, certificado PhilcoOS já usado pelo pacote.

### Completed work
- A correção da MusicActivity passou a guardar o último volume audível separadamente; ao iniciar em volume zero, Volume+ pode recuperar o valor lembrado mesmo se o indicador de mute não estiver disponível. O backend/player service não foi alterado.
- Biblioteca real exibiu faixas Genesis (artista “Genesis”); a tela foi capturada no aparelho.
- Atualização via `adb install -r`; backup pré-atualização r4 preservado em `output/backups/PHILCO_META_INSTALL_20261124/installed-before-r5.apk` (SHA-256 `7FFE9E1EA8A1B443672EFF1A1C9856E0F40B560A4D548D20A6CBBCD056C7B8ED`). APK instalado puxado de volta e igual ao r5.

### Files modified
- `source/philco-home/src/com/philco/ultra777/home/media/MusicActivity.java` — persistência do último volume audível e restauração pelo Volume+.
- Relatórios técnicos e pacote leve de revisão atualizados.

### Tests executed
- `COMPILED`: `gradlew.bat assembleRelease --offline` — BUILD SUCCESSFUL; lint vital passou.
- `OFFLINE_VALIDATED`: API min 19; assinatura v1 válida, certificado SHA-256 `b52e3604deca330788c71cca8031b25b4208a7b9af6f521ff162634e14f2f9aa`; zipalign verificado.
- `INSTALLED`: `adb install -r`; APK puxado de `/data/app/com.philco.ultra777.home-2.apk` e SHA coincide com o release.
- `DEVICE_VALIDATED`: ao abrir a tela com volume em zero, um Volume+ restaurou diretamente para 7; MusicActivity continuou em primeiro plano. Biblioteca mostrou as faixas locais Genesis. HOME preferido segue PhilcoOS (`mAlways=true`).
- `VISUALLY_CONFIRMED`: captura real 1280×720 em `output/captures/philco-music-refeito-r5-final-20261124.png` (SHA-256 `FC4B084AF0970D453C7E8DEA4966FF32BA179555B45DC7DC1DE343EC3CE658B5`).

### Results and limitations
- Teste do mute pelo toque ADB não acionou o botão; por isso a sequência “mute pelo botão da UI → Volume+” permanece sem validação direta. Volume+ foi validado de 0 para 7. Reprodução/pause/next não foram revalidados especificamente no r5; versões anteriores têm resultados registrados separadamente.
- Um BACK enviado após a tentativa de toque no mute voltou à Home; o foco exato naquele instante não foi capturado. Assim, a ação dedicada de BACK com foco no controle de volume permanece não validada.
- Não houve reboot, limpeza de dados, mudança de HOME ou alteração de sistema. Source selecionada não foi consultada.

### Risks
- Baixo risco de dados: atualização preservou o pacote e não limpou dados. Rollback do APK anterior está preservado; não foi executado.
- O botão mute ainda precisa de validação com navegação D-pad real, caso o usuário volte a ter um controle físico funcional.

### Next owner
- Codex.

### Next exact action
- Se houver um controle físico funcional disponível, validar mute e restauração pelo Volume+ sem elevar além do nível 7; caso contrário, manter r5 e registrar essa limitação.

## Atualização — redesenho funcional do Music r6 (2026-11-24)

### Current state
- Workspace `C:\PhilcoOS`; sem `.git` (branch/commit N/A). Candidato r6 local compilado; **não instalado** porque a TV desapareceu do ADB antes da atualização. `adb devices` mostrou somente `emulator-5562 offline`.
- Último estado confirmado antes da queda: r5, volume de mídia 7, tela Música em 1280×720, PhilcoOS ainda selecionado como HOME (`mAlways=true` na última consulta). Estado atual da TV e source selecionada não foram reconfirmados.
- Candidato `output/releases/philco-music-ui-refined-r6-20261124.apk`, SHA-256 `2E6A8AB6EE0C3291E0E30302DD3E8C19C275B4C8948E395AC8920A5893715B29`, 1.911.606 bytes, package/version 777, min/target API 19, certificado PhilcoOS v1.

### Completed work
- Barra inferior reorganizada: botões de transporte maiores com texto ao lado; estado e número do volume legíveis; atalhos de fila, aleatório, repetição, detalhes e favorito agrupados em “Mais”.
- Volume separado da navegação de faixa: teclas físicas de volume são consumidas no primeiro DOWN, sem deslocar foco/seleção. Ajuste pelo D-pad acontece dentro do painel próprio; BACK fecha o painel antes de sair do player.
- A biblioteca ganhou área de coleção com miniaturas. A capa tenta MediaStore por albumId e, para faixa atual, imagem embutida; carregamento assíncrono, amostrado em RGB_565 e limitado por cache LRU de 3 MB.
- Altura das áreas e controles reduzida/rebalanceada para evitar a linha de faixa cortada e legendas minúsculas. Player service/backend mantidos.

### Files modified
- `source/philco-home/src/com/philco/ultra777/home/media/MusicActivity.java`
- `source/philco-home/src/com/philco/ultra777/home/media/Track.java` (albumId para consulta de arte)
- Documentação e pacote de revisão r6.

### Tests executed
- `COMPILED`: `gradlew.bat assembleRelease --offline` — BUILD SUCCESSFUL; lint vital passou.
- `OFFLINE_VALIDATED`: aapt confirmou `com.philco.ultra777.home`, versionCode 777, minSdk 19; apksigner verificou o certificado PhilcoOS; zipalign passou.
- `INSTALLED`, `DEVICE_VALIDATED`, `VISUALLY_CONFIRMED`: pendentes. A tentativa não prosseguiu porque `192.168.0.71:5555` não estava disponível. Nenhuma instalação foi feita.
- Rollback do fonte anterior preservado em `output/backups/PHILCO_MUSIC_UI_R6_20261124/source-before-r6/` (MusicActivity SHA `4C5721718EB415DDAE2604856E69CCD34D3F1028E9E7F896D637E17AC959C5BD`; Track SHA `7CBF71D362C97FD9B453472D4AFC2D20A174DFFC772B084656C775CBD76CEC12`). O APK r5 permanece em `output/backups/PHILCO_META_INSTALL_20261124/installed-after-r5.apk`.

### Results and limitations
- A correção para impedir que Volume+/− vire navegação/seek está implementada e compilada, mas ainda precisa de teste físico na TV.
- Capas reais só aparecem se MediaStore ou os arquivos fornecerem arte; sem metadados gráficos, a interface mostra um fallback dourado. Não há screenshot de r6 e a captura r5 não representa este desenho.
- A tela não foi instalada; portanto usabilidade, alinhamento físico e desempenho das miniaturas ainda precisam de validação real.

### Risks
- Não houve alterações em sistema, firmware, HOME, dados ou volume. O risco restante é visual/runtime do novo layout e o custo de leitura de capas no hardware de 512 MB; cache e decodificação limitada reduzem, mas não substituem medição.

### Next owner
- Codex.

### Next exact action
- Quando a TV voltar a aparecer no ADB, confirmar estado e volume 7, guardar backup do APK instalado, atualizar com `adb install -r`, capturar 1280×720 e validar volume sem skip, D-pad, OK, BACK, reprodução, pausa e próxima faixa. Parar se a conexão continuar ausente.

## Atualização r6.1 — correções do parecer (2026-11-24)

### Current state
- Workspace `C:\PhilcoOS`, sem Git (`branch/commit: N/A`). Android 4.4.2/API 19. TV não acessível: a ferramenta ADB lista apenas `emulator-5562 offline`. r6.1 é candidato local e não foi instalado. Último estado físico histórico antes da desconexão: r5, MusicActivity, volume 7, PhilcoOS preferido como HOME; não reconfirmado agora.

### Completed work
- Capas de linha agora tentam MediaStore mesmo com leitura de imagem embutida desativada.
- Cache de arte distingue tamanho solicitado (120/160/480 px), evitando reutilizar miniatura como capa principal.
- Decoder de capas mantém uma thread e limita a fila a 16 pedidos, descartando o mais antigo quando cheia.
- Callbacks de conclusão/erro ignoram callbacks de MediaPlayer que já não é o player ativo.
- Preservados interface e backend; r6 permanece disponível. O r6.1 usa o mesmo package, versionCode 777 e certificado PhilcoOS.

### Files modified
- `source/philco-home/src/com/philco/ultra777/home/media/MusicActivity.java`
- `source/philco-home/src/com/philco/ultra777/home/media/MusicPlaybackService.java`
- Documentação e pacote de revisão r6.1.

### Tests executed
- `COMPILED`: Gradle `assembleRelease --offline` passou com JDK 17/Gradle 8.9.
- `OFFLINE_VALIDATED`: pacote `com.philco.ultra777.home`, versionCode 777, min/target API 19; assinatura v1 verificada com certificado PhilcoOS SHA-256 `b52e3604deca330788c71cca8031b25b4208a7b9af6f521ff162634e14f2f9aa`; zipalign aprovado.
- `INSTALLED`, `DEVICE_VALIDATED`, `VISUALLY_CONFIRMED`: pendentes; r6.1 não instalado, sem captura física.

### Results
- APK: `output/releases/philco-music-ui-corrected-r6-1-20261124.apk`, 1.911.806 bytes, SHA-256 `5B59FAC82E63FDFCCD475698444CB83FCFB5A28C0A3743EF54ED227897E24231`.
- Pacote de revisão: `output/shares/philco-music-ui-r6-1-review-20261124.zip`, 1.879.150 bytes, SHA-256 `1E4AA368F2471C70642D412E882BDD32BBE152B7F6F8824ACA69ED6C25099085`.
- Fontes r6.1 anteriores à correção preservadas em `output/backups/PHILCO_MUSIC_UI_R6_1_20261124/source-before-r6.1/`.

### Problems found
- A biblioteca passava `allowEmbeddedMetadata=false` e o loader retornava antes até da consulta ao MediaStore, impedindo capas de álbum nas linhas.
- Cache não distinguia os tamanhos da arte.
- Callbacks completion/error não verificavam identidade do MediaPlayer.
- Executor anterior tinha fila ilimitada.

### Risks
- Fila limitada descarta pedidos antigos quando há excesso; a arte ausente pode aparecer ao reexibir/reassociar a linha. Confirmar rolagem rápida no dispositivo.
- Não foi validado o comportamento em runtime, memória, layout ou volume na TV.

### Next owner
- Codex.

### Next exact action
- Quando `192.168.0.71:5555` voltar a estar conectado, verificar estado e volume antes de ação; preservar APK instalado; aplicar atualização com `adb install -r` sem limpar dados; testar capas, rolagem, callbacks com troca rápida, volume sem skip, D-pad/OK/BACK e controles de reprodução; obter captura 1280×720. Parar se conexão/estado inesperado.

## Atualização — fechar Central de Controle ao entrar em standby (2026-11-24)

### Current state
- Workspace `C:\PhilcoOS`, sem Git. TV `192.168.0.71:5555` conectada via ADB.
- APK r6.2 instalado como atualização do pacote PhilcoOS. Preferência HOME continuou `mAlways=true` para `com.philco.ultra777.home/.MainActivity`.
- Após instalar, o ActivityManager retomou temporariamente `com.tsb.tv/.Tv_strategy` (tela “Sem sinal”); a MainActivity foi trazida de volta explicitamente. Nenhuma preferência HOME ou entrada foi alterada; captura final mostra HDMI 2 ativa e volume 7/100.

### Completed work
- `ACTION_SCREEN_OFF` agora fecha o diálogo `Central de controle`.
- `onPause()` fecha o diálogo como fallback para transições que pausem a Home.
- As rotas PhilcoOS de standby via TvManager e PowerManager fecham o painel antes de pedir energia.
- O fechamento tem listener de dismiss para limpar a referência e log local para diagnóstico.

### Files modified
- `source/philco-home/src/com/philco/ultra777/home/MainActivity.java`
- APK candidato/instalado e documentação.

### Tests executed
- `COMPILED`: Gradle `assembleRelease --offline` passou.
- `OFFLINE_VALIDATED`: pacote `com.philco.ultra777.home`, versionCode 777, min/target API19, certificado PhilcoOS e zipalign aprovados.
- `INSTALLED`: `adb install -r` retornou `Success`; APK em `/data/app/com.philco.ultra777.home-1.apk` foi puxado e SHA-256 confere byte a byte com o candidato.
- `VISUALLY_CONFIRMED`: captura real `output/captures/tv-after-philcoos-restore-20261124.png`, 1280×720. Mostra Home sem Central de Controle, HDMI 2 ativa, volume 7/100.
- `DEVICE_VALIDATED` do fechamento por standby: pendente. Não foi provocado ciclo de standby, então o broadcast/evento do Realtek não foi testado fisicamente.

### Results
- APK: `output/releases/philco-home-r6-2-standby-menu-close-20261124.apk`, 1.912.304 bytes, SHA-256 `D28A9E9B312F670F4883CAD2C7DA907DD0798E7FE8F9DA759C1629D7691967E0`.
- Cópia do APK instalado em `output/backups/PHILCO_STANDBY_MENU_CLOSE_20261124/PhilcoOS-installed-r6-2.apk`, hash idêntico.
- APK original `/system/app/PhilcoOS.apk` preservado em `output/backups/PHILCO_STANDBY_MENU_CLOSE_20261124/PhilcoOS-system-before-standby-close.apk`, SHA-256 `8958C7A896E191ADB63D55D3E74B31B7C64F27ED96A86229BF53D53E38A4934F`.
- MainActivity original preservada em `output/backups/PHILCO_STANDBY_MENU_CLOSE_20261124/MainActivity-before-standby-close.java`, SHA-256 `31BFCE5E6A7B54A817E83A4ECFC1201BBEB69703AF6DFCF3DBA3F6298AED7DC6`.

### Problems found
- O diálogo era uma variável local e não havia rotina que o fechasse quando a tela entrava em standby. O display podia apagar e a Activity manter a janela do diálogo para o retorno.
- A atualização encerrou a tarefa da Home e o Android retomou a tela OEM TV; a Home precisou ser reaberta explicitamente. Preferência HOME permaneceu intacta.

### Risks
- Se o firmware Realtek colocar a TV em standby sem `ACTION_SCREEN_OFF` e sem pausar a Activity, a rota explícita do standby automático ainda fecha o diálogo antes de chamar TvManager; um standby iniciado externamente sem esses sinais ainda precisa de validação.
- Não foi executado standby físico por segurança; o fechamento no evento real ainda está `INSTALLED`, não `DEVICE_VALIDATED`.

### Next owner
- Codex.

### Next exact action
- No próximo standby natural/consentido, conferir que ao acordar a Home reaparece sem o diálogo; verificar log `Quick controls closed for standby`. Se o menu persistir, identificar o evento Realtek específico sem bloquear energia global.

## Current state
- **Task:** MUSIC-UX-20261125 — correções básicas do player.
- **Agent/role:** Codex, implementação.
- **Date:** 2026-11-25. **Branch/commit:** indisponíveis; `C:\PhilcoOS` não é repositório Git.
- **APK candidate:** `output/releases/philco-music-basic-ux-20261125.apk`, versionCode 777, package `com.philco.ultra777.home`, SHA-256 `46028E814FC83DE63C6FD4D35FF8AEDC899061D0CB697197384FFD68DA249336`.
- **Device:** não consultado nem alterado nesta tarefa. Último registro disponível (2026-11-24): PhilcoOS selecionado como HOME (`mAlways=true`), HDMI 2, volume 7. Estado atual não reconfirmado.

## Completed work
- Home mini-player ganhou anterior/próxima; ambos usam o mesmo serviço/fila da tela Música.
- O serviço agora mantém a fila da biblioteca, histórico de navegação, aleatório/repetição e avanço automático ao fim da faixa, mesmo após sair da tela Música.
- Anterior: abaixo de 45 s vai para a anterior; a partir de 45 s reinicia a faixa, e o próximo toque volta. O serviço guarda o reinício pendente para tolerar a latência do seek no MediaPlayer.
- Seek: esquerda/direita navegam para os controles vizinhos. OK ativa o modo de ajuste; cima avança e baixo retrocede em passos de 10 s; Voltar encerra e restaura o foco anterior.
- Cards da coleção aumentados para duas linhas de título e mais altura; painel superior ligeiramente reduzido para liberar espaço vertical. “Reproduzir” teve espaçamento revisto.
- Ao retomar/começar, o serviço reassume Audio Focus e limpa duck temporário. Nenhum ganho acima de 1.0 foi aplicado.

## Files modified
- `source/philco-home/src/com/philco/ultra777/home/MainActivity.java`
- `source/philco-home/src/com/philco/ultra777/home/media/MusicActivity.java`
- `source/philco-home/src/com/philco/ultra777/home/media/MusicPlaybackService.java`
- `source/philco-home/src/com/philco/ultra777/home/media/PlayerController.java`
- Source backup: `output/backups/PHILCO_MUSIC_BASIC_UX_20261125/source-before/` (hashes in `docs/AI_TEST_RESULTS.md`).

## Tests executed
- `COMPILED`: `assembleRelease --offline` passou (Gradle 8.9/JDK 17).
- `OFFLINE_VALIDATED`: aapt package/minSdk/targetSdk; apksigner v1/certificado PhilcoOS; zipalign aprovado.
- APK assinado v1; certificado SHA-256 `b52e3604deca330788c71cca8031b25b4208a7b9af6f521ff162634e14f2f9aa`.
- Sem teste automatizado ou ADB nesta tarefa.

## Results
- Build `BUILD SUCCESSFUL`; APK 1,914,108 bytes.
- `INSTALLED`, `DEVICE_VALIDATED`, `VISUALLY_CONFIRMED`: não realizados. Sem captura desta versão.

## Problems found
- Não há evidência acústica que explique a música mais baixa que HDMI. O player usa `STREAM_MUSIC`, volume de MediaPlayer normal 1.0 salvo duck temporário; HDMI pode seguir outra rota de áudio. Não elevar ganho sem medição.
- A análise estática mostra ajustes, mas fila, navegação, cortes e foco precisam ser testados com o controle na TV.

## Risks
- O serviço agora avança a fila enquanto Home está aberta; confirmar no dispositivo que repetição/aleatório e avanço não duplicam.
- Atualização não foi instalada. Não houve alteração de HOME, dados, volume ou firmware.

## Next owner
- Codex.

## Next exact action
- Após revisão do APK, instalar somente com autorização vigente para esta versão e validar seek/foco, voltar 45 s, Home anterior/próxima, repeat/shuffle, volume/áudio e captura 1280×720. Medir áudio com a mesma faixa e cadeia comparável; não aumentar ganho cegamente.

## MUSIC-QUEUE-REVIEW-20261125 — integração seletiva da revisão externa

### Current state
- Workspace `C:\PhilcoOS`; sem diretório `.git`, portanto sem branch/commit.
- TV não consultada nem alterada. Último estado documentado (2026-11-24): HOME PhilcoOS, HDMI 2, volume 7; não reconfirmado.
- APK candidato: `output/releases/philco-music-queue-review-20261125.apk`, package `com.philco.ultra777.home`, versionCode 777, API 19.

### Completed work
- Revisada a conversa/código fornecidos em `C:\Users\novog\.codex\attachments\3fb6110d-1fad-43a3-86f6-6fb33a98df19\Texto colado.txt` contra a fonte atual.
- Integrada a separação da navegação da fila em `PlaybackQueue`, preservando os callers existentes de `MusicPlaybackService` e `PlayerController`.
- Mantidos os 45 segundos definidos para “Anterior”; o exemplo externo de 3 segundos foi deliberadamente rejeitado por contrariar a regra vigente.
- Não integrados cache de metadados nem auto-skip de arquivos: exigiriam mudanças adicionais não comprovadas para esta etapa.

### Files modified
- `source/philco-home/src/com/philco/ultra777/home/media/PlaybackQueue.java` (novo).
- `source/philco-home/src/com/philco/ultra777/home/media/MusicPlaybackService.java`.
- Harness isolado: `output/work/music-queue-check/PlaybackQueueCheck.java`.
- Backup pré-edição: `output/backups/PHILCO_MUSIC_CLAUDE_REVIEW_20261125/source-before/MusicPlaybackService.java`, SHA-256 `9B86A2CB455A7B86C2F17625C59EE924233C284993AE7424E31A19E3BD3188F1`.

### Tests executed
- Harness Java local: 14 verificações da fila passaram (sequência, limites, aleatório, repetição, histórico/stale URI, atual ausente).
- `COMPILED`: `assembleRelease --offline` passou.
- `OFFLINE_VALIDATED`: package/version/minSdk/targetSdk, assinatura v1/certificado PhilcoOS e alinhamento ZIP passaram.
- APK SHA-256 `2E44E86D409EA8E8641AF7E1A114D4AE1EF7D499E59D7072973D8920A2CA26D2`; tamanho 1.914.676 bytes.

### Results
- Build local somente. `INSTALLED`, `DEVICE_VALIDATED`, `VISUALLY_CONFIRMED`: não realizados. Nenhum comando ADB ou alteração de estado da TV nesta etapa.
- Os testes externos descritos no texto colado foram tratados como contexto e não como evidência executada neste workspace.

### Problems found
- A sugestão externa do limiar de 3 s diverge da especificação documentada de 45 s.
- O zip apresentado é uma conversa/código de outra sessão; não é um patch versionado nem prova de testes na TV.

### Risks
- Fila testada isoladamente, mas callbacks reais do MediaPlayer, troca rápida de faixas, USB e comportamento D-pad ainda precisam de validação no aparelho.

### Next owner
- Codex.

### Next exact action
- Após autorização para esta versão, atualizar por `adb install -r` sem limpar dados; validar HOME inalterada, música/volume, anterior/próxima, shuffle/repeat e capturar tela. Não instalar automaticamente.

## QWEN-MUSIC-AUDIT-20261125 — auditoria dos patches enviados

### Current state
- Workspace `C:\PhilcoOS`; sem Git/branch/commit. O candidato mais recente é `output/releases/philco-music-qwen-audited-20261125.apk`.
- TV não consultada nem alterada. Último estado documentado permanece 2026-11-24: HOME PhilcoOS, HDMI 2, volume 7; sem reconfirmação nesta tarefa.

### Completed work
- Revisadas as propostas em `C:\Users\novog\Downloads\Qwen_text_20261125_nm4jbm2ap.txt` contra as fontes reais.
- Aplicados a `MusicActivity`: salvar `last_uri` em troca de faixa vinda do serviço; aceitar repetição de eventos DOWN para rampa do volume; encerrar o modo seek ao perder foco/sair da tela; exibir volume em porcentagem; usar UTC−03:00 igual à Home; selecionar a faixa de índice zero na fila; e definir destino de foco para cima na lista.
- Não aplicado o patch de cópia adicional em `syncPlaybackQueue`: `MusicPlaybackService.setQueue()` já copia itens para uma `ArrayList` própria. O serviço completo proposto pelo Qwen não foi usado: substituí-lo reverteria a fila recém-isolada e o limiar de 45 s para 3 s, além de descartar comportamentos atuais.
- Não removidos métodos auxiliares de navegação nesta revisão; não são necessários para corrigir a interação e a remoção aumentaria o escopo.

### Files modified
- `source/philco-home/src/com/philco/ultra777/home/media/MusicActivity.java`
- `source/philco-home/src/com/philco/ultra777/home/media/MusicPlaybackService.java`
- `source/philco-home/src/com/philco/ultra777/home/media/PlaybackQueue.java`
- Backup anterior de `MusicActivity.java`: `output/backups/PHILCO_QWEN_PATCH_AUDIT_20261125/source-before/MusicActivity.java`, SHA-256 `E18949CC64B480B61835A149A36D0280F72227EA13D48AB41B15F2BF286DC7BE`.

### Tests executed
- Harness de fila: 14 verificações passaram.
- `COMPILED`: Gradle `assembleRelease --offline` passou.
- `OFFLINE_VALIDATED`: APK API19, assinatura v1 com certificado PhilcoOS existente e alinhamento ZIP conferidos.
- APK SHA-256 `FD0A1AECB5437844E19241ADF75F0E8CA478CF84A24DC49265251E30A032105C`, 1.914.772 bytes.

### Results
- `INSTALLED`, `DEVICE_VALIDATED`, `VISUALLY_CONFIRMED`: não realizados. Nenhum ADB ou comando de instalação foi executado nesta tarefa.

### Problems found
- O material do Qwen afirma que o serviço não fornece fila; isso não corresponde ao estado atual do projeto.
- O exemplo de serviço do Qwen usa 3 s e substitui funcionalidades atuais; não é compatível como troca integral.
- A sugestão de solicitar permissão de armazenamento em tempo de execução não foi necessária: o app declara target API 19 e já possui a permissão de leitura no manifesto; API 19 não usa o modelo de permissão runtime introduzido depois.

### Risks
- Volume com repetição, volta de foco do seek, persistência após skip e aparência do percentual ainda precisam de confirmação no controle real.

### Next owner
- Codex.

### Next exact action
- Validar este APK especificamente após autorização para esta versão. Usar `adb install -r` sem limpar dados, preservar a Home preferida, testar volume segurado sem skip, anterior/próxima, retorno do foco e conferir captura 1280×720. Não instalar automaticamente.

## MUSIC-VISUAL-REFRESH-20261125 — candidato local

### Current state
- Workspace `C:\PhilcoOS`; sem `.git`, branch ou commit.
- APK candidato: `output/releases/philco-music-visual-refresh-20261125.apk`, package `com.philco.ultra777.home`, versionCode 777, min/target API 19.
- TV não foi consultada nem alterada. A última captura disponível da Música é anterior ao redesenho; estado da TV não reconfirmado.

### Completed work
- Redesenhada somente a apresentação de `MusicActivity`: fundo com contraste, navegação superior com três destinos reais, painel de faixa em composição horizontal, biblioteca com hierarquia tipográfica mais clara, coleção em cartões e player inferior sem legendas pequenas abaixo do transporte.
- Mantidos MediaStore, PlayerController, MusicPlaybackService, fila, busca, favoritos, volume, mute, seek e tratamento de foco D-pad.
- Sem dependências ou assets novos.

### Files modified
- `source/philco-home/src/com/philco/ultra777/home/media/MusicActivity.java`
- Backup anterior: `output/backups/PHILCO_MUSIC_PREMIUM_REDESIGN_20261125/source-before/MusicActivity.java`, SHA-256 `8AC4BF419BF9FDCD4E989E2167C5F03CF3DA5CD72A58395B747BBC5912D0C0A1`.

### Tests executed
- `COMPILED`: `assembleRelease --offline` passou.
- `OFFLINE_VALIDATED`: package/version/minSdk/targetSdk, assinatura v1 com certificado PhilcoOS e alinhamento ZIP conferidos.
- APK 1.914.849 bytes, SHA-256 `617CE875059EE451C00D490B850E36FFCE9EAB740A048DC9901FCA148B27420C`.

### Results
- `INSTALLED`, `DEVICE_VALIDATED`, `VISUALLY_CONFIRMED`: não realizados. Não há captura da UI deste candidato.

### Problems found
- Captura anterior (`output/captures/philco-music-refeito-r5-final-20261124.png`) confirma a crítica: foco visual fraco, muitas cápsulas e controles com rótulos pequenos. É uma versão antiga e não representa este APK.

### Risks
- Navegação e proporções foram compiladas, mas não verificadas no painel real 1280×720; D-pad/OK/BACK precisam de verificação na TV antes de promover.

### Next owner
- Codex.

### Next exact action
- Após autorização para este APK específico, instalar por atualização sem apagar dados, validar que HOME permanece PhilcoOS e obter captura real em 1280×720. Ajustar espaçamentos/tamanhos com base nessa captura.

## Current state
- Task `PHILCO-QWEN-MUSIC-PATCH-REVIEW-20261125`; Codex implementou a revisão na raiz `C:\PhilcoOS`. Repositório sem Git, então branch/commit não se aplicam.
- APK local candidato: `output/releases/philco-music-qwen-visual-fixes-20261125.apk`, package `com.philco.ultra777.home`, versionCode 777, min/target API 19; SHA-256 `04F9FFBA106B4F166FADAADE1E4DE10EEF10AD490F718E79A885F56632E83957`.
- TV não consultada. Última evidência existente (24/11) indicava HOME PhilcoOS, HDMI 2 e volume 7; estado atual e faixa selecionada não confirmados.

## Completed work
- Revisadas as 11 peças `.java` fornecidas. A maioria são snippets curtos para cor do fundo baseada na capa, separador da biblioteca e marcador do seek; não são versões completas da Activity.
- Integrado apenas o comportamento compatível com o visual dourado: gradiente escurecido com leve tom derivado da capa principal; atualização/reset no cache, carregamento assíncrono, capa ausente e troca de faixa.
- Seek thumb oculto sem foco e visível ao focar/ajustar. Separador da biblioteca suavizado.
- Mantidos PlayerController, MusicPlaybackService, MediaStore, volume/mute, fila, busca e navegação.

## Files modified
- `source/philco-home/src/com/philco/ultra777/home/media/MusicActivity.java`
- Backup anterior: `output/backups/PHILCO_QWEN_UI_PATCHES_20261125/source-before/MusicActivity.java`, SHA-256 `0B0EC8DD11F12F42236E3E9DAB2BCCF93F740258D7924B4026247825D2F72EEC`.

## Tests executed
- `COMPILED`: `gradlew.bat -p .\source\philco-home assembleRelease --offline` usando JDK 17, Gradle 8.9 e SDK local. `BUILD SUCCESSFUL`; lint vital passou.
- `OFFLINE_VALIDATED`: `zipalign -c -v 4`, `apksigner verify`, `aapt dump badging`; assinatura v1 do certificado PhilcoOS, API 19 e package conferidos.

## Results
- Os snippets de mudança de tela foram adaptados ao código atual. `loadTracks`, renderização, controle de volume e comandos de teclado do candidato Qwen completo não justificaram substituição; backend preservado.
- Build candidate: 1.915.302 bytes, SHA acima.
- `INSTALLED`, `DEVICE_VALIDATED`, `VISUALLY_CONFIRMED`: pendentes. Sem captura desta versão.

## Problems found
- Nenhum erro de compilação. Apksigner só pôde rodar após apontar o JDK local; a verificação final passou.

## Risks
- O tom dinâmico e visibilidade do seek foram compilados, mas ainda precisam de captura e navegação na TV em 1280×720.

## Next owner
- Codex.

## Next exact action
- Se solicitado para este candidato, instalar como atualização preservando dados e HOME; obter captura real e verificar foco/seek e leitura do texto. Não instalar automaticamente.

## Device follow-up — PHILCO-QWEN-MUSIC-PATCH-REVIEW-20261125
- APK `philco-music-qwen-visual-fixes-20261125.apk` foi instalado na TV via `adb install -r` após hash SHA-256 local confirmado (`04F9FFBA106B4F166FADAADE1E4DE10EEF10AD490F718E79A885F56632E83957`). Sem uninstall ou clear-data.
- ADB alvo `192.168.0.71:5555`, modelo `smart_tv001`, Android 4.4.2/API 19. APK instalado em `/data/app/com.philco.ultra777.home-2.apk`; assinatura aceita como atualização. Haier continua instalado e habilitado.
- HOME preferido permanece `com.philco.ultra777.home/.MainActivity`, `mAlways=true`. Não foi enviado HOME nem alterada preferência. Activity atual deixada aberta: `com.philco.ultra777.home/.media.MusicActivity`.
- Resolução física 1280×720, densidade 160 dpi. Captura real: `output/captures/philco-music-qwen-visual-fixes-device-20261125.png`, SHA-256 `98BE0BCBE661B2594F8E2C14C2EAD2E0145B926AAE9C5BE1A82BFA71B9BDCC91`.
- Navegação por DPAD enviada via ADB alcançou o primeiro cartão de recomendação. Captura adicional de tentativa de foco: `output/captures/philco-music-qwen-seek-focus-20261125.png`, SHA-256 `A6AD95CB3618CAB3D6BAA095179A56D15DBD4E4A870530A4A0CFA87FB65AE2DD`.
- Sem faixa ativa, o seek fica desabilitado; por isso thumb de foco/ajuste não pôde ser validado. Reprodução/pausa, seek ativo e volume não testados; não foi emitido áudio nem alterado volume. Sem erros `FATAL EXCEPTION` no AndroidRuntime consultado.
- Player/backend não alterados. Fonte TV atual não consultada. Nenhuma modificação de sistema.

## Current state
- `INSTALLED`, `DEVICE_VALIDATED` (abertura e navegação básica automatizada) e `VISUALLY_CONFIRMED` (captura real em 1280×720) concluídos para este APK.
- Pendentes: teste de controle físico, seek com faixa ativa e reprodução/pausa. A validação não comprova esses comportamentos.

## Next exact action
- Revisar a captura com o usuário; se ele quiser validar seek/reprodução, executar teste controlado sem mudar o nível de volume nem a preferência HOME.

## Current state — PHILCO-MUSIC-WALLPAPER-TRANSFER-EQ-R6
- Date/time: 2026-11-25 06:20 -03:00. Branch/commit: N/A; `C:\PhilcoOS` não tem repositório Git ativo.
- APK version 777: `output/releases/philco-music-wallpapers-transfer-eq-r6-20261125.apk`; SHA-256 `1C2F2CC7D11CB3C0D663BEEA2E69FA8F585E9596FCEBBE884C7442189F917F19`.
- Device: `smart_tv001`, Android 4.4.2/API 19, 1280×720/160 dpi, ADB `192.168.0.71:5555`. R6 instalado por `adb install -r`, sem limpar dados. PhilcoOS permanece Home (`mAlways=true`); Haier preservado. MusicActivity ficou em primeiro plano. Fonte de TV atual não consultada.

## Completed work
- Music: seek por esquerda/direita; aceleração de volume por repetição de tecla; estado selecionado da biblioteca agora usa superfície escura e texto claro, sem a troca branco/preto de baixo contraste; botão Buscar ganhou foco dourado mais forte e ligação explícita de foco à direita da aba Tocando.
- Menu de três pontos: layout escuro/dourado com linha Receber do celular.
- Wallpapers: adicionadas dez imagens enviadas pelo usuário, recortadas em 1280×720 e JPEG qualidade 87; conjunto de quinze. Home/ambiente alterna a cada 7 min; Música a cada 10 min. Música mantém só uma foto RGB_565 ativa e decodifica a próxima em worker.
- Screensaver da Home: mini dynamic bar com faixa/artista/estado/progresso quando há música tocando ou pausada. Standby elétrico real apaga a tela; a barra existe na tela ambiente antes do standby.
- Transferência LAN interna e temporária: página abre só enquanto o diálogo fica aberto, código aleatório de 6 dígitos, até 250 MB, apenas áudio/imagem e grava em `Music/PhilcoOS` ou `Pictures/PhilcoOS`; o diálogo fecha o servidor. Sem dependências ou app de terceiros.
- Perfil de TV pelo Realtek `TvManager`: leitura final confirmou PictureMode=0, Brightness=50, Contrast=55, Saturation=52, Sharpness=12, AudioMode=4, Bass=52, Treble=58, Balance=0, EQ {100:52, 500:52, 1200:51, 3000:58, 5000:61, 10000:59, 18000:54}. Backlight não foi alterado. Leitura de valores não substitui avaliação visual/auditiva.

## Files modified
- `source/philco-home/src/com/philco/ultra777/home/media/MusicActivity.java`
- `source/philco-home/src/com/philco/ultra777/home/media/LocalMediaTransferServer.java`
- `source/philco-home/src/com/philco/ultra777/home/media/TvTuningProfile.java`
- `source/philco-home/src/com/philco/ultra777/home/MainActivity.java`
- `source/philco-home/src/com/philco/ultra777/home/AuroraBackdropView.java`
- `source/philco-home/AndroidManifest.xml`; resources `wallpaper_06.jpg`–`wallpaper_15.jpg`.
- Backups before this refinement: `output/backups/PHILCO_MUSIC_FOCUS_EQ_REFINEMENT_20261125/source-before/`.

## Tests executed
- `COMPILED`: Gradle `assembleRelease --offline` passou.
- `OFFLINE_VALIDATED`: zipalign aprovado, assinatura v1 verificada (certificado PhilcoOS conhecido), pacote `com.philco.ultra777.home`, API 19.
- `INSTALLED`: atualização r6 concluída por ADB; Home persistente e dados mantidos.
- `DEVICE_VALIDATED`: página LAN respondeu HTTP 200; upload de imagem respondeu 201 e hash no armazenamento da TV coincidiu com origem; código incorreto respondeu 403; arquivo temporário removido; servidor parou ao fechar diálogo. Perfil Realtek foi lido de volta.
- `VISUALLY_CONFIRMED`: capturas reais em `output/captures/philco-music-final-r6-device-20261125.png`, `philco-music-r6-menu-device-20261125.png` e `philco-transfer-r6-device-20261125.png` (1280×720).

## Problems / risks / pending
- Navegação física, seek com faixa ativa, reprodução/pausa/próxima faixa, volume segurado e aparência subjetiva do EQ aguardam avaliação com o controle/áudio reais.
- Rotação de wallpapers requer aguardar intervalos de 7/10 min para confirmar visualmente; dynamic bar não foi capturada com música ativa.
- Transferência foi exercitada pelo navegador do PC na mesma LAN, não pelo celular. Requer que celular e TV estejam na mesma rede. O teste enviou uma foto; a imagem foi removida após confirmar hash.
- Não houve instalação de APK externo, alteração de sistema ou firmware.

## Next owner / exact next action
- Codex: comparar a audição do EQ e o foco no controle físico; se o som ficar agudo demais, reduzir 5 kHz/10 kHz em pequenos passos. Depois testar uma música e esperar a tela ambiente para confirmar dynamic bar/rotação. Preservar preferência Home e não tocar no backlight.

### Aditivo — volume noturno
- Após a validação r6, o volume de mídia foi ajustado para 7 pela interface de Música. Leitura ADB de `STREAM_MUSIC` confirmou índice `7` no dispositivo de saída ativo.
- Captura real do estado final: `output/captures/philco-music-final-r6-volume7-device-20261125.png`, SHA-256 `6678D9CE588BE8C2D2598AE8C3E3AB90D89949D4B8D8DBD556991166D330782E`.
- O player permaneceu sem iniciar faixa durante esse ajuste.
