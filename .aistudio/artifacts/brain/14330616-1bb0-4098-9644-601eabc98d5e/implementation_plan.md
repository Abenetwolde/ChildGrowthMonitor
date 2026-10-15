# NutriScan AI: Software Requirements Specification (SRS) & Implementation Blueprint

NutriScan AI is an offline-first mobile healthcare screening tool designed specifically for Health Extension Workers (HEWs) in rural and urban Ethiopia to screen children aged 6–59 months for acute malnutrition using guided clinical workflows, smartphone camera image quality assistance, and physical anthropometric measurements (MUAC, bilateral edema, and optional height/weight).

---

## User Review & Critical Decisions

> [!IMPORTANT]
> The following operational and clinical parameters have been confirmed through initial requirements clarification:

- **Clinical Protocol Baseline [Confirmed]**: Aligned strictly with the **Ethiopian National Guideline for the Management of Acute Malnutrition** and **WHO Child Growth Standards**:
  - Severe Acute Malnutrition (SAM): MUAC < 11.5 cm OR presence of bilateral pitting edema (+, ++, +++).
  - Moderate Acute Malnutrition (MAM): MUAC ≥ 11.5 cm and < 12.5 cm without edema.
  - Normal / Well-Nourished: MUAC ≥ 12.5 cm without edema.
  - Weight-for-height Z-score (WHZ) supported as secondary clinical metric when scales/stadiometers are present.
- **Role of Camera-Based AI [Confirmed]**: **Quality Assurance & Ergonomic Guide with Mandatory Manual Measurement Confirmation**.
  - Camera computer vision guides proper arm positioning, distance, lighting, and detects motion blur or tape occlusion.
  - The app **never** generates an automated clinical diagnosis solely from an image. Manual MUAC tape measurement entered by the HEW remains the authoritative clinical ground truth.
- **Language & Localization [Confirmed]**: Tri-lingual mobile experience with **English**, **Amharic (አማርኛ)**, and **Afaan Oromo**, incorporating Ethiopian administrative hierarchy (Region, Zone, Woreda, Kebele, Got/Gott).

---

## 1. Introduction

### 1.1 Purpose
This Software Requirements Specification (SRS) defines the complete functional, clinical, behavioral, data, and architectural requirements for NutriScan AI. It serves as the definitive reference for engineering, clinical verification, field testing, and quality assurance.

### 1.2 Document Conventions
- **Requirement Identifiers**:
  - `FR-xxx`: Functional Requirement
  - `NFR-xxx`: Non-Functional Requirement
  - `CLIN-xxx`: Clinical & Safety Requirement
  - `SEC-xxx`: Security, Privacy & Compliance Requirement
  - `OFF-xxx`: Offline & Synchronization Requirement
  - `AI-xxx`: AI & Computer Vision Assistance Requirement
- **Priority**:
  - **P0 (Mandatory / MVP)**: Blocking for field deployment.
  - **P1 (High)**: Essential for workflow optimization and follow-up.
  - **P2 (Nice-to-have / Future)**: Enhanced analytical features.

---

## 2. Project Objectives

1. **Reduce Screening Errors**: Guide HEWs step-by-step to eliminate measurement placement errors and transcription mistakes.
2. **Offline Field Reliability**: Guarantee 100% data entry, validation, and historical review functionality in remote Kebeles without cellular connectivity.
3. **Actionable Community Triage**: Provide immediate clinical categorization (SAM, MAM, Normal) with explicit referral guidance and follow-up scheduling according to Ethiopian Ministry of Health protocols.
4. **Caregiver Engagement**: Record verifiable consent, deliver plain-language counseling tips in local languages, and issue follow-up date reminders.
5. **Zero Data Loss**: Maintain SQLite transactional local integrity with conflict-free queued background synchronization to central health information databases.

---

## 3. Scope & Limitations

### 3.1 In Scope
- Mobile application for Android smartphones (Android 7.0+ / API 24+) utilized by Health Extension Workers.
- Child registration with administrative location tags (Region, Zone, Woreda, Kebele).
- 7-step guided screening workflow: Child Selection, Confirmation & Consent, Physical Measurements, Camera QA Guide, Assessment Evaluation, Result Display & Counseling, and Session Finalization.
- Full offline SQLite storage with bidirectional sync queue.
- Child profile, longitudinal screening history, growth trend charts, and follow-up visit management.
- REST API backend (Node.js/Express/TypeScript + PostgreSQL/Prisma) and modular Python FastAPI microservice for CV assistance.

