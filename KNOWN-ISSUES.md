# Known Issues — SKB-Music

Priority: 🔴 critical · 🟠 high · 🟡 medium · 🟢 low

---

## 🟠 KI-M01 · Empty folders (domain, library, playlist, lyrics)

**Reality:** Folders exist in package structure but contain
no files. Likely planned-and-abandoned.

**Impact:** Confusing structure. New devs guess.

**Fix:** Either populate with real modules OR delete.

---

## 🟠 KI-M02 · Empty `docs/` folder

**Reality:** `docs/` exists at root, empty.

**Fix:** Either fill with deep-dives OR delete.

---

## 🟠 KI-M03 · No README / CHANGELOG (before this commit)

**Reality:** Repo had neither.

**Fix:** ✅ README + CHANGELOG added in this release.

---

## 🟡 KI-M04 · ReplayGain reflection fragility

**Reality:** Session ID accessed via reflection (commit f20889a).

**Impact:** Could break on Media3 upgrade.

**Fix:** Watch Media3 changelog; migrate when API exists.

---

## 🟡 KI-M05 · No DI framework

**Reality:** Manual wiring (ViewModels, Repository).

**Impact:** As modules grow, wiring gets messy.

**Fix:** Hilt or Koin in v3.0.

---

## 🟡 KI-M06 · No tests

**Reality:** Zero automated tests.

**Impact:** Refactor risk.

**Fix:** Start with Repository + ViewModel unit tests.

---

## 🟢 KI-M07 · No LICENSE

**Fix:** Add Apache-2.0 or MIT.

---

## ✅ RESOLVED — Security Incident 2026-09-16

### 🚨 KI-M00 · GitHub PAT leaked in git remote URL

**Reality:** `.git/config` had:
`https://ghp_***@github.com/skbsakib500/SKB-Music.git`

**Impact:** Token visible in any terminal output / screen log.

**Timeline:**
- 2026-09-16: Detected during recon
- Fixed local remote URL → `https://github.com/...`
- Verified `.git/config` was NEVER committed
- PAT manually revoked on GitHub → verified dead (HTTP 401)

**Post-mortem:**
- Never embed credentials in remote URLs
- Use `gh auth login` (OAuth) instead of classic PAT
- `.gitignore` should always include `.git/config` backup patterns

**Status:** ✅ Resolved · 2026-09-16
