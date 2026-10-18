// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'precursor_test_run_request.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PrecursorTestRunRequestCWProxy {
  PrecursorTestRunRequest from(DateTime? from);

  PrecursorTestRunRequest to(DateTime? to);

  PrecursorTestRunRequest fromSeq(int? fromSeq);

  PrecursorTestRunRequest toSeq(int? toSeq);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PrecursorTestRunRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PrecursorTestRunRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  PrecursorTestRunRequest call({
    DateTime? from,
    DateTime? to,
    int? fromSeq,
    int? toSeq,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPrecursorTestRunRequest.copyWith(...)` or call `instanceOfPrecursorTestRunRequest.copyWith.fieldName(value)` for a single field.
class _$PrecursorTestRunRequestCWProxyImpl
    implements _$PrecursorTestRunRequestCWProxy {
  const _$PrecursorTestRunRequestCWProxyImpl(this._value);

  final PrecursorTestRunRequest _value;

  @override
  PrecursorTestRunRequest from(DateTime? from) => call(from: from);

  @override
  PrecursorTestRunRequest to(DateTime? to) => call(to: to);

  @override
  PrecursorTestRunRequest fromSeq(int? fromSeq) => call(fromSeq: fromSeq);

  @override
  PrecursorTestRunRequest toSeq(int? toSeq) => call(toSeq: toSeq);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PrecursorTestRunRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PrecursorTestRunRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PrecursorTestRunRequest call({
    Object? from = const $CopyWithPlaceholder(),
    Object? to = const $CopyWithPlaceholder(),
    Object? fromSeq = const $CopyWithPlaceholder(),
    Object? toSeq = const $CopyWithPlaceholder(),
  }) {
    return PrecursorTestRunRequest(
      from: from == const $CopyWithPlaceholder()
          ? _value.from
          // ignore: cast_nullable_to_non_nullable
          : from as DateTime?,
      to: to == const $CopyWithPlaceholder()
          ? _value.to
          // ignore: cast_nullable_to_non_nullable
          : to as DateTime?,
      fromSeq: fromSeq == const $CopyWithPlaceholder()
          ? _value.fromSeq
          // ignore: cast_nullable_to_non_nullable
          : fromSeq as int?,
      toSeq: toSeq == const $CopyWithPlaceholder()
          ? _value.toSeq
          // ignore: cast_nullable_to_non_nullable
          : toSeq as int?,
    );
  }
}

extension $PrecursorTestRunRequestCopyWith on PrecursorTestRunRequest {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPrecursorTestRunRequest.copyWith(...)` or `instanceOfPrecursorTestRunRequest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PrecursorTestRunRequestCWProxy get copyWith =>
      _$PrecursorTestRunRequestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PrecursorTestRunRequest _$PrecursorTestRunRequestFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('PrecursorTestRunRequest', json, ($checkedConvert) {
  final val = PrecursorTestRunRequest(
    from: $checkedConvert(
      'from',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    to: $checkedConvert(
      'to',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    fromSeq: $checkedConvert('fromSeq', (v) => (v as num?)?.toInt()),
    toSeq: $checkedConvert('toSeq', (v) => (v as num?)?.toInt()),
  );
  return val;
});

Map<String, dynamic> _$PrecursorTestRunRequestToJson(
  PrecursorTestRunRequest instance,
) => <String, dynamic>{
  'from': ?instance.from?.toIso8601String(),
  'to': ?instance.to?.toIso8601String(),
  'fromSeq': ?instance.fromSeq,
  'toSeq': ?instance.toSeq,
};
