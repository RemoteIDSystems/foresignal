//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'scorecard_entry_recalibration.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ScorecardEntryRecalibration {
  /// Returns a new [ScorecardEntryRecalibration] instance.
  ScorecardEntryRecalibration({

    required  this.a,

    required  this.b,

    required  this.n,

     this.fittedAt,
  });

  @JsonKey(
    
    name: r'a',
    required: true,
    includeIfNull: false,
  )


  final num a;



  @JsonKey(
    
    name: r'b',
    required: true,
    includeIfNull: false,
  )


  final num b;



  @JsonKey(
    
    name: r'n',
    required: true,
    includeIfNull: false,
  )


  final int n;



  @JsonKey(
    
    name: r'fittedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? fittedAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ScorecardEntryRecalibration &&
      other.a == a &&
      other.b == b &&
      other.n == n &&
      other.fittedAt == fittedAt;

    @override
    int get hashCode =>
        a.hashCode +
        b.hashCode +
        n.hashCode +
        fittedAt.hashCode;

  factory ScorecardEntryRecalibration.fromJson(Map<String, dynamic> json) => _$ScorecardEntryRecalibrationFromJson(json);

  Map<String, dynamic> toJson() => _$ScorecardEntryRecalibrationToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

