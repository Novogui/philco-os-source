# PhilcoOS — análise do daemon keypad e do registro PWR

Data: 2026-11-23  
Escopo: análise estática local; sem interação com a TV, escrita, compilação ou instalação.

## Artefatos e validade

- ELF copiado do `/system/bin/keypad` instalado: `output/work/rear-dpad-audit-20261123/keypad`, 9.728 bytes, SHA-256 `FE908E30623A8B7CD2A9176CBDFD0219E6897AA41594FC15688C1963B81EF429`.
- `.text`: VA/offset `0x7d8–0x141f`; `.data`: VA `0x3000`, offset `0x2000`, tamanho `0xb0`; `.bss`: VA `0x30b0`.
- Análise das funções Thumb e limites `.ARM.exidx`, com Capstone local. Entradas principais: parser `0x118c`, lookup de parâmetro `0x8a0`, leitura `0xa08`, loop `0xa28`, inicialização `0x105c`, memória mapeada `0x132c`, leitura de registrador `0x140c`, escrita `0x13f8`.
- O kernel local disponível é apenas o pacote da baseline de agosto (`extracted/factory_baseline/firmware/package4/vmlinux.develop.android.kk.rtd298x.tv001.emmc.bin`, 14.192.628 bytes, SHA-256 `7B5F5476FFBE9B66CC4E128B9BB8480B6638C1995123FADEC793778DC0AB695E`). Ele contém strings `venus_gpio`, mas não corresponde ao build instalado de maio. Os módulos listados como `venus_gpio.ko` são `modules.builtin`; os objetos/fonte e mapa de registradores do build instalado não foram encontrados localmente.
- Não há Git metadata em `C:\PhilcoOS`; commit/branch indisponíveis. Nenhum APK/JAR foi gerado.

## Estrutura comprovada dos sete registros

Tabela em `.data`, início VA `0x3008`, stride `0x18` (24 bytes), sete entradas. Campos observados no código:

| Offset | Campo | Evidência |
|---|---|---|
| `+0x00` | ponteiro para nome `PIN_KEYPAD_*` | `0x105c–0x10cc` passa o nome ao lookup `0x8a0` |
| `+0x04` | byte ativo/presente | `0xa28` ignora a linha se zero; inicialização grava 1 quando encontra parâmetro |
| `+0x08..+0x0f` | descritor de pino, 64 bits | lookup `0x8a0` copia dois words; consumidos por `0xa28` |
| `+0x10` | `cmd` de despacho | carregado em `0xac8`, `0xad2`, `0xb26` |
| `+0x14` | modo | zero entra no ramo de tecla única (`0xb6a`); não zero no ramo repetível (`0xabe`) |

| Índice / VA | Nome | `cmd` estático instalado | modo |
|---:|---|---:|---:|
| 0 / `0x3008` | UP | 19 | 1 |
| 1 / `0x3020` | DOWN | 20 | 1 |
| 2 / `0x3038` | LEFT | 21 | 1 |
| 3 / `0x3050` | RIGHT | 22 | 1 |
| 4 / `0x3068` | SOURCE | 5 | 1 |
| 5 / `0x3080` | MENU | 17 | 1 |
| 6 / `0x3098` | PWR | 5 | 0 |

Os nomes são rótulos da tabela; não provam sozinhos a ligação elétrica. O setup do daemon procura cada nome no conjunto de parâmetros lido de `pcb_enum`. Se encontra, copia o descritor para `+8` e marca `+4=1`. Não altera `cmd` nem modo.

## Interpretação operacional de `PIN_KEYPAD_PWR`

O valor live `0x400000001`, em little-endian de 64 bits, é `01 00 00 00 04 00 00 00`. O loop usa:

- descritor byte `+1` como índice de leitura;
- byte `+3` como valor esperado;
- byte `+4` (byte baixo do word alto) como tolerância.

Assim, PWR seleciona índice 0, espera valor 0 e aceita diferença absoluta estritamente menor que 4. UP/DOWN/LEFT/RIGHT usam o mesmo índice 0, valores esperados 31/39/8/48 e tolerâncias 4/4/4/5. Isso é uma codificação de amostra/valor/tolerância, não uma prova de GPIO digital. Os bytes `+0` e `+2` do descritor não são consumidos pelo loop examinado.

