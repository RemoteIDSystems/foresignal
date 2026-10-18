// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'cot_backfill_request.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CotBackfillRequestCWProxy {
  CotBackfillRequest from(DateTime? from);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CotBackfillRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CotBackfillRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  CotBackfillRequest call({DateTime? from});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfCotBackfillRequest.copyWith(...)` or call `instanceOfCotBackfillRequest.copyWith.fieldName(value)` for a single field.
class _$CotBackfillRequestCWProxyImpl implements _$CotBackfillRequestCWProxy {
  const _$CotBackfillRequestCWProxyImpl(this._value);

  final CotBackfillRequest _value;

  @override
  CotBackfillRequest from(DateTime? from) => call(from: from);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CotBackfillRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CotBackfillRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  CotBackfillRequest call({Object? from = const $CopyWithPlaceholder()}) {
    return CotBackfillRequest(
      from: from == const $CopyWithPlaceholder()
          ? _value.from
          // ignore: cast_nullable_to_non_nullable
          : from as DateTime?,
    );
  }
}

extension $CotBackfillRequestCopyWith on CotBackfillRequest {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfCotBackfillRequest.copyWith(...)` or `instanceOfCotBackfillRequest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CotBackfillRequestCWProxy get copyWith =>
      _$CotBackfillRequestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CotBackfillRequest _$CotBackfillRequestFromJson(Map<String, dynamic> json) =>
    $checkedCreate('CotBackfillRequest', json, ($checkedConvert) {
      final val = CotBackfillRequest(
        from: $checkedConvert(
          'from',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$CotBackfillRequestToJson(CotBackfillRequest instance) =>
    <String, dynamic>{'from': ?instance.from?.toIso8601String()};
