import objectClasses
import os
import sqlite3

def print_table(list_of_tuples):
    for item in list_of_tuples:
        print(item)


def assign_class(classroom,course_to_insert,conn,iteration):
    objectClasses.Dao(objectClasses.Classroom, conn).update({'current_course_id': course_to_insert.id},{'id': course_to_insert.classroom_id})
    objectClasses.Dao(objectClasses.Classroom, conn).update({'current_course_time_left': course_to_insert.course_length}, {'id': course_to_insert.classroom_id})
    studentType = course_to_insert.student
    countToAssign = objectClasses.Dao(objectClasses.Student, conn).find(grade=studentType)
    countToAssign = countToAssign[0].count - course_to_insert.number_of_students
    objectClasses.Dao(objectClasses.Student, conn).update({'count': countToAssign}, {'grade': studentType})
    print("({}) {}: {} is schedule to start".format(iteration , classroom.location , course_to_insert.course_name))


def print_tables(conn):
    print("courses")
    print_table(objectClasses.Dao(objectClasses.Course,conn).take_all())

    print("classrooms")
    print_table(objectClasses.Dao(objectClasses.Classroom,conn).take_all())

    print("students")
    print_table(objectClasses.Dao(objectClasses.Student,conn).take_all())


if os.path.isfile('schedule.db'):
    conn = sqlite3.connect('schedule.db')
    cursor = conn.cursor()

# represent iteration
iteration = 0
if len(objectClasses.Dao(objectClasses.Course,conn).find_all())<=0:
    print_tables()

while os.path.isfile('schedule.db') and len(objectClasses.Dao(objectClasses.Course,conn).find_all()) > 0:
    # conditions to break the loop
    # there is no db
    # if not os.path.isfile('schedule.db'):
    #    break

    # filling classes with courses
    for checkedClass in objectClasses.Dao(objectClasses.Classroom,conn).find_all():
            if  checkedClass.current_course_id == 0: #if there is no course in that class
                course_to_assign = objectClasses.Dao(objectClasses.Course,conn).find(classroom_id=checkedClass.id)
                numOfCoursesToAssign=len(course_to_assign)
                if numOfCoursesToAssign >0:
                    course_to_insert = course_to_assign[0]
                    assign_class(checkedClass, course_to_insert, conn, iteration)


            else:
                objectClasses.Dao(objectClasses.Classroom,conn).update({'current_course_time_left':checkedClass.current_course_time_left-1},{'id':checkedClass.id}) #reduce time left
                if checkedClass.current_course_time_left-1 == 0:
                    done_course=objectClasses.Dao(objectClasses.Course,conn).find(classroom_id=checkedClass.id)[0]
                    print("({}) {}: {} is done".format(iteration,checkedClass.location,done_course.course_name))
                    objectClasses.Dao(objectClasses.Course,conn).delete(id=checkedClass.current_course_id)
                    objectClasses.Dao(objectClasses.Classroom, conn).update({'current_course_id': 0}, {'id': checkedClass.id})
                    course_to_add = objectClasses.Dao(objectClasses.Course, conn).find(classroom_id=checkedClass.id)
                    numOfCoursesToAdd = len(course_to_add)
                    if numOfCoursesToAdd > 0:
                        course_to_insert = course_to_add[0]
                        assign_class(checkedClass,course_to_insert,conn,iteration)
                else:
                    not_done_course=objectClasses.Dao(objectClasses.Course,conn).find(classroom_id=checkedClass.id)[0]
                    print("({}) {}: occupied by {}".format(iteration,checkedClass.location,not_done_course.course_name))
    iteration=iteration+1
    print_tables(conn)
    conn.commit()