### 3.2 Out of Scope (Phase 1)
- Administrative dashboards, supervisor portals, and woreda-level aggregated reporting portals.
- Automated autonomous medical diagnostics (all assessments are community-level triage recommendations requiring clinical confirmation).
- Complex role-based access control (RBAC) beyond authenticated HEW field sessions.
- Biometric facial recognition or iris scanning.

---

## 4. Target User & Characteristics

### 4.1 Persona: Health Extension Worker (HEW) / የጤና ኤክስቴንሽን ሠራተኛ
- **Operating Context**: Rural health posts (Kebeles) and urban community centers across Ethiopia. High sunlight glare or dim hut lighting, intermittent electricity, dust, low-cost Android smartphones (2GB–3GB RAM, limited storage).
- **Technical Literacy**: Moderate; familiar with basic smartphone messaging, prefers large high-contrast touch targets, visual guides, minimal text entry, and local language scripts (Ge'ez and Latin).
- **Clinical Role**: Performs community nutrition screening, administers ready-to-use therapeutic food (RUTF) for uncomplicated SAM, refers complicated SAM to Health Centers/Stabilization Centers (SC), and schedules bi-weekly follow-ups.

---

## 5. Functional Requirements (FR)

| Req ID | Title | Description | Priority |
| :--- | :--- | :--- | :--- |
| **FR-001** | User Authentication | HEW can log in with username/phone and secure PIN/password. Session persists securely offline. | P0 |
| **FR-002** | Local Language Switcher | User can switch UI language instantly between English, Amharic, and Afaan Oromo from login or settings. | P0 |
| **FR-003** | Dashboard Statistics | Home screen displays real-time metrics: registered children, completed screenings, follow-ups due, and pending sync count. | P0 |
| **FR-004** | Child Registration | Form capturing Child ID, full name, sex, DOB or estimated age (months), caregiver name, phone, Region, Zone, Woreda, Kebele. | P0 |
| **FR-005** | Draft Saving | Incomplete registration or screening can be saved as a draft and resumed without data loss. | P0 |
| **FR-006** | Child Search & Filter | Fast local search by name or Child ID with filters for screening status (Unscreened, Normal, MAM, SAM) and follow-up urgency. | P0 |
| **FR-007** | Step-by-Step Screening | Enforce 7-step sequential screening wizard with back/forward navigation and input validation. | P0 |
| **FR-008** | Physical Measurements | Input fields for MUAC (cm, decimal), bilateral pitting edema (None, Grade +, ++, +++), optional Weight (kg), Height (cm). | P0 |
| **FR-009** | Outlier Warning | Real-time warnings for physiologically implausible entries (e.g. MUAC < 6.0 cm or > 25.0 cm, Weight > 30 kg for under 5). | P0 |
| **FR-010** | Camera Framing QA | Guided overlay camera preview providing visual indicators for focal distance, arm silhouette alignment, and lighting adequacy. | P0 |
| **FR-011** | Manual Fallback | Ability to skip camera guide at any point and proceed directly to manual measurement entry. | P0 |
| **FR-012** | Assessment Engine | Local rule-based engine categorizing child into SAM (Red), MAM (Yellow), or Normal (Green) per Ethiopian MOH guidelines. | P0 |
| **FR-013** | Referral Generation | When SAM or complicated criteria are met, app generates a structured digital referral slip with reasons for referral. | P0 |
| **FR-014** | Follow-Up Scheduling | Automatic suggestion of next screening date (7 days for SAM, 14 days for MAM, 30 days for Normal) with custom date override. | P0 |
| **FR-015** | Longitudinal History | Child profile screen showing past screening sessions, historical MUAC values, and trend graphs. | P1 |
| **FR-016** | Offline Sync Queue | Screenings and registrations saved locally with state tags (`draft`, `synced`, `pending_sync`, `sync_error`). | P0 |
| **FR-017** | Manual & Auto Sync | Background sync triggers automatically when network connectivity is detected; manual "Sync Now" button available. | P0 |

---

## 6. Non-Functional Requirements (NFR)

| Req ID | Title | Specification |
| :--- | :--- | :--- |
| **NFR-001** | Offline Operation | 100% of registration, screening, assessment, and record retrieval functions operate without network. |
| **NFR-002** | Device Resource Footprint | Cold start time < 2.0s on entry-level Android devices (2GB RAM). Total APK install size < 35MB. |
| **NFR-003** | Battery Efficiency | Camera preview optimized to auto-pause after 45 seconds of inactivity to conserve battery in remote field sites. |
| **NFR-004** | Touch Target Sizing | All interactive buttons, tabs, and input controls adhere to minimum 48dp x 48dp touch target boundaries. |
| **NFR-005** | High Sunlight Legibility | UI adheres to WCAG 2.1 AA contrast ratios (minimum 4.5:1 for body text, 7:1 for critical clinical alerts). |
| **NFR-006** | Data Durability | SQLite database uses Write-Ahead Logging (WAL) and atomic transactions to prevent corruption during unexpected shutdowns. |
| **NFR-007** | Network Resilience | Synchronizer utilizes exponential backoff retry (1s, 2s, 4s, 8s up to 60s) with payload compression (gzip). |

---

## 7. Clinical & Safety Requirements (CLIN)

| Req ID | Clinical Protocol Rule |
| :--- | :--- |
| **CLIN-001** | **SAM Classification**: If bilateral pitting edema is present (Grade +, ++, +++) OR MUAC is strictly less than 11.5 cm (< 115 mm), the session MUST trigger a RED alert: "Severe Acute Malnutrition (SAM) - Immediate Referral/OTP Admission". |
| **CLIN-002** | **MAM Classification**: If edema is absent AND 11.5 cm ≤ MUAC < 12.5 cm, the session MUST trigger a YELLOW alert: "Moderate Acute Malnutrition (MAM) - Supplementary Feeding Program (TSFP) & Counseling". |
| **CLIN-003** | **Normal Classification**: If edema is absent AND MUAC ≥ 12.5 cm, the session MUST trigger a GREEN alert: "Well Nourished - Routine Growth Monitoring". |
| **CLIN-004** | **Medical Disclaimer**: Every assessment screen must state: *"NutriScan AI is a community screening aid and does not constitute a definitive medical diagnosis. Clinical verification by a healthcare provider is required."* |
| **CLIN-005** | **Danger Sign Screen**: Immediate referral prompt if child exhibits danger signs: unable to drink/breastfeed, persistent vomiting, convulsions, lethargy, or acute respiratory distress. |

---

## 8. User Stories & Acceptance Criteria

### US-1: Registering a Child in a Remote Village
- **As a** Health Extension Worker,
- **I want to** register a 14-month-old child while offline in a remote Kebele,
- **So that** I can establish their clinical record before initiating nutrition screening.
- **Acceptance Criteria**:
  - GIVEN no internet connection, WHEN I enter the child's name, estimated age (14 months), sex, mother's name, and Kebele, THEN the system generates a unique UUID and human-readable ID (e.g., `ETH-OR-24-00812`).
  - WHEN I tap "Save & Begin Screening", the record persists in local SQLite with `sync_status = 'pending_sync'`, and advances to Step 2.

### US-2: Conducting Guided MUAC Screening with Camera QA
- **As a** Health Extension Worker,
- **I want to** use the camera to verify arm positioning and record the physical tape measurement,
- **So that** I avoid inaccurate tape placement and get immediate clinical categorization.
- **Acceptance Criteria**:
  - GIVEN the camera preview, WHEN the child's arm is in the viewfinder, THEN the overlay provides visual framing guides (midpoint between acromion and olecranon).
  - WHEN lighting is insufficient, a "Poor Lighting" warning appears with a button to turn on the flashlight.
  - WHEN the HEW enters 11.2 cm MUAC and confirms zero edema, THEN the system displays a RED SAM banner with specific referral steps to the nearest Health Post OTP.

### US-3: Syncing Field Data upon Returning to Town
- **As a** Health Extension Worker,
- **I want** all 25 screenings conducted during my village outreach to sync automatically when I regain 3G/Wi-Fi,
- **So that** health center records stay up-to-date without manual re-entry.
- **Acceptance Criteria**:
  - WHEN network connectivity transitions from offline to online, THEN the sync manager sends batch payloads with deduplication idempotency keys.
  - The dashboard updates `Unsynchronized Records` count down to 0, showing "All records synced".

---

## 9. Mobile Screen Specifications & Navigation Flow

### 9.1 Screen List
1. **SCR-01: Splash & Language Selection**: App logo, language selection buttons (English / አማርኛ / Afaan Oromoo), offline-readiness check.
2. **SCR-02: Authentication / Pin Login**: HEW credential entry, remembered user badge, biometric/PIN unlock for offline sessions.
3. **SCR-03: Home Dashboard**: Greeting, Kebele context, 4 key metrics cards, prominent primary action buttons ("Start New Screening", "Register Child"), and recent screenings list.
4. **SCR-04: Child List & Directory**: Search bar with debounced query, filter chips (All, Unscreened, Normal, MAM, SAM, Follow-up Due), child list items with color-coded status badges.
5. **SCR-05: Child Registration / Edit Form**: Administrative dropdowns (Region -> Zone -> Woreda -> Kebele), child demographic inputs, caregiver contact, Save Draft / Submit buttons.
6. **SCR-06: Screening Step 1 - Select Child**: Searchable picker of existing children with "+ Register New Child" button.
7. **SCR-07: Screening Step 2 - Confirmation & Consent**: Verified child details, age confirmation, caregiver verbal consent toggle.
8. **SCR-08: Screening Step 3 - Physical Measurements**: Numeric keypad for MUAC (cm), bilateral pitting edema segmented button (None, +, ++, +++), optional Weight and Height inputs.
9. **SCR-09: Screening Step 4 - Camera QA Assistant**: Viewfinder with arm silhouette overlay, real-time lighting check, torch toggle, capture/skip buttons.
10. **SCR-10: Screening Step 5 & 6 - Assessment Results**: High-impact color alert banner (Red/Yellow/Green), MUAC gauge, clinical recommendation, referral slip button.
11. **SCR-11: Screening Step 7 - Follow-Up & Finalize**: Calendar date picker pre-filled with MOH guideline interval, notes field, Save & Complete button.
12. **SCR-12: Child Profile & Longitudinal History**: Summary demographics card, chronological timeline of past screenings, interactive MUAC trend line chart.
13. **SCR-13: Follow-Up Management**: Tabbed view of "Due Today", "Upcoming", and "Overdue" follow-up screenings with one-tap action to begin follow-up screening.
14. **SCR-14: Sync Center & Settings**: Connectivity status, last sync timestamp, pending records count, "Sync Now" button, language toggle, and offline storage clear/export options.

### 9.2 Navigation Architecture Flow

```
┌────────────────────────────────────────────────────────┐
│              SCR-01: Splash & Language                 │
└───────────────────────────┬────────────────────────────┘
                            │
┌───────────────────────────▼────────────────────────────┐
│              SCR-02: Authentication                    │
└───────────────────────────┬────────────────────────────┘
                            │
┌───────────────────────────▼────────────────────────────┐
│                   MAIN APP CONTAINER                   │
│         (Bottom Navigation: 5 Primary Tabs)            │
├─────────────┬─────────────┬──────────────┬─────────────┤
│   Tab 1     │    Tab 2    │    Tab 3     │    Tab 4    │    Tab 5
│   HOME      │  CHILDREN   │NEW SCREENING │ FOLLOW-UPS  │   PROFILE
│  (SCR-03)   │  (SCR-04)   │  (SCR-06)    │  (SCR-13)   │  (SCR-14)
└──────┬──────┴──────┬──────┴──────┬───────┴──────┬──────┴──────┬──────┘
       │             │             │              │             │
       │             │             ▼              │             │
       │             │     ┌───────────────┐      │             │
       │             │     │ SCR-07: Step 2│      │             │
       │             │     └───────┬───────┘      │             │
       │             │             ▼              │             │
       │             │     ┌───────────────┐      │             │
       │             │     │ SCR-08: Step 3│      │             │
       │             │     └───────┬───────┘      │             │
       │             │             ▼              │             │
       │             │     ┌───────────────┐      │             │
       │             │     │ SCR-09: Step 4│      │             │
       │             │     └───────┬───────┘      │             │
       │             │             ▼              │             │
       │             │     ┌───────────────┐      │             │
       │             │     │ SCR-10: Step 5│      │             │
       │             │     └───────┬───────┘      │             │
       │             │             ▼              │             │
       │             │     ┌───────────────┐      │             │
       │             │     │ SCR-11: Step 7│      │             │
       │             │     └───────┬───────┘      │             │
       │             │             │              │             │
       │             ▼             │              ▼             │
       │       ┌───────────┐       │        ┌───────────┐       │
       └──────►│ SCR-12:   │◄──────┴───────►│ SCR-05:   │◄──────┘
               │Child View │                │Child Reg. │
               └───────────┘                └───────────┘
```

---

## 10. Screening Step-by-Step Clinical Workflow

```
[Start Screening] ──► [Step 1: Select/Register Child]
                             │
                             ▼
                      [Step 2: Confirm Age & Consent]
                             │
                             ▼
                      [Step 3: Enter Anthropometrics]
                             ├── Edema Checked (None / +, ++, +++)
                             ├── MUAC Measured & Entered (cm)
                             └── Outlier Checks Validated
                             │
                             ▼
                      [Step 4: Camera Positioning QA]
                             ├── Pass: Framing & Lighting OK
                             └── Skip: Field conditions prohibitive
                             │
                             ▼
                      [Step 5: Clinical Evaluation]
                             ├── SAM: Edema present OR MUAC < 11.5cm
                             ├── MAM: No edema AND 11.5cm ≤ MUAC < 12.5cm
                             └── Normal: No edema AND MUAC ≥ 12.5cm
                             │
                             ▼
                      [Step 6: Display Results & Counseling]
                             ├── Red Banner (SAM): Urgent OTP Referral
                             ├── Yellow Banner (MAM): TSFP & Counseling
                             └── Green Banner (Normal): Growth Monitoring
                             │
                             ▼
                      [Step 7: Finalize & Schedule Follow-Up]
                             ├── SAM: 7-day recall
                             ├── MAM: 14-day recall
                             └── Saved to SQLite (Pending Sync)
```

---

## 11. System Architecture

```
┌────────────────────────────────────────────────────────────────────────┐
│                       ANDROID CLIENT (FIELD)                           │
│                                                                        │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │                    Presentation Layer (Compose/UI)               │  │
│  │  - Material Design 3 (High-Contrast, Sunlight Optimized)         │  │
│  │  - Localization Engine (English, Amharic, Afaan Oromo)           │  │
│  │  - CameraX / Guidance Overlay Component                          │  │
│  └──────────────────────────────────┬───────────────────────────────┘  │
│                                     ▼                                  │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │                     State & Business Logic                       │  │
│  │  - ViewModel + StateFlow Reactive Pipeline                       │  │
│  │  - Clinical Assessment Engine (MOH/WHO Protocol Rules)           │  │
│  │  - Measurement Validator & Plausibility Guard                    │  │
│  └──────────────────────────────────┬───────────────────────────────┘  │
│                                     ▼                                  │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │                     Local Data Layer (Offline)                   │  │
│  │  - Room / SQLite Database (WAL Mode, Encrypted SQLCipher)        │  │
│  │  - Sync Queue Table & Conflict-Free Change Log                   │  │
│  │  - Secure Keystore (Auth Tokens & Field Session State)           │  │
│  └──────────────────────────────────┬───────────────────────────────┘  │
└─────────────────────────────────────┼──────────────────────────────────┘
                                      │ HTTPS / TLS 1.3
                                      │ (When network available)
                                      ▼
┌────────────────────────────────────────────────────────────────────────┐
│                        CENTRAL BACKEND (CLOUD)                         │
│                                                                        │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │            REST API Gateway (Node.js / Express / TypeScript)     │  │
│  │  - JWT Authentication & Session Verifier                         │  │
│  │  - Batch Sync Ingestion & Idempotent Conflict Resolver           │  │
│  │  - Audit Logging & Health System Integration                     │  │
│  └───────────────────┬──────────────────────────────┬───────────────┘  │
│                      │                              │                  │
│                      ▼                              ▼                  │
│  ┌─────────────────────────────────────┐  ┌─────────────────────────┐  │
│  │     PostgreSQL + Prisma ORM         │  │ Python FastAPI CV Micro │  │
│  │  - Central Child Registry           │  │  - Image Quality Check  │  │
│  │  - Longitudinal Screening History   │  │  - Arm Silhouette Model │  │
│  │  - Administrative Geographies       │  │  - OpenCV Blur & Light  │  │
│  └─────────────────────────────────────┘  └─────────────────────────┘  │
└────────────────────────────────────────────────────────────────────────┘
```

---

## 12. Database Entity-Relationship Diagram (ERD)

```
┌──────────────────┐          1:N         ┌──────────────────┐
│      USERS       │─────────────────────<│     CHILDREN     │
│  (HEW Workers)   │                      │  (Master Record) │
└────────┬─────────┘                      └────────┬─────────┘
         │                                         │
         │ 1:N                                     │ 1:N
         │                                         │
         ▼                                         ▼
┌──────────────────┐          1:N         ┌──────────────────┐
│   AUDIT_LOGS     │                      │SCREENING_SESSIONS│
│  (Action Trail)  │                      │ (Clinical Events)│
└──────────────────┘                      └────────┬─────────┘
                                                   │
                   ┌───────────────────────────────┼──────────────────────────────┐
                   │ 1:1                           │ 1:1                          │ 1:1
                   ▼                               ▼                              ▼
         ┌──────────────────┐            ┌──────────────────┐           ┌──────────────────┐
         │ ANTHROPOMETRICS  │            │  AI_ASSESSMENTS  │           │   REFERRALS      │
         │(MUAC, Edema, etc)│            │(Quality & Notes) │           │(Health Post/OTP) │
         └──────────────────┘            └──────────────────┘           └──────────────────┘
                   │
                   │ 1:N
                   ▼
         ┌──────────────────┐
         │ FOLLOW_UP_VISITS │
         │(Scheduled/Done)  │
         └──────────────────┘
```

---

## 13. Database Schema (PostgreSQL Server & SQLite Local Mirror)

### 13.1 Table: `users`
```sql
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20),
    assigned_region VARCHAR(50) NOT NULL,
    assigned_zone VARCHAR(50) NOT NULL,
    assigned_woreda VARCHAR(50) NOT NULL,
    assigned_kebele VARCHAR(50) NOT NULL,
    preferred_language VARCHAR(10) DEFAULT 'en', -- 'en', 'am', 'om'
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);
```

### 13.2 Table: `children`
```sql
CREATE TABLE children (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    local_id VARCHAR(50) UNIQUE NOT NULL, -- e.g., 'ETH-OR-24-00812'
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    sex VARCHAR(10) NOT NULL, -- 'MALE', 'FEMALE'
    date_of_birth DATE,
    estimated_age_months INTEGER NOT NULL,
    caregiver_name VARCHAR(100) NOT NULL,
    caregiver_phone VARCHAR(20),
    region VARCHAR(50) NOT NULL,
    zone VARCHAR(50) NOT NULL,
    woreda VARCHAR(50) NOT NULL,
    kebele VARCHAR(50) NOT NULL,
    got_community VARCHAR(50),
    registered_by_user_id UUID NOT NULL REFERENCES users(id),
    is_draft BOOLEAN DEFAULT false,
    sync_status VARCHAR(20) DEFAULT 'pending_sync', -- 'synced', 'pending_sync', 'sync_error'
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);
CREATE INDEX idx_children_kebele ON children(kebele);
CREATE INDEX idx_children_local_id ON children(local_id);
```

### 13.3 Table: `screening_sessions`
```sql
CREATE TABLE screening_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    local_session_id VARCHAR(50) UNIQUE NOT NULL,
    child_id UUID NOT NULL REFERENCES children(id) ON DELETE CASCADE,
    screened_by_user_id UUID NOT NULL REFERENCES users(id),
    screening_date TIMESTAMP WITH TIME ZONE NOT NULL,
    screening_location VARCHAR(100) NOT NULL,
    caregiver_consent BOOLEAN NOT NULL DEFAULT true,
    clinical_category VARCHAR(20) NOT NULL, -- 'SAM', 'MAM', 'NORMAL'
    has_bilateral_edema BOOLEAN NOT NULL DEFAULT false,
    edema_grade VARCHAR(10) NOT NULL DEFAULT 'NONE', -- 'NONE', 'PLUS_1', 'PLUS_2', 'PLUS_3'
    muac_cm NUMERIC(4, 1) NOT NULL, -- e.g., 11.3
    weight_kg NUMERIC(4, 2), -- Optional e.g., 8.45
    height_cm NUMERIC(4, 1), -- Optional e.g., 74.2
    danger_signs_present BOOLEAN DEFAULT false,
    danger_signs_notes TEXT,
    action_taken VARCHAR(50) NOT NULL, -- 'REFERRED_TO_OTP', 'TSFP_ENROLLED', 'ROUTINE_COUNSELING'
    next_follow_up_date DATE,
    sync_status VARCHAR(20) DEFAULT 'pending_sync',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);
CREATE INDEX idx_screenings_child ON screening_sessions(child_id);
CREATE INDEX idx_screenings_date ON screening_sessions(screening_date);
```

### 13.4 Table: `ai_quality_assessments`
```sql
CREATE TABLE ai_quality_assessments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    screening_session_id UUID NOT NULL REFERENCES screening_sessions(id) ON DELETE CASCADE,
    lighting_score NUMERIC(3, 2), -- 0.00 to 1.00
    blur_score NUMERIC(3, 2),     -- 0.00 to 1.00
    arm_position_detected BOOLEAN DEFAULT true,
    image_quality_verdict VARCHAR(20) NOT NULL, -- 'ACCEPTABLE', 'POOR_LIGHTING', 'BLURRY', 'SKIPPED'
    manual_override_used BOOLEAN DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);
```

### 13.5 Table: `referrals`
```sql
CREATE TABLE referrals (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    screening_session_id UUID NOT NULL REFERENCES screening_sessions(id) ON DELETE CASCADE,
    child_id UUID NOT NULL REFERENCES children(id),
    referral_facility_name VARCHAR(100) NOT NULL,
    reason_for_referral TEXT NOT NULL,
    urgency_level VARCHAR(20) NOT NULL, -- 'URGENT', 'STANDARD'
    transport_support_needed BOOLEAN DEFAULT false,
    referral_status VARCHAR(20) DEFAULT 'ISSUED', -- 'ISSUED', 'ARRIVED', 'COMPLETED'
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);
```

### 13.6 Table: `follow_up_visits`
```sql
CREATE TABLE follow_up_visits (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    child_id UUID NOT NULL REFERENCES children(id) ON DELETE CASCADE,
    original_screening_id UUID REFERENCES screening_sessions(id),
    scheduled_date DATE NOT NULL,
    completed_date DATE,
    status VARCHAR(20) DEFAULT 'PENDING', -- 'PENDING', 'COMPLETED', 'MISSED', 'RESCHEDULED'
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);
CREATE INDEX idx_follow_ups_child_date ON follow_up_visits(child_id, scheduled_date);
```

---

## 14. API Endpoint Specifications

All endpoints use JSON over HTTPS with Bearer JWT tokens.

### 14.1 Authentication
- `POST /api/v1/auth/login`
  - Body: `{ username, password }`
  - Response: `{ token, user: { id, fullName, assignedKebele, preferredLanguage } }`
- `POST /api/v1/auth/refresh`
  - Body: `{ refreshToken }`
  - Response: `{ token }`

### 14.2 Child Records
- `GET /api/v1/children?kebele={kebele}&updatedAfter={timestamp}`
  - Synchronizes children registered or modified since last check.
- `POST /api/v1/children`
  - Body: Full child payload with `localId` for idempotency.
  - Response: `{ id, localId, syncStatus: "synced" }`
- `PUT /api/v1/children/:id`
  - Updates demographic records with optimistic concurrency check.

### 14.3 Screening & Assessments
- `POST /api/v1/screenings`
  - Body: `{ localSessionId, childId, screeningDate, muacCm, edemaGrade, clinicalCategory, ... }`
  - Response: `{ id, localSessionId, syncStatus: "synced" }`
- `GET /api/v1/children/:id/screenings`
  - Returns complete chronological screening history for the child.

### 14.4 Batch Synchronization
- `POST /api/v1/sync/batch`
  - Body:
    ```json
    {
      "batchId": "urn:uuid:6c9b3a2a-43c1-482a-bc90-df4f0524458b",
      "children": [ /* array of new/modified children */ ],
      "screenings": [ /* array of screening sessions */ ],
      "followUps": [ /* array of follow-up updates */ ]
    }
    ```
  - Response:
    ```json
    {
      "processed": 14,
      "failed": 0,
      "serverTime": "2026-10-14T14:30:00Z",
      "conflicts": []
    }
    ```

---

## 15. Offline Storage & Synchronization Design

### 15.1 Local SQLite Architecture
1. **Primary Key Strategy**: Every locally created entity generates a UUID v4 as its client primary key, alongside a human-readable regional identifier (`local_id`).
2. **Sync State Flagging**: Every mutable row has:
   - `sync_state`: `SYNCED`, `INSERT_PENDING`, `UPDATE_PENDING`, `ERROR`
   - `last_modified_local`: Microsecond epoch timestamp
   - `synced_at`: Nullable timestamp
3. **Queue Processor**:
   - Monitored by a background task that detects active network connectivity (`NetworkCapabilities.NET_CAPABILITY_VALIDATED`).
   - Bundles all `INSERT_PENDING` and `UPDATE_PENDING` rows into an atomic JSON batch.
   - On HTTP 200/201, marks local rows as `SYNCED`.
   - On server conflict (e.g. child edited on another device), applies "Server Wins" for demographics unless local edit is newer by timestamp. Screening sessions are strictly append-only and never conflict.

---

## 16. AI Integration & Computer Vision Specifications

### 16.1 Vision Quality Assurance Pipeline
```
[Camera Frame 1080p]
         │
         ▼
[Preprocessing: Resize 640x480 & Grayscale]
         │
         ├──► [Laplacian Variance Check]: Blur Detection (Threshold > 100)
         ├──► [Mean Luminance Check]: Lighting Detection (50 < Y < 220)
         └──► [Contour Aspect Ratio]: Arm Upper-Third Alignment Guidance
         │
         ▼
[UI Feedback Engine]
         ├── "Lighting Adequate" (Green border) / "Too Dark" (Torch Prompt)
         ├── "Steady - Hold Still" (Motion dampening indicator)
         └── "Arm in Target Box" (Overlay silhouette alignment)
```

### 16.2 Safety Guardrails
- **No Autonomous Diagnostic Decisions**: The AI engine never computes a SAM/MAM category on its own.
- **Mandatory Tape Entry**: The UI explicitly disables the "Evaluate" button until a numerical MUAC measurement is typed into the keypad.
- **Image Retention Policy**: Field photographs are processed exclusively in RAM for quality feedback and immediately discarded unless the caregiver provided separate, written research consent.

---

## 17. Security, Privacy & Ethiopian Healthcare Compliance

1. **Authentication**: 6-digit cryptographic PIN or strong password hashing using Argon2id/BCrypt.
2. **Local Encryption**: SQLite database encrypted via SQLCipher using AES-256 with keys stored in the Android Keystore hardware-backed security module (TEE/StrongBox).
3. **Data Minimization**: Only clinically necessary anthropometrics and maternal contact info are collected.
4. **Caregiver Consent**: Explicit verbal consent checkbox recorded at Step 2 of every screening session with timestamp.
5. **No Cloud Leakage in Logs**: Child names, phone numbers, and exact GPS coordinates are stripped from application debug logs.

---

## 18. Error-Handling Requirements

- **ERR-001 (Camera Permission Denied)**: App must display an educational card explaining why camera access is beneficial, while immediately offering: *"Skip to Manual Measurement"*.
- **ERR-002 (Implausible Measurements)**: Values like MUAC < 6cm or > 25cm show an inline non-blocking confirmation dialog: *"Please re-verify: 8.2 cm indicates severe wasting. Confirm value?"*
- **ERR-003 (Network Timeout during Sync)**: Silent exponential backoff retry. Zero intrusive error banners on the home dashboard.
- **ERR-004 (Low Storage Space)**: Alert HEW if available device flash memory falls below 50MB, providing a safe purge of cached thumbnail assets while retaining database rows.

---

## 19. Testing Strategy

1. **Unit Testing (JUnit / Robolectric)**:
   - Comprehensive test coverage of the `ClinicalAssessmentEngine` verifying 100% of boundary conditions (< 11.5cm, exactly 11.5cm, 12.4cm, 12.5cm, edema Grade 1/2/3).
   - Form validation logic and outlier detection thresholds.
2. **Integration Testing**:
   - Room SQLite database transactions and migration scripts.
   - Sync queue processor simulating network drops, server 500 errors, and duplicate transmissions.
3. **Usability & Accessibility Testing in Field Simulation**:
   - Testing under simulated direct midday sunlight (contrast check).
   - Single-hand navigation testing on 5.5-inch screens.
   - Localization verification (Amharic Ge'ez font rendering and text wrapping).

---

## 20. Deployment Plan

- **Phase 1: Alpha Pilot**: 20 Health Extension Workers across 2 Woredas (1 Rural, 1 Peri-urban). Baseline synchronization testing, paper register cross-verification.
- **Phase 2: Expanded Field Trial**: 150 HEWs in 15 Woredas. Evaluation of screening throughput time and referral adherence.
- **Phase 3: National Rollout**: Integration with Ethiopian National Health Information System (eCHIS / DHIS2).

---

## 21. Risks & Assumptions

- **Assumption 1**: HEW smartphones have working rear cameras with autofocus and operational LED flashlights.
- **Assumption 2**: Standard paper MUAC tapes (UNICEF standard tri-color) remain the primary measurement device during all field screenings.
- **Risk 1 (Device Breakage or Loss)**: Addressed by encrypting local database with Android Keystore so lost phones cannot be accessed.
- **Risk 2 (Prolonged Power Outages)**: App enforces dark theme / low-power display modes and minimizes CPU background wake locks to preserve battery life for up to 3 days of outreach.

---

## 22. Future Enhancements

- **FE-001**: Direct integration with Ethiopia's electronic Community Health Information System (eCHIS).
- **FE-002**: Automated optical character recognition (OCR) of paper MUAC tape markings for instant measurement reading.
- **FE-003**: Automated SMS appointment reminders sent directly to caregiver mobile phones via Ethio Telecom gateway.
