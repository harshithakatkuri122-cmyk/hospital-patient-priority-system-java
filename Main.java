import java.util.concurrent.atomic.AtomicInteger;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.Arrays;

// --- Patient Hierarchy (Model) ---

abstract class Patient implements Comparable<Patient> {
    private static final AtomicInteger ID_GENERATOR = new AtomicInteger(1000);

    private final int patientID;
    private final String name;
    private final int age;
    private final int severity;
    private final String caseType;
    private final int priority;

    Patient(String name, int age, int severity, String caseType) {
        this.patientID = ID_GENERATOR.incrementAndGet();
        this.name = name;
        this.age = age;
        this.severity = severity;
        this.caseType = caseType;
        this.priority = calculatePriority();
    }

    protected abstract int calculatePriority();

    @Override
    public int compareTo(Patient other) {
        // Higher priority value = treated earlier
        return Integer.compare(other.priority, this.priority);
    }

    public int getPatientID() { return patientID; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public int getSeverity() { return severity; }
    public int getPriority() { return priority; }
    public String getCaseType() { return caseType; }
    
    @Override
    public String toString() {
        return String.format("%s (ID: %d, Priority: %d, Case: %s)",
                             name, patientID, priority, caseType);
    }
}

class NormalPatient extends Patient {
    NormalPatient(String name, int age, int severity) {
        super(name, age, severity, "Normal");
    }

    @Override
    protected int calculatePriority() {
        // Example: severity has more weight than age
        return (getSeverity() * 10) + getAge();
    }
}

class EmergencyPatient extends Patient {
    EmergencyPatient(String name, int age, int severity) {
        super(name, age, severity, "EMERGENCY");
    }

    @Override
    protected int calculatePriority() {
        // Emergency gets extra weight on severity
        return (getSeverity() * 20) + getAge();
    }
}

// --- HospitalManager (Controller) ---

class HospitalManager {
    private final PriorityQueue<Patient> waitingQueue; 
    private final HashMap<Integer, Patient> allPatientsMap; 
    private final LinkedList<Patient> history; 

    HospitalManager() {
        this.waitingQueue = new PriorityQueue<>(); // Uses Patient.compareTo()
        this.allPatientsMap = new HashMap<>();
        this.history = new LinkedList<>();
    }

    public void addPatient(Patient patient) {
        waitingQueue.offer(patient); 
        allPatientsMap.put(patient.getPatientID(), patient);
        
        System.out.printf("ID: %d | Calculated Priority: %d (%s logic used)%n", 
                          patient.getPatientID(), patient.getPriority(), patient.getCaseType());
    }

    public Patient treatNextPatient() {
        if (waitingQueue.isEmpty()) {
            System.out.println("The waiting queue is currently empty.");
            return null;
        }
        
        Patient treatedPatient = waitingQueue.poll(); 
        history.add(treatedPatient); 
        
        return treatedPatient;
    }

    public Patient searchPatient(int id) {
        return allPatientsMap.get(id); 
    }
    
    public PriorityQueue<Patient> getWaitingQueue() {
        return waitingQueue;
    }

    public LinkedList<Patient> getHistory() {
        return history;
    }
}

// --- HospitalPriorityApp (View/Driver) ---

class HospitalPriorityApp {
    private final HospitalManager manager;
    private final Scanner scanner;

    public HospitalPriorityApp() {
        this.manager = new HospitalManager();
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        System.out.println("\n==== Hospital Patient Management System (OOP + DSA Focus) ====");
        int choice;
        do {
            displayMenu();
            if (scanner.hasNextInt()) {
                choice = scanner.nextInt();
                scanner.nextLine(); // consume newline
            } else {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine(); // clear invalid input
                choice = 0;
                continue;
            }

            switch (choice) {
                case 1: addPatientInteraction(); break;
                case 2: treatNextPatientInteraction(); break;
                case 3: viewWaitingQueueInteraction(); break;
                case 4: searchPatientInteraction(); break;
                case 5: viewHistoryInteraction(); break;
                case 6: System.out.println("Exiting System. Goodbye!"); break;
                default: System.out.println("Invalid choice. Please try again.");
            }
        } while (choice != 6);
    }

    private void displayMenu() {
        System.out.println("\n--- Menu ---");
        System.out.println("1. Add New Patient (Admission)");
        System.out.println("2. Treat Next Patient (Dequeue)");
        System.out.println("3. View Current Waiting Queue");
        System.out.println("4. Search Patient by ID");
        System.out.println("5. View Treatment History");
        System.out.print("6. Exit\nEnter choice: ");
    }

    private void addPatientInteraction() {
        System.out.print("Enter Patient Name: ");
        String name = scanner.nextLine();
        
        System.out.print("Enter Age: ");
        int age = getIntInput();
        
        System.out.print("Enter Severity (1-10): ");
        int severity = getIntInput();
        
        System.out.print("Is this an Emergency Case? (yes/no): ");
        String emergencyStatus = scanner.nextLine().trim().toLowerCase();

        Patient newPatient;
        if (emergencyStatus.equals("yes")) {
            newPatient = new EmergencyPatient(name, age, severity);
        } else {
            newPatient = new NormalPatient(name, age, severity);
        }
        
        manager.addPatient(newPatient);
    }

    private void treatNextPatientInteraction() {
        System.out.println("Treating Patient...");
        Patient treated = manager.treatNextPatient();
        if (treated != null) {
            System.out.printf("Treated: %s | Priority: %d | Case: %s%n", 
                              treated.getName(), treated.getPriority(), treated.getCaseType());
        }
    }

    private void viewWaitingQueueInteraction() {
        System.out.println("--- Current Waiting Queue (Highest Priority First) ---");
        if (manager.getWaitingQueue().isEmpty()) {
            System.out.println("Queue is empty.");
        } else {
            Patient[] patients = manager.getWaitingQueue().toArray(new Patient[0]);
            // Use natural ordering defined by Patient.compareTo()
            Arrays.sort(patients);
            
            for (int i = 0; i < patients.length; i++) {
                System.out.printf("%d. %s%n", i + 1, patients[i].toString());
            }
        }
        System.out.println("-----------------------------------------------------");
    }

    private void searchPatientInteraction() {
        System.out.print("Enter Patient ID to search: ");
        int id = getIntInput();
            
        Patient foundPatient = manager.searchPatient(id);
        if (foundPatient != null) {
            boolean isTreated = manager.getHistory().contains(foundPatient);
            String status = isTreated ? "DISCHARGED" : "WAITING";
            System.out.printf("Patient Found: %s | Status: %s%n", 
                              foundPatient.getName(), status);
        } else {
            System.out.println("Patient with ID " + id + " not found.");
        }
    }

    private void viewHistoryInteraction() {
        System.out.println("--- Treatment History (Chronological) ---");
        if (manager.getHistory().isEmpty()) {
            System.out.println("No patients have been treated yet.");
        } else {
            for (int i = 0; i < manager.getHistory().size(); i++) {
                System.out.printf("%d. %s%n", i + 1, manager.getHistory().get(i).toString());
            }
        }
        System.out.println("-----------------------------------------");
    }

    private int getIntInput() {
        while (!scanner.hasNextInt()) {
            System.out.println("Invalid input. Please enter a number.");
            scanner.nextLine();
            System.out.print("Enter value: ");
        }
        int value = scanner.nextInt();
        scanner.nextLine(); // consume newline
        return value;
    }
}

// --- Main Class for Online Compilers ---

public class Main {
    public static void main(String[] args) {
        new HospitalPriorityApp().start();
    }
}