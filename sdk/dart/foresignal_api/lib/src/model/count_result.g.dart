// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'count_result.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CountResultCWProxy {
  CountResult count(int count);

  CountResult exact(bool exact);

  CountResult watermarkSeq(int watermarkSeq);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CountResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CountResult(...).copyWith(id: 12, name: "My name")
  /// ```
  CountResult call({int count, bool exact, int watermarkSeq});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfCountResult.copyWith(...)` or call `instanceOfCountResult.copyWith.fieldName(value)` for a single field.
class _$CountResultCWProxyImpl implements _$CountResultCWProxy {
  const _$CountResultCWProxyImpl(this._value);

  final CountResult _value;

  @override
  CountResult count(int count) => call(count: count);

  @override
  CountResult exact(bool exact) => call(exact: exact);

  @override
  CountResult watermarkSeq(int watermarkSeq) =>
      call(watermarkSeq: watermarkSeq);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CountResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CountResult(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  CountResult call({
    Object? count = const $CopyWithPlaceholder(),
    Object? exact = const $CopyWithPlaceholder(),
    Object? watermarkSeq = const $CopyWithPlaceholder(),
  }) {
    return CountResult(
      count: count == const $CopyWithPlaceholder() || count == null
          ? _value.count
          // ignore: cast_nullable_to_non_nullable
          : count as int,
      exact: exact == const $CopyWithPlaceholder() || exact == null
          ? _value.exact
          // ignore: cast_nullable_to_non_nullable
          : exact as bool,
      watermarkSeq:
          watermarkSeq == const $CopyWithPlaceholder() || watermarkSeq == null
          ? _value.watermarkSeq
          // ignore: cast_nullable_to_non_nullable
          : watermarkSeq as int,
    );
  }
}

extension $CountResultCopyWith on CountResult {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfCountResult.copyWith(...)` or `instanceOfCountResult.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CountResultCWProxy get copyWith => _$CountResultCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CountResult _$CountResultFromJson(Map<String, dynamic> json) => $checkedCreate(
  'CountResult',
  json,
  ($checkedConvert) {
    $checkKeys(json, requiredKeys: const ['count', 'exact', 'watermarkSeq']);
    final val = CountResult(
      count: $checkedConvert('count', (v) => (v as num).toInt()),
      exact: $checkedConvert('exact', (v) => v as bool),
      watermarkSeq: $checkedConvert('watermarkSeq', (v) => (v as num).toInt()),
    );
    return val;
  },
);

Map<String, dynamic> _$CountResultToJson(CountResult instance) =>
    <String, dynamic>{
      'count': instance.count,
      'exact': instance.exact,
      'watermarkSeq': instance.watermarkSeq,
    };
