# INVESTIGAÇÃO DE PERSISTÊNCIA DA ATUALIZAÇÃO PHILCOOS EM /data/app

**Data/Hora local:** 2026-11-23 02:22:00 -03:00  
**Diretório de trabalho validado (CWD):** `C:\PhilcoOS`  
**Alvo ADB:** `192.168.0.71:5555` (`rtd298x_tv001`, `smart_tv001`, Android 4.4.2 API 19)  
**Executável ADB:** `C:\PhilcoOS\.toolchain\android-sdk\platform-tools\adb.exe`  

---

## 1. Status da Coleta Remota via ADB

Em conformidade com a instrução expressa (*"Se ADB/read de arquivos falhar, registre o erro e pare; não tente instalar ou reiniciar"*):

* **Falha de Inicialização do Daemon e Conexão:**
  * O processo do daemon do ADB server (`tcp:5037`) foi finalizado após o reinício do ambiente.
  * A tentativa de inicialização/acesso em modo padrão de sandbox resultou no bloqueio de soquete e rede:
    ```
    * daemon not running; starting now at tcp:5037
    could not read ok from ADB Server
    * failed to start daemon
    adb.exe: failed to check server version: cannot connect to daemon
    [WinError 10013] Foi feita uma tentativa de acesso a um soquete de uma maneira que é proibida pelas permissões de acesso
    ```
  * A tentativa de execução fora do isolamento encerrou com código de saída 1 sem estabelecer o daemon.
* **Ação tomada:** A coleta de telemetria remota ao vivo (`pm path`, `dumpsys package`, `ls -l /data/app`, `df /data`, `mount`, logcat atual) foi **interrompida imediatamente**, sem tentativas de forçar instalação, comandos root ou reboot da TV.

---

## 2. Metadados e Hashes dos APKs Locais (Releases e Backups)

Todos os artefatos locais preservados em `C:\PhilcoOS` foram verificados e auditados quanto a tamanho, SHA-256 e presença de biblioteca nativa:

| Artefato | Tamanho (bytes) | SHA-256 | versionCode | libconscrypt_jni.so interno |
| :--- | :--- | :--- | :--- | :--- |
| `output/releases/philco-home-weather-reference-min-no-accent-20261122.apk` (Base em `/system`) | 1.878.370 | `5C7DBA8B693B10FD7EDD6606A54E106954D2DE6D491B90E0EE77AC7163BA6C3D` | 777 | Sim (`lib/armeabi-v7a/`) |
| `output/releases/philco-home-backlight-recovery-20261123.apk` | 1.878.576 | `8C9495927AEE57CBC8F6FD3223A9792F0DEF47A2F25516F99F04380480DB2B90` | 777 | Sim (`lib/armeabi-v7a/`) |
| `output/releases/philco-home-backlight-idle-fix-20261123.apk` (Release corrigido) | 1.878.544 | `9EBF709350EF7EF980017C947A4AFE05A96AC39E413FC8CCD3F34A4CDB63195F` | 777 | Sim (`lib/armeabi-v7a/`) |
| `output/releases/philco-home-idle-backlight-15-30-60m-20261123.apk` | 1.878.925 | `1C63DCBF371287A1AD7089B91E6E2F5C1D21B71424974EA1EB6BFECE10F40904` | 777 | Sim (`lib/armeabi-v7a/`) |
| `output/backups/PhilcoOS-system-before-conscrypt-repair-20261123.apk` | 1.878.370 | `5C7DBA8B693B10FD7EDD6606A54E106954D2DE6D491B90E0EE77AC7163BA6C3D` | 777 | Sim (`lib/armeabi-v7a/`) |
| `output/backups/PhilcoOS-system-pre-idle-schedule-20261123.apk` | 1.878.370 | `5C7DBA8B693B10FD7EDD6606A54E106954D2DE6D491B90E0EE77AC7163BA6C3D` | 777 | Sim (`lib/armeabi-v7a/`) |
| `output/backups/PhilcoOS-system-before-min-label-20261122.apk` | 1.878.367 | `65313B6085B7B02A71A5632F347FB59EDA2D2678111991E8C9BB53D90069CD21` | 777 | Sim (`lib/armeabi-v7a/`) |

*Nenhuma cópia auxiliar ou temporária foi mantida no repositório.*

---

## 3. Registros Históricos Comprovados vs. Observações Atuais

