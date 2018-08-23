package org.millburn.kiosk.http;

import org.millburn.kiosk.Executor;
import org.millburn.kiosk.LogEvent;
import org.millburn.kiosk.Server;
import org.millburn.kiosk.Student;
import org.millburn.kiosk.logging.Logger;
import org.springframework.web.bind.annotation.*;

import java.util.*;
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

            Executor.async(() -> Server.getCurrent().processTransaction(s, transaction));
        }, () ->
            log.debug("Kiosk requested ID " + userid + ", no student found")
        );

        return maybestudent.get();
    }

    @CrossOrigin()
    @RequestMapping(value = "/students", method = RequestMethod.GET)
    public List<Student> getAllStudents(){
        return Server.getCurrent().getAllStudents();
    }

    @CrossOrigin()
    @RequestMapping(value = "/student", method = RequestMethod.GET)
    public Student getStudent(@RequestParam("id") int id){
        return Server.getCurrent().getStudent(id).get();
    }

    @CrossOrigin()
    @RequestMapping(value = "/students/out", method = RequestMethod.GET)
    public List<StudentLogPair> getStudentsOut(){
        return getPairFromList(Server.getCurrent().getStudentsOut());
    }

    @CrossOrigin
    @RequestMapping(value = "/img", method = RequestMethod.GET)
    public String getImage(@RequestParam("id") int id){
        return getStudent(id).getPath();
    }

    @CrossOrigin
    @RequestMapping(value = "/student/out", method = RequestMethod.GET)
    public Optional<LogEvent> isOut(@RequestParam("id") int id){
        return Server.getCurrent().getStudentsOut()
                .stream()
                .filter(log -> log.getId() == id)
                .findFirst();
    }

    @CrossOrigin
    @RequestMapping(value = "/transactions", method = RequestMethod.GET)
    public List<LogEvent> getAllTransactions(){
        return Server.getCurrent().getAllTransactions();
    }

    @CrossOrigin
    @RequestMapping(value = "/transactions/flagged", method = RequestMethod.GET)
    public List<StudentLogPair> getAllFlagged(){
        return getPairFromList(Server.getCurrent().getFlaggedTransactions());
    }

    @CrossOrigin
    @RequestMapping(value = "/tablet/getrecent", method = RequestMethod.GET)
    public List<StudentLogPair> getRecent(@RequestParam("kiosk") int kiosk){
        var list = new ArrayList<>(getPairFromList(Server.getCurrent().getTransactionsToday()).stream().filter(s -> s.log.getKiosk() == kiosk).collect(Collectors.toList()));
        Collections.reverse(list);
        return list;
    }

    @CrossOrigin
    @RequestMapping(value = "/tablet/flag", method = RequestMethod.POST)
    public int flag(@RequestParam("id") int id, @RequestParam("flagged") int flagged){
        if(flagged > 0)
            Server.getCurrent().setValidated(Server.getCurrent().getLatestTransactionFor(id), true);
        else
            Server.getCurrent().setValidated(Server.getCurrent().getLatestTransactionFor(id), false);

        return 1;
    }

    @CrossOrigin
    @RequestMapping(value = "/getlongsize", method = RequestMethod.GET)
    public int getSize(){
        return new Random().nextInt(1000);
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
