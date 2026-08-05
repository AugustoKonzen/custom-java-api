package api.visualcrossing;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class VisualCrossingCurrentConditions {

    private String datetime;
    private int datetimeEpoch;
    private double temp;
    private double feelslike;
    private double humidity;
    private double precip;
    private double precipprob;
    private double windspeed;
    private double winddir;
    private double pressure;
    private double cloudcover;
    private String conditions;
    private String sunrise;
    private int sunriseEpoch;
    private String sunset;
    private int sunsetEpoch;
}
