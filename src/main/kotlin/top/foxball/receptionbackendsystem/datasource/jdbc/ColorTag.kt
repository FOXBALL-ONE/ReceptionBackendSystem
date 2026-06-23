package top.foxball.receptionbackendsystem.datasource.jdbc

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction

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

    /** 所属活动，删除活动时由数据库级联删除标签记录。 */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    var activity: Activities? = null,

    /** 标签名称。 */
    @Column(name = "name")
    var name: String? = null,

    /** 标签颜色值，通常为十六进制颜色或前端约定的颜色标识。 */
    @Column(name = "color")
    var color: String? = null,

    /** 标签用途类型，用于区分人员分类和住宿分类。 */
    @Column(name = "type")
    var type: String? = null,
) {
    companion object {
        const val TYPE_PERSON = "PERSON"
        const val TYPE_LODGING = "LODGING"
    }
}
