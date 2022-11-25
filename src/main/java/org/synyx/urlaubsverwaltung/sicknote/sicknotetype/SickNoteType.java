package org.synyx.urlaubsverwaltung.sicknote.sicknotetype;

import jakarta.persistence.GenerationType;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCategory;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import java.util.Objects;

import static jakarta.persistence.EnumType.STRING;

@Entity
public class SickNoteType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(STRING)
    private SickNoteCategory category;

    private String messageKey;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public SickNoteCategory getCategory() {
        return this.category;
    }

    public boolean isOfCategory(SickNoteCategory category) {
        return getCategory().equals(category);
    }

    public void setCategory(SickNoteCategory category) {
        this.category = category;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }

    @Override
    public String toString() {
        return "SickNoteType{" +
            "category=" + category +
            ", messageKey='" + messageKey + '\'' +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final SickNoteType that = (SickNoteType) o;
        return null != this.getId() && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
