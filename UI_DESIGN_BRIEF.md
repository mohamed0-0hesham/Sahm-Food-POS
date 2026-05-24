# Sahm Food POS — UI Design Brief

A handoff document for designing the visual UI of the Sahm Food POS app. This brief describes **what** each screen needs to do and **why**, not how it currently looks — the existing implementation is a baseline you can replace.

---

## 1. Product overview

**Sahm Food POS** is a point-of-sale app for a food shop. A cashier uses it to:

1. Browse the product catalog
2. Build an order by tapping products into a cart
3. Apply a discount if needed
4. Take payment (cash, card, or other)
5. Print or save a receipt
6. Review past orders and re-print receipts
7. Manage store settings (name, tax rate, currency, printer)

The app must feel like a **modern eCommerce app** — premium, fast, confident — even though it's used by staff rather than customers. Think Wolt / Talabat / Square / Stripe Dashboard rather than a legacy POS terminal.

---

## 2. Users & context

| Trait | Detail |
|---|---|
| **Primary user** | Shop cashier or counter staff |
| **Skill level** | Non-technical; learns by repetition |
| **Speed** | Fast-paced — every tap during a rush matters |
| **Environment** | Counter, often with one hand busy. Bright lighting, sometimes glare. |
| **Session length** | Long (whole shift). UI should not fatigue. |
| **Secondary user** | Shop owner / manager — reviews orders, edits settings, less frequent |

**Design implications:**
- Big touch targets (≥44pt). Generous spacing.
- High contrast text for glare conditions.
- Primary action on every screen is obvious and reachable with the thumb.
- Avoid clever-but-subtle micro-copy; cashiers should not have to think.
- Confirmation patterns (sheets, dialogs) for destructive or financial actions.

---

## 3. Devices & form factors

