// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'push_provider.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PushProviderCWProxy {
  PushProvider id(String id);

  PushProvider type(PushProviderTypeEnum type);

  PushProvider appIds(List<String> appIds);

  PushProvider config(PushProviderInputConfig config);

  PushProvider secretFields(List<String>? secretFields);

  PushProvider enabled(bool enabled);

  PushProvider lastTestAt(DateTime? lastTestAt);

  PushProvider lastError(String? lastError);

  PushProvider updatedAt(DateTime? updatedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PushProvider(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PushProvider(...).copyWith(id: 12, name: "My name")
  /// ```
  PushProvider call({
    String id,
    PushProviderTypeEnum type,
    List<String> appIds,
    PushProviderInputConfig config,
    List<String>? secretFields,
    bool enabled,
    DateTime? lastTestAt,
    String? lastError,
    DateTime? updatedAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPushProvider.copyWith(...)` or call `instanceOfPushProvider.copyWith.fieldName(value)` for a single field.
class _$PushProviderCWProxyImpl implements _$PushProviderCWProxy {
  const _$PushProviderCWProxyImpl(this._value);

  final PushProvider _value;

  @override
  PushProvider id(String id) => call(id: id);

  @override
  PushProvider type(PushProviderTypeEnum type) => call(type: type);

  @override
  PushProvider appIds(List<String> appIds) => call(appIds: appIds);

  @override
  PushProvider config(PushProviderInputConfig config) => call(config: config);

  @override
  PushProvider secretFields(List<String>? secretFields) =>
      call(secretFields: secretFields);

  @override
  PushProvider enabled(bool enabled) => call(enabled: enabled);

  @override
  PushProvider lastTestAt(DateTime? lastTestAt) => call(lastTestAt: lastTestAt);

  @override
  PushProvider lastError(String? lastError) => call(lastError: lastError);

  @override
  PushProvider updatedAt(DateTime? updatedAt) => call(updatedAt: updatedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PushProvider(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PushProvider(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PushProvider call({
    Object? id = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? appIds = const $CopyWithPlaceholder(),
    Object? config = const $CopyWithPlaceholder(),
    Object? secretFields = const $CopyWithPlaceholder(),
    Object? enabled = const $CopyWithPlaceholder(),
    Object? lastTestAt = const $CopyWithPlaceholder(),
    Object? lastError = const $CopyWithPlaceholder(),
    Object? updatedAt = const $CopyWithPlaceholder(),
  }) {
    return PushProvider(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as PushProviderTypeEnum,
      appIds: appIds == const $CopyWithPlaceholder() || appIds == null
          ? _value.appIds
          // ignore: cast_nullable_to_non_nullable
          : appIds as List<String>,
      config: config == const $CopyWithPlaceholder() || config == null
          ? _value.config
          // ignore: cast_nullable_to_non_nullable
          : config as PushProviderInputConfig,
      secretFields: secretFields == const $CopyWithPlaceholder()
          ? _value.secretFields
          // ignore: cast_nullable_to_non_nullable
          : secretFields as List<String>?,
      enabled: enabled == const $CopyWithPlaceholder() || enabled == null
          ? _value.enabled
          // ignore: cast_nullable_to_non_nullable
          : enabled as bool,
      lastTestAt: lastTestAt == const $CopyWithPlaceholder()
          ? _value.lastTestAt
          // ignore: cast_nullable_to_non_nullable
          : lastTestAt as DateTime?,
      lastError: lastError == const $CopyWithPlaceholder()
          ? _value.lastError
          // ignore: cast_nullable_to_non_nullable
          : lastError as String?,
      updatedAt: updatedAt == const $CopyWithPlaceholder()
          ? _value.updatedAt
          // ignore: cast_nullable_to_non_nullable
          : updatedAt as DateTime?,
    );
  }
}

extension $PushProviderCopyWith on PushProvider {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPushProvider.copyWith(...)` or `instanceOfPushProvider.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PushProviderCWProxy get copyWith => _$PushProviderCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PushProvider _$PushProviderFromJson(Map<String, dynamic> json) =>
    $checkedCreate('PushProvider', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['id', 'type', 'appIds', 'config', 'enabled'],
      );
      final val = PushProvider(
        id: $checkedConvert('id', (v) => v as String),
        type: $checkedConvert(
          'type',
          (v) => $enumDecode(_$PushProviderTypeEnumEnumMap, v),
        ),
        appIds: $checkedConvert(
          'appIds',
          (v) => (v as List<dynamic>).map((e) => e as String).toList(),
        ),
        config: $checkedConvert(
          'config',
          (v) => PushProviderInputConfig.fromJson(v as Map<String, dynamic>),
        ),
        secretFields: $checkedConvert(
          'secretFields',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        enabled: $checkedConvert('enabled', (v) => v as bool),
        lastTestAt: $checkedConvert(
          'lastTestAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        lastError: $checkedConvert('lastError', (v) => v as String?),
        updatedAt: $checkedConvert(
          'updatedAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$PushProviderToJson(PushProvider instance) =>
    <String, dynamic>{
      'id': instance.id,
      'type': _$PushProviderTypeEnumEnumMap[instance.type]!,
      'appIds': instance.appIds,
      'config': instance.config.toJson(),
      'secretFields': ?instance.secretFields,
      'enabled': instance.enabled,
      'lastTestAt': ?instance.lastTestAt?.toIso8601String(),
      'lastError': ?instance.lastError,
      'updatedAt': ?instance.updatedAt?.toIso8601String(),
    };

const _$PushProviderTypeEnumEnumMap = {
  PushProviderTypeEnum.apns: 'apns',
  PushProviderTypeEnum.fcm: 'fcm',
};
