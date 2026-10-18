//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'precursor_test_spatial_one_of1.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PrecursorTestSpatialOneOf1 {
  /// Returns a new [PrecursorTestSpatialOneOf1] instance.
  PrecursorTestSpatialOneOf1({

    required  this.sameRegion,
  });

  @JsonKey(
    
    name: r'sameRegion',
    required: true,
    includeIfNull: false,
  )


  final PrecursorTestSpatialOneOf1SameRegionEnum sameRegion;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PrecursorTestSpatialOneOf1 &&
      other.sameRegion == sameRegion;

    @override
    int get hashCode =>
        sameRegion.hashCode;

  factory PrecursorTestSpatialOneOf1.fromJson(Map<String, dynamic> json) => _$PrecursorTestSpatialOneOf1FromJson(json);

  Map<String, dynamic> toJson() => _$PrecursorTestSpatialOneOf1ToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum PrecursorTestSpatialOneOf1SameRegionEnum {
@JsonValue(r'country')
country(r'country'),
@JsonValue(r'admin1')
admin1(r'admin1');

const PrecursorTestSpatialOneOf1SameRegionEnum(this.value);

final String value;

@override
String toString() => value;
}


