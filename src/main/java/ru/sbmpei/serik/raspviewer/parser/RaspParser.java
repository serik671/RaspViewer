package ru.sbmpei.serik.raspviewer.parser;

import java.io.FileInputStream;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;
import ru.sbmpei.serik.raspviewer.parser.model.StudGroup;
import ru.sbmpei.serik.raspviewer.parser.model.StudSubject;
import ru.sbmpei.serik.raspviewer.parser.model.WorkDay;
import ru.sbmpei.serik.raspviewer.parser.model.WorkSubject;
import ru.sbmpei.serik.raspviewer.parser.model.WorkTime;

/**
 *
 * @author SLakeev
 */
public class RaspParser {

    private final int GROUP_NAME_ROW = 1;
    private final int DAY_OF_WEEK_COLUMN = 0;
    private final int WORK_TIME_COLUMN = 1;

    private final CellRangeAddress EMPTY_ADDRESS = CellRangeAddress.valueOf("A1:A1");

    private final Pattern classesInfoPattern = Pattern.compile("(\\d\\sи\\s)*\\d\\sпара");

    public static Map<String, DayOfWeek> dayOfWeek = Collections.unmodifiableMap(
            Map.of(
                    "понедельник", DayOfWeek.MONDAY,
                    "вторник", DayOfWeek.TUESDAY,
                    "среда", DayOfWeek.WEDNESDAY,
                    "четверг", DayOfWeek.THURSDAY,
                    "пятница", DayOfWeek.FRIDAY,
                    "суббота", DayOfWeek.SATURDAY
            )
    );

    private final String fileName;
    private final Map<String, StudGroup> studGroups = new HashMap();

    public RaspParser(String fileName) {
        this.fileName = fileName;
    }

    public Map<String, StudGroup> parse() {
        try (Workbook rasp = new HSSFWorkbook(new FileInputStream(fileName))) {
            IntStream.range(0, rasp.getNumberOfSheets())
                    .limit(1) // TODO: remove limit for production
                    .mapToObj(rasp::getSheetAt).forEach(this::parseSheet);
        } catch (Exception e) {
            IO.println("Exception: " + e.getMessage());
            e.printStackTrace();
        }
        return studGroups;
    }

    private void parseSheet(Sheet sheet) {
        IO.println(sheet.getSheetName());
//        parseGroups(sheet);
        parseStudDays(sheet);
    }

//    private void parseGroups(Sheet sheet) {
//        Row row = sheet.getRow(GROUP_NAME_ROW);
//        if (row != null) { // TODO: add throw exception if null
//            List<StudGroup> groups = new ArrayList<>();
//            for (int i = 0;; i++) {
//                Cell cell = row.getCell(i);
//                if (cell == null) {
//                    break;
//                }
//                String groupName = cellValue(cell);
//                if (StringUtils.isNotBlank(groupName)) {
//                    groups.add(new StudGroup(groupName, mergedRegion(sheet, cell.getAddress())));
//                }
//            }
//            IO.println(groups);
//        }
//    }
    private CellRangeAddress mergedRegion(Sheet sheet, CellAddress cellAddress) {
        return sheet.getMergedRegions().stream()
                .filter(r -> r.isInRange(cellAddress))
                .findFirst().orElse(EMPTY_ADDRESS);
    }

    private CellRangeAddress mergedRegion(Sheet sheet, int rowInd, int colInd) {
        return sheet.getMergedRegions().stream()
                .filter(r -> r.isInRange(rowInd, colInd))
                .findFirst().orElse(EMPTY_ADDRESS);
    }

//    private Stream<CellRangeAddress>
    private String cellValue(Cell cell) {
        return switch (cell.getCellType()) {
            case CellType.STRING ->
                cell.getStringCellValue();
            case CellType.NUMERIC ->
                String.valueOf(cell.getNumericCellValue());
            default ->
                "";
        };
    }

