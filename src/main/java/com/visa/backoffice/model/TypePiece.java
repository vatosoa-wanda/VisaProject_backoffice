package com.visa.backoffice.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "type_piece")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TypePiece {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, unique = true, nullable = false)
    private String code;

    @OneToMany(mappedBy = "typePiece")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Piece> pieces = new HashSet<>();
}
