# PLAN: Refinamento de Design do Weather Card (Apple-Like UI) — PhilcoOS Ultra 777

## 1. Visão Geral e Objetivo
Proposta de composição visual premium para o widget de clima do PhilcoOS Ultra 777, inspirada na clareza tipográfica, respiração e elegância dos widgets de clima da Apple (tvOS / iOS), respeitando as anotações do usuário e a captura de tela `output/captures/weather-clean-card-20261123.png`:
1. **Área esquerda mais estreita:** Otimizar a coluna de ícone e condição para não desperdiçar largura horizontal.
2. **Legibilidade aprimorada:** Condição climática e Cidade/"AGORA" com contraste e tipografia refinados para visualização a 3 metros (10-foot experience).
3. **Destaque da Temperatura:** Maior peso visual e proporção equilibrada para a temperatura atual.
4. **Alinhamento e harmonia de Máx. e Min.:** Alinhamento vertical claro e direto em relação à temperatura e cidade.
5. **Preservação total de requisitos:** Sem divisor central (já removido), mantendo o degradê grafite suave, borda de foco D-pad, footprint de 360×112dp e renderização nativa de baixo custo (sem WebView, sem alocações em onDraw) compatível com Android 4.4.2 KitKat (API 19).

---

## 2. Diagnóstico da Captura Atual (`weather-clean-card-20261123.png`)
* **Coluna esquerda (Condição / Ícone):**
  * Ocupa atualmente `weight = 1f` (~188dp de largura útil), com ícone de 72×72dp e texto de condição centralizado em 15sp. Fica com sobra horizontal excessiva nas laterais do ícone, "empurrando" os dados textuais da direita.
* **Coluna direita (Cidade, AGORA, Temperatura, Máx/Min):**
  * Largura fixa atual de 148dp.
  * O rótulo `AGORA` (9sp) e a Cidade (16sp) ficam ligeiramente comprimidos acima da temperatura.
  * A temperatura (`37sp`, `dp(43)`) tem bom tamanho mas poderia ter um peso tipográfico mais marcante e proporção horizontal mais confortável em conjunto com a Máx/Min.
  * O texto de `Máx 28° · Min 21°` (11sp) fica próximo da base sem uma âncora visual forte com a temperatura.

---

## 3. Especificação do Novo Design (Estilo Apple / tvOS)

### A. Geometria e Dimensões do Card
* **Largura total:** `360dp` (inalterada, respeitando o layout da Home e status bar superior).
* **Altura total:** `112dp` (inalterada, perfeita integração com a safe area e o dock).
* **Corner Radius:** `20dp`.
* **Padding interno:** `left: 14dp`, `top: 10dp`, `right: 16dp`, `bottom: 10dp`.
* **Distribuição de Colunas:**
  * **Coluna Esquerda (Ícone 3D + Condição):** Largura fixa de `116dp` (estreitada de ~188dp para 116dp).
  * **Coluna Direita (Informações Numéricas e Geográficas):** Largura flexível / restante de `214dp` (ampliada de 148dp para 214dp, ganho de +66dp de respiração horizontal).

```
+--------------------------------------------------------------------------------+
|  CARD TOTAL: 360dp x 112dp (Graphite Gradient #454B53 -> #747B84)             |
|                                                                                |
|  [ Coluna Esquerda: 116dp ]        [ Coluna Direita: 214dp ]                   |
|                                                                                |
|          [ 64x64dp ]                 CIDADE (16sp Medium)   •  AGORA (10sp)    |
|           Ícone 3D                   ----------------------------------------  |
|                                      21°C (42sp Display Bold)   Máx 28°        |
|      Garoa (13sp Medium)                                        Min 21° (12sp) |
|                                      ----------------------------------------  |
|                                      Open-Meteo • Atualizado (8sp)             |
+--------------------------------------------------------------------------------+
```

---

### B. Especificação Tipográfica e Cores (Android 4.4 / KitKat)

| Elemento | Texto / Exemplo | Tamanho (sp) | Typeface (API 19) | Cor / Alpha | Sombra / Efeito |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Ícone 3D** | `weather_3d_*.png` | `64×64dp` | N/A | Cor natural do asset | Centralizado horizontalmente |
| **Condição** | `Garoa` / `Ensolarado` | `13sp` | `sans-serif-medium` | `#F2F5FA` (95% branco) | Leve sombra `dp(1)`, 0x66000000 |
| **Cidade** | `Joinville` | `16sp` | `sans-serif-medium` | `#FFFFFF` (100% branco) | `dp(1)` shadow, tracking limpo |
| **Tag Tempo** | `AGORA` | `10sp` | `sans-serif-medium` | `#A6BDD9` (Azul-gelo Apple) | Uppercase, espaçamento sutil |
| **Temperatura** | `21°C` | `42sp` | `displayTypeface` (Lato Bold/Medium) | `#FFFFFF` | Sombra refinada `dp(2)` 0x60000000 |
| **Máx / Min** | `Máx 28°`<br>`Min 21°` | `12sp` | `sans-serif-medium` | `Máx`: `#FFB088` (Tom quente suave)<br>`Min`: `#90CAF9` (Tom frio suave) ou `#DCE6F2` | Empilhadas ou lado a lado com ícones sutis `↑ 28°  ↓ 21°` |
| **Atribuição** | `Open-Meteo` | `8sp` | `sans-serif` (Normal) | `#8E9CAE` (60% contraste) | Rodapé direito discreto |

