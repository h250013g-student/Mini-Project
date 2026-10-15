import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;


public class LibraryManagementSystem {
    public static void main(String[] args) {
        Library library = new Library(Path.of("data"));
        try {
            library.load();
            System.out.println("Saved library data loaded.");
        } catch (IOException | RuntimeException e) {
            System.out.println("Some saved data could not be loaded: " + e.getMessage());
        }
        try (Scanner input = new Scanner(System.in)) {
            boolean running = true;
            while (running) {
                menu();
                System.out.print("Choose: ");
                String choice = input.nextLine().trim();
                try {
                    switch (choice) {
                        case "1" -> {
                            System.out.print("Type (book/dvd): ");
                            String type = input.nextLine().trim();
                            System.out.print("Item ID: ");
                            String id = input.nextLine().trim();
                            System.out.print("Title: ");
                            String title = input.nextLine().trim();
                            LibraryItem item;
                            if (type.equalsIgnoreCase("book")) item = new Book(id, title);
                            else if (type.equalsIgnoreCase("dvd")) item = new Dvd(id, title);
                            else throw new IllegalArgumentException("Type must be book or dvd.");
                            library.addItem(item);
                            System.out.println("Added. Loan period: " + item.loanPeriodDays() + " days.");
                        }
                        case "2" -> {
                            System.out.print("Member ID: ");
                            String id = input.nextLine().trim();
                            System.out.print("Member name: ");
                            String name = input.nextLine().trim();
                            library.registerMember(new Member(id, name));
                            System.out.println("Member registered.");
                        }
                        case "3" -> {
                            System.out.print("Member ID: ");
                            String memberId = input.nextLine().trim();
                            System.out.print("Item ID: ");
                            String itemId = input.nextLine().trim();
                            library.borrow(memberId, itemId);
                            System.out.println("Item borrowed successfully.");
                        }
                        case "4" -> {
                            System.out.print("Item ID to return: ");
                            library.giveBack(input.nextLine().trim());
                            System.out.println("Item returned successfully.");
                        }
                        case "5" -> printItems(library.listOverdueItems());
                        case "6" -> {
                            System.out.print("Title or part of title: ");
                            printItems(library.searchByTitle(input.nextLine()));
                        }
                        case "7" -> printItems(new ArrayList<>(library.getItems().values()));
                        case "8" -> {
                            if (library.getMembers().isEmpty()) System.out.println("No members registered.");
                            else library.getMembers().values().forEach(System.out::println);
                        }
                        case "9" -> {
                            if (library.getActiveLoans().isEmpty()) System.out.println("No active loans.");
                            else library.getActiveLoans().forEach(System.out::println);
                        }
                        case "0" -> running = false;
                        default -> System.out.println("Choose a number from 0 to 9.");
                    }
                } catch (ItemNotAvailableException | MemberLimitExceededException |
                         IllegalArgumentException e) {
                    System.out.println("Request could not be completed: " + e.getMessage());
                }
                System.out.println();
            }
        }
        try {
            library.save();
            System.out.println("Data saved. Goodbye!");
        } catch (IOException e) {
            System.out.println("Warning: could not save data: " + e.getMessage());
        }
    }

    private static void menu() {
        System.out.println("=== LIBRARY MANAGEMENT SYSTEM ===");
        System.out.println("1. Add item (Book/DVD)");
        System.out.println("2. Register member");
        System.out.println("3. Borrow item");
        System.out.println("4. Return item");
        System.out.println("5. List overdue items");
        System.out.println("6. Search by title");
        System.out.println("7. List all items");
        System.out.println("8. List members");
        System.out.println("9. List active loans");
        System.out.println("0. Save and exit");
    }

    private static void printItems(List<LibraryItem> items) {
        if (items.isEmpty()) System.out.println("No matching items found.");
        else items.forEach(System.out::println);
    }
}

// Abstraction: common information and behaviour for all library items.
abstract class LibraryItem implements Borrowable {
    private final String id;
    private final String title;
    private boolean available = true;
    private String borrowerId;
    private LocalDate dueDate;

    protected LibraryItem(String id, String title) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Item ID is required.");
        if (title == null || title.isBlank()) throw new IllegalArgumentException("Title is required.");
        this.id = id.trim();
        this.title = title.trim();
    }
    public abstract int loanPeriodDays();
    public abstract String getType();
    public String getId() { return id; }
    public String getTitle() { return title; }
    public boolean isAvailable() { return available; }
    public String getBorrowerId() { return borrowerId; }
    public LocalDate getDueDate() { return dueDate; }

    void markBorrowed(String memberId, LocalDate due) {
        available = false; borrowerId = memberId; dueDate = due;
    }
    void markReturned() {
        available = true; borrowerId = null; dueDate = null;
    }

    // EXPLAIN: Library handles borrowing so that item and member records stay consistent.
    @Override public void borrow(Member member) throws ItemNotAvailableException, MemberLimitExceededException {
        throw new UnsupportedOperationException("Use the Library service to borrow items.");
    }
    @Override public void giveBack() {
        throw new UnsupportedOperationException("Use the Library service to return items.");
    }
    @Override public String toString() {
        return "[" + getType() + "] " + id + " - " + title + " | " +
            (available ? "Available" : "On loan; due " + dueDate);
    }
}

