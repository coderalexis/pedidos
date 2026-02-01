package com.liverpool.customer.infrastructure.adapter.out.persistence.document;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Document(collection = "customers")
@CompoundIndexes({
    @CompoundIndex(name = "idx_nombre_completo",
                   def = "{'apellidoPaterno': 1, 'apellidoMaterno': 1, 'nombre': 1}"),
    @CompoundIndex(name = "idx_estado_ciudad",
                   def = "{'direccionEnvio.estado': 1, 'direccionEnvio.ciudad': 1}")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerDocument {

    @Id
    private String id;

    @Field("customer_id")
    @Indexed(unique = true)
    private String customerId;

    @Field("nombre")
    private String nombre;

    @Field("apellido_paterno")
    @Indexed
    private String apellidoPaterno;

    @Field("apellido_materno")
    private String apellidoMaterno;

    @Field("email")
    @Indexed(unique = true)
    private String email;

    @Field("direccion_envio")
    private AddressDocument direccionEnvio;

    @Field("activo")
    @Indexed
    @Builder.Default
    private Boolean activo = true;

    @CreatedDate
    @Field("fecha_creacion")
    @Indexed
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    @Field("fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
}
