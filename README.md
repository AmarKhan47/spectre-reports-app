# Spectre Reports – Android app (WebView wrapper)

A small Kotlin app that opens your Django reporting portal full-screen with no URL bar,
no browser UI and no website hints. Push notifications are intentionally left out of v1.

You do **not** need Android Studio or the Android SDK on your PC. GitHub builds the APK for you.

## 1. Configure

Edit `gradle.properties`:

| Key | Meaning |
|---|---|
| `PORTAL_URL` | Full **https** URL the app opens first, e.g. `https://reports.yourdomain.com/` |
| `APP_NAME` | Name under the launcher icon and in dialogs |
| `APP_ID` | Unique package id (can't change after Play Store release) |

Optional: brand colours in `app/src/main/res/values/colors.xml`. The icon is a vector in
`app/src/main/res/drawable/` (`ic_launcher_background.xml`, `ic_launcher_foreground.xml`,
`ic_launcher_monochrome.xml` for Android 13+ themed icons).

## 2. Build in the cloud

1. Create a **private** GitHub repo and push this folder to the `main` branch.
2. Open the repo's **Actions** tab. The *Build Android app* workflow runs on every push.
3. When it finishes, open the run and download the **app-release** artifact. It contains the `.apk` (and a `.aab`).
4. Copy the APK to your phone and install it (allow "install unknown apps" once).

The first build is signed with a debug key, which is fine for testing.

## 3. Set up proper signing (do this before giving the app to clients)

Android only lets an app update over itself if every version is signed with the **same** key.

1. Actions tab → **Create signing keystore (run once)** → *Run workflow*.
2. Download the **keystore-secrets** artifact. It contains `KEYSTORE_BASE64.txt` and `credentials.txt`.
3. Repo → Settings → Secrets and variables → Actions → add four secrets:
   `KEYSTORE_BASE64` (contents of the txt file), `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD` (from `credentials.txt`).
4. **Back up both files somewhere safe, then delete the artifact.** If you lose the key you can never update the app.
5. Re-run *Build Android app*. Builds are now properly signed, and the version code goes up automatically.

For the Play Store, upload the signed `.aab`.

## 4. Django side

Your portal sees the app's User-Agent containing `SpectreApp`. Hide web-only elements for it:

```python
# context_processors.py  (add to TEMPLATES -> OPTIONS -> context_processors)
def is_app(request):
    return {"is_app": "SpectreApp" in request.META.get("HTTP_USER_AGENT", "")}
```

```django
{% if not is_app %}  <!-- footer links, "download our app" banners, etc. -->  {% endif %}
```

Also worth doing:
- Make 404/500 pages and the login page look like part of the app.
- Use `table-responsive` or card layouts for wide report tables.
- Set `SESSION_COOKIE_SECURE`, `CSRF_COOKIE_SECURE`, `CSRF_TRUSTED_ORIGINS`, `SECURE_SSL_REDIRECT`, and a sensible `SESSION_COOKIE_AGE`.
- Serve downloads (PDF/Excel) from real URLs with `Content-Disposition: attachment`.

## What the app does

- Locks navigation to your domain (subdomains allowed). Other links open in the phone's browser; `tel:`, `mailto:`, `sms:`, `whatsapp:` and `geo:` open their apps.
- Branded offline screen with a retry button, so the failed URL never shows.
- JS `alert`/`confirm` dialogs are replaced with ones titled with your app name, since the default shows the site address.
- Long-press link and image menus are disabled. Zoom is off.
- Splash screen, swipe-to-refresh, progress bar, back-button navigation, state kept across rotation.
- File uploads (`<input type="file">`) and downloads via the system Download Manager.
- HTTPS only. Minimum Android 10 (API 29).

## Limits to know about

- `blob:` and `data:` downloads (files generated in the browser by JavaScript) aren't supported. Serve files from a server URL.
- The portal address can still be found by decompiling the APK or sniffing traffic. Keep all security on the server.
- Google Play may reject apps that are only a website in a frame. Adding a few native features later (push notifications, biometric lock) helps. Sideloading APKs to clients has no such rule.
