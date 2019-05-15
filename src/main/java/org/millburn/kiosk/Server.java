package org.millburn.kiosk;

import org.millburn.kiosk.db.Database;
import org.millburn.kiosk.db.SQLFuture;
import org.millburn.kiosk.db.SQLResult;
import org.millburn.kiosk.exception.InvalidServerStateException;
import org.millburn.kiosk.http.StudentLogPair;
import org.millburn.kiosk.logging.Logger;
import org.millburn.kiosk.util.Divider;
import org.millburn.kiosk.util.FileUtil;
import org.millburn.kiosk.websocket.SocketHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.Instant;
import java.time.Period;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@SpringBootApplication
public class Server {
    private static Logger logger = new Logger();
    private static Server server;

    private Database database;
    private State state = State.INITIALIZING;
    private boolean test = false;

    private SocketHandler handler;

    public Server() {
        server = this;
        try {
            loadConfigs("cfg");
/*
            database = Database.getDatabase(Configuration.get("Address") + "/" + Configuration.get("DatabaseName"),
                    Configuration.get("User"), Configuration.get("Password"));

            //Executor.every(Period.ofWeeks(1), this::repopulate);
            Executor.every(Period.ofDays(1), this::checkAndClearViolators);

            Transaction.currentId = getAllTransactions()
                    .stream()
                    .mapToInt(t -> (int) t.getTransaction())
                    .max().orElse(0);*/

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void initialize() {
        Executor.initialize();
        SpringApplication.run(Server.class);
    }

    /**
     * Sets the socket handler for the WebSocket connections for the client <br>
     *     This instance is used for the login request transfer to the guard view
     * @param handler
     * @return
     */
    public SocketHandler setSocketHandler(SocketHandler handler){
        return this.handler = handler;
    }

    /**
     * Gets the socket handler for the WebSocket connections for the client <br>
     *     This instance is used for the login request transfer to the guard view
     * @return
     */
    public SocketHandler getSocketHandler(){
        return handler;
    }

    /**
     * Loads the configuration files from the given path, and override any copies in memory <br>
     *     It searches up to 10 directories deep from the home directory
     * @param path Path to load configuration files from
     * @throws IOException If a file fails to load as a configuration file
     */
    private static void loadConfigs(String path) throws IOException {
        var allconfigs = Files.find(Paths.get(path), 10, (p, bi) -> bi.isRegularFile())
                .map(Path::toFile)
                .filter(File::canRead)
                .filter(File::isFile)
                .filter(f -> f.getName().endsWith(".ini"))
                .collect(Collectors.toList());

        for (var config : allconfigs) {
            try {
                Configuration.load(config);
            } catch (IOException e) {
                logger.warn("Failed to load configuration file at " + config.getAbsolutePath());
            }
        }
    }

    /**
     * Gets the current server instance
     * @return Current server
     * @throws InvalidServerStateException Throws if the server is not instantiated on calling this metnod
     */
    public static Server getCurrent() {
        if (server == null) throw new InvalidServerStateException();
        return server;
    }

    /**
     * Loads all transactions from the database, sorted in insertion order
     * @return
     */
    public List<LogEvent> getAllTransactions(){
        return this.getDatabase().query("SELECT * FROM `timelogs`;").getResults().stream()
                .map(LogEvent::new)
                .collect(Collectors.toList());
    }

    /**
     * Loads all transactions marked as violations in the timelogs.violations table
     * @return
     */
    public List<LogEvent> getAllViolations(){
        return this.getDatabase().query("SELECT * FROM `timelogs` INNER JOIN `violations` ON `timelogs`.transaction = `violations`.transaction;").getResults().stream()
                .map(LogEvent::new)
                .collect(Collectors.toList());
    }

    /**
     * Gets all transactions since the given transaction ID, exclusive
     * @param transaction Transaction ID to load from
     * @return
     */
    public List<LogEvent> getTransactionsSince(long transaction){
        return this.getDatabase().query("SELECT * FROM `timelogs` WHERE `transaction`>" + transaction + ";").getResults().stream()
                .map(LogEvent::new)
                .collect(Collectors.toList());
    }

    /**
     * Gets all transactions within the last day <br>
     *     Note, this is not within the current day, instead from the current time a calendar day ago
     * @return
     */
    public List<LogEvent> getTransactionsToday(){
        return this.getDatabase().query("SELECT * FROM kiosk.`timelogs`;").getResults().stream()
                .map(LogEvent::new)
                .filter(e -> e.date.plus(Period.ofDays(1)).isAfter(Instant.now()))
                .collect(Collectors.toList());
    }

    /**
     * Gets all transactions flagged as invalid
     * @return
     */
    public List<LogEvent> getFlaggedTransactions(){
        return this.getDatabase().query("SELECT * FROM kiosk.`timelogs`WHERE `valid`=0;").getResults().stream()
                .map(LogEvent::new)
                .collect(Collectors.toList());
    }

    /**
     * Gets a specific transaction indexed by ID
     * @param id ID of transaction
     * @return Optional of transaction
     */
    public Optional<LogEvent> getTransaction(long id){
        return this.getDatabase().query("SELECT * FROM kiosk.`timelogs` WHERE `transaction`=" + id + ";").getResults().stream()
                .map(LogEvent::new)
                .findFirst();
    }

    /**
     * Gets the latest transaction ID for the given student ID
     * @param id ID of student
     * @return ID of latest transation for the student
     */
    public long getLatestTransactionFor(int id){
        try {
            var list = getDatabase().query("SELECT * FROM kiosk.timelogs WHERE `id`=" + id + ";").getResults()
                    .stream().collect(Collectors.toList());
            return list.get(list.size()-1).getLong("transaction");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the given student by ID
     * @param id
     * @return
     */
    public Optional<Student> getStudent(int id){
        return getDatabase().query("SELECT * FROM kiosk.allstudents WHERE `id`=" + id + ";").getResults()
                .stream()
                .map(Student::new)
                .findFirst();
    }

    public void deleteStudent(int id){
        this.getDatabase().query("DELETE FROM kiosk.allstudents WHERE `id`=" + id + ";");
    }

    /**
     * Gets every non-invalidated student
     * @return
     */
    public List<Student> getAllStudents(){
        return getDatabase().query("SELECT * FROM kiosk.allstudents").getResults()
                .getRows()
                .stream()
                .map(Student::new)
                .collect(Collectors.toList());
    }

    /**
     * Gets all student edits <br>
     *     This includes any student creations or deletions
     * @return
     */
    public List<Edit> getAllEdits(){
        return getDatabase().query("SELECT * FROM kiosk.editlog").getResults()
                .getRows()
                .stream()
                .map(Edit::new)
                .collect(Collectors.toList());
    }

    /**
     * Gets the latest log event for every student marked out as of the last calendar day
     * @return
     */
    public List<LogEvent> getStudentsOut(){
        return this.getTransactionsToday().stream()
                .collect(Collector.of(
                        Divider::new,
                        ((Divider<Integer, LogEvent> container, LogEvent event) -> container.add(event.getId(), event)),
                        Divider::addAll))
                .getMap().values().stream()
                .filter(s -> s.size() % 2 != 0)
                .map(s -> s.get(s.size() - 1))
                .collect(Collectors.toList());
    }

    /**
     * Checks for edits in the file named in FlatFileName, and loads new data from it if the checksum doesn't match
     */
    public void repopulate() {
        try {

            String checksum = FileUtil.getMD5Checksum(Configuration.get("FlatFileName"));
            if(checksum.equals(Configuration.get("LastChecksum")))
                return;

            logger.info("Found change in flat file, uploading new users to database");

            Configuration.set("LastChecksum", checksum);

            Thread.sleep(1000);

            Files.lines(Paths.get(Configuration.get("FlatFileName")))
                    .skip(1)
                    .map(Student::new)
                    .forEach(Student::upload);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks for students currently out, marks them as violators, and resets all students to being in
     */
    public void checkAndClearViolators() {
        getStudentsOut().forEach(LogEvent::upload);

        Server.getCurrent().getDatabase().query("UPDATE kiosk.allstudents SET `isIn`=1;");
    }

    /**
     * Creates a transaction <br>
     *     Note, this does not upload it to the database
     * @param userid
     * @param kiosk
     * @param valid
     * @return
     */
    public Transaction createTransaction(int userid, int kiosk, boolean valid) {
        var transaction = new Transaction(userid, kiosk, valid);

        return transaction;
    }

    /**
     * Processes the given transaction for the given student
     * <br>
     *     This includes sending it to the guard view and potentially logging it to the database.
     *     Additionally, if the doLog flag is on, it will block until the transaction is uploaded
     * @param student
     * @param transaction
     * @param dolog If the transaction should be logged to the database
     */
    public void processTransaction(Student student, Transaction transaction, boolean dolog) {
        SQLFuture<SQLResult> request = null;
        if(dolog) {
            request = this.getDatabase().query("INSERT INTO `kiosk`.`timelogs`(ID,transaction,kiosk) " +
                    "VALUES(" +
                    "" + transaction.getUserId() + "," +
                    "" + transaction.getTransactionId() + "," +
                    "" + transaction.getKiosk() + ");");
        }

        if(transaction.isValid()){
            this.getDatabase().query("UPDATE kiosk.allstudents " +
                    "SET `isIn`=" + (Server.getCurrent().getStudentsOut().stream().anyMatch(log -> log.getId() == transaction.getUserId()) ? 0 : 1)
                    + " WHERE `ID`=" + transaction.getUserId() + ";").getResults();

            request.getResults();
            Executor.async(() -> getTransaction(transaction.getTransactionId()).ifPresentOrElse(
                    log -> Server.getCurrent().getSocketHandler().sendLogin(new StudentLogPair(getStudent(transaction.getUserId()).get(), log)),
                    () -> logger.warn("Failed to find recently added transaction!")));
        }else{
            Server.getCurrent().getSocketHandler().sendLogin(new StudentLogPair(student, LogEvent.createFake(student, transaction.getKiosk())));
        }
    }

    /**
     * Sets the valid status of a transaction and updates the database
     * @param transaction
     * @param valid
     */
    public void setValidated(long transaction, boolean valid){
        this.getDatabase().query("UPDATE kiosk.timelogs SET `valid`=" + (valid ? 1 : 0) + " WHERE `transaction`=" + transaction + ";");
    }

    /**
     * Gets the database instance
     * @return
     */
    public Database getDatabase() {
        return database;
    }

    public enum State {
        INITIALIZING, RUNNING, CLOSED, CRASH;
    }


}
