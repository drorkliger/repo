import sqlite3
import os
import atexit
import sys
import objectClasses


class _Repository:
    def __init__(self, con):
        self._conn = con

    def _close(self):
        self._conn.commit()
        self._conn.close()

    def create_tables(self):
        self._conn.executescript("""

        CREATE TABLE students (
                grade      TEXT PRIMARY KEY,
                count               INTEGER NOT NULL);

        CREATE TABLE classrooms(
                id                 INTEGER PRIMARY KEY,
                location                    TEXT NOT NULL,
                current_course_id           INTEGER NOT NULL,
                current_course_time_left    INTEGER NOT NULL
                                                       );                                    

        CREATE TABLE courses(
               id                           INT PRIMARY KEY,
               course_name                  TEXT NOT NULL,
               student                      TEXT NOT NULL,
               number_of_students           INT NOT NULL,
               classroom_id                 INTEGER REFERENCES classrooms(id),
               course_length                INT NOT NULL
                       );
                    """);


def Parser(connection):  # parse the argument and insert accordingly
    fileName = sys.argv[1]
    with open(fileName) as file:
        lines = file.readlines();

    for line in lines:
        line = line[0:len(line)]
        splittedLine = line.split(',')
        lineAsList = []
        for word in splittedLine:
            lineAsList.append(word.strip())

        # for word in line:
        if lineAsList[0] == 'R':
            class1 = objectClasses.Classroom(lineAsList[1], lineAsList[2])
            objectClasses.Dao(objectClasses.Classroom, connection).insert(class1)

        elif lineAsList[0] == 'C':
            course1 = objectClasses.Course(lineAsList[1], lineAsList[2], lineAsList[3],
                                           lineAsList[4], lineAsList[5], lineAsList[6])
            objectClasses.Dao(objectClasses.Course, connection).insert(course1)

        else:
            student1 = objectClasses.Student(lineAsList[1], lineAsList[2])
            objectClasses.Dao(objectClasses.Student, connection).insert(student1)


def print_table(list_of_tuples):
    for item in list_of_tuples:
        print(item)


def print_tables(Connection):
    print("courses")
    print_table(objectClasses.Dao(objectClasses.Course, Connection).take_all())

    print("classrooms")
    print_table(objectClasses.Dao(objectClasses.Classroom, Connection).take_all())

    print("students")
    print_table(objectClasses.Dao(objectClasses.Student, Connection).take_all())


def main():
    if os.path.isfile('schedule.db'):
        sys.exit();
    else:
        Connection = sqlite3.connect('schedule.db')

        repo = _Repository(Connection)
        atexit.register(repo._close)

        repo.create_tables()

        Parser(Connection)

        print_tables(Connection)


if __name__ == '__main__':
    main()




