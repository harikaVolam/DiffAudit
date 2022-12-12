package domain;

import diff.IncludeInDiff;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Employee {

    @IncludeInDiff
    private String name;

    public Employee(String name, String email) {
        this.name = name;
        this.email = email;
    }

    @IncludeInDiff
    private String email;


}
