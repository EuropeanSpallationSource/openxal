# CKEditor 5 Markdown editor

Markdown editor of the Olog post entry dialog. `OlogPostEntryController` inlines `build/ckeditor.js` between
`ckeditor_top.html` and `ckeditor_bottom.html` and loads the page in a JavaFX `WebView`. If the build is missing, the
dialog falls back to a plain text editor with a preview.

The editor uses the open-source `ckeditor5` package under the GPL (`licenseKey: 'GPL'`).

## Building

```
npm install
npm run build
```

The bundle is written to `build/`, which is not under version control; CI builds it before running Maven.
Open `ckeditor.html` in a browser to try the editor outside the application.

The plugins and toolbar are configured in `src/ckeditor.js`.
