# HyperPOS Mobile

Android companion app for HyperPOS Mobile API v1.

## Locked scope

- Kotlin Android client only.
- Laravel remains source of truth for auth, role, permissions, products, stock, supplier invoices, payment proof attachments, and audit.
- XML and ViewBinding UI.
- OkHttp only for HTTP.
- Custom encrypted token storage from v1.

## Package boundary

- id.hyperpos.mobile.domain
- id.hyperpos.mobile.application
- id.hyperpos.mobile.application.ports
- id.hyperpos.mobile.adapters.http
- id.hyperpos.mobile.adapters.storage
- id.hyperpos.mobile.adapters.file
- id.hyperpos.mobile.features.login
- id.hyperpos.mobile.features.cashierproductsearch
- id.hyperpos.mobile.features.admininvoices
- id.hyperpos.mobile.features.paymentproofupload
- id.hyperpos.mobile.shared

## Build

Run from this directory:

./gradlew assembleDebug
