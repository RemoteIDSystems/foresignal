//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'media_provider_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class MediaProviderInput {
  /// Returns a new [MediaProviderInput] instance.
  MediaProviderInput({

     this.enabled,

     this.licenceAcknowledged,

     this.permissionRecorded,

     this.apiKey,
  });

  @JsonKey(
    
    name: r'enabled',
    required: false,
    includeIfNull: false,
  )


  final bool? enabled;



  @JsonKey(
    
    name: r'licenceAcknowledged',
    required: false,
    includeIfNull: false,
  )


  final bool? licenceAcknowledged;



  @JsonKey(
    
    name: r'permissionRecorded',
    required: false,
    includeIfNull: false,
  )


  final bool? permissionRecorded;



      /// MBFC API key (write-only)
  @JsonKey(
    
    name: r'apiKey',
    required: false,
    includeIfNull: false,
  )


  final String? apiKey;





    @override
    bool operator ==(Object other) => identical(this, other) || other is MediaProviderInput &&
      other.enabled == enabled &&
      other.licenceAcknowledged == licenceAcknowledged &&
      other.permissionRecorded == permissionRecorded &&
      other.apiKey == apiKey;

    @override
    int get hashCode =>
        enabled.hashCode +
        licenceAcknowledged.hashCode +
        permissionRecorded.hashCode +
        apiKey.hashCode;

  factory MediaProviderInput.fromJson(Map<String, dynamic> json) => _$MediaProviderInputFromJson(json);

  Map<String, dynamic> toJson() => _$MediaProviderInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

