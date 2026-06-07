package top.foxball.receptionbackendsystem.datasource.jdbc

import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction

@Table(name = "car")
@Entity
data class Car(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Int? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    var activity: Activities? = null,

    @Column(name = "car_number")
    var carNumber: Long? = null,

    @Column(name = "driver")
    var driver: String? = null,

    @Column(name = "driver_number")
    var driverNumber: String? = null,


    var passengersOnBoardList: MutableList<PassengersOnBoardItem> = mutableListOf(),


    var passengersList: MutableList<Person> = mutableListOf(),

    )

@Embeddable
data class PassengersOnBoardItem(
    @Column(name = "name")
    var name: String? = null,

    @Column(name = "phone")
    var phone: String? = null,
)
