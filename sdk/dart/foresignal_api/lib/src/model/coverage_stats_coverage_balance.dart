//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'coverage_stats_coverage_balance.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class CoverageStatsCoverageBalance {
  /// Returns a new [CoverageStatsCoverageBalance] instance.
  CoverageStatsCoverageBalance({

    required  this.left,

    required  this.center,

    required  this.right,

    required  this.unrated,
  });

          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'left',
    required: true,
    includeIfNull: false,
  )


  final num left;



          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'center',
    required: true,
    includeIfNull: false,
  )


  final num center;



          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'right',
    required: true,
    includeIfNull: false,
  )


  final num right;



          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'unrated',
    required: true,
    includeIfNull: false,
  )


  final num unrated;





    @override
    bool operator ==(Object other) => identical(this, other) || other is CoverageStatsCoverageBalance &&
      other.left == left &&
      other.center == center &&
      other.right == right &&
      other.unrated == unrated;

    @override
    int get hashCode =>
        left.hashCode +
        center.hashCode +
        right.hashCode +
        unrated.hashCode;

  factory CoverageStatsCoverageBalance.fromJson(Map<String, dynamic> json) => _$CoverageStatsCoverageBalanceFromJson(json);

  Map<String, dynamic> toJson() => _$CoverageStatsCoverageBalanceToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

