// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'collab_attachment_image_meta.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CollabAttachmentImageMetaCWProxy {
  CollabAttachmentImageMeta width(int width);

  CollabAttachmentImageMeta height(int height);

  CollabAttachmentImageMeta exifStripped(bool exifStripped);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CollabAttachmentImageMeta(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CollabAttachmentImageMeta(...).copyWith(id: 12, name: "My name")
  /// ```
  CollabAttachmentImageMeta call({int width, int height, bool exifStripped});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfCollabAttachmentImageMeta.copyWith(...)` or call `instanceOfCollabAttachmentImageMeta.copyWith.fieldName(value)` for a single field.
class _$CollabAttachmentImageMetaCWProxyImpl
    implements _$CollabAttachmentImageMetaCWProxy {
  const _$CollabAttachmentImageMetaCWProxyImpl(this._value);

  final CollabAttachmentImageMeta _value;

  @override
  CollabAttachmentImageMeta width(int width) => call(width: width);

  @override
  CollabAttachmentImageMeta height(int height) => call(height: height);

  @override
  CollabAttachmentImageMeta exifStripped(bool exifStripped) =>
      call(exifStripped: exifStripped);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CollabAttachmentImageMeta(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CollabAttachmentImageMeta(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  CollabAttachmentImageMeta call({
    Object? width = const $CopyWithPlaceholder(),
    Object? height = const $CopyWithPlaceholder(),
    Object? exifStripped = const $CopyWithPlaceholder(),
  }) {
    return CollabAttachmentImageMeta(
      width: width == const $CopyWithPlaceholder() || width == null
          ? _value.width
          // ignore: cast_nullable_to_non_nullable
          : width as int,
      height: height == const $CopyWithPlaceholder() || height == null
          ? _value.height
          // ignore: cast_nullable_to_non_nullable
          : height as int,
      exifStripped:
          exifStripped == const $CopyWithPlaceholder() || exifStripped == null
          ? _value.exifStripped
          // ignore: cast_nullable_to_non_nullable
          : exifStripped as bool,
    );
  }
}

extension $CollabAttachmentImageMetaCopyWith on CollabAttachmentImageMeta {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfCollabAttachmentImageMeta.copyWith(...)` or `instanceOfCollabAttachmentImageMeta.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CollabAttachmentImageMetaCWProxy get copyWith =>
      _$CollabAttachmentImageMetaCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CollabAttachmentImageMeta _$CollabAttachmentImageMetaFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('CollabAttachmentImageMeta', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['width', 'height', 'exifStripped']);
  final val = CollabAttachmentImageMeta(
    width: $checkedConvert('width', (v) => (v as num).toInt()),
    height: $checkedConvert('height', (v) => (v as num).toInt()),
    exifStripped: $checkedConvert('exifStripped', (v) => v as bool),
  );
  return val;
});

Map<String, dynamic> _$CollabAttachmentImageMetaToJson(
  CollabAttachmentImageMeta instance,
) => <String, dynamic>{
  'width': instance.width,
  'height': instance.height,
  'exifStripped': instance.exifStripped,
};
