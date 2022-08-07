/* Declan Kelly - G00378925 */

import java.io.*; // PrintWriter File
import java.net.*; // SocketServer Socket
import java.text.*; // SimpleDateFormat Date
import java.util.*; // Scanner Calendar

class Member { // Properties of the Member structure
    private String name;
    private int age;
    private int playerID;
    private String clubID;
    private String dateLastUsed;
    private float membershipFee;
    private int memberType;
    private int paymentStatus;

    public static final int ADULT = 1;
    public static final int SENIOR = 2;
    public static final int JUNIOR = 3;

    public static final int PAID = 1;
    public static final int PART_PAID = 2;
    public static final int NOT_PAID = 3;

    public Member(String name, int age, int playerID, String clubID, String dateLastUsed, float fee, int memberType, int paymentStatus) {
        this.name = name; this.age = age; this.playerID = playerID; this.clubID = clubID;
        this.dateLastUsed = dateLastUsed; this.membershipFee = fee; this.memberType = memberType; this.paymentStatus = paymentStatus;
    }

    public Member(String input) { // Parsing records stored in the text file
        String[] inputArray = input.split(" ");
        this.name = inputArray[0].replace("%20", " "); // Full names have space, so %20 denotes this
        this.age = Integer.parseInt(inputArray[1]);
        this.playerID = Integer.parseInt(inputArray[2]);
        this.clubID = inputArray[3];
        this.dateLastUsed = inputArray[4];
        this.membershipFee = Float.parseFloat(inputArray[5]);
        this.memberType = Integer.parseInt(inputArray[6]);
        this.paymentStatus = Integer.parseInt(inputArray[7]);
    }

    // Getter and Setter methods
    public int getPlayerID() { return this.playerID; }

    public String getClubID() { return this.clubID; }
    public void setClubID(String clubID) { this.clubID = clubID; }

    public void setMemberType(int memberType) { this.memberType = memberType; }
    public void setMembershipFee(float membershipFee) { this.membershipFee = membershipFee; }
    public void setPaymentStatus(int paymentStatus) { this.paymentStatus = paymentStatus; }

    public int getPaymentStatus() { return this.paymentStatus; }

    public String getDateLastUsed() { return this.dateLastUsed; }

    public String toString() { // toString returns the format Member record will be stored in the local file
        return String.format("%s %d %d %s %s %.2f %d %d", name.replace(" ", "%20"), age, playerID, clubID, dateLastUsed, membershipFee, memberType, paymentStatus);
    }

    private String getMembertypeString(int mType) {
        switch (mType) {
            case ADULT: return "Adult";
            case SENIOR: return "Senior";
            case JUNIOR: return "Junior";
        }
        return new String();
    }

    private String getPaymentStatusString(int pStatus) {
        switch (pStatus) {
            case PAID: return "Paid";
            case PART_PAID: return "Part Paid";
            case NOT_PAID: return "Not Paid";
        } 
        return new String();
    }

    public String getRecord() {
        return String.format("%s, %d, %d, %s, %s, %.2f, %s, %s", name, age, playerID, clubID, dateLastUsed, membershipFee, getMembertypeString(memberType), getPaymentStatusString(paymentStatus));
    }
}

class Club { // Properties of the Club structure
    private String clubName;
    private String clubID;
    private String email;

    public Club(String input) { // Parsing records stored in the text file
        String[] inputArray = input.split(" ");
        this.clubName = inputArray[0];
        this.clubID = inputArray[1];
        this.email = inputArray[2];
    }

    public Club(String clubName, String clubID, String email) {
        this.clubName = clubName;
        this.clubID = clubID;
        this.email = email;
    }

    // Getter and Setter methods
    public String getClubName() { return clubName; }
    public String getClubID() { return clubID; }
    public String getEmail() { return email; }

    public String toString() { // toString returns the format Club records will be stored in the local file
        return String.format("%s %s %s", clubName, clubID, email);
    }
}

public class Project {
    // Constants for the Project class
    private static final String CLUB_FILE = "clubFile.txt";
    private static final String MEMBER_FILE = "memberFile.txt";
    private static final int PORT_NUMBER = 8000; // Using port 8000

    public static Scanner console = new Scanner(System.in);

    public static ArrayList<Club> clubsList = new ArrayList<Club>();
    public static ArrayList<Member> memberList = new ArrayList<Member>();

    private static synchronized void saveToFile() throws FileNotFoundException {
        File clubFile = new File(CLUB_FILE); // Open file descriptors for clubFile.txt and memberFile.txt
        File memberFile = new File(MEMBER_FILE);
        PrintWriter clubWriter = new PrintWriter(clubFile);
        PrintWriter memberWriter = new PrintWriter(memberFile);

        // Loop through all elements in the ArrayList save to text file
        for (Club c : clubsList) { clubWriter.println(c.toString()); }
        for (Member m : memberList) { memberWriter.println(m.toString()); }
        clubWriter.close();
        memberWriter.close();
    }

