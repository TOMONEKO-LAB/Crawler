# Java Crawler

This is the crawler written by Java.  
This crawler is used jsoup.

## Functions

### Download
- Fetch `HTML` from a specified URL
- Download linked `CSS` files
- Download linked `JavaScript` files
- Download image resources referenced by `<img>` tags
- Download icon files referenced by `<link rel="icon">`
- Rewrite resource paths in `HTML` to local files

### Settings
- Configure download buffer size
- Configure default file extension
- Configure default character encoding
- Enable or disable debug logging

## Dependencies

- **Java**
- **jsoup**

## Notes / Limitations

- This crawler is intended for **educational purposes only**.
- It does **not**:

  - Respect `robots.txt`
  - Handle CSS-internal resources (`url()`, `@import`, fonts)
  - Support advanced HTML elements such as `<video>`, `<picture>`, or `<source>`
- Crawling commercial websites may violate their terms of service.

## Disclaimer

Use this crawler only on websites you own or have permission to access.
The author assumes no responsibility for misuse.
