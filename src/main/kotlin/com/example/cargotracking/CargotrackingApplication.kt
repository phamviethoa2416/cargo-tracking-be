package com.example.cargotracking

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration
import org.springframework.boot.runApplication

@SpringBootApplication
class CargotrackingApplication

fun main(args: Array<String>) {
	runApplication<CargotrackingApplication>(*args)
}
