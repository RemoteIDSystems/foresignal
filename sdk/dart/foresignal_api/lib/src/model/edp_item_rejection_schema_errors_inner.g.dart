// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'edp_item_rejection_schema_errors_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EdpItemRejectionSchemaErrorsInnerCWProxy {
  EdpItemRejectionSchemaErrorsInner path(String? path);

  EdpItemRejectionSchemaErrorsInner message(String? message);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdpItemRejectionSchemaErrorsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdpItemRejectionSchemaErrorsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  EdpItemRejectionSchemaErrorsInner call({String? path, String? message});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEdpItemRejectionSchemaErrorsInner.copyWith(...)` or call `instanceOfEdpItemRejectionSchemaErrorsInner.copyWith.fieldName(value)` for a single field.
class _$EdpItemRejectionSchemaErrorsInnerCWProxyImpl
    implements _$EdpItemRejectionSchemaErrorsInnerCWProxy {
  const _$EdpItemRejectionSchemaErrorsInnerCWProxyImpl(this._value);

  final EdpItemRejectionSchemaErrorsInner _value;

  @override
  EdpItemRejectionSchemaErrorsInner path(String? path) => call(path: path);

  @override
  EdpItemRejectionSchemaErrorsInner message(String? message) =>
      call(message: message);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdpItemRejectionSchemaErrorsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdpItemRejectionSchemaErrorsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EdpItemRejectionSchemaErrorsInner call({
    Object? path = const $CopyWithPlaceholder(),
    Object? message = const $CopyWithPlaceholder(),
  }) {
    return EdpItemRejectionSchemaErrorsInner(
      path: path == const $CopyWithPlaceholder()
          ? _value.path
          // ignore: cast_nullable_to_non_nullable
          : path as String?,
      message: message == const $CopyWithPlaceholder()
          ? _value.message
          // ignore: cast_nullable_to_non_nullable
          : message as String?,
    );
  }
}

extension $EdpItemRejectionSchemaErrorsInnerCopyWith
    on EdpItemRejectionSchemaErrorsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEdpItemRejectionSchemaErrorsInner.copyWith(...)` or `instanceOfEdpItemRejectionSchemaErrorsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EdpItemRejectionSchemaErrorsInnerCWProxy get copyWith =>
      _$EdpItemRejectionSchemaErrorsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EdpItemRejectionSchemaErrorsInner _$EdpItemRejectionSchemaErrorsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('EdpItemRejectionSchemaErrorsInner', json, (
  $checkedConvert,
) {
  final val = EdpItemRejectionSchemaErrorsInner(
    path: $checkedConvert('path', (v) => v as String?),
    message: $checkedConvert('message', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$EdpItemRejectionSchemaErrorsInnerToJson(
  EdpItemRejectionSchemaErrorsInner instance,
) => <String, dynamic>{'path': ?instance.path, 'message': ?instance.message};
