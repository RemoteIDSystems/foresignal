//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'metric_projection_labels_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class MetricProjectionLabelsInner {
  /// Returns a new [MetricProjectionLabelsInner] instance.
  MetricProjectionLabelsInner({

    required  this.name,

    required  this.path,
  });

  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



  @JsonKey(
    
    name: r'path',
    required: true,
    includeIfNull: false,
  )


  final String path;





    @override
    bool operator ==(Object other) => identical(this, other) || other is MetricProjectionLabelsInner &&
      other.name == name &&
      other.path == path;

    @override
    int get hashCode =>
        name.hashCode +
        path.hashCode;

  factory MetricProjectionLabelsInner.fromJson(Map<String, dynamic> json) => _$MetricProjectionLabelsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$MetricProjectionLabelsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

