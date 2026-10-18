//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'place_weather_digest_period_hazards_entered_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PlaceWeatherDigestPeriodHazardsEnteredInner {
  /// Returns a new [PlaceWeatherDigestPeriodHazardsEnteredInner] instance.
  PlaceWeatherDigestPeriodHazardsEnteredInner({

    required  this.kind,

    required  this.title,

    required  this.at,

     this.ref,
  });

  @JsonKey(
    
    name: r'kind',
    required: true,
    includeIfNull: false,
  )


  final String kind;



  @JsonKey(
    
    name: r'title',
    required: true,
    includeIfNull: false,
  )


  final String title;



  @JsonKey(
    
    name: r'at',
    required: true,
    includeIfNull: false,
  )


  final DateTime at;



  @JsonKey(
    
    name: r'ref',
    required: false,
    includeIfNull: false,
  )


  final String? ref;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PlaceWeatherDigestPeriodHazardsEnteredInner &&
      other.kind == kind &&
      other.title == title &&
      other.at == at &&
      other.ref == ref;

    @override
    int get hashCode =>
        kind.hashCode +
        title.hashCode +
        at.hashCode +
        ref.hashCode;

  factory PlaceWeatherDigestPeriodHazardsEnteredInner.fromJson(Map<String, dynamic> json) => _$PlaceWeatherDigestPeriodHazardsEnteredInnerFromJson(json);

  Map<String, dynamic> toJson() => _$PlaceWeatherDigestPeriodHazardsEnteredInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

