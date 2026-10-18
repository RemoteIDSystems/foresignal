//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/precursor_test_spatial_one_of.dart';
import 'package:foresignal_api/src/model/precursor_test_spatial_one_of1.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'precursor_test_spatial.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PrecursorTestSpatial {
  /// Returns a new [PrecursorTestSpatial] instance.
  PrecursorTestSpatial({

    required  this.withinKm,

    required  this.sameRegion,
  });

          // minimum: 0
  @JsonKey(
    
    name: r'withinKm',
    required: true,
    includeIfNull: false,
  )


  final num withinKm;



  @JsonKey(
    
    name: r'sameRegion',
    required: true,
    includeIfNull: false,
  )


  final PrecursorTestSpatialSameRegionEnum sameRegion;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PrecursorTestSpatial &&
      other.withinKm == withinKm &&
      other.sameRegion == sameRegion;

    @override
    int get hashCode =>
        withinKm.hashCode +
        sameRegion.hashCode;

  factory PrecursorTestSpatial.fromJson(Map<String, dynamic> json) => _$PrecursorTestSpatialFromJson(json);

  Map<String, dynamic> toJson() => _$PrecursorTestSpatialToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum PrecursorTestSpatialSameRegionEnum {
@JsonValue(r'country')
country(r'country'),
@JsonValue(r'admin1')
admin1(r'admin1');

const PrecursorTestSpatialSameRegionEnum(this.value);

final String value;

@override
String toString() => value;
}


