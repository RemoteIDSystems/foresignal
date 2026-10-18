// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'cpa_result.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CpaResultCWProxy {
  CpaResult a(KinematicState a);

  CpaResult b(KinematicState b);

  CpaResult t0(DateTime t0);

  CpaResult tcpaSeconds(num tcpaSeconds);

  CpaResult cpaAt(DateTime cpaAt);

  CpaResult dcpaMeters(num dcpaMeters);

  CpaResult verticalSeparationM(num? verticalSeparationM);

  CpaResult horizon(String? horizon);

  CpaResult asOf(DateTime asOf);

  CpaResult stale(bool stale);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CpaResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CpaResult(...).copyWith(id: 12, name: "My name")
  /// ```
  CpaResult call({
    KinematicState a,
    KinematicState b,
    DateTime t0,
    num tcpaSeconds,
    DateTime cpaAt,
    num dcpaMeters,
    num? verticalSeparationM,
    String? horizon,
    DateTime asOf,
    bool stale,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfCpaResult.copyWith(...)` or call `instanceOfCpaResult.copyWith.fieldName(value)` for a single field.
class _$CpaResultCWProxyImpl implements _$CpaResultCWProxy {
  const _$CpaResultCWProxyImpl(this._value);

  final CpaResult _value;

  @override
  CpaResult a(KinematicState a) => call(a: a);

  @override
  CpaResult b(KinematicState b) => call(b: b);

  @override
  CpaResult t0(DateTime t0) => call(t0: t0);

  @override
  CpaResult tcpaSeconds(num tcpaSeconds) => call(tcpaSeconds: tcpaSeconds);

  @override
  CpaResult cpaAt(DateTime cpaAt) => call(cpaAt: cpaAt);

  @override
  CpaResult dcpaMeters(num dcpaMeters) => call(dcpaMeters: dcpaMeters);

  @override
  CpaResult verticalSeparationM(num? verticalSeparationM) =>
      call(verticalSeparationM: verticalSeparationM);

  @override
  CpaResult horizon(String? horizon) => call(horizon: horizon);

  @override
  CpaResult asOf(DateTime asOf) => call(asOf: asOf);

  @override
  CpaResult stale(bool stale) => call(stale: stale);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CpaResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CpaResult(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  CpaResult call({
    Object? a = const $CopyWithPlaceholder(),
    Object? b = const $CopyWithPlaceholder(),
    Object? t0 = const $CopyWithPlaceholder(),
    Object? tcpaSeconds = const $CopyWithPlaceholder(),
    Object? cpaAt = const $CopyWithPlaceholder(),
    Object? dcpaMeters = const $CopyWithPlaceholder(),
    Object? verticalSeparationM = const $CopyWithPlaceholder(),
    Object? horizon = const $CopyWithPlaceholder(),
    Object? asOf = const $CopyWithPlaceholder(),
    Object? stale = const $CopyWithPlaceholder(),
  }) {
    return CpaResult(
      a: a == const $CopyWithPlaceholder() || a == null
          ? _value.a
          // ignore: cast_nullable_to_non_nullable
          : a as KinematicState,
      b: b == const $CopyWithPlaceholder() || b == null
          ? _value.b
          // ignore: cast_nullable_to_non_nullable
          : b as KinematicState,
      t0: t0 == const $CopyWithPlaceholder() || t0 == null
          ? _value.t0
          // ignore: cast_nullable_to_non_nullable
          : t0 as DateTime,
      tcpaSeconds:
          tcpaSeconds == const $CopyWithPlaceholder() || tcpaSeconds == null
          ? _value.tcpaSeconds
          // ignore: cast_nullable_to_non_nullable
          : tcpaSeconds as num,
      cpaAt: cpaAt == const $CopyWithPlaceholder() || cpaAt == null
          ? _value.cpaAt
          // ignore: cast_nullable_to_non_nullable
          : cpaAt as DateTime,
      dcpaMeters:
          dcpaMeters == const $CopyWithPlaceholder() || dcpaMeters == null
          ? _value.dcpaMeters
          // ignore: cast_nullable_to_non_nullable
          : dcpaMeters as num,
      verticalSeparationM: verticalSeparationM == const $CopyWithPlaceholder()
          ? _value.verticalSeparationM
          // ignore: cast_nullable_to_non_nullable
          : verticalSeparationM as num?,
      horizon: horizon == const $CopyWithPlaceholder()
          ? _value.horizon
          // ignore: cast_nullable_to_non_nullable
          : horizon as String?,
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

extension $CpaResultCopyWith on CpaResult {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfCpaResult.copyWith(...)` or `instanceOfCpaResult.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CpaResultCWProxy get copyWith => _$CpaResultCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CpaResult _$CpaResultFromJson(Map<String, dynamic> json) =>
    $checkedCreate('CpaResult', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'a',
          'b',
          't0',
          'tcpaSeconds',
          'cpaAt',
          'dcpaMeters',
          'asOf',
          'stale',
        ],
      );
      final val = CpaResult(
        a: $checkedConvert(
          'a',
          (v) => KinematicState.fromJson(v as Map<String, dynamic>),
        ),
        b: $checkedConvert(
          'b',
          (v) => KinematicState.fromJson(v as Map<String, dynamic>),
        ),
        t0: $checkedConvert('t0', (v) => DateTime.parse(v as String)),
        tcpaSeconds: $checkedConvert('tcpaSeconds', (v) => v as num),
        cpaAt: $checkedConvert('cpaAt', (v) => DateTime.parse(v as String)),
        dcpaMeters: $checkedConvert('dcpaMeters', (v) => v as num),
        verticalSeparationM: $checkedConvert(
          'verticalSeparationM',
          (v) => v as num?,
        ),
        horizon: $checkedConvert('horizon', (v) => v as String?),
        asOf: $checkedConvert('asOf', (v) => DateTime.parse(v as String)),
        stale: $checkedConvert('stale', (v) => v as bool),
      );
      return val;
    });

Map<String, dynamic> _$CpaResultToJson(CpaResult instance) => <String, dynamic>{
  'a': instance.a.toJson(),
  'b': instance.b.toJson(),
  't0': instance.t0.toIso8601String(),
  'tcpaSeconds': instance.tcpaSeconds,
  'cpaAt': instance.cpaAt.toIso8601String(),
  'dcpaMeters': instance.dcpaMeters,
  'verticalSeparationM': ?instance.verticalSeparationM,
  'horizon': ?instance.horizon,
  'asOf': instance.asOf.toIso8601String(),
  'stale': instance.stale,
};
