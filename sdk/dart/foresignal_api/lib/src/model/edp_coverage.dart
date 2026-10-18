//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/edp_geometry.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'edp_coverage.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EdpCoverage {
  /// Returns a new [EdpCoverage] instance.
  EdpCoverage({

     this.area,

     this.family,

     this.pDetect,
  });

  @JsonKey(
    
    name: r'area',
    required: false,
    includeIfNull: false,
  )


  final EdpGeometry? area;



  @JsonKey(
    
    name: r'family',
    required: false,
    includeIfNull: false,
  )


  final String? family;



          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'pDetect',
    required: false,
    includeIfNull: false,
  )


  final num? pDetect;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EdpCoverage &&
      other.area == area &&
      other.family == family &&
      other.pDetect == pDetect;

    @override
    int get hashCode =>
        (area == null ? 0 : area.hashCode) +
        (family == null ? 0 : family.hashCode) +
        (pDetect == null ? 0 : pDetect.hashCode);

  factory EdpCoverage.fromJson(Map<String, dynamic> json) => _$EdpCoverageFromJson(json);

  Map<String, dynamic> toJson() => _$EdpCoverageToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

