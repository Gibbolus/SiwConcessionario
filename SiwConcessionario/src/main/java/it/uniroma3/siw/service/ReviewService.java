package it.uniroma3.siw.service;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.uniroma3.siw.model.Car;
import it.uniroma3.siw.model.Supplier;
import it.uniroma3.siw.repository.ReviewRepository;

@Service
public class ReviewService{

	@Autowired ReviewRepository reviewRepository;
	
	public boolean existsBySupplierAndCar(Supplier s, Car c) {
		return this.reviewRepository.existsBySupplierAndCar(s,c);
	}

}