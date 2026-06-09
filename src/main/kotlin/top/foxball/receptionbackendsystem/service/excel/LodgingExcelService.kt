package top.foxball.receptionbackendsystem.service.excel

import com.alibaba.excel.EasyExcel
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import top.foxball.receptionbackendsystem.datasource.excel.LodgingItem
import java.io.FileInputStream
import java.io.InputStream

@Service
class LodgingExcelService {
    fun importLodging(filePath: String): List<LodgingItem> {
        return FileInputStream(filePath).use { inputStream ->
            importLodging(inputStream)
        }
    }

    fun importLodging(file: MultipartFile): List<LodgingItem> {
        return file.inputStream.use { inputStream ->
            importLodging(inputStream)
        }
    }

    fun importLodging(inputStream: InputStream): List<LodgingItem> {
        return EasyExcel.read(inputStream)
            .head(LodgingItem::class.java)
            .sheet(3)
            .doReadSync<LodgingItem>()
    }
}