O helper `0xa08` limita o índice a 0–4, lê um word e o reduz ao byte baixo (`uxtb`). Em seguida, `0xa28` compara a amostra com o valor esperado; um segundo sample é lido depois de `usleep(100 µs)` e precisa permanecer a até 2 unidades do primeiro. O valor `0x3f` segue o caminho de “sem correspondência” e limpa estado. A conclusão de que se trata de um keypad analógico/ladder é **PROVÁVEL** pelos valores discretos compartilhando o índice 0; o bloco de hardware/função do registrador segue desconhecido.

### ABI de memória mapeada inferida diretamente do ELF

`0x132c` abre `/dev/mem` e chama `mmap` para uma região de 1 MiB, `PROT_READ|PROT_WRITE`, `MAP_SHARED`, offset `0x18000000`. `0x140c` lê `*(mapBase + (0x48000000 + offset))`, aritmética ARM de 32 bits. O helper passa `0xb8061500 + index*4`; a soma com `0x48000000` transborda para `0x00061500 + index*4`. Sob a semântica Linux de `/dev/mem`, isso corresponde ao endereço físico mapeado `0x18061500 + index*4`.

**COMPROVADO no código user-space:** a operação `/dev/mem`/mmap e a fórmula do endereço efetivo derivada das instruções.  
**DESCONHECIDO:** qual bloco de hardware é `0x18061500`, se é ADC, multiplexador ou outro registrador, qual pino/centro ele representa e a semântica elétrica. Não chamar esse endereço de GPIO sem o mapa do SoC/placa.

## Fluxo causal reconstruído

1. `pcb_enum` é varrido por `0x118c`: nomes e valores hexadecimais são lidos e convertidos por `strtoull`.
2. Inicialização `0x105c` consulta os sete nomes com `0x8a0`; parâmetros encontrados ativam a linha e fornecem os 64 bits do descritor.
3. O daemon abre `/dev/mem`, mapeia 1 MiB e lê os índices permitidos. O loop `0xa28` amostra e valida o byte do registrador; faz segunda leitura/debounce.
4. A linha PWR tem modo zero. O contador incrementa em amostras estáveis; após passar 9, quando o `cmd` atual ainda é diferente, grava o `cmd` da linha (5) e cai no dispatcher.
5. O dispatcher de `cmd=5` sintetiza `EV_KEY 0x225` (`TV_INPUT`) DOWN/UP em `/dev/uinput`. O código do emitter não transporta duração física; o evento é um pulso curto. Isso é compatível com os DOWN/UP de 56 µs observados no event2, mas a captura não prova causalidade para um gesto longo.
6. Para PWR, em amostras subsequentes com o mesmo `cmd=5`, a comparação `counter <= 0x77` adia o ramo de limiar. Quando o contador fica maior que 119, o código grava 1 no global `.bss` VA `0x3514`, imprime diagnósticos e zera o contador temporário. `r6` permanece zero e o fluxo sai para a espera; não chama o dispatcher nessa ramificação.
7. Não foi encontrada leitura do global `0x3514` no código do ELF. Portanto ele parece um marcador sem consumidor local, mas a intenção do autor é desconhecida.

Pseudocódigo aproximado do ramo PWR:

```text
para cada uma das 7 linhas a cada passagem:
  se linha não habilitada: continue
  sample = low8(read32(mapBase + 0x61500 + 4 * descritor.index))
  se sample == 0x3f ou abs(sample - esperado) >= tolerancia:
      limpar estado da tecla; continue
  sleep(100 us)
  second = read novamente
  se abs(sample - second) > 2: continue
  se modo != 0:
      lógica repetível
  senão:                         # PWR, single-key
      count++
      se count <= 9: continue
      se currentCmd != row.cmd:
          currentCmd = row.cmd  # PWR instalado: 5
          dispatch(currentCmd)  # TV_INPUT
      senão se count > 119:
          global_0x3514 = 1     # não define cmd=1 nem chama dispatcher
          count = 0
  sleep(13 ms) ao sair da passagem
```

O loop também usa atraso de 50 ms após despachar um evento; as passagens sem evento terminam com `usleep(0x32c8)` = 13 ms. O contador PWR, portanto, é baseado em amostras mantidas, não na duração do KeyEvent de 56 µs. Limiar `>119` significa nominalmente cerca de 120 ciclos de 13 ms (aprox. 1,56 s, mais overhead e o atraso adicional da primeira emissão), **não 5 segundos**. É estimativa estática, sem medição de relógio em execução.

## Por que `TV_INPUT` e `KEY_POWER` aparecem como opções

