// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'collab_attachment_links.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CollabAttachmentLinksCWProxy {
  CollabAttachmentLinks content(String? content);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CollabAttachmentLinks(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CollabAttachmentLinks(...).copyWith(id: 12, name: "My name")
  /// ```
  CollabAttachmentLinks call({String? content});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfCollabAttachmentLinks.copyWith(...)` or call `instanceOfCollabAttachmentLinks.copyWith.fieldName(value)` for a single field.
class _$CollabAttachmentLinksCWProxyImpl
    implements _$CollabAttachmentLinksCWProxy {
  const _$CollabAttachmentLinksCWProxyImpl(this._value);

  final CollabAttachmentLinks _value;

  @override
  CollabAttachmentLinks content(String? content) => call(content: content);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CollabAttachmentLinks(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CollabAttachmentLinks(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  CollabAttachmentLinks call({Object? content = const $CopyWithPlaceholder()}) {
    return CollabAttachmentLinks(
      content: content == const $CopyWithPlaceholder()
          ? _value.content
          // ignore: cast_nullable_to_non_nullable
          : content as String?,
    );
  }
}

extension $CollabAttachmentLinksCopyWith on CollabAttachmentLinks {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfCollabAttachmentLinks.copyWith(...)` or `instanceOfCollabAttachmentLinks.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CollabAttachmentLinksCWProxy get copyWith =>
      _$CollabAttachmentLinksCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CollabAttachmentLinks _$CollabAttachmentLinksFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('CollabAttachmentLinks', json, ($checkedConvert) {
  final val = CollabAttachmentLinks(
    content: $checkedConvert('content', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$CollabAttachmentLinksToJson(
  CollabAttachmentLinks instance,
) => <String, dynamic>{'content': ?instance.content};
