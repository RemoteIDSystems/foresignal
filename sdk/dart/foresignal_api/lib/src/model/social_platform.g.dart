// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'social_platform.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SocialPlatformCWProxy {
  SocialPlatform id(SocialPlatformId id);

  SocialPlatform network(String network);

  SocialPlatform name(String name);

  SocialPlatform state(SocialPlatformStateEnum state);

  SocialPlatform stateReason(String? stateReason);

  SocialPlatform defaultEnabled(bool? defaultEnabled);

  SocialPlatform type(SocialPlatformTypeEnum? type);

  SocialPlatform mode(String? mode);

  SocialPlatform paths(List<SocialCollectionPath> paths);

  SocialPlatform credentials(SocialPlatformCredentials credentials);

  SocialPlatform health(SourceHealth? health);

  SocialPlatform stream(SocialPlatformStream? stream);

  SocialPlatform quotas(List<SocialQuota>? quotas);

  SocialPlatform spend(SocialSpend? spend);

  SocialPlatform deletionSync(SocialPlatformDeletionSync? deletionSync);

  SocialPlatform terms(SocialPlatformTerms? terms);

  SocialPlatform settings(SocialPlatformSettings settings);

  SocialPlatform updatedAt(DateTime? updatedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialPlatform(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialPlatform(...).copyWith(id: 12, name: "My name")
  /// ```
  SocialPlatform call({
    SocialPlatformId id,
    String network,
    String name,
    SocialPlatformStateEnum state,
    String? stateReason,
    bool? defaultEnabled,
    SocialPlatformTypeEnum? type,
    String? mode,
    List<SocialCollectionPath> paths,
    SocialPlatformCredentials credentials,
    SourceHealth? health,
    SocialPlatformStream? stream,
    List<SocialQuota>? quotas,
    SocialSpend? spend,
    SocialPlatformDeletionSync? deletionSync,
    SocialPlatformTerms? terms,
    SocialPlatformSettings settings,
    DateTime? updatedAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSocialPlatform.copyWith(...)` or call `instanceOfSocialPlatform.copyWith.fieldName(value)` for a single field.
class _$SocialPlatformCWProxyImpl implements _$SocialPlatformCWProxy {
  const _$SocialPlatformCWProxyImpl(this._value);

  final SocialPlatform _value;

  @override
  SocialPlatform id(SocialPlatformId id) => call(id: id);

  @override
  SocialPlatform network(String network) => call(network: network);

  @override
  SocialPlatform name(String name) => call(name: name);

  @override
  SocialPlatform state(SocialPlatformStateEnum state) => call(state: state);

  @override
  SocialPlatform stateReason(String? stateReason) =>
      call(stateReason: stateReason);

  @override
  SocialPlatform defaultEnabled(bool? defaultEnabled) =>
      call(defaultEnabled: defaultEnabled);

  @override
  SocialPlatform type(SocialPlatformTypeEnum? type) => call(type: type);

  @override
  SocialPlatform mode(String? mode) => call(mode: mode);

  @override
  SocialPlatform paths(List<SocialCollectionPath> paths) => call(paths: paths);

  @override
  SocialPlatform credentials(SocialPlatformCredentials credentials) =>
      call(credentials: credentials);

  @override
  SocialPlatform health(SourceHealth? health) => call(health: health);

  @override
  SocialPlatform stream(SocialPlatformStream? stream) => call(stream: stream);

  @override
  SocialPlatform quotas(List<SocialQuota>? quotas) => call(quotas: quotas);

  @override
  SocialPlatform spend(SocialSpend? spend) => call(spend: spend);

  @override
  SocialPlatform deletionSync(SocialPlatformDeletionSync? deletionSync) =>
      call(deletionSync: deletionSync);

  @override
  SocialPlatform terms(SocialPlatformTerms? terms) => call(terms: terms);

  @override
  SocialPlatform settings(SocialPlatformSettings settings) =>
      call(settings: settings);

  @override
  SocialPlatform updatedAt(DateTime? updatedAt) => call(updatedAt: updatedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialPlatform(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialPlatform(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SocialPlatform call({
    Object? id = const $CopyWithPlaceholder(),
    Object? network = const $CopyWithPlaceholder(),
    Object? name = const $CopyWithPlaceholder(),
    Object? state = const $CopyWithPlaceholder(),
    Object? stateReason = const $CopyWithPlaceholder(),
    Object? defaultEnabled = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? mode = const $CopyWithPlaceholder(),
    Object? paths = const $CopyWithPlaceholder(),
    Object? credentials = const $CopyWithPlaceholder(),
    Object? health = const $CopyWithPlaceholder(),
    Object? stream = const $CopyWithPlaceholder(),
    Object? quotas = const $CopyWithPlaceholder(),
    Object? spend = const $CopyWithPlaceholder(),
    Object? deletionSync = const $CopyWithPlaceholder(),
    Object? terms = const $CopyWithPlaceholder(),
    Object? settings = const $CopyWithPlaceholder(),
    Object? updatedAt = const $CopyWithPlaceholder(),
  }) {
    return SocialPlatform(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as SocialPlatformId,
      network: network == const $CopyWithPlaceholder() || network == null
          ? _value.network
          // ignore: cast_nullable_to_non_nullable
          : network as String,
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      state: state == const $CopyWithPlaceholder() || state == null
          ? _value.state
          // ignore: cast_nullable_to_non_nullable
          : state as SocialPlatformStateEnum,
      stateReason: stateReason == const $CopyWithPlaceholder()
          ? _value.stateReason
          // ignore: cast_nullable_to_non_nullable
          : stateReason as String?,
      defaultEnabled: defaultEnabled == const $CopyWithPlaceholder()
          ? _value.defaultEnabled
          // ignore: cast_nullable_to_non_nullable
          : defaultEnabled as bool?,
      type: type == const $CopyWithPlaceholder()
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as SocialPlatformTypeEnum?,
      mode: mode == const $CopyWithPlaceholder()
          ? _value.mode
          // ignore: cast_nullable_to_non_nullable
          : mode as String?,
      paths: paths == const $CopyWithPlaceholder() || paths == null
          ? _value.paths
          // ignore: cast_nullable_to_non_nullable
          : paths as List<SocialCollectionPath>,
      credentials:
          credentials == const $CopyWithPlaceholder() || credentials == null
          ? _value.credentials
          // ignore: cast_nullable_to_non_nullable
          : credentials as SocialPlatformCredentials,
      health: health == const $CopyWithPlaceholder()
          ? _value.health
          // ignore: cast_nullable_to_non_nullable
          : health as SourceHealth?,
      stream: stream == const $CopyWithPlaceholder()
          ? _value.stream
          // ignore: cast_nullable_to_non_nullable
          : stream as SocialPlatformStream?,
      quotas: quotas == const $CopyWithPlaceholder()
          ? _value.quotas
          // ignore: cast_nullable_to_non_nullable
          : quotas as List<SocialQuota>?,
      spend: spend == const $CopyWithPlaceholder()
          ? _value.spend
          // ignore: cast_nullable_to_non_nullable
          : spend as SocialSpend?,
      deletionSync: deletionSync == const $CopyWithPlaceholder()
          ? _value.deletionSync
          // ignore: cast_nullable_to_non_nullable
          : deletionSync as SocialPlatformDeletionSync?,
      terms: terms == const $CopyWithPlaceholder()
          ? _value.terms
          // ignore: cast_nullable_to_non_nullable
          : terms as SocialPlatformTerms?,
      settings: settings == const $CopyWithPlaceholder() || settings == null
          ? _value.settings
          // ignore: cast_nullable_to_non_nullable
          : settings as SocialPlatformSettings,
      updatedAt: updatedAt == const $CopyWithPlaceholder()
          ? _value.updatedAt
          // ignore: cast_nullable_to_non_nullable
          : updatedAt as DateTime?,
    );
  }
}

extension $SocialPlatformCopyWith on SocialPlatform {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSocialPlatform.copyWith(...)` or `instanceOfSocialPlatform.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SocialPlatformCWProxy get copyWith => _$SocialPlatformCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SocialPlatform _$SocialPlatformFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SocialPlatform', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'id',
      'network',
      'name',
      'state',
      'paths',
      'credentials',
      'settings',
    ],
  );
  final val = SocialPlatform(
    id: $checkedConvert('id', (v) => $enumDecode(_$SocialPlatformIdEnumMap, v)),
    network: $checkedConvert('network', (v) => v as String),
    name: $checkedConvert('name', (v) => v as String),
    state: $checkedConvert(
      'state',
      (v) => $enumDecode(_$SocialPlatformStateEnumEnumMap, v),
    ),
    stateReason: $checkedConvert('stateReason', (v) => v as String?),
    defaultEnabled: $checkedConvert('defaultEnabled', (v) => v as bool?),
    type: $checkedConvert(
      'type',
      (v) => $enumDecodeNullable(_$SocialPlatformTypeEnumEnumMap, v),
    ),
    mode: $checkedConvert('mode', (v) => v as String?),
    paths: $checkedConvert(
      'paths',
      (v) => (v as List<dynamic>)
          .map((e) => SocialCollectionPath.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    credentials: $checkedConvert(
      'credentials',
      (v) => SocialPlatformCredentials.fromJson(v as Map<String, dynamic>),
    ),
    health: $checkedConvert(
      'health',
      (v) =>
          v == null ? null : SourceHealth.fromJson(v as Map<String, dynamic>),
    ),
    stream: $checkedConvert(
      'stream',
      (v) => v == null
          ? null
          : SocialPlatformStream.fromJson(v as Map<String, dynamic>),
    ),
    quotas: $checkedConvert(
      'quotas',
      (v) => (v as List<dynamic>?)
          ?.map((e) => SocialQuota.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    spend: $checkedConvert(
      'spend',
      (v) => v == null ? null : SocialSpend.fromJson(v as Map<String, dynamic>),
    ),
    deletionSync: $checkedConvert(
      'deletionSync',
      (v) => v == null
          ? null
          : SocialPlatformDeletionSync.fromJson(v as Map<String, dynamic>),
    ),
    terms: $checkedConvert(
      'terms',
      (v) => v == null
          ? null
          : SocialPlatformTerms.fromJson(v as Map<String, dynamic>),
    ),
    settings: $checkedConvert(
      'settings',
      (v) => SocialPlatformSettings.fromJson(v as Map<String, dynamic>),
    ),
    updatedAt: $checkedConvert(
      'updatedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
  );
  return val;
});

Map<String, dynamic> _$SocialPlatformToJson(SocialPlatform instance) =>
    <String, dynamic>{
      'id': _$SocialPlatformIdEnumMap[instance.id]!,
      'network': instance.network,
      'name': instance.name,
      'state': _$SocialPlatformStateEnumEnumMap[instance.state]!,
      'stateReason': ?instance.stateReason,
      'defaultEnabled': ?instance.defaultEnabled,
      'type': ?_$SocialPlatformTypeEnumEnumMap[instance.type],
      'mode': ?instance.mode,
      'paths': instance.paths.map((e) => e.toJson()).toList(),
      'credentials': instance.credentials.toJson(),
      'health': ?instance.health?.toJson(),
      'stream': ?instance.stream?.toJson(),
      'quotas': ?instance.quotas?.map((e) => e.toJson()).toList(),
      'spend': ?instance.spend?.toJson(),
      'deletionSync': ?instance.deletionSync?.toJson(),
      'terms': ?instance.terms?.toJson(),
      'settings': instance.settings.toJson(),
      'updatedAt': ?instance.updatedAt?.toIso8601String(),
    };

const _$SocialPlatformIdEnumMap = {
  SocialPlatformId.blueskyJetstream: 'bluesky-jetstream',
  SocialPlatformId.blueskySearch: 'bluesky-search',
  SocialPlatformId.mastodon: 'mastodon',
  SocialPlatformId.reddit: 'reddit',
  SocialPlatformId.youtube: 'youtube',
  SocialPlatformId.x: 'x',
  SocialPlatformId.threads: 'threads',
  SocialPlatformId.instagramHashtags: 'instagram-hashtags',
};

const _$SocialPlatformStateEnumEnumMap = {
  SocialPlatformStateEnum.enabled: 'enabled',
  SocialPlatformStateEnum.disabled: 'disabled',
  SocialPlatformStateEnum.degraded: 'degraded',
  SocialPlatformStateEnum.budgetExhausted: 'budget-exhausted',
  SocialPlatformStateEnum.credentialsRequired: 'credentials-required',
  SocialPlatformStateEnum.unavailable: 'unavailable',
};

const _$SocialPlatformTypeEnumEnumMap = {
  SocialPlatformTypeEnum.STREAMING: 'STREAMING',
  SocialPlatformTypeEnum.POLLING: 'POLLING',
  SocialPlatformTypeEnum.sTREAMINGPlusPOLLING: 'STREAMING+POLLING',
  SocialPlatformTypeEnum.CUE: 'CUE',
};
