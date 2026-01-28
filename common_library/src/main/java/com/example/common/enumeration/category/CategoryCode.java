package com.example.common.enumeration.category;

public enum CategoryCode {

    WOMAN_CLOTHES("Женская одежда"),
    MAN_CLOTHES("Мужская одежда"),
    COSMETIC("Косметика"),
    APPLIANCES("Бытовая техника"),
    ELECTRONICS("Электроника"),
    FOOD("Продукты питания"),
    FURNITURE("Мебель"),
    BOOKS("Книги"),
    PHARMACY("Аптека");

    private String name;

    CategoryCode(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }




}
