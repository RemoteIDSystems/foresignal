// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'media_links.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$MediaLinksCWProxy {
  MediaLinks self(String? self);

  MediaLinks meta(String? meta);

  MediaLinks thumbnail256(String? thumbnail256);

  MediaLinks thumbnail1024(String? thumbnail1024);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MediaLinks(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MediaLinks(...).copyWith(id: 12, name: "My name")
  /// ```
  MediaLinks call({
    String? self,
    String? meta,
    String? thumbnail256,
    String? thumbnail1024,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfMediaLinks.copyWith(...)` or call `instanceOfMediaLinks.copyWith.fieldName(value)` for a single field.
class _$MediaLinksCWProxyImpl implements _$MediaLinksCWProxy {
  const _$MediaLinksCWProxyImpl(this._value);

  final MediaLinks _value;

  @override
  MediaLinks self(String? self) => call(self: self);

  @override
  MediaLinks meta(String? meta) => call(meta: meta);

  @override
  MediaLinks thumbnail256(String? thumbnail256) =>
      call(thumbnail256: thumbnail256);

  @override
  MediaLinks thumbnail1024(String? thumbnail1024) =>
      call(thumbnail1024: thumbnail1024);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MediaLinks(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MediaLinks(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  MediaLinks call({
    Object? self = const $CopyWithPlaceholder(),
    Object? meta = const $CopyWithPlaceholder(),
    Object? thumbnail256 = const $CopyWithPlaceholder(),
    Object? thumbnail1024 = const $CopyWithPlaceholder(),
  }) {
    return MediaLinks(
      self: self == const $CopyWithPlaceholder()
          ? _value.self
          // ignore: cast_nullable_to_non_nullable
          : self as String?,
      meta: meta == const $CopyWithPlaceholder()
          ? _value.meta
          // ignore: cast_nullable_to_non_nullable
          : meta as String?,
      thumbnail256: thumbnail256 == const $CopyWithPlaceholder()
          ? _value.thumbnail256
          // ignore: cast_nullable_to_non_nullable
          : thumbnail256 as String?,
      thumbnail1024: thumbnail1024 == const $CopyWithPlaceholder()
          ? _value.thumbnail1024
          // ignore: cast_nullable_to_non_nullable
          : thumbnail1024 as String?,
    );
  }
}

extension $MediaLinksCopyWith on MediaLinks {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfMediaLinks.copyWith(...)` or `instanceOfMediaLinks.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$MediaLinksCWProxy get copyWith => _$MediaLinksCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

MediaLinks _$MediaLinksFromJson(Map<String, dynamic> json) =>
    $checkedCreate('MediaLinks', json, ($checkedConvert) {
      final val = MediaLinks(
        self: $checkedConvert('self', (v) => v as String?),
        meta: $checkedConvert('meta', (v) => v as String?),
        thumbnail256: $checkedConvert('thumbnail256', (v) => v as String?),
        thumbnail1024: $checkedConvert('thumbnail1024', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$MediaLinksToJson(MediaLinks instance) =>
    <String, dynamic>{
      'self': ?instance.self,
      'meta': ?instance.meta,
      'thumbnail256': ?instance.thumbnail256,
      'thumbnail1024': ?instance.thumbnail1024,
    };
