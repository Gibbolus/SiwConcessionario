package it.uniroma3.siw.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import it.uniroma3.siw.model.Car;

import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.model.Supplier;
import it.uniroma3.siw.model.Optional;

import it.uniroma3.siw.model.User;
import it.uniroma3.siw.service.CarService;
import it.uniroma3.siw.service.CredentialsService;
import it.uniroma3.siw.validator.CarValidator;
import jakarta.persistence.EntityManager;
import jakarta.validation.Valid;

@Controller
public class CarController {
	
	@Autowired CarService carService;
	
	@Autowired GlobalController gc;
	
	@Autowired CredentialsService credentialsService;
	
	@Autowired CarValidator carValidator;
	
	@Autowired EntityManager entityManager;
	
	
	@GetMapping("/car/{id}")
	public String getCar(@PathVariable("id")Long id, Model model) {
		model.addAttribute("car",this.carService.findById(id));
		return "car.html";
	}
	
	@GetMapping("/car")
	  public String showCars(Model model) {
	     model.addAttribute("cars", this.carService.findAll());
	    return "cars.html";
	  }
	
	@GetMapping("/formSearchCar")
	public String formSearchCar() {
		return "formSearchCar.html";
	}
	
	@PostMapping("/formSearchCar")
	public String getCarByModello(@RequestParam String modello, Model model) {
		String query="SELECT c FROM Car c WHERE LOWER(c.modello) LIKE LOWER('%"+ modello +"%')";
		List<Car> cars=this.entityManager.createQuery(query,Car.class).getResultList();
		model.addAttribute("cars", cars);
		return "cars.html";
	}
	
	@GetMapping("/supplier/manageCars")
	public String manageCars(Model model) {
		UserDetails u=gc.getUser();
		String username=u.getUsername();
		Credentials credenziali=this.credentialsService.getCredentials(username);
		User utenteCorrente=credenziali.getUser();
		Supplier fornitoreCorrente= utenteCorrente.getSupplier();
		model.addAttribute("cars", fornitoreCorrente.getCars());
		return "supplier/manageCars.html";
	}
	
	@GetMapping("/supplier/addCar")
	public String formNewCar(Model model) {
		Car car=new Car();
		model.addAttribute("car",car);
		
		return "supplier/addCar.html";
	}
	
	@GetMapping("supplier/FormUpdateCar/{id}")
	public String UpdateCar(@PathVariable("id") Long id, Model model) {
		model.addAttribute("car", this.carService.findById(id));
		return "supplier/formUpdateCar.html";
	}
	
	@GetMapping("supplier/removeCar/{id}")
	public String removeCar(@PathVariable("id")Long id, Model model) {
		Car car=this.carService.findById(id);
		Supplier s=car.supplier;
		if(s!=null) {
			s.getCars().remove(car);
		}
		/* DA IMPLEMENTARE CON OPTIONAL
		if(recipe.getIngredients().isEmpty()) {
				this.recipeService.delete(recipe);
				return "cookUser/index.html";
		}
		else {
				for (RecipeIngredient ing: recipe.getIngredients()) {
					ing.setRecipe(null);
					this.recipeIngredientService.remove(ing);
				}
				recipe.getIngredients().removeAll(recipe.getIngredients());
				
		}
		*/
		
		this.carService.delete(car);
		return "/supplier/index.html";
	}
	
	
	@PostMapping("/car")
	public String newCar(@Valid @ModelAttribute("car") Car car , BindingResult carBindingResult) {
		UserDetails u=gc.getUser();
		String username=u.getUsername();
		Credentials credenziali=this.credentialsService.getCredentials(username);
		User utenteCorrente=credenziali.getUser();
		Supplier fornitore= utenteCorrente.getSupplier();
		car.setSupplier(fornitore);
		car.optionals=new ArrayList<Optional>();
		
		this.carValidator.validate(car, carBindingResult);
		if(!carBindingResult.hasErrors()) {
			fornitore.getCars().add(car);
			this.carService.save(car);
			return "redirect:car/" + car.getId();
		}
		else {
			if(credenziali.getRole().equals("SUPPLIER"))
				return "/supplier/addCar.html";
			else
				return "/admin/addCar.hmtl";
		}
	}
	
	@GetMapping("/admin/addCar")
	public String AdminformNewCar(Model model) {
		Car car=new Car();
		model.addAttribute("car", car);
		return "admin/addCar.html";
	}
	
	@GetMapping("admin/manageCars")
	public String AdminManageCars(Model model) {
		model.addAttribute("cars",this.carService.findAll());
		return "admin/manageCars.html";
	}
	@GetMapping("admin/FormUpdateCar/{id}")
	public String AdminUpdateCar(@PathVariable("id") Long id, Model model) {
		model.addAttribute("car", this.carService.findById(id));
		return "admin/formUpdateCar.html";
	}
	
	@GetMapping("admin/removeCar/{id}")
	public String AdminremoveCar(@PathVariable("id")Long id, Model model) {
		Car car=this.carService.findById(id);
		Supplier s=car.supplier;
		if(s!=null) {
			s.getCars().remove(car);
		}
		/* Optional Da Implementare
		if(recipe.getIngredients().isEmpty()) {
				this.recipeService.delete(recipe);
				return "admin/index.html";
		}
		else {
				for (RecipeIngredient ing: recipe.getIngredients()) {
					ing.setRecipe(null);
					this.recipeIngredientService.remove(ing);
				}
				recipe.getIngredients().removeAll(recipe.getIngredients());
				
		}
		*/
		
		this.carService.delete(car);
		return "/admin/index.html";
	
		
	}
	
	

}