class Book extends LibraryItem {
    Book(String id, String title) { super(id, title); }
    @Override public int loanPeriodDays() { return 14; }
    @Override public String getType() { return "Book"; }
}
class Dvd extends LibraryItem {
    Dvd(String id, String title) { super(id, title); }
    @Override public int loanPeriodDays() { return 7; }
    @Override public String getType() { return "DVD"; }
}

interface Borrowable {
    void borrow(Member member) throws ItemNotAvailableException, MemberLimitExceededException;
    void giveBack();
}
// EXPLAIN: Member class tracks loans and enforces a maximum of 3 items per member.
class Member {
    private final String id;
    private final String name;
    private final List<String> loanedItemIds = new ArrayList<>();

    Member(String id, String name) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Member ID is required.");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Member name is required.");
        this.id = id.trim(); this.name = name.trim();
    }
    String getId() { return id; }
    String getName() { return name; }
    List<String> getLoanedItemIds() { return Collections.unmodifiableList(loanedItemIds); }
    void addLoan(String itemId) { loanedItemIds.add(itemId); }
    void removeLoan(String itemId) { loanedItemIds.remove(itemId); }
    @Override public String toString() {
        return "Member " + id + " - " + name + " | Current loans: " + loanedItemIds.size() + "/3";
    }
}
// EXPLAIN: Loan class records the details of a borrowed item, including the member who borrowed it and the due date.
class Loan {
    private final String itemId;
    private final String memberId;
    private final LocalDate borrowedOn;
    private final LocalDate dueDate;

    Loan(String itemId, String memberId, LocalDate borrowedOn, LocalDate dueDate) {
        if (itemId == null || memberId == null || borrowedOn == null || dueDate == null ||
            dueDate.isBefore(borrowedOn)) throw new IllegalArgumentException("Invalid loan details.");
        this.itemId = itemId; this.memberId = memberId;
        this.borrowedOn = borrowedOn; this.dueDate = dueDate;
    }
    String getItemId() { return itemId; }
    String getMemberId() { return memberId; }
    LocalDate getBorrowedOn() { return borrowedOn; }
    LocalDate getDueDate() { return dueDate; }
    @Override public String toString() {
        return "Item " + itemId + " borrowed by member " + memberId + " on " +
            borrowedOn + ", due " + dueDate;
    }
}
// EXPLAIN: Custom exceptions for specific error conditions in the library system.
class ItemNotAvailableException extends Exception {
    ItemNotAvailableException(String message) { super(message); }
}
// EXPLAIN: Custom exception for when a member exceeds the borrowing limit.
class MemberLimitExceededException extends Exception {
    MemberLimitExceededException(String message) { super(message); }
}
// EXPLAIN: Library class manages items, members, and loans, enforcing borrowing rules and handling persistence.
class Library {
    private static final int MAX_LOANS = 3;
    private final Map<String, LibraryItem> items = new LinkedHashMap<>();
    private final Map<String, Member> members = new LinkedHashMap<>();
    private final Map<String, Loan> activeLoans = new LinkedHashMap<>();
    private final Path dataDirectory;

    Library(Path dataDirectory) { this.dataDirectory = dataDirectory; }
    Map<String, LibraryItem> getItems() { return Collections.unmodifiableMap(items); }
    Map<String, Member> getMembers() { return Collections.unmodifiableMap(members); }
    List<Loan> getActiveLoans() { return new ArrayList<>(activeLoans.values()); }

    void addItem(LibraryItem item) {
        if (items.containsKey(item.getId())) throw new IllegalArgumentException("Item ID already exists.");
        items.put(item.getId(), item);
    }
    void registerMember(Member member) {
        if (members.containsKey(member.getId())) throw new IllegalArgumentException("Member ID already exists.");
        members.put(member.getId(), member);
    }

    void borrow(String memberId, String itemId)
            throws ItemNotAvailableException, MemberLimitExceededException {
        Member member = members.get(memberId);
        LibraryItem item = items.get(itemId);
        if (member == null) throw new IllegalArgumentException("Member ID not found.");
        if (item == null) throw new IllegalArgumentException("Item ID not found.");
        if (!item.isAvailable()) throw new ItemNotAvailableException("This item is already on loan.");
        if (member.getLoanedItemIds().size() >= MAX_LOANS)
            throw new MemberLimitExceededException("A member may borrow no more than 3 items.");
        LocalDate today = LocalDate.now();
        LocalDate due = today.plusDays(item.loanPeriodDays());
        item.markBorrowed(memberId, due);
        member.addLoan(itemId);
        activeLoans.put(itemId, new Loan(itemId, memberId, today, due));
    }

