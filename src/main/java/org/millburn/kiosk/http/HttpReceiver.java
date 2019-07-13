package org.millburn.kiosk.http;

import org.millburn.kiosk.LogEvent;
import org.millburn.kiosk.Server;
import org.millburn.kiosk.Student;
import org.millburn.kiosk.logging.Logger;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class HttpReceiver {
    Logger log = new Logger();

    @CrossOrigin()
    @RequestMapping(value = "/kiosk/login", method = RequestMethod.GET)
    public Student login(@RequestParam(value = "id") int id, @RequestParam(value = "kiosk") int kiosk) {
        log.debug("Received ID " + id);
        var student = Server.getCurrent().getStudent(id);
        student.ifPresentOrElse(s ->
            Server.getCurrent().processTransaction(s, kiosk, true),
        () ->
            Server.getCurrent().processTransaction(Student.getNonexistent(id), kiosk, false)
        );

        return student.orElse(Student.nonexistent);
    }

    @CrossOrigin()
    @RequestMapping(value = "/students/out", method = RequestMethod.GET)
    public List<StudentLogPair> getStudentsOut(){
        return getPairFromList(Server.getCurrent().getStudentsOut());
    }

    @CrossOrigin
    @RequestMapping(value = "/img", method = RequestMethod.GET)
    public String getImage(@RequestParam("id") int id){
        return Server.getCurrent().getStudent(id).orElse(Student.getNonexistent(id)).getPath();
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
    @RequestMapping(value = "/student/delete", method = RequestMethod.GET)
    public void delete(@RequestParam(value = "id") int id) {
        log.debug("Deleting Student with ID: " + id);
        Server.getCurrent().deleteStudent(id);




    }

    @CrossOrigin
    @RequestMapping(value = "/transactions", method = RequestMethod.GET)
    public List<LogEvent> getAllTransactions(@RequestParam(value = "today", defaultValue = "false") boolean today,
                                             @RequestParam(value = "id", defaultValue = "-1") String id,
                                             @RequestParam(value = "name", defaultValue = "") String name,
                                             @RequestParam(value = "start", defaultValue = "2000-05-01T01:00:00Z") String start,
                                             @RequestParam(value = "end", defaultValue = "2145-01-01T01:00:00Z") String end,
                                             @RequestParam(value = "valid", defaultValue = "") String valid,
                                             @RequestParam(value = "violations", defaultValue = "false") String violations,
                                             @RequestParam(value = "kiosks", defaultValue = "0,1,2,3,4,5,6") String kiosks,
                                             @RequestParam(value = "sort", defaultValue = "transaction") String sort){

        var startInstant = parseInstantFromString(start);

        var endInstant = parseInstantFromString(end);

        List<Student> students = Server.getCurrent()
                .getAllStudents();

        Map<String, String> IDToName = students.stream()
                .collect(Collectors.toMap(Student::getId, Student::getName));

        var ids = new ArrayList<Integer>();
        if(!id.equals("-1")) ids.add(Integer.parseInt(id));

        students
                .stream()
                .filter(s -> s.getName().equals(name))
                .map(Student::getId)
                .map(Integer::parseInt)
                .forEach(ids::add);

        List<LogEvent> source =
                today ? Server.getCurrent().getTransactionsToday() :
                        violations.equals("true") ?
                            Server.getCurrent().getAllViolations() :
                            Server.getCurrent().getAllTransactions();

        var filteredStream = source
                .stream()
                .filter(s -> ids.isEmpty() || ids.stream().anyMatch(i -> s.getId() == i))
                .filter(s -> kiosks.contains(Integer.toString(s.getKiosk())))
                .filter(s -> s.getDate().isAfter(startInstant))
                .filter(s -> s.getDate().isBefore(endInstant))
                .filter(s -> valid.isEmpty() || s.isValid() == Boolean.parseBoolean(valid));

        List<LogEvent> result;

        switch(sort){
            case "time":
                result = filteredStream.sorted(Comparator.comparing(LogEvent::getDate)).collect(Collectors.toList());
                break;
            case "id":
                result = filteredStream.sorted(Comparator.comparing(LogEvent::getId)).collect(Collectors.toList());
                break;
            case "name":
                result = filteredStream.sorted(Comparator.comparing(log -> IDToName.get(Integer.toString(log.getId())))).collect(Collectors.toList());
                break;
            default:
                result = filteredStream.collect(Collectors.toList());
                break;
        }

        return result;
    }

    private Instant parseInstantFromString(String start) {
        try{
            return LocalDateTime.parse(start).atZone(ZoneId.of("America/New_York")).toInstant();
        }catch (DateTimeException ee){
            try {
                return ZonedDateTime.parse(start).toInstant();
            }catch (DateTimeException e){
                try{
                    return OffsetDateTime.parse(start).toInstant();
                }catch (Exception eee){
                    throw new IllegalArgumentException("Failed to parse argument as a DateTime: " + start);
                }
            }
        }
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
}
