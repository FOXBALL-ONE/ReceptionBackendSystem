package top.foxball.receptionbackendsystem.service.excel

import com.alibaba.excel.EasyExcel
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import top.foxball.receptionbackendsystem.datasource.excel.CarExcelRow
import top.foxball.receptionbackendsystem.datasource.excel.CarItem
import top.foxball.receptionbackendsystem.datasource.excel.StaffItem
import java.io.FileInputStream
import java.io.InputStream

/**
 * 乘车安排的 Excel 导入服务。
 *
 * 读取工作簿第 2 个 sheet（index=1，"乘车安排"），按 [CarExcelRow] 列结构解析为扁平行，
 * 再按车号聚合成 [CarItem]（含工作人员与乘车人员），
 * 对应活动下属的 [top.foxball.receptionbackendsystem.datasource.jdbc.Car] 实体。
 */
@Service
class CarExcelService {
    /** 从文件路径读取乘车安排 sheet。 */
    fun importCar(filePath: String): List<CarItem> {
        return FileInputStream(filePath).use { inputStream ->
            importCar(inputStream)
        }
    }

    /** 从上传文件读取乘车安排 sheet。 */
    fun importCar(file: MultipartFile): List<CarItem> {
        return file.inputStream.use { inputStream ->
            importCar(inputStream)
        }
    }

    /**
     * 读取"乘车安排" sheet 并按车号分组合并。
     * 车号非空视为新车起始行；后续无车号行的"工作人员"与"乘车人员"挂到当前车，
     * 同名同号的"工作人员"会去重。
     */
    fun importCar(inputStream: InputStream): List<CarItem> {
        val rows = EasyExcel.read(inputStream)
            .head(CarExcelRow::class.java)
            .sheet(1)
            .doReadSync<CarExcelRow>()

        return rows.toCarItems()
    }

    private fun List<CarExcelRow>.toCarItems(): List<CarItem> {
        val cars = mutableListOf<CarItem>()
        var currentCar: CarItem? = null

        for (row in this) {
            if (row.isBlankRow()) {
                continue
            }

            val carNumber = row.carNumber
            if (carNumber != null) {
                currentCar = CarItem(
                    carNumber = carNumber,
                    carPlate = row.carPlate?.trimToNull(),
                    driver = row.driver?.trimToNull(),
                    driverNumber = row.driverNumber?.trimToNull(),
                )
                cars += currentCar
            }

            val car = currentCar ?: continue

            val staffName = row.staffName?.trimToNull()
            val staffPhone = row.staffPhone?.trimToNull()
            if (staffName != null || staffPhone != null) {
                val staff = StaffItem(name = staffName, phone = staffPhone)
                if (car.staff.none { it.name == staff.name && it.phone == staff.phone }) {
                    car.staff += staff
                }
            }

            row.passengerName?.trimToNull()?.let { passengerName ->
                car.passengers += passengerName
            }
        }

        return cars
    }

    private fun CarExcelRow.isBlankRow(): Boolean =
        carNumber == null &&
            carPlate.isNullOrBlank() &&
            driver.isNullOrBlank() &&
            driverNumber.isNullOrBlank() &&
            staffName.isNullOrBlank() &&
            staffPhone.isNullOrBlank() &&
            passengerName.isNullOrBlank()

    private fun String.trimToNull(): String? = trim().takeIf { it.isNotEmpty() }
}
