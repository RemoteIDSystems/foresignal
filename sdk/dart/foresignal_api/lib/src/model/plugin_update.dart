//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'plugin_update.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PluginUpdate {
  /// Returns a new [PluginUpdate] instance.
  PluginUpdate({

    required  this.enabled,

     this.sha256,
  });

  @JsonKey(
    
    name: r'enabled',
    required: true,
    includeIfNull: false,
  )


  final bool enabled;



      /// Must equal the plugin file's actual hash to enable it
  @JsonKey(
    
    name: r'sha256',
    required: false,
    includeIfNull: false,
  )


  final String? sha256;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PluginUpdate &&
      other.enabled == enabled &&
      other.sha256 == sha256;

    @override
    int get hashCode =>
        enabled.hashCode +
        sha256.hashCode;

  factory PluginUpdate.fromJson(Map<String, dynamic> json) => _$PluginUpdateFromJson(json);

  Map<String, dynamic> toJson() => _$PluginUpdateToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

