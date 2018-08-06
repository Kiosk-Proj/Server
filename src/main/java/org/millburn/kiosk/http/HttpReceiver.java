package org.millburn.kiosk.http;

import org.millburn.kiosk.LogEvent;
import org.millburn.kiosk.Server;
import org.millburn.kiosk.Student;
import org.millburn.kiosk.logging.Logger;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
public class HttpReceiver {
    Logger log = new Logger();

    @CrossOrigin()
    @RequestMapping(value = "/kiosk/login", method = RequestMethod.GET)
    public Student login(@RequestParam(value = "id") int userid, @RequestParam(value = "kiosk") int kiosk) {
        var maybestudent = Server.getCurrent().getStudent(userid);
        maybestudent.ifPresentOrElse(s -> {
            if(!s.isSeniorPriv()){
                log.debug("Kiosk requested ID " + userid + ", found student " + s.getName() + " but they lack privilege");
                return;
            }

            var transaction = Server.getCurrent().createTransaction(userid, kiosk, s.getName());

            log.debug("Kiosk requested ID " + userid + ", found student " + s.getName());

            Server.getCurrent().processTransaction(transaction);

        }, () ->
            log.debug("Kiosk requested ID " + userid + ", no student found")
        );

        return maybestudent.get();
    }

    @CrossOrigin()
    @RequestMapping(value = "/getallstudents", method = RequestMethod.GET)
    public List<Student> getAllStudents(){
        return Server.getCurrent().getAllStudents();
    }

    @CrossOrigin()
    @RequestMapping(value = "/getalltransactions", method = RequestMethod.GET)
    public List<LogEvent> getAllTransactions(){
        return Server.getCurrent().getAllTransactions();
    }

    @CrossOrigin()
    @RequestMapping(value = "/getstudent", method = RequestMethod.GET)
    public Student getStudent(@RequestParam("id") int id){
        return Server.getCurrent().getStudent(id).get();
    }

    @CrossOrigin()
    @RequestMapping(value = "/getstudentsout", method = RequestMethod.GET)
    public List<StudentLogPair> getStudentsOut(){
        return getPairFromList(Server.getCurrent().getStudentsOut());
    }

    @CrossOrigin()
    @RequestMapping(value = "/isout", method = RequestMethod.GET)
    public Optional<LogEvent> isOut(@RequestParam("id") int id){
        return Server.getCurrent().getStudentsOut()
                .stream()
                .filter(log -> log.getId() == id)
                .findFirst();
    }

    @CrossOrigin()
    @RequestMapping(value = "/tablet/getrecent", method = RequestMethod.GET)
    public List<StudentLogPair> getrecent(@RequestParam("kiosk") int kiosk){
        var list = new ArrayList<>(getPairFromList(Server.getCurrent().getTransactionsToday()).stream().filter(s -> s.log.getKiosk() == kiosk).collect(Collectors.toList()));
        Collections.reverse(list);
        return list;
    }

    @CrossOrigin()
    @RequestMapping(value = "/tablet/flag", method = RequestMethod.GET)
    public int flag(@RequestParam("id") int id){
        Server.getCurrent().setValidated(Server.getCurrent().getLatestTransactionFor(id), false);
        return 1;
    }

    @CrossOrigin()
    @RequestMapping(value = "/tablet/unflag", method = RequestMethod.GET)
    public int unflag(@RequestParam("id") int id){
        Server.getCurrent().setValidated(Server.getCurrent().getLatestTransactionFor(id), true);
        return 0;
    }

    @CrossOrigin()
    @RequestMapping(value = "/tablet/getflagged", method = RequestMethod.GET)
    public List<StudentLogPair> getAllFlagged(){
        return getPairFromList(Server.getCurrent().getFlaggedTransactions());
    }

    @CrossOrigin()
    @RequestMapping(value = "/getlongsize", method = RequestMethod.GET)
    public int getSize(){
        return 6;
    }

    public static List<StudentLogPair> getPairFromList(List<LogEvent> events){
        return events.parallelStream()
                .map(v -> new StudentLogPair(Server.getCurrent().getDatabase().query("SELECT * FROM kiosk.allstudents WHERE `id`=" + v.getId() + ";")
                        .getResults()
                        .stream()
                        .map(Student::new)
                        .findFirst().get(), v))
                .collect(Collectors.toList());
    }
}
