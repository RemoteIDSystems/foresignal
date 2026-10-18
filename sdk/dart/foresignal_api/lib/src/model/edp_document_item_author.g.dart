// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'edp_document_item_author.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EdpDocumentItemAuthorCWProxy {
  EdpDocumentItemAuthor handle(String? handle);

  EdpDocumentItemAuthor name(String? name);

  EdpDocumentItemAuthor accountId(String? accountId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdpDocumentItemAuthor(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdpDocumentItemAuthor(...).copyWith(id: 12, name: "My name")
  /// ```
  EdpDocumentItemAuthor call({String? handle, String? name, String? accountId});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEdpDocumentItemAuthor.copyWith(...)` or call `instanceOfEdpDocumentItemAuthor.copyWith.fieldName(value)` for a single field.
class _$EdpDocumentItemAuthorCWProxyImpl
    implements _$EdpDocumentItemAuthorCWProxy {
  const _$EdpDocumentItemAuthorCWProxyImpl(this._value);

  final EdpDocumentItemAuthor _value;

  @override
  EdpDocumentItemAuthor handle(String? handle) => call(handle: handle);

  @override
  EdpDocumentItemAuthor name(String? name) => call(name: name);

  @override
  EdpDocumentItemAuthor accountId(String? accountId) =>
      call(accountId: accountId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdpDocumentItemAuthor(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdpDocumentItemAuthor(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EdpDocumentItemAuthor call({
    Object? handle = const $CopyWithPlaceholder(),
    Object? name = const $CopyWithPlaceholder(),
    Object? accountId = const $CopyWithPlaceholder(),
  }) {
    return EdpDocumentItemAuthor(
      handle: handle == const $CopyWithPlaceholder()
          ? _value.handle
          // ignore: cast_nullable_to_non_nullable
          : handle as String?,
      name: name == const $CopyWithPlaceholder()
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String?,
      accountId: accountId == const $CopyWithPlaceholder()
          ? _value.accountId
          // ignore: cast_nullable_to_non_nullable
          : accountId as String?,
    );
  }
}

extension $EdpDocumentItemAuthorCopyWith on EdpDocumentItemAuthor {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEdpDocumentItemAuthor.copyWith(...)` or `instanceOfEdpDocumentItemAuthor.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EdpDocumentItemAuthorCWProxy get copyWith =>
      _$EdpDocumentItemAuthorCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EdpDocumentItemAuthor _$EdpDocumentItemAuthorFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('EdpDocumentItemAuthor', json, ($checkedConvert) {
  final val = EdpDocumentItemAuthor(
    handle: $checkedConvert('handle', (v) => v as String?),
    name: $checkedConvert('name', (v) => v as String?),
    accountId: $checkedConvert('accountId', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$EdpDocumentItemAuthorToJson(
  EdpDocumentItemAuthor instance,
) => <String, dynamic>{
  'handle': ?instance.handle,
  'name': ?instance.name,
  'accountId': ?instance.accountId,
};
