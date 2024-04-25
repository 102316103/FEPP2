package com.syscom.fep.frmcommon.parse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.Date;

/**
 * 針對Date類型的字串做特殊處理
 *
 * @param <T>
 * @author Richard
 */
public class GsonDateParser<T> implements Parser<T, String> {
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Date.class, new TypeAdapter<Date>() {
                @Override
                public void write(JsonWriter jsonWriter, Date date) throws IOException {
                    jsonWriter.value(date == null ? StringUtils.EMPTY : FormatUtil.dateTimeFormat(date));
                }

                @Override
                public Date read(JsonReader jsonReader) throws IOException {
                    try {
                        return FormatUtil.parseDataTime(jsonReader.nextString(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS);
                    } catch (ParseException e) {
                        return null;
                    }
                }
            })
            .create();
    private Type type;

    public GsonDateParser(Type type) {
        this.type = type;
    }

    @Override
    public T readIn(String jsonStr) throws Exception {
        try {
            return gson.fromJson(jsonStr, this.type);
        } catch (JsonSyntaxException e) {
            throw ExceptionUtil.createException(e, e.getMessage());
        }
    }

    @Override
    public String writeOut(T t) throws Exception {
        return gson.toJson(t);
    }

    public final Type getType() {
        return type;
    }
}