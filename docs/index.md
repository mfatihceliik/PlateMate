---
type: backend-doc
area: docs-index
updated: 2026-05-29
---

# Documentation Index

## Routing

Read `AGENTS.md` first, then use this matrix to select the smallest useful documentation set.

**Read order:** `AGENTS.md` → this file → task-specific docs.
**Changelog reading rules:**
- Normal tasklarda: Sadece `docs/changelog.md` içindeki **Recent Changes Summary** bölümünü oku.
- Behavior-changing (API, database, security) tasklarda: İlgili detay entry'ye bak.
- Büyük refactorlarda: Gerekirse `docs/changelog-archive.md` dosyasını oku.

## Endpoint & Service Reading Rules

Yeni bir endpoint eklerken, controller veya service oluştururken/güncellerkeren mutlaka **[[conventions]]** dosyası içindeki **New Endpoint Checklist** bölümünü okuyun ve adım adım takip edin.

## Dependency Graph Reading Rules

Eğer aşağıdaki işlemlerden birini yapıyorsanız, **[[services-business]]** dosyasındaki **Cross-Manager Dependencies** grafiğini (tablosunu) mutlaka inceleyin:
* Manager sınıflarını refactor etme (bölme/birleştirme vb.)
* Mevcut bir Service methodunun imzasını veya dönüş tipini değiştirme
* Notification, Chat, Friendship veya Plate özelliklerinde (domain/business layer) değişiklik yapma

## Package Scale & Architecture Reference

Öncelikle full-scan veya gereksiz derinlikte grep/find komutları çalıştırmak yerine, projenin büyüklüğünü ve genel yapısını anlamak için **[[architecture]]** dosyasındaki **Package Scale** tablosunu referans alın. Dosya sayıları source tree ile doğrulanmıştır.

## Task-to-Docs Matrix

| Task type                         | Docs to read                                                                 |
| --------------------------------- | ---------------------------------------------------------------------------- |
| Auth / security / JWT             | [[security]], [[api-contracts]], [[services-business]]                       |
| Controller / API endpoint         | [[controllers]], [[api-contracts]], [[services-business]]                    |
| Business / manager logic          | [[services-business]], [[data-access]], [[entities]]                         |
| Database / entity / migration     | [[database]], [[entities]], [[data-access]]                                  |
| Chat / socket                     | [[socket-chat]], [[services-business]], [[security]]                         |
| FCM / notification                | [[notifications-fcm]], [[services-business]], [[socket-chat]]                |
| Moderation / compliance           | [[moderation-compliance]], [[database]], [[entities]], [[services-business]] |
| Testing                           | [[testing]] + feature-specific docs                                          |
| Config / infrastructure           | [[configuration-infrastructure]], [[security]], [[database]]                 |
| Client contract sync              | [[api-contracts]], [[controllers]], [[entities]]                             |
| Utility / wrapper / exception     | [[core-utilities]], [[conventions]]                                          |
| Architecture / refactor / cleanup | [[architecture]], [[conventions]], [[known-violations]]                      |
| Naming / pattern question         | [[conventions]]                                                              |
| Bug fix / targeted fix            | feature-specific docs + [[known-violations]]                                 |

**Always read** `[[known-violations]]` when the task touches a file listed there.

## Agent Workflow

For detailed rules on context budget, source inspection, and completion checklists, refer to **[[agent-workflow]]**.

## Doc Catalog

| Doc                              | Focus                                                                |
| -------------------------------- | -------------------------------------------------------------------- |
| [[architecture]]                 | Layers, packages, request flow, dependency graph, package scale      |
| [[agent-workflow]]               | Context budget, source inspection strategy, completion checklists    |
| [[conventions]]                  | Naming, patterns, response style, templates, build/run               |
| [[controllers]]                  | Controller architecture and access rules                             |
| [[api-contracts]]                | Endpoint tables, request/response DTOs, status codes                 |
| [[services-business]]            | Manager workflows, business rules, cross-manager dependencies        |
| [[data-access]]                  | Repositories, query patterns, persistence conventions                |
| [[entities]]                     | JPA entities, DTOs, validation, lookup models                        |
| [[database]]                     | Migrations, schema, indexes, constraints, Docker DB                  |
| [[security]]                     | JWT, auth flow, authorization rules, token refresh                   |
| [[core-utilities]]               | Result wrappers, pagination, mappers, messages, exception handler    |
| [[socket-chat]]                  | Socket.io architecture, events, chat handler flow                    |
| [[notifications-fcm]]            | FCM tokens, push delivery, notification triggers                     |
| [[moderation-compliance]]        | Review moderation, comment reports, removal requests, retention      |
| [[configuration-infrastructure]] | Config classes, properties, seed runners, Docker                     |
| [[testing]]                      | Test structure, styles, commands, coverage gaps                      |
| [[known-violations]]             | Tracked architecture violations and technical debt                   |
| [[changelog]]                    | Change history (read summary section first)                          |

## Notes

* If docs and source conflict, source is truth.
* Do not recreate broad architecture audit files for every small issue.
* Track cleanup work in `[[known-violations]]` or the relevant feature doc.
