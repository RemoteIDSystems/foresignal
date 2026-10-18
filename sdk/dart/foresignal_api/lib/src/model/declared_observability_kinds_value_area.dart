//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'declared_observability_kinds_value_area.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class DeclaredObservabilityKindsValueArea {
  /// Returns a new [DeclaredObservabilityKindsValueArea] instance.
  DeclaredObservabilityKindsValueArea({

    required  this.type,

     this.bbox,

     this.regionCodes,
  });

  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final DeclaredObservabilityKindsValueAreaTypeEnum type;



      /// [minLon, minLat, maxLon, maxLat] (WGS84)
  @JsonKey(
    
    name: r'bbox',
    required: false,
    includeIfNull: false,
  )


  final List<num>? bbox;



  @JsonKey(
    
    name: r'regionCodes',
    required: false,
    includeIfNull: false,
  )


  final List<String>? regionCodes;





    @override
    bool operator ==(Object other) => identical(this, other) || other is DeclaredObservabilityKindsValueArea &&
      other.type == type &&
      other.bbox == bbox &&
      other.regionCodes == regionCodes;

    @override
    int get hashCode =>
        type.hashCode +
        (bbox == null ? 0 : bbox.hashCode) +
        regionCodes.hashCode;

  factory DeclaredObservabilityKindsValueArea.fromJson(Map<String, dynamic> json) => _$DeclaredObservabilityKindsValueAreaFromJson(json);

  Map<String, dynamic> toJson() => _$DeclaredObservabilityKindsValueAreaToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum DeclaredObservabilityKindsValueAreaTypeEnum {
@JsonValue(r'global')
global(r'global'),
@JsonValue(r'bbox')
bbox(r'bbox'),
@JsonValue(r'regions')
regions(r'regions');

const DeclaredObservabilityKindsValueAreaTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


