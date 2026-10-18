//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'field_encodings_velocity_json.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class FieldEncodingsVelocityJson {
  /// Returns a new [FieldEncodingsVelocityJson] instance.
  FieldEncodingsVelocityJson({

     this.url,
  });

  @JsonKey(
    
    name: r'url',
    required: false,
    includeIfNull: false,
  )


  final String? url;





    @override
    bool operator ==(Object other) => identical(this, other) || other is FieldEncodingsVelocityJson &&
      other.url == url;

    @override
    int get hashCode =>
        url.hashCode;

  factory FieldEncodingsVelocityJson.fromJson(Map<String, dynamic> json) => _$FieldEncodingsVelocityJsonFromJson(json);

  Map<String, dynamic> toJson() => _$FieldEncodingsVelocityJsonToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

