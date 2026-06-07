package top.foxball.receptionbackendsystem.datasource.jdbc

import jakarta.persistence.*

/**
 * 通用颜色标签实体。
 *
 * 用于保存业务中可复用的标签名称和颜色值。
 */
@Table(name = "color_tag")
@Entity
data class ColorTag(
    /** 颜色标签主键。 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Int? = null,

    /** 标签名称。 */
    @Column(name = "name")
    var name: String? = null,

    /** 标签颜色值，通常为十六进制颜色或前端约定的颜色标识。 */
    @Column(name = "color")
    var color: String? = null
)