| Form factor | Priority | Notes |
|---|---|---|
| **Android tablet (10")** | Primary | Landscape. Catalog + persistent cart side-panel. |
| **iOS iPad** | Primary | Same as Android tablet. |
| **Android phone** | Secondary | Portrait. Cart is its own screen reached from a floating bar. |
| **iOS iPhone** | Secondary | Same as Android phone. |

Layout breakpoint: **width ≥ 600dp ⇒ tablet layout**.

The visual language should be **identical across iOS and Android** — this is one product, not a Material vs Cupertino split.

---

## 4. Brand direction

The brand we're targeting is **minimal monochrome + one bold accent**, in the spirit of Aritzia, Acne Studios, Linear, Vercel, Shopify Polaris.

| Token | Direction |
|---|---|
| **Surfaces** | Near-white / near-black neutrals. Tonal greys for cards. No drop shadows; use thin borders. |
| **Type** | Modern sans-serif (system stack OK). Negative tracking on display sizes. Medium-weight body text. Uppercase eyebrow labels for sections. |
| **Accent** | A single saturated hue. Current pick is warm orange `#FF5A1F` (food-friendly, distinctive). Open to alternatives — must be confident, distinctive, and age well. |
| **Corners** | Soft but not childish. 8/12/16/24 progression. Pill (999) for chips and toggles. |
| **Motion** | Tasteful micro-interactions: press scale on cards (0.97x), animated colors on selections, vertical-slide on count flips, crossfades between modes, slide-up modal sheets. No parallax, no hero-image flights, no spring physics on everything. |
| **Imagery** | Real product photography (square crops in the grid). Shimmer placeholder during load. |
| **Iconography** | Minimal. Currently uses Unicode glyphs (`◧ ▤ ◉ ✓ ⌕ ✕ ‹ ›`). Open to a custom monoline icon set if it stays restrained. |

**Reserve the accent.** Use it for primary buttons, prices, focus states, and badges. Don't paint the chrome with it. Selected states use **filled-ink** (label-primary fill on neutral background), keeping the accent for true CTAs.

**Light mode is the priority.** Dark mode must exist and feel native — not just inverted.

---

## 5. Information architecture

```
Splash  →  Auth  →  Tab shell
                    ├── Shop    (Catalog → Product detail → Cart → Checkout → Receipt)
                    ├── Orders  (Order history → Order detail → Receipt)
                    └── Account (Settings → About)

Modals (any tab):
  • Sync status sheet
  • Product detail sheet
  • Discount sheet
  • Checkout sheet
  • Receipt sheet
```

Navigation model:
- **3 tabs** with independent back stacks.
- **Modal sheets** slide up from the bottom (full-width on phone, centered-ish on tablet).
- **Push** for drill-down (order detail, about, phone-only cart).

---

## 6. Screen-by-screen brief

Each section below tells you: **purpose**, **what the user sees**, **interactions**, **key states**, and **edge cases**.

---

### 6.1 Splash

**Purpose:** Bridge launch → auth resolution. Reassures the user the app is alive while we figure out if they're signed in.

**Content:**
- App brand mark (currently a rotating monogram tile)
- Wordmark "Sahm Food"
- Optional micro-caption ("POINT OF SALE")

**Interactions:** None. Auto-dismisses in <1s.

**States:** Single state.

**Edge cases:** Cold launch can take a couple of seconds on low-end devices — design must hold attention for up to ~2s without feeling stuck.

---

### 6.2 Auth

**Purpose:** Sign the cashier (or shop owner) into the device.

**Content:**
- Brand tile (small, top-left)
- Headline + subhead (changes between Sign In and Sign Up)
- Mode toggle: Sign In ↔ Sign Up
- **Sign Up extras:** Display name field (animated reveal)
- Email field (labeled, with placeholder `you@store.com`)
- Password field (labeled, masked, placeholder `••••••••`)
- Inline error pill (slides in when relevant — wrong password, validation, etc.)
- Primary button: "Sign In" / "Create Account"
- Divider: "or continue with"
- "Continue with Google" button (always shown when available; uses the official Google G icon on a white surface)
- "Continue with Apple" button (iOS only; uses Apple's typography conventions — black surface, Apple logo glyph)

**Interactions:**
- Toggling modes crossfades the headline and animates the display-name slot in/out.
- Tapping a social button kicks off the platform OAuth flow (currently stubbed).
- Tapping the primary CTA submits.

**States:**
- Idle
- Submitting (primary button shows spinner)
- Error (inline pill)
- Social-in-flight (social buttons disabled, others remain interactive)

**Edge cases:**
- Email validation is loose — backend is authoritative.
- "Forgot password" is **not** in scope yet; design with room for it as a future link below the password field.

---

### 6.3 Root shell (tab bar)

**Purpose:** The persistent chrome around the three top-level destinations.

**Content:**
- Three tabs: **Shop** (catalog), **Orders** (history), **Account** (settings).
- Each tab shows its icon + label.
- Orders tab has an optional **count badge** (number of pending sync operations).
- Active tab has a soft pill highlight behind the icon, label switches to primary color.
- A thin top border separates the tab bar from content.

**Interactions:**
- Tapping a tab swaps the content area with a quick crossfade.
- Tapping the active tab pops to root within that tab.

**Behavior:**
- Tab bar is hidden when a modal sheet is up? **No.** Sheets cover it from the bottom.
- Tab bar respects system navigation insets (Android gesture bar, iOS home indicator).

---

### 6.4 Shop / Catalog

**Purpose:** Browse and tap products into the cart. The most-used screen.

**Layout:**

**Tablet (≥600dp):**
- Left column: catalog (header, search, categories, product grid)
- Right column: persistent cart side-panel (~360dp wide)

**Phone:**
- Single column catalog
- Floating cart bar slides up from the bottom once items are added

**Content (catalog column):**
- Hero header: large title "Discover" + subtitle "Today's catalog"
- Trailing icon chip for sync status (opens sync sheet)
- Search field (full-width, focus-animated border)
- Horizontal **category strip** — pills, scrollable. First pill is "All". Selected pill inverts to filled-ink.
- **Product grid** — 2 columns on phone, 4 on tablet
  - Each card: square image (16:9 acceptable too), product name (1 line), category eyebrow (optional), price in accent color
  - Cards have a 1px border + tonal surface; press-scale animation on tap
  - **Quantity badge** in the top-right corner when the item is in the cart — a small dark circle with the count in label-inverted color
- **Floating cart bar** (phone only): full-width pill at the bottom, dark background. Left: accent circle with item count. Center: "View order". Right: total + chevron.

**States:**
- Loading (first launch, before remote refresh completes) — show the grid empty, the shimmer of `ProductThumbnail` covers individual cells.
- Empty catalog (no products + offline) — empty state with `▢` glyph, "Catalog is empty", and a hint about checking the connection.
- Empty search — `⌕` glyph, "No matches", "Try a different search term".
- Empty category — `▦` glyph, "Nothing in this category".

**Interactions:**
- Tap product → adds 1 to cart, brief acknowledgement (subtle).
- Long-press product → opens **Product Detail** sheet.
- Tap category → filters grid (animated).
- Tap search → typing filters instantly.
- Tap sync chip → opens **Sync Status** sheet.
- Tap floating cart bar (phone) → pushes **Cart** screen.

---

### 6.5 Product Detail sheet

**Purpose:** Add a specific product with a chosen quantity, view description.

**Layout:** Modal sheet, slides up from the bottom. Contains:

**Content:**
- Drag handle (40×4 pill at top center)
- Hero image (16:11, rounded)
- Category eyebrow (uppercase label)
- Product name (title style)
- Description (body, 2-3 lines)
- Row: "Unit price" caption + price (title) on the left, **Quantity stepper** on the right
- Total card (tonal surface): "Total" label + computed total in accent color (`unit price × quantity`)
- Primary button: "Add to order"
- Tertiary: "Cancel"

**States:** Idle / adding.

**Interactions:**
- Stepper +/− adjusts quantity (count flips with vertical-slide animation).
- "Add to order" dismisses the sheet and adds N × product to cart.

---

### 6.6 Cart (phone-only screen + tablet side-panel)

**Purpose:** Review the current order, adjust quantities, apply discount, proceed to checkout.

**Layout:**
- Phone: full screen, top nav bar (back chip + "Your Bag" + item count + "Clear" link)
- Tablet: side-panel embedded in catalog; uses a similar header but no back affordance

**Content:**
- **Line items** — vertical list of cart rows. Each row: small product thumbnail (56px), name + "unit price · subtotal" subline, quantity stepper.
- **Totals card** (tonal surface): subtotal → tax → optional discount (in success green) → total.
- "Apply discount" tertiary button (above the totals card, plus-prefixed).
- Primary: "Proceed to checkout" (full-width).

**States:**
- Empty: `▢` glyph empty state "Your bag is empty / Tap a product in the catalog to start a new order".

**Edge cases:**
- "Clear" button only appears when items > 0.
- If discount > subtotal, show 0.00 and clamp — don't display negative totals.

---

### 6.7 Discount sheet

**Purpose:** Apply a manual discount to the current order.

**Content:**
- Drag handle
- Title "Apply a discount"
- Subtitle "Subtracted from the order before payment."
- Amount field (decimal keyboard, labeled, placeholder `0.00`)
- Reason field (text, labeled, placeholder `e.g. Staff discount`, optional)
- Primary: "Apply discount"
- Tertiary: "Cancel"

**Interactions:**
- Amount accepts digits and one decimal.
- Empty reason is fine.

---

### 6.8 Checkout sheet

**Purpose:** Confirm the order and take payment.

**Content:**
- Drag handle
- Title "Checkout" + subtitle "Confirm and collect payment"
- Close chip (top right)
- **Summary card** (tonal): `N ITEMS` eyebrow → subtotal → tax → optional discount → divider → "Total due" + total (accent, title size).
- "PAYMENT METHOD" label
- **Segmented control**: Cash · Card · Other
- **Method panel** (swaps with crossfade):
  - **Cash:** "Amount received" field (large numeric); "Change due" success-tinted chip below.
  - **Card:** Placeholder panel "Card terminal ready".
  - **Other:** Placeholder panel "Recorded as 'Other' — note will be saved with the order".
- Inline error pill (when validation fails — e.g. cash received < total).
- Primary: "Complete & print receipt" (shows "Processing…" with spinner when in flight).
- Tertiary: "Cancel".

**States:**
- Idle / processing / error.

---

### 6.9 Receipt sheet

**Purpose:** Confirm the order is done; show the printed receipt content; offer reprint.

**Content:**
- Drag handle
- **Status halo** — large circle (72px), tinted background, glyph inside:
  - Success (green halo, `✓`) when the printer succeeded.
  - Warning (amber halo, `⚠`) when the printer was offline and we saved locally.
- Headline: "Order complete" / "Saved locally"
- Subtitle: "Receipt printed successfully." / "Printer offline — receipt saved for later."
- **Receipt paper** — a cream-tinted rounded panel containing the receipt text in a monospace font. Scrollable.
- Row of actions: "Reprint" (tertiary, ⅓ width) + "Done" (primary, ⅔ width).

**Interactions:**
- "Done" dismisses; if this receipt closes a checkout (Flow A), the app also returns to the Shop tab at root.
- "Reprint" runs the printer again.

---

### 6.10 Orders (history)

**Purpose:** Browse past orders, jump into details, retry failed syncs.

**Content:**
- Large title "Orders" + count subtitle ("12 total")
- Search field
- Filter pills: All · Synced · Pending · Failed
- List, grouped by date (header label per group)
- Each row:
  - Time tile (left, 48×48, tonal background, shows HH:MM)
  - "Order #ABC123" headline + sync status icon (small)
  - "5 items · Burger, Coke, Fries" subtitle (1 line, ellipsis)
  - Total (right, headline)

**States:**
- Empty: `▤` glyph "No orders yet" / "Completed orders will appear here as you ring them up."

---

### 6.11 Order detail

**Purpose:** Inspect a single order; reprint or retry sync.

**Content:**
- Top bar with back chip + "Order #ABC123"
- Sections (each: uppercase label + a `PosCard`):
  - **Status** — sync status icon + status label
  - **Items** — each row has a `×N` tile + product name + per-unit price + line total. Separator between rows.
  - **Totals** — subtotal / tax / (discount, success-tinted) / divider / **Total** (accent, title)
  - **Payment** (when present) — method / received / change
- Inline error text when actions fail
- "Reprint receipt" secondary button
- "Retry sync" secondary button (only when status = failed)

---

### 6.12 Account / Settings

**Purpose:** Configure store details and hardware. Entry point to About.

**Content:**
- Header: large title "Account" + the current store name as subtitle
- Sections (each: uppercase label above a `PosCard`):
  - **Store** — Store name · Tax rate · Currency (rows, tap to edit)
  - **Hardware** — Printer (read-only "Mock Printer (Demo)") · Auto-print receipts toggle
  - **Sync** — Status (read-only)
  - **About** — Version · "About Sahm POS" (chevron, pushes About screen)

**Interactions:**
- Tapping an editable row opens a centered dialog.
- The toggle uses an animated pill switch (accent fill when on).

**Edit dialogs:**
- Store name — text field, "Save" disabled when blank
- Tax rate — decimal field constrained to 0–100, "Save" disabled when invalid
- Currency — picker (EGP, USD)

---

### 6.13 About

**Purpose:** Brand placeholder + version.

**Content:**
- Top bar (back chip + "About")
- Centered column:
  - Brand tile (80×80, accent background, "S" monogram)
  - "Sahm Food POS" (title)
  - "Version 1.0.0" (body, secondary)
  - "Built with Kotlin Multiplatform" (body, secondary)
  - "© 2026 Sahm Food" (footnote, tertiary)

---

### 6.14 Sync status sheet

**Purpose:** Show connection state and pending / failed sync operations; let the user retry.

**Content:**
- Drag handle + close chip
- Title "Sync" + subtitle showing connection status
- **Connection panel** — tinted by state (green when online, amber when offline). Status dot + "Online" / "Offline" + helper text ("Syncing in background" / "Will sync when reconnected").
- List of pending / failed operations. Each row:
  - Status indicator (amber dot for pending, `⚠` for failed)
  - "Order #ABC12345" headline
  - "Retries: 3 · last error message" subtitle
  - "Retry" tertiary button (failed only)

**States:**
- Empty: `✓` glyph "All up to date" / "There are no pending sync operations."

---

## 7. Core user flows

### 7.1 Standard sale (most common path)

1. Cashier opens app → Splash → (already signed in) → **Shop**.
2. Optional: tap category, search, or scroll to find product.
3. Tap product → added to cart (badge appears on card; floating cart bar slides up on phone).
4. Repeat for additional items.
5. Tap cart (floating bar on phone / side-panel on tablet) → **Cart**.
6. Optional: tap "Apply discount" → **Discount sheet** → enter amount → Apply.
7. Tap "Proceed to checkout" → **Checkout sheet**.
8. Pick payment method, enter cash amount (if cash).
9. Tap "Complete & print receipt" → **Receipt sheet**.
10. Tap "Done" → app returns to **Shop** for the next order.

### 7.2 Reprint receipt

1. **Orders** tab → tap an order → **Order detail**.
2. Tap "Reprint receipt" → **Receipt sheet** (no "completesCheckout"; "Done" just dismisses).

### 7.3 Retry failed sync

1. **Sync status sheet** (entered from the catalog header sync chip).
2. Find the failed operation → tap "Retry".
3. Status updates inline.

### 7.4 First-time sign-in

1. **Splash** → auth resolved as "not signed in" → **Auth**.
2. User chooses email or social.
3. On success, **Auth** dismisses and **Shop** appears (catalog may be empty briefly if the remote refresh hasn't landed yet).

---

## 8. Cross-cutting design requirements

### Empty / loading / error
Every list screen has all three. The current implementation uses a glyph-in-a-tile, headline, body, and (optional) action button — this pattern should remain visually consistent across the app.

### Accessibility
- Touch targets ≥ 44pt.
- Color contrast WCAG AA minimum on all text.
- Don't rely on color alone for state (sync status uses glyph + color).
- Type scale must remain legible at the system's largest accessibility setting (consider truncation rules).

### Localization
- All copy in this brief is English; the app may localize later. Plan for **+30% text expansion** in buttons and labels.
- Right-to-left (Arabic) is on the roadmap. Layouts should mirror cleanly.

### Pricing
- Always displayed with currency code (e.g. `EGP 125.50`).
- Use **tabular figures** so prices align in lists and totals.

### Data shown in the design
- Use realistic-looking products from DummyJSON (`https://dummyjson.com/products`) — they're the real demo data source.
- Realistic prices (EGP equivalent of the DummyJSON USD prices: roughly `price × 50`).
- Sample order IDs: 6-character hex suffixes (`#A1B2C3`).

---

## 9. What's out of scope (for now)

These are deliberately omitted; mention them in handoff so the designer knows not to design them yet:

- Multi-shift / multi-cashier handoff
- Inventory management
- Customer profiles / loyalty
- Refunds (UI doesn't exist yet)
- Reports & analytics
- Multi-store
- Hardware settings beyond printer (scales, scanners)
- Tipping flow

---

## 10. Reference: current visual tokens

These are what the app uses today. Treat them as a starting baseline — feel free to evolve them, but if you do, evolve the **whole system** rather than mixing old and new.

### Color (light)

| Role | Hex |
|---|---|
| Background primary | `#FFFFFF` |
| Background secondary | `#FAFAFA` |
| Background tertiary | `#F4F4F5` |
| Surface elevated | `#FFFFFF` |
| Label primary | `#09090B` |
| Label secondary | `#52525B` |
| Label tertiary | `#A1A1AA` |
| Label inverted | `#FAFAFA` |
| Separator | `#E4E4E7` |
| Separator strong | `#D4D4D8` |
| **Accent** | `#FF5A1F` |
| On-accent | `#FFFFFF` |
| Success | `#16A34A` |
| Warning | `#EAB308` |
| Destructive | `#DC2626` |

### Color (dark)
Same roles, inverted neutrals. Accent shifts to `#FF7A47` for better dark-mode contrast.

### Type scale
display 44 / largeTitle 32 / title1 26 / title2 22 / title3 18 / headline 16 / body 15 / callout 15 / subhead 14 / footnote 13 / caption1 12 / caption2 11 / mono 13 / label 11 (uppercase, +1.2 tracking).

### Shape
6 / 8 / 12 / 16 / 24 dp + pill (999) + sheet (top-only 24).

### Motion
Tween durations 120 / 220 / 360 ms with a single emphasized easing. Press scale 0.97x.

### Layout
4-pt base unit. Section paddings 20dp. Grid gaps 14dp. Sheet content padding 20dp.

---

## 11. Deliverables we'd love

If you're scoping a design pass, the most useful deliverables — in priority order — are:

1. **Catalog + Product Detail + Cart + Checkout + Receipt** (the sale flow) — these are 80% of the app's time-on-screen.
2. **Auth + Splash** — first impressions.
3. **Orders + Order Detail** — second-most-used screens.
4. **Settings + About + Sync sheet** — lower frequency, can iterate later.
5. **Design tokens documented as a single artifact** — colors, type, shapes, motion. Light + dark.
6. **Empty / loading / error states** — at least one per list screen.
7. **A tablet layout** of Shop showing the persistent cart side-panel.

---

*Questions? The codebase is at the same repo as this brief — flow logic lives in `presentation/<feature>/<Feature>ViewModel.kt`, and the current screens are in `presentation/<feature>/<Feature>Screen.kt`.*
