@import java.util.List

@param List<String> subgroups

@if (!subgroups.isEmpty())
	<label>Подгруппа</label>
	<select name='subgroup'>
		@for (String subgroup : subgroups)
			<option value='${subgroup}'>${subgroup}</option>
		@endfor
	</select>
@endif