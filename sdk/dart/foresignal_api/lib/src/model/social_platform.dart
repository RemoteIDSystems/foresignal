//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/social_collection_path.dart';
import 'package:foresignal_api/src/model/social_platform_settings.dart';
import 'package:foresignal_api/src/model/social_platform_credentials.dart';
import 'package:foresignal_api/src/model/social_platform_id.dart';
import 'package:foresignal_api/src/model/social_quota.dart';
import 'package:foresignal_api/src/model/social_spend.dart';
import 'package:foresignal_api/src/model/social_platform_terms.dart';
import 'package:foresignal_api/src/model/social_platform_deletion_sync.dart';
import 'package:foresignal_api/src/model/social_platform_stream.dart';
import 'package:foresignal_api/src/model/source_health.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'social_platform.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SocialPlatform {
  /// Returns a new [SocialPlatform] instance.
  SocialPlatform({

    required  this.id,

    required  this.network,

    required  this.name,

    required  this.state,

     this.stateReason,

     this.defaultEnabled,

     this.type,

     this.mode,

    required  this.paths,

    required  this.credentials,

     this.health,

     this.stream,

     this.quotas,

     this.spend,

     this.deletionSync,

     this.terms,

    required  this.settings,

     this.updatedAt,
  });

  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final SocialPlatformId id;



      /// Social network: `bluesky`, `mastodon`, `reddit`, `youtube`, `x`, `threads`, `instagram`, or the platform an EDP provider (024, 138) declares
  @JsonKey(
    
    name: r'network',
    required: true,
    includeIfNull: false,
  )


  final String network;



  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



      /// Adapter state; changes emit `social.platform.state`
  @JsonKey(
    
    name: r'state',
    required: true,
    includeIfNull: false,
  )


  final SocialPlatformStateEnum state;



  @JsonKey(
    
    name: r'stateReason',
    required: false,
    includeIfNull: false,
  )


  final String? stateReason;



  @JsonKey(
    
    name: r'defaultEnabled',
    required: false,
    includeIfNull: false,
  )


  final bool? defaultEnabled;



      /// Source type
  @JsonKey(
    
    name: r'type',
    required: false,
    includeIfNull: false,
  )


  final SocialPlatformTypeEnum? type;



  @JsonKey(
    
    name: r'mode',
    required: false,
    includeIfNull: false,
  )


  final String? mode;



  @JsonKey(
    
    name: r'paths',
    required: true,
    includeIfNull: false,
  )


  final List<SocialCollectionPath> paths;



  @JsonKey(
    
    name: r'credentials',
    required: true,
    includeIfNull: false,
  )


  final SocialPlatformCredentials credentials;



  @JsonKey(
    
    name: r'health',
    required: false,
    includeIfNull: false,
  )


  final SourceHealth? health;



  @JsonKey(
    
    name: r'stream',
    required: false,
    includeIfNull: false,
  )


  final SocialPlatformStream? stream;



  @JsonKey(
    
    name: r'quotas',
    required: false,
    includeIfNull: false,
  )


  final List<SocialQuota>? quotas;



  @JsonKey(
    
    name: r'spend',
    required: false,
    includeIfNull: false,
  )


  final SocialSpend? spend;



  @JsonKey(
    
    name: r'deletionSync',
    required: false,
    includeIfNull: false,
  )


  final SocialPlatformDeletionSync? deletionSync;



  @JsonKey(
    
    name: r'terms',
    required: false,
    includeIfNull: false,
  )


  final SocialPlatformTerms? terms;



  @JsonKey(
    
    name: r'settings',
    required: true,
    includeIfNull: false,
  )


  final SocialPlatformSettings settings;



  @JsonKey(
    
    name: r'updatedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? updatedAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SocialPlatform &&
      other.id == id &&
      other.network == network &&
      other.name == name &&
      other.state == state &&
      other.stateReason == stateReason &&
      other.defaultEnabled == defaultEnabled &&
      other.type == type &&
      other.mode == mode &&
      other.paths == paths &&
      other.credentials == credentials &&
      other.health == health &&
      other.stream == stream &&
      other.quotas == quotas &&
      other.spend == spend &&
      other.deletionSync == deletionSync &&
      other.terms == terms &&
      other.settings == settings &&
      other.updatedAt == updatedAt;

    @override
    int get hashCode =>
        id.hashCode +
        network.hashCode +
        name.hashCode +
        state.hashCode +
        (stateReason == null ? 0 : stateReason.hashCode) +
        defaultEnabled.hashCode +
        type.hashCode +
        (mode == null ? 0 : mode.hashCode) +
        paths.hashCode +
        credentials.hashCode +
        (health == null ? 0 : health.hashCode) +
        (stream == null ? 0 : stream.hashCode) +
        quotas.hashCode +
        (spend == null ? 0 : spend.hashCode) +
        deletionSync.hashCode +
        terms.hashCode +
        settings.hashCode +
        updatedAt.hashCode;

  factory SocialPlatform.fromJson(Map<String, dynamic> json) => _$SocialPlatformFromJson(json);

  Map<String, dynamic> toJson() => _$SocialPlatformToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// Adapter state; changes emit `social.platform.state`
enum SocialPlatformStateEnum {
@JsonValue(r'enabled')
enabled(r'enabled'),
@JsonValue(r'disabled')
disabled(r'disabled'),
@JsonValue(r'degraded')
degraded(r'degraded'),
@JsonValue(r'budget-exhausted')
budgetExhausted(r'budget-exhausted'),
@JsonValue(r'credentials-required')
credentialsRequired(r'credentials-required'),
@JsonValue(r'unavailable')
unavailable(r'unavailable');

const SocialPlatformStateEnum(this.value);

final String value;

@override
String toString() => value;
}


/// Source type
enum SocialPlatformTypeEnum {
@JsonValue(r'STREAMING')
STREAMING(r'STREAMING'),
@JsonValue(r'POLLING')
POLLING(r'POLLING'),
@JsonValue(r'STREAMING+POLLING')
sTREAMINGPlusPOLLING(r'STREAMING+POLLING'),
@JsonValue(r'CUE')
CUE(r'CUE');

const SocialPlatformTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


