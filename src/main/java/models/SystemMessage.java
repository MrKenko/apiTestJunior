package models;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum SystemMessage {
   MESSAGE ("Profile updated successfully");

    private final String text;

}
