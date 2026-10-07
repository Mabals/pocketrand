# PocketRand

**An AI-powered personal finance app for South Africans.**
Upload your bank statement (PDF or CSV), and PocketRand categorises your spending automatically, shows monthly insights, tracks budgets, and estimates your income tax.

> 🚧 **Status: in active development.** Follow the progress in the commit history.

---

## The problem

Most people know their salary and their bank balance, but not where the money in between went. Bank statements list hundreds of lines with cryptic descriptions, and sorting them by hand in a spreadsheet is slow, so most people never do it.

Personal bank statements are also usually downloaded as **PDFs**, not spreadsheets, so PocketRand reads PDF statements directly as well as CSV files.

## Features

| Feature | Status |
|---|---|
| Project setup and health endpoint | ✅ Done |
| Transactions API (create, view, edit, delete) with PostgreSQL | 🔨 In progress |
| User accounts with secure login (Spring Security + JWT) | ⏳ Planned |
| Statement import: CSV and PDF | ⏳ Planned |
| Automatic categorisation (keyword rules + AI) | ⏳ Planned |
| Monthly insights: totals, spending by category, top expenses | ⏳ Planned |
| Budgets with near-limit and over-limit warnings | ⏳ Planned |
| Income tax (PAYE) estimate using current SARS tables | ⏳ Planned |
| React + TypeScript dashboard | ⏳ Planned |
| Docker, CI/CD pipeline and live deployment | ⏳ Planned |

## Tech stack

| Layer | Technology |
|---|---|
| Backend | Java 25, Spring Boot 4 (Web, Validation; Security and Data JPA planned) |
| Database | PostgreSQL |
| AI | Google Gemini API (categorisation and statement extraction) |
| Frontend | React + TypeScript |
| Testing | JUnit 5, Mockito, Spring Boot Test |
| Build | Maven (wrapper included, no install needed) |
| DevOps | Docker, GitHub Actions |

## Architecture

A three-tier web app: a React frontend calls a Spring Boot REST API, which is the only part that talks to the database and the AI service.

```
React frontend  ──HTTPS / JSON──▶  Spring Boot API  ──▶  PostgreSQL
                                   (Security → Controllers → Services → Repositories)
                                          │
                                          └──▶  Gemini API (transaction lines only)
```

Full details are in the [project scope and business requirements](docs/requirements.md).

## Getting started

### Prerequisites
- Java 25 (for example, Eclipse Temurin)
- Git

### Run locally
```bash
git clone https://github.com/Mabals/pocketrand.git
cd pocketrand
./mvnw spring-boot:run        # on Windows PowerShell: .\mvnw spring-boot:run
```
The API starts on **http://localhost:8080**.

### Try it
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/health` | Returns the service status as JSON |
| GET | `/api/hello?name=Thato` | Returns a greeting |

## Project documents
- [Project scope and business requirements](docs/requirements.md): personas, requirements, user stories, business rules, risks and delivery plan

## Privacy

PocketRand is designed with South Africa's POPIA in mind: only the data needed is stored, uploaded statements are deleted after processing, and no account, ID or card numbers are kept. The demo uses sample data only.

## Author

**Thato Khonkhe**, Full-Stack Developer
[LinkedIn](https://linkedin.com/in/thato-siyabonga-khonkheb07a12226) · [GitHub](https://github.com/Mabals)