---

### C. Cores e Superfície (Degradê Grafite Preservado)
* **Background Normal (Unfocused):**
  * `GradientDrawable` com orientação `LEFT_RIGHT`.
  * Início: `#40464E` (Grafite escuro neutro).
  * Fim: `#626972` (Grafite médio acetinado).
  * Stroke: `1dp` com `#33FFFFFF` (acabamento sutil de vidro fosco).
* **Background Focado (D-pad Focus):**
  * Início: `#343A42` (leve escurecimento de contraste).
  * Fim: `#505760`.
  * Stroke: `3dp` com `#FFE28A` (borda dourada característica do foco PhilcoOS Ultra 777) ou `#FFFFFF` dependendo da paleta de foco padrão, garantindo acessibilidade a 3 metros.

---

## 4. Mockups Visuais

### Mockup 1: Close-up do Card do Clima (Apple-Inspired Layout)

```
┌────────────────────────────────────────────────────────────────────────┐
│  ╭─────────╮                                                           │
│  │  (3D)   │     JOINVILLE  •  AGORA                                   │
│  │  CHUVA  │                                                           │
│  ╰─────────╯     21°C       ▲ 28°                                      │
│   Garoa                     ▼ 21°                                      │
│                             Open-Meteo                                 │
└────────────────────────────────────────────────────────────────────────┘
```

*Detalhamento do alinhamento da Coluna Direita:*
* **Linha Superior:** `Joinville` alinhado à esquerda da coluna; ao lado com bullet discreto: `• AGORA` em azul suave.
* **Linha Central:** A temperatura principal `21°C` ganha destaque majestoso (42sp). Imediatamente ao lado direito da temperatura (ou em bloco dedicado), o par de extremos diários aparece formatado de modo super legível:
  * Opção A (Empilhada Apple-style):
    * `Máx 28°` (cor levemente aquecida / âmbar)
    * `Min 21°` (cor levemente fria / azul)
  * Opção B (Linha única compacta): `Máx 28° · Min 21°` em 12sp logo abaixo de `Joinville`.

### Mockup 2: Contexto Completo na Home (1280×720)

```
 00:00                     [ Paisagens ]  [ Ajustes ]  [ Standby ]
 Terça-feira, 24/11/2026           Wi-Fi Conectado  •  Áudio 100%

                         ┌──────────────────────────────────────┐
                         │  [3D]   JOINVILLE  •  AGORA          │
                         │ (ICON)                               │
                         │         21°C   ▲ 28°  ▼ 21°          │
                         │  Garoa         Open-Meteo            │
                         └──────────────────────────────────────┘
 PHILCO / ULTRA 777
┌──────────────────────────────────────────────────────────────────────┐
│ [ SmartTube ] [ AR ] [ Cabo ] [ AV ] [ HDMI 1 ] [ HDMI 2 ] [ Apps ]  │
└──────────────────────────────────────────────────────────────────────┘
```

---

## 5. Plano de Implementação para o Codex (Zero Regressão)

1. **Alteração em `MainActivity.java` (`createWeatherCard`):**
   * Ajustar largura da `conditionPanel` para `dp(116)` fixa (em vez de `weight = 1f`).
   * Reduzir ícone de clima de `72×72dp` para `64×64dp` para ganho de proporção e respiração do texto da condição.
   * Ajustar `currentPanel` para preencher o espaço restante (`width = 0, weight = 1f` ou `dp(214)`).
   * Aumentar `temperatureView` de `37sp` para `42sp` (`dp(48)` de altura).
   * Formatar linha de Máx/Min com maior corpo (`12sp`) e alinhamento próximo à temperatura.
2. **Compatibilidade API 19 / Memória:**
   * Nenhum objeto pesado alocado em runtime.
   * Aproveitamento dos TextViews e Layouts nativos existentes.
   * Preservação do binding de D-pad, clique para refresh manual e long-click para troca de cidade.

---

## 6. Feedback Solicitado ao Usuário
- O alinhamento dos extremos térmicos agrada mais no formato empilhado Apple (`▲ Máx 28°` / `▼ Min 21°`) ou no formato em linha única (`Máx 28° · Min 21°`)?
- A temperatura em `42sp` com o ícone em `64dp` atende perfeitamente ao peso visual desejado?
