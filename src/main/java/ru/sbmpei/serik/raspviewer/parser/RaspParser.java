package ru.sbmpei.serik.raspviewer.parser;

import java.io.FileInputStream;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
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
import ru.sbmpei.serik.raspviewer.parser.model.StudSubject.SubjectInfo;
import ru.sbmpei.serik.raspviewer.parser.model.WorkDay;
import ru.sbmpei.serik.raspviewer.parser.model.WorkSubject;

/**
 *
 * @author SLakeev
 */
public class RaspParser {

    private static final Logger LOGGER = LogManager.getLogger();

    private final int GROUP_NAME_ROW = 1;
    private final int DAY_OF_WEEK_COLUMN = 0;
    private final int WORK_TIME_COLUMN = 1;

    private final CellRangeAddress EMPTY_ADDRESS = CellRangeAddress.valueOf("A1:A1");

    private final Pattern CLASSES_INFO_PATTERN = Pattern.compile("\\d\\sи\\s\\d\\sпара");
    private final Pattern SUBJECT_FACTOR = Pattern.compile("([a-я]+\\.)+\\s[A-Я].{1,20}\\s[A-Я]\\.[A-Я]\\.");

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

    public Map<String, StudGroup> parse() throws Exception {
        LOGGER.debug("Begin parse file: {}", fileName);
        try (Workbook rasp = new HSSFWorkbook(new FileInputStream(fileName))) {
            IntStream.range(0, rasp.getNumberOfSheets())
                    .mapToObj(rasp::getSheetAt).forEach(this::parseSheet);
        } catch (Exception e) {
            LOGGER.warn("Неудалось обработать файл ({})", fileName);
            throw e;
        }
        return studGroups;
    }

