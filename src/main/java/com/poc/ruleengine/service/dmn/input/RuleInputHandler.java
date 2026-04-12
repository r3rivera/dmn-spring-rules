package com.poc.ruleengine.service.dmn.input;

import com.poc.ruleengine.domain.InputField;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class RuleInputHandler {

    public <T> String getInputClassName(Class<T> type){
        return type.getSimpleName();
    }

    public <T> List<InputField> extractFieldAndType(Class<T> type){
        final Field[] fields = type.getDeclaredFields();

        final List<InputField> fieldList = new ArrayList<>();
        for (Field field : fields) {
            final InputField inputField = new InputField();
            inputField.setName(field.getName());
            inputField.setType(translateType(field.getType().getSimpleName().toLowerCase()));
            fieldList.add(inputField);
        }
        return fieldList;
    }

    private String translateType(String type){
        return switch (type) {
            case "integer" -> "number";
            default -> "string";
        };
    }
}
