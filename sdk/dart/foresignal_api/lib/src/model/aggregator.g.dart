// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'aggregator.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AggregatorCWProxy {
  Aggregator domain(String domain);

  Aggregator name(String name);

  Aggregator enabled(bool enabled);

  Aggregator mode(String? mode);

  Aggregator endpoint(String? endpoint);

  Aggregator status(String? status);

  Aggregator supersededSources(List<String>? supersededSources);

  Aggregator lastSeenAt(DateTime? lastSeenAt);

  Aggregator config(Map<String, Object>? config);

  Aggregator disableSupersededSources(bool? disableSupersededSources);

  Aggregator implemented(bool? implemented);

  Aggregator statusReason(String? statusReason);

  Aggregator sourceIds(List<String>? sourceIds);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Aggregator(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Aggregator(...).copyWith(id: 12, name: "My name")
  /// ```
  Aggregator call({
    String domain,
    String name,
    bool enabled,
    String? mode,
    String? endpoint,
    String? status,
    List<String>? supersededSources,
    DateTime? lastSeenAt,
    Map<String, Object>? config,
    bool? disableSupersededSources,
    bool? implemented,
    String? statusReason,
    List<String>? sourceIds,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAggregator.copyWith(...)` or call `instanceOfAggregator.copyWith.fieldName(value)` for a single field.
class _$AggregatorCWProxyImpl implements _$AggregatorCWProxy {
  const _$AggregatorCWProxyImpl(this._value);

  final Aggregator _value;

  @override
  Aggregator domain(String domain) => call(domain: domain);

  @override
  Aggregator name(String name) => call(name: name);

  @override
  Aggregator enabled(bool enabled) => call(enabled: enabled);

  @override
  Aggregator mode(String? mode) => call(mode: mode);

  @override
  Aggregator endpoint(String? endpoint) => call(endpoint: endpoint);

  @override
  Aggregator status(String? status) => call(status: status);

  @override
  Aggregator supersededSources(List<String>? supersededSources) =>
      call(supersededSources: supersededSources);

  @override
  Aggregator lastSeenAt(DateTime? lastSeenAt) => call(lastSeenAt: lastSeenAt);

  @override
  Aggregator config(Map<String, Object>? config) => call(config: config);

  @override
  Aggregator disableSupersededSources(bool? disableSupersededSources) =>
      call(disableSupersededSources: disableSupersededSources);

  @override
  Aggregator implemented(bool? implemented) => call(implemented: implemented);

  @override
  Aggregator statusReason(String? statusReason) =>
      call(statusReason: statusReason);

  @override
  Aggregator sourceIds(List<String>? sourceIds) => call(sourceIds: sourceIds);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Aggregator(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Aggregator(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  Aggregator call({
    Object? domain = const $CopyWithPlaceholder(),
    Object? name = const $CopyWithPlaceholder(),
    Object? enabled = const $CopyWithPlaceholder(),
    Object? mode = const $CopyWithPlaceholder(),
    Object? endpoint = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? supersededSources = const $CopyWithPlaceholder(),
    Object? lastSeenAt = const $CopyWithPlaceholder(),
    Object? config = const $CopyWithPlaceholder(),
    Object? disableSupersededSources = const $CopyWithPlaceholder(),
    Object? implemented = const $CopyWithPlaceholder(),
    Object? statusReason = const $CopyWithPlaceholder(),
    Object? sourceIds = const $CopyWithPlaceholder(),
  }) {
    return Aggregator(
      domain: domain == const $CopyWithPlaceholder() || domain == null
          ? _value.domain
          // ignore: cast_nullable_to_non_nullable
          : domain as String,
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      enabled: enabled == const $CopyWithPlaceholder() || enabled == null
          ? _value.enabled
          // ignore: cast_nullable_to_non_nullable
          : enabled as bool,
      mode: mode == const $CopyWithPlaceholder()
          ? _value.mode
          // ignore: cast_nullable_to_non_nullable
          : mode as String?,
      endpoint: endpoint == const $CopyWithPlaceholder()
          ? _value.endpoint
          // ignore: cast_nullable_to_non_nullable
          : endpoint as String?,
      status: status == const $CopyWithPlaceholder()
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as String?,
      supersededSources: supersededSources == const $CopyWithPlaceholder()
          ? _value.supersededSources
          // ignore: cast_nullable_to_non_nullable
          : supersededSources as List<String>?,
      lastSeenAt: lastSeenAt == const $CopyWithPlaceholder()
          ? _value.lastSeenAt
          // ignore: cast_nullable_to_non_nullable
          : lastSeenAt as DateTime?,
      config: config == const $CopyWithPlaceholder()
          ? _value.config
          // ignore: cast_nullable_to_non_nullable
          : config as Map<String, Object>?,
      disableSupersededSources:
          disableSupersededSources == const $CopyWithPlaceholder()
          ? _value.disableSupersededSources
          // ignore: cast_nullable_to_non_nullable
          : disableSupersededSources as bool?,
      implemented: implemented == const $CopyWithPlaceholder()
          ? _value.implemented
          // ignore: cast_nullable_to_non_nullable
          : implemented as bool?,
      statusReason: statusReason == const $CopyWithPlaceholder()
          ? _value.statusReason
          // ignore: cast_nullable_to_non_nullable
          : statusReason as String?,
      sourceIds: sourceIds == const $CopyWithPlaceholder()
          ? _value.sourceIds
          // ignore: cast_nullable_to_non_nullable
          : sourceIds as List<String>?,
    );
  }
}

extension $AggregatorCopyWith on Aggregator {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAggregator.copyWith(...)` or `instanceOfAggregator.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AggregatorCWProxy get copyWith => _$AggregatorCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Aggregator _$AggregatorFromJson(Map<String, dynamic> json) =>
    $checkedCreate('Aggregator', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['domain', 'name', 'enabled']);
      final val = Aggregator(
        domain: $checkedConvert('domain', (v) => v as String),
        name: $checkedConvert('name', (v) => v as String),
        enabled: $checkedConvert('enabled', (v) => v as bool? ?? false),
        mode: $checkedConvert('mode', (v) => v as String?),
        endpoint: $checkedConvert('endpoint', (v) => v as String?),
        status: $checkedConvert('status', (v) => v as String?),
        supersededSources: $checkedConvert(
          'supersededSources',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        lastSeenAt: $checkedConvert(
          'lastSeenAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        config: $checkedConvert(
          'config',
          (v) => (v as Map<String, dynamic>?)?.map(
            (k, e) => MapEntry(k, e as Object),
          ),
        ),
        disableSupersededSources: $checkedConvert(
          'disableSupersededSources',
          (v) => v as bool?,
        ),
        implemented: $checkedConvert('implemented', (v) => v as bool?),
        statusReason: $checkedConvert('statusReason', (v) => v as String?),
        sourceIds: $checkedConvert(
          'sourceIds',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$AggregatorToJson(Aggregator instance) =>
    <String, dynamic>{
      'domain': instance.domain,
      'name': instance.name,
      'enabled': instance.enabled,
      'mode': ?instance.mode,
      'endpoint': ?instance.endpoint,
      'status': ?instance.status,
      'supersededSources': ?instance.supersededSources,
      'lastSeenAt': ?instance.lastSeenAt?.toIso8601String(),
      'config': ?instance.config,
      'disableSupersededSources': ?instance.disableSupersededSources,
      'implemented': ?instance.implemented,
      'statusReason': ?instance.statusReason,
      'sourceIds': ?instance.sourceIds,
    };
