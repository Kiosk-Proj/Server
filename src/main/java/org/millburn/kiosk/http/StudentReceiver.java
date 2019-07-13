package org.millburn.kiosk.http;

import org.millburn.kiosk.Edit;
import org.millburn.kiosk.Server;
import org.millburn.kiosk.Student;
import org.millburn.kiosk.util.Tuple;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
public class StudentReceiver {
    @CrossOrigin()
    @RequestMapping(value = "/students", method = RequestMethod.GET)
    public List<Student> getAllStudents(){
        return Server.getCurrent().getAllStudents();
    }

    @CrossOrigin()
    @RequestMapping(value = "/student", method = RequestMethod.GET)
    public Student getStudent(@RequestParam("id") int id){
        return Server.getCurrent().getStudent(id).orElseThrow(() -> new NullPointerException("Failed to find student with id " + id));
    }

    @CrossOrigin()
    @RequestMapping(value = "/student/update", method = RequestMethod.GET)
    public Tuple<Student, Boolean> updateStudent(@RequestParam("id") int id,
                                                 @RequestParam("field") String field,
                                                 @RequestParam("value") String value){
        var student = Server.getCurrent().getStudent(id).orElseThrow(() -> new NullPointerException("Failed to find student with id " + id));

        String oldVal;

        switch (field){
            case "name":
                oldVal = student.getName();
                student.setName(value);
                break;
            case "hasPrivilege":
                oldVal = String.valueOf(student.isSeniorPriv());
                student.setSeniorPriv(Boolean.parseBoolean(value));
                break;
            case "grade":
                oldVal = student.getGrade();
                student.setGrade(value);
                break;
            default:
                return Tuple.of(student, false);
        }

        var upload = student.upload();
        upload.getResults();


        var edit = new Edit();
        edit.setId(id);
        edit.setField(field);
        edit.setOldValue(oldVal);
        edit.setValue(value);
        edit.upload();

        return Tuple.of(Server.getCurrent().getStudent(id).get(), true);
    }

    @CrossOrigin()
    @RequestMapping(value = "/student/new", method = RequestMethod.GET)
    public Optional<Student> createStudent(@RequestParam("id") String id,
                                           @RequestParam("name") String name,
                                           @RequestParam("grade") String grade,
                                           @RequestParam("seniorPriv") String seniorPriv,
                                           @RequestParam("image") String image){

        var student = new Student(Objects.requireNonNull(id), Objects.requireNonNull(image), Objects.requireNonNull(grade), Objects.requireNonNull(id), Boolean.parseBoolean(seniorPriv));

        Edit edit = new Edit();
        edit.setId(Long.parseLong(id));
        edit.setField("NEW");
        edit.setOldValue("");
        edit.setValue(name + ", " + image + ", " + grade + ", " + id + ", " + seniorPriv);

        student.upload().getResults();
        edit.upload();

        return Optional.of(student);
    }



    @CrossOrigin()
    @RequestMapping(value = "/edits", method = RequestMethod.GET)
    public List<Edit> getAllEdits(@RequestParam(value = "id", defaultValue = "-1") int id){
        return Server.getCurrent().getAllEdits()
                .stream()
                .filter(e -> id == -1 || e.getId() == id)
                .collect(Collectors.toList());
    }
}
