package top.foxball.receptionbackendsystem

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication
class ReceptionBackendSystemApplication

fun main(args: Array<String>) {
    runApplication<ReceptionBackendSystemApplication>(*args)
}
