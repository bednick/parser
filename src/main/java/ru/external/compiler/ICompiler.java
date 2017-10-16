package main.java.ru.external.compiler;

/**
 * Интерфейс отвечающий за преобразование данных из  внешних источников в классы
 */
public interface ICompiler<S extends java.io.InputStream,T> {

    public void loadData(S stream, Object[] args);

    public T getData();
}
