# BGV Portal — API Endpoint Test Report

**Date:** 2026-05-25 (re-tested failures: 2026-05-25)  
**Base URL:** `http://localhost:8080`  
**Spring Boot version:** 3.4.5  
**Test verifier:** `testverifier@bgvtest.com`  
**Test admin:** `admin` (role: `super_admin`)

---

## Legend

| Symbol | Meaning |
|--------|---------|
| ✅ | Pass — correct status and response body |
| ❌ | Fail — error or wrong behaviour |
| 🔧 | Bug fixed during this test session |

---

## 1. Verifier Auth — `/api/auth`

| # | Method | Endpoint | Scenario | Expected | Actual | Result |
|---|--------|----------|----------|----------|--------|--------|
| 1 | POST | `/api/auth/register` | New verifier | 201 | 201 `{"id":42,...}` | ✅ |
| 2 | POST | `/api/auth/register` | Duplicate email | 409 | 409 `"Email is already registered"` | ✅ |
| 3 | POST | `/api/auth/login` | Valid credentials | 200 `requireOtp:true` | 200 `{"requireOtp":true,...}` | ✅ |
| 4 | POST | `/api/auth/login` | Wrong password | 401 | 401 `"Invalid credentials"` | ✅ |
| 5 | POST | `/api/auth/verify-otp` | Correct OTP (`12345`) | 200 + JWT | 200 `{"token":"eyJ...","verifier":{...}}` | ✅ |
| 6 | POST | `/api/auth/verify-otp` | Wrong OTP | 401 | 401 `"Invalid credentials"` | ✅ |
| 7 | GET | `/api/auth/me` | Authenticated | 200 + profile | 200 `{"email":"testverifier@...","isActive":true,...}` | ✅ |
| 8 | GET | `/api/auth/me` | No token | 401 | 401 | ✅ |
| 9 | PUT | `/api/auth/me` | Update company name | 200 + updated | 200 `{"companyName":"Test BGV Agency"}` | ✅ |
| 10 | POST | `/api/auth/revoke` | Valid token | 200 | 200 `{"message":"Token revoked"}` | ✅ |
| 11 | GET | `/api/auth/me` | Revoked token | 401 | 401 | ✅ |

**Section result: 11/11 pass**

---

## 2. Admin Auth — `/api/admin/auth`

| # | Method | Endpoint | Scenario | Expected | Actual | Result |
|---|--------|----------|----------|----------|--------|--------|
| 1 | POST | `/api/admin/auth/login` | Valid credentials | 200 + JWT | 200 `{"token":"eyJ...","admin":{...}}` | ✅ |
| 2 | POST | `/api/admin/auth/login` | Wrong password | 401 | 401 `"Invalid credentials"` | ✅ |

**Section result: 2/2 pass**

---

## 3. Verification — `/api/verify`

| # | Method | Endpoint | Scenario | Expected | Actual | Result |
|---|--------|----------|----------|----------|--------|--------|
| 1 | POST | `/api/verify/validate-employee` | Correct name | 200 `found:true` | 200 `{"found":true,"employeeId":"5014900",...}` | ✅ |
| 2 | POST | `/api/verify/validate-employee` | Name mismatch | 200 `found:false` + attempt counter | 200 `{"found":false,...,"2 attempt(s) remaining"}` | ✅ |
| 3 | POST | `/api/verify/validate-employee` | Unknown employee | 200 `found:false` | 200 `{"found":false,...}` | ✅ |
| 4 | POST | `/api/verify/request` | All fields match | 200 `matched` score=100 | 200 `{"overallStatus":"matched","matchScore":100,...}` | ✅ |
| 5 | POST | `/api/verify/request` | Partial match (wrong designation) | 200 `partial_match` | 200 `{"overallStatus":"partial_match","matchScore":86,...}` | ✅ |
| 6 | GET | `/api/verify/request` | History (all verifs for user) | 200 + array | 200 `[{...}]` count=3 | ✅ |
| 7 | GET | `/api/verify/request?verificationId=VER000035` | Specific record | 200 + record | 200 `{"verificationId":"VER000035",...}` | ✅ |

**Section result: 7/7 pass**

> **Bug fixed:** `BGV_VERIFICATION_ATTEMPTS.employee_id` column was `NUMBER` in the original schema but the service stores string employee codes. A Flyway V2 migration converted the column to `VARCHAR2(100)`. Flyway ran on restart.

---

## 4. Appeals — `/api/appeals`

