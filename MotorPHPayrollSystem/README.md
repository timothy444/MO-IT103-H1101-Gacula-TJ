# MotorPH Payroll System

**Author:** Timothy Justin Sonido Gacula  
**Date:** July 19, 2026

---

## Overview

A Java Swing desktop app for managing employee records, attendance, and payroll computation. It reads from and writes to UTF-8 CSV files, validates inputs, computes statutory deductions, and generates payroll summaries. On macOS it uses the native Aqua look and feel (system menu bar and automatic light/dark appearance).

---

## Features

- **Employee Database** — Add, edit, delete, search, and filter employees by status
- **Attendance Management** — Computes hours worked from time-in/time-out logs (with lunch deduction)
- **Payroll Computation** — Calculates gross pay, deductions (SSS, PhilHealth, Pag-IBIG, withholding tax), and net pay; manual hours can override attendance
- **Payroll Records** — Saves, views, and deletes (single or multi-select) computed payroll entries
- **Clipboard Support** — Copy table cells with Ctrl+C / Cmd+C
- **macOS-native UI** — Native system look and feel, top menu bar, and system light/dark appearance

---

## Tech Stack

- **Language:** Java 17
- **UI:** Java Swing (NetBeans Matisse)
- **Data:** UTF-8 CSV files
- **Build:** Maven (`pom.xml`, main class `com.motorph.ui.PayrollPortalMain`); also runnable with plain `javac`

---

## Project Structure

```
src/main/java/com/motorph/
├── model/
│   ├── Attendance.java
│   ├── Employee.java
│   └── PayrollRecord.java
├── manager/
│   ├── AttendanceManager.java
│   ├── CsvUtil.java
│   ├── EmployeeFormService.java
│   ├── EmployeeManager.java
│   ├── PayrollCalculator.java
│   ├── PayrollComputationResult.java
│   ├── PayrollRecordManager.java
│   └── PayrollService.java
└── ui/
    ├── ClipboardHelper.java
    ├── PayrollPortalMain.java
    ├── PayrollPortalMain.form
    ├── PayrollUIHelper.java
    └── TableRenderHelper.java
```

**Design:** Models hold data. Managers handle persistence and file I/O. Services orchestrate business logic. The UI class handles display and events only. `PayrollUIHelper` centralizes the theme and GroupLayout arrangements so the form class stays focused on wiring; `TableRenderHelper` and `ClipboardHelper` provide table rendering and copy-to-clipboard support.

---

## Getting Started

### Prerequisites
- JDK 17+
- NetBeans (recommended) or Maven

### Run in NetBeans
1. Open the project folder
2. Right-click → **Run**

### Run from Command Line
```bash
javac --release 17 -d target/classes $(find src/main/java -name "*.java")
java -cp target/classes com.motorph.ui.PayrollPortalMain
```

> Run from the project root so the CSV files are found in the working directory.

---

## How to Use

### Employee Form
1. Enter Employee ID → name auto-populates
2. Enter Pay Coverage. Format: `Month D-D, Year` (e.g., `June 1-30, 2024`). The dates must overlap entries in `Attendance.csv`, otherwise hours worked default to 0.
3. Optionally enter Manual Hours to override attendance
4. Click **Compute Payroll**

### Submitted Records
- **Shift+Click** or **Ctrl/Cmd+Click** to select multiple rows
- **Delete Selected** removes highlighted records
- **Clear All Records** wipes the entire history

### Employee Database
- Search by name or ID
- Filter by status (All, Regular, Probationary)
- Add, edit, or delete employees

---

## Data Files

| File | Purpose |
|---|---|
| `Attendance.csv` | Time-in/time-out logs (the sample data is dated 2024) |
| `EmployeeDatabase.csv` | Employee master data |
| `PayrollRecords.csv` | Payroll history |

Files are stored in the working directory. Back them up before clearing records.

---

## Notes

- Attendance deducts a 60-minute lunch break when a shift is 5 hours (300 minutes) or longer.
- When adding/editing an employee, entering **Basic Salary** auto-derives **Hourly Rate** as `Basic Salary / (22 days × 8 hours = 176)`; entering **Hourly Rate** derives Basic Salary the same way. At least one is required.
- Deductions use simplified formulas for academic scope.
- No undo/redo — deletes are immediate.
- Pay Coverage dates must match `Attendance.csv`. The sample data uses 2024 dates, so use a 2024 coverage (e.g., `June 1-30, 2024`) to see non-zero attendance hours.

---

*Built by Timothy Justin Sonido Gacula*
