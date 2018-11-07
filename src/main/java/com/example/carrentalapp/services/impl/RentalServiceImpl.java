package com.example.carrentalapp.services.impl;

import com.example.carrentalapp.entities.Rental;
import com.example.carrentalapp.entities.User;
import com.example.carrentalapp.repositories.RentalRepository;
import com.example.carrentalapp.services.RentalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class RentalServiceImpl implements RentalService {

    private RentalRepository rentalRepository;

    @Autowired
    public RentalServiceImpl(RentalRepository rentalRepository){
        this.rentalRepository = rentalRepository;
    }

    @Override
    public Optional<Rental> findRentalById(Long id) {
        return rentalRepository.findById(id);
    }

    @Override
    public List<Rental> findRentalsByDateBetween(LocalDate startDate, LocalDate endDate) {
        Iterable<Rental> rentals = rentalRepository.findAllByRentalDateBeforeAndPlannedDateAfter(endDate, startDate);
        return StreamSupport.stream(rentals.spliterator(), true).collect(Collectors.toList());
    }

    @Override
    public List<Rental> findAllRentals() {
        Iterable<Rental> rentals = rentalRepository.findAll();
        return StreamSupport.stream(rentals.spliterator(), true).collect(Collectors.toList());
    }

    @Override
    public List<Rental> findAllRentalsByUser(User user) {
        Iterable<Rental> rentals = rentalRepository.findAllByUser(user);
        return StreamSupport.stream(rentals.spliterator(), true).collect(Collectors.toList());
    }

    @Override
    public void saveRental(Rental rental) {
        rentalRepository.save(rental);
    }

    @Override
    public void deleteRental(Long id) {
        Optional<Rental> result = rentalRepository.findById(id);
        result.ifPresent(rental -> rentalRepository.delete(rental));
    }
}
