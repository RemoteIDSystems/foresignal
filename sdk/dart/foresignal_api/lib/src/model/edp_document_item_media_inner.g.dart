// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'edp_document_item_media_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EdpDocumentItemMediaInnerCWProxy {
  EdpDocumentItemMediaInner type(String? type);

  EdpDocumentItemMediaInner url(String url);

  EdpDocumentItemMediaInner thumbUrl(String? thumbUrl);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdpDocumentItemMediaInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdpDocumentItemMediaInner(...).copyWith(id: 12, name: "My name")
  /// ```
  EdpDocumentItemMediaInner call({String? type, String url, String? thumbUrl});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEdpDocumentItemMediaInner.copyWith(...)` or call `instanceOfEdpDocumentItemMediaInner.copyWith.fieldName(value)` for a single field.
class _$EdpDocumentItemMediaInnerCWProxyImpl
    implements _$EdpDocumentItemMediaInnerCWProxy {
  const _$EdpDocumentItemMediaInnerCWProxyImpl(this._value);

  final EdpDocumentItemMediaInner _value;

  @override
  EdpDocumentItemMediaInner type(String? type) => call(type: type);

  @override
  EdpDocumentItemMediaInner url(String url) => call(url: url);

  @override
  EdpDocumentItemMediaInner thumbUrl(String? thumbUrl) =>
      call(thumbUrl: thumbUrl);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdpDocumentItemMediaInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdpDocumentItemMediaInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EdpDocumentItemMediaInner call({
    Object? type = const $CopyWithPlaceholder(),
    Object? url = const $CopyWithPlaceholder(),
    Object? thumbUrl = const $CopyWithPlaceholder(),
  }) {
    return EdpDocumentItemMediaInner(
      type: type == const $CopyWithPlaceholder()
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as String?,
      url: url == const $CopyWithPlaceholder() || url == null
          ? _value.url
          // ignore: cast_nullable_to_non_nullable
          : url as String,
      thumbUrl: thumbUrl == const $CopyWithPlaceholder()
          ? _value.thumbUrl
          // ignore: cast_nullable_to_non_nullable
          : thumbUrl as String?,
    );
  }
}

extension $EdpDocumentItemMediaInnerCopyWith on EdpDocumentItemMediaInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEdpDocumentItemMediaInner.copyWith(...)` or `instanceOfEdpDocumentItemMediaInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EdpDocumentItemMediaInnerCWProxy get copyWith =>
      _$EdpDocumentItemMediaInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EdpDocumentItemMediaInner _$EdpDocumentItemMediaInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('EdpDocumentItemMediaInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['url']);
  final val = EdpDocumentItemMediaInner(
    type: $checkedConvert('type', (v) => v as String?),
    url: $checkedConvert('url', (v) => v as String),
    thumbUrl: $checkedConvert('thumbUrl', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$EdpDocumentItemMediaInnerToJson(
  EdpDocumentItemMediaInner instance,
) => <String, dynamic>{
  'type': ?instance.type,
  'url': instance.url,
  'thumbUrl': ?instance.thumbUrl,
};
