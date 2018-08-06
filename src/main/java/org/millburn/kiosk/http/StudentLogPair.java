package org.millburn.kiosk.http;

import org.millburn.kiosk.LogEvent;
import org.millburn.kiosk.Student;

public class StudentLogPair {
    Student student;
    LogEvent log;

    public StudentLogPair(Student student, LogEvent log) {
        this.student = student;
        this.log = log;
    }

    public Student getStudent() {
        return student;
    }

    public LogEvent getLog() {
        return log;
    }
}
