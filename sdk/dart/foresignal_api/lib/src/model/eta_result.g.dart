// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'eta_result.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EtaResultCWProxy {
  EtaResult record(KinematicState record);

  EtaResult target(EtaResultTarget target);

  EtaResult distanceMeters(num distanceMeters);

  EtaResult bearingDeg(num? bearingDeg);

  EtaResult bearingDiffDeg(num? bearingDiffDeg);

  EtaResult approaching(bool approaching);

  EtaResult etaSeconds(num? etaSeconds);

  EtaResult etaAt(DateTime? etaAt);

  EtaResult asOf(DateTime asOf);

  EtaResult stale(bool stale);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EtaResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EtaResult(...).copyWith(id: 12, name: "My name")
  /// ```
  EtaResult call({
    KinematicState record,
    EtaResultTarget target,
    num distanceMeters,
    num? bearingDeg,
    num? bearingDiffDeg,
    bool approaching,
    num? etaSeconds,
    DateTime? etaAt,
    DateTime asOf,
    bool stale,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEtaResult.copyWith(...)` or call `instanceOfEtaResult.copyWith.fieldName(value)` for a single field.
class _$EtaResultCWProxyImpl implements _$EtaResultCWProxy {
  const _$EtaResultCWProxyImpl(this._value);

  final EtaResult _value;

  @override
  EtaResult record(KinematicState record) => call(record: record);

  @override
  EtaResult target(EtaResultTarget target) => call(target: target);

  @override
  EtaResult distanceMeters(num distanceMeters) =>
      call(distanceMeters: distanceMeters);

  @override
  EtaResult bearingDeg(num? bearingDeg) => call(bearingDeg: bearingDeg);

  @override
  EtaResult bearingDiffDeg(num? bearingDiffDeg) =>
      call(bearingDiffDeg: bearingDiffDeg);

  @override
  EtaResult approaching(bool approaching) => call(approaching: approaching);

  @override
  EtaResult etaSeconds(num? etaSeconds) => call(etaSeconds: etaSeconds);

  @override
  EtaResult etaAt(DateTime? etaAt) => call(etaAt: etaAt);

  @override
  EtaResult asOf(DateTime asOf) => call(asOf: asOf);

  @override
  EtaResult stale(bool stale) => call(stale: stale);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EtaResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EtaResult(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EtaResult call({
    Object? record = const $CopyWithPlaceholder(),
    Object? target = const $CopyWithPlaceholder(),
    Object? distanceMeters = const $CopyWithPlaceholder(),
    Object? bearingDeg = const $CopyWithPlaceholder(),
    Object? bearingDiffDeg = const $CopyWithPlaceholder(),
    Object? approaching = const $CopyWithPlaceholder(),
    Object? etaSeconds = const $CopyWithPlaceholder(),
    Object? etaAt = const $CopyWithPlaceholder(),
    Object? asOf = const $CopyWithPlaceholder(),
    Object? stale = const $CopyWithPlaceholder(),
  }) {
    return EtaResult(
      record: record == const $CopyWithPlaceholder() || record == null
          ? _value.record
          // ignore: cast_nullable_to_non_nullable
          : record as KinematicState,
      target: target == const $CopyWithPlaceholder() || target == null
          ? _value.target
          // ignore: cast_nullable_to_non_nullable
          : target as EtaResultTarget,
      distanceMeters:
          distanceMeters == const $CopyWithPlaceholder() ||
              distanceMeters == null
          ? _value.distanceMeters
          // ignore: cast_nullable_to_non_nullable
          : distanceMeters as num,
      bearingDeg: bearingDeg == const $CopyWithPlaceholder()
          ? _value.bearingDeg
          // ignore: cast_nullable_to_non_nullable
          : bearingDeg as num?,
      bearingDiffDeg: bearingDiffDeg == const $CopyWithPlaceholder()
          ? _value.bearingDiffDeg
          // ignore: cast_nullable_to_non_nullable
          : bearingDiffDeg as num?,
      approaching:
          approaching == const $CopyWithPlaceholder() || approaching == null
          ? _value.approaching
          // ignore: cast_nullable_to_non_nullable
          : approaching as bool,
      etaSeconds: etaSeconds == const $CopyWithPlaceholder()
          ? _value.etaSeconds
          // ignore: cast_nullable_to_non_nullable
          : etaSeconds as num?,
      etaAt: etaAt == const $CopyWithPlaceholder()
          ? _value.etaAt
          // ignore: cast_nullable_to_non_nullable
          : etaAt as DateTime?,
      asOf: asOf == const $CopyWithPlaceholder() || asOf == null
          ? _value.asOf
          // ignore: cast_nullable_to_non_nullable
          : asOf as DateTime,
      stale: stale == const $CopyWithPlaceholder() || stale == null
          ? _value.stale
          // ignore: cast_nullable_to_non_nullable
          : stale as bool,
    );
  }
}

extension $EtaResultCopyWith on EtaResult {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEtaResult.copyWith(...)` or `instanceOfEtaResult.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EtaResultCWProxy get copyWith => _$EtaResultCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EtaResult _$EtaResultFromJson(Map<String, dynamic> json) =>
    $checkedCreate('EtaResult', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'record',
          'target',
          'distanceMeters',
          'approaching',
          'asOf',
          'stale',
        ],
      );
      final val = EtaResult(
        record: $checkedConvert(
          'record',
          (v) => KinematicState.fromJson(v as Map<String, dynamic>),
        ),
        target: $checkedConvert(
          'target',
          (v) => EtaResultTarget.fromJson(v as Map<String, dynamic>),
        ),
        distanceMeters: $checkedConvert('distanceMeters', (v) => v as num),
        bearingDeg: $checkedConvert('bearingDeg', (v) => v as num?),
        bearingDiffDeg: $checkedConvert('bearingDiffDeg', (v) => v as num?),
        approaching: $checkedConvert('approaching', (v) => v as bool),
        etaSeconds: $checkedConvert('etaSeconds', (v) => v as num?),
        etaAt: $checkedConvert(
          'etaAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        asOf: $checkedConvert('asOf', (v) => DateTime.parse(v as String)),
        stale: $checkedConvert('stale', (v) => v as bool),
      );
      return val;
    });

Map<String, dynamic> _$EtaResultToJson(EtaResult instance) => <String, dynamic>{
  'record': instance.record.toJson(),
  'target': instance.target.toJson(),
  'distanceMeters': instance.distanceMeters,
  'bearingDeg': ?instance.bearingDeg,
  'bearingDiffDeg': ?instance.bearingDiffDeg,
  'approaching': instance.approaching,
  'etaSeconds': ?instance.etaSeconds,
  'etaAt': ?instance.etaAt?.toIso8601String(),
  'asOf': instance.asOf.toIso8601String(),
  'stale': instance.stale,
};