    private void parseSheet(Sheet sheet) {
        LOGGER.debug("Begin parse sheet: {}", sheet.getSheetName());

        int rowIndex = GROUP_NAME_ROW + 1;
        for (int i = rowIndex;; i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                break;
            }
            for (int j = WORK_TIME_COLUMN + 1;; j++) {
                Cell cell = row.getCell(j);
                if (cell == null) {
                    break;
                }

                CellRangeAddress region = mergedRegion(sheet, cell.getAddress());
                LOGGER.debug("Current cell ({}, {})", i, j);
                LOGGER.debug("Region: {}", region);

                if (cellAddressEquals(region, EMPTY_ADDRESS)) {
                    fillStudSubject(sheet, cellValue(cell), cell, 1);
                } else {
                    Cell firstCell = firstCellFromRegion(sheet, region);
                    int numberOfColumns = region.getLastColumn() - region.getFirstColumn() + 1;
                    fillStudSubject(sheet, cellValue(firstCell), cell, numberOfColumns);
                    j = region.getLastColumn();
                }
            }
        }
    }

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

    private String cellValue(Cell cell) {
        return switch (cell.getCellType()) {
            case CellType.STRING ->
                cell.getStringCellValue();
            case CellType.NUMERIC ->
                String.valueOf((int) cell.getNumericCellValue());
            default ->
                "";
        };
    }

    private boolean cellAddressEquals(CellRangeAddress address1, CellRangeAddress address2) {
        if (address1 == null || address2 == null) {
            return address1 == address2;
        }
        return address1.formatAsString().equals(address2.formatAsString());
    }

    private void fillStudSubject(Sheet sheet, String value, Cell c, int numberOfColumns) {
        int cell = c.getColumnIndex();
        int row = c.getRowIndex();

        LOGGER.debug("Cell value is '{}'", value);

        String groupName = groupNameForColumn(sheet, cell);
        if (StringUtils.isBlank(groupName)) {
            LOGGER.trace("Group name is blank for column: {}", cell);
            return;
        }
        StudGroup group = studGroups.get(groupName);
        if (group == null) {
            group = new StudGroup(new HashMap<>(), sheet.getSheetName());
            studGroups.put(groupName, group);
        }

        DayOfWeek day = workDayForRow(sheet, row);
        if (day == null) {
            LOGGER.trace("Not found day of week for row: {}", row);
            return;
        }
        WorkDay workDay = group.days().get(day);
        if (workDay == null) {
            workDay = new WorkDay(new HashMap<>());
            group.days().put(day, workDay);
        }

        String workTimeKey = workTimeForRow(sheet, row);
        if (StringUtils.isBlank(workTimeKey)) {
            LOGGER.trace("Work time is blank for row: {}", row);
            return;
        }

        WorkSubject workSubject = workDay.workSubjects().get(workTimeKey);
        if (workSubject == null) {
            workSubject = new WorkSubject();
            workDay.workSubjects().put(workTimeKey, workSubject);
        }

        CellRangeAddress groupRange = groupRangeForColumn(sheet, cell);

        LOGGER.debug("Group cell range is {}", groupRange);

        LOGGER.debug("Group: '{}', DayOfWeek: '{}', Time: '{}', Value: '{}'",
                groupName, day, workTimeKey, value);

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
                    int currentSubjectNumber = workDay.workSubjects().size();
                    int anotherClass = anotherClassFromClassesInfo(studSubject, currentSubjectNumber);

                    CellRangeAddress subjectRegion = mergedRegion(sheet, row, cell);
                    String anotherWorkTime = workTimeForClasses(sheet, currentSubjectNumber, anotherClass, subjectRegion);

                    if (containsSubject(studSubject)) {
                        studSubject.info().add(new SubjectInfo(anotherWorkTime, SubjectInfo.Type.ANOTHER_TIME));
                        workSubject.setOddSubject(studSubject);
                    } else {
                        WorkSubject subject = workDay.workSubjects().get(anotherWorkTime);
                        subject.getOddSubject().info().add(new SubjectInfo(studSubject.title(), SubjectInfo.Type.CLASSES));
                        subject.getOddSubject().info().add(new SubjectInfo(workTimeKey, SubjectInfo.Type.ANOTHER_TIME));
                        workSubject.setOddSubject(StudSubject.EMPTY);
                    }
                } else {
                    workSubject.setOddSubject(studSubject);
                }
            }
        } else if (cell == groupRange.getLastColumn()) {
            if (workSubject.getDenominatorSubject() == null
                    && !studSubjectIsNullOrEmpty(workSubject.getNumeratorSubject())) {
                workSubject.getNumeratorSubject().info().add(new SubjectInfo(studSubject.title(), SubjectInfo.Type.AUDIENCE)); // Аудитория по числителю
            } else if (!studSubjectIsNullOrEmpty(workSubject.getDenominatorSubject())) {
                workSubject.getDenominatorSubject().info().add(new SubjectInfo(studSubject.title(), SubjectInfo.Type.AUDIENCE)); // Аудитория по знаменателю
            }
        } else if (workSubject.getEvenSubject() == null) { // если чётная неделя |0|1|
            if (containClassesInfo(studSubject)) {
                int currentSubjectNumber = workDay.workSubjects().size();
                int anotherClass = anotherClassFromClassesInfo(studSubject, currentSubjectNumber);

                CellRangeAddress subjectRegion = mergedRegion(sheet, row, cell);
                String anotherWorkTime = workTimeForClasses(sheet, currentSubjectNumber, anotherClass, subjectRegion);

                if (containsSubject(studSubject)) {
                    studSubject.info().add(new SubjectInfo(anotherWorkTime, SubjectInfo.Type.ANOTHER_TIME));
                    workSubject.setEvenSubject(studSubject);
                } else {
                    WorkSubject subject = workDay.workSubjects().get(workTimeForRow(sheet, row - 1)); // На предыдущей строке искать пару
                    subject.getEvenSubject().info().add(new SubjectInfo(studSubject.title(), SubjectInfo.Type.CLASSES));
                    subject.getEvenSubject().info().add(new SubjectInfo(workTimeKey, SubjectInfo.Type.ANOTHER_TIME));
                    workSubject.setEvenSubject(StudSubject.EMPTY);
                }
            } else {
                workSubject.setEvenSubject(studSubject);
            }
        }

    }

    private boolean containClassesInfo(StudSubject studSubject) {
        return CLASSES_INFO_PATTERN.matcher(studSubject.title()).find();
    }

    private boolean containsSubject(StudSubject studSubject) {
        return SUBJECT_FACTOR.matcher(studSubject.title()).find();
    }

    private int anotherClassFromClassesInfo(StudSubject studSubject, int currentClass) {
        Matcher matcher = CLASSES_INFO_PATTERN.matcher(studSubject.title());
        if (matcher.find()) {
            String[] classes = matcher.group()
                    .replaceAll("и", "")
                    .replaceAll("пара", "")
                    .split("\\s");
            int[] classesArray = Stream.of(classes)
                    .filter(StringUtils::isNoneBlank)
                    .mapToInt(Integer::parseInt)
                    .filter(it -> it != currentClass)
                    .toArray();
            if (classesArray.length <= 0) {
                throw new IllegalArgumentException("Bad classes info in title: " + matcher.group());
            }
            if (classesArray.length > 1) {
                IO.println("Значение ячейки: '" + studSubject.title() + "'");
                IO.println("Текущая и указанные пары не совпадают.");
                IO.println("Текущая для этой строки пара: " + currentClass + ". Указано: " + matcher.group());
                String number = IO.readln("Вместо (" + matcher.group() + "), указать " + currentClass + " и (цифра): ");
                return Integer.parseInt(number);
            } else {
                return classesArray[0];
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    private String workTimeForRow(Sheet sheet, int row) {
        CellRangeAddress workTimeRegion = mergedRegion(sheet, row, WORK_TIME_COLUMN);
        if (workTimeRegion == EMPTY_ADDRESS) {
            return null;
        }
        Cell workTime = firstCellFromRegion(sheet, workTimeRegion);
        return cellValue(workTime);
    }

    private String workTimeForClasses(Sheet sheet, int current, int another, CellRangeAddress subjectRegion) {
        return (current < another)
                ? workTimeForRow(sheet, subjectRegion.getLastRow() + 1)
                : workTimeForRow(sheet, subjectRegion.getFirstRow() - 1);
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
