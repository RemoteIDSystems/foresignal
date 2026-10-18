//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'chronicle_summary_layers_inner_series_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ChronicleSummaryLayersInnerSeriesInner {
  /// Returns a new [ChronicleSummaryLayersInnerSeriesInner] instance.
  ChronicleSummaryLayersInnerSeriesInner({

    required  this.t,

    required  this.count,
  });

  @JsonKey(
    
    name: r't',
    required: true,
    includeIfNull: false,
  )


  final DateTime t;



  @JsonKey(
    
    name: r'count',
    required: true,
    includeIfNull: false,
  )


  final int count;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ChronicleSummaryLayersInnerSeriesInner &&
      other.t == t &&
      other.count == count;

    @override
    int get hashCode =>
        t.hashCode +
        count.hashCode;

  factory ChronicleSummaryLayersInnerSeriesInner.fromJson(Map<String, dynamic> json) => _$ChronicleSummaryLayersInnerSeriesInnerFromJson(json);

  Map<String, dynamic> toJson() => _$ChronicleSummaryLayersInnerSeriesInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

