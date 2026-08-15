# Sist Design Tokens — Faz 1

Onaylanan tasarım yönü: **cesur kimlik** — derin orman yeşili + altın vurgu + hero yüzeyi + tabular rakamlar.
Kapsam: yalnızca görsel katman (`ui/theme/*`, `presentation/**` Composable'ları).
Dokunulmayanlar: `domain/`, `data/`, worker'lar, navigasyon hedefleri, ViewModel sözleşmeleri.

## Renk

- **Seed / primary:** Derin orman yeşili `#00634D` (light), `#5BDBAF` (dark).
- **Tertiary / imza vurgu:** Altın `#7A5C0C` (light), `#EBC15F` (dark).
- **Hero yüzey:** Her iki temada derin yeşil-siyah `#06281C` — ana ekran başlık paneli ve vurgulu kartlar için.
- **Dynamic color:** Android 12+ seçeneği `SistTheme(dynamicColor = true)` ile korunur; varsayılan kapalı, marka paleti kullanılır.
- **Finansal anlam renkleri (tek kaynak: `SistColors.kt`):**

| Token | Light | Dark |
|---|---|---|
| `positive` (kâr/gelir) | `#0E7A55` | `#4EDC9D` |
| `positiveContainer` | `#DDF5E9` | `#0C3A28` |
| `negative` (zarar/gider) | `#C43B3B` | `#FF7B7B` |
| `negativeContainer` | `#FBE6E6` | `#431A1A` |
| `warning` | `#B45309` | `#FBBF24` |
| `gold` / `goldContainer` | `#A97C0B` / `#FFF0CD` | `#E8C15C` / `#46370A` |

- Kategori vurguları (sarı/mavi/mor/turuncu) `SistColors.category*` altında tek yerde.
- Erişim: `LocalSistColors.current` (SistTheme tarafından sağlanır). `IncomeGreen` vb. legacy isimler alias olarak durur, yeni kod bunları kullanmaz.
- Kural: presentation katmanında hardcoded `Color(0x…)` yasak.

## Tipografi

- M3 type scale korunur; `titleLarge` SemiBold (ekran başlığı kimliği).
- `SistTypography`: tutarlar için **tabular rakam** (`tnum`) zorunlu.

| Rol | Stil |
|---|---|
| Ana bakiye / hero tutar | `amountDisplay` (36sp Bold, tnum) |
| Kart içi tutar | `amountHeadline` (24sp SemiBold, tnum) |
| Liste satırı tutarı | `amountTitle` (16sp SemiBold, tnum) |
| Küçük tutar | `amountSmall` (13sp Medium, tnum) |
| Bölüm etiketi | `sectionLabel` (12sp SemiBold, ls 0.6) |
| İstatistik etiketi | `statLabel` (11sp Medium, ls 0.8) |

## Spacing / Shape

- `SistDimens`: xxs 4, xs 8, s 12, m 16, l 24, xl 32, xxl 40 (dp).
- `SistRadius`: sm 8, md 12, lg 16, xl 24, pill 28 (dp).

## Motion

- `SistMotion`: quick 150ms, standard 250ms, emphasized 350ms, page 300ms.
- Easing: `standardEasing` (FastOutSlowIn), `emphasizedEasing` (EmphasizedDecelerate).

## İkonografi

- Material Icons (filled) tek set; `IconMapper` kategori ikon+renk eşlemesinin tek kaynağıdır (tüm ekranlar, widget, bildirimler).

## Bileşenler

- Paylaşılan: `SistTopBar`, `SummaryCards`, `TransactionItem`, `EmptyState` (zenginleştirilmiş: ikon+başlık+açıklama+CTA).
- `AssetDetailScreen` içindeki özel `TransactionItem` kopyası ortak bileşene birleştirilir.
- Boş durumlar yönlendirici CTA içerir ("Henüz işlem yok — İlk işlemini ekle" vb.).
