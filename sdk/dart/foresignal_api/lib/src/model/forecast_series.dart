//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/weather_section_status.dart';
import 'package:foresignal_api/src/model/forecast_series_point.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'forecast_series.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ForecastSeries {
  /// Returns a new [ForecastSeries] instance.
  ForecastSeries({

    required  this.provider,

     this.run,

    required  this.updatedAt,

     this.validFrom,

     this.status,

    required  this.items,
  });

  @JsonKey(
    
    name: r'provider',
    required: true,
    includeIfNull: false,
  )


  final ForecastSeriesProviderEnum provider;



      /// Model run (gfs, hrrr)
  @JsonKey(
    
    name: r'run',
    required: false,
    includeIfNull: false,
  )


  final String? run;



  @JsonKey(
    
    name: r'updatedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime updatedAt;



  @JsonKey(
    
    name: r'validFrom',
    required: false,
    includeIfNull: false,
  )


  final DateTime? validFrom;



  @JsonKey(
    
    name: r'status',
    required: false,
    includeIfNull: false,
  )


  final WeatherSectionStatus? status;



  @JsonKey(
    
    name: r'items',
    required: true,
    includeIfNull: false,
  )


  final List<ForecastSeriesPoint> items;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ForecastSeries &&
      other.provider == provider &&
      other.run == run &&
      other.updatedAt == updatedAt &&
      other.validFrom == validFrom &&
      other.status == status &&
      other.items == items;

    @override
    int get hashCode =>
        provider.hashCode +
        (run == null ? 0 : run.hashCode) +
        updatedAt.hashCode +
        validFrom.hashCode +
        status.hashCode +
        items.hashCode;

  factory ForecastSeries.fromJson(Map<String, dynamic> json) => _$ForecastSeriesFromJson(json);

  Map<String, dynamic> toJson() => _$ForecastSeriesToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ForecastSeriesProviderEnum {
@JsonValue(r'gfs')
gfs(r'gfs'),
@JsonValue(r'hrrr')
hrrr(r'hrrr'),
@JsonValue(r'ifs')
ifs(r'ifs'),
@JsonValue(r'aifs')
aifs(r'aifs'),
@JsonValue(r'nws')
nws(r'nws');

const ForecastSeriesProviderEnum(this.value);

final String value;

@override
String toString() => value;
}


