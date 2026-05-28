package com.mefy.platemate.entities.concrete;

import com.mefy.platemate.entities.abstracts.IEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "code_id", insertable = false, updatable = false)
    private UserRoleCodeLookup codeRef;

    @Column(nullable = false, unique = true, length = 60)
    private String name;

    @Column(nullable = false)
    private boolean active = true;

    @Transient
    public UserRoleCode getCode() {
        UserRoleCode fromId = UserRoleCode.fromId(codeId);
        if (fromId != null) {
            return fromId;
        }
        String codeFromRef = resolveCodeFromRef();
        if (codeFromRef != null) {
            return UserRoleCode.fromCode(codeFromRef);
        }
        return null;
    }

    public void setCode(UserRoleCode code) {
        this.codeId = code == null ? null : code.getId();
    }

    @Transient
    public String getCodeValue() {
        UserRoleCode code = UserRoleCode.fromId(codeId);
        if (code != null) {
            return code.getCode();
        }
        return resolveCodeFromRef();
    }

    private String resolveCodeFromRef() {
        if (codeRef == null || !Hibernate.isInitialized(codeRef)) {
            return null;
        }
        return codeRef.getCode();
    }
}
