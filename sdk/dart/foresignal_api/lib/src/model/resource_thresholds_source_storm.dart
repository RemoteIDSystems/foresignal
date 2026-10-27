//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'resource_thresholds_source_storm.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ResourceThresholdsSourceStorm {
  /// Returns a new [ResourceThresholdsSourceStorm] instance.
  ResourceThresholdsSourceStorm({

     this.failedSources,
  });

      /// null turns the storm alert off
          // minimum: 1
  @JsonKey(
    
    name: r'failedSources',
    required: false,
    includeIfNull: false,
  )


  final int? failedSources;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ResourceThresholdsSourceStorm &&
      other.failedSources == failedSources;

    @override
    int get hashCode =>
        (failedSources == null ? 0 : failedSources.hashCode);

  factory ResourceThresholdsSourceStorm.fromJson(Map<String, dynamic> json) => _$ResourceThresholdsSourceStormFromJson(json);

  Map<String, dynamic> toJson() => _$ResourceThresholdsSourceStormToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

