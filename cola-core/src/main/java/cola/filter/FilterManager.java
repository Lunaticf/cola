package cola.filter;


/**
 * @author lcf
 * toy filter 实现
 */
public class FilterManager {
    public static AbstractBeforeFilter beforeFilter;
    public static AbstractAfterFilter afterFilter;

    private static AbstractAfterFilter tailAfterFilter;
    private static AbstractBeforeFilter tailBeforeFilter;

    public static void addBeforeFilter(AbstractBeforeFilter filter) {
        if (beforeFilter == null) {
            beforeFilter = filter;
            tailBeforeFilter = filter;
        } else {
            tailBeforeFilter.next = filter;
            tailBeforeFilter = tailBeforeFilter.next;
        }
    }

    public static void addAfterFilter(AbstractAfterFilter filter) {
        if (afterFilter == null) {
            afterFilter = filter;
            tailAfterFilter = filter;
        } else {
            tailAfterFilter.next = filter;
            tailAfterFilter = tailAfterFilter.next;
        }
    }
}
