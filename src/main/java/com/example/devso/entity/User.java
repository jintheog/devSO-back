package com.example.devso.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
public class User extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 30)
    private String username;

    @Column(nullable = false, length = 60)
    private String password;

    @Column(nullable = false, length = 30)
    private String name;

//    @Column(nullable = false)
    private Role role;


    @Column( length = 30)
    private String phone;

    @Column(length = 500)
    private String bio;


    private String email;

    private String profileImageUrl;

    private String portfolio;

    @Column(nullable = false)
    private AuthProvider provider = AuthProvider.LOCAL;

    private String providerId;

    @Builder
    public User(Long id, String username, String password, String name, Role role, String phone, String bio,
                String email, String profileImageUrl, String portfolio, AuthProvider provider, String providerId) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.name = name;
        this.role = role;
        this.phone = phone;
        this.bio = bio;
        this.email = email;
        this.profileImageUrl = profileImageUrl;
        this.portfolio = portfolio;
        this.provider = provider != null ? provider : AuthProvider.LOCAL;
        this.providerId = providerId;
    }

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Career> careers = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Education> educations = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Certi> certis = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Activity> activities = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Skill> skills = new ArrayList<>();

    // --- 업데이트 메서드 ---
    public void updateProfile(String name, String bio, String profileImageUrl, String portfolio, String phone, String email) {
        this.name = name; // 이름 수정 기능 추가
        this.bio = bio;
        this.profileImageUrl = profileImageUrl;
        this.portfolio = portfolio;
        this.phone = phone;
        this.email = email;
    }


    public void updateCareers(List<Career> newCareers) {
        this.careers.clear();
        if (newCareers != null) {
            newCareers.forEach(c -> {
                c.setUser(this);
                this.careers.add(c);
            });
        }
    }

    public void updateEducations(List<Education> newEducations) {
        this.educations.clear();
        if (newEducations != null) {
            newEducations.forEach(ed -> {
                ed.setUser(this);
                this.educations.add(ed);
            });
        }
    }

    public void updateCertis(List<Certi> newCertis) {
        this.certis.clear();
        if (newCertis != null) {
            newCertis.forEach(c -> {
                c.setUser(this);
                this.certis.add(c);
            });
        }
    }

    public void updateActivities(List<Activity> newActivities) {
        this.activities.clear();
        if (newActivities != null) {
            newActivities.forEach(a -> {
                a.setUser(this);
                this.activities.add(a);
            });
        }
    }

    public void updateSkills(List<Skill> newSkills) {
        this.skills.clear();
        if (newSkills != null) {
            newSkills.forEach(s -> {
                s.setUser(this);
                this.skills.add(s);
            });
        }
    }

    public void updateOauthProfile(String name, String profileImageUrl) {
        if (name != null) this.name = name;
        if (profileImageUrl != null) this.profileImageUrl = profileImageUrl;
    }

    public void updatePassword(String newEncodedPassword) {
        this.password = newEncodedPassword;
    }


}
