//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'plugin_settings.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PluginSettings {
  /// Returns a new [PluginSettings] instance.
  PluginSettings({

     this.enabled = false,
  });

  @JsonKey(
    defaultValue: false,
    name: r'enabled',
    required: true,
    includeIfNull: false,
  )


  final bool enabled;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PluginSettings &&
      other.enabled == enabled;

    @override
    int get hashCode =>
        enabled.hashCode;

  factory PluginSettings.fromJson(Map<String, dynamic> json) => _$PluginSettingsFromJson(json);

  Map<String, dynamic> toJson() => _$PluginSettingsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

