package br.com.vrum.util;

import jakarta.faces.component.UIComponent;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.convert.FacesConverter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@FacesConverter("localDateConverter")
public class LocalDateConverter implements Converter {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public Object getAsObject(FacesContext ctx, UIComponent comp, String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            return LocalDate.parse(value.trim(), FORMATTER);
        } catch (Exception e) {
            throw new ConverterException(new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Data inválida",
                    "Informe uma data válida no formato dd/MM/aaaa."));
        }
    }

    @Override
    public String getAsString(FacesContext ctx, UIComponent comp, Object value) {
        if (value == null) return "";
        if (value instanceof LocalDate) {
            return ((LocalDate) value).format(FORMATTER);
        }
        return value.toString();
    }
}
