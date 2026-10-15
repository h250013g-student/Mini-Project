## H250013G LESLIE SARAI SOFTWARE ENGINEERING PART 2.1
# Mini-Project : Library Management System

A console-based library management application written in Java. It supports books and DVDs, member registration, borrowing and returns, title searches, overdue-item listings, and saving data between runs.


## Features and rules

- Add books and DVDs, and register members.
- Borrow an available item and return it by item ID.
- Search items by full or partial title; list all items, members, and active loans.
- List overdue items. An item is overdue when its due date is before the current date.
- Books have a 14-day loan period; DVDs have a 7-day loan period.
- A member may have up to 3 active loans.
- Item IDs and member IDs must be unique.
- An item already on loan cannot be borrowed again until it is returned.

## Saved data

The application creates a `data` folder in the project root. It loads existing data at startup and saves when you choose **0**:

- `data/items.csv` — items and their availability
- `data/members.csv` — registered members
- `data/loans.csv` — active loans

## Project files

- [src/LibraryManagementSystem.java](src/LibraryManagementSystem.java) — application and supporting classes
- [Class_Diagram.mmd](Class_Diagram.mmd) — editable Mermaid class diagram
- [Class_Diagram.dot](Class_Diagram.dot) — Graphviz class diagram source
- [Class_Diagram.png](Class_Diagram.png) — rendered class diagram
- [Mini_Project_Report.docx](Mini_Project_Report.docx) — project report

All Java classes are kept in one source file to simplify compiling and running the project. They are still separate classes and interfaces and compile into separate `.class` files.
