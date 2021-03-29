package main.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "tags")
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Integer id;


    @Column(nullable = false)
    private String name;
}