    void giveBack(String itemId) {
        LibraryItem item = items.get(itemId);
        if (item == null) throw new IllegalArgumentException("Item ID not found.");
        if (item.isAvailable()) throw new IllegalArgumentException("Item is not currently on loan.");
        Member member = members.get(item.getBorrowerId());
        if (member != null) member.removeLoan(itemId);
        item.markReturned();
        activeLoans.remove(itemId);
    }

    List<LibraryItem> listOverdueItems() {
        LocalDate today = LocalDate.now();
        return items.values().stream()
            .filter(i -> !i.isAvailable() && i.getDueDate() != null && i.getDueDate().isBefore(today))
            .sorted(Comparator.comparing(LibraryItem::getTitle)).collect(Collectors.toList());
    }

    // EXPLAIN: Streams filter titles without requiring a manual search loop.
    List<LibraryItem> searchByTitle(String text) {
        String query = text == null ? "" : text.trim().toLowerCase();
        return items.values().stream()
            .filter(i -> i.getTitle().toLowerCase().contains(query))
            .sorted(Comparator.comparing(LibraryItem::getTitle)).collect(Collectors.toList());
    }

    void save() throws IOException {
        Files.createDirectories(dataDirectory);
        List<String> itemLines = new ArrayList<>();
        for (LibraryItem i : items.values()) {
            itemLines.add(csv(i.getType()) + "," + csv(i.getId()) + "," + csv(i.getTitle()) + "," +
                i.isAvailable() + "," + csv(i.getBorrowerId() == null ? "" : i.getBorrowerId()) + "," +
                csv(i.getDueDate() == null ? "" : i.getDueDate().toString()));
        }
        Files.write(dataDirectory.resolve("items.csv"), itemLines, StandardCharsets.UTF_8);
        List<String> memberLines = new ArrayList<>();
        for (Member m : members.values()) memberLines.add(csv(m.getId()) + "," + csv(m.getName()));
        Files.write(dataDirectory.resolve("members.csv"), memberLines, StandardCharsets.UTF_8);
        List<String> loanLines = new ArrayList<>();
        for (Loan l : activeLoans.values()) loanLines.add(csv(l.getItemId()) + "," + csv(l.getMemberId()) +
            "," + l.getBorrowedOn() + "," + l.getDueDate());
        Files.write(dataDirectory.resolve("loans.csv"), loanLines, StandardCharsets.UTF_8);
    }

    void load() throws IOException {
        Files.createDirectories(dataDirectory);
        Path mf = dataDirectory.resolve("members.csv");
        if (Files.exists(mf)) for (String line : Files.readAllLines(mf, StandardCharsets.UTF_8)) {
            if (line.isBlank()) continue;
            List<String> c = parseCsv(line);
            if (c.size() >= 2) members.put(c.get(0), new Member(c.get(0), c.get(1)));
        }
        Path itf = dataDirectory.resolve("items.csv");
        if (Files.exists(itf)) for (String line : Files.readAllLines(itf, StandardCharsets.UTF_8)) {
            if (line.isBlank()) continue;
            List<String> c = parseCsv(line);
            if (c.size() < 6) continue;
            LibraryItem item = c.get(0).equalsIgnoreCase("DVD") ?
                new Dvd(c.get(1), c.get(2)) : new Book(c.get(1), c.get(2));
            items.put(item.getId(), item);
            if (!Boolean.parseBoolean(c.get(3)) && !c.get(4).isBlank() && !c.get(5).isBlank())
                item.markBorrowed(c.get(4), LocalDate.parse(c.get(5)));
        }
        Path lf = dataDirectory.resolve("loans.csv");
        if (Files.exists(lf)) for (String line : Files.readAllLines(lf, StandardCharsets.UTF_8)) {
            if (line.isBlank()) continue;
            List<String> c = parseCsv(line);
            if (c.size() < 4) continue;
            Loan loan = new Loan(c.get(0), c.get(1), LocalDate.parse(c.get(2)), LocalDate.parse(c.get(3)));
            activeLoans.put(loan.getItemId(), loan);
            Member member = members.get(loan.getMemberId());
            if (member != null && !member.getLoanedItemIds().contains(loan.getItemId()))
                member.addLoan(loan.getItemId());
        }
    }

    private static String csv(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
    private static List<String> parseCsv(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    field.append('"'); i++;
                } else quoted = !quoted;
            } else if (ch == ',' && !quoted) {
                result.add(field.toString()); field.setLength(0);
            } else field.append(ch);
        }
        result.add(field.toString());
        return result;
    }
}
