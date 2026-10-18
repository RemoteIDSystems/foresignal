// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'edp_validation_request.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EdpValidationRequestCWProxy {
  EdpValidationRequest manifest(EdpManifest? manifest);

  EdpValidationRequest sourceId(String? sourceId);

  EdpValidationRequest items(List<Map<String, Object>>? items);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdpValidationRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdpValidationRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  EdpValidationRequest call({
    EdpManifest? manifest,
    String? sourceId,
    List<Map<String, Object>>? items,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEdpValidationRequest.copyWith(...)` or call `instanceOfEdpValidationRequest.copyWith.fieldName(value)` for a single field.
class _$EdpValidationRequestCWProxyImpl
    implements _$EdpValidationRequestCWProxy {
  const _$EdpValidationRequestCWProxyImpl(this._value);

  final EdpValidationRequest _value;

  @override
  EdpValidationRequest manifest(EdpManifest? manifest) =>
      call(manifest: manifest);

  @override
  EdpValidationRequest sourceId(String? sourceId) => call(sourceId: sourceId);

  @override
  EdpValidationRequest items(List<Map<String, Object>>? items) =>
      call(items: items);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdpValidationRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdpValidationRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EdpValidationRequest call({
    Object? manifest = const $CopyWithPlaceholder(),
    Object? sourceId = const $CopyWithPlaceholder(),
    Object? items = const $CopyWithPlaceholder(),
  }) {
    return EdpValidationRequest(
      manifest: manifest == const $CopyWithPlaceholder()
          ? _value.manifest
          // ignore: cast_nullable_to_non_nullable
          : manifest as EdpManifest?,
      sourceId: sourceId == const $CopyWithPlaceholder()
          ? _value.sourceId
          // ignore: cast_nullable_to_non_nullable
          : sourceId as String?,
      items: items == const $CopyWithPlaceholder()
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<Map<String, Object>>?,
    );
  }
}

extension $EdpValidationRequestCopyWith on EdpValidationRequest {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEdpValidationRequest.copyWith(...)` or `instanceOfEdpValidationRequest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EdpValidationRequestCWProxy get copyWith =>
      _$EdpValidationRequestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EdpValidationRequest _$EdpValidationRequestFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('EdpValidationRequest', json, ($checkedConvert) {
  final val = EdpValidationRequest(
    manifest: $checkedConvert(
      'manifest',
      (v) => v == null ? null : EdpManifest.fromJson(v as Map<String, dynamic>),
    ),
    sourceId: $checkedConvert('sourceId', (v) => v as String?),
    items: $checkedConvert(
      'items',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => (e as Map<String, dynamic>).map(
              (k, e) => MapEntry(k, e as Object),
            ),
          )
          .toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$EdpValidationRequestToJson(
  EdpValidationRequest instance,
) => <String, dynamic>{
  'manifest': ?instance.manifest?.toJson(),
  'sourceId': ?instance.sourceId,
  'items': ?instance.items,
};
