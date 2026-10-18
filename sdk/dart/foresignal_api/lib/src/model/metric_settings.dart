//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/metric_projection.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'metric_settings.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class MetricSettings {
  /// Returns a new [MetricSettings] instance.
  MetricSettings({

    required  this.projections,
  });

  @JsonKey(
    
    name: r'projections',
    required: true,
    includeIfNull: false,
  )


  final List<MetricProjection> projections;





    @override
    bool operator ==(Object other) => identical(this, other) || other is MetricSettings &&
      other.projections == projections;

    @override
    int get hashCode =>
        projections.hashCode;

  factory MetricSettings.fromJson(Map<String, dynamic> json) => _$MetricSettingsFromJson(json);

  Map<String, dynamic> toJson() => _$MetricSettingsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