O dispatcher possui caso `cmd=1` que emite Linux `KEY_POWER (0x74)` e caso `cmd=5` que emite `TV_INPUT (0x225)`. Isso comprova a tradução `cmd → keycode`, não a origem física do valor `cmd`.

No ELF instalado, o campo `cmd` é parte imutável da tabela em `.data` para o fluxo observado. Os únicos valores são 19,20,21,22,5,17,5; o parser de `pcb_enum` só escreve o descritor e o byte ativo. O global `currentCmd` é inicializado em zero e recebe o campo `cmd` da linha selecionada. Assim, **não há caminho encontrado nesta tabela/ramificação que converta o PWR de cmd 5 para cmd 1**. O caso 1 existe no dispatcher, mas não é alcançado pelo PWR com o estado instalado examinado, salvo alteração de memória/código ou outro produtor/fluxo ainda não identificado.

Isso é evidência nova contra a hipótese anterior de que o próprio ramo de hold do `/system/bin/keypad` emita `KEY_POWER`. Não prova que o daemon jamais possa emitir POWER sob qualquer configuração, nem exclui outro daemon/kernel/OEM.

## Desligamento observado: avaliação

- **COMPROVADO:** existe no emitter uma tradução genérica `cmd 1 → KEY_POWER`.
- **COMPROVADO:** PWR tem `cmd 5`, modo 0 no ELF instalado; seu primeiro despacho configurado resulta em `TV_INPUT`.
- **COMPROVADO no fluxo analisado:** o limiar `>119` apenas grava o marcador `0x3514`, não muda `currentCmd` para 1 e não despacha KEY_POWER.
- **DESCONHECIDO:** a origem do `KEY_POWER` observado em uma captura anterior e a causa do desligamento físico. A captura não correlacionou com segurança evento e gesto; TvServer/driver pode ser outro produtor, mas nenhuma comunicação direta foi provada.
- **Não usar como explicação comprovada:** “segurar PWR no keypad transforma cmd5 em cmd1”. O ELF atual contradiz essa transição no ramo examinado.

## Decisão para implementação futura

A camada com melhor observabilidade de estado contínuo é o daemon `/system/bin/keypad`, pois ele relê o mesmo registro em passagens, enquanto EventHub/Android recebe apenas o pulso uinput. A amostra contínua é **COMPROVADA**; sua relação elétrica exata com o centro é **PROVÁVEL**, baseada no parâmetro PWR e na captura curta `0x225`.

O lugar para medir duração seria a transição da amostra do descritor PWR de “não correspondente” para “correspondente estável”, usando relógio monotônico no daemon. Porém ainda não se deve alterar: antes seria necessário identificar o produtor independente de POWER e evitar a emissão precoce de cmd5. Para distinguir corretamente clique curto de hold, o PWR não poderia emitir TV_INPUT antes da decisão final; clique curto teria de ser convertido em ação OK apenas na soltura, e o hold em um evento dedicado, com suppressão de qualquer POWER proveniente do mesmo gesto. Isso muda comportamento e precisa de teste em cópia/simulador e recuperação comprovada; não foi feito.

### Classificação consolidada

| Conclusão | Estado |
|---|---|
| Serviço executa o daemon e o daemon cria uinput | COMPROVADA por init/ELF anteriores |
| Sete linhas de 24 bytes, seus campos/valores e modo PWR=0 | COMPROVADA no ELF instalado |
| `0x400000001` alimenta índice 0, esperado 0, tolerância 4 | COMPROVADA como interpretação operacional do código |
| Endereço mapeado resultante `0x18061500` | COMPROVADA pela aritmética ELF + mmap; nome/função de hardware DESCONHECIDO |
| PWR corresponde a amostra analógica/ladder | PROVÁVEL, sem esquema/driver |
| PWR estável gera primeiro cmd5/TV_INPUT | COMPROVADA no fluxo configurado do ELF; vínculo com gesto físico PROVÁVEL |
| `>119` é 5 segundos | REFUTADA pelo ciclo nominal de 13 ms; marcador ocorre perto de 1,6 s, sem relógio medido |
| hold do PWR converte cmd5 em cmd1 no daemon analisado | NÃO ENCONTRADO; fluxo atual não faz essa conversão |
| causa do desligamento anterior | DESCONHECIDA |

## Bloqueio e próximo artefato

