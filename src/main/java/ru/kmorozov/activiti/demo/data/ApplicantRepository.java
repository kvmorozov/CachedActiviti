package ru.kmorozov.activiti.demo.data;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by sbt-morozov-kv on 12.09.2016.
 */
public interface ApplicantRepository extends JpaRepository<Applicant, Long> {
}
