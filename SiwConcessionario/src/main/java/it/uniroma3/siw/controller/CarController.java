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
import it.uniroma3.siw.model.OptionalCar;
import it.uniroma3.siw.model.Review;
import it.uniroma3.siw.model.User;
import it.uniroma3.siw.service.CarService;
import it.uniroma3.siw.service.CredentialsService;
import it.uniroma3.siw.service.OptionalCarService;
import it.uniroma3.siw.service.ReviewService;
import it.uniroma3.siw.validator.CarValidator;
import jakarta.persistence.EntityManager;
import jakarta.validation.Valid;

@Controller
public class CarController {

	@Autowired
	CarService carService;

	@Autowired
	OptionalCarService optCarService;

	@Autowired
	ReviewService reviewService;

	@Autowired
	GlobalController gc;

	@Autowired
	CredentialsService credentialsService;

	@Autowired
	CarValidator carValidator;

	@Autowired
	EntityManager entityManager;

	@GetMapping(value = "/car/{id}")
	public String getCar(@PathVariable("id") Long id, Model model) {
		model.addAttribute("car", this.carService.findById(id));
		return "car.html";
	}

	@GetMapping(value = "/car")
	public String showCars(Model model) {
		model.addAttribute("cars", this.carService.findAll());
		return "cars.html";
	}

	@GetMapping(value = "/formSearchCar")
	public String formSearchCar() {
		return "formSearchCar.html";
	}

	@PostMapping(value = "/formSearchCar")
	public String getCarByModello(@RequestParam String modello, Model model) {
		String query = "SELECT c FROM Car c WHERE LOWER(c.modello) LIKE LOWER('%" + modello + "%')";
		List<Car> cars = this.entityManager.createQuery(query, Car.class).getResultList();
		model.addAttribute("cars", cars);
		return "cars.html";
	}

	@GetMapping(value = "/supplier/manageCars")
	public String manageCars(Model model) {
		UserDetails u = gc.getUser();
		String username = u.getUsername();
		Credentials credenziali = this.credentialsService.getCredentials(username);
		User utenteCorrente = credenziali.getUser();
		Supplier fornitoreCorrente = utenteCorrente.getSupplier();
		model.addAttribute("cars", fornitoreCorrente.getCars());
		return "supplier/manageCars.html";
	}

	@GetMapping(value = "/supplier/addCar")
	public String formNewCar(Model model) {
		Car car = new Car();
		model.addAttribute("car", car);
		return "supplier/addCar.html";
	}

	@GetMapping(value = "supplier/formUpdateCar/{id}")
	public String UpdateCar(@PathVariable("id") Long id, Model model) {
		model.addAttribute("car", this.carService.findById(id));
		return "supplier/formUpdateCar.html";
	}

	@GetMapping(value = "supplier/removeCar/{id}")
	public String removeCar(@PathVariable("id") Long id, Model model) {
		Car car = this.carService.findById(id);
		Supplier s = car.supplier;
		if (s != null) {
			s.getCars().remove(car);
		}
		if (car.getOptionals().isEmpty() && car.getReviews().isEmpty()) {
			this.carService.delete(car);
			return "redirect:/supplier/manageCars";
		} else {
			if (!car.getOptionals().isEmpty()) {
				for (OptionalCar optCar : car.getOptionals()) {
					optCar.setCar(null);
					this.optCarService.remove(optCar);
				}
				car.getOptionals().removeAll(car.getOptionals());
			}
			if (!car.getReviews().isEmpty()) {
				for (Review review : car.getReviews()) {
					review.setCar(null);
					this.reviewService.save(review);
				}
				car.getReviews().removeAll(car.getReviews());
			}
		}
		this.carService.delete(car);
		return "redirect:/supplier/manageCars";
	}

	@PostMapping(value = "/car")
	public String newCar(@Valid @ModelAttribute("car") Car car, BindingResult carBindingResult) {
		UserDetails u = gc.getUser();
		String username = u.getUsername();
		Credentials credenziali = this.credentialsService.getCredentials(username);
		User utenteCorrente = credenziali.getUser();
		Supplier fornitore = utenteCorrente.getSupplier();
		car.setSupplier(fornitore);
		car.optionals = new ArrayList<OptionalCar>();
		car.review = new ArrayList<Review>();

		this.carValidator.validate(car, carBindingResult);
		if (!carBindingResult.hasErrors()) {
			fornitore.getCars().add(car);
			this.carService.save(car);
			return "redirect:car/" + car.getId();
		} else {
			if (credenziali.getRole().equals("SUPPLIER"))
				return "/supplier/addCar.html";
			else
				return "/admin/addCar.hmtl";
		}
	}

	@GetMapping(value = "/admin/addCar")
	public String AdminFormNewCar(Model model) {
		Car car = new Car();
		model.addAttribute("car", car);
		return "admin/addCar.html";
	}

	@GetMapping(value = "admin/manageCars")
	public String AdminManageCars(Model model) {
		model.addAttribute("cars", this.carService.findAll());
		return "admin/manageCars.html";
	}

	@GetMapping(value = "admin/FormUpdateCar/{id}")
	public String AdminUpdateCar(@PathVariable("id") Long id, Model model) {
		model.addAttribute("car", this.carService.findById(id));
		return "admin/formUpdateCar.html";
	}

	@GetMapping(value = "admin/removeCar/{id}")
	public String AdminRemoveCar(@PathVariable("id") Long id, Model model) {
		Car car = this.carService.findById(id);
		Supplier s = car.supplier;
		if (s != null) {
			s.getCars().remove(car);
		}
		if (car.getOptionals().isEmpty() && car.getReviews().isEmpty()) {
			this.carService.delete(car);
			return "redirect:/supplier/manageCars";
		} else {
			if (!car.getOptionals().isEmpty()) {
				for (OptionalCar optCar : car.getOptionals()) {
					optCar.setCar(null);
					this.optCarService.remove(optCar);
				}
				car.getOptionals().removeAll(car.getOptionals());
			}
			if (!car.getReviews().isEmpty()) {
				for (Review review : car.getReviews()) {
					review.setCar(null);
					this.reviewService.save(review);
				}
				car.getReviews().removeAll(car.getReviews());
			}
		}
		this.carService.delete(car);
		return "redirect:/supplier/manageCars";

	}

}
