# Foresignal website

A standalone, responsive, multi-page marketing website concept for the Foresignal ecosystem. Built as ordinary HTML, CSS, JavaScript, and optimized WebP assets; no Node runtime, frontend framework, build step, JavaScript CDN dependency, or live backend connection is required. Google Fonts are optional; system font fallbacks are provided.

## Site architecture

| Page | File | Story |
|---|---|---|
| Overview | `index.html` | The engine and Admin are the primary products; unlimited independent clients |
| Engine | `engine.html` | The headless data, intelligence, history, forecasting and API foundation |
| Admin | `admin.html` | Native operator control plane and supplied Admin interface mockups |
| Client showcase | `clients.html` | The optional reference web client, with filterable designs and full-size previews |
| Intelligence | `intelligence.html` | Correlation, rolling hypotheses, optional AI, forecasting and accountability |
| Automated Newspaper | `newspaper.html` | Personalized, cited written and spoken editions, news translation and delivery |
| Developers | `developers.html` | Three-repo separation, API surfaces and a development quickstart |

## Run it locally

Open `index.html` in a modern browser, or serve the whole folder locally:

```sh
cd foresignal-website
python3 -m http.server 8000
```

Then visit `http://localhost:8000`.

For production, serve this folder as static files from the site or web server of your choice. The marketing website is independent of the headless `foresignal` service and the `foresignal-admin` native app. It does **not** become another runtime component of the intelligence platform.

## The three repositories

The website consistently distinguishes:

- **`foresignal`** — the headless intelligence backend (primary offering)
- **`foresignal-admin`** — the separate native Flutter control plane (primary offering)
- **`foresignal-client`** — a sample React/Cesium web visualization (optional example)

To set the correct GitLab URLs, edit `assets/js/site-config.js`:

```js
window.FORESIGNAL_SITE = {
  repositories: {
    engine: 'https://gitlab.com/adam.allard/foresignal',
    admin: '', // Set the confirmed Foresignal Admin repository URL
    client: '' // Set the confirmed reference client repository URL
  }
};
```

Only the engine URL was present in the supplied charter, so the Admin and Client URL fields are intentionally blank rather than invented. Repo cards show a copy-name action by default and add an "Open repo" link when a valid HTTPS URL is configured.

## Interaction and design notes

- Mobile menu navigation, client gallery filtering, image lightboxes, Escape-key dismissal, repository links and copy-command controls all use vanilla JavaScript.
- Any installed instances, live telemetry, alert counts, or system metrics are **not** faked; all screenshots are supplied interface concepts or generated illustrative hero art.
- The homepage uses a cropped illustration from the supplied/generated Foresignal infographic, with accessible HTML headline and call-to-action buttons. A "View full infographic" control opens the original design.
- Screenshots supplied by the user were converted to optimized WebP while preserving originals outside this site package.
- The interface mockups are presented as visual concepts; they are not evidence of a verified live deployment.
- Key facts used in copy come from the supplied Foresignal Product Charter, Executive Summary, Admin Charter and Client Charter. Deployment verification for the engine remains a pending owner task in the supplied documentation.
- Accessibility: semantic headings, keyboard-navigable menu and lightbox, descriptive image alt text, visible focus rings, reduced-motion handling, and responsive layouts.

## Structure

```text
foresignal-website/
  index.html
  engine.html
  admin.html
  clients.html
  intelligence.html
  developers.html
  assets/
    css/styles.css
    js/site-config.js
    js/site.js
    images/*.webp
    favicon.svg
  README.md
```

## Suggested launch review

Before public launch, confirm the two remaining repo URLs, add any production documentation links, verify current feature/release claims, and test in a normal browser at desktop and mobile widths. No contact form, authentication, or analytics service has been invented for this static concept.

## Automated Newspaper (new page)

`newspaper.html` is a static marketing design for the capabilities described in the supplied `Automated-Newspaper.md` (backend API version 2.1.1). Its illustrated newspaper is **a mockup, not a live edition**, and contains no real news headlines or invented performance numbers.

This is a capability of the existing `foresignal` backend, configured in `foresignal-admin`, and consumable by any client. It is **not a fourth repository or independently deployed service**. The source docs specify metadata-only news articles (titles and excerpts, not full bodies), a deterministic digest fallback when LLM writing fails, citation verification and optional provenance/corroboration. Operator configuration and user-owned edition self-service have distinct permissions.
