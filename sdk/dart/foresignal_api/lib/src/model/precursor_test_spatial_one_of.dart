//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'precursor_test_spatial_one_of.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PrecursorTestSpatialOneOf {
  /// Returns a new [PrecursorTestSpatialOneOf] instance.
  PrecursorTestSpatialOneOf({

    required  this.withinKm,
  });

          // minimum: 0
  @JsonKey(
    
    name: r'withinKm',
    required: true,
    includeIfNull: false,
  )


  final num withinKm;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PrecursorTestSpatialOneOf &&
      other.withinKm == withinKm;

    @override
    int get hashCode =>
        withinKm.hashCode;

  factory PrecursorTestSpatialOneOf.fromJson(Map<String, dynamic> json) => _$PrecursorTestSpatialOneOfFromJson(json);

  Map<String, dynamic> toJson() => _$PrecursorTestSpatialOneOfToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

