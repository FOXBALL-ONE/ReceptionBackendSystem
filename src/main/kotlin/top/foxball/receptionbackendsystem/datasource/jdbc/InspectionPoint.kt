package top.foxball.receptionbackendsystem.datasource.jdbc

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction

/**
 * 考察点实体。
 *
 * 通过 activity_id 标记所属活动。
 */
@Table(name = "inspection_point")
@Entity
class InspectionPoint(
    /** 考察点主键。 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Int? = null,

    /** 所属活动，删除活动时由数据库级联删除考察点记录。 */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    var activity: Activities? = null,

    /** 考察点名称。 */
    @Column(name = "name", length = 255)
    var name: String? = null,

    /** 考察点图片地址。 */
    @Column(name = "image_url", length = 1024)
    var imageURL: String? = null,

    /** 考察点说明。 */
    @Column(name = "description", length = 10240)
    var description: String? = null,
)
