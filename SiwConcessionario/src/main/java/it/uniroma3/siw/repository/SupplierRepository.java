package it.uniroma3.siw.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.repository.CrudRepository;

import it.uniroma3.siw.model.Supplier;


public interface SupplierRepository  extends CrudRepository<Supplier,Long>{
		
	
		public List<Supplier> findBySurname(String surname);

		public boolean existsByName(String name);
		
		public boolean existsBySurname(String surname);
		
		public boolean existsByBirth(LocalDate birth);

}
