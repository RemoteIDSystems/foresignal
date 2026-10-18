// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'precursor_history_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PrecursorHistoryInnerCWProxy {
  PrecursorHistoryInner at(DateTime at);

  PrecursorHistoryInner by(String by);

  PrecursorHistoryInner from(String? from);

  PrecursorHistoryInner to(String to);

  PrecursorHistoryInner reason(String? reason);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PrecursorHistoryInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PrecursorHistoryInner(...).copyWith(id: 12, name: "My name")
  /// ```
  PrecursorHistoryInner call({
    DateTime at,
    String by,
    String? from,
    String to,
    String? reason,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPrecursorHistoryInner.copyWith(...)` or call `instanceOfPrecursorHistoryInner.copyWith.fieldName(value)` for a single field.
class _$PrecursorHistoryInnerCWProxyImpl
    implements _$PrecursorHistoryInnerCWProxy {
  const _$PrecursorHistoryInnerCWProxyImpl(this._value);

  final PrecursorHistoryInner _value;

  @override
  PrecursorHistoryInner at(DateTime at) => call(at: at);

  @override
  PrecursorHistoryInner by(String by) => call(by: by);

  @override
  PrecursorHistoryInner from(String? from) => call(from: from);

  @override
  PrecursorHistoryInner to(String to) => call(to: to);

  @override
  PrecursorHistoryInner reason(String? reason) => call(reason: reason);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PrecursorHistoryInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PrecursorHistoryInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PrecursorHistoryInner call({
    Object? at = const $CopyWithPlaceholder(),
    Object? by = const $CopyWithPlaceholder(),
    Object? from = const $CopyWithPlaceholder(),
    Object? to = const $CopyWithPlaceholder(),
    Object? reason = const $CopyWithPlaceholder(),
  }) {
    return PrecursorHistoryInner(
      at: at == const $CopyWithPlaceholder() || at == null
          ? _value.at
          // ignore: cast_nullable_to_non_nullable
          : at as DateTime,
      by: by == const $CopyWithPlaceholder() || by == null
          ? _value.by
          // ignore: cast_nullable_to_non_nullable
          : by as String,
      from: from == const $CopyWithPlaceholder()
          ? _value.from
          // ignore: cast_nullable_to_non_nullable
          : from as String?,
      to: to == const $CopyWithPlaceholder() || to == null
          ? _value.to
          // ignore: cast_nullable_to_non_nullable
          : to as String,
      reason: reason == const $CopyWithPlaceholder()
          ? _value.reason
          // ignore: cast_nullable_to_non_nullable
          : reason as String?,
    );
  }
}

extension $PrecursorHistoryInnerCopyWith on PrecursorHistoryInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPrecursorHistoryInner.copyWith(...)` or `instanceOfPrecursorHistoryInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PrecursorHistoryInnerCWProxy get copyWith =>
      _$PrecursorHistoryInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PrecursorHistoryInner _$PrecursorHistoryInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('PrecursorHistoryInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['at', 'by', 'to']);
  final val = PrecursorHistoryInner(
    at: $checkedConvert('at', (v) => DateTime.parse(v as String)),
    by: $checkedConvert('by', (v) => v as String),
    from: $checkedConvert('from', (v) => v as String?),
    to: $checkedConvert('to', (v) => v as String),
    reason: $checkedConvert('reason', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$PrecursorHistoryInnerToJson(
  PrecursorHistoryInner instance,
) => <String, dynamic>{
  'at': instance.at.toIso8601String(),
  'by': instance.by,
  'from': ?instance.from,
  'to': instance.to,
  'reason': ?instance.reason,
};
