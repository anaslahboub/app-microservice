package com.anas.groupservice.entity;

import com.anas.groupservice.common.BaseAuditingEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "group_members", 
       uniqueConstraints = {@UniqueConstraint(columnNames = {"group_id", "user_id"})})
public class GroupMember extends BaseAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "is_admin")
    private boolean isAdmin = false;

    @Column(name = "is_co_admin")
    private boolean isCoAdmin = false;

    // Status could be: ACTIVE, INACTIVE, LEFT
    @Column(name = "status")
    private String status = "ACTIVE";
}