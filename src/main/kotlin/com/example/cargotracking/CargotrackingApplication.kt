package com.example.cargotracking

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication
@EnableJpaAuditing
class CargotrackingApplication

fun main(args: Array<String>) {
	runApplication<CargotrackingApplication>(*args)
}
