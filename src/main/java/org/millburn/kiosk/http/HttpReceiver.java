package org.millburn.kiosk.http;

import org.millburn.kiosk.Executor;
import org.millburn.kiosk.LogEvent;
import org.millburn.kiosk.Server;
import org.millburn.kiosk.Student;
import org.millburn.kiosk.logging.Logger;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class HttpReceiver {
    Logger log = new Logger();

    @CrossOrigin()
    @RequestMapping(value = "/kiosk/login", method = RequestMethod.GET)
    public Student login(@RequestParam(value = "id") int id, @RequestParam(value = "kiosk") int kiosk) {
        log.debug("Received ID " + id);
        Server.getCurrent().getStudent(id).ifPresentOrElse(s ->
            Server.getCurrent().processTransaction(s,
                    Server.getCurrent().createTransaction(id, kiosk, s.isSeniorPriv()), true),
        () ->
            Server.getCurrent().processTransaction(Student.getNonexistent(id),
                    Server.getCurrent().createTransaction(id, kiosk, false), false)
        );

        return Server.getCurrent().getStudent(id).orElse(Student.nonexistent);
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
    public List<LogEvent> getAllTransactions(@RequestParam(value = "id", defaultValue = "-1") String id,
                                             @RequestParam(value = "name", defaultValue = "") String name,
                                             @RequestParam(value = "start", defaultValue = "2000-05-01T01:00:00Z") String start,
                                             @RequestParam(value = "end", defaultValue = "2145-01-01T01:00:00Z") String end,
                                             @RequestParam(value = "valid", defaultValue = "") String valid,
                                             @RequestParam(value = "violations", defaultValue = "false") String violations,
                                             @RequestParam(value = "kiosks", defaultValue = "1,2,3,4,5,6") String kiosks){

        var startinstant = new LambdaContainer<Instant>();
        var endinstant = new LambdaContainer<Instant>();

        try{
            startinstant.val = LocalDateTime.parse(start).atZone(ZoneId.of("America/New_York")).toInstant();
        }catch (DateTimeException ee){
            try {
                startinstant.val = ZonedDateTime.parse(start).toInstant();
            }catch (DateTimeException e){
                startinstant.val = OffsetDateTime.parse(start).toInstant();
            }
        }

        try{
            endinstant.val = LocalDateTime.parse(end).atZone(ZoneId.of("America/New_York")).toInstant();
        }catch (DateTimeException ee){
            try {
                endinstant.val = ZonedDateTime.parse(end).toInstant();
            }catch (DateTimeException e){
                endinstant.val = OffsetDateTime.parse(end).toInstant();
            }
        }

        var ids = new ArrayList<Integer>();
        if(!id.equals("-1")) ids.add(Integer.parseInt(id));

        List<LogEvent> source = violations.equals("true") ?
                Server.getCurrent().getAllViolations() :
                Server.getCurrent().getAllTransactions();

        Server.getCurrent()
                .getAllStudents()
                .stream()
                .filter(s -> s.getName().equals(name))
                .map(Student::getId)
                .map(Integer::parseInt)
                .forEach(ids::add);

        return source
                .stream()
                .filter(s -> ids.isEmpty() || ids.stream().anyMatch(i -> s.getId() == i))
                .filter(s -> kiosks.contains(Integer.toString(s.getKiosk())))
                .filter(s -> s.getDate().isAfter(startinstant.val))
                .filter(s -> s.getDate().isBefore(endinstant.val))
                .filter(s -> valid.isEmpty() || s.isValid() == Boolean.parseBoolean(valid))
                .collect(Collectors.toList());

    }

    @CrossOrigin
    @RequestMapping(value = "/transactions/violations", method = RequestMethod.GET)
    public List<LogEvent> getViolations(){
        return Server.getCurrent().getAllViolations();
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
    @RequestMapping(value = "/tablet/flag", method = RequestMethod.GET)
    public int flag(@RequestParam("id") long id, @RequestParam("flagged") int flagged){
        if(flagged > 0)
            Server.getCurrent().setValidated(id, true);
        else
            Server.getCurrent().setValidated(id, false);

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

    class LambdaContainer<T>{
        public T val;
    }
}
