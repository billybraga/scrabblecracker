package braga.utils;

public abstract class ParamedRunnable implements Runnable {
    protected Object[] params;

    public abstract void run();

    public ParamedRunnable(Object... params) {
        this.params = params;
    }
}