    private void parseStudDays(Sheet sheet) {
        int rowIndex = GROUP_NAME_ROW + 1;
        for (int i = rowIndex;; i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                break;
            }
            IO.print(row.getRowNum() + ": ");
            for (int j = WORK_TIME_COLUMN + 1;; j++) {
                Cell cell = row.getCell(j);
                if (cell == null) {
                    break;
                }
                CellRangeAddress region = mergedRegion(sheet, cell.getAddress());

                if (cellAddressEquals(region, EMPTY_ADDRESS)) {
                    fillStudSubject(sheet, cellValue(cell), row.getRowNum(), cell.getColumnIndex(), 1);
                } else {
                    Cell firstCell = firstCellFromRegion(sheet, region);
                    int numberOfColumns = region.getLastColumn() - region.getFirstColumn() + 1;
                    fillStudSubject(sheet, cellValue(firstCell), row.getRowNum(), cell.getColumnIndex(), numberOfColumns);
                    j = region.getLastColumn();
                }
            }
        }
    }

    private boolean cellAddressEquals(CellRangeAddress address1, CellRangeAddress address2) {
        if (address1 == null || address2 == null) {
            return address1 == address2;
        }
        return address1.formatAsString().equals(address2.formatAsString());
    }

    private void fillStudSubject(Sheet sheet, String value, int row, int cell, int numberOfColumns) {
        String groupName = groupNameForColumn(sheet, cell);
        if (StringUtils.isBlank(groupName)) {
            return;
        }
        StudGroup group = studGroups.get(groupName);
        if (group == null) {
            group = new StudGroup(new HashMap<>());
            studGroups.put(groupName, group);
        }

        DayOfWeek day = workDayForRow(sheet, row);
        if (day == null) {
            return;
        }
        WorkDay workDay = group.days().get(day);
        if (workDay == null) {
            workDay = new WorkDay(new HashMap<>());
            group.days().put(day, workDay);
        }

        String workTimeKey = workTimeForRow(sheet, row);
        if (StringUtils.isBlank(workTimeKey)) {
            return;
        }
        WorkSubject workSubject = workDay.workSubjects().get(workTimeKey);
        if (workSubject == null) {
            workSubject = new WorkSubject();
            workDay.workSubjects().put(workTimeKey, workSubject);
        }

        CellRangeAddress groupRange = groupRangeForColumn(sheet, cell);

        StudSubject studSubject = StringUtils.isBlank(value)
                ? StudSubject.EMPTY : new StudSubject(value, new ArrayList<>());

        if (cell == groupRange.getFirstColumn()) { // если нечётная неделя |1|0|
            if (numberOfColumns > 1) { // числитель или знаменатель
                if (workSubject.getNumeratorSubject() == null) { // если числитель
                    workSubject.setNumeratorSubject(studSubject);
                } else { // иначе знаменатель
                    workSubject.setDenominatorSubject(studSubject);
                }
            } else if (workSubject.getOddSubject() == null) { // нечётная неделя
                if (containClassesInfo(studSubject)) {
                    WorkSubject subject = workDay.workSubjects().get(workTimeForRow(sheet, row - 1)); // На предыдущей строке искать пару
                    subject.getOddSubject().info().add(studSubject.title());
                    workSubject.setOddSubject(StudSubject.EMPTY);
                } else {
                    workSubject.setOddSubject(studSubject);
                }
            }
        } else if (cell == groupRange.getLastColumn()) {
            if (workSubject.getDenominatorSubject() == null
                    && !studSubjectIsNullOrEmpty(workSubject.getNumeratorSubject())) {
                workSubject.getNumeratorSubject().info().add(studSubject.title()); // Аудитория по числителю
            } else if (!studSubjectIsNullOrEmpty(workSubject.getDenominatorSubject())) {
                workSubject.getDenominatorSubject().info().add(studSubject.title()); // Аудитория по знаменателю
            }
        } else if (workSubject.getEvenSubject() == null) { // если чётная неделя |0|1|
            if (containClassesInfo(studSubject)) {
                WorkSubject subject = workDay.workSubjects().get(workTimeForRow(sheet, row - 1)); // На предыдущей строке искать пару
                subject.getEvenSubject().info().add(studSubject.title());
                workSubject.setEvenSubject(StudSubject.EMPTY);
            } else {
                workSubject.setEvenSubject(studSubject);
            }
        }

    }

    private boolean containClassesInfo(StudSubject studSubject) {
        return classesInfoPattern.matcher(studSubject.title()).find();
    }

    private String workTimeForRow(Sheet sheet, int row) {
        CellRangeAddress workTimeRegion = mergedRegion(sheet, row, WORK_TIME_COLUMN);
        if (workTimeRegion == EMPTY_ADDRESS) {
            return null;
        }
        Cell workTime = firstCellFromRegion(sheet, workTimeRegion);
        return cellValue(workTime);
    }

    private DayOfWeek workDayForRow(Sheet sheet, int row) {
        CellRangeAddress workDayRegion = mergedRegion(sheet, row, DAY_OF_WEEK_COLUMN);
        if (workDayRegion == EMPTY_ADDRESS) {
            return null;
        }
        Cell workDay = firstCellFromRegion(sheet, workDayRegion);
        return dayOfWeek.get(cellValue(workDay).toLowerCase());
    }

    private String groupNameForColumn(Sheet sheet, int colInd) {
        CellRangeAddress groupRegion = groupRangeForColumn(sheet, colInd);
        if (groupRegion == EMPTY_ADDRESS) {
            return null;
        }
        Cell cell = firstCellFromRegion(sheet, groupRegion);
        return cellValue(cell);
    }

    private boolean studSubjectIsNullOrEmpty(StudSubject s) {
        return s == null || s == StudSubject.EMPTY;
    }

    private CellRangeAddress groupRangeForColumn(Sheet sheet, int colInd) {
        return mergedRegion(sheet, GROUP_NAME_ROW, colInd);
    }

    private Cell firstCellFromRegion(Sheet sheet, CellRangeAddress address) {
        return sheet.getRow(address.getFirstRow()).getCell(address.getFirstColumn());
    }

}
