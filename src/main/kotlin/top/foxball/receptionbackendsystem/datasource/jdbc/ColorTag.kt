package top.foxball.receptionbackendsystem.datasource.jdbc

import jakarta.persistence.Column
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

data class ColorTag(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @Column(name = "name")
    var name: String? = null,

    @Column(name = "color")
    var color: String? = null
)
