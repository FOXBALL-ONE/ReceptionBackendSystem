package top.foxball.receptionbackendsystem.service.excel

import com.alibaba.excel.EasyExcel
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import top.foxball.receptionbackendsystem.datasource.excel.PersonnelItem
import java.io.FileInputStream
import java.io.InputStream

@Service
class PersonnelExcelService {

    fun importPersonnel(filePath: String): List<PersonnelItem> {
        return FileInputStream(filePath).use { inputStream ->
            importPersonnel(inputStream)
        }
    }

    fun importPersonnel(file: MultipartFile): List<PersonnelItem> {
        return file.inputStream.use { inputStream ->
            importPersonnel(inputStream)
        }
    }

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
