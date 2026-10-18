// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'precursor_window_stats_reverse.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PrecursorWindowStatsReverseCWProxy {
  PrecursorWindowStatsReverse p(num? p);

  PrecursorWindowStatsReverse statistic(num? statistic);

  PrecursorWindowStatsReverse coMovement(bool coMovement);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PrecursorWindowStatsReverse(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PrecursorWindowStatsReverse(...).copyWith(id: 12, name: "My name")
  /// ```
  PrecursorWindowStatsReverse call({num? p, num? statistic, bool coMovement});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPrecursorWindowStatsReverse.copyWith(...)` or call `instanceOfPrecursorWindowStatsReverse.copyWith.fieldName(value)` for a single field.
class _$PrecursorWindowStatsReverseCWProxyImpl
    implements _$PrecursorWindowStatsReverseCWProxy {
  const _$PrecursorWindowStatsReverseCWProxyImpl(this._value);

  final PrecursorWindowStatsReverse _value;

  @override
  PrecursorWindowStatsReverse p(num? p) => call(p: p);

  @override
  PrecursorWindowStatsReverse statistic(num? statistic) =>
      call(statistic: statistic);

  @override
  PrecursorWindowStatsReverse coMovement(bool coMovement) =>
      call(coMovement: coMovement);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PrecursorWindowStatsReverse(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PrecursorWindowStatsReverse(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PrecursorWindowStatsReverse call({
    Object? p = const $CopyWithPlaceholder(),
    Object? statistic = const $CopyWithPlaceholder(),
    Object? coMovement = const $CopyWithPlaceholder(),
  }) {
    return PrecursorWindowStatsReverse(
      p: p == const $CopyWithPlaceholder()
          ? _value.p
          // ignore: cast_nullable_to_non_nullable
          : p as num?,
      statistic: statistic == const $CopyWithPlaceholder()
          ? _value.statistic
          // ignore: cast_nullable_to_non_nullable
          : statistic as num?,
      coMovement:
          coMovement == const $CopyWithPlaceholder() || coMovement == null
          ? _value.coMovement
          // ignore: cast_nullable_to_non_nullable
          : coMovement as bool,
    );
  }
}

extension $PrecursorWindowStatsReverseCopyWith on PrecursorWindowStatsReverse {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPrecursorWindowStatsReverse.copyWith(...)` or `instanceOfPrecursorWindowStatsReverse.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PrecursorWindowStatsReverseCWProxy get copyWith =>
      _$PrecursorWindowStatsReverseCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PrecursorWindowStatsReverse _$PrecursorWindowStatsReverseFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('PrecursorWindowStatsReverse', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['coMovement']);
  final val = PrecursorWindowStatsReverse(
    p: $checkedConvert('p', (v) => v as num?),
    statistic: $checkedConvert('statistic', (v) => v as num?),
    coMovement: $checkedConvert('coMovement', (v) => v as bool),
  );
  return val;
});

Map<String, dynamic> _$PrecursorWindowStatsReverseToJson(
  PrecursorWindowStatsReverse instance,
) => <String, dynamic>{
  'p': ?instance.p,
  'statistic': ?instance.statistic,
  'coMovement': instance.coMovement,
};
