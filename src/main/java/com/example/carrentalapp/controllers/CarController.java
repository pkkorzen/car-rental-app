package com.example.carrentalapp.controllers;

import com.example.carrentalapp.dto.UserDto;
import com.example.carrentalapp.entities.*;
import com.example.carrentalapp.entities.enums.Gearbox;
import com.example.carrentalapp.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class CarController {

    private CarService carService;
    private RentalService rentalService;
    private TypeService typeService;
    private UserService userService;
    private LocationService locationService;

    @Autowired
    public CarController(CarService carService, RentalService rentalService, TypeService typeService, UserService userService, LocationService locationService){
        this.carService = carService;
        this.rentalService = rentalService;
        this.typeService = typeService;
        this.userService = userService;
        this.locationService = locationService;
    }

    @GetMapping("/all-cars")
    public String showAllCars(Model model, Authentication authentication){
        List<Car> cars = carService.findAllCars();
        model.addAttribute("cars", cars);
        model.addAttribute("text", "All");
        Optional<UserDto> userOptional = userService.findUserByLogin(authentication.getName());
        userOptional.ifPresent(user -> model.addAttribute("userRole", user.getRole()));
        return "cars/all-cars";
    }

/*    @GetMapping("/available-cars")
    public String showAvailableCars(Model model, @RequestParam(name="startDate") LocalDate startDate,
                                    @RequestParam(name="endDate") LocalDate endDate){
        List<Car> cars = carService.findAllCars();
        List<Rental> rentals = rentalService.findRentalsByDateBetween(startDate, endDate);
        List<Car> rentedCars = rentals.stream().map(Rental::getCar).collect(Collectors.toList());
        cars.removeAll(rentedCars);
        model.addAttribute("cars", cars);
        return"/available-cars";
    }*/

//to wyglada na lepsze rozwiazanie z customRepository i jpql query aniżeli przerabianie danych z 2 tabel w javie
    @PostMapping("/available-cars")
    public String showAvailableCars(Model model, @RequestParam(name="startDate") String startDate,
                                    @RequestParam(name="endDate") String endDate,
                                    @RequestParam(name="startLocation") String startLocation,
                                    @RequestParam(name="endLocation") String endLocation,
                                    Authentication authentication){
        LocalDate rentalDate = LocalDate.parse(startDate);
        LocalDate returnDate = LocalDate.parse(endDate);
        Long startLocationId = Long.parseLong(startLocation);
        Long endLocationId = Long.parseLong(endLocation);
        Optional<Location> startLocationOptional = locationService.findLocationById(startLocationId);
        Optional<Location> endLocationOptional = locationService.findLocationById(endLocationId);
        Location rentalLocation = null;
        if(startLocationOptional.isPresent()){
            model.addAttribute("startLocation", startLocationOptional.get());
            rentalLocation = startLocationOptional.get();
        }
        endLocationOptional.ifPresent(location -> model.addAttribute("endLocation", location));
        List<Car> cars = carService.findCarsAvailableBetweenDatesInGivenLocation(rentalDate, returnDate, rentalLocation);
        model.addAttribute("cars", cars);
        model.addAttribute("text", "Available");
        model.addAttribute("startDate", rentalDate);
        model.addAttribute("endDate", returnDate);
        Optional<UserDto> userOptional = userService.findUserByLogin(authentication.getName());
        userOptional.ifPresent(user -> model.addAttribute("userRole", user.getRole()));
        return"cars/all-cars";
    }

    @PostMapping("cars/save")
    public String saveCar(@ModelAttribute Car car, @RequestParam("pressed-button") String pushedButton){

        if(pushedButton.equalsIgnoreCase("save")){
            carService.saveCar(car);
        }
        return "redirect:/all-cars";
    }

    @GetMapping("cars/add")
    public String addCar(Model model){
        List<Type> types = typeService.findAll();
        model.addAttribute("text", "New");
        model.addAttribute("car", new Car());
        model.addAttribute("gearboxType", Gearbox.values());
        model.addAttribute("types", types);
        return "cars/car";
    }

    @GetMapping("cars/edit/{id}")
    public String editCar(@PathVariable Long id, Model model){
        List<Type> types = typeService.findAll();
        model.addAttribute("text", "Edit");
        model.addAttribute("gearboxType", Gearbox.values());
        model.addAttribute("types", types);
        Optional<Car> carOptional = carService.findCarById(id);
        carOptional.ifPresent(car -> model.addAttribute("car", car));

        return "cars/car";
    }

    @GetMapping("cars/delete-confirmation/{id}")
    public String deleteConfirmation(@PathVariable Long id, Model model){
        Optional<Car> carOptional = carService.findCarById(id);
        carOptional.ifPresent(car -> model.addAttribute("carToAsk", car));
        return "cars/delete-confirmation";
    }

    @GetMapping("cars/delete/{id}")
    public String deleteCar(@PathVariable Long id){

        carService.deleteCar(id);

        return "cars/all-cars";
    }

    @GetMapping("/")
    public String index(Model model){
        List<Location> locations = locationService.findAllLocations();
        model.addAttribute("locations", locations);
        return "../static/index";
    }
}
