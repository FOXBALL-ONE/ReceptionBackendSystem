package top.foxball.receptionbackendsystem.datasource.jdbc

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

/**
 * 活动车辆实体。
 *
 * 保存活动中的车辆、驾驶员、随车人员和乘客名单等接待用车信息。
 */
@Table(name = "car")
@Entity
data class Car(
    /** 车辆记录主键。 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Int? = null,

    /** 所属活动，删除活动时由数据库级联删除车辆记录。 */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    var activity: Activities? = null,

    /** 车号。 */
    @Column(name = "car_number")
    var carNumber: Long? = null,

    /** 驾驶员姓名。 */
    @Column(name = "driver")
    var driver: String? = null,

    /** 驾驶员联系电话。 */
    @Column(name = "driver_number")
    var driverNumber: String? = null,

    /** 随车人员列表，作为车辆的值对象集合单独建表保存。 */
    @ElementCollection
    @CollectionTable(
        name = "car_passengers_on_board",
        joinColumns = [JoinColumn(name = "car_id")]
    )
    @OrderColumn(name = "passenger_order")
    var passengersOnBoardList: MutableList<PassengersOnBoardItem> = mutableListOf(),

    /** 乘客名单，使用 jsonb 保存人员快照，避免与活动人员关系耦合。 */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "passengers_list", columnDefinition = "jsonb")
    var passengersList: MutableList<Person> = mutableListOf(),
)

/** 随车人员值对象。 */
@Embeddable
data class PassengersOnBoardItem(
    /** 随车人员姓名。 */
    @Column(name = "name")
    var name: String? = null,

    /** 随车人员联系电话。 */
    @Column(name = "phone")
    var phone: String? = null,
)
