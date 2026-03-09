package org.example.calc;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CalcController {

    @PostMapping(path = "/api/calc", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public CalcResponse calculate(@RequestBody CalcRequest request) {
        double a = request.a();
        double b = request.b();
        String op = request.op();

        double result = switch (op) {
            case "+" -> a + b;
            case "-" -> a - b;
            case "*" -> a * b;
            case "/" -> {
                if (b == 0.0) {
                    throw new IllegalArgumentException("Division by zero");
                }
                yield a / b;
            }
            default -> throw new IllegalArgumentException("Unsupported operation: " + op);
        };

        return new CalcResponse(result);
    }
}
