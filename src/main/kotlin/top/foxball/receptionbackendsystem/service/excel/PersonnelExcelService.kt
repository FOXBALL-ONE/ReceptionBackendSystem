package top.foxball.receptionbackendsystem.service.excel

import com.alibaba.excel.EasyExcel
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import top.foxball.receptionbackendsystem.datasource.excel.PersonnelItem
import java.io.FileInputStream
import java.io.InputStream

/**
 * 人员名单的 Excel 导入服务。
 *
 * 读取工作簿第 1 个 sheet（index=0，"人员名单"），按 [PersonnelItem] 列结构解析为内存人员项，
 * 对应活动下属的 [top.foxball.receptionbackendsystem.datasource.jdbc.Person] 实体。
 */
@Service
class PersonnelExcelService {

    /** 从文件路径读取人员名单 sheet。 */
    fun importPersonnel(filePath: String): List<PersonnelItem> {
        return FileInputStream(filePath).use { inputStream ->
            importPersonnel(inputStream)
        }
    }

    /** 从上传文件读取人员名单 sheet。 */
    fun importPersonnel(file: MultipartFile): List<PersonnelItem> {
        return file.inputStream.use { inputStream ->
            importPersonnel(inputStream)
        }
    }

    /**
     * 读取"人员名单" sheet：按 [PersonnelItem] 表头解析并过滤空行。
     * 空行判定见 [PersonnelItem.isBlankRow]（序号/姓名/单位/类别/考察组全空视为空行）。
     */
    fun importPersonnel(inputStream: InputStream): List<PersonnelItem> {
        return EasyExcel.read(inputStream)
            .head(PersonnelItem::class.java)
            .sheet(0)
            .doReadSync<PersonnelItem>()
            .filterNot { it.isBlankRow() }
    }

    private fun PersonnelItem.isBlankRow(): Boolean =
        id == null &&
            name.isNullOrBlank() &&
            unit.isNullOrBlank() &&
            position.isNullOrBlank() &&
            inspectionTeam.isNullOrBlank()
}
