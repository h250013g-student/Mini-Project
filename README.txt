LIBRARY MANAGEMENT SYSTEM — MINI-PROJECT

Requirements
- Java 17 or newer (the program uses switch expressions).
- No external libraries are needed.

Run on Windows
1. Extract the ZIP.
2. Open a terminal in the extracted project folder.
3. Compile:
   javac -d out src\LibraryManagementSystem.java
4. Run:
   java -cp out LibraryManagementSystem

Run on Linux/macOS
   javac -d out src/LibraryManagementSystem.java
   java -cp out LibraryManagementSystem

The program creates a data folder and saves items.csv, members.csv, and loans.csv when you choose 0 (Save and exit). On the next run, saved data is loaded automatically.

Menu
1 Add a Book or DVD
2 Register a member
3 Borrow an item
4 Return an item
5 List overdue items
6 Search by title
7 List all items
8 List members
9 List active loans
0 Save and exit

Rules
- Book loan period: 14 days.
- DVD loan period: 7 days.
- Each member can have at most 3 active loans.
- IDs must be unique.
- A borrowed item cannot be borrowed by another member until returned.

Project files
- src/LibraryManagementSystem.java — complete runnable application and supporting classes
- docs/Mini_Project_Report.docx — short project report
- docs/Class_Diagram.mmd — editable Mermaid class diagram source
- docs/Class_Diagram.png — class diagram image (if generated)

Design note
To keep the project easy to run for a beginner, all Java classes are in one source file. They remain separate classes and interfaces, and Java compiles them into separate .class files.
