@import java.util.List

@param List<String> groups
@param int week

<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>RaspViewer</title>
    <link rel="stylesheet" type="text/css" href="/css/style.css">
</head>
<body style='height: 100%'>
    <header>
        <h1>Учебное расписание</h1>
        <h2>Идёт ${week} неделя</h2>
    </header>

    <main>
        <form>
            <div>
                <span>
                    <label>Группа</label>
                    <select name='group' hx-post="/subgroup" hx-target="#subgroup">
                        <option value="">-</option>
                    @for (String group : groups)
                        <option value="${group}">${group}</option>
                    @endfor
                    </select>
                </span>
                <span>
                    <span id='subgroup'></span>
                </span>
            </div>
            <div>
                <label>Показать расписание на</label>
                <span class='btnComposite'>
                    <button hx-get="/raspForToday" hx-target="#raspContent">Сегодня</button>
                    <button hx-get="/raspForTomorrow" hx-target="#raspContent">Завтра</button>
                    <button hx-get="/raspForThreeDays" hx-target="#raspContent">3 Дня</button>
                    <button hx-get="/raspForCurrentWeek" hx-target="#raspContent">Неделю</button>
                    <button hx-get="/raspForNextWeek" hx-target="#raspContent">Следующую неделю</button>
                </span>
            </div>
            <div>
                <span>
                    <label>Дата</label>
                    <input type="date" name="fromDate">
                    <span> - </span>
                    <input type="date" name="toDate">
                    <button hx-post="/rasp" hx-target="#raspContent">Поиск</button>
                </span>
            </div>
        </form>
        <div id='raspContent' class="raspContent"></div>
    </main>

    <footer>
        <div id='time' class="time">${java.time.LocalDateTime.now().toString()}</div>
        <script src="/js/htmx.min.js"></script>
    </footer>
</body>
</html>