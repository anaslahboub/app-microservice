package com.anas.groupservice.entity;

import com.anas.groupservice.common.BaseAuditingEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "groups")
public class Group extends BaseAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(name = "subject")
    private String subject;

    @Column(name = "is_archived")
    private boolean archived = false;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<GroupMember> groupMembers;

    @Transient
    public int getMemberCount() {
        return groupMembers != null ? groupMembers.size() : 0;
    }

    @Transient
    public boolean isAdmin(String userId) {
        if (groupMembers == null) return false;
        return groupMembers.stream()
                .anyMatch(gm -> gm.getUserId().equals(userId) && gm.isAdmin());
    }

    @Transient
    public boolean isMember(String userId) {
        if (groupMembers == null) return false;
        return groupMembers.stream()
                .anyMatch(gm -> gm.getUserId().equals(userId));
    }
}