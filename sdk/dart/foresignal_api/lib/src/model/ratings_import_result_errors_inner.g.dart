// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ratings_import_result_errors_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RatingsImportResultErrorsInnerCWProxy {
  RatingsImportResultErrorsInner row(int? row);

  RatingsImportResultErrorsInner message(String message);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RatingsImportResultErrorsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RatingsImportResultErrorsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  RatingsImportResultErrorsInner call({int? row, String message});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRatingsImportResultErrorsInner.copyWith(...)` or call `instanceOfRatingsImportResultErrorsInner.copyWith.fieldName(value)` for a single field.
class _$RatingsImportResultErrorsInnerCWProxyImpl
    implements _$RatingsImportResultErrorsInnerCWProxy {
  const _$RatingsImportResultErrorsInnerCWProxyImpl(this._value);

  final RatingsImportResultErrorsInner _value;

  @override
  RatingsImportResultErrorsInner row(int? row) => call(row: row);

  @override
  RatingsImportResultErrorsInner message(String message) =>
      call(message: message);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RatingsImportResultErrorsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RatingsImportResultErrorsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RatingsImportResultErrorsInner call({
    Object? row = const $CopyWithPlaceholder(),
    Object? message = const $CopyWithPlaceholder(),
  }) {
    return RatingsImportResultErrorsInner(
      row: row == const $CopyWithPlaceholder()
          ? _value.row
          // ignore: cast_nullable_to_non_nullable
          : row as int?,
      message: message == const $CopyWithPlaceholder() || message == null
          ? _value.message
          // ignore: cast_nullable_to_non_nullable
          : message as String,
    );
  }
}

extension $RatingsImportResultErrorsInnerCopyWith
    on RatingsImportResultErrorsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRatingsImportResultErrorsInner.copyWith(...)` or `instanceOfRatingsImportResultErrorsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RatingsImportResultErrorsInnerCWProxy get copyWith =>
      _$RatingsImportResultErrorsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RatingsImportResultErrorsInner _$RatingsImportResultErrorsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('RatingsImportResultErrorsInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['message']);
  final val = RatingsImportResultErrorsInner(
    row: $checkedConvert('row', (v) => (v as num?)?.toInt()),
    message: $checkedConvert('message', (v) => v as String),
  );
  return val;
});

Map<String, dynamic> _$RatingsImportResultErrorsInnerToJson(
  RatingsImportResultErrorsInner instance,
) => <String, dynamic>{'row': ?instance.row, 'message': instance.message};
