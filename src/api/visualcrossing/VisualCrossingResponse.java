package api.visualcrossing;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class VisualCrossingResponse {

    private String latitude;
    private String longitude;
    private String resolvedAddress;
    private String timezone;
    private VisualCrossingCurrentConditions currentConditions;
}
