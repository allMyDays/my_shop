package com.example.managerapp.entity;

import java.util.ArrayList;
import java.util.List;

public record ProductRecord(long id, String title, int price, String description, String previewImageFileName, List<String> imageFileNames) {

}
