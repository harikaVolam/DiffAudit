import diff.DiffMap;
import diff.ObjectDiffer;
import domain.Employee;

public class Main {

    public static void main(String[] args) {
        Employee employee1 = new Employee("Niharika","harikavolam@gmail.com");


        Employee employee2 = new Employee("Nrika","arikavolam@gmail.com");


        DiffMap diffMap = ObjectDiffer.diff(employee1,employee2,Employee.class);
    }
}
