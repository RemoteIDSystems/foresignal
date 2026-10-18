// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'claim_event_time.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ClaimEventTimeCWProxy {
  ClaimEventTime from(DateTime from);

  ClaimEventTime to(DateTime? to);

  ClaimEventTime precision(String precision);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ClaimEventTime(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ClaimEventTime(...).copyWith(id: 12, name: "My name")
  /// ```
  ClaimEventTime call({DateTime from, DateTime? to, String precision});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfClaimEventTime.copyWith(...)` or call `instanceOfClaimEventTime.copyWith.fieldName(value)` for a single field.
class _$ClaimEventTimeCWProxyImpl implements _$ClaimEventTimeCWProxy {
  const _$ClaimEventTimeCWProxyImpl(this._value);

  final ClaimEventTime _value;

  @override
  ClaimEventTime from(DateTime from) => call(from: from);

  @override
  ClaimEventTime to(DateTime? to) => call(to: to);

  @override
  ClaimEventTime precision(String precision) => call(precision: precision);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ClaimEventTime(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ClaimEventTime(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ClaimEventTime call({
    Object? from = const $CopyWithPlaceholder(),
    Object? to = const $CopyWithPlaceholder(),
    Object? precision = const $CopyWithPlaceholder(),
  }) {
    return ClaimEventTime(
      from: from == const $CopyWithPlaceholder() || from == null
          ? _value.from
          // ignore: cast_nullable_to_non_nullable
          : from as DateTime,
      to: to == const $CopyWithPlaceholder()
          ? _value.to
          // ignore: cast_nullable_to_non_nullable
          : to as DateTime?,
      precision: precision == const $CopyWithPlaceholder() || precision == null
          ? _value.precision
          // ignore: cast_nullable_to_non_nullable
          : precision as String,
    );
  }
}

extension $ClaimEventTimeCopyWith on ClaimEventTime {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfClaimEventTime.copyWith(...)` or `instanceOfClaimEventTime.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ClaimEventTimeCWProxy get copyWith => _$ClaimEventTimeCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ClaimEventTime _$ClaimEventTimeFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ClaimEventTime', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['from', 'precision']);
      final val = ClaimEventTime(
        from: $checkedConvert('from', (v) => DateTime.parse(v as String)),
        to: $checkedConvert(
          'to',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        precision: $checkedConvert('precision', (v) => v as String),
      );
      return val;
    });

Map<String, dynamic> _$ClaimEventTimeToJson(ClaimEventTime instance) =>
    <String, dynamic>{
      'from': instance.from.toIso8601String(),
      'to': ?instance.to?.toIso8601String(),
      'precision': instance.precision,
    };
