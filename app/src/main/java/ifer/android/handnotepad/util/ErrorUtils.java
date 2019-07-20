package ifer.android.handnotepad.util;

import java.io.IOException;
import java.lang.annotation.Annotation;


import ifer.android.handnotepad.api.ApiClient;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Response;

/**
 * Created by ifer on 24/7/2017.
 */

public class ErrorUtils {
    public static APIError parseError(Response<?> response) {
        Converter<ResponseBody, APIError> converter =
                ApiClient.getRetrofit()
                        .responseBodyConverter(APIError.class, new Annotation[0]);

        APIError error;

        try {
            error = converter.convert(response.errorBody());
        } catch (IOException e) {
            return new APIError();
        }

        return error;
    }
}
