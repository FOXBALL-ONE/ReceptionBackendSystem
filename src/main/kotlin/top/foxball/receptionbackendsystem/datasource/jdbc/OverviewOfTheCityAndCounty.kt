package top.foxball.receptionbackendsystem.datasource.jdbc

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.hibernate.type.SqlTypes

/**
 * 市县概况实体。
 *
 * 通过 activity_id 标记所属活动，正文段落作为概况内部结构化内容保存。
 */
@Table(name = "overview_of_the_city_and_county")
@Entity
data class OverviewOfTheCityAndCounty(
    /** 市县概况主键。 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Int? = null,

    /** 所属活动，删除活动时由数据库级联删除概况记录。 */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    var activity: Activities? = null,

    /** 概况标题。 */
    @Column(name = "title")
    var title: String? = null,

    /** 顶部图片地址。 */
    @Column(name = "top_image_url")
    var topImageUrl: String? = null,

    /** 概况正文段落。 */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "description", columnDefinition = "jsonb")
    var description: MutableList<ParagraphContentItem> = mutableListOf(),
)

/** 图文段落内容。 */
data class ParagraphContentItem(
    /** 段落标题。 */
    var title: String? = null,

    /** 段落正文。 */
    var content: String? = null,
)