### A. Registros Históricos Comprovados (em `AI_TEST_RESULTS.md` e `AI_HANDOFF.md`):
1. **Antes do desligamento:** O app PhilcoOS estava instalado em `/data/app/com.philco.ultra777.home-1.apk` (release `9EBF7093...`), com a biblioteca nativa extraída pelo instalador em `/data/app-lib/com.philco.ultra777.home-1/libconscrypt_jni.so`.
2. **Após desligamento abrupto (puxada de tomada):**
   * O `PackageManager` reverteu o carregamento para `/system/app/PhilcoOS.apk` (SHA `5C7DBA...`).
   * A execução falhou na inicialização do TLS com `NoClassDefFoundError: ...Tls12SocketFactory` porque o Android 4.4 **não extrai bibliotecas JNI** de APKs em `/system/app/` para `/system/lib/`, e o diretório `/data/app-lib/` não continha mais a biblioteca da atualização.
   * `adb install -r` do release `9EBF7093...` retornou `Success` imediato, restaurando o arquivo em `/data/app` e a extração em `/data/app-lib/`.
3. **Após reboot controlado (documentado em `AI_TEST_RESULTS.md` l. 367):**
   * O `PackageManager` novamente expôs apenas a base `/system/app/PhilcoOS.apk`, exigindo reinstalação para `/data/app`.

### B. Observações Atuais:
1. Todas as compilações até o momento (base de sistema e atualizações) compartilham rigorosamente o mesmo `versionCode 777` e `versionName 1.0.0-ultra777` declarados em `source/philco-home/build.gradle`.
2. A reinstalação via `adb install -r` nunca falhou por conflito de assinatura, indicando que a assinatura v1 é consistente entre os builds gerados pelo mesmo keystore.

---

## 4. Análise de Causa: Comprovado vs. Hipóteses

### Fatos Comprovados:
* O APK base do sistema (`/system/app/PhilcoOS.apk`) é funcionalmente incompleto para HTTPS moderno porque carece de `libconscrypt_jni.so` em `/system/lib/`.
* A atualização para `/data/app` fornece essa biblioteca via `/data/app-lib/`.
* No ciclo de reinicialização da TV, o Android 4.4 deixa de carregar `/data/app` e reverte para a imagem de `/system`.

### Hipóteses em Aberto:
1. **Hipótese 1 — Rollback voluntário do PackageManager por paridade de versão:**
   * No AOSP KitKat 4.4, quando um aplicativo de sistema (`FLAG_SYSTEM`) possui uma atualização em `/data/app`, o `PackageManagerService` avalia a precedência de versão no boot. Com ambos em `versionCode = 777` (`mVersionCode <= ps.versionCode`), o PMS pode considerar a versão em `/data` obsoleta ou redundante em relação à imagem do sistema recém-escaneada, descartando-a ou ignorando-a.
2. **Hipótese 2 — Falha de persistência física por corte de alimentação:**
   * Desligamento da tomada sem flush de escrita (`sync`) ou perda de transação no journal do sistema de arquivos (`ext4` ou `f2fs`) em `/data`, fazendo a checagem de integridade (e2fsck) reverter a partição para o estado limpo anterior.
3. **Hipótese 3 — Corrupção / invalidação de cache de pacotes:**
   * Invalidação do arquivo de controle `/data/system/packages.xml` ou do Dalvik-cache do app atualizado durante a montagem inicial.

---

## 5. Evidência Específica Necessária para Conclusão Definitiva

Para distinguir categoricamente entre **Rollback lógico pelo PackageManager** e **Falha física de persistência**:

* **Evidência exigida:** Captura das primeiras linhas do `logcat -b main -b system` geradas pelo processo `system_server` durante o boot, filtrando pelas tags:
  * `PackageManager`
  * `scanDirLI`
  * `scanPackageLI`
* **Critério de distinção:**
  * Se o log reportar que o arquivo `/data/app/com.philco.ultra777.home-*.apk` foi encontrado, inspecionado e posteriormente descartado/deletado (ex.: mensagem informando versão inferior ou igual à versão de sistema), **comprova-se o Rollback do PackageManager**.
  * Se o log indicar que o diretório `/data/app` foi escaneado e o arquivo `.apk` simplesmente não constava na listagem de arquivos da partição no momento da leitura, **comprova-se a Falha de Persistência Física em Disco**.
