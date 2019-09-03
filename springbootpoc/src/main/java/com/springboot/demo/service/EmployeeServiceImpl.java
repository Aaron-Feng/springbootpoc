package com.springboot.demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.springboot.demo.entity.Employee;
import com.springboot.demo.repo.EmployeeRepository;

@Service
public class EmployeeServiceImpl implements EmployeeService {


	private EmployeeRepository employeeRepository;
	
	@Autowired
	public EmployeeServiceImpl(EmployeeRepository theEmployeeRepository) {
		employeeRepository = theEmployeeRepository;
	}
	
	@Override
	public List<Employee> findAll() {
		return employeeRepository.findAll();
	}

	@Override	
	public Employee findById(int theId) {
		Optional<Employee> result = employeeRepository.findById(theId);
		Employee e = null;
		if(result.isPresent()) {
			e = result.get();;
		} else {
			throw new RuntimeException("did not find eid" + theId);
		}
		return e;
	}

	@Override	
	public void save(Employee theEmployee) {
		employeeRepository.save(theEmployee);
	}

	@Override	
	public void deleteById(int theId) {
		employeeRepository.deleteById(theId);
	}
	
	//@Override	
	
	
}