O artefato específico que impede atribuir o desligamento e autorizar uma implementação segura é o **kernel/build exato de 31/05/2017 com fonte ou símbolos do driver GPIO/ADC/keypad embutido (`venus_gpio`/bloco em `0x18061500`) e o mapa de pinos da `TP.RT2984T.PB801`**. O único vmlinux local é de baseline de agosto e não serve como substituto. Também será necessária uma captura já disponível ou futura, sincronizada e passiva, que associe um evento POWER a uma origem de input; não se deve provocar novo hold para obtê-la.

Sem esse artefato, não é possível provar que o centro não acione um caminho paralelo de energia. **Instalação e teste físico continuam bloqueados.**

## Estado desta rodada

- Nenhuma interação ADB/física, escrita na TV, alteração em código/candidato, build, instalação ou reboot.
- APK: não gerado nesta rodada.
- Estado atual da TV não foi consultado; último estado documentado antes desta rodada era online em HDMI 2, PhilcoOS Home.
- Arquivos originais, hashes, backups e candidatos preservados.

## Pseudocódigo do dispatcher

```text
cmd = currentCmd - 1
switch cmd:
  case 0:       linuxKey = KEY_POWER (0x74)   # cmd original 1
  case 4:       linuxKey = TV_INPUT (0x225)   # cmd original 5
  case 16:      linuxKey = MENU (0x8b)        # cmd original 17
  case 18..21:  linuxKey = DPAD_UP/DOWN/LEFT/RIGHT (0x67/0x6c/0x69/0x6a)
  otherwise:    sem evento para esse cmd

se houver mapeamento:
  escrever EV_KEY(value=1) em /dev/uinput
  escrever EV_KEY(value=0) em /dev/uinput
```

Os números `0..21` resultam do decremento de `currentCmd` antes da tabela `tbh`. A existência do `case 0` demonstra suporte genérico a cmd 1, mas a tabela estática do ELF instalado não fornece esse valor. A associação de evento emitido ao gesto do usuário segue condicionada à origem da amostra física.

## Complemento pontual: produtores alternativos de KEY_POWER — 2026-11-23
- **Descoberta concreta:** o binário legado `extracted/factory_baseline/system/bin/CVTEkeypad` (9.752 bytes, SHA-256 `22C61F86C4F5F0714CA74E610710235FCC473AC8220C800474C1EE5471DFBE8A`) contém tabela PWR em VA `0x3008` e PWR2 em `0x30b0`, ambos com `cmd=1`. No dispatcher `0x0a4c`, o caso `cmd=1` prepara EV_KEY code `0x74` (KEY_POWER) e o envia por `write` ao descritor uinput. Portanto é um **produtor potencial comprovado no binário**, se executado.
- **Atividade:** não há referência a `CVTEkeypad` nos scripts/serviços locais examinados; a evidência read-only da TV registra `KeypadService` ativo como `/system/bin/keypad` (PID 1797). Não há prova de que `CVTEkeypad` estivesse instalado/executando durante o desligamento. Este ELF é baseline/staging, não a cópia instalada de maio.
- **TvServer:** o ELF instalado contém subsistema GPIO/keypad, mas não contém `/dev/uinput`, `Smart_TV_Keypad` ou `KEY_POWER`, e não foi localizado caminho de escrita EV_KEY. Isso não elimina uma ação OEM indireta de power; não prova que TvServer emita o evento Linux.
- **Captura existente:** event2 registrou dois pares `KEY_POWER` (1795453287.594755 e 1795453289.417640), intercalados com `TV_INPUT`; captura parcial sem marcadores de gesto. A TV desligou naquela sequência, logo há risco real de KEY_POWER chegar ao sistema, mas não é possível atribuir ao hold central nem ao produtor.
- **Estado `0x3514`:** no ELF `keypad` instalado, é uma escrita de 1 no ramo `count>119`; nenhuma leitura/xref consumidora foi encontrada no processo. Não altera `currentCmd`, não troca cmd 5 por 1 e não dispara o dispatcher. Destino observado: marcador privado sem consumidor local conhecido.
- **Endereço `0x18061500`:** única ocorrência bruta no vmlinux de agosto em offset `0xB3074B` (desalinhada; sem símbolo/xref); não identifica bloco/register. Fonte/System.map do kernel exato de maio e mapa do bloco continuam ausentes.
- **Conclusão:** risco de desligamento pelo hold permanece **possível e material**, pois a captura registra KEY_POWER em event2 e o legado CVTEkeypad tem uma rota PWR→KEY_POWER. Não está comprovado que o hold tenha gerado esses pares nem que CVTEkeypad estivesse ativo. Nenhuma ação de dispositivo, build ou instalação.
