//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'chronicle_summary_gaps_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ChronicleSummaryGapsInner {
  /// Returns a new [ChronicleSummaryGapsInner] instance.
  ChronicleSummaryGapsInner({

    required  this.from,

    required  this.to,

     this.reason,
  });

  @JsonKey(
    
    name: r'from',
    required: true,
    includeIfNull: false,
  )


  final DateTime from;



  @JsonKey(
    
    name: r'to',
    required: true,
    includeIfNull: false,
  )


  final DateTime to;



  @JsonKey(
    
    name: r'reason',
    required: false,
    includeIfNull: false,
  )


  final String? reason;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ChronicleSummaryGapsInner &&
      other.from == from &&
      other.to == to &&
      other.reason == reason;

    @override
    int get hashCode =>
        from.hashCode +
        to.hashCode +
        reason.hashCode;

  factory ChronicleSummaryGapsInner.fromJson(Map<String, dynamic> json) => _$ChronicleSummaryGapsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$ChronicleSummaryGapsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

