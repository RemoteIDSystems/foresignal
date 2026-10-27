// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'egress_health.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EgressHealthCWProxy {
  EgressHealth reachable(bool reachable);

  EgressHealth exitIp(String? exitIp);

  EgressHealth latencyMs(num? latencyMs);

  EgressHealth lastCheckedAt(DateTime? lastCheckedAt);

  EgressHealth consecutiveFailures(int? consecutiveFailures);

  EgressHealth backoffUntil(DateTime? backoffUntil);

  EgressHealth lastError(String? lastError);

  EgressHealth sources(List<EgressHealthSourcesInner>? sources);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EgressHealth(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EgressHealth(...).copyWith(id: 12, name: "My name")
  /// ```
  EgressHealth call({
    bool reachable,
    String? exitIp,
    num? latencyMs,
    DateTime? lastCheckedAt,
    int? consecutiveFailures,
    DateTime? backoffUntil,
    String? lastError,
    List<EgressHealthSourcesInner>? sources,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEgressHealth.copyWith(...)` or call `instanceOfEgressHealth.copyWith.fieldName(value)` for a single field.
class _$EgressHealthCWProxyImpl implements _$EgressHealthCWProxy {
  const _$EgressHealthCWProxyImpl(this._value);

  final EgressHealth _value;

  @override
  EgressHealth reachable(bool reachable) => call(reachable: reachable);

  @override
  EgressHealth exitIp(String? exitIp) => call(exitIp: exitIp);

  @override
  EgressHealth latencyMs(num? latencyMs) => call(latencyMs: latencyMs);

  @override
  EgressHealth lastCheckedAt(DateTime? lastCheckedAt) =>
      call(lastCheckedAt: lastCheckedAt);

  @override
  EgressHealth consecutiveFailures(int? consecutiveFailures) =>
      call(consecutiveFailures: consecutiveFailures);

  @override
  EgressHealth backoffUntil(DateTime? backoffUntil) =>
      call(backoffUntil: backoffUntil);

  @override
  EgressHealth lastError(String? lastError) => call(lastError: lastError);

  @override
  EgressHealth sources(List<EgressHealthSourcesInner>? sources) =>
      call(sources: sources);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EgressHealth(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EgressHealth(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EgressHealth call({
    Object? reachable = const $CopyWithPlaceholder(),
    Object? exitIp = const $CopyWithPlaceholder(),
    Object? latencyMs = const $CopyWithPlaceholder(),
    Object? lastCheckedAt = const $CopyWithPlaceholder(),
    Object? consecutiveFailures = const $CopyWithPlaceholder(),
    Object? backoffUntil = const $CopyWithPlaceholder(),
    Object? lastError = const $CopyWithPlaceholder(),
    Object? sources = const $CopyWithPlaceholder(),
  }) {
    return EgressHealth(
      reachable: reachable == const $CopyWithPlaceholder() || reachable == null
          ? _value.reachable
          // ignore: cast_nullable_to_non_nullable
          : reachable as bool,
      exitIp: exitIp == const $CopyWithPlaceholder()
          ? _value.exitIp
          // ignore: cast_nullable_to_non_nullable
          : exitIp as String?,
      latencyMs: latencyMs == const $CopyWithPlaceholder()
          ? _value.latencyMs
          // ignore: cast_nullable_to_non_nullable
          : latencyMs as num?,
      lastCheckedAt: lastCheckedAt == const $CopyWithPlaceholder()
          ? _value.lastCheckedAt
          // ignore: cast_nullable_to_non_nullable
          : lastCheckedAt as DateTime?,
      consecutiveFailures: consecutiveFailures == const $CopyWithPlaceholder()
          ? _value.consecutiveFailures
          // ignore: cast_nullable_to_non_nullable
          : consecutiveFailures as int?,
      backoffUntil: backoffUntil == const $CopyWithPlaceholder()
          ? _value.backoffUntil
          // ignore: cast_nullable_to_non_nullable
          : backoffUntil as DateTime?,
      lastError: lastError == const $CopyWithPlaceholder()
          ? _value.lastError
          // ignore: cast_nullable_to_non_nullable
          : lastError as String?,
      sources: sources == const $CopyWithPlaceholder()
          ? _value.sources
          // ignore: cast_nullable_to_non_nullable
          : sources as List<EgressHealthSourcesInner>?,
    );
  }
}

extension $EgressHealthCopyWith on EgressHealth {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEgressHealth.copyWith(...)` or `instanceOfEgressHealth.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EgressHealthCWProxy get copyWith => _$EgressHealthCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EgressHealth _$EgressHealthFromJson(Map<String, dynamic> json) =>
    $checkedCreate('EgressHealth', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['reachable']);
      final val = EgressHealth(
        reachable: $checkedConvert('reachable', (v) => v as bool),
        exitIp: $checkedConvert('exitIp', (v) => v as String?),
        latencyMs: $checkedConvert('latencyMs', (v) => v as num?),
        lastCheckedAt: $checkedConvert(
          'lastCheckedAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        consecutiveFailures: $checkedConvert(
          'consecutiveFailures',
          (v) => (v as num?)?.toInt() ?? 0,
        ),
        backoffUntil: $checkedConvert(
          'backoffUntil',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        lastError: $checkedConvert('lastError', (v) => v as String?),
        sources: $checkedConvert(
          'sources',
          (v) => (v as List<dynamic>?)
              ?.map(
                (e) => EgressHealthSourcesInner.fromJson(
                  e as Map<String, dynamic>,
                ),
              )
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$EgressHealthToJson(EgressHealth instance) =>
    <String, dynamic>{
      'reachable': instance.reachable,
      'exitIp': ?instance.exitIp,
      'latencyMs': ?instance.latencyMs,
      'lastCheckedAt': ?instance.lastCheckedAt?.toIso8601String(),
      'consecutiveFailures': ?instance.consecutiveFailures,
      'backoffUntil': ?instance.backoffUntil?.toIso8601String(),
      'lastError': ?instance.lastError,
      'sources': ?instance.sources?.map((e) => e.toJson()).toList(),
    };
