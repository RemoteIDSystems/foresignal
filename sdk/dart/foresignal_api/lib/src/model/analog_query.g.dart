// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'analog_query.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AnalogQueryCWProxy {
  AnalogQuery regionCode(String regionCode);

  AnalogQuery from(DateTime from);

  AnalogQuery to(DateTime to);

  AnalogQuery k(int? k);

  AnalogQuery outcome(String? outcome);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalogQuery(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalogQuery(...).copyWith(id: 12, name: "My name")
  /// ```
  AnalogQuery call({
    String regionCode,
    DateTime from,
    DateTime to,
    int? k,
    String? outcome,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAnalogQuery.copyWith(...)` or call `instanceOfAnalogQuery.copyWith.fieldName(value)` for a single field.
class _$AnalogQueryCWProxyImpl implements _$AnalogQueryCWProxy {
  const _$AnalogQueryCWProxyImpl(this._value);

  final AnalogQuery _value;

  @override
  AnalogQuery regionCode(String regionCode) => call(regionCode: regionCode);

  @override
  AnalogQuery from(DateTime from) => call(from: from);

  @override
  AnalogQuery to(DateTime to) => call(to: to);

  @override
  AnalogQuery k(int? k) => call(k: k);

  @override
  AnalogQuery outcome(String? outcome) => call(outcome: outcome);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalogQuery(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalogQuery(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AnalogQuery call({
    Object? regionCode = const $CopyWithPlaceholder(),
    Object? from = const $CopyWithPlaceholder(),
    Object? to = const $CopyWithPlaceholder(),
    Object? k = const $CopyWithPlaceholder(),
    Object? outcome = const $CopyWithPlaceholder(),
  }) {
    return AnalogQuery(
      regionCode:
          regionCode == const $CopyWithPlaceholder() || regionCode == null
          ? _value.regionCode
          // ignore: cast_nullable_to_non_nullable
          : regionCode as String,
      from: from == const $CopyWithPlaceholder() || from == null
          ? _value.from
          // ignore: cast_nullable_to_non_nullable
          : from as DateTime,
      to: to == const $CopyWithPlaceholder() || to == null
          ? _value.to
          // ignore: cast_nullable_to_non_nullable
          : to as DateTime,
      k: k == const $CopyWithPlaceholder()
          ? _value.k
          // ignore: cast_nullable_to_non_nullable
          : k as int?,
      outcome: outcome == const $CopyWithPlaceholder()
          ? _value.outcome
          // ignore: cast_nullable_to_non_nullable
          : outcome as String?,
    );
  }
}

extension $AnalogQueryCopyWith on AnalogQuery {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAnalogQuery.copyWith(...)` or `instanceOfAnalogQuery.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AnalogQueryCWProxy get copyWith => _$AnalogQueryCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AnalogQuery _$AnalogQueryFromJson(Map<String, dynamic> json) =>
    $checkedCreate('AnalogQuery', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['regionCode', 'from', 'to']);
      final val = AnalogQuery(
        regionCode: $checkedConvert('regionCode', (v) => v as String),
        from: $checkedConvert('from', (v) => DateTime.parse(v as String)),
        to: $checkedConvert('to', (v) => DateTime.parse(v as String)),
        k: $checkedConvert('k', (v) => (v as num?)?.toInt() ?? 10),
        outcome: $checkedConvert('outcome', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$AnalogQueryToJson(AnalogQuery instance) =>
    <String, dynamic>{
      'regionCode': instance.regionCode,
      'from': instance.from.toIso8601String(),
      'to': instance.to.toIso8601String(),
      'k': ?instance.k,
      'outcome': ?instance.outcome,
    };
