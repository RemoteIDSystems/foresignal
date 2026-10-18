//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'cors_settings.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class CorsSettings {
  /// Returns a new [CorsSettings] instance.
  CorsSettings({

    required  this.readOrigins,

    required  this.writeOrigins,
  });

  @JsonKey(
    
    name: r'readOrigins',
    required: true,
    includeIfNull: false,
  )


  final List<String> readOrigins;



  @JsonKey(
    
    name: r'writeOrigins',
    required: true,
    includeIfNull: false,
  )


  final List<String> writeOrigins;





    @override
    bool operator ==(Object other) => identical(this, other) || other is CorsSettings &&
      other.readOrigins == readOrigins &&
      other.writeOrigins == writeOrigins;

    @override
    int get hashCode =>
        readOrigins.hashCode +
        writeOrigins.hashCode;

  factory CorsSettings.fromJson(Map<String, dynamic> json) => _$CorsSettingsFromJson(json);

  Map<String, dynamic> toJson() => _$CorsSettingsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

