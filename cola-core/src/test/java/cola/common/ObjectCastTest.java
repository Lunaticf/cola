package cola.common;

public class ObjectCastTest {

    public static void main(String[] args) {
        Object obj = new Person();
        System.out.println(obj.getClass());

        func(1,2,3);
    }

    private static void func(Object... args) {
        for (Object obj : args) {
            System.out.println(obj.getClass().getName());
        }
    }

}
