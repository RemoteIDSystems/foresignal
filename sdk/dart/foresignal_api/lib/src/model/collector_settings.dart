//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'collector_settings.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class CollectorSettings {
  /// Returns a new [CollectorSettings] instance.
  CollectorSettings({

     this.maxConcurrentPulls = 2,
  });

      /// How many polling fetches may pull from upstream at once across every source: the size of the global pull governor. A stored or submitted value outside 1 to 16 is clamped. Streams are not counted.
          // minimum: 1
          // maximum: 16
  @JsonKey(
    defaultValue: 2,
    name: r'maxConcurrentPulls',
    required: true,
    includeIfNull: false,
  )


  final int maxConcurrentPulls;





    @override
    bool operator ==(Object other) => identical(this, other) || other is CollectorSettings &&
      other.maxConcurrentPulls == maxConcurrentPulls;

    @override
    int get hashCode =>
        maxConcurrentPulls.hashCode;

  factory CollectorSettings.fromJson(Map<String, dynamic> json) => _$CollectorSettingsFromJson(json);

  Map<String, dynamic> toJson() => _$CollectorSettingsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

