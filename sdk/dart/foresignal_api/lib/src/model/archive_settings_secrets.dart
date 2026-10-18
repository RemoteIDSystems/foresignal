//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'archive_settings_secrets.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ArchiveSettingsSecrets {
  /// Returns a new [ArchiveSettingsSecrets] instance.
  ArchiveSettingsSecrets({

     this.accessKey,

     this.secretKey,
  });

  @JsonKey(
    
    name: r'accessKey',
    required: false,
    includeIfNull: false,
  )


  final String? accessKey;



  @JsonKey(
    
    name: r'secretKey',
    required: false,
    includeIfNull: false,
  )


  final String? secretKey;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ArchiveSettingsSecrets &&
      other.accessKey == accessKey &&
      other.secretKey == secretKey;

    @override
    int get hashCode =>
        accessKey.hashCode +
        secretKey.hashCode;

  factory ArchiveSettingsSecrets.fromJson(Map<String, dynamic> json) => _$ArchiveSettingsSecretsFromJson(json);

  Map<String, dynamic> toJson() => _$ArchiveSettingsSecretsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

