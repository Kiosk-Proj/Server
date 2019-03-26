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
    public Student login(@RequestParam(value = "id") int userid, @RequestParam(value = "kiosk") int kiosk) {
        Server.getCurrent().getStudent(userid).ifPresentOrElse(s ->
            Server.getCurrent().processTransaction(s,
                    Server.getCurrent().createTransaction(userid, kiosk, s.isSeniorPriv())),
        () ->
            Server.getCurrent().processTransaction(Student.nonexistent,
                    Server.getCurrent().createTransaction(userid, kiosk, false))
        );

        return Server.getCurrent().getStudent(userid).orElse(Student.nonexistent);
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
                                             @RequestParam(value = "kiosks", defaultValue = "1,2,3,4,5,6") String kiosks,
                                             @RequestParam(value = "sort", defaultValue = "transaction") String sort){

        InstantContainer startinstant = new InstantContainer();
        InstantContainer endinstant = new InstantContainer();

        try{
            startinstant.instant = LocalDateTime.parse(start).atZone(ZoneId.of("America/New_York")).toInstant();
        }catch (DateTimeException ee){
            try {
                startinstant.instant = ZonedDateTime.parse(start).toInstant();
            }catch (DateTimeException e){
                startinstant.instant = OffsetDateTime.parse(start).toInstant();
            }
        }

        try{
            endinstant.instant = LocalDateTime.parse(end).atZone(ZoneId.of("America/New_York")).toInstant();
        }catch (DateTimeException ee){
            try {
                endinstant.instant = ZonedDateTime.parse(end).toInstant();
            }catch (DateTimeException e){
                endinstant.instant = OffsetDateTime.parse(end).toInstant();
            }
        }

        boolean validbool = Boolean.parseBoolean(valid);

        List<Student> students = Server.getCurrent()
                .getAllStudents();

        Map<String, String> nameToID =  students.stream()
                .collect(Collectors.toMap(Student::getName, Student::getId));

        Map<String, String> IDToName =  students.stream()
                .collect(Collectors.toMap(Student::getId, Student::getName));

        if(!name.isEmpty()){
            if(!nameToID.containsKey(name)) return new ArrayList<>();
            else id = nameToID.get(name);
        }

        String studentid = id;
        
        List<LogEvent> source;

        if(violations.equals("true"))
            source = Server.getCurrent().getAllViolations();
        else
            source = Server.getCurrent().getAllTransactions();

        var filteredStream = source
                .stream()
                .filter(s -> s.getId() == Integer.parseInt(studentid) || studentid.equals("-1"))
                .filter(s -> kiosks.contains(Integer.toString(s.getKiosk())))
                .filter(s -> s.getDate().isAfter(startinstant.instant))
                .filter(s -> s.getDate().isBefore(endinstant.instant))
                .filter(s -> valid.isEmpty() || s.isValid() == validbool);

        List<LogEvent> result;

        switch(sort){
            case "time":
                result = filteredStream.sorted(Comparator.comparing(LogEvent::getDate)).collect(Collectors.toList());
                break;
            case "id":
                result = filteredStream.sorted(Comparator.comparing(LogEvent::getId)).collect(Collectors.toList());
                break;
            case "name":
                result = filteredStream.sorted(Comparator.comparingInt(log -> Integer.parseInt(IDToName.get(Integer.toString(log.getId()))))).collect(Collectors.toList());
                break;
            default:
                result = filteredStream.collect(Collectors.toList());
                break;
        }

        return result;
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

    class InstantContainer{
        public Instant instant;
    }
}
