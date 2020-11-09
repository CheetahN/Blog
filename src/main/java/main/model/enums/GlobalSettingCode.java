package main.model.enums;

public enum GlobalSettingCode {
    MULTIUSER_MODE("Многопользовательский режим"),
    POST_PREMODERATION("Премодерация постов"),
    STATISTICS_IS_PUBLIC("Показывать всем статистику блога");

    private String name;
    GlobalSettingCode(String name) {
        this.name = name;
    }
    public String getName(){
        return name;
    }
}
