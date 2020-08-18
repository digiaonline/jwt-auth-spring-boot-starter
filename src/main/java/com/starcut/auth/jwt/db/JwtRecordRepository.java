package com.starcut.auth.jwt.db;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.starcut.auth.jwt.db.entity.JwtRecord;

public interface JwtRecordRepository extends JpaRepository<JwtRecord, String> {

	List<JwtRecord> findBySubject(String subject);
}
