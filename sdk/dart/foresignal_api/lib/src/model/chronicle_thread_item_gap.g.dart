// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chronicle_thread_item_gap.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChronicleThreadItemGapCWProxy {
  ChronicleThreadItemGap from(DateTime from);

  ChronicleThreadItemGap to(DateTime to);

  ChronicleThreadItemGap reason(String? reason);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleThreadItemGap(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleThreadItemGap(...).copyWith(id: 12, name: "My name")
  /// ```
  ChronicleThreadItemGap call({DateTime from, DateTime to, String? reason});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChronicleThreadItemGap.copyWith(...)` or call `instanceOfChronicleThreadItemGap.copyWith.fieldName(value)` for a single field.
class _$ChronicleThreadItemGapCWProxyImpl
    implements _$ChronicleThreadItemGapCWProxy {
  const _$ChronicleThreadItemGapCWProxyImpl(this._value);

  final ChronicleThreadItemGap _value;

  @override
  ChronicleThreadItemGap from(DateTime from) => call(from: from);

  @override
  ChronicleThreadItemGap to(DateTime to) => call(to: to);

  @override
  ChronicleThreadItemGap reason(String? reason) => call(reason: reason);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleThreadItemGap(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleThreadItemGap(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChronicleThreadItemGap call({
    Object? from = const $CopyWithPlaceholder(),
    Object? to = const $CopyWithPlaceholder(),
    Object? reason = const $CopyWithPlaceholder(),
  }) {
    return ChronicleThreadItemGap(
      from: from == const $CopyWithPlaceholder() || from == null
          ? _value.from
          // ignore: cast_nullable_to_non_nullable
          : from as DateTime,
      to: to == const $CopyWithPlaceholder() || to == null
          ? _value.to
          // ignore: cast_nullable_to_non_nullable
          : to as DateTime,
      reason: reason == const $CopyWithPlaceholder()
          ? _value.reason
          // ignore: cast_nullable_to_non_nullable
          : reason as String?,
    );
  }
}

extension $ChronicleThreadItemGapCopyWith on ChronicleThreadItemGap {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChronicleThreadItemGap.copyWith(...)` or `instanceOfChronicleThreadItemGap.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChronicleThreadItemGapCWProxy get copyWith =>
      _$ChronicleThreadItemGapCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChronicleThreadItemGap _$ChronicleThreadItemGapFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ChronicleThreadItemGap', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['from', 'to']);
  final val = ChronicleThreadItemGap(
    from: $checkedConvert('from', (v) => DateTime.parse(v as String)),
    to: $checkedConvert('to', (v) => DateTime.parse(v as String)),
    reason: $checkedConvert('reason', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$ChronicleThreadItemGapToJson(
  ChronicleThreadItemGap instance,
) => <String, dynamic>{
  'from': instance.from.toIso8601String(),
  'to': instance.to.toIso8601String(),
  'reason': ?instance.reason,
};
