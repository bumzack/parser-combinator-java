package at.grg.bumzack;


public class Result<T, ERROR> {

    private final T t;
    private final ERROR err;

    public Result(T t, ERROR err) {
        this.t = t;
        this.err = err;
    }

    public static <TT, EERROR> Result<TT, EERROR> of(final TT t, final EERROR err) {
        return new Result<>(t, err);
    }

    public T get() {
        return t;
    }

    public ERROR getErr() {
        return err;
    }

    @Override
    public String toString() {
        return "Result{" +
                "t=" + t +
                ", err=" + err +
                '}';
    }
}
