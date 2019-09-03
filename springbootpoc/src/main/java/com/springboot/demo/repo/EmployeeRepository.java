package com.springboot.demo.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.springboot.demo.entity.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
	//个性化query
	//@Query("select from where ")
	//
}
