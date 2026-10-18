//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/chronicle_summary_layers_inner_series_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'chronicle_summary_layers_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ChronicleSummaryLayersInner {
  /// Returns a new [ChronicleSummaryLayersInner] instance.
  ChronicleSummaryLayersInner({

    required  this.layerId,

    required  this.name,

    required  this.rows,

     this.series,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'layerId',
    required: true,
    includeIfNull: false,
  )


  final String layerId;



  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



  @JsonKey(
    
    name: r'rows',
    required: true,
    includeIfNull: false,
  )


  final int rows;



  @JsonKey(
    
    name: r'series',
    required: false,
    includeIfNull: false,
  )


  final List<ChronicleSummaryLayersInnerSeriesInner>? series;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ChronicleSummaryLayersInner &&
      other.layerId == layerId &&
      other.name == name &&
      other.rows == rows &&
      other.series == series;

    @override
    int get hashCode =>
        layerId.hashCode +
        name.hashCode +
        rows.hashCode +
        series.hashCode;

  factory ChronicleSummaryLayersInner.fromJson(Map<String, dynamic> json) => _$ChronicleSummaryLayersInnerFromJson(json);

  Map<String, dynamic> toJson() => _$ChronicleSummaryLayersInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

