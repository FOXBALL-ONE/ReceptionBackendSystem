package top.foxball.receptionbackendsystem.service

import top.foxball.receptionbackendsystem.datasource.jdbc.InspectionTeamItem

interface InspectionTeamService : ActivityEntityService<InspectionTeamItem, Long> {
    fun findByNameContaining(name: String): List<InspectionTeamItem>
}
