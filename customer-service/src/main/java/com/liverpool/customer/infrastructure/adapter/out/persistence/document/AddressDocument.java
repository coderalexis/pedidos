package com.liverpool.customer.infrastructure.adapter.out.persistence.document;

import lombok.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressDocument {

    @Field("calle")
    private String calle;

    @Field("numero_exterior")
    private String numeroExterior;

    @Field("numero_interior")
    private String numeroInterior;

    @Field("colonia")
    private String colonia;

    @Field("ciudad")
    private String ciudad;

    @Field("estado")
    private String estado;

    @Field("codigo_postal")
    @Indexed
    private String codigoPostal;

    @Field("pais")
    @Builder.Default
    private String pais = "México";
}
