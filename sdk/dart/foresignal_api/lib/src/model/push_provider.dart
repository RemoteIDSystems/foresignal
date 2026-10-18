//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/push_provider_input_config.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'push_provider.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PushProvider {
  /// Returns a new [PushProvider] instance.
  PushProvider({

    required  this.id,

    required  this.type,

    required  this.appIds,

    required  this.config,

     this.secretFields,

    required  this.enabled,

     this.lastTestAt,

     this.lastError,

     this.updatedAt,
  });

  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final PushProviderTypeEnum type;



  @JsonKey(
    
    name: r'appIds',
    required: true,
    includeIfNull: false,
  )


  final List<String> appIds;



  @JsonKey(
    
    name: r'config',
    required: true,
    includeIfNull: false,
  )


  final PushProviderInputConfig config;



      /// Names of secrets that are set
  @JsonKey(
    
    name: r'secretFields',
    required: false,
    includeIfNull: false,
  )


  final List<String>? secretFields;



  @JsonKey(
    
    name: r'enabled',
    required: true,
    includeIfNull: false,
  )


  final bool enabled;



  @JsonKey(
    
    name: r'lastTestAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? lastTestAt;



  @JsonKey(
    
    name: r'lastError',
    required: false,
    includeIfNull: false,
  )


  final String? lastError;



  @JsonKey(
    
    name: r'updatedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? updatedAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PushProvider &&
      other.id == id &&
      other.type == type &&
      other.appIds == appIds &&
      other.config == config &&
      other.secretFields == secretFields &&
      other.enabled == enabled &&
      other.lastTestAt == lastTestAt &&
      other.lastError == lastError &&
      other.updatedAt == updatedAt;

    @override
    int get hashCode =>
        id.hashCode +
        type.hashCode +
        appIds.hashCode +
        config.hashCode +
        secretFields.hashCode +
        enabled.hashCode +
        (lastTestAt == null ? 0 : lastTestAt.hashCode) +
        (lastError == null ? 0 : lastError.hashCode) +
        updatedAt.hashCode;

  factory PushProvider.fromJson(Map<String, dynamic> json) => _$PushProviderFromJson(json);

  Map<String, dynamic> toJson() => _$PushProviderToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum PushProviderTypeEnum {
@JsonValue(r'apns')
apns(r'apns'),
@JsonValue(r'fcm')
fcm(r'fcm');

const PushProviderTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


