// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'scorecard_entry_recalibration.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ScorecardEntryRecalibrationCWProxy {
  ScorecardEntryRecalibration a(num a);

  ScorecardEntryRecalibration b(num b);

  ScorecardEntryRecalibration n(int n);

  ScorecardEntryRecalibration fittedAt(DateTime? fittedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ScorecardEntryRecalibration(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ScorecardEntryRecalibration(...).copyWith(id: 12, name: "My name")
  /// ```
  ScorecardEntryRecalibration call({num a, num b, int n, DateTime? fittedAt});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfScorecardEntryRecalibration.copyWith(...)` or call `instanceOfScorecardEntryRecalibration.copyWith.fieldName(value)` for a single field.
class _$ScorecardEntryRecalibrationCWProxyImpl
    implements _$ScorecardEntryRecalibrationCWProxy {
  const _$ScorecardEntryRecalibrationCWProxyImpl(this._value);

  final ScorecardEntryRecalibration _value;

  @override
  ScorecardEntryRecalibration a(num a) => call(a: a);

  @override
  ScorecardEntryRecalibration b(num b) => call(b: b);

  @override
  ScorecardEntryRecalibration n(int n) => call(n: n);

  @override
  ScorecardEntryRecalibration fittedAt(DateTime? fittedAt) =>
      call(fittedAt: fittedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ScorecardEntryRecalibration(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ScorecardEntryRecalibration(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ScorecardEntryRecalibration call({
    Object? a = const $CopyWithPlaceholder(),
    Object? b = const $CopyWithPlaceholder(),
    Object? n = const $CopyWithPlaceholder(),
    Object? fittedAt = const $CopyWithPlaceholder(),
  }) {
    return ScorecardEntryRecalibration(
      a: a == const $CopyWithPlaceholder() || a == null
          ? _value.a
          // ignore: cast_nullable_to_non_nullable
          : a as num,
      b: b == const $CopyWithPlaceholder() || b == null
          ? _value.b
          // ignore: cast_nullable_to_non_nullable
          : b as num,
      n: n == const $CopyWithPlaceholder() || n == null
          ? _value.n
          // ignore: cast_nullable_to_non_nullable
          : n as int,
      fittedAt: fittedAt == const $CopyWithPlaceholder()
          ? _value.fittedAt
          // ignore: cast_nullable_to_non_nullable
          : fittedAt as DateTime?,
    );
  }
}

extension $ScorecardEntryRecalibrationCopyWith on ScorecardEntryRecalibration {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfScorecardEntryRecalibration.copyWith(...)` or `instanceOfScorecardEntryRecalibration.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ScorecardEntryRecalibrationCWProxy get copyWith =>
      _$ScorecardEntryRecalibrationCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ScorecardEntryRecalibration _$ScorecardEntryRecalibrationFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ScorecardEntryRecalibration', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['a', 'b', 'n']);
  final val = ScorecardEntryRecalibration(
    a: $checkedConvert('a', (v) => v as num),
    b: $checkedConvert('b', (v) => v as num),
    n: $checkedConvert('n', (v) => (v as num).toInt()),
    fittedAt: $checkedConvert(
      'fittedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
  );
  return val;
});

Map<String, dynamic> _$ScorecardEntryRecalibrationToJson(
  ScorecardEntryRecalibration instance,
) => <String, dynamic>{
  'a': instance.a,
  'b': instance.b,
  'n': instance.n,
  'fittedAt': ?instance.fittedAt?.toIso8601String(),
};
