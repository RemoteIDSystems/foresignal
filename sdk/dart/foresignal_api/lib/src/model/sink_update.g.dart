// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'sink_update.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SinkUpdateCWProxy {
  SinkUpdate name(String name);

  SinkUpdate enabled(bool? enabled);

  SinkUpdate config(Map<String, Object> config);

  SinkUpdate secrets(Map<String, String?>? secrets);

  SinkUpdate filter(SinkFilter? filter);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SinkUpdate(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SinkUpdate(...).copyWith(id: 12, name: "My name")
  /// ```
  SinkUpdate call({
    String name,
    bool? enabled,
    Map<String, Object> config,
    Map<String, String?>? secrets,
    SinkFilter? filter,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSinkUpdate.copyWith(...)` or call `instanceOfSinkUpdate.copyWith.fieldName(value)` for a single field.
class _$SinkUpdateCWProxyImpl implements _$SinkUpdateCWProxy {
  const _$SinkUpdateCWProxyImpl(this._value);

  final SinkUpdate _value;

  @override
  SinkUpdate name(String name) => call(name: name);

  @override
  SinkUpdate enabled(bool? enabled) => call(enabled: enabled);

  @override
  SinkUpdate config(Map<String, Object> config) => call(config: config);

  @override
  SinkUpdate secrets(Map<String, String?>? secrets) => call(secrets: secrets);

  @override
  SinkUpdate filter(SinkFilter? filter) => call(filter: filter);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SinkUpdate(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SinkUpdate(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SinkUpdate call({
    Object? name = const $CopyWithPlaceholder(),
    Object? enabled = const $CopyWithPlaceholder(),
    Object? config = const $CopyWithPlaceholder(),
    Object? secrets = const $CopyWithPlaceholder(),
    Object? filter = const $CopyWithPlaceholder(),
  }) {
    return SinkUpdate(
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      enabled: enabled == const $CopyWithPlaceholder()
          ? _value.enabled
          // ignore: cast_nullable_to_non_nullable
          : enabled as bool?,
      config: config == const $CopyWithPlaceholder() || config == null
          ? _value.config
          // ignore: cast_nullable_to_non_nullable
          : config as Map<String, Object>,
      secrets: secrets == const $CopyWithPlaceholder()
          ? _value.secrets
          // ignore: cast_nullable_to_non_nullable
          : secrets as Map<String, String?>?,
      filter: filter == const $CopyWithPlaceholder()
          ? _value.filter
          // ignore: cast_nullable_to_non_nullable
          : filter as SinkFilter?,
    );
  }
}

extension $SinkUpdateCopyWith on SinkUpdate {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSinkUpdate.copyWith(...)` or `instanceOfSinkUpdate.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SinkUpdateCWProxy get copyWith => _$SinkUpdateCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SinkUpdate _$SinkUpdateFromJson(Map<String, dynamic> json) => $checkedCreate(
  'SinkUpdate',
  json,
  ($checkedConvert) {
    $checkKeys(json, requiredKeys: const ['name', 'config']);
    final val = SinkUpdate(
      name: $checkedConvert('name', (v) => v as String),
      enabled: $checkedConvert('enabled', (v) => v as bool?),
      config: $checkedConvert(
        'config',
        (v) =>
            (v as Map<String, dynamic>).map((k, e) => MapEntry(k, e as Object)),
      ),
      secrets: $checkedConvert(
        'secrets',
        (v) => (v as Map<String, dynamic>?)?.map(
          (k, e) => MapEntry(k, e as String?),
        ),
      ),
      filter: $checkedConvert(
        'filter',
        (v) =>
            v == null ? null : SinkFilter.fromJson(v as Map<String, dynamic>),
      ),
    );
    return val;
  },
);

Map<String, dynamic> _$SinkUpdateToJson(SinkUpdate instance) =>
    <String, dynamic>{
      'name': instance.name,
      'enabled': ?instance.enabled,
      'config': instance.config,
      'secrets': ?instance.secrets,
      'filter': ?instance.filter?.toJson(),
    };
