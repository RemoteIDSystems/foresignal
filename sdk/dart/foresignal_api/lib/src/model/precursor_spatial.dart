//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'precursor_spatial.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PrecursorSpatial {
  /// Returns a new [PrecursorSpatial] instance.
  PrecursorSpatial({

    required  this.rule,

     this.withinKm,

     this.regionLevel,
  });

  @JsonKey(
    
    name: r'rule',
    required: true,
    includeIfNull: false,
  )


  final PrecursorSpatialRuleEnum rule;



          // minimum: 0
  @JsonKey(
    
    name: r'withinKm',
    required: false,
    includeIfNull: false,
  )


  final num? withinKm;



  @JsonKey(
    
    name: r'regionLevel',
    required: false,
    includeIfNull: false,
  )


  final PrecursorSpatialRegionLevelEnum? regionLevel;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PrecursorSpatial &&
      other.rule == rule &&
      other.withinKm == withinKm &&
      other.regionLevel == regionLevel;

    @override
    int get hashCode =>
        rule.hashCode +
        (withinKm == null ? 0 : withinKm.hashCode) +
        (regionLevel == null ? 0 : regionLevel.hashCode);

  factory PrecursorSpatial.fromJson(Map<String, dynamic> json) => _$PrecursorSpatialFromJson(json);

  Map<String, dynamic> toJson() => _$PrecursorSpatialToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum PrecursorSpatialRuleEnum {
@JsonValue(r'sameEntity')
sameEntity(r'sameEntity'),
@JsonValue(r'withinKm')
withinKm(r'withinKm'),
@JsonValue(r'sameRegion')
sameRegion(r'sameRegion'),
@JsonValue(r'none')
none(r'none');

const PrecursorSpatialRuleEnum(this.value);

final String value;

@override
String toString() => value;
}


enum PrecursorSpatialRegionLevelEnum {
@JsonValue(r'country')
country(r'country'),
@JsonValue(r'admin1')
admin1(r'admin1');

const PrecursorSpatialRegionLevelEnum(this.value);

final String value;

@override
String toString() => value;
}


