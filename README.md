# MotorPH Payroll System

## Description
The MotorPH Payroll System is a Java Swing desktop application designed to manage employee records and compute payroll using CSV-based data storage. It allows the user to load employee data, compute payroll based on attendance or manual hours, save payroll records, and manage employee information through a graphical user interface.

## Technologies Used
- Java
- Java Swing GUI
- CSV file handling using `BufferedReader` and `BufferedWriter`

## Project Structure
- `PayrollPortalMain` – main GUI class of the application.
- `AttendanceManager` – handles attendance-based hour computation. 
- `PayrollCalculator` – handles payroll calculations such as gross pay, deductions, and net pay. 
- `PayrollRecordManager` – handles loading, saving, and deleting payroll records. 
- `Employee` – employee model used by the application. 
- `PayrollRecord` – payroll record model for saved computations. 

## Required Files
Make sure these CSV files are placed in the project root folder:
- `EmployeeDatabase.csv`
- `Attendance.csv`
- `PayrollRecords.csv`

## Author
# MO-IT103-H1101-Timothy-Justin-Sonido-Gacula