| # | Method | Endpoint | Scenario | Expected | Actual | Result |
|---|--------|----------|----------|----------|--------|--------|
| 1 | POST | `/api/appeals` | Valid submission (verifier) | 201 + appeal | 201 `{"appealId":"APP000034","status":"pending",...}` | ✅ |
| 2 | POST | `/api/appeals` | Comments < 10 chars | 400 validation | 400 `"Appeal reason must be 10-1000 characters"` | ✅ |
| 3 | GET | `/api/appeals` | All appeals (admin) | 200 + page | 200 `{"totalElements":18,...}` | ✅ |
| 4 | GET | `/api/appeals?status=pending` | Filtered by status | 200 + page | 200 `{"totalElements":11,...}` | ✅ |
| 5 | GET | `/api/appeals/APP000034` | Specific appeal (admin) | 200 + appeal | 200 `{"status":"pending",...}` | ✅ |
| 6 | GET | `/api/appeals/APP000034` | Specific appeal (verifier) | 403 | 403 | ✅ |
| 7 | POST | `/api/appeals/APP000034/respond` | HR responds (admin) | 200 `completed` | 200 `{"status":"completed",...}` | ✅ |

**Section result: 7/7 pass**

> **Bug fixed:** `SequenceServiceImpl` fallback used `COUNT(*)+1` which collided with existing IDs when records had gaps. Fixed to use `MAX(TO_NUMBER(SUBSTR(id_col, prefix_len+1)))+1` so the next ID always exceeds the current maximum.

---

## 5. Admin — `/api/admin`

| # | Method | Endpoint | Scenario | Expected | Actual | Result |
|---|--------|----------|----------|----------|--------|--------|
| 1 | GET | `/api/admin/dashboard` | Admin token | 200 + stats | 200 `{"totalVerifications":36,"pendingAppeals":10,"activeVerifiers":6,"totalEmployees":60,...}` | ✅ |
| 2 | GET | `/api/admin/verifiers` | Admin token | 200 + list | 200 array of 6 verifiers | ✅ |
| 3 | GET | `/api/admin/verifiers` | Verifier token | 403 | 403 | ✅ |
| 4 | POST | `/api/admin/verifiers/41/toggle` | Toggle active state | 200 + updated | 200 `{"isActive":false,...}` (toggled back) | ✅ |
| 5 | GET | `/api/admin/blocked-verifiers` | Admin token | 200 + list | 200 count=3 blocked attempts | ✅ |
| 6 | GET | `/api/admin/logs` | Admin token | 200 + page | 200 `{"totalElements":101,...}` 🔧 | ✅ |
| 7 | GET | `/api/admin/logs?status=SUCCESS` | Filtered logs | 200 + page | 200 `{"totalElements":93,...}` 🔧 | ✅ |
| 8 | GET | `/api/admin/export` | Excel download | 200 + .xlsx | 200 `Content-Type: application/vnd.openxmlformats...` 5409 bytes | ✅ |

**Section result: 8/8 pass** *(was 6/8 — fixed after restart)*

> **Bug fixed:** `AccessLog.timestamp` was mapped as `@Column(name = "\"timestamp\"")`. Hibernate generated `al1_0."timestamp"` (lowercase quoted), but Oracle stored the column as `TIMESTAMP` (uppercase), causing `ORA-00904: invalid identifier`. Fixed to `@Column(name = "timestamp")` — Oracle resolves it case-insensitively.

---

## 6. Reports — `/api/reports`

| # | Method | Endpoint | Scenario | Expected | Actual | Result |
|---|--------|----------|----------|----------|--------|--------|
| 1 | GET | `/api/reports/generate?verificationId=VER000035` | Verifier (owns record) | 200 PDF | 200 `Content-Type: application/pdf` 11938 bytes | ✅ |
| 2 | GET | `/api/reports/generate?verificationId=VER000035` | Admin | 200 PDF | 200 `application/pdf` 11938 bytes 🔧 | ✅ |
| 3 | GET | `/api/reports/generate?verificationId=VERXXXXXX` | Bad ID | 404 | 404 `"Verification not found: VERXXXXXX"` | ✅ |

**Section result: 3/3 pass** *(was 2/3 — fixed after restart)*

> **Bug fixed:** `ReportController` checked `startsWith("ROLE_ADMIN")` which does not match `ROLE_SUPER_ADMIN`. Admin users were getting 404 because the verifier ownership check ran with the admin's numeric ID. Fixed to `noneMatch(a -> a.equals("ROLE_VERIFIER"))` — any non-verifier role bypasses the ownership check.

---

## Summary

| Section | Pass | Fail | Total |
|---------|------|------|-------|
| Verifier Auth | 11 | 0 | 11 |
| Admin Auth | 2 | 0 | 2 |
| Verification | 7 | 0 | 7 |
| Appeals | 7 | 0 | 7 |
| Admin | 8 | 0 | 8 |
| Reports | 3 | 0 | 3 |
| **Total** | **38** | **0** | **38** |

---

## Final Status

All 38 endpoints pass. No outstanding failures.
