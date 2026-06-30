@import java.util.List
@import ru.sbmpei.serik.raspviewer.view.DayView
@import ru.sbmpei.serik.raspviewer.view.SubjectView

@param List<DayView> days

@for (DayView day : days)
	<table>
		<thead>
			<tr>
				<th colspan="3">${day.name()}</th>
			</tr>
		</thead>
		<tbody>
			@for (SubjectView subject : day.subjects())
				<tr>
					<td align="center" style="white-space: nowrap">${subject.time()}</td>
					<td align="center">${subject.title()}</td>
					@if ("EMPTY".equals(subject.audience()))
						<td align="center">-</td>
					@else
						<td align="center">${subject.audience()}</td>
					@endif
				</tr>
			@else
				<tr>
					<td align="center" colspan="3">Выходной</td>
				</tr>
			@endfor
		</tbody>
		<tfoot>
			<tr>
				<th colspan="3" align="center">${day.date()}  |  ${day.week()} неделя</th>
			</tr>
		</tfoot>
	</table>
@endfor