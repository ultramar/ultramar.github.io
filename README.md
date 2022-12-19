
## Font subsetting

`glyphhanger http://localhost:8080/ --spider-limit=0 --string --family='Source Serif 4 Subhead'`

```bash
pyftsubset resources/public/fonts/SourceSerif4Subhead-Regular.otf --output-file=resources/public/fonts/SourceSerif4Subhead-Regular-subset.woff2 --text-file=glout --layout-features+=onum --flavor=woff2
pyftsubset resources/public/fonts/SourceSerif4Subhead-It.otf --output-file=resources/public/fonts/SourceSerif4Subhead-It-subset.woff2 --text-file=glout --layout-features+=onum --flavor=woff2
pyftsubset resources/public/fonts/SourceSerif4Subhead-Semibold.otf --output-file=resources/public/fonts/SourceSerif4Subhead-Semibold-subset.woff2 --text-file=glout --layout-features+=onum --flavor=woff2
pyftsubset resources/public/fonts/SourceSerif4Subhead-SemiboldIt.otf --output-file=resources/public/fonts/SourceSerif4Subhead-SemiboldIt-subset.woff2 --text-file=glout --layout-features+=onum --flavor=woff2
```

```bash
pyftsubset resources/public/fonts/SourceSerif4Subhead-Regular.otf --output-file=resources/public/fonts/SourceSerif4Subhead-Regular-subset.woff --text-file=glout --layout-features+=onum --flavor=woff --with-zopfli
pyftsubset resources/public/fonts/SourceSerif4Subhead-It.otf --output-file=resources/public/fonts/SourceSerif4Subhead-It-subset.woff --text-file=glout --layout-features+=onum --flavor=woff --with-zopfli
pyftsubset resources/public/fonts/SourceSerif4Subhead-Semibold.otf --output-file=resources/public/fonts/SourceSerif4Subhead-Semibold-subset.woff --text-file=glout --layout-features+=onum --flavor=woff --with-zopfli
pyftsubset resources/public/fonts/SourceSerif4Subhead-SemiboldIt.otf --output-file=resources/public/fonts/SourceSerif4Subhead-SemiboldIt-subset.woff --text-file=glout --layout-features+=onum --flavor=woff --with-zopfli
```
