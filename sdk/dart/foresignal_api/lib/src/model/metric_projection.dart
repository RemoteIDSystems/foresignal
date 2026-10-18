//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/metric_projection_labels_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'metric_projection.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class MetricProjection {
  /// Returns a new [MetricProjection] instance.
  MetricProjection({

    required  this.name,

    required  this.source_,

     this.kind,

    required  this.path,

     this.labels,

     this.unit,

     this.help,

     this.maxSeries = 500,

     this.allowPrivate = false,
  });

  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



  @JsonKey(
    
    name: r'source',
    required: true,
    includeIfNull: false,
  )


  final String source_;



  @JsonKey(
    
    name: r'kind',
    required: false,
    includeIfNull: false,
  )


  final String? kind;



      /// 017 trigger path syntax; must not be a volatile field
  @JsonKey(
    
    name: r'path',
    required: true,
    includeIfNull: false,
  )


  final String path;



      /// Default labels source and external_id; entities with the same label values share one series (the latest write wins)
  @JsonKey(
    
    name: r'labels',
    required: false,
    includeIfNull: false,
  )


  final List<MetricProjectionLabelsInner>? labels;



  @JsonKey(
    
    name: r'unit',
    required: false,
    includeIfNull: false,
  )


  final String? unit;



  @JsonKey(
    
    name: r'help',
    required: false,
    includeIfNull: false,
  )


  final String? help;



          // minimum: 1
          // maximum: 10000
  @JsonKey(
    defaultValue: 500,
    name: r'maxSeries',
    required: false,
    includeIfNull: false,
  )


  final int? maxSeries;



  @JsonKey(
    defaultValue: false,
    name: r'allowPrivate',
    required: false,
    includeIfNull: false,
  )


  final bool? allowPrivate;





    @override
    bool operator ==(Object other) => identical(this, other) || other is MetricProjection &&
      other.name == name &&
      other.source_ == source_ &&
      other.kind == kind &&
      other.path == path &&
      other.labels == labels &&
      other.unit == unit &&
      other.help == help &&
      other.maxSeries == maxSeries &&
      other.allowPrivate == allowPrivate;

    @override
    int get hashCode =>
        name.hashCode +
        source_.hashCode +
        (kind == null ? 0 : kind.hashCode) +
        path.hashCode +
        labels.hashCode +
        (unit == null ? 0 : unit.hashCode) +
        (help == null ? 0 : help.hashCode) +
        maxSeries.hashCode +
        allowPrivate.hashCode;

  factory MetricProjection.fromJson(Map<String, dynamic> json) => _$MetricProjectionFromJson(json);

  Map<String, dynamic> toJson() => _$MetricProjectionToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

