//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'coverage.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Coverage {
  /// Returns a new [Coverage] instance.
  Coverage({

     this.receivers,

     this.expected,

     this.actual,

     this.region,
  });

  @JsonKey(
    
    name: r'receivers',
    required: false,
    includeIfNull: false,
  )


  final int? receivers;



  @JsonKey(
    
    name: r'expected',
    required: false,
    includeIfNull: false,
  )


  final int? expected;



  @JsonKey(
    
    name: r'actual',
    required: false,
    includeIfNull: false,
  )


  final int? actual;



      /// Covered region [minLon, minLat, maxLon, maxLat]
  @JsonKey(
    
    name: r'region',
    required: false,
    includeIfNull: false,
  )


  final List<num>? region;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Coverage &&
      other.receivers == receivers &&
      other.expected == expected &&
      other.actual == actual &&
      other.region == region;

    @override
    int get hashCode =>
        (receivers == null ? 0 : receivers.hashCode) +
        (expected == null ? 0 : expected.hashCode) +
        (actual == null ? 0 : actual.hashCode) +
        (region == null ? 0 : region.hashCode);

  factory Coverage.fromJson(Map<String, dynamic> json) => _$CoverageFromJson(json);

  Map<String, dynamic> toJson() => _$CoverageToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

