package com.example.carrentalapp.repositories;

import com.example.carrentalapp.entities.Car;
import com.example.carrentalapp.entities.Location;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface CarRepository extends CrudRepository<Car, Long> {
    @Query(value = "SELECT c FROM Car c WHERE c.id NOT IN (SELECT ca.id FROM Car ca LEFT JOIN Rental r ON ca.id = r.car.id WHERE r.plannedDate > ?1 AND r.rentalDate < ?2)")
    List<Car> findCarsAvailableBetweenDates(LocalDate startDate, LocalDate endDate);

    @Query(value = "SELECT c FROM Car c WHERE c.id NOT IN (SELECT ca.id FROM Car ca LEFT JOIN Rental r ON ca.id = r.car.id WHERE r.rentalDate < :endDate AND r.plannedDate > :startDate) AND c.id IN (SELECT ca.id FROM Car ca LEFT JOIN Rental r ON ca.id = r.car.id WHERE r.plannedDate = (SELECT MAX(ra.plannedDate) FROM Rental ra WHERE ra.plannedDate < :startDate AND r.car.id = ra.car.id) AND r.returnPlace = :location)")
    List<Car> findCarsAvailableBetweenDatesInGivenLocation(@Param("startDate") LocalDate startDate, @Param("endDate")LocalDate endDate, @Param("location") Location location);

    @Query(value = "SELECT c FROM Car c WHERE c.id NOT IN " +
            "(SELECT ca.id FROM Car ca LEFT JOIN Rental r ON ca.id = r.car.id " +
            "WHERE r.rentalDate < :endDate AND r.plannedDate > :startDate AND (r.rentalStatus.status = 'rented' OR r.rentalStatus.status = 'reserved')) " +
            "AND c.id IN " +
            "(SELECT ca.id FROM Car ca LEFT JOIN Rental r ON ca.id = r.car.id " +
            "WHERE r.plannedDate = " +
            "(SELECT MAX(ra.plannedDate) FROM Rental ra WHERE ra.plannedDate < :startDate AND r.car.id = ra.car.id AND r.rentalStatus.status <> 'cancelled') " +
            "AND r.returnPlace = :startLocation AND r.rentalStatus.status <> 'cancelled') " + //nie wiem czy tu też nie trzeba właśnie tego statusu uwzględnić, do zastanawoienia czy ta część jest dobrze napisana teraz
            "AND c.id NOT IN " +
            "(SELECT ca.id FROM Car ca LEFT JOIN Rental r ON ca.id = r.car.id " +
            "WHERE r.rentalDate = :nextDate AND r.rentalPlace <> :endLocation AND r.rentalStatus.status = 'reserved')" +
            "AND c.available = true")
    List<Car> findCarsAvailableByDatesAndLocation(@Param("startDate") LocalDate startDate,
                                                  @Param("endDate")LocalDate endDate,
                                                  @Param("nextDate")LocalDate nextDate,
                                                  @Param("startLocation") Location startLocation,
                                                  @Param("endLocation") Location endLocation);
}