    private static void loadFromFile() throws FileNotFoundException {
        // Load records from two files using the Scanner class
        Scanner clubScanner = new Scanner(new File(CLUB_FILE));
        Scanner memberScanner = new Scanner(new File(MEMBER_FILE));

        while (clubScanner.hasNextLine())
            clubsList.add(new Club(clubScanner.nextLine()));

        while (memberScanner.hasNextLine())
            memberList.add(new Member(memberScanner.nextLine()));
    }

    public static void runServer() throws IOException {
        // Load backup files
        try { loadFromFile(); } catch (Exception e) { System.err.println(e); }

        ServerSocket ss = new ServerSocket(PORT_NUMBER);

        while (true) {
            var socket = ss.accept();
            new Thread() {
                private Club loggedIn;
                private PrintWriter output;
                private Scanner input;

                // Asks the client for input
                private String getInput(String prompt) {
                    output.println("1" + prompt);
                    return input.nextLine().trim();
                }

                // Sends print a line message to the client
                private void println(String str) { output.println("2" + str); }

                private void hangUp() { output.println("3"); } // Sends hangup command to the client

                private Club createNewClub() {
                    String clubName, clubID, email;
                    
                    while (true) {
                        clubName = getInput("Club name: ");
                        clubID = getInput("Club ID: ");
                        email = getInput("Club email: ");
                        boolean findDuplicate = false;
                        for (Club c : clubsList) { //  The same business name and id can only be registered once)
                            if (c.getClubName().equals(clubName) || c.getClubID().equals(clubID))
                                findDuplicate = true;
                        }
                        if (!findDuplicate)
                            break;
                    }

                        // Create new Club object and call saveToFile method
                    Club newClub = new Club(clubName, clubID, email);
                    clubsList.add(newClub);
                    try {
                        saveToFile();
                    } catch (Exception err) {
                        System.err.println(err);
                    }
                    return newClub;
                }

                private Club login() {
                    if (clubsList.size() == 0) {
                        println("No clubs present, must create one");
                        return createNewClub();
                    } else {
                        while (true) {
                            println("Club login");

                            String clubName = getInput("Club Name: ");
                            String clubID = getInput("Club ID: ");

                            for (Club club : clubsList) {
                                if (club.getClubName().equals(clubName) && club.getClubID().equals(clubID))
                                    return club;   
                            } // If reach this point a matching Club was not found
                            println("Club not found!");
                        }
                    }
                }

                private void addNewMember() { // 1
                    String memberName = getInput("Member name: ");
                    int memberAge = Integer.parseInt(getInput("Member age: "));

                    int memberID = 0;
                    do { // Loop until we have a unique member ID
                        memberID = (int) Math.floor(Math.random() * 1000);
                        boolean clashingID = false;
                        for (Member m : memberList) { // Loop through all the Members in the System
                            if (m.getPlayerID() == memberID)
                                clashingID = true;
                        }
                        if (!clashingID) break;
                    } while (true);
                    String clubID = loggedIn.getClubID();
                    String dateLastUsed = getInput("Date last used: ");
                    float fee = Float.parseFloat(getInput("Fee: "));
                    int memberType = Integer.parseInt(getInput("Member Type (1 Adult, 2 Senior, 3 Junior): "));
                    int paymentStatus = Integer.parseInt(getInput("Payment status (1 Paid, 2 Part Paid, 3 Not Paid): "));

                    memberList.add(new Member(memberName, memberAge, memberID, clubID, dateLastUsed, fee, memberType, paymentStatus));
                    try { saveToFile(); } catch (Exception e) { System.err.println(e); }
                }

                private void updateMemberType() { // 2
                    int playerID = Integer.parseInt(getInput("Enter player ID: "));
                    int memberType = Integer.parseInt(getInput("Member Type (1 Adult, 2 Senior, 3 Junior): "));
                    float fee = Float.parseFloat(getInput("Fee: "));
                    
                    for (Member m : memberList) { // Find the matching player ID and update the memberType property
                        if (m.getPlayerID() == playerID) {
                            m.setMemberType(memberType);
                            m.setMembershipFee(fee);
                        }
                    } // Save changes to the file
                    try { saveToFile(); } catch (Exception e) { System.err.println(e); }
                }

                private void updateMemberPayments() { // 3
                    int playerID = Integer.parseInt(getInput("Enter player ID: "));
                    int paymentStatus = Integer.parseInt(getInput("Payment status (1 Paid, 2 Part Paid, 3 Not Paid): "));

                    for (Member m : memberList) {
                        if (m.getPlayerID() == playerID) {
                            m.setPaymentStatus(paymentStatus);
                        }
                    }
                    try { saveToFile(); } catch (Exception e) { System.err.println(e); }
                }

                private void searchMembers14Days() { // 4
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                    for (Member m : memberList) {
                        Calendar playerCal = Calendar.getInstance();
                        Date d = sdf.parse(m.getDateLastUsed(), new ParsePosition(0));
                        playerCal.setTime(d); // Convert date format to Calendar, convert milliseconds, substract from current time

                        // 14 days in milliseconds
                        long _14Days = 60_0000 * 60 * 60 * 24 * 14;
                        if (m.getClubID() == loggedIn.getClubID() && (playerCal.getTimeInMillis() - Calendar.getInstance().getTimeInMillis()) < _14Days) {
                            println(m.getRecord());
                        }
                    }
                    println("");
                }

                private void searchMembersPaid() { // 5
                    for (Member m : memberList) {
                        if (m.getPaymentStatus() == Member.PAID)
                            println(m.getRecord());
                    }
                    println("");
                }

                private synchronized void removeMemberFromClub() { // 6
                    int playerID = Integer.parseInt(getInput("Enter player ID: "));
                    for (Member m : memberList) { // Find matching playerID and remove from ArrayList and update file
                        if (m.getPlayerID() == playerID && m.getClubID().equals(loggedIn.getClubID())) {
                            memberList.remove(m);
                            break;
                        }
                    }
                    try { saveToFile(); } catch (Exception e) { System.err.println(e); }
                }

                private void searchAllClubs() { // 7
                    String clubID = getInput("Enter club ID: ");
                    for (Club c : clubsList) {
                        if (c.getClubID().equals(clubID))
                            println(c.toString()); // Print information about club
                    }
                    println("");
                }

                private void moveMemberToClub() { // 8
                    int playerID = Integer.parseInt(getInput("Enter player ID: "));
                    String clubID = getInput("Enter new club ID: ");
                    for (Member m : memberList) {
                        if (m.getPlayerID() == playerID && m.getPaymentStatus() == Member.PAID)
                            m.setClubID(clubID);
                    }
                    try { saveToFile(); } catch (Exception e) { System.err.println(e); }
                }

                private void listAllInClub() { // 10
                    for (Member m : memberList) {
                        if (m.getClubID().equals(loggedIn.getClubID()))
                            println(m.getRecord());
                    }
                    println("");
                }

                public void run() {
                    try {
                        input = new Scanner(socket.getInputStream());
                        output = new PrintWriter(socket.getOutputStream(), true);
                        this.loggedIn = login();

                        while (true) { // Menu options for Club
                            println("1) Add a new member");
                            println("2) Update the members membership type and fee");
                            println("3) Update the player's payments status");
                            println("4) Search for all members who have visited the club in the last 14 days");
                            println("5) Search for all members who paid their membership fee");
                            println("6) Remove a member in their club");
                            println("7) Search all clubs registered on the system");
                            println("8) Move a member to another Club");
                            println("9) Add new Club");
                            println("10) List all members in club");
                            println("11) Logout");
                            int option = Integer.parseInt(getInput("Option> "));
                            switch (option) {
                                case 1 -> addNewMember();
                                case 2 -> updateMemberType();
                                case 3 -> updateMemberPayments();
                                case 4 -> searchMembers14Days();
                                case 5 -> searchMembersPaid();
                                case 6 -> removeMemberFromClub();
                                case 7 -> searchAllClubs();
                                case 8 -> moveMemberToClub();
                                case 9 -> createNewClub();
                                case 10 -> listAllInClub();
                                case 11 -> {hangUp(); break;} // Breaks loop, run() returns killing Thread
                            }
                        }
                    } catch (Exception e) {
                        System.err.println(e);
                    }
                }
            }.start();
        }
    }

    public static void runClient() throws IOException {
        Socket s = new Socket("0.0.0.0", PORT_NUMBER);
        PrintWriter pw = new PrintWriter(s.getOutputStream(), true);
        Scanner reader = new Scanner(s.getInputStream());

        while (true) { // Retreive Data from the server
            String read = reader.nextLine();
            if (read.charAt(0) == '1') { // First character is the command type
                System.out.print(read.substring(1));
                pw.println(console.nextLine());
            } else if (read.charAt(0) == '2') {
                System.out.print(read.substring(1) + "\n");
            } else if (read.charAt(0) == '3') {
                System.exit(0);
            }
        }

    }

    public static void main(String[] args) throws IOException {
        while (true) {  // Menu asking user if want to run Server or Client
            System.out.print("1) Server, 2) Client: ");
            String choice = console.nextLine();        

            if (choice.equals("1")) {
                runServer();
            } else if (choice.equals("2")) {
                runClient();
            }
            System.err.println(String.format("'%s' is not a valid option", choice));
        }

    }
}
