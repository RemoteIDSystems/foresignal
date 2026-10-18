// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'plugin.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PluginCWProxy {
  Plugin name(String name);

  Plugin version(String version);

  Plugin abi(PluginAbiEnum abi);

  Plugin kind(PluginKindEnum kind);

  Plugin sha256(String sha256);

  Plugin actualSha256(String? actualSha256);

  Plugin hashMatches(bool hashMatches);

  Plugin enabled(bool enabled);

  Plugin state(PluginStateEnum state);

  Plugin error(String? error);

  Plugin sourceIds(List<String>? sourceIds);

  Plugin loadedAt(DateTime? loadedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Plugin(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Plugin(...).copyWith(id: 12, name: "My name")
  /// ```
  Plugin call({
    String name,
    String version,
    PluginAbiEnum abi,
    PluginKindEnum kind,
    String sha256,
    String? actualSha256,
    bool hashMatches,
    bool enabled,
    PluginStateEnum state,
    String? error,
    List<String>? sourceIds,
    DateTime? loadedAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPlugin.copyWith(...)` or call `instanceOfPlugin.copyWith.fieldName(value)` for a single field.
class _$PluginCWProxyImpl implements _$PluginCWProxy {
  const _$PluginCWProxyImpl(this._value);

  final Plugin _value;

  @override
  Plugin name(String name) => call(name: name);

  @override
  Plugin version(String version) => call(version: version);

  @override
  Plugin abi(PluginAbiEnum abi) => call(abi: abi);

  @override
  Plugin kind(PluginKindEnum kind) => call(kind: kind);

  @override
  Plugin sha256(String sha256) => call(sha256: sha256);

  @override
  Plugin actualSha256(String? actualSha256) => call(actualSha256: actualSha256);

  @override
  Plugin hashMatches(bool hashMatches) => call(hashMatches: hashMatches);

  @override
  Plugin enabled(bool enabled) => call(enabled: enabled);

  @override
  Plugin state(PluginStateEnum state) => call(state: state);

  @override
  Plugin error(String? error) => call(error: error);

  @override
  Plugin sourceIds(List<String>? sourceIds) => call(sourceIds: sourceIds);

  @override
  Plugin loadedAt(DateTime? loadedAt) => call(loadedAt: loadedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Plugin(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Plugin(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  Plugin call({
    Object? name = const $CopyWithPlaceholder(),
    Object? version = const $CopyWithPlaceholder(),
    Object? abi = const $CopyWithPlaceholder(),
    Object? kind = const $CopyWithPlaceholder(),
    Object? sha256 = const $CopyWithPlaceholder(),
    Object? actualSha256 = const $CopyWithPlaceholder(),
    Object? hashMatches = const $CopyWithPlaceholder(),
    Object? enabled = const $CopyWithPlaceholder(),
    Object? state = const $CopyWithPlaceholder(),
    Object? error = const $CopyWithPlaceholder(),
    Object? sourceIds = const $CopyWithPlaceholder(),
    Object? loadedAt = const $CopyWithPlaceholder(),
  }) {
    return Plugin(
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      version: version == const $CopyWithPlaceholder() || version == null
          ? _value.version
          // ignore: cast_nullable_to_non_nullable
          : version as String,
      abi: abi == const $CopyWithPlaceholder() || abi == null
          ? _value.abi
          // ignore: cast_nullable_to_non_nullable
          : abi as PluginAbiEnum,
      kind: kind == const $CopyWithPlaceholder() || kind == null
          ? _value.kind
          // ignore: cast_nullable_to_non_nullable
          : kind as PluginKindEnum,
      sha256: sha256 == const $CopyWithPlaceholder() || sha256 == null
          ? _value.sha256
          // ignore: cast_nullable_to_non_nullable
          : sha256 as String,
      actualSha256: actualSha256 == const $CopyWithPlaceholder()
          ? _value.actualSha256
          // ignore: cast_nullable_to_non_nullable
          : actualSha256 as String?,
      hashMatches:
          hashMatches == const $CopyWithPlaceholder() || hashMatches == null
          ? _value.hashMatches
          // ignore: cast_nullable_to_non_nullable
          : hashMatches as bool,
      enabled: enabled == const $CopyWithPlaceholder() || enabled == null
          ? _value.enabled
          // ignore: cast_nullable_to_non_nullable
          : enabled as bool,
      state: state == const $CopyWithPlaceholder() || state == null
          ? _value.state
          // ignore: cast_nullable_to_non_nullable
          : state as PluginStateEnum,
      error: error == const $CopyWithPlaceholder()
          ? _value.error
          // ignore: cast_nullable_to_non_nullable
          : error as String?,
      sourceIds: sourceIds == const $CopyWithPlaceholder()
          ? _value.sourceIds
          // ignore: cast_nullable_to_non_nullable
          : sourceIds as List<String>?,
      loadedAt: loadedAt == const $CopyWithPlaceholder()
          ? _value.loadedAt
          // ignore: cast_nullable_to_non_nullable
          : loadedAt as DateTime?,
    );
  }
}

extension $PluginCopyWith on Plugin {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPlugin.copyWith(...)` or `instanceOfPlugin.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PluginCWProxy get copyWith => _$PluginCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Plugin _$PluginFromJson(Map<String, dynamic> json) =>
    $checkedCreate('Plugin', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'name',
          'version',
          'abi',
          'kind',
          'sha256',
          'hashMatches',
          'enabled',
          'state',
        ],
      );
      final val = Plugin(
        name: $checkedConvert('name', (v) => v as String),
        version: $checkedConvert('version', (v) => v as String),
        abi: $checkedConvert(
          'abi',
          (v) => $enumDecode(_$PluginAbiEnumEnumMap, v),
        ),
        kind: $checkedConvert(
          'kind',
          (v) => $enumDecode(_$PluginKindEnumEnumMap, v),
        ),
        sha256: $checkedConvert('sha256', (v) => v as String),
        actualSha256: $checkedConvert('actualSha256', (v) => v as String?),
        hashMatches: $checkedConvert('hashMatches', (v) => v as bool),
        enabled: $checkedConvert('enabled', (v) => v as bool),
        state: $checkedConvert(
          'state',
          (v) => $enumDecode(_$PluginStateEnumEnumMap, v),
        ),
        error: $checkedConvert('error', (v) => v as String?),
        sourceIds: $checkedConvert(
          'sourceIds',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        loadedAt: $checkedConvert(
          'loadedAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$PluginToJson(Plugin instance) => <String, dynamic>{
  'name': instance.name,
  'version': instance.version,
  'abi': _$PluginAbiEnumEnumMap[instance.abi]!,
  'kind': _$PluginKindEnumEnumMap[instance.kind]!,
  'sha256': instance.sha256,
  'actualSha256': ?instance.actualSha256,
  'hashMatches': instance.hashMatches,
  'enabled': instance.enabled,
  'state': _$PluginStateEnumEnumMap[instance.state]!,
  'error': ?instance.error,
  'sourceIds': ?instance.sourceIds,
  'loadedAt': ?instance.loadedAt?.toIso8601String(),
};

const _$PluginAbiEnumEnumMap = {PluginAbiEnum.number1: 1};

const _$PluginKindEnumEnumMap = {PluginKindEnum.parser: 'parser'};

const _$PluginStateEnumEnumMap = {
  PluginStateEnum.disabled: 'disabled',
  PluginStateEnum.enabled: 'enabled',
  PluginStateEnum.hashMismatch: 'hash-mismatch',
  PluginStateEnum.error: 'error',
};
