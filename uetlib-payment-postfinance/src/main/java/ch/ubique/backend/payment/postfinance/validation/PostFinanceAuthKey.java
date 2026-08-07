package ch.ubique.backend.payment.postfinance.validation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.RECORD_COMPONENT;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import ch.ubique.backend.payment.postfinance.internal.PostfinanceAuthKeyValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE, RECORD_COMPONENT})
@Retention(RUNTIME)
@Constraint(validatedBy = PostfinanceAuthKeyValidator.class)
@Documented
public @interface PostFinanceAuthKey {

    String message() default "value '${validatedValue}' must be a valid Base64-encoded key with at least 256 bits for HS256 signing";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

