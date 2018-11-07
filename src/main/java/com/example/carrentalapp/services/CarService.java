package com.example.carrentalapp.services;

import com.example.carrentalapp.entities.Car;

import java.util.List;
import java.util.Optional;

public interface CarService {
    Optional<Car> findCarById(Long id);
    List<Car> findAllCars();
    void saveCar(Car car);
    void deleteCar(Long id);
}