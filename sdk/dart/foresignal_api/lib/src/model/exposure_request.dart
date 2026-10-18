//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/geometry.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'exposure_request.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ExposureRequest {
  /// Returns a new [ExposureRequest] instance.
  ExposureRequest({

     this.geometry,

     this.placeId,

     this.eventId,

     this.population,

     this.dependents = true,
  });

  @JsonKey(
    
    name: r'geometry',
    required: false,
    includeIfNull: false,
  )


  final Geometry? geometry;



  @JsonKey(
    
    name: r'placeId',
    required: false,
    includeIfNull: false,
  )


  final String? placeId;



      /// ULID identifier
  @JsonKey(
    
    name: r'eventId',
    required: false,
    includeIfNull: false,
  )


  final String? eventId;



      /// auto: section reports not-installed without a pack; required: 409 PACK_NOT_INSTALLED
  @JsonKey(
    
    name: r'population',
    required: false,
    includeIfNull: false,
  )


  final ExposureRequestPopulationEnum? population;



      /// Include the infrastructure dependents summary (104)
  @JsonKey(
    defaultValue: true,
    name: r'dependents',
    required: false,
    includeIfNull: false,
  )


  final bool? dependents;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ExposureRequest &&
      other.geometry == geometry &&
      other.placeId == placeId &&
      other.eventId == eventId &&
      other.population == population &&
      other.dependents == dependents;

    @override
    int get hashCode =>
        geometry.hashCode +
        placeId.hashCode +
        eventId.hashCode +
        population.hashCode +
        dependents.hashCode;

  factory ExposureRequest.fromJson(Map<String, dynamic> json) => _$ExposureRequestFromJson(json);

  Map<String, dynamic> toJson() => _$ExposureRequestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// auto: section reports not-installed without a pack; required: 409 PACK_NOT_INSTALLED
enum ExposureRequestPopulationEnum {
@JsonValue(r'auto')
auto(r'auto'),
@JsonValue(r'off')
off(r'off'),
@JsonValue(r'required')
required_(r'required');

const ExposureRequestPopulationEnum(this.value);

final String value;

@override
String toString() => value;
}


