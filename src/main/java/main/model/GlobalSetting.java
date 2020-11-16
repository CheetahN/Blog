package main.model;

import lombok.Data;
import main.model.enums.GlobalSettingCode;
import main.model.enums.GlobalSettingValue;

import javax.persistence.*;

@Entity
@Data
@Table(name = "global_settings")
public class GlobalSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private int id;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(255)", nullable = false)
    private GlobalSettingCode code;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(255)", nullable = false)
    private GlobalSettingValue value;
}
