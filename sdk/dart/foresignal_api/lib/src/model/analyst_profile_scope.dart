//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/severity.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'analyst_profile_scope.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AnalystProfileScope {
  /// Returns a new [AnalystProfileScope] instance.
  AnalystProfileScope({

     this.sources,

     this.kinds,

     this.places,

     this.bbox,

     this.eventTypes,

     this.ruleTags,

     this.minSeverity,
  });

  @JsonKey(
    
    name: r'sources',
    required: false,
    includeIfNull: false,
  )


  final List<String>? sources;



  @JsonKey(
    
    name: r'kinds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? kinds;



  @JsonKey(
    
    name: r'places',
    required: false,
    includeIfNull: false,
  )


  final List<String>? places;



  @JsonKey(
    
    name: r'bbox',
    required: false,
    includeIfNull: false,
  )


  final List<num>? bbox;



  @JsonKey(
    
    name: r'eventTypes',
    required: false,
    includeIfNull: false,
  )


  final List<AnalystProfileScopeEventTypesEnum>? eventTypes;



  @JsonKey(
    
    name: r'ruleTags',
    required: false,
    includeIfNull: false,
  )


  final List<String>? ruleTags;



  @JsonKey(
    
    name: r'minSeverity',
    required: false,
    includeIfNull: false,
  )


  final Severity? minSeverity;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AnalystProfileScope &&
      other.sources == sources &&
      other.kinds == kinds &&
      other.places == places &&
      other.bbox == bbox &&
      other.eventTypes == eventTypes &&
      other.ruleTags == ruleTags &&
      other.minSeverity == minSeverity;

    @override
    int get hashCode =>
        sources.hashCode +
        kinds.hashCode +
        places.hashCode +
        (bbox == null ? 0 : bbox.hashCode) +
        eventTypes.hashCode +
        ruleTags.hashCode +
        minSeverity.hashCode;

  factory AnalystProfileScope.fromJson(Map<String, dynamic> json) => _$AnalystProfileScopeFromJson(json);

  Map<String, dynamic> toJson() => _$AnalystProfileScopeToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum AnalystProfileScopeEventTypesEnum {
@JsonValue(r'maritime-disruption')
maritimeDisruption(r'maritime-disruption'),
@JsonValue(r'aviation-anomaly')
aviationAnomaly(r'aviation-anomaly'),
@JsonValue(r'infrastructure-disruption')
infrastructureDisruption(r'infrastructure-disruption'),
@JsonValue(r'weather-impact')
weatherImpact(r'weather-impact'),
@JsonValue(r'seismic')
seismic(r'seismic'),
@JsonValue(r'custom')
custom(r'custom');

const AnalystProfileScopeEventTypesEnum(this.value);

final String value;

@override
String toString() => value;
}


