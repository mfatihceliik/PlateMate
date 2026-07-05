package com.mefy.platemate.entities.concrete;

import com.mefy.platemate.entities.abstracts.IEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_roles")
@Getter
@Setter
@NoArgsConstructor
public class UserRole implements IEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code_id", nullable = false, unique = true)
    private Long codeId;

    @Column(nullable = false, unique = true, length = 60)
    private String name;

    @Column(nullable = false)
    private boolean active = true;

    @Transient
    public UserRoleCode getCode() {
        return UserRoleCode.fromId(codeId);
    }

    public void setCode(UserRoleCode code) {
        this.codeId = code == null ? null : code.getId();
    }

    @Transient
    public String getCodeValue() {
        UserRoleCode code = UserRoleCode.fromId(codeId);
        return code == null ? null : code.getCode();
    }
}
