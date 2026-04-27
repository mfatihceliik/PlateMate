package com.mefy.platemate.entities.concrete;

import com.mefy.platemate.entities.abstracts.IEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "vehicles")
@Getter
@Setter
@NoArgsConstructor
public class Vehicle implements IEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String plateCode;

    private String brand;
    private String model;
    private String color;


    @ManyToOne
    @JoinColumn(name = "city_id")
    private City city; // Plakanın bağlı olduğu il

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
