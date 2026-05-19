package ru.sbmpei.serik.raspviewer.parser.model;

/**
 *
 * @author SLakeev
 */
public class WorkSubject {

    private StudSubject numeratorSubject; // Числитель (предмет по числителю)
    private StudSubject denominatorSubject; // Знаменатель (предмет по знаменателю)
    private StudSubject evenSubject; // Чётный (предмет по чётным неделям)
    private StudSubject oddSubject; // Нечётный (предмет по нечётным неделям)

    public WorkSubject() {
    }

    public StudSubject getNumeratorSubject() {
        return numeratorSubject;
    }

    public void setNumeratorSubject(StudSubject numeratorSubject) {
        this.numeratorSubject = numeratorSubject;
    }

    public StudSubject getDenominatorSubject() {
        return denominatorSubject;
    }

    public void setDenominatorSubject(StudSubject denominatorSubject) {
        this.denominatorSubject = denominatorSubject;
    }

    public StudSubject getEvenSubject() {
        return evenSubject;
    }

    public void setEvenSubject(StudSubject evenSubject) {
        this.evenSubject = evenSubject;
    }

    public StudSubject getOddSubject() {
        return oddSubject;
    }

    public void setOddSubject(StudSubject oddSubject) {
        this.oddSubject = oddSubject;
    }

    @Override
    public String toString() {
        return "WorkSubject{\nnumeratorSubject=" + numeratorSubject + "\ndenominatorSubject=" + denominatorSubject + "\nevenSubject=" + evenSubject + "\noddSubject=" + oddSubject + "\n}";
    }

}
