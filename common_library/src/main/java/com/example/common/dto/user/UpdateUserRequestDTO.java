package com.example.common.dto.user;

import com.example.common.security.XssSanitizer;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
public class UpdateUserRequestDTO {

    @Size(min=3, max=30, message = "Имя должно иметь длину от 3 до 30 символов")
    private String firstName;

    @Size(min=3, max=30, message = "Фамилия должна иметь длину от 3 до 30 символов")
    private String lastName;

    @Email(message = "Вы ввели некорректный email")
    private String email;

    public void setFirstName(String firstName) {
        if(firstName==null||firstName.trim().isEmpty()){
            this.firstName = null;
            return;
        }
        this.firstName = XssSanitizer.sanitize(firstName);
    }

    public void setLastName(String lastName) {
        if(lastName==null||lastName.trim().isEmpty()){
            this.lastName = null;
            return;
        }
         this.lastName = XssSanitizer.sanitize(lastName);
    }


    public void setEmail(String email) {
        if(email==null||email.trim().isEmpty()){
            this.email = null;
            return;
        }
        this.email = XssSanitizer.sanitize(email);
    }
